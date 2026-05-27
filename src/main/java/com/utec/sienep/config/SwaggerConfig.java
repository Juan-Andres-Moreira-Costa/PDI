package com.utec.sienep.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SIENEP API")
                        .description("""
                                API RESTful del Sistema de Seguimiento Integral de Estudiantes
                                con Necesidades Educativas Personalizadas (SIENEP) — UTEC 2026.
                                
                                Entrega 1: Setup del proyecto, API REST inicial y CRUD base de Estudiantes.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo SIENEP — UTEC")
                                .email("sienep@utec.edu.uy"))
                        .license(new License()
                                .name("Uso académico — UTEC 2026")));
    }
}
