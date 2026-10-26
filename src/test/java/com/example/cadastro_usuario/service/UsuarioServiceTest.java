package com.example.cadastro_usuario.service;

import com.example.cadastro_usuario.domain.Usuario;
import com.example.cadastro_usuario.exception.EmailJaCadastradoException;
import com.example.cadastro_usuario.exception.UsuarioNaoEncontradoException;
import com.example.cadastro_usuario.repository.UsuarioRepository;
import com.example.cadastro_usuario.service.UsuarioSalvoEvent.Operacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private UsuarioService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioService(repository, passwordEncoder, eventPublisher);
    }

    private Usuario usuarioComId(Long id, String nome, String email, String senhaHash) {
        Usuario usuario = new Usuario(nome, email, senhaHash);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    @Test
    void deveGravarComSenhaCriptografadaEPublicarEventoQuandoCadastrar() {
        // given
        when(repository.existsByEmail("maria@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash-123456");
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario salvo = invocation.getArgument(0);
            ReflectionTestUtils.setField(salvo, "id", 1L);
            return salvo;
        });

        // when
        Usuario resultado = service.cadastrar("Maria", "maria@teste.com", "123456");

        // then
        assertThat(resultado.getSenha()).isEqualTo("hash-123456");
        ArgumentCaptor<UsuarioSalvoEvent> evento = ArgumentCaptor.forClass(UsuarioSalvoEvent.class);
        verify(eventPublisher).publishEvent(evento.capture());
        assertThat(evento.getValue()).isEqualTo(new UsuarioSalvoEvent(1L, "Maria", "maria@teste.com", Operacao.CADASTRO));
    }

    @Test
    void deveLancarExcecaoSemGravarNemPublicarQuandoEmailJaCadastrado() {
        // given
        when(repository.existsByEmail("maria@teste.com")).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> service.cadastrar("Maria", "maria@teste.com", "123456"))
                .isInstanceOf(EmailJaCadastradoException.class);
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveAtualizarDadosEPublicarEventoQuandoEditar() {
        // given
        Usuario existente = usuarioComId(1L, "Maria", "maria@teste.com", "hash-antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot("maria.nova@teste.com", 1L)).thenReturn(false);
        when(passwordEncoder.encode("nova-senha")).thenReturn("hash-novo");

        // when
        Usuario resultado = service.editar(1L, "Maria Souza", "maria.nova@teste.com", "nova-senha");

        // then
        assertThat(resultado.getNome()).isEqualTo("Maria Souza");
        assertThat(resultado.getEmail()).isEqualTo("maria.nova@teste.com");
        assertThat(resultado.getSenha()).isEqualTo("hash-novo");
        verify(eventPublisher).publishEvent(
                new UsuarioSalvoEvent(1L, "Maria Souza", "maria.nova@teste.com", Operacao.EDICAO));
    }

    @Test
    void deveManterSenhaQuandoEditarSemInformarNovaSenha() {
        // given
        Usuario existente = usuarioComId(1L, "Maria", "maria@teste.com", "hash-antigo");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot("maria@teste.com", 1L)).thenReturn(false);

        // when
        Usuario resultado = service.editar(1L, "Maria Souza", "maria@teste.com", null);

        // then
        assertThat(resultado.getSenha()).isEqualTo("hash-antigo");
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void deveLancarExcecaoQuandoEditarComEmailDeOutroUsuario() {
        // given
        Usuario existente = usuarioComId(1L, "Maria", "maria@teste.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot("joao@teste.com", 1L)).thenReturn(true);

        // when / then
        assertThatThrownBy(() -> service.editar(1L, "Maria", "joao@teste.com", null))
                .isInstanceOf(EmailJaCadastradoException.class);
        verify(repository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveLancarExcecaoQuandoEditarUsuarioInexistente() {
        // given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.editar(99L, "Maria", "maria@teste.com", null))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveRetornarUsuarioQuandoBuscarPorIdExistente() {
        // given
        Usuario existente = usuarioComId(1L, "Maria", "maria@teste.com", "hash");
        when(repository.findById(1L)).thenReturn(Optional.of(existente));

        // when
        Usuario resultado = service.buscarPorId(1L);

        // then
        assertThat(resultado).isSameAs(existente);
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorIdInexistente() {
        // given
        when(repository.findById(99L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void deveListarTodosQuandoNomeNaoInformado() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> pagina = new PageImpl<>(List.of(new Usuario("Maria", "maria@teste.com", "hash")));
        when(repository.findAll(pageable)).thenReturn(pagina);

        // when
        Page<Usuario> resultado = service.listar("  ", pageable);

        // then
        assertThat(resultado).isSameAs(pagina);
        verify(repository, never()).findByNomeContainingIgnoreCase(any(), any());
    }

    @Test
    void deveFiltrarPorNomeQuandoNomeInformado() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> pagina = new PageImpl<>(List.of(new Usuario("Maria", "maria@teste.com", "hash")));
        when(repository.findByNomeContainingIgnoreCase("ari", pageable)).thenReturn(pagina);

        // when
        Page<Usuario> resultado = service.listar("ari", pageable);

        // then
        assertThat(resultado).isSameAs(pagina);
        verify(repository, never()).findAll(any(Pageable.class));
    }
}
