package com.devcrew1os.service.admin.auth;

import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthTransaction {

    private final AdminUserRepository adminRepo;
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthTransaction.class);

    /*===========================
       회원가입
    ===========================*/
    @Transactional
    public void saveAdminData(AdminUser newAdmin) {
        try {
            adminRepo.save(newAdmin);
        } catch (DataAccessException err){
            logger.info("[AdminAuthTrans][{}] Failed to save new admin data: {}", newAdmin.getId(), err.getMessage());
            throw err;
        }
    }

    /*===========================
       회원탈퇴
    ===========================*/
    @Transactional
    public void deleteAdminData(String adminId){
        AdminUser admin = adminRepo.findById(adminId)
                .orElseThrow(() -> {
                    logger.info("[AdminAuthTrans][{}] Admin not found", adminId);
                    return new RuntimeException("Admin not found");
                });

        adminRepo.delete(admin);
        logger.info("[AdminAuthTrans][{}] Successfully deleted admin", adminId);
    }
}
