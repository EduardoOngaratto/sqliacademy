package br.edu.uri.sqliacademy.service;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.ProgressoConteudo;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.ConteudoRepository;
import br.edu.uri.sqliacademy.repository.ModuloRepository;
import br.edu.uri.sqliacademy.repository.ProgressoConteudoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConteudoService {

    private final ModuloRepository moduloRepository;
    private final ConteudoRepository conteudoRepository;
    private final ProgressoConteudoRepository progressoConteudoRepository;

    public ConteudoService(ModuloRepository moduloRepository,
                           ConteudoRepository conteudoRepository,
                           ProgressoConteudoRepository progressoConteudoRepository) {
        this.moduloRepository = moduloRepository;
        this.conteudoRepository = conteudoRepository;
        this.progressoConteudoRepository = progressoConteudoRepository;
    }

    // Id inexistente vira 404 (pagina de erro generica)
    public Modulo buscarModulo(Long id) {
        return moduloRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Conteudo buscarConteudo(Long id) {
        return conteudoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Conteudo> listarPorModulo(Modulo modulo) {
        return conteudoRepository.findByModuloOrderByOrdemAsc(modulo);
    }

    public Set<Long> idsConcluidos(Usuario usuario, Modulo modulo) {
        return progressoConteudoRepository.findByUsuarioAndConteudoModulo(usuario, modulo).stream()
                .map(p -> p.getConteudo().getId())
                .collect(Collectors.toSet());
    }

    public boolean estaConcluido(Usuario usuario, Conteudo conteudo) {
        return progressoConteudoRepository.existsByUsuarioAndConteudo(usuario, conteudo);
    }

    // Proximo conteudo do mesmo modulo pela ordem (null se for o ultimo)
    public Conteudo proximo(Conteudo conteudo) {
        List<Conteudo> conteudos = listarPorModulo(conteudo.getModulo());
        for (int i = 0; i < conteudos.size() - 1; i++) {
            if (conteudos.get(i).getId().equals(conteudo.getId())) {
                return conteudos.get(i + 1);
            }
        }
        return null;
    }

    // Sem @Transactional de proposito: o save roda na propria transacao do repository,
    // entao a violacao do UNIQUE (clique duplo) pode ser capturada aqui sem marcar rollback.
    public void marcarConcluido(Usuario usuario, Conteudo conteudo) {
        if (estaConcluido(usuario, conteudo)) {
            return;
        }
        try {
            progressoConteudoRepository.save(new ProgressoConteudo(usuario, conteudo));
        } catch (DataIntegrityViolationException e) {
            // Outra requisicao gravou o mesmo registro ao mesmo tempo: ja esta concluido
        }
    }
}
