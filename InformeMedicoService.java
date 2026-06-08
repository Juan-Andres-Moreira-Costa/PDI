package com.utec.sienep.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // Esquema de seguridad: Bearer Token (JWT)
        SecurityScheme bearerScheme = new SecurityScheme()
                .name("bearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Ingresá el token JWT obtenido en POST /api/v1/auth/login");

        return new OpenAPI()
                .info(new Info()
                        .title("SIENEP API")
                        .description("""
                                API RESTful del Sistema de Seguimiento Integral de Estudiantes
                                con Necesidades Educativas Personalizadas (SIENEP) — UTEC 2026.

                                **Entrega 2:** Autenticación JWT, seguridad con Spring Security,
                                CRUD completo de Estudiantes, Informes Médicos e Instancias.

                                **Cómo autenticarse:**
                                1. Ejecutar POST /api/v1/auth/login con usuario y contraseña.
                                2. Copiar el token de la respuesta.
                                3. Hacer clic en el botón Authorize (🔒) e ingresar el token.
                                """)
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Equipo SIENEP — UTEC")
                                .email("sienep@utec.edu.uy"))
                        .license(new License().name("Uso académico — UTEC 2026")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", bearerScheme))
                // Aplica autenticación por defecto a todos los endpoints
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
