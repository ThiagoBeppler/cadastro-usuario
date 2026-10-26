package com.example.cadastro_usuario.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentacaoApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornarDocumentoOpenApiSemTokenQuandoAcessarApiDocs() throws Exception {
        // given / when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de Usuários"))
                .andExpect(jsonPath("$.paths['/usuarios']").exists())
                .andExpect(jsonPath("$.paths['/usuarios/{id}']").exists())
                .andExpect(jsonPath("$.paths['/auth/login']").exists());
    }

    @Test
    void deveDeclararEsquemaBearerJwtQuandoGerarDocumentacao() throws Exception {
        // given / when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"));
    }

    @Test
    void deveExigirBearerApenasNasRotasProtegidasQuandoGerarDocumentacao() throws Exception {
        // given / when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/usuarios'].get.security[*].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/usuarios/{id}'].get.security[*].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/usuarios/{id}'].put.security[*].bearerAuth").exists())
                .andExpect(jsonPath("$.paths['/usuarios'].post.security").isEmpty())
                .andExpect(jsonPath("$.paths['/auth/login'].post.security").isEmpty());
    }

    @Test
    void deveExporParametrosDePaginacaoQuandoDocumentarListagem() throws Exception {
        // given / when / then
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(jsonPath("$.paths['/usuarios'].get.parameters[*].name").value(hasItem("nome")))
                .andExpect(jsonPath("$.paths['/usuarios'].get.parameters[*].name").value(hasItem("page")))
                .andExpect(jsonPath("$.paths['/usuarios'].get.parameters[*].name").value(hasItem("size")))
                .andExpect(jsonPath("$.paths['/usuarios'].get.parameters[*].name").value(hasItem("sort")));
    }

    @Test
    void deveAbrirSwaggerUiSemTokenQuandoAcessarInterface() throws Exception {
        // given / when / then
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
