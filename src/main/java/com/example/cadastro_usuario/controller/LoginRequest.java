package com.example.cadastro_usuario.controller;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "é obrigatório") String email,
        @NotBlank(message = "é obrigatória") String senha) {
}
