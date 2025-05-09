package com.devcrew1os.service.admin.auth;

import com.devcrew1os.common.enums.admin.AdminStatus;
import com.devcrew1os.dto.admin.auth.AdminSignupReq;
import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminAuthTransaction {

    private final AdminUserRepository adminRepo;

    private static final Logger logger = LoggerFactory.getLogger(AdminAuthTransaction.class);

    /*===========================
       회원가입
    ===========================*/
    @Transactional
    public void setAdminProcess(AdminSignupReq req) {
        LocalDateTime now = LocalDateTime.now();
        AdminUser newAdmin = AdminUser.builder()
                .userId(null)
                .name(req.getName())
                .email(req.getEmail())
                .status(AdminStatus.UNREGISTERED.getValue())
                .createdAt(now)
                .updatedAt(now)
                .deletedAt(null)
                .build();
        adminRepo.save(newAdmin);
    }

    /*===========================
       로그인
    ===========================*/
    @Transactional
    public AdminUser activateAdmin(String uid, String email) {
        AdminUser candidate = adminRepo.findByUserEmailAndStatus(email, AdminStatus.UNREGISTERED.getValue()).orElseThrow(
                () -> new RuntimeException("Candidate admin not found for email: " + email));

        if (candidate.getUserId() != null) {
            if (!candidate.getUserId().equals(uid)) {
                logger.error("[AdminUserTrans] CONFLICT! Email {} already activated with DIFFERENT UID {} (attempted with UID {})", email, candidate.getUserId(), uid);
                throw new IllegalStateException("Admin user [" + email + "] already activated with a different UID.");
            } else {
                logger.warn("[AdminUserTrans] Admin user [{}] already activated with UID {}. Proceeding as subsequent login.", email, uid);
                return candidate;
            }
        }
        candidate.setUserId(uid);
        candidate.setStatus(AdminStatus.REGISTERED.getValue());
        AdminUser activatedAdmin = adminRepo.save(candidate);
        logger.info("[AdminUserTrans] Activated admin user Email: {}, Firebase UID: {}", activatedAdmin.getEmail(), activatedAdmin.getUserId());
        return activatedAdmin;
    }

    /*===========================
       회원탈퇴
    ===========================*/
    @Transactional
    public void deleteAdminData(String adminId){
        AdminUser admin = adminRepo.findById(adminId).orElseThrow(
                () -> new RuntimeException("Request AdminId(" + adminId + ") not found")
      );
        adminRepo.delete(admin);
    }
}
