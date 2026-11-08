package br.edu.uri.sqliacademy.service;

import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.StatusModulo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.ConteudoRepository;
import br.edu.uri.sqliacademy.repository.ModuloRepository;
import br.edu.uri.sqliacademy.repository.ProgressoConteudoRepository;
import br.edu.uri.sqliacademy.repository.TentativaLaboratorioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Calcula status, progresso e pontuacao dos modulos (regras do CLAUDE.md).
 * Modulo concluido = liberado + todos os conteudos lidos + lab resolvido.
 * Modulo liberado = primeiro da trilha ou anterior concluido.
 */
@Service
public class ProgressoService {

    private final ModuloRepository moduloRepository;
    private final ConteudoRepository conteudoRepository;
    private final ProgressoConteudoRepository progressoConteudoRepository;
    private final TentativaLaboratorioRepository tentativaLaboratorioRepository;

    public ProgressoService(ModuloRepository moduloRepository,
                            ConteudoRepository conteudoRepository,
                            ProgressoConteudoRepository progressoConteudoRepository,
                            TentativaLaboratorioRepository tentativaLaboratorioRepository) {
        this.moduloRepository = moduloRepository;
        this.conteudoRepository = conteudoRepository;
        this.progressoConteudoRepository = progressoConteudoRepository;
        this.tentativaLaboratorioRepository = tentativaLaboratorioRepository;
    }

    public List<ProgressoModulo> calcularTrilha(Usuario usuario) {
        List<ProgressoModulo> trilha = new ArrayList<>();
        boolean anteriorConcluido = true; // o primeiro modulo e sempre liberado

        for (Modulo modulo : moduloRepository.findAllByOrderByOrdemAsc()) {
            long totalConteudos = conteudoRepository.countByModulo(modulo);
            long conteudosLidos = progressoConteudoRepository.countByUsuarioAndConteudoModulo(usuario, modulo);
            boolean labResolvido = tentativaLaboratorioRepository
                    .existsByUsuarioAndLaboratorioModuloAndSucessoTrue(usuario, modulo);

            boolean completo = conteudosLidos >= totalConteudos && labResolvido;
            StatusModulo status;
            if (!anteriorConcluido) {
                status = StatusModulo.BLOQUEADO;
            } else if (completo) {
                status = StatusModulo.CONCLUIDO;
            } else {
                status = StatusModulo.LIBERADO;
            }

            trilha.add(new ProgressoModulo(modulo, totalConteudos, conteudosLidos, labResolvido, status));
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

    // Pontuacao = pontos dos labs resolvidos (calculada, nao armazenada)
    public int pontuacao(List<ProgressoModulo> trilha) {
        return trilha.stream().mapToInt(ProgressoModulo::getPontos).sum();
    }

    public int pontuacaoMaxima(List<ProgressoModulo> trilha) {
        return trilha.size() * ProgressoModulo.PONTOS_LABORATORIO;
    }
}
