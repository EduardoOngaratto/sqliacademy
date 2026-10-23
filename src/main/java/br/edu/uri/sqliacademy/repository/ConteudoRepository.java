package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConteudoRepository extends JpaRepository<Conteudo, Long> {

    boolean existsByModulo(Modulo modulo);

    long countByModulo(Modulo modulo);

    List<Conteudo> findByModuloOrderByOrdemAsc(Modulo modulo);
}
