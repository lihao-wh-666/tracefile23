package com.exam.controller;

import com.exam.common.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public Result<String> hello() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String info = "auth=" + authentication
                + ", isAuthenticated=" + (authentication != null ? authentication.isAuthenticated() : "null")
                + ", principal=" + (authentication != null ? authentication.getName() : "null")
                + ", authorities=" + (authentication != null ? authentication.getAuthorities() : "null");
        return Result.ok("Hello! " + info);
    }
}
