package com.example.cadastro_usuario.security;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Usuários")
                        .version("0.0.1")
                        .description("Cadastro, edição e consulta de usuários. "
                                + "Faça login em /auth/login e informe o token no botão Authorize."))
                .components(new Components().addSecuritySchemes(ESQUEMA_BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
