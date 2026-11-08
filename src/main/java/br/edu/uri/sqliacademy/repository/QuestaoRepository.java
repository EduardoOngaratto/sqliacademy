package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Questao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestaoRepository extends JpaRepository<Questao, Long> {
}
