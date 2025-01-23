package com.devcrew1os.config;

import java.util.List;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todopang Server API")
                        .version("1.0")
                        .description("Todopang Server API documentation")
                );
    }

    @Bean
    public OpenApiCustomiser customApiResponses() {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) -> {
                pathItem.readOperations().forEach(operation -> {
                    // auth
                    if (path.startsWith("/auth")) {
                        configureAuthResponses(path, operation);
                    }
                });
            });
        };
    }

    /*===========================
       Auth & User
    ===========================*/
    private void configureAuthResponses(String path, Operation operation) {
        ApiResponses responses = operation.getResponses();

        // 1. Login
        if (path.contains("/login")) {
            responses.addApiResponse("200", createApiResponse(
                    "로그인 성공",
                    "{"
                            + "\"status\":200,"
                            + "\"result\":\"SUCCESS\","
                            + "\"message\":\"Successfully Logged in!\","
                            + "\"data\": true"
                            + "}"
            ));
            responses.addApiResponse("401", createApiResponse(
                    "인증 실패",
                    "{"
                            + "\"status\":401,"
                            + "\"result\":\"FAIL\","
                            + "\"message\":\"Provided IdToken UID mismatched\","
                            + "\"data\": false"
                            + "}"
            ));
            responses.addApiResponse("500", createApiResponse(
                    "서버 오류",
                    "{"
                            + "\"status\":500,"
                            + "\"result\":\"FAIL\","
                            + "\"message\":\"Internal server error\","
                            + "\"data\": false"
                            + "}"
            ));

            // 2. Signup
        } else if (path.contains("/signup")) {
            responses.addApiResponse("200", createApiResponse(
                    "회원가입 성공",
                    "{"
                            + "\"status\":200,"
                            + "\"result\":\"SUCCESS\","
                            + "\"message\":\"Successfully signed up new user\","
                            + "\"data\": true"
                            + "}"
            ));
            responses.addApiResponse("400", createApiResponse(
                    "잘못된 요청 데이터",
                    "{"
                            + "\"status\":400,"
                            + "\"result\":\"FAIL\","
                            + "\"message\":\"Invalid signup request\","
                            + "\"data\": false"
                            + "}"
            ));
            responses.addApiResponse("409", createApiResponse(
                    "중복된 요청 데이터",
                    "{"
                            + "\"status\":409,"
                            + "\"result\":\"FAIL\","
                            + "\"message\":\"User already exists\","
                            + "\"data\": false"
                            + "}"
            ));
            responses.addApiResponse("500", createApiResponse(
                    "서버 오류",
                    "{"
                            + "\"status\":500,"
                            + "\"result\":\"FAIL\","
                            + "\"message\":\"Internal server error\","
                            + "\"data\":false"
                            + "}"
            ));
        }
    }

    /*===========================
       Util
    ===========================*/
    private ApiResponse createApiResponse(String description, String example) {
        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        MediaType.APPLICATION_JSON_VALUE,
                        new io.swagger.v3.oas.models.media.MediaType().example(example))
                );
    }
}
