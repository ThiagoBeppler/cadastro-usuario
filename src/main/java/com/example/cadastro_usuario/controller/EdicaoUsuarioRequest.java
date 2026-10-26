package com.example.cadastro_usuario.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EdicaoUsuarioRequest(
        @NotBlank(message = "é obrigatório")
        @Size(max = 150, message = "deve ter no máximo {max} caracteres")
        String nome,

        @NotBlank(message = "é obrigatório")
        @Email(message = "e-mail inválido")
        @Size(max = 150, message = "deve ter no máximo {max} caracteres")
        String email,

        @Size(min = 6, max = 72, message = "deve ter entre {min} e {max} caracteres")
        String senha) {
}
