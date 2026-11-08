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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Regras de liberacao, conclusao (conteudos lidos + lab resolvido) e pontuacao (2 conteudos por modulo)
class ProgressoServiceTest {

    private final ModuloRepository moduloRepository = mock(ModuloRepository.class);
    private final ConteudoRepository conteudoRepository = mock(ConteudoRepository.class);
    private final ProgressoConteudoRepository progressoConteudoRepository = mock(ProgressoConteudoRepository.class);
    private final TentativaLaboratorioRepository tentativaLaboratorioRepository = mock(TentativaLaboratorioRepository.class);

    private final Usuario aluno = new Usuario("Aluno", "aluno@x.com", "hash", Perfil.ALUNO);
    private final Usuario admin = new Usuario("Admin", "admin@x.com", "hash", Perfil.ADMIN);

    private ProgressoService service;
    private Modulo m1;
    private Modulo m2;
    private Modulo m3;

    @BeforeEach
    void setUp() {
        service = new ProgressoService(moduloRepository, conteudoRepository, progressoConteudoRepository,
                tentativaLaboratorioRepository);
        m1 = modulo(1L, 1);
        m2 = modulo(2L, 2);
        m3 = modulo(3L, 3);
        when(moduloRepository.findAllByOrderByOrdemAsc()).thenReturn(List.of(m1, m2, m3));
        when(conteudoRepository.countByModulo(any())).thenReturn(2L);
        // Padrao dos mocks: 0 conteudos lidos e lab nao resolvido
    }

    @Test
    void semProgressoPrimeiroLiberadoDemaisBloqueados() {
        List<ProgressoModulo> trilha = service.calcularTrilha(aluno);

        assertEquals(List.of(StatusModulo.LIBERADO, StatusModulo.BLOQUEADO, StatusModulo.BLOQUEADO), status(trilha));
        assertEquals(0, trilha.get(0).getConteudosLidos());
        assertFalse(trilha.get(0).isLabResolvido());
    }

    @Test
    void conteudosLidosELabResolvidoConcluiELiberaOProximo() {
        lerConteudos(m1, 2);
        resolverLab(m1);

        assertEquals(List.of(StatusModulo.CONCLUIDO, StatusModulo.LIBERADO, StatusModulo.BLOQUEADO),
                status(service.calcularTrilha(aluno)));
    }

    @Test
    void todosCompletosTodosConcluidos() {
        for (Modulo m : List.of(m1, m2, m3)) {
            lerConteudos(m, 2);
            resolverLab(m);
        }

        assertEquals(List.of(StatusModulo.CONCLUIDO, StatusModulo.CONCLUIDO, StatusModulo.CONCLUIDO),
                status(service.calcularTrilha(aluno)));
    }

    @Test
    void conteudosIncompletosNaoConclui() {
        lerConteudos(m1, 1);
        resolverLab(m1);

        ProgressoModulo p = service.calcularTrilha(aluno).get(0);
        assertEquals(1, p.getConteudosLidos());
        assertEquals(StatusModulo.LIBERADO, p.getStatus());
    }

    @Test
    void labPendenteNaoConclui() {
        lerConteudos(m1, 2);

        assertEquals(StatusModulo.LIBERADO, service.calcularTrilha(aluno).get(0).getStatus());
    }

    @Test
    void moduloCompletoComAnteriorIncompletoContinuaBloqueado() {
        // Ex.: um conteudo novo entrou no modulo 1 depois que o aluno concluiu tudo
        lerConteudos(m2, 2);
        resolverLab(m2);

        assertEquals(List.of(StatusModulo.LIBERADO, StatusModulo.BLOQUEADO, StatusModulo.BLOQUEADO),
                status(service.calcularTrilha(aluno)));
    }

    @Test
    void calcularModuloDevolveOModuloPedido() {
        lerConteudos(m1, 2);
        resolverLab(m1);

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

    @Test
    void pontuacaoSoContaLabsResolvidos() {
        resolverLab(m1); // lab resolvido mesmo sem ler os conteudos conta pontos
        resolverLab(m2); // e tambem em modulo ainda bloqueado

        List<ProgressoModulo> trilha = service.calcularTrilha(aluno);
        assertEquals(List.of(100, 100, 0), trilha.stream().map(ProgressoModulo::getPontos).toList());
        assertEquals(200, service.pontuacao(trilha));
        assertEquals(300, service.pontuacaoMaxima(trilha));
    }

    @Test
    void semLabResolvidoPontuacaoZero() {
        lerConteudos(m1, 2);

        assertEquals(0, service.pontuacao(service.calcularTrilha(aluno)));
    }

    private void lerConteudos(Modulo modulo, long lidos) {
        when(progressoConteudoRepository.countByUsuarioAndConteudoModulo(aluno, modulo)).thenReturn(lidos);
    }

    private void resolverLab(Modulo modulo) {
        when(tentativaLaboratorioRepository.existsByUsuarioAndLaboratorioModuloAndSucessoTrue(aluno, modulo)).thenReturn(true);
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
