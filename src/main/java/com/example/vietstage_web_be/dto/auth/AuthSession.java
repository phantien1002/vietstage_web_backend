package com.example.vietstage_web_be.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("auth:session")
public class AuthSession implements Serializable {

    @Id
    private String sessionId; // Sẽ tự map thành "auth:session:{sessionId}"
    
    private String email;
    private String role;
    
    // Hash của refresh token để verify khi client gửi refresh request
    private String refreshTokenHash;
    
    private String userAgent;
    private String ipAddress;

    @TimeToLive
    private Long expirationSeconds; // Redis sẽ tự động expire key này
}
