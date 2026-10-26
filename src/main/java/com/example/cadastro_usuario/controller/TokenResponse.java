package com.example.cadastro_usuario.controller;

public record TokenResponse(String token, String tipo, long expiraEm) {
}
