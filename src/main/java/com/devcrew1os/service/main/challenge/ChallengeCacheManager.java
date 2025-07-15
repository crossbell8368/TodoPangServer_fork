package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.DataStatus;
import com.devcrew1os.dto.main.challenge.ChallengeDetailData;
import com.devcrew1os.dto.main.challenge.ChallengeListInfo;
import com.devcrew1os.dto.main.challenge.ChallengeTodoData;
import com.devcrew1os.entity.challenge.Category;
import com.devcrew1os.entity.challenge.Challenge;
import com.devcrew1os.entity.challenge.Todo;
import com.devcrew1os.repository.challenge.CategoryRepository;
import com.devcrew1os.repository.challenge.ChallengeRepository;
import com.devcrew1os.repository.challenge.TodoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChallengeCacheManager {

    private final TodoRepository todoRepo;
    private final CategoryRepository categoryRepo;
    private final ChallengeRepository challengeRepo;

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Integer> integerRedisTemplate;

    @Value("${devcrew1os.redis.key.challenge.category}")
    private String categoryKey;
    @Value("${devcrew1os.redis.key.challenge.data.id}")
    private String challengeIdKey;
    @Value("${devcrew1os.redis.key.challenge.data}")
    private String challengeCardKey;
    @Value("${devcrew1os.redis.key.challenge.detail}")
    private String challengeDetailKey;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeCacheManager.class);

    @PostConstruct
    public void initManager(){
        logger.info("[ChallengeCacheManager] Initializing Challenge and Category cache at startup...");
        refreshAllData();
    }

    /*===========================
        전체 Cache 초기화
    ===========================*/
    public void refreshAllData() {
        try {
            // 1. fetch data from DB
            // category
            List<Category> categoryList = categoryRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
            if(categoryList.isEmpty()) {
                logger.error("[ChallengeCacheManager] Failed to search category data at db");
                return;
            }
            // challenge
            List<Challenge> challengeList = challengeRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());
            if(challengeList.isEmpty()) {
                logger.error("[ChallengeCacheManager] Failed to search challenges data at db");
                return;
            }
            List<Integer> challengeIds = challengeList.stream()
                    .map(Challenge::getId).collect(Collectors.toList());

            // todos
            List<Todo> todoList = todoRepo.findAllByChallengeIdsAndStatus(challengeIds, DataStatus.DEPLOYED.getValue());
            if(challengeIds.isEmpty()) {
                logger.error("[ChallengeCacheManager] Failed to search todos data at db");
                return;
            }
            Map<Integer, List<Todo>> todosByChallengeId = todoList.stream()
                    .collect(Collectors.groupingBy(todo -> todo.getChallenge().getId()));

            // 2. cleanup & prepare cache
            cleanupCache();
            ListOperations<String, Integer> listOps = integerRedisTemplate.opsForList();
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();

            // 3. struct DTO & set cache
            // category
            Map<Integer, String> categoryMap = categoryList.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getTitle));
            String jsonCategory = objectMapper.writeValueAsString(categoryMap);
            valueOps.set(categoryKey, jsonCategory);

            // challengeIds
            listOps.rightPushAll(challengeIdKey, challengeIds);

            // challenge(Card)
            for(Challenge entity : challengeList) {
                ChallengeListInfo challengeCard = new ChallengeListInfo(
                        entity.getId(),
                        entity.getCategory().getId(),
                        entity.getTitle(),
                        entity.getDiff(),
                        entity.getStat().getParticipateUserCount()
                );
                String cardKey = challengeCardKey + entity.getId();
                String jsonCardData = objectMapper.writeValueAsString(challengeCard);
                valueOps.set(cardKey, jsonCardData);

                // challenge(Detail)
                List<Todo> relatedTodos = todosByChallengeId.getOrDefault(entity.getId(), Collections.emptyList());
                List<ChallengeTodoData> todoDtoList = relatedTodos.stream()
                        .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                        .collect(Collectors.toList());

                ChallengeDetailData detailData = new ChallengeDetailData(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getStat().getParticipateUserCount(),
                        entity.getCategory().getId(),
                        entity.getTerm(),
                        entity.getDiff(),
                        todoDtoList,
                        new ArrayList<>()
                );
                String detailKey = challengeDetailKey + entity.getId();
                String jsonDetailData = objectMapper.writeValueAsString(detailData);
                valueOps.set(detailKey, jsonDetailData);
            }
        }  catch (JsonProcessingException err) {
            logger.error("[ChallengeCacheManager] Failed to convert DTO to JSON", err);
        } catch (Exception err) {
            logger.error("[ChallengeCacheManager] Failed to upload Category & Challenge data to cache", err);
        }
    }

    /*===========================
        전체 Cache 무효화
    ===========================*/
    public void cleanupCache() {
        logger.info("[ChallengeCacheManager] Initiating cache cleanup for all challenge data...");
        try {
            // 1. get challengeIds from cache
            ListOperations<String, Integer> listOps = integerRedisTemplate.opsForList();
            List<Integer> previousChallengeIds = listOps.range(challengeIdKey, 0, -1);

            // 2. collect truncate target key
            List<String> keysToDelete = new ArrayList<>();
            keysToDelete.add(categoryKey);
            keysToDelete.add(challengeIdKey);
            if (previousChallengeIds != null && !previousChallengeIds.isEmpty()) {
                previousChallengeIds.forEach(id -> keysToDelete.add(challengeCardKey + id));
                previousChallengeIds.forEach(id -> keysToDelete.add(challengeDetailKey + id));
            }
            // 3. truncate cache
            if (!keysToDelete.isEmpty()) {
                Long deletedCount = stringRedisTemplate.delete(keysToDelete);
                logger.info("[ChallengeCacheManager] Cache cleanup complete. Deleted {} keys.", deletedCount);
            } else {
                logger.info("[ChallengeCacheManager] No cache keys found to delete.");
            }
        } catch (Exception err) {
            logger.error("[ChallengeCacheManager]An error occurred during cache cleanup.", err);
        }
    }

    /*===========================
        Category 초기화
    ===========================*/
    public void refreshCategory() {
        try {
            // 1. truncate cache
            stringRedisTemplate.delete(categoryKey);

            // 2. fetch data from DB
            List<Category> categoryList = categoryRepo.findAllByStatusDesc(DataStatus.DEPLOYED.getValue());

            // 3. update cache
            if(categoryList != null && !categoryList.isEmpty()) {
                Map<Integer, String> categoryMap = categoryList.stream()
                        .collect(Collectors.toMap(Category::getId, Category::getTitle));

                String jsonCategory = objectMapper.writeValueAsString(categoryMap);
                ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
                valueOps.set(categoryKey, jsonCategory);
                logger.info("[ChallengeCacheManager] Category cache has been refreshed with {} items.", categoryMap.size());
            } else {
                logger.info("[ChallengeCacheManager] No deployed categories found in DB. Cache remains empty.");
            }

        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed to update category at cache", err);
        }
    }

    public void refreshCategory(Map<Integer, String> categoryMap) {
        try {
            // 1. truncate cache
            stringRedisTemplate.delete(categoryKey);

            // 2. update cache
            String jsonCategory = objectMapper.writeValueAsString(categoryMap);
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            valueOps.set(categoryKey, jsonCategory);
            logger.info("[ChallengeCacheManager] Category cache has been refreshed with {} items.", categoryMap.size());

        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed to update category at cache", err);
        }
    }

    /*===========================
        Challenge 데이터 추가
    ===========================*/
    public void addChallengeData(List<Integer> challengeId) {
        try {
            // 1. fetch data from db
            // challenge
            List<Challenge> challengeList = challengeRepo.findAllByChallengeIdsAndStatus(challengeId, DataStatus.DEPLOYED.getValue());
            if(challengeList.isEmpty()) {
                logger.error("[ChallengeCacheManager] Failed to search challenge data at db");
                return;
            }

            // todos
            List<Integer> challengeIds = challengeList.stream()
                    .map(Challenge::getId).collect(Collectors.toList());
            List<Todo> todoList = todoRepo.findAllByChallengeIdsAndStatus(challengeIds, DataStatus.DEPLOYED.getValue());
            if(challengeIds.isEmpty()) {
                logger.error("[ChallengeCacheManager] Failed to search todo data at db");
                return;
            }
            Map<Integer, List<Todo>> todosByChallengeId = todoList.stream()
                    .collect(Collectors.groupingBy(todo -> todo.getChallenge().getId()));

            // 2. struct dto & add/update to cache
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            // challengeCard
            for(Challenge entity : challengeList) {
                ChallengeListInfo challengeCard = new ChallengeListInfo(
                        entity.getId(),
                        entity.getCategory().getId(),
                        entity.getTitle(),
                        entity.getDiff(),
                        entity.getStat().getParticipateUserCount()
                );
                String cardKey = challengeCardKey + entity.getId();
                String jsonCardData = objectMapper.writeValueAsString(challengeCard);
                valueOps.set(cardKey, jsonCardData);

                // challengeDetail
                List<Todo> relatedTodos = todosByChallengeId.getOrDefault(entity.getId(), Collections.emptyList());
                List<ChallengeTodoData> todoDtoList = relatedTodos.stream()
                        .map(todo -> new ChallengeTodoData(todo.getId(), todo.getDesc()))
                        .collect(Collectors.toList());
                ChallengeDetailData detailData = new ChallengeDetailData(
                        entity.getId(),
                        entity.getTitle(),
                        entity.getStat().getParticipateUserCount(),
                        entity.getCategory().getId(),
                        entity.getTerm(),
                        entity.getDiff(),
                        todoDtoList,
                        new ArrayList<>()
                );
                String detailKey = challengeDetailKey + entity.getId();
                String jsonDetailData = objectMapper.writeValueAsString(detailData);
                valueOps.set(detailKey, jsonDetailData);
            }
            // challengeIds
            refreshChallengeIds();

        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed add '{}' challenge data at cache", challengeId, err);
        }
    }

    /*===========================
        Challenge 데이터 제거
    ===========================*/
    public void deleteChallenges(List<Integer> deletedChallengeIds) {
        try {
            // 1. update challengeIds cache
            List<Integer> prevChallengeIds = integerRedisTemplate.opsForList().range(challengeIdKey, 0, -1);
            if(prevChallengeIds != null && !prevChallengeIds.isEmpty()) {
                List<Integer> updatedChallengeIds = prevChallengeIds.stream()
                        .filter(id -> !deletedChallengeIds.contains(id))
                        .collect(Collectors.toList());
                refreshChallengeIds();
            } else {
                logger.error("[ChallengeCacheManager] Failed to get previous ChallengeIdsKeys.");
            }

            // 2. update challenge cache
            List<String> keysToDelete = new ArrayList<>();
            if (deletedChallengeIds != null && !deletedChallengeIds.isEmpty()) {
                deletedChallengeIds.forEach(id -> keysToDelete.add(challengeCardKey + id));
                deletedChallengeIds.forEach(id -> keysToDelete.add(challengeDetailKey + id));
            }
            if(!keysToDelete.isEmpty()) {
                Long deletedCount = stringRedisTemplate.delete(keysToDelete);
                logger.info("[ChallengeCacheManager] Successfully deleted {} ChallengeKeys.", deletedCount);
            } else {
                logger.info("[ChallengeCacheManager] No ChallengeKeys to delete.");
            }
        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed to delete ChallengeKeys.", err);
        }
    }

    /*===========================
        Challenge 데이터 초기화
    ===========================*/
    public void refreshChallengeCard(List<ChallengeListInfo> dtoList) {

        // 1. check args
        if (dtoList == null || dtoList.isEmpty()) {
            logger.warn("[ChallengeCacheManager] Received an empty list to refresh card cache. No action taken.");
            return;
        }
        try {
            // 2. struct dto
            Map<String, String> valuesToSet = new HashMap<>();
            for (ChallengeListInfo dto : dtoList) {
                String cardKey = challengeCardKey + dto.getId();
                String jsonCardData = objectMapper.writeValueAsString(dto);
                valuesToSet.put(cardKey, jsonCardData);
            }
            // 2. update cache
            stringRedisTemplate.opsForValue().multiSet(valuesToSet);
            logger.info("[ChallengeCacheManager] Successfully refresh '{}' challengeCard at cache", dtoList.size());

        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed to refresh challengeCard at cache", err);
        }
    }

    public void refreshChallengeDetails(ChallengeDetailData dto) {

        // 1. check args
        if(dto == null) {
            logger.warn("[ChallengeCacheManager] Received an empty object to refresh detail cache. No action taken.");
            return;
        }
        try {
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            String detailKey = challengeDetailKey + dto.getChallengeId();
            String jsonDetailData = objectMapper.writeValueAsString(dto);
            valueOps.set(detailKey, jsonDetailData);
            logger.info("[ChallengeCacheManager] Successfully refresh '{}' challengeDetail at cache", dto.getChallengeId());

        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed to refresh challengeDetail at cache", err);
        }
    }

    /*===========================
        ChallengeIds 초기화
    ===========================*/
    public void refreshChallengeIds() {
        try {
            // 1. truncate cache
            stringRedisTemplate.delete(challengeIdKey);

            // 2. fetch Ids from DB
            List<Integer> updatedChallengeIds = challengeRepo.findAllIdsByStatus(DataStatus.DEPLOYED.getValue());

            // 2. update cache
            ListOperations<String, Integer> listOps = integerRedisTemplate.opsForList();
            listOps.rightPushAll(challengeIdKey, updatedChallengeIds);
            logger.info("[ChallengeCacheManager] ChallengeIds cache has been refreshed with {} items.", updatedChallengeIds.size());

        } catch(Exception err) {
            logger.error("[ChallengeCacheManager] Failed to update ChallengeIds cache.", err);
        }
    }
}
