package br.edu.uri.sqliacademy.service;

import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.StatusModulo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.TentativaQuestionario;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.ConteudoRepository;
import br.edu.uri.sqliacademy.repository.ModuloRepository;
import br.edu.uri.sqliacademy.repository.ProgressoConteudoRepository;
import br.edu.uri.sqliacademy.repository.TentativaLaboratorioRepository;
import br.edu.uri.sqliacademy.repository.TentativaQuestionarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcula status e progresso dos modulos (regras do CLAUDE.md).
 * Modulo concluido = liberado + todos os conteudos lidos + lab resolvido + questionario aprovado.
 * Modulo liberado = primeiro da trilha ou anterior concluido.
 */
@Service
public class ProgressoService {

    private final ModuloRepository moduloRepository;
    private final ConteudoRepository conteudoRepository;
    private final ProgressoConteudoRepository progressoConteudoRepository;
    private final TentativaLaboratorioRepository tentativaLaboratorioRepository;
    private final TentativaQuestionarioRepository tentativaQuestionarioRepository;
    private final int notaMinima;

    public ProgressoService(ModuloRepository moduloRepository,
                            ConteudoRepository conteudoRepository,
                            ProgressoConteudoRepository progressoConteudoRepository,
                            TentativaLaboratorioRepository tentativaLaboratorioRepository,
                            TentativaQuestionarioRepository tentativaQuestionarioRepository,
                            @Value("${sqliacademy.nota-minima}") int notaMinima) {
        this.moduloRepository = moduloRepository;
        this.conteudoRepository = conteudoRepository;
        this.progressoConteudoRepository = progressoConteudoRepository;
        this.tentativaLaboratorioRepository = tentativaLaboratorioRepository;
        this.tentativaQuestionarioRepository = tentativaQuestionarioRepository;
        this.notaMinima = notaMinima;
    }

    public List<ProgressoModulo> calcularTrilha(Usuario usuario) {
        List<ProgressoModulo> trilha = new ArrayList<>();
        boolean anteriorConcluido = true; // o primeiro modulo e sempre liberado

        for (Modulo modulo : moduloRepository.findAllByOrderByOrdemAsc()) {
            long totalConteudos = conteudoRepository.countByModulo(modulo);
            long conteudosLidos = progressoConteudoRepository.countByUsuarioAndConteudoModulo(usuario, modulo);
            boolean labResolvido = tentativaLaboratorioRepository
                    .existsByUsuarioAndLaboratorioModuloAndSucessoTrue(usuario, modulo);

            List<TentativaQuestionario> tentativas = tentativaQuestionarioRepository.findByUsuarioAndModulo(usuario, modulo);
            Integer melhorPercentual = melhorPercentual(tentativas);
            boolean questionarioAprovado = tentativas.stream().anyMatch(this::aprovada);

            boolean completo = conteudosLidos >= totalConteudos && labResolvido && questionarioAprovado;
            StatusModulo status;
            if (!anteriorConcluido) {
                status = StatusModulo.BLOQUEADO;
            } else if (completo) {
                status = StatusModulo.CONCLUIDO;
            } else {
                status = StatusModulo.LIBERADO;
            }

            trilha.add(new ProgressoModulo(modulo, totalConteudos, conteudosLidos, labResolvido,
                    melhorPercentual, questionarioAprovado, status));
            anteriorConcluido = status == StatusModulo.CONCLUIDO;
        }
        return trilha;
    }

    // Reaproveita a trilha inteira, pois a liberacao depende dos modulos anteriores
    public ProgressoModulo calcularModulo(Usuario usuario, Modulo modulo) {
        return calcularTrilha(usuario).stream()
                .filter(p -> p.getModulo().getId().equals(modulo.getId()))
                .findFirst()
                .orElseThrow();
    }

    // Admin ve todos os modulos; aluno so os que nao estao bloqueados
    public boolean podeAcessar(Usuario usuario, Modulo modulo) {
        return usuario.getPerfil() == Perfil.ADMIN
                || calcularModulo(usuario, modulo).getStatus() != StatusModulo.BLOQUEADO;
    }

    // Comparacao em inteiros, sem divisao: acertos/total >= notaMinima/100
    private boolean aprovada(TentativaQuestionario tentativa) {
        return tentativa.getTotal() > 0
                && tentativa.getAcertos() * 100 >= notaMinima * tentativa.getTotal();
    }

    // Melhor nota pelo percentual (o total de questoes pode variar entre tentativas)
    private Integer melhorPercentual(List<TentativaQuestionario> tentativas) {
        Integer melhor = null;
        for (TentativaQuestionario t : tentativas) {
            if (t.getTotal() > 0) {
                int percentual = t.getAcertos() * 100 / t.getTotal();
                if (melhor == null || percentual > melhor) {
                    melhor = percentual;
                }
            }
        }
        return melhor;
    }
}
