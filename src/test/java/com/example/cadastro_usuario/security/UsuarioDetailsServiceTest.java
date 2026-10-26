package com.example.cadastro_usuario.security;

import com.example.cadastro_usuario.domain.Usuario;
import com.example.cadastro_usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository repository;

    @InjectMocks
    private UsuarioDetailsService service;

    @Test
    void deveRetornarUsuarioComHashQuandoEmailExiste() {
        // given
        when(repository.findByEmail("maria@teste.com"))
                .thenReturn(Optional.of(new Usuario("Maria", "maria@teste.com", "hash-bcrypt")));

        // when
        UserDetails detalhes = service.loadUserByUsername("maria@teste.com");

        // then
        assertThat(detalhes.getUsername()).isEqualTo("maria@teste.com");
        assertThat(detalhes.getPassword()).isEqualTo("hash-bcrypt");
    }

    @Test
    void deveLancarExcecaoQuandoEmailNaoExiste() {
        // given
        when(repository.findByEmail("naoexiste@teste.com")).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.loadUserByUsername("naoexiste@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
