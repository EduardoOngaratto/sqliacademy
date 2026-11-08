package br.edu.uri.sqliacademy.repository;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.ProgressoConteudo;
import br.edu.uri.sqliacademy.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressoConteudoRepository extends JpaRepository<ProgressoConteudo, Long> {

    long countByUsuarioAndConteudoModulo(Usuario usuario, Modulo modulo);

    List<ProgressoConteudo> findByUsuarioAndConteudoModulo(Usuario usuario, Modulo modulo);

    boolean existsByUsuarioAndConteudo(Usuario usuario, Conteudo conteudo);

    // Usado pela carga inicial antes de apagar um conteudo (FK de progresso_conteudo)
    void deleteByConteudo(Conteudo conteudo);
}
