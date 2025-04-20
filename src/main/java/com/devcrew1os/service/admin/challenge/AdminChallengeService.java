package com.devcrew1os.service.admin.challenge;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.challenge.GetAdminChallengeData;
import com.devcrew1os.dto.admin.challenge.GetAdminChallengeRes;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminChallengeService {

    private final AdminChallengeTransaction transaction;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*===========================
       도전과제 목록조회
    ===========================*/
    public GetAdminChallengeRes getAdminChallenges(String adminId) {
        GetAdminChallengeRes res = new GetAdminChallengeRes(false, "[Info] Get admin challenges initiated", ErrorCode.OK);

        List<GetAdminChallengeData> data = getChallengeData(adminId, res);
        if(data == null) return res;

        res.setData(data);
        res.setSuccess(true);
        res.addMessage("[Info] Successfully retrieved challenges");
        logger.info("[AdminCategory][{}] Successfully retrieved challenges", adminId);
        return res;
    }

    private List<GetAdminChallengeData> getChallengeData(String adminId, GetAdminChallengeRes res) {
        try {
            List<GetAdminChallengeData> adminChallenges = transaction.getAdminChallengeList().stream()
                    .map(challenge -> new GetAdminChallengeData(
                            challenge.getId(),
                            challenge.getCategory().getTitle(),
                            challenge.getTerm(),
                            challenge.getDiff(),
                            challenge.getTodoCount(),
                            challenge.getStatus(),
                            challenge.getLastUpdatedAt(),
                            challenge.getLastUpdatedBy()
                    ))
                    .collect(Collectors.toList());
            logger.info("[AdminChallenge][{}] Successfully retrieved {} challenges", adminId, adminChallenges.size());
            return adminChallenges;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Error detected while retrieving admin challenges");
            logger.info("[AdminChallenge][{}] Failed to retrieved challenges", adminId);
            return null;
        }
    }
}
