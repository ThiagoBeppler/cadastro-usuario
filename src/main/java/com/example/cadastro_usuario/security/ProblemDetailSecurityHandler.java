package com.example.cadastro_usuario.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Responde 401 e 403 no formato ProblemDetail (RFC 7807), igual ao resto da API.
 * Os textos são fixos, então o JSON é montado à mão, sem dados do usuário.
 */
public class ProblemDetailSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setHeader("WWW-Authenticate", "Bearer");
        escrever(response, HttpServletResponse.SC_UNAUTHORIZED, "Não autenticado",
                "Token ausente, inválido ou expirado");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escrever(response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado",
                "Você não tem permissão para acessar este recurso");
    }

    private void escrever(HttpServletResponse response, int status, String titulo, String detalhe)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(
                "{\"type\":\"about:blank\",\"title\":\"" + titulo + "\",\"status\":" + status
                        + ",\"detail\":\"" + detalhe + "\"}");
    }
}
