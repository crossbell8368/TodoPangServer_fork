package com.devcrew1os.service.admin.users;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.dto.admin.auth.AdminLoginRes;
import com.devcrew1os.dto.admin.auth.AdminSignupReq;
import com.devcrew1os.dto.admin.auth.AdminSignupRes;
import com.devcrew1os.dto.admin.auth.AdminWithdrawRes;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserRepository adminRepo;
    private final AdminAuthTransaction adminTrans;

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);

    /*===========================
       관리자 회원가입
    ===========================*/
    public AdminSignupRes signup(String adminId, AdminSignupReq req) {
        LocalDateTime now = LocalDateTime.now();
        AdminSignupRes res = new AdminSignupRes(false, "[Info] Admin Signup initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        if(!isAdminIdExist(adminId, res)) return res;

        AdminUser newAdmin = createAdmin(adminId, req, now);
        try {
            adminTrans.saveAdminData(newAdmin);
        } catch (Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Database error detected, at save new admin");
            logger.error("[AdminAuth][{}] Database error detected, at save new admin: {}", adminId, res.getMessage());
            return res;
        }
        res.setSuccess(true);
        res.addMessage("[Info] Admin Signup finish");
        logger.info("[AdminAuth][{}] Admin Signup successful", adminId);
        return res;
    }

    private boolean isRequestValid(String adminId, AdminSignupReq req, AdminSignupRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getEmail() == null || req.getEmail().isEmpty()) {
            errors.add("[Failed] Email must not be null or empty");
        }
        if (req.getName() == null || req.getName().isEmpty()) {
            errors.add("[Failed] Name must not be null or empty");
        }
        if (req.getSocialType() == null || !UserSocialType.contains(req.getSocialType())) {
            errors.add("[Failed] SocialType must not be null or must be specified value");
        }
        if (!errors.isEmpty()) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage(String.join("\n", errors));
            logger.error("[AdminAuth][{}] Invalid argument detected, at admin signup: {}", adminId, res.getMessage());
            return false;
        }
        res.addMessage("[Success] Valid Signup request");
        logger.info("[AdminAuth][{}] Valid Signup request", adminId);
        return true;
    }

    private boolean isAdminIdExist(String adminId, AdminSignupRes res){
        if(adminRepo.existsAdminUserByUserId(adminId)){
            res.setErrorCode(ErrorCode.DUPLICATE_USER);
            res.addMessage("[Failed] Request AdminID already exists");
            logger.warn("[AuthService][{}] Request AdminID already exists, at Signup", adminId);
            return false;
        }
        res.addMessage("[Success] Valid AdminID");
        logger.info("[AuthService][{}] Valid AdminID, at Signup", adminId);
        return true;
    }

    private AdminUser createAdmin(String adminId, AdminSignupReq req, LocalDateTime now) {
        return AdminUser.builder()
                .userId(adminId)
                .userEmail(req.getEmail())
                .userName(req.getName())
                .socialType(req.getSocialType())
                .status(UserStatus.ACTIVE.getValue())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();
    }

    /*===========================
       관리자 로그인
    ===========================*/
    public AdminLoginRes login(String adminId) {
        logger.info("[AdminAuth][{}] Admin Login finish", adminId);
        return new AdminLoginRes(true, "[Info] Admin Login finish", ErrorCode.OK);
    }

    public AdminWithdrawRes withdraw(String adminId) {
        AdminWithdrawRes res = new AdminWithdrawRes(false, "[Info] Admin Withdraw initiated", ErrorCode.OK);
        try {
            adminTrans.deleteAdminData(adminId);
            res.setSuccess(true);
            res.addMessage("[Success] Admin Withdraw finish");
            logger.info("[AdminAuth][{}] Admin Withdraw successfully finish", adminId);
        } catch (Exception err) {
            if (err.getMessage().contains("not found")) {
                res.setErrorCode(ErrorCode.USER_NOT_FOUND);
                res.addMessage("[Failed] Admin not found");
            } else {
                res.setErrorCode(ErrorCode.DATABASE_ERROR);
                res.addMessage("[Failed] An error occurred while deleting admin");
            }
            logger.error("[AdminAuth][{}] Admin Withdraw failed: {}", adminId, err.getMessage());
        }
        return res;
    }
}
