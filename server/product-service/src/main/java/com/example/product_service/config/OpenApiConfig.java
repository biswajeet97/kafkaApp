package com.example.product_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI productServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Product Service API")
                .description("API for managing products, stock, and inventory reservations.")
                .version("v1")
                .contact(new Contact().name("Product Service Team"))
                .license(new License().name("Proprietary")));
    }
}
