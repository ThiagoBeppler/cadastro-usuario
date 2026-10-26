package com.example.cadastro_usuario.service;

import com.example.cadastro_usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
class UsuarioEmailTransacaoTest {

    @Autowired
    private UsuarioService service;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private EmailService emailService;

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    @Test
    void deveEnviarUmEmailQuandoCadastroForCommitado() {
        // given / when
        service.cadastrar("Maria", "maria@teste.com", "123456");

        // then
        verify(emailService, times(1)).enviar(anyString(), anyString(), anyString());
        assertThat(repository.existsByEmail("maria@teste.com")).isTrue();
    }

    @Test
    void deveNaoEnviarEmailQuandoTransacaoFizerRollback() {
        // given
        TransactionTemplate transacao = new TransactionTemplate(transactionManager);

        // when
        transacao.executeWithoutResult(status -> {
            service.cadastrar("Maria", "maria@teste.com", "123456");
            status.setRollbackOnly();
        });

        // then
        verify(emailService, never()).enviar(anyString(), anyString(), anyString());
        assertThat(repository.count()).isZero();
    }

    @Test
    void deveManterUsuarioGravadoQuandoEnvioDeEmailFalhar() {
        // given
        doThrow(new RuntimeException("servidor de e-mail fora do ar"))
                .when(emailService).enviar(anyString(), anyString(), anyString());

        // when / then
        assertThatCode(() -> service.cadastrar("Maria", "maria@teste.com", "123456"))
                .doesNotThrowAnyException();
        assertThat(repository.existsByEmail("maria@teste.com")).isTrue();
    }
}
