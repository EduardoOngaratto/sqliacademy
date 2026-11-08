package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {

    boolean existsByModulo(Modulo modulo);

    List<Questao> findByModuloOrderByIdAsc(Modulo modulo);
}
