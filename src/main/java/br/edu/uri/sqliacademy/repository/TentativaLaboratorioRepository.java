package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.TentativaLaboratorio;
import br.edu.uri.sqliacademy.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaLaboratorioRepository extends JpaRepository<TentativaLaboratorio, Long> {

    // Lab do modulo resolvido = existe ao menos uma tentativa com sucesso
    boolean existsByUsuarioAndLaboratorioModuloAndSucessoTrue(Usuario usuario, Modulo modulo);
}
