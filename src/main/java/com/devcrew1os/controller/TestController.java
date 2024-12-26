package com.devcrew1os.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api")
public class TestController {

    @GetMapping("/test")
    public String getTestString(@RequestParam String req) {
        return "Successfully receive request : " + req;
    }
}
