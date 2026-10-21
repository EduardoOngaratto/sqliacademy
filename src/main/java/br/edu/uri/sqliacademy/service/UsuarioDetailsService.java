package br.edu.uri.sqliacademy.service;

import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Mesma normalizacao do cadastro, para "Fulano@X.com" encontrar "fulano@x.com"
        String emailNormalizado = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository.findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        // Usuario inativo vira conta desabilitada (DisabledException no login)
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .roles(usuario.getPerfil().name())
                .disabled(!usuario.isAtivo())
                .build();
    }
}
