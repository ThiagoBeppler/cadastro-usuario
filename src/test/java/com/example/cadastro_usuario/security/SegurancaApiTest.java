package com.example.cadastro_usuario.security;

import com.example.cadastro_usuario.repository.UsuarioRepository;
import com.example.cadastro_usuario.service.EmailService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SegurancaApiTest {

    private static final String USUARIO_JSON = """
            {"nome":"Maria Silva","email":"maria@teste.com","senha":"123456"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private EmailService emailService;

    @AfterEach
    void limpar() {
        repository.deleteAll();
    }

    private void cadastrarMaria() throws Exception {
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(USUARIO_JSON))
                .andExpect(status().isCreated());
    }

    private String fazerLogin(String senha) throws Exception {
        String corpo = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@teste.com\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(corpo, "$.token");
    }

    private String tokenComValidade(Instant emitidoEm, Instant expiraEm) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("cadastro-usuario")
                .subject("maria@teste.com")
                .issuedAt(emitidoEm)
                .expiresAt(expiraEm)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }

    @Test
    void deveRetornar401EmProblemDetailQuandoAcessarRotaProtegidaSemToken() throws Exception {
        // given / when / then
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void deveRetornar201SemTokenQuandoCadastrarUsuario() throws Exception {
        // given / when / then
        mockMvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON).content(USUARIO_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void deveAcessarRotaProtegidaQuandoLoginGerarTokenValido() throws Exception {
        // given
        cadastrarMaria();
        String token = fazerLogin("123456");

        // when / then
        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("maria@teste.com"))
                .andExpect(jsonPath("$.content[0].senha").doesNotExist());
    }

    @Test
    void deveRetornar401QuandoSenhaIncorreta() throws Exception {
        // given
        cadastrarMaria();

        // when / then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"maria@teste.com\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    void deveRetornar401ComMesmaMensagemQuandoEmailNaoCadastrado() throws Exception {
        // given / when / then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ninguem@teste.com\",\"senha\":\"123456\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
    }

    @Test
    void deveRetornar401QuandoTokenAdulterado() throws Exception {
        // given
        cadastrarMaria();
        String token = fazerLogin("123456");
        String adulterado = token.substring(0, token.length() - 3) + (token.endsWith("AAA") ? "BBB" : "AAA");

        // when / then
        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + adulterado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornar401QuandoTokenExpirado() throws Exception {
        // given
        Instant duasHorasAtras = Instant.now().minusSeconds(7200);
        String expirado = tokenComValidade(duasHorasAtras, duasHorasAtras.plusSeconds(3600));

        // when / then
        mockMvc.perform(get("/usuarios").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }
}
