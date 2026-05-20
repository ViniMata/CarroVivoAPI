package com.carrovivo.api.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Carro Vivo API")
                        .description("API REST para gerenciamento inteligente de veículos — diagnóstico de peças, manutenções, garantias e concessionárias.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Carro Vivo")
                                .email("contato@carrovivo.com")));
    }
}