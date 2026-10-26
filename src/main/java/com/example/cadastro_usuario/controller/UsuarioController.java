package com.example.cadastro_usuario.controller;

import com.example.cadastro_usuario.domain.Usuario;
import com.example.cadastro_usuario.security.OpenApiConfig;
import com.example.cadastro_usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Cadastro, edição e consulta de usuários")
public class UsuarioController {

    private static final String PROBLEM_JSON = "application/problem+json";

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @SecurityRequirements
    @Operation(summary = "Cadastra um usuário",
            description = "Rota pública. Envia um e-mail ao usuário depois que o cadastro é gravado.")
    @ApiResponse(responseCode = "201", description = "Usuário cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        Usuario usuario = service.cadastrar(request.nome(), request.email(), request.senha());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();
        return ResponseEntity.created(location).body(UsuarioResponse.from(usuario));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    @Operation(summary = "Edita um usuário",
            description = "A senha é opcional: se não for enviada, a atual é mantida. "
                    + "Envia um e-mail ao usuário depois que a edição é gravada.")
    @ApiResponse(responseCode = "200", description = "Usuário atualizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já usado por outro usuário",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public UsuarioResponse editar(@PathVariable Long id, @Valid @RequestBody EdicaoUsuarioRequest request) {
        return UsuarioResponse.from(service.editar(id, request.nome(), request.email(), request.senha()));
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    @Operation(summary = "Consulta um usuário pelo id")
    @ApiResponse(responseCode = "200", description = "Usuário encontrado")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return UsuarioResponse.from(service.buscarPorId(id));
    }

    @GetMapping
    @SecurityRequirement(name = OpenApiConfig.ESQUEMA_BEARER)
    @Operation(summary = "Lista usuários com paginação",
            description = "O parâmetro nome é opcional e filtra por qualquer parte do nome, "
                    + "sem diferenciar maiúsculas de minúsculas. Tamanho máximo da página: 100.")
    @ApiResponse(responseCode = "200", description = "Página de usuários")
    @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    public PaginaResponse<UsuarioResponse> listar(
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return PaginaResponse.from(service.listar(nome, pageable), UsuarioResponse::from);
    }
}
