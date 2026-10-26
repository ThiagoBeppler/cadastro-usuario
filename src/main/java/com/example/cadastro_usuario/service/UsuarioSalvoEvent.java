package com.example.cadastro_usuario.service;

public record UsuarioSalvoEvent(Long id, String nome, String email, Operacao operacao) {

    public enum Operacao {
        CADASTRO,
        EDICAO
    }
}
