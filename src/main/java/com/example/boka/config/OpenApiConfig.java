package com.example.boka.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Boka API",
                description = "Gym class booking platform — REST API",
                version = "1.0.0"
        )
)
public class OpenApiConfig {}
