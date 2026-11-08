package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    Optional<Laboratorio> findByCodigo(String codigo);

    Optional<Laboratorio> findByModulo(Modulo modulo);
}
