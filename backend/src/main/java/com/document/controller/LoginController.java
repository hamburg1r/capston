package com.document.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.document.dto.UserLoginRequestDto;

@RestController
@RequestMapping("/auth")
public class LoginController {
//    private final UserService userService;
//    private final JwtUtil jwtUtil;
//    public LoginController(UserService userService, JwtUtil jwtUtil) {
//        this.userService = userService;
//        this.jwtUtil = jwtUtil;
//    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto req) {
        // Find user by email
//        UserModel user = userService.findByEmail(req.getEmail());
//        if (user == null) {
//            return ResponseEntity.status(401).body("User not found");
//        }
//        // Check password
//        boolean validPassword = userService.checkPassword(user, req.getPassword());
//        if (!validPassword) {
//            return ResponseEntity.status(401).body("Invalid password");
//        }
//        // Generate JWT token
//        String token = jwtUtil.generateToken(user.getUserId(), 24 * 60 * 60 * 1000); // 24 hours
//        // Return token + userId
//        LoginResponse response = new LoginResponse(token, user.getUserId());
//        return ResponseEntity.ok(response);
    	return ResponseEntity.ok("data in coming "+req);
    }
}

