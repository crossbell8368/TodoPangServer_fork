package com.devcrew1os.config;

import java.util.List;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .addSchemas("ResponseBody", createResponseBodySchema())
                        .addSchemas("ResponseResult", createResponseResultSchema())
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("Todopang Server API")
                .version("1.0")
                .description("Todopang Server API documentation");
    }

    private Schema<?> createResponseBodySchema() {
        Schema<Object> responseBody = new Schema<>();
        responseBody.setName("ResponseBody");
        responseBody.addProperty("status", new Schema<Integer>().example(200).description("HTTP 상태 코드"));
        responseBody.addProperty("result", new Schema<String>().example("SUCCESS").description("응답 결과 상태 (SUCCESS or FAIL)"));
        responseBody.addProperty("message", new Schema<String>().example("요청이 성공적으로 처리되었습니다."));
        responseBody.addProperty("data", new Schema<Object>().description("실제 데이터 객체"));
        return responseBody;
    }

    private Schema<?> createResponseResultSchema() {
        Schema<String> responseResult = new Schema<>();
        responseResult.setName("ResponseResult");
        responseResult.setType("string");
        responseResult.setEnum(List.of("SUCCESS", "FAIL"));
        responseResult.setDescription("응답 결과 상태");
        return responseResult;
    }
}
