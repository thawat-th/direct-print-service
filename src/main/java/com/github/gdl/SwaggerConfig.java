package com.github.gdl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/v1/**")
                .build();
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Direct print service")
                        .description("Direct print to client local printer API Documentation")
                        .version("v0.0.1")
                        .license(new License().name("Apache License Version 2.0").url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
