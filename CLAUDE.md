# API de Usuários – Teste técnico Desenvolvedor Java (Sênior)

## Contexto
Aplicação web com acesso restrito expondo endpoints REST para:
- Cadastro de usuário (nome, email, senha)
- Edição de usuário
- Consulta paginada com filtro opcional por qualquer parte do nome
- Consulta por id

Classes obrigatórias: UsuarioController, UsuarioService, EmailService (só imprime no console), UsuarioRepository.

Regra central: ao cadastrar ou editar, enviar e-mail ao usuário. A persistência roda em transação e o e-mail só é disparado APÓS o commit (@TransactionalEventListener(phase = AFTER_COMMIT)). Falha no banco → nenhum e-mail. Falha no e-mail → não desfaz o cadastro (apenas loga).

## Stack
- Java 21, Spring Boot 4.1.x (Spring Framework 7, Jakarta EE 11), Maven
- Atenção: no Boot 4 os starters e os test slices foram modularizados (ex.: @WebMvcTest e @DataJpaTest vêm de módulos próprios); conferir os pacotes de import antes de assumir os do Boot 3
- Spring Web, Spring Data JPA, Spring Security (OAuth2 Resource Server com JWT HS256), Bean Validation
- Flyway para o schema (não usar ddl-auto=update; usar validate)
- H2 em memória, na aplicação e nos testes (sem Docker e sem PostgreSQL)
- Configuração em application.properties (não usar YAML)
- JUnit 5, Mockito, AssertJ, spring-security-test
- springdoc-openapi (Swagger)

## Estrutura de pacotes
com.example.cadastro_usuario (pacote gerado pelo Initializr, mantido)
- controller   (controllers + DTOs de request/response como records)
- service      (regras de negócio, eventos)
- repository
- domain       (entidades)
- security     (config, token, UserDetailsService)
- exception    (exceções de negócio + @RestControllerAdvice com ProblemDetail)

## Convenções
- Nunca expor a entidade nem a senha na API; sempre DTOs
- Injeção por construtor, campos final
- Senha sempre com BCrypt
- Erros no formato ProblemDetail (RFC 7807)
- Nomes de domínio em português (Usuario, nome, senha), código técnico em inglês
- Testes no padrão deveRetornarXQuandoY, estrutura given/when/then

## Regras de trabalho (IMPORTANTE)
- Eu (desenvolvedor) conduzo o projeto. Trabalhe uma etapa por vez, só a que eu pedir.
- Antes de editar código, apresente um plano curto e espere minha aprovação.
- Não crie arquivos, dependências ou funcionalidades fora do escopo pedido.
- Não faça commits sem eu autorizar; eu reviso o diff antes.
- Ao terminar uma etapa, rode `./mvnw test` e mostre o resultado.
- Explique decisões não óbvias em uma ou duas frases, para eu registrar no README.
- O README.md já existe e documenta decisões, testes e opções de frontend. Atualize-o se o comportamento mudar.

## Estado do projeto
Todas as etapas do roteiro estão feitas: fundação (H2 + Flyway), domínio e persistência, service com e-mail pós-commit, controller com DTOs e ProblemDetail, segurança JWT, Swagger e README. A API exige token em tudo, exceto `POST /usuarios`, `POST /auth/login` e as rotas do Swagger.

## Comandos
- O Java do PATH pode ser o 17; o projeto exige 21. Aponte `JAVA_HOME` para um JDK 21 antes de rodar o Maven.
- Variável de ambiente `JWT_SECRET`: segredo HS256 do JWT, com pelo menos 32 bytes. Há um valor padrão só para desenvolvimento.
- Build e testes: ./mvnw clean verify
- Rodar app: ./mvnw spring-boot:run
- Swagger UI: http://localhost:8080/swagger-ui.html
