package com.devcrew1os.service.main.home;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.main.home.HomeChallengeData;
import com.devcrew1os.dto.main.home.HomeData;
import com.devcrew1os.dto.main.home.HomeUserData;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.entity.user.Users;
import com.devcrew1os.repository.challenge.ChallengeStatRepository;
import com.devcrew1os.repository.users.UsersRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeTransaction {

    private final UsersRepository userRepo;
    private final ChallengeStatRepository statRepo;

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final ReentrantLock cacheLock = new ReentrantLock();

    @Value("${devcrew1os.redis.key.home.ranking.id}")
    private String rankingIdKey;
    @Value("${devcrew1os.redis.key.home.ranking.data}")
    private String rankingDataKey;

    private static final Logger logger = LoggerFactory.getLogger(HomeTransaction.class);

    /*===========================
       홈화면 데이터 조회
    ===========================*/
    @Transactional(readOnly = true)
    public HomeData getHomeData(String userId) {
        // 1. fetch data: user
        Users user = userRepo.findByUserId(userId).orElseThrow(
                () -> new RuntimeException("User not found with id " + userId)
        );

        // 1-2. struct data: user
        HomeUserData userData = new HomeUserData(
                user.getUserName(),
                user.getStat().getServiceTerm(),
                user.getStat().getCompletedChallenges()
        );

        // 2-1. fetch data from cache: challenge
        List<HomeChallengeData> challengeList = getHomeRanking(userId);
        return new HomeData(userData, challengeList);
    }

    /*===========================
       홈화면 순위 조회
    ===========================*/
    @Transactional(readOnly = true)
    public List<HomeChallengeData> getHomeRanking(String userId) {
        // 1. fetch data from cache
        List<HomeChallengeData> challengeListFromCache = getChallengeDataFromCache(userId);
        if (!challengeListFromCache.isEmpty()) {
            return challengeListFromCache;
        }

        // 2. fetch data from db: cache miss
        cacheLock.lock();
        try {
            // 2-1. check cache again
            challengeListFromCache = getChallengeDataFromCache(userId);
            if (!challengeListFromCache.isEmpty()) {
                return challengeListFromCache;
            }
            // 3. get data from db
            logger.warn("[HomeTrans][{}] Cache is still empty after acquiring lock. Fetching from DB...", userId);
            return getChallengeDataFromDB(userId);
        } finally {
            cacheLock.unlock();
        }
    }

    /*===========================
       유틸리티
    ===========================*/
    // Func: Get challenges from Redis
    private List<HomeChallengeData> getChallengeDataFromCache(String userId) {
        // 1. get challenge ranking id
        ZSetOperations<String, String> challengeIdZSetOps = redisTemplate.opsForZSet();
        Set<String> challengeIds = challengeIdZSetOps.reverseRange(rankingIdKey, 0, 9);
        if (challengeIds == null || challengeIds.isEmpty()) {
            logger.error("[HomeTrans][{}] Failed to get challenge rankingIds", userId);
            return Collections.emptyList();
        }
        // 2-1. struct challenge data key
        List<String> challengeDataKey = challengeIds.stream()
                .map(id -> rankingDataKey + id)
                .collect(Collectors.toList());

        // 2-2. fetch challenge data: json
        List<String> jsonChallengeDataList = redisTemplate.opsForValue().multiGet(challengeDataKey);
        if(jsonChallengeDataList == null || jsonChallengeDataList.isEmpty()){
            logger.error("[HomeTrans][{}] Failed to get json challenge data", userId);
            return Collections.emptyList();
        }
        // 2-3. convert challenge data: to dto
        return jsonChallengeDataList.stream()
                .filter(Objects::nonNull)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, HomeChallengeData.class);
                    } catch(JsonProcessingException err) {
                        logger.error("[HomeTrans][{}] Failed to covert json data to dto", userId, err);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // Func: Get challenges from DB
    private List<HomeChallengeData> getChallengeDataFromDB(String userId) {
        // 1. get challenge data from db
        Pageable top10 = PageRequest.of(0, 10);
        List<ChallengeStat> statList = statRepo.findTop10ByPopularityWithDetail(DataStatus.DEPLOYED.getValue(), top10);
        if (statList.isEmpty()) {
            throw new RuntimeException("Challenge list is empty");
        }

        // 2. struct dto
        List<HomeChallengeData> challengeListFromDB = statList.stream()
                .map(stat -> HomeChallengeData.builder()
                        .challengeId(stat.getChallenge().getId())
                        .title(stat.getChallenge().getTitle())
                        .category(stat.getChallenge().getCategory().getId())
                        .diff(stat.getChallenge().getDiff())
                        .popularity(stat.getParticipateUserCount())
                        .build()
                ).collect(Collectors.toList());

        // 3. reset cache
        try {
            resetChallengeDataAtCache(challengeListFromDB, userId);
        } catch(JsonProcessingException err) {
            logger.error("[HomeTrans][{}] Failed to reset cache during DB fallback.", userId, err);
        }
        return challengeListFromDB;
    }

    // Func: Reset challenges at Redis
    private void resetChallengeDataAtCache(List<HomeChallengeData> challengeList, String userId) throws JsonProcessingException {

        // truncate current cache
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        cleanupPopularChallenges(zSetOps, userId);

        // convert dto to json
        for (HomeChallengeData dto : challengeList) {
            String challengeIdStr = String.valueOf(dto.getChallengeId());
            String jsonDTO = objectMapper.writeValueAsString(dto);

            // set new json challenges to cache
            zSetOps.add(rankingIdKey, challengeIdStr, dto.getPopularity());
            valueOps.set(rankingDataKey + challengeIdStr, jsonDTO, Duration.ofHours(2));
        }
        redisTemplate.expire(rankingIdKey, Duration.ofHours(2));
        logger.info("[HomeTrans][{}] Successfully seeded Redis cache with {} items.", userId, challengeList.size());
    }

    // Func: cleanup Redis
    private void cleanupPopularChallenges(ZSetOperations<String, String> zSetOps, String userId) {
        // 1. get previous Id
        Set<String> previousChallengeIds = zSetOps.range(rankingIdKey, 0, -1);

        // 2.struct data Key
        if(previousChallengeIds != null && !previousChallengeIds.isEmpty()) {
            List<String> previousDataKey = previousChallengeIds.stream()
                    .map(id -> rankingDataKey + id)
                    .collect(Collectors.toList());

            // 3. reset redis
            redisTemplate.delete(rankingIdKey);
            redisTemplate.delete(previousDataKey);
        } else {
            logger.error("[HomeTrans][{}] No previous challenge ranking data found to clean up. Proceeding...", userId);
        }
    }
}
