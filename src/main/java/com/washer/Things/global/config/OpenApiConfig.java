package com.washer.Things.global.config;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.gsm-washer.com", description = "Production Server"),
                @Server(url = "http://localhost:8080", description = "Local Server"),
                @Server(url = "/", description = "Default Server")
        }
)
@Configuration
public class OpenApiConfig {
}