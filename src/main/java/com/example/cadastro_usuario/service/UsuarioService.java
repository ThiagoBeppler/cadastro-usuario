package com.example.cadastro_usuario.service;

import com.example.cadastro_usuario.domain.Usuario;
import com.example.cadastro_usuario.exception.EmailJaCadastradoException;
import com.example.cadastro_usuario.exception.UsuarioNaoEncontradoException;
import com.example.cadastro_usuario.repository.UsuarioRepository;
import com.example.cadastro_usuario.service.UsuarioSalvoEvent.Operacao;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UsuarioService(UsuarioRepository repository,
                          PasswordEncoder passwordEncoder,
                          ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Usuario cadastrar(String nome, String email, String senha) {
        if (repository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }
        Usuario usuario = repository.save(new Usuario(nome, email, passwordEncoder.encode(senha)));
        publicarEvento(usuario, Operacao.CADASTRO);
        return usuario;
    }

    @Transactional
    public Usuario editar(Long id, String nome, String email, String senha) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new EmailJaCadastradoException(email);
        }
        usuario.atualizar(nome, email);
        if (senha != null && !senha.isBlank()) {
            usuario.alterarSenha(passwordEncoder.encode(senha));
        }
        repository.save(usuario);
        publicarEvento(usuario, Operacao.EDICAO);
        return usuario;
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(String nome, Pageable pageable) {
        if (nome == null || nome.isBlank()) {
            return repository.findAll(pageable);
        }
        return repository.findByNomeContainingIgnoreCase(nome, pageable);
    }

    private void publicarEvento(Usuario usuario, Operacao operacao) {
        eventPublisher.publishEvent(
                new UsuarioSalvoEvent(usuario.getId(), usuario.getNome(), usuario.getEmail(), operacao));
    }
}
