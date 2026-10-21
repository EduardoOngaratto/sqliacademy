package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    Optional<Modulo> findByOrdem(Integer ordem);
}
