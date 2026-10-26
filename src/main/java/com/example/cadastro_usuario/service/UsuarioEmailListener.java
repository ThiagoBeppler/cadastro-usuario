package com.example.cadastro_usuario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class UsuarioEmailListener {

    private static final Logger log = LoggerFactory.getLogger(UsuarioEmailListener.class);

    private final EmailService emailService;

    public UsuarioEmailListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoSalvarUsuario(UsuarioSalvoEvent event) {
        try {
            emailService.enviar(event.email(), assunto(event), corpo(event));
        } catch (RuntimeException ex) {
            log.error("Falha ao enviar e-mail para o usuário {}; o cadastro foi mantido", event.id(), ex);
        }
    }

    private String assunto(UsuarioSalvoEvent event) {
        return switch (event.operacao()) {
            case CADASTRO -> "Bem-vindo(a)!";
            case EDICAO -> "Seus dados foram atualizados";
        };
    }

    private String corpo(UsuarioSalvoEvent event) {
        return switch (event.operacao()) {
            case CADASTRO -> "Olá, " + event.nome() + ". Seu cadastro foi realizado com sucesso.";
            case EDICAO -> "Olá, " + event.nome() + ". Seus dados cadastrais foram atualizados.";
        };
    }
}
