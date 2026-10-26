package com.example.cadastro_usuario.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public void enviar(String destinatario, String assunto, String corpo) {
        System.out.printf("[E-MAIL ENVIADO] para=%s | assunto=%s | corpo=%s%n", destinatario, assunto, corpo);
    }
}
