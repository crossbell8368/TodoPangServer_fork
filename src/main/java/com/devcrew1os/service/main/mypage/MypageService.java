package com.devcrew1os.service.main.mypage;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.users.UserStatus;
import com.devcrew1os.common.enums.users.UserWithdrawReason;
import com.devcrew1os.dto.main.mypage.*;
import com.devcrew1os.entity.user.Users;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class MypageService {

    private final MypageAsync authAsync;
    private final MypageTransaction transaction;

    private static final Logger logger = LoggerFactory.getLogger(MypageService.class);

    /*===========================
       계정이름 변경
    ===========================*/
    public UpdateUserNameRes updateUserName(String userId, UpdateUserNameReq req) {
        UpdateUserNameRes res = new UpdateUserNameRes(false, "[Info] Update userName initiated", ErrorCode.OK);
        try {
            if(!isValidRequest(userId, req,res )) return res;

            if(!transaction.renameUserProcess(userId, req.getNewUserName())) {
                res.setErrorCode(ErrorCode.BAD_REQUEST);
                res.addMessage("[Failed] Recent update exist");
                logger.warn("[MypageService][{}] UserName update failed, recent update exist", userId);
                return res;
            }
            res.setSuccess(true);
            res.addMessage("[Info] Update userName finish");
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DATA_NOT_FOUND);
            res.addMessage("[Failed] User not found");
            logger.warn("[MypageService][{}] User not found, at update userName", userId);
            return res;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to update userName");
            logger.error("[MypageService][{}] Failed to update userName: {}", userId, err.getMessage());
            return res;
        }
    }

    private boolean isValidRequest(String userId, UpdateUserNameReq req, UpdateUserNameRes res) {
        if(req.getNewUserName() == null || req.getNewUserName().trim().isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Failed] New user name is empty");
            logger.error("[MypageService][{}] New user name is empty]", userId);
            return false;
        } else {
            return true;
        }
    }

    /*===========================
       사용자 의견등록
    ===========================*/
    public AddUserOpinionRes addUserOpinion(String userId, AddUserOpinionReq req) {
        AddUserOpinionRes res = new AddUserOpinionRes(false, "[Info] Add user opinion initiated", ErrorCode.OK);

        try {
            if(!transaction.addUserOpinionProcess(userId, req)){
                res.setErrorCode(ErrorCode.BAD_REQUEST);
                res.addMessage("[Failed] Recent add user opinion exist");
                return res;
            }
            res.setSuccess(true);
            res.addMessage("[Info] Add user opinion finish");
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Failed to Add user opinion");
            logger.error("[MypageService][{}] Failed to Add user opinion: {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       탈퇴사유 조회
    ===========================*/
    public GetWithdrawReasonRes getWithdrawReason(String userId) {
        GetWithdrawReasonRes res = new GetWithdrawReasonRes(false, "[Info] Get withdraw reason initiated", ErrorCode.OK);
        try {
            List<GetWithdrawReasonData> data = transaction.getWithdrawReason();
            res.setData(data);
            res.setSuccess(true);
            res.addMessage("[Info] Get withdraw reason finish");
            return res;

        } catch(RuntimeException err) {
            res.addMessage("[Failed] Failed to fetch user withdraw reason");
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            logger.error("[AuthService][{}] Failed to fetch user withdraw reason: {}", userId, err.getMessage());
            return res;

        } catch(Exception err) {
            res.addMessage("[Failed] Failed to get user withdraw reason");
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            logger.error("[AuthService][{}] Failed to get user withdraw reason: {}", userId, err.getMessage());
            return res;
        }
    }

    /*===========================
       회원탈퇴
    ===========================*/
    public WithdrawRes withdraw(String userId, WithdrawReq req) {
        LocalDateTime now = LocalDateTime.now();
        WithdrawRes res = new WithdrawRes(false, "[Info] Withdraw initiated", ErrorCode.OK);

        // 1. 입력값 검증
        if(!isRequestValid(userId, req, res)) return res;

        // 2. 업데이트 데이터 준비
        Users data = getWithdrawData(userId, req, res, now);
        if(data == null) return res;

        // 3. Firebase & Database 업데이트
        if(!isWithdrawSuccess(data, res))return res;

        res.setSuccess(true);
        res.addMessage("[Info] Withdraw successful");
        logger.info("[AuthService][{}] Withdraw successful", userId);
        return res;
    }

    private boolean isRequestValid(String userId, WithdrawReq req, WithdrawRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getReasonId() == null) {
            errors.add("[Failed] Reason must not be null");
        } else {
            if (!UserWithdrawReason.contains(req.getReasonId())) {
                errors.add("[Failed] Invalid reason");
            }
        }

        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AuthService][{}] Invalid argument detected, while withdrawReq: {}", userId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Withdraw request");
        logger.info("[AuthService][{}] Valid Withdraw request", userId);
        return true;
    }

    private Users getWithdrawData(String userId, WithdrawReq req, WithdrawRes res, LocalDateTime now) {
        try {
            Users user = transaction.getUsersByUserId(userId);

            user.setStatus(UserStatus.DELETED.getValue());
            user.setWithdrawReason(req.getReasonId());
            user.setWithdrawAt(now);
            return user;

        } catch (Exception err) {
            res.setErrorCode(ErrorCode.USER_NOT_FOUND);
            res.addMessage("[Failed] User not found");
            logger.error("[AuthService][{}] User not found at withdraw process: {}", userId, err.getMessage());
            return null;
        }
    }

    private boolean isWithdrawSuccess(Users user, WithdrawRes res) {
        boolean status = false;
        StringBuilder firebaseMsg = new StringBuilder();
        StringBuilder databaseMsg = new StringBuilder();

        try {
            CompletableFuture<Boolean> firebaseFuture = authAsync.isUserDeletedFromFirebase(user.getUserId(), firebaseMsg);
            CompletableFuture<Boolean> databaseFuture = authAsync.isUserDeletedFromDatabase(user, databaseMsg);
            CompletableFuture.allOf(firebaseFuture, databaseFuture).join();

            Boolean firebaseResult = firebaseFuture.get();
            Boolean databaseResult = databaseFuture.get();

            if(!firebaseResult && !databaseResult) {
                res.setErrorCode(ErrorCode.INTERNAL_ERROR);
                res.addMessage("[Failed] Firebase & database error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase & database: {} \n {}", user.getUserId(), firebaseMsg.toString(), databaseMsg.toString());
            } else if (!firebaseResult) {
                res.setErrorCode(ErrorCode.FIREBASE_ERROR);
                res.addMessage("[Failed] Firebase error detected");
                logger.error("[AuthService][{}] Failed to delete user from firebase: {}", user.getUserId(), firebaseMsg.toString());
            } else if (!databaseResult) {
                res.setErrorCode(ErrorCode.DATABASE_ERROR);
                res.addMessage("[Failed] Database error detected");
                logger.error("[AuthService][{}] Failed to delete user from database: {}", user.getUserId(), databaseMsg.toString());
            } else {
                status = true;
                res.addMessage("[Info] User data deleted");
                logger.info("[AuthService][{}] Withdraw user data deleted: \n {} \n {}", user.getUserId(), firebaseMsg.toString(), databaseMsg.toString());
            }
            return status;

        } catch (Exception err) {
            res.addMessage("[Failed] Delete process exception");
            res.setErrorCode(ErrorCode.INTERNAL_ERROR);
            logger.error("[AuthService][{}] Withdraw delete process exception: {} \n {} \n {}", user.getUserId(), err.getMessage(), firebaseMsg.toString(), databaseMsg.toString());
            return false;
        }
    }
}
