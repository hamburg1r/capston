package com.document.controller;



import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController

public class TestController {

    @GetMapping("/test/auth-check")
    public String checkAuth() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return "Token Verified Successfully! UserId = " + principal;
    }
    @GetMapping("/health")
    public String help() {
    	return "aya hai"; 
    }
}
