package br.edu.uri.sqliacademy.lab;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Regras do Lab 3 (escolher a versao segura): acerto, explicacao de cada armadilha e resposta invalida
class Lab3ValidadorTest {

    private final Lab3Validador validador = new Lab3Validador();

    @Test
    void versaoComPreparedStatementResolve() {
        ResultadoValidacao r = validar("C");
        assertTrue(r.isSucesso());
        assertEquals(Lab3Validador.ACERTO, r.getFeedback());
    }

    @Test
    void cadaArmadilhaTemExplicacaoPropriaSemRevelarAResposta() {
        Set<String> feedbacks = new HashSet<>();
        for (String letra : List.of("A", "B", "D")) {
            ResultadoValidacao r = validar(letra);
            assertFalse(r.isSucesso(), letra);
            assertTrue(r.getFeedback().startsWith("Versão " + letra + ": "), letra);
            assertFalse(r.getFeedback().contains("versão C"), letra);
            feedbacks.add(r.getFeedback());
        }
        assertEquals(3, feedbacks.size());
        assertTrue(validar("A").getFeedback().contains("String.format"));
        assertTrue(validar("B").getFeedback().contains("barra invertida"));
        assertTrue(validar("D").getFeedback().contains("navegador"));
    }

    @Test
    void respostaAusenteOuInvalida() {
        assertEquals(Lab3Validador.SEM_RESPOSTA, validador.validar(Map.of()).getFeedback());
        assertEquals(Lab3Validador.SEM_RESPOSTA, validar("c").getFeedback());
        assertEquals(Lab3Validador.SEM_RESPOSTA, validar("E").getFeedback());
        assertFalse(validar("").isSucesso());
    }

    @Test
    void modeloTemOriginalEQuatroVersoes() {
        Map<String, Object> modelo = validador.getModelo();
        assertTrue(((String) modelo.get("original")).contains("\" + login"));
        @SuppressWarnings("unchecked")
        List<VersaoCodigo> versoes = (List<VersaoCodigo>) modelo.get("versoes");
        assertEquals(List.of("A", "B", "C", "D"), versoes.stream().map(VersaoCodigo::getLetra).toList());
        assertTrue(versoes.get(2).getCodigo().contains("prepareStatement"));
        // So a versao D tem codigo no navegador
        assertEquals(1, versoes.stream().filter(v -> v.getCodigoNavegador() != null).count());
    }

    @Test
    void temTresDicasEUmCampo() {
        assertEquals("LAB3", validador.getCodigo());
        assertEquals(3, validador.getDicas().size());
        assertEquals(List.of("versao"), validador.getCampos());
    }

    private ResultadoValidacao validar(String versao) {
        return validador.validar(Map.of("versao", versao));
    }
}
