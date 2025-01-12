package com.devcrew1os.service;

import com.devcrew1os.common.enums.UserSocialType;
import com.devcrew1os.common.enums.UserStatus;
import com.devcrew1os.common.util.Response;
import com.devcrew1os.dto.user.UserReqDTO;
import com.devcrew1os.entity.Users;
import com.devcrew1os.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    @Autowired
    private Response<?> response;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    /*===========================
       신규회원
    ===========================*/
    public ResponseEntity<?> signup(UserReqDTO.Signup dto) {

        try {
            // 1. 입력값 검증
            validateSignupRequest(dto);

            // 2. userId 중복검사
            if (userRepo.existsByUserId(dto.getUserId())) {
                logger.warn("[UserService] Signup attempt with duplicate userId: {}", dto.getUserId());
                return response.fail("[Response] Already exists UserId");
            }

            // 3. 신규 회원 객체 생성
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

            // 4. 데이터 저장
            userRepo.save(newUser);

            // 5. 응답 생성
            logger.info("[UserService] Successfully created new user: {}", newUser.getUserId());
            return response.success("[Response] Successfully signed up new user");

        } catch (IllegalArgumentException e) {
            logger.error("[UserService] Invalid signup request: {}", e.getMessage());
            return response.fail("[Response] Invalid signup request", HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("[UserService] Unexpected error during signup: {}", e.getMessage());
            return response.fail("[Response] An unexpected error occurred during signup", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /*===========================
       입력값 검증 메서드
    ===========================*/
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
}
