package com.devcrew1os.service.main.home;

import com.devcrew1os.dto.main.home.HomeReq;
import com.devcrew1os.entity.main.user.UserInfo;
import com.devcrew1os.repository.main.users.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeTransaction {

    private final UserInfoRepository userRepo;

    public UserInfo getUserInfo(HomeReq req) {
        return userRepo.findByUserId(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id " + req.getUserId()));
    }
}
