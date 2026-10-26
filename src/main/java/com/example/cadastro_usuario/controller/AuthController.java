package com.example.cadastro_usuario.controller;

import com.example.cadastro_usuario.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e emissão do token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica e devolve o token JWT",
            description = "Rota pública. Use o token no botão Authorize, com o esquema Bearer.")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(responseCode = "400", description = "Campos obrigatórios ausentes",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication autenticacao = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.senha()));
        String token = tokenService.gerarToken(autenticacao.getName());
        return new TokenResponse(token, "Bearer", tokenService.getExpiracaoEmSegundos());
    }
}
