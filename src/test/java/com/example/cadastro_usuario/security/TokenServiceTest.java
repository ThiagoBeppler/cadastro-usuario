package com.example.cadastro_usuario.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes!!";
    private static final String OUTRO_SEGREDO = "outro-segredo-de-teste-com-32-bytes!!!!";

    private final JwtConfig config = new JwtConfig();

    private TokenService tokenServiceCom(String segredo) {
        JwtProperties properties = new JwtProperties(segredo, 3600);
        SecretKey chave = config.jwtSecretKey(properties);
        return new TokenService(config.jwtEncoder(chave), properties);
    }

    private JwtDecoder decoderCom(String segredo) {
        return config.jwtDecoder(config.jwtSecretKey(new JwtProperties(segredo, 3600)));
    }

    @Test
    void deveGerarTokenComEmailEExpiracaoQuandoGerarToken() {
        // given
        TokenService service = tokenServiceCom(SEGREDO);

        // when
        String token = service.gerarToken("maria@teste.com");
        Jwt jwt = decoderCom(SEGREDO).decode(token);

        // then
        assertThat(jwt.getSubject()).isEqualTo("maria@teste.com");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("cadastro-usuario");
        assertThat(jwt.getExpiresAt()).isEqualTo(jwt.getIssuedAt().plusSeconds(3600));
        assertThat(service.getExpiracaoEmSegundos()).isEqualTo(3600);
    }

    @Test
    void deveRejeitarTokenQuandoAssinadoComOutroSegredo() {
        // given
        String token = tokenServiceCom(OUTRO_SEGREDO).gerarToken("maria@teste.com");

        // when / then
        assertThatThrownBy(() -> decoderCom(SEGREDO).decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void deveLancarExcecaoQuandoSegredoTiverMenosDe32Bytes() {
        // given / when / then
        assertThatThrownBy(() -> new JwtProperties("curto", 3600))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("32 bytes");
    }
}
