package com.example.cadastro_usuario.repository;

import com.example.cadastro_usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository repository;

    private Usuario salvar(String nome, String email) {
        return repository.saveAndFlush(new Usuario(nome, email, "hash"));
    }

    @Test
    void deveRetornarUsuariosQuandoNomeContemTrecho() {
        // given
        salvar("Maria Silva", "maria@teste.com");
        salvar("João Pereira", "joao@teste.com");

        // when
        Page<Usuario> resultado = repository.findByNomeContainingIgnoreCase("ria Si", PageRequest.of(0, 10));

        // then
        assertThat(resultado.getContent()).extracting(Usuario::getNome).containsExactly("Maria Silva");
    }

    @Test
    void deveIgnorarMaiusculasQuandoFiltrarPorNome() {
        // given
        salvar("Ana Souza", "ana@teste.com");
        salvar("MARIANA Lima", "mariana@teste.com");
        salvar("Carlos Dias", "carlos@teste.com");

        // when
        Page<Usuario> resultado = repository.findByNomeContainingIgnoreCase("ana", PageRequest.of(0, 10));

        // then
        assertThat(resultado.getContent()).extracting(Usuario::getNome)
                .containsExactlyInAnyOrder("Ana Souza", "MARIANA Lima");
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNenhumNomeCorresponde() {
        // given
        salvar("Maria Silva", "maria@teste.com");

        // when
        Page<Usuario> resultado = repository.findByNomeContainingIgnoreCase("xyz", PageRequest.of(0, 10));

        // then
        assertThat(resultado.getContent()).isEmpty();
        assertThat(resultado.getTotalElements()).isZero();
    }

    @Test
    void deveRespeitarPaginacaoQuandoHaMaisResultadosQueOTamanhoDaPagina() {
        // given
        for (int i = 1; i <= 5; i++) {
            salvar("Usuario " + i, "usuario" + i + "@teste.com");
        }

        // when
        Page<Usuario> primeiraPagina = repository.findByNomeContainingIgnoreCase("usuario", PageRequest.of(0, 2));
        Page<Usuario> ultimaPagina = repository.findByNomeContainingIgnoreCase("usuario", PageRequest.of(2, 2));

        // then
        assertThat(primeiraPagina.getContent()).hasSize(2);
        assertThat(primeiraPagina.getTotalElements()).isEqualTo(5);
        assertThat(primeiraPagina.getTotalPages()).isEqualTo(3);
        assertThat(ultimaPagina.getContent()).hasSize(1);
    }

    @Test
    void deveRetornarUsuarioQuandoBuscarPorEmailExistente() {
        // given
        salvar("Maria Silva", "maria@teste.com");

        // when
        var resultado = repository.findByEmail("maria@teste.com");

        // then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNome()).isEqualTo("Maria Silva");
    }

    @Test
    void deveRetornarVazioQuandoBuscarPorEmailInexistente() {
        // given / when
        var resultado = repository.findByEmail("ninguem@teste.com");

        // then
        assertThat(resultado).isEmpty();
    }

    @Test
    void deveRetornarTrueQuandoEmailJaExiste() {
        // given
        salvar("Maria Silva", "maria@teste.com");

        // when
        boolean existe = repository.existsByEmail("maria@teste.com");

        // then
        assertThat(existe).isTrue();
        assertThat(repository.existsByEmail("outro@teste.com")).isFalse();
    }

    @Test
    void deveRetornarFalseQuandoEmailPertenceAoProprioUsuario() {
        // given
        Usuario maria = salvar("Maria Silva", "maria@teste.com");
        Usuario joao = salvar("João Pereira", "joao@teste.com");

        // when
        boolean conflitoComProprio = repository.existsByEmailAndIdNot("maria@teste.com", maria.getId());
        boolean conflitoComOutro = repository.existsByEmailAndIdNot("maria@teste.com", joao.getId());

        // then
        assertThat(conflitoComProprio).isFalse();
        assertThat(conflitoComOutro).isTrue();
    }

    @Test
    void deveLancarExcecaoQuandoEmailDuplicado() {
        // given
        salvar("Maria Silva", "maria@teste.com");

        // when / then
        assertThatThrownBy(() -> salvar("Outra Maria", "maria@teste.com"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void devePreencherDatasQuandoSalvar() throws InterruptedException {
        // given
        LocalDateTime antes = LocalDateTime.now();

        // when
        Usuario usuario = salvar("Maria Silva", "maria@teste.com");
        LocalDateTime atualizadoNaCriacao = usuario.getAtualizadoEm();
        Thread.sleep(10);
        usuario.atualizar("Maria Souza", "maria@teste.com");
        repository.saveAndFlush(usuario);

        // then
        assertThat(usuario.getCriadoEm()).isAfterOrEqualTo(antes);
        assertThat(atualizadoNaCriacao).isEqualTo(usuario.getCriadoEm());
        assertThat(usuario.getAtualizadoEm()).isAfter(atualizadoNaCriacao);
    }
}
