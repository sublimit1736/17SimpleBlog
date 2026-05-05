package cn.chunana.simblog17api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 元数据与安全方案配置。
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI simBlogOpenApi() {
        return new OpenAPI()
                .info(new Info()
                              .title("SimBlog17 接口文档")
                              .description("SimBlog17 后端 REST API 文档")
                              .version("v1")
                              .contact(new Contact().name("SimBlog17 开发团队"))
                              .license(new License().name("内部使用")))
                .addSecurityItem(new SecurityRequirement().addList("basicAuth"))
                .schemaRequirement("basicAuth", new SecurityScheme()
                        .name("basicAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("basic"));
    }
}

