package com.devcrew1os.service.main.home;


import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.main.home.HomeChallengeData;
import com.devcrew1os.entity.challenge.ChallengeStat;
import com.devcrew1os.repository.challenge.ChallengeStatRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HomeScheduler {

    private final ChallengeStatRepository challengeRepo;

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    @Value("${devcrew1os.redis.key.home.ranking.id}")
    private String rankingIdKey;
    @Value("${devcrew1os.redis.key.home.ranking.data}")
    private String rankingDataKey;

    private static final Logger logger = LoggerFactory.getLogger(HomeScheduler.class);


    @Scheduled(initialDelay = 5000, fixedRate = 3600000)
    public void updatePopularChallenges() {
        // 1. Remove previous ranking data
        cleanupPopularChallenges();
        try {
            // 2. Get top10 challenges, based on popularity
            Pageable top10 = PageRequest.of(0, 10);
            List<ChallengeStat> topChallenges = challengeRepo.findTop10ByPopularityWithDetail(DataStatus.DEPLOYED.getValue(), top10);

            // 3. Reset redis
            ZSetOperations<String, String> challengeIdZSetOps = redisTemplate.opsForZSet();
            ValueOperations<String, String> challengeValueOps = redisTemplate.opsForValue();

            for(ChallengeStat challengeStat : topChallenges) {

                // 3-1. save challengeId: for sort
                challengeIdZSetOps.add(rankingIdKey, String.valueOf(challengeStat.getChallenge().getId()), challengeStat.getParticipateUserCount());

                // 3-2. convert challengeData
                HomeChallengeData dto = HomeChallengeData.builder()
                        .challengeId(challengeStat.getChallenge().getId())
                        .title(challengeStat.getChallenge().getTitle())
                        .category(challengeStat.getChallenge().getCategory().getId())
                        .diff(challengeStat.getChallenge().getDiff())
                        .popularity(challengeStat.getParticipateUserCount())
                        .build();
                String jsonDTO = objectMapper.writeValueAsString(dto);

                // 3-3. save challengeData
                String dataKey = rankingDataKey + challengeStat.getChallenge().getId();
                challengeValueOps.set(dataKey, jsonDTO, Duration.ofHours(2));
            }
            redisTemplate.expire(rankingIdKey, Duration.ofHours(2));
            logger.info("[HomeScheduler][Batch] Successfully update '{}' challenges at cache", topChallenges.size());

        } catch (JsonProcessingException err) {
            logger.error("[HomeScheduler] Error detected while convert challenge data to json.", err);
        } catch (Exception err) {
            logger.error("[HomeScheduler] Error detected while updating popular challenges.", err);
        }
    }

    private void cleanupPopularChallenges() {
        ZSetOperations<String, String> challengeIdZSetOps = redisTemplate.opsForZSet();

        // 1. get previous Id
        Set<String> previousChallengeIds = challengeIdZSetOps.range(rankingIdKey, 0, -1);

        // 2.struct data Key
        if(previousChallengeIds != null && !previousChallengeIds.isEmpty()) {
            List<String> previousDataKey = previousChallengeIds.stream()
                    .map(id -> rankingDataKey + id)
                    .collect(Collectors.toList());

            // 3. reset redis
            redisTemplate.delete(rankingIdKey);
            redisTemplate.delete(previousDataKey);

        } else {
            logger.error("[HomeScheduler] No previous challenge ranking data found to clean up. Proceeding...");
        }
    }
}
