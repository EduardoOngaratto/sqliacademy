package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {

    boolean existsByCodigo(String codigo);
}
