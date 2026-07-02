package com.bookingengine.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Booking Engine API")
                        .version("1.0")
                        .description("""
                                API de reservas com controle de concorrência transacional.
                                Garante que N tentativas simultâneas de reservar o mesmo slot
                                resultem em exatamente uma reserva confirmada (HTTP 201) e
                                N-1 conflitos (HTTP 409).
                                """));
    }
}
