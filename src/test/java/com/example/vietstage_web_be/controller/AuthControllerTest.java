package com.example.vietstage_web_be.controller;

import com.example.vietstage_web_be.dto.BaseResponse;
import com.example.vietstage_web_be.dto.request.LoginRequest;
import com.example.vietstage_web_be.dto.request.RegisterRequest;
import com.example.vietstage_web_be.dto.response.AuthResponse;
import com.example.vietstage_web_be.service.IAuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    @Mock
    private IAuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@gmail.com");
        req.setPassword("password123");
        req.setFullName("Test User");

        AuthResponse mockResp = AuthResponse.builder().message("Register successfully!").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockResp);

        ResponseEntity<BaseResponse<AuthResponse>> response = authController.register(req);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Register successfully", response.getBody().getMessage());
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@gmail.com");
        req.setPassword("password123");

        AuthResponse mockResp = AuthResponse.builder()
                .message("Login successfully")
                .token("access_token_mock")
                .refreshToken("refresh_token_mock")
                .build();
                
        when(authService.login(any(LoginRequest.class))).thenReturn(mockResp);

        ResponseEntity<BaseResponse<AuthResponse>> response = authController.login(req);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("access_token_mock", response.getBody().getData().getToken());
    }
}
