package com.example.vietstage_web_be.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String JWT_SECRET = "vietstage_secret_key_fixed_length_for_hmac_sha256_must_be_long_enough";
    private final Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());

    public String generateLoginToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000L)) // 24 giờ
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateResetPasswordToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 900000L))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromResetToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
