package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
