package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.TentativaQuestionario;
import br.edu.uri.sqliacademy.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TentativaQuestionarioRepository extends JpaRepository<TentativaQuestionario, Long> {

    List<TentativaQuestionario> findByUsuarioAndModulo(Usuario usuario, Modulo modulo);
}
