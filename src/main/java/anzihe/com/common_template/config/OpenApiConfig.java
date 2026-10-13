package anzihe.com.common_template.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档信息配置。
 * 原先由 knife4j 的 knife4j.openapi.title/version 提供，
 * 替换为 springdoc 后改用 OpenAPI Bean 声明。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI commonTemplateOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("common-template")
                        .version("1.0")
                        .description("通用后台模板"));
    }
}
