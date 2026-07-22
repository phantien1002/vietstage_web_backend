package com.example.vietstage_web_be;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class BCryptGen {
    public static void main(String[] args) {
        System.out.println("BCRYPT_OUTPUT: " + new BCryptPasswordEncoder().encode("123456"));
    }
}
