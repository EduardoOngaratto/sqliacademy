package br.edu.uri.sqliacademy.config;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.repository.ConteudoRepository;
import br.edu.uri.sqliacademy.repository.ProgressoConteudoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Carga de conteudos a partir dos arquivos de src/test/resources/conteudos (modulo 9)
class CargaConteudoTest {

    private final ConteudoRepository conteudoRepository = mock(ConteudoRepository.class);
    private final ProgressoConteudoRepository progressoConteudoRepository = mock(ProgressoConteudoRepository.class);
    private final CargaConteudo carga = new CargaConteudo(conteudoRepository, progressoConteudoRepository);

    private Modulo modulo9;
    private Modulo modulo8;

    @BeforeEach
    void setUp() {
        modulo9 = new Modulo("Teste", "Descrição", 9);
        modulo8 = new Modulo("Sem arquivos", "Descrição", 8);
        when(conteudoRepository.findByModuloAndOrdem(any(), any())).thenReturn(Optional.empty());
    }

    @Test
    void criaUmConteudoPorArquivoComTituloDoH1() {
        carga.carregarConteudos(modulo9);

        ArgumentCaptor<Conteudo> captor = ArgumentCaptor.forClass(Conteudo.class);
        verify(conteudoRepository, times(2)).save(captor.capture());
        Conteudo primeiro = captor.getAllValues().get(0);
        assertEquals("Primeiro conteúdo de teste", primeiro.getTitulo());
        assertEquals(1, primeiro.getOrdem());
        assertFalse(primeiro.getCorpo().contains("<h1>"));
        assertTrue(primeiro.getCorpo().startsWith("<p>Texto do primeiro conteúdo.</p>"));
        assertTrue(primeiro.getCorpo().contains("<code class=\"language-sql\">"));
        // Tags dentro do h1 saem do titulo
        assertEquals("Segundo conteúdo de teste", captor.getAllValues().get(1).getTitulo());
        assertEquals(2, captor.getAllValues().get(1).getOrdem());
    }

    @Test
    void removeOProvisorioESeuProgressoAntes() {
        Conteudo provisorio = new Conteudo(modulo9, CargaConteudo.TITULO_PROVISORIO, "<p>Em breve.</p>", 1);
        when(conteudoRepository.findByModuloOrderByOrdemAsc(modulo9)).thenReturn(List.of(provisorio));

        carga.carregarConteudos(modulo9);

        verify(progressoConteudoRepository).deleteByConteudo(provisorio);
        verify(conteudoRepository).delete(provisorio);
    }

    @Test
    void atualizaConteudoExistenteSoQuandoMuda() {
        Conteudo desatualizado = new Conteudo(modulo9, "Título antigo", "<p>antigo</p>", 1);
        Conteudo igual = new Conteudo(modulo9, "Segundo conteúdo de teste", "<p>Texto do segundo conteúdo.</p>", 2);
        when(conteudoRepository.findByModuloAndOrdem(modulo9, 1)).thenReturn(Optional.of(desatualizado));
        when(conteudoRepository.findByModuloAndOrdem(modulo9, 2)).thenReturn(Optional.of(igual));

        carga.carregarConteudos(modulo9);

        assertEquals("Primeiro conteúdo de teste", desatualizado.getTitulo());
        verify(conteudoRepository).save(desatualizado);
        verify(conteudoRepository, never()).save(igual);
    }

    @Test
    void moduloSemArquivosNaoMexeEmNada() {
        carga.carregarConteudos(modulo8);

        verify(conteudoRepository, never()).save(any());
        verify(conteudoRepository, never()).delete(any());
    }
}
