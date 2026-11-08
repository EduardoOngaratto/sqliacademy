package br.edu.uri.sqliacademy.lab;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Regras do Lab 2 (identificar a linha vulneravel): respostas, feedback por trecho e o porque ao resolver
class Lab2ValidadorTest {

    private final Lab2Validador validador = new Lab2Validador();

    // Gabarito: trecho1 linha 3, trecho2 nenhuma, trecho3 linha 2, trecho4 nenhuma
    private Map<String, String> corretas() {
        Map<String, String> campos = new HashMap<>();
        campos.put("trecho1", "3");
        campos.put("trecho2", "0");
        campos.put("trecho3", "2");
        campos.put("trecho4", "0");
        return campos;
    }

    @Test
    void todasCorretasResolveEMostraOPorqueDeCadaTrecho() {
        ResultadoValidacao r = validador.validar(corretas());

        assertTrue(r.isSucesso());
        assertEquals("Você acertou os 4 trechos!", r.getFeedback());
        assertEquals(4, r.getDetalhes().size());
        r.getDetalhes().values().forEach(d -> {
            assertTrue(d.isCorreto());
            assertFalse(d.getMensagem().equals(Lab2Validador.CORRETO));
        });
        assertTrue(r.getDetalhes().get("trecho1").getMensagem().startsWith("Linha 3"));
        assertTrue(r.getDetalhes().get("trecho2").getMensagem().contains("coringas do LIKE"));
    }

    @Test
    void umErroNaoResolveEExplicaSoOTrechoErrado() {
        Map<String, String> campos = corretas();
        campos.put("trecho3", "4"); // linha do executeQuery

        ResultadoValidacao r = validador.validar(campos);

        assertFalse(r.isSucesso());
        assertEquals("Você acertou 3 de 4 trechos. Veja as observações em cada trecho e tente de novo.",
                r.getFeedback());
        DetalheCampo errado = r.getDetalhes().get("trecho3");
        assertFalse(errado.isCorreto());
        assertTrue(errado.getMensagem().startsWith("Executar a consulta não é o problema"));
        // Trechos certos so dizem "Correto!" enquanto o lab nao for resolvido
        assertEquals(Lab2Validador.CORRETO, r.getDetalhes().get("trecho1").getMensagem());
    }

    @Test
    void cadaOpcaoErradaTemExplicacaoQueNaoRevelaALinhaCerta() {
        Map<String, Integer> respostas = Map.of("trecho1", 3, "trecho2", 0, "trecho3", 2, "trecho4", 0);
        List<Trecho> trechos = trechos();
        assertEquals(4, trechos.size());

        for (Trecho trecho : trechos) {
            int certa = respostas.get(trecho.getCampo());
            for (int opcao = 0; opcao <= trecho.getLinhas().size(); opcao++) {
                if (opcao == certa) {
                    continue;
                }
                Map<String, String> campos = corretas();
                campos.put(trecho.getCampo(), String.valueOf(opcao));
                DetalheCampo d = validador.validar(campos).getDetalhes().get(trecho.getCampo());

                String descricao = trecho.getCampo() + " opcao " + opcao;
                assertFalse(d.isCorreto(), descricao);
                assertNotNull(d.getMensagem(), descricao);
                assertFalse(d.getMensagem().equals(Lab2Validador.SEM_RESPOSTA), descricao);
                assertFalse(d.getMensagem().contains("Linha " + certa), descricao);
            }
        }
    }

    @Test
    void respostaAusenteOuInvalidaContaComoErro() {
        Map<String, String> campos = corretas();
        campos.remove("trecho1");
        campos.put("trecho2", "abc");
        campos.put("trecho3", "99");
        campos.put("trecho4", "-1");

        ResultadoValidacao r = validador.validar(campos);

        assertFalse(r.isSucesso());
        r.getDetalhes().values().forEach(d -> {
            assertFalse(d.isCorreto());
            assertEquals(Lab2Validador.SEM_RESPOSTA, d.getMensagem());
        });
    }

    @Test
    void temTresDicasEUmCampoPorTrecho() {
        assertEquals("LAB2", validador.getCodigo());
        assertEquals(3, validador.getDicas().size());
        assertEquals(List.of("trecho1", "trecho2", "trecho3", "trecho4"), validador.getCampos());
    }

    @SuppressWarnings("unchecked")
    private List<Trecho> trechos() {
        return (List<Trecho>) validador.getModelo().get("trechos");
    }
}
