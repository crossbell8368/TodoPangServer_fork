package com.devcrew1os.controller.main;

import com.devcrew1os.dto.Response;
import com.devcrew1os.dto.main.home.HomeRankingRes;
import com.devcrew1os.dto.main.home.HomeRes;
import com.devcrew1os.service.main.home.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/main/home")
public class HomeController {

    private final Response<?> response;
    private final HomeService service;

    /*===========================
       최초 데이터 조회
    ===========================*/
    @PostMapping("/data")
    public ResponseEntity<?> getHome() {
        HomeRes res = service.getHomes(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }

    /*===========================
       도전과제 목록 업데이트
    ===========================*/
    @PostMapping("/ranking")
    public ResponseEntity<?> getRanking() {
        HomeRankingRes res = service.getHomeRanking(
                (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal()
        );
        return response.handleResult(res);
    }
}
