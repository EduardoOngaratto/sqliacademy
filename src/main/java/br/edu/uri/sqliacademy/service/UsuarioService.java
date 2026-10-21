package br.edu.uri.sqliacademy.service;

import br.edu.uri.sqliacademy.dto.CadastroForm;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean emailJaCadastrado(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    // O e-mail ja chega normalizado pelo CadastroForm
    @Transactional
    public Usuario cadastrarAluno(CadastroForm form) {
        Usuario usuario = new Usuario(
                form.getNome().trim(),
                form.getEmail(),
                passwordEncoder.encode(form.getSenha()),
                Perfil.ALUNO);
        return usuarioRepository.save(usuario);
    }
}
