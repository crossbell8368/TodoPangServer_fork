package com.devcrew1os.service.main.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.main.challenge.GetChallengesData;
import com.devcrew1os.dto.main.challenge.GetChallengesInfo;
import com.devcrew1os.dto.main.challenge.GetChallengesReq;
import com.devcrew1os.dto.main.challenge.GetChallengesRes;
import com.devcrew1os.entity.challenge.ChallengeCategory;
import com.devcrew1os.entity.challenge.ChallengeInfo;
import com.devcrew1os.repository.UserRepository;
import com.devcrew1os.repository.challenge.ChallengeCategoryRepository;
import com.devcrew1os.repository.challenge.ChallengeInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeCategoryRepository categoryRepo;
    private final ChallengeInfoRepository infoRepo;
    private final UserRepository userRepo;

    private static final Logger logger = LoggerFactory.getLogger(ChallengeService.class);

    /*===========================
       도전과제 목록조회
    ===========================*/
    public GetChallengesRes getChallenges(GetChallengesReq req) {
        GetChallengesRes res = new GetChallengesRes(false, false, "[Info] Get challenges initiated", ErrorCode.OK);

        if(!isRequestValid(req, res)) return res;

        if(!isUserIdExist(req, res)) return res;

        GetChallengesData data = getChallengesData(req, res);
        if (data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully get challenges");
        logger.info("[ChallengeService][{}] Successfully get challenges", req.getUserId());
        return res;
    }

    // Request 검증
    private boolean isRequestValid(GetChallengesReq req, GetChallengesRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getUserId() == null || req.getUserId().isEmpty()) {
            errors.add("[Failed] UserID must not be null or empty");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[ChallengeService][{}] Invalid argument detected during GetChallenges: {}", req.getUserId(), res.getMessage());
            return false;
        }
        res.addMessage("[Success] GetChallengeList request is valid");
        logger.info("[ChallengeService][{}] GetChallengeList request is valid", req.getUserId());
        return true;
    }

    // UserId 검증
    private boolean isUserIdExist(GetChallengesReq req, GetChallengesRes res) {
        if (userRepo.existsByUserId(req.getUserId())) {
            res.addMessage("[Success] User(Info) exists");
            logger.info("[ChallengeService][{}] User exists, at get challenges", req.getUserId());
            return true;
        }
        res.setErrorCode(ErrorCode.USER_NOT_FOUND);
        res.addMessage("[Failed] User(Info) not found");
        logger.warn("[ChallengeService][{}] User not found, at get challenges", req.getUserId());
        return false;
    }

    // Response data fetch
    private GetChallengesData getChallengesData(GetChallengesReq req, GetChallengesRes res) {

        Map<Integer, String> categories = getCategories(req);
        if(categories == null) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] During get challenge categories, database error detected");
            return null;
        }

        List<GetChallengesInfo> infos = getInfos(req);
        if(infos == null || infos.isEmpty()) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] During get challenge data, database error detected");
            return null;
        }

        res.addMessage("[Info] Successfully get challenge categories & infos");
        return new GetChallengesData(categories, infos);
    }

    // Category 데이터 fetch
    private Map<Integer, String> getCategories(GetChallengesReq req) {

        Map<Integer, String> categoryMap = new HashMap<>();
        try {
            List<ChallengeCategory> categoryList = categoryRepo.findAllByOrderByIdDesc();
            for(ChallengeCategory category : categoryList) {
                categoryMap.put(category.getId(), category.getTitle());
            }
            logger.info("[ChallengeService][{}] Successfully retrieved {} categories", req.getUserId(), categoryList.size());
            return categoryMap;

        } catch (Exception err) {
            logger.error("[ChallengeService][{}] Failed to search challenge category data", req.getUserId());
            return null;
        }
    }

    // Info 데이터 fetch
    private List<GetChallengesInfo> getInfos(GetChallengesReq req) {

        List<GetChallengesInfo> infoDTOList = new ArrayList<>();
        try {
            List<ChallengeInfo> infoEntityList = infoRepo.findAllByOrderByIdDesc();
            for(ChallengeInfo entity : infoEntityList) {
                infoDTOList.add(
                        new GetChallengesInfo(
                                entity.getId(),
                                entity.getCategory().getId(),
                                entity.getTitle(),
                                entity.getDiff(),
                                entity.getStat().getPopularity()
                        )
                );
            };
            logger.info("[ChallengeService][{}] Successfully retrieved {} challenge data", req.getUserId(), infoDTOList.size());
            return infoDTOList;

        } catch (Exception err) {
            logger.error("[ChallengeService][{}] Failed to search challenge data", req.getUserId());
            return null;
        }
    }
}
