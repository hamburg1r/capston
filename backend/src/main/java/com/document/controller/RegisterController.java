package com.document.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.document.dto.UserRegisterRequestDto;

@RestController
@RequestMapping("/auth")
public class RegisterController {
//    private final UserService userService;
//    public RegisterController(UserService userService) {
//        this.userService = userService;
//    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserRegisterRequestDto request) {
        // Check already exists
//        if (userService.findByEmail(request.getEmail()) != null) {
//            return ResponseEntity
//                    .badRequest()
//                    .body("Email already registered");
//        }
//        // Create new user
//        UserModel savedUser = userService.register(
//                request.getUsername(),
//                request.getEmail(),
//                request.getPassword()
//        );
       // return ResponseEntity.ok("User registered successfully with ID: " + savedUser.getUserId());
    	return ResponseEntity.ok("User registered successfully with ID: "+request);
    }
}
