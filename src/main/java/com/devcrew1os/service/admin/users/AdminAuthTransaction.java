package com.devcrew1os.service.admin.users;

import com.devcrew1os.entity.admin.AdminUser;
import com.devcrew1os.repository.admin.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAuthTransaction {

    private final AdminUserRepository adminRepo;
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthTransaction.class);

    @Transactional
    public void saveAdminData(AdminUser newAdmin) {
        try {
            adminRepo.save(newAdmin);
        } catch (DataAccessException err){
            logger.info("[AdminAuthTrans][{}] Failed to save new admin data: {}", newAdmin.getUserId(), err.getMessage());
            throw err;
        }
    }

    @Transactional
    public void deleteAdminData(String adminId){
        AdminUser admin = adminRepo.findByUserId(adminId)
                .orElseThrow(() -> {
                    logger.info("[AdminAuthTrans][{}] Admin not found", adminId);
                    return new RuntimeException("Admin not found");
                });

        adminRepo.delete(admin);
        logger.info("[AdminAuthTrans][{}] Successfully deleted admin", adminId);
    }
}
