package com.example.cadastro_usuario.controller;

import com.example.cadastro_usuario.domain.Usuario;
import com.example.cadastro_usuario.exception.EmailJaCadastradoException;
import com.example.cadastro_usuario.exception.GlobalExceptionHandler;
import com.example.cadastro_usuario.exception.UsuarioNaoEncontradoException;
import com.example.cadastro_usuario.security.SecurityConfig;
import com.example.cadastro_usuario.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    private Usuario usuario(Long id, String nome, String email) {
        Usuario usuario = new Usuario(nome, email, "hash-que-nao-pode-vazar");
        ReflectionTestUtils.setField(usuario, "id", id);
        ReflectionTestUtils.setField(usuario, "criadoEm", LocalDateTime.of(2026, 1, 1, 10, 0));
        ReflectionTestUtils.setField(usuario, "atualizadoEm", LocalDateTime.of(2026, 1, 1, 10, 0));
        return usuario;
    }

    @Test
    void deveRetornar201ELocationQuandoCadastrarComDadosValidos() throws Exception {
        // given
        when(service.cadastrar("Maria", "maria@teste.com", "123456"))
                .thenReturn(usuario(1L, "Maria", "maria@teste.com"));

        // when / then
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria","email":"maria@teste.com","senha":"123456"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/usuarios/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Maria"))
                .andExpect(jsonPath("$.email").value("maria@teste.com"));
    }

    @Test
    void deveNaoExporSenhaQuandoRetornarUsuario() throws Exception {
        // given
        when(service.buscarPorId(1L)).thenReturn(usuario(1L, "Maria", "maria@teste.com"));

        // when / then
        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(content().string(not(containsString("hash-que-nao-pode-vazar"))));
    }

    @Test
    void deveRetornar400ComErrosPorCampoQuandoDadosInvalidos() throws Exception {
        // given / when / then
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"nao-e-email","senha":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.senha").exists());
    }

    @Test
    void deveRetornar409QuandoEmailJaCadastrado() throws Exception {
        // given
        when(service.cadastrar("Maria", "maria@teste.com", "123456"))
                .thenThrow(new EmailJaCadastradoException("maria@teste.com"));

        // when / then
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria","email":"maria@teste.com","senha":"123456"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(containsString("maria@teste.com")));
    }

    @Test
    void deveRetornar200QuandoEditarComDadosValidos() throws Exception {
        // given
        when(service.editar(1L, "Maria Souza", "maria@teste.com", null))
                .thenReturn(usuario(1L, "Maria Souza", "maria@teste.com"));

        // when / then
        mockMvc.perform(put("/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria Souza","email":"maria@teste.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void deveRetornar404QuandoEditarUsuarioInexistente() throws Exception {
        // given
        when(service.editar(eq(99L), any(), any(), any()))
                .thenThrow(new UsuarioNaoEncontradoException(99L));

        // when / then
        mockMvc.perform(put("/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Maria","email":"maria@teste.com"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRetornar200QuandoBuscarPorIdExistente() throws Exception {
        // given
        when(service.buscarPorId(1L)).thenReturn(usuario(1L, "Maria", "maria@teste.com"));

        // when / then
        mockMvc.perform(get("/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("maria@teste.com"));
    }

    @Test
    void deveRetornar404QuandoBuscarPorIdInexistente() throws Exception {
        // given
        when(service.buscarPorId(99L)).thenThrow(new UsuarioNaoEncontradoException(99L));

        // when / then
        mockMvc.perform(get("/usuarios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Usuário não encontrado"));
    }

    @Test
    void devePassarFiltroEPaginacaoQuandoListar() throws Exception {
        // given
        Pageable pedido = org.springframework.data.domain.PageRequest.of(1, 5);
        when(service.listar(eq("ari"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(usuario(1L, "Maria", "maria@teste.com")), pedido, 6));

        // when
        mockMvc.perform(get("/usuarios").param("nome", "ari").param("page", "1").param("size", "5"))
                // then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nome").value("Maria"))
                .andExpect(jsonPath("$.content[0].senha").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(service).listar(eq("ari"), captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(5);
    }
}
