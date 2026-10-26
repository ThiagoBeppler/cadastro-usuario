package com.example.cadastro_usuario.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EdicaoUsuarioRequest(
        @Schema(description = "Nome completo", example = "Maria Souza")
        @NotBlank(message = "é obrigatório")
        @Size(max = 150, message = "deve ter no máximo {max} caracteres")
        String nome,

        @Schema(description = "E-mail, único no sistema", example = "maria@exemplo.com")
        @NotBlank(message = "é obrigatório")
        @Email(message = "e-mail inválido")
        @Size(max = 150, message = "deve ter no máximo {max} caracteres")
        String email,

        @Schema(description = "Nova senha de 6 a 72 caracteres. Se omitida, a senha atual é mantida.",
                example = "novaSenha123", nullable = true)
        @Size(min = 6, max = 72, message = "deve ter entre {min} e {max} caracteres")
        String senha) {
}
