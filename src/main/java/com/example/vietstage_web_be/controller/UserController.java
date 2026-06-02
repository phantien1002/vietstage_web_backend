package com.example.vietstage_web_be.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "APIs for user profile management")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<String> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok("Hello learner! Your email is: " + userDetails.getUsername() 
                + ", roles: " + userDetails.getAuthorities());
    }
}
