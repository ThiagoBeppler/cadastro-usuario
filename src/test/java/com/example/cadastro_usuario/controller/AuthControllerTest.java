package com.example.cadastro_usuario.controller;

import com.example.cadastro_usuario.exception.GlobalExceptionHandler;
import com.example.cadastro_usuario.security.SecurityConfig;
import com.example.cadastro_usuario.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void deveRetornarTokenQuandoCredenciaisValidas() throws Exception {
        // given
        when(authenticationManager.authenticate(any()))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated("maria@teste.com", null, java.util.List.of()));
        when(tokenService.gerarToken("maria@teste.com")).thenReturn("token-jwt");
        when(tokenService.getExpiracaoEmSegundos()).thenReturn(3600L);

        // when / then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@teste.com","senha":"123456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiraEm").value(3600));
    }

    @Test
    void deveRetornar401ComMensagemUnicaQuandoCredenciaisInvalidas() throws Exception {
        // given
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // when / then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"maria@teste.com","senha":"errada"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    void deveRetornar400QuandoLoginSemCampos() throws Exception {
        // given / when / then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.email").value("é obrigatório"))
                .andExpect(jsonPath("$.erros.senha").value("é obrigatória"));
    }
}
