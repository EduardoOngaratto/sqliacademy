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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Regras de liberacao e conclusao dos modulos (nota minima 60%, 2 conteudos por modulo)
class ProgressoServiceTest {

    private final ModuloRepository moduloRepository = mock(ModuloRepository.class);
    private final ConteudoRepository conteudoRepository = mock(ConteudoRepository.class);
    private final ProgressoConteudoRepository progressoConteudoRepository = mock(ProgressoConteudoRepository.class);
    private final TentativaLaboratorioRepository tentativaLaboratorioRepository = mock(TentativaLaboratorioRepository.class);
    private final TentativaQuestionarioRepository tentativaQuestionarioRepository = mock(TentativaQuestionarioRepository.class);

    private final Usuario aluno = new Usuario("Aluno", "aluno@x.com", "hash", Perfil.ALUNO);
    private final Usuario admin = new Usuario("Admin", "admin@x.com", "hash", Perfil.ADMIN);

    private ProgressoService service;
    private Modulo m1;
    private Modulo m2;
    private Modulo m3;

    @BeforeEach
    void setUp() {
        service = new ProgressoService(moduloRepository, conteudoRepository, progressoConteudoRepository,
                tentativaLaboratorioRepository, tentativaQuestionarioRepository, 60);
        m1 = modulo(1L, 1);
        m2 = modulo(2L, 2);
        m3 = modulo(3L, 3);
        when(moduloRepository.findAllByOrderByOrdemAsc()).thenReturn(List.of(m1, m2, m3));
        when(conteudoRepository.countByModulo(any())).thenReturn(2L);
        // Padrao dos mocks: 0 conteudos lidos, lab nao resolvido, nenhuma tentativa de questionario
    }

    @Test
    void semProgressoPrimeiroLiberadoDemaisBloqueados() {
        List<ProgressoModulo> trilha = service.calcularTrilha(aluno);

        assertEquals(List.of(StatusModulo.LIBERADO, StatusModulo.BLOQUEADO, StatusModulo.BLOQUEADO), status(trilha));
        assertNull(trilha.get(0).getMelhorPercentual());
        assertFalse(trilha.get(0).isQuestionarioAprovado());
    }

    @Test
    void moduloCompletoFicaConcluidoELiberaOProximo() {
        completar(m1, tentativa(m1, 6, 10));

        assertEquals(List.of(StatusModulo.CONCLUIDO, StatusModulo.LIBERADO, StatusModulo.BLOQUEADO),
                status(service.calcularTrilha(aluno)));
    }

    @Test
    void todosCompletosTodosConcluidos() {
        completar(m1, tentativa(m1, 10, 10));
        completar(m2, tentativa(m2, 10, 10));
        completar(m3, tentativa(m3, 10, 10));

        assertEquals(List.of(StatusModulo.CONCLUIDO, StatusModulo.CONCLUIDO, StatusModulo.CONCLUIDO),
                status(service.calcularTrilha(aluno)));
    }

    @Test
    void notaAbaixoDaMinimaNaoAprova() {
        completar(m1, tentativa(m1, 59, 100));

        ProgressoModulo p = service.calcularTrilha(aluno).get(0);
        assertFalse(p.isQuestionarioAprovado());
        assertEquals(59, p.getMelhorPercentual());
        assertEquals(StatusModulo.LIBERADO, p.getStatus());
    }

    @Test
    void notaExatamenteNaMinimaAprova() {
        completar(m1, tentativa(m1, 3, 5)); // 60%

        ProgressoModulo p = service.calcularTrilha(aluno).get(0);
        assertTrue(p.isQuestionarioAprovado());
        assertEquals(60, p.getMelhorPercentual());
        assertEquals(StatusModulo.CONCLUIDO, p.getStatus());
    }

    @Test
    void notaQuaseNaMinimaComDivisaoNaoExataNaoAprova() {
        completar(m1, tentativa(m1, 5, 9)); // 55,5%

        assertFalse(service.calcularTrilha(aluno).get(0).isQuestionarioAprovado());
    }

    @Test
    void melhorTentativaPeloPercentualENaoPelosAcertos() {
        completar(m1, tentativa(m1, 7, 14), tentativa(m1, 6, 10)); // 50% e 60%

        ProgressoModulo p = service.calcularTrilha(aluno).get(0);
        assertEquals(60, p.getMelhorPercentual());
        assertTrue(p.isQuestionarioAprovado());
    }

    @Test
    void tentativaComTotalZeroEIgnorada() {
        completar(m1, tentativa(m1, 0, 0));

        ProgressoModulo p = service.calcularTrilha(aluno).get(0);
        assertNull(p.getMelhorPercentual());
        assertFalse(p.isQuestionarioAprovado());
        assertEquals(StatusModulo.LIBERADO, p.getStatus());
    }

    @Test
    void conteudosIncompletosNaoConclui() {
        completar(m1, tentativa(m1, 10, 10));
        when(progressoConteudoRepository.countByUsuarioAndConteudoModulo(aluno, m1)).thenReturn(1L);

        ProgressoModulo p = service.calcularTrilha(aluno).get(0);
        assertEquals(1, p.getConteudosLidos());
        assertEquals(StatusModulo.LIBERADO, p.getStatus());
    }

    @Test
    void labPendenteNaoConclui() {
        completar(m1, tentativa(m1, 10, 10));
        when(tentativaLaboratorioRepository.existsByUsuarioAndLaboratorioModuloAndSucessoTrue(aluno, m1)).thenReturn(false);

        assertEquals(StatusModulo.LIBERADO, service.calcularTrilha(aluno).get(0).getStatus());
    }

    @Test
    void moduloCompletoComAnteriorIncompletoContinuaBloqueado() {
        // Ex.: admin adicionou conteudo no modulo 1 depois que o aluno concluiu tudo
        completar(m2, tentativa(m2, 10, 10));

        assertEquals(List.of(StatusModulo.LIBERADO, StatusModulo.BLOQUEADO, StatusModulo.BLOQUEADO),
                status(service.calcularTrilha(aluno)));
    }

    @Test
    void calcularModuloDevolveOModuloPedido() {
        completar(m1, tentativa(m1, 6, 10));

        ProgressoModulo p = service.calcularModulo(aluno, m2);
        assertEquals(m2, p.getModulo());
        assertEquals(StatusModulo.LIBERADO, p.getStatus());
    }

    @Test
    void podeAcessarAlunoSoNaoBloqueadoAdminTudo() {
        assertTrue(service.podeAcessar(aluno, m1));
        assertFalse(service.podeAcessar(aluno, m2));
        assertFalse(service.podeAcessar(aluno, m3));
        assertTrue(service.podeAcessar(admin, m2));
        assertTrue(service.podeAcessar(admin, m3));
    }

    private void completar(Modulo modulo, TentativaQuestionario... tentativas) {
        when(progressoConteudoRepository.countByUsuarioAndConteudoModulo(aluno, modulo)).thenReturn(2L);
        when(tentativaLaboratorioRepository.existsByUsuarioAndLaboratorioModuloAndSucessoTrue(aluno, modulo)).thenReturn(true);
        when(tentativaQuestionarioRepository.findByUsuarioAndModulo(aluno, modulo)).thenReturn(Arrays.asList(tentativas));
    }

    private TentativaQuestionario tentativa(Modulo modulo, int acertos, int total) {
        return new TentativaQuestionario(aluno, modulo, acertos, total);
    }

    private static Modulo modulo(Long id, int ordem) {
        Modulo m = new Modulo("Módulo " + ordem, "Descrição", ordem);
        m.setId(id);
        return m;
    }

    private static List<StatusModulo> status(List<ProgressoModulo> trilha) {
        return trilha.stream().map(ProgressoModulo::getStatus).toList();
    }
}
