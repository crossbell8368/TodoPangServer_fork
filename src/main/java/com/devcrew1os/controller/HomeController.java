package com.devcrew1os.controller;

import com.devcrew1os.common.util.Response;
import com.devcrew1os.dto.home.HomeReq;
import com.devcrew1os.dto.home.HomeRes;
import com.devcrew1os.service.home.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final Response<?> response;
    private final HomeService homeService;

    @PostMapping("/data")
    public ResponseEntity<?> getHome(
            @RequestBody HomeReq req
    ) {
        HomeRes res = homeService.getHomes(req);
        return response.handleResult(res);
    }
}
