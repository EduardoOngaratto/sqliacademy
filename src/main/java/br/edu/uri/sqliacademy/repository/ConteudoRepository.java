package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Conteudo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {
}
