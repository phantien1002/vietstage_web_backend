package com.example.vietstage_web_be.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VietStage API")
                        .version("1.0")
                        .description("VietStage API Documentation"))
                // Định nghĩa thứ tự hiển thị của các tags trên Swagger UI
                .tags(List.of(
                        new Tag().name("Authentication").description("Các API liên quan đến Xác thực tài khoản"),
                        new Tag().name("Users").description("Các API liên quan đến Quản lý người dùng"),
                        new Tag().name("Lessons").description("Các API liên quan đến Quản lý bài học"),
                        new Tag().name("Instruments").description("Các API liên quan đến Quản lý nhạc cụ"),
                        new Tag().name("Techniques").description("Các API liên quan đến Quản lý kỹ thuật"),
                        new Tag().name("Admin").description("Các API quản trị hệ thống")
                ))
                // Thêm nút Authorize 🔒 trên Swagger UI
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token. Ví dụ: eyJhbGci...")));
    }
}

