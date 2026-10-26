package com.example.cadastro_usuario.service;

import com.example.cadastro_usuario.service.UsuarioSalvoEvent.Operacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioEmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UsuarioEmailListener listener;

    @Test
    void deveEnviarEmailParaOUsuarioQuandoReceberEventoDeCadastro() {
        // given
        UsuarioSalvoEvent evento = new UsuarioSalvoEvent(1L, "Maria", "maria@teste.com", Operacao.CADASTRO);

        // when
        listener.aoSalvarUsuario(evento);

        // then
        verify(emailService).enviar(eq("maria@teste.com"), anyString(), contains("Maria"));
    }

    @Test
    void deveNaoPropagarExcecaoQuandoEnvioDeEmailFalhar() {
        // given
        UsuarioSalvoEvent evento = new UsuarioSalvoEvent(1L, "Maria", "maria@teste.com", Operacao.EDICAO);
        doThrow(new RuntimeException("servidor de e-mail fora do ar"))
                .when(emailService).enviar(anyString(), anyString(), anyString());

        // when / then
        assertThatCode(() -> listener.aoSalvarUsuario(evento)).doesNotThrowAnyException();
    }
}
