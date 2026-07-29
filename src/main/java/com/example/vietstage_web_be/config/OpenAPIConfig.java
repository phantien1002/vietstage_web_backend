package com.example.vietstage_web_be.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.Comparator;
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
                .tags(List.of(
                        new Tag().name("Admin").description("Các API quản trị hệ thống"),
                        new Tag().name("Authentication").description("Các API liên quan đến Xác thực tài khoản"),
                        new Tag().name("Users").description("Các API liên quan đến tài khoản cá nhân"),
                        new Tag().name("Lesson").description("Các API quản lý Bài học"),
                        new Tag().name("Instruments").description("Các API quản lý Nhạc cụ"),
                        new Tag().name("Techniques").description("Các API quản lý Kỹ thuật nhạc cụ"),
                        new Tag().name("Skill Levels").description("Các API quản lý Trình độ bài học")
                ))
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

    @Bean
    public OpenApiCustomizer sortTagsCustomizer() {
        return openApi -> {
            List<String> order = List.of(
                    "Admin",
                    "Authentication",
                    "Users",
                    "Lesson",
                    "Instruments",
                    "Techniques",
                    "Skill Levels"
            );
            
            if (openApi.getTags() != null) {
                openApi.getTags().sort(Comparator.comparingInt(tag -> {
                    int index = order.indexOf(tag.getName());
                    return index == -1 ? Integer.MAX_VALUE : index;
                }));
            }
            
            if (openApi.getPaths() != null) {
                io.swagger.v3.oas.models.Paths sortedPaths = new io.swagger.v3.oas.models.Paths();
                openApi.getPaths().entrySet().stream()
                        .sorted(java.util.Map.Entry.comparingByKey())
                        .forEach(entry -> sortedPaths.addPathItem(entry.getKey(), entry.getValue()));
                openApi.setPaths(sortedPaths);
            }
        };
    }
}

