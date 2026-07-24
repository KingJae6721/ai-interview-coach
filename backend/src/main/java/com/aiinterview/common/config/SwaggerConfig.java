package com.aiinterview.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("AI Interview Coach API")
                .version("v1")
                .description("AI Interview Coach Backend API");

        Server localServer = new Server()
                .url("http://localhost:8081")
                .description("Local Server");

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
