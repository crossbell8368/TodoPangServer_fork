package com.devcrew1os.service.admin.auth;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.dto.admin.auth.AdminLoginRes;
import com.devcrew1os.dto.admin.auth.AdminSignupReq;
import com.devcrew1os.dto.admin.auth.AdminSignupRes;
import com.devcrew1os.dto.admin.auth.AdminWithdrawRes;
import com.devcrew1os.dto.main.auth.LoginDTO;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminAuthTransaction transaction;
    private final AdminUserRepository adminRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthService.class);

    /*===========================
       관리자 회원가입
    ===========================*/
    public AdminSignupRes signup(String adminId, AdminSignupReq req) {
        AdminSignupRes res = new AdminSignupRes(false, "[Info] Admin Signup initiated", ErrorCode.OK);

        if(!isRequestValid(adminId, req, res)) return res;

        try {
            transaction.setAdminProcess(req);
            res.setSuccess(true);
            res.addMessage("[Info] Admin Signup complete");
            logger.info("[AdminAuth][{}] Admin Signup complete", adminId);
            return res;

        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.DUPLICATE_USER);
            res.addMessage("[Failed] Request AdminID already exists");
            logger.warn("[AdminAuth][{}] Request AdminID already exists, at Signup", adminId);
            return res;

        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] Database error detected, at set new admin");
            logger.error("[AdminAuth][{}] Database error detected, at set new admin: {}", adminId, res.getMessage());
            return res;
        }
    }

    private boolean isRequestValid(String adminId, AdminSignupReq req, AdminSignupRes res) {
        List<String> errors = new ArrayList<>();

        if (req.getName() == null || req.getName().isEmpty()) {
            errors.add("[Failed] Name must not be null or empty");
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

    /*===========================
       관리자 로그인
    ===========================*/
    public AdminLoginRes login(String adminId, LoginDTO dto) {
        AdminLoginRes res = new AdminLoginRes(false, "[Info] Admin Login initiated", ErrorCode.OK);

        try {
            Optional<AdminUser> adminUser = adminRepo.findByUserId(adminId);
            if(adminUser.isPresent()) {
                res.setSuccess(true);
                res.addMessage("[Info] Admin Login complete");
                logger.info("[AdminAuth][{}] Admin Login complete", adminId);
                return res;
            } else {
                AdminUser activateAdmin = transaction.activateAdmin(adminId, dto.getEmail());
                if(activateAdmin == null) {
                    res.addMessage("[Failed] Admin activate & Login failed");
                    logger.error("[AdminAuth][{}] Admin activate & Login failed", adminId);
                    return res;
                }
                res.setSuccess(true);
                res.addMessage("[Info] Admin Activate & Login complete");
                logger.info("[AdminAuth][{}] Admin Activate & Login complete", adminId);
                return res;
            }
        } catch(RuntimeException err) {
            res.setErrorCode(ErrorCode.BAD_REQUEST);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminAuth][{}] {}}", adminId, err.getMessage());
            return res;
        } catch(Exception err) {
            res.setErrorCode(ErrorCode.DATABASE_ERROR);
            res.addMessage("[Failed] " + err.getMessage());
            logger.error("[AdminAuth][{}] {}}", adminId, err.getMessage());
            return res;
        }
    }

    /*===========================
       관리자 회원탈퇴
    ===========================*/
    public AdminWithdrawRes withdraw(String adminId) {
        AdminWithdrawRes res = new AdminWithdrawRes(false, "[Info] Admin Withdraw initiated", ErrorCode.OK);
        try {
            transaction.deleteAdminData(adminId);
            res.setSuccess(true);
            res.addMessage("[Success] Admin Withdraw finish");
            logger.info("[AdminAuth][{}] Admin Withdraw successfully finish", adminId);
            return res;

        } catch (Exception err) {
            if (err.getMessage().contains("not found")) {
                res.setErrorCode(ErrorCode.USER_NOT_FOUND);
                res.addMessage("[Failed] Admin not found");
            } else {
                res.setErrorCode(ErrorCode.DATABASE_ERROR);
                res.addMessage("[Failed] An error occurred while deleting admin");
            }
            logger.error("[AdminAuth][{}] Admin Withdraw failed: {}", adminId, err.getMessage());
            return res;
        }
    }
}
