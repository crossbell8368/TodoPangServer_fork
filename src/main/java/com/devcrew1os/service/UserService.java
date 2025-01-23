package com.devcrew1os.service;

import com.devcrew1os.common.enums.ErrorCode;
import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.common.util.Result;
import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.entity.Users;
import com.devcrew1os.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /*===========================
       신규회원
    ===========================*/
    public Result signup(UserReqDTO.Signup dto) {

        try {
            // 1. validate request
            validateSignupRequest(dto);

            // 2. check userId
            if (checkUserId(dto.getUserId())) {
                logger.warn("[UserService] Signup attempt with duplicate userId: {}", dto.getUserId());
                return Result.failure("Already exists UserId", ErrorCode.DUPLICATE_USER);
            }
            // 3. create user
            Users newUser = createUser(dto);

            // 4. apply data
            userRepo.save(newUser);

            // 5. result
            logger.info("[UserService] Successfully created new user: {}", newUser.getUserId());
            return Result.success("Successfully registered new user");

            // Exceptions
        } catch (IllegalArgumentException e) {
            logger.error("[UserService] Invalid argument detected during request: {}", e.getMessage());
            return Result.failure("Invalid argument detected during request", ErrorCode.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("[UserService] Unexpected error during signup: {}", e.getMessage());
            return Result.failure("Unexpected error during signup", ErrorCode.INTERNAL_ERROR);
        }
    }

    private void validateSignupRequest(UserReqDTO.Signup dto) {
        if (dto.getUserId() == null || dto.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        if (dto.getName() == null || dto.getName().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty");
        }
        if (dto.getSocialType() == null) {
            throw new IllegalArgumentException("Social Type must not be null");
        }
    }

    private boolean checkUserId(String userId) {
        return userRepo.existsByUserId(userId);
    }

    private Users createUser(UserReqDTO.Signup dto) {
        LocalDateTime now = LocalDateTime.now();
        Users newUser = Users.builder()
                .userId(dto.getUserId())
                .userEmail(dto.getEmail())
                .userName(dto.getName())
                .userSocialType(UserSocialType.fromValue(dto.getSocialType()))
                .userStatus(UserStatus.ACTIVE)
                .userCreatedAt(now)
                .userUpdatedAt(now)
                .userDeletedAt(now)
                .userLastLoginAt(now)
                .userLastLogoutAt(now)
                .build();
        return newUser;
    }

    /*===========================
       회원탈퇴
    ===========================*/
    public Result withdraw(UserReqDTO.Withdraw dto) {
        try {
            // 1. validate request
            validateWithdrawRequest(dto);

            // 2. delete data from local
            withdrawUserFromLocal(dto.getUserId());

            // 3. delete data from Auth
            withdrawUserFromFirebase(dto.getUserId());

            // 4. result
            return Result.success("Successfully withdraw user");

            // Exceptions
        } catch (IllegalArgumentException e)  {
            logger.error("[UserService] Failed to withdraw userData from local: {}", e.getMessage());
            return Result.failure("Invalid argument detected during withdraw", ErrorCode.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("[UserService] Failed to withdraw userData from Auth: {}", e.getMessage());
            return Result.failure("Failed to delete uid from Auth", ErrorCode.FIREBASE_ERROR);
        } catch (Exception e) {
            logger.error("[UserService] Unexpected error during withdraw: {}", e.getMessage());
            return Result.failure("Unexpected error during withdraw", ErrorCode.INTERNAL_ERROR);
        }
    }

    private void validateWithdrawRequest(UserReqDTO.Withdraw dto) {
        if (dto.getUserId() == null || dto.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID must not be null or empty");
        }
        if (dto.getWithdrawReason() == null) {
            throw new IllegalArgumentException("Withdraw reason must not be null");
        }
    }

    private void withdrawUserFromLocal(String userId) {
        // 1. database check
        Users user = userRepo.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Not exist userId: " + userId));

        // 2. update status
        user.updateUserStatus(UserStatus.DELETED);
        userRepo.save(user);

        // 3. result
        logger.info("[UserService] Successfully withdraw user: {}", userId);
    }

    private void withdrawUserFromFirebase(String userId) {
        try {
            // 1. Auth process
            FirebaseAuth.getInstance().deleteUser(userId);

            // 2. result
            logger.info("[UserService] Successfully withdraw user from Firebase: {}", userId);

            // Exceptions
        } catch (FirebaseAuthException err) {
            throw new RuntimeException("Failed to withdraw user from Firebase: " + err.getMessage(), err);
        }
    }
}

