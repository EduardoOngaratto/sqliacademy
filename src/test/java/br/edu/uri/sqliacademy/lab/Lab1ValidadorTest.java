package br.edu.uri.sqliacademy.lab;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Regras do Lab 1 (login ficticio): variacoes aceitas, recusadas e o feedback de cada erro
class Lab1ValidadorTest {

    private final Lab1Validador validador = new Lab1Validador();

    static Stream<Arguments> injecoesAceitas() {
        return Stream.of(
                Arguments.of("admin' --", "x"),
                Arguments.of("admin'--", "x"),
                Arguments.of("admin' #", ""),
                Arguments.of("admin'#", "qualquer"),
                Arguments.of("ADMIN'   --  ", ""),
                Arguments.of("admin';--", ""),
                Arguments.of("admin'; -- fim", ""),
                Arguments.of("' OR 1=1 --", ""),
                Arguments.of("' or 1=1#", ""),
                Arguments.of("'   Or   1 = 1   -- ", ""),
                Arguments.of("' OR '1'='1' --", ""),
                Arguments.of("' or 'a'='a'#", ""),
                Arguments.of("' OR 2>1 --", ""),
                Arguments.of("' OR true --", ""),
                Arguments.of("x", "' OR '1'='1"),
                Arguments.of("x", "' or 'a'='a"),
                Arguments.of("x", "'   OR   '1' = '1"),
                Arguments.of("x", "' OR 1=1 --"),
                Arguments.of("x", "' OR 1=1#"));
    }

    @ParameterizedTest
    @MethodSource("injecoesAceitas")
    void aceitaVariacoesComuns(String usuario, String senha) {
        ResultadoValidacao r = validar(usuario, senha);
        assertTrue(r.isSucesso(), () -> "deveria aceitar: [" + usuario + "] / [" + senha + "] -> " + r.getFeedback());
        assertFalse(r.getLinhas().isEmpty());
    }

    @Test
    void queryMontadaComAEntradaComoTexto() {
        assertEquals("SELECT * FROM usuario WHERE login = 'admin' --' AND senha = 'x'",
                validar("admin' --", "x").getQuery());
    }

    @Test
    void comentarioComUsuarioExistenteDevolveSoEle() {
        ResultadoValidacao r = validar("maria' --", "");
        assertTrue(r.isSucesso());
        assertEquals(1, r.getLinhas().size());
        assertEquals("maria", r.getLinhas().get(0).get(1));
    }

    @Test
    void tautologiaDevolveTodosEEntraComoOPrimeiro() {
        ResultadoValidacao r = validar("' OR 1=1 --", "");
        assertEquals(3, r.getLinhas().size());
        assertTrue(r.getFeedback().contains("\"admin\""));
    }

    @Test
    void entradaSemAspaETextoComum() {
        ResultadoValidacao r = validar("admin", "senha");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.SEM_INJECAO, r.getFeedback());
    }

    @Test
    void comentarioSemAspaFicaDentroDoTexto() {
        assertEquals(Lab1Validador.SEM_INJECAO, validar("admin --", "# x").getFeedback());
    }

    @Test
    void senhaCorretaSemInjecaoNaoConta() {
        ResultadoValidacao r = validar("admin", "Adm#2026!");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.SENHA_CORRETA, r.getFeedback());
    }

    @Test
    void tautologiaSoNoUsuarioFalhaPelaPrecedenciaDoAnd() {
        // login = '' OR ('1'='1' AND senha = 'x')
        ResultadoValidacao r = validar("' OR '1'='1", "x");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.RESTO_NAO_NEUTRALIZADO, r.getFeedback());
    }

    @Test
    void tautologiaSemFecharAspaQuebraASintaxe() {
        ResultadoValidacao r = validar("' OR 1=1", "");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.ASPA_ABERTA, r.getFeedback());
    }

    @Test
    void parentesesSobrandoQuebraASintaxe() {
        assertEquals(Lab1Validador.SINTAXE_INVALIDA, validar("') OR 1=1 --", "").getFeedback());
    }

    @Test
    void colunaInexistenteQuebraASintaxe() {
        assertEquals(Lab1Validador.SINTAXE_INVALIDA, validar("' OR x=1 --", "").getFeedback());
    }

    @Test
    void comentarioComUsuarioInexistente() {
        ResultadoValidacao r = validar("fulano' --", "");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.COMENTARIO_SEM_USUARIO, r.getFeedback());
    }

    @Test
    void consultaValidaSemResultado() {
        assertEquals(Lab1Validador.SEM_RESULTADO, validar("admin' AND '1'='1", "").getFeedback());
    }

    @Test
    void comandoExtraNaoEAceito() {
        ResultadoValidacao r = validar("admin'; DROP TABLE usuario --", "");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.COMANDO_EXTRA, r.getFeedback());
    }

    @Test
    void aspasTipograficasGanhamAviso() {
        ResultadoValidacao r = validar("‘ OR ‘1’=’1", "");
        assertFalse(r.isSucesso());
        assertEquals(Lab1Validador.ASPAS_TIPOGRAFICAS, r.getFeedback());
    }

    @Test
    void temTresDicasEDoisCampos() {
        assertEquals(3, validador.getDicas().size());
        assertEquals("LAB1", validador.getCodigo());
        assertEquals(2, validador.getCampos().size());
    }

    private ResultadoValidacao validar(String usuario, String senha) {
        return validador.validar(Map.of("usuario", usuario, "senha", senha));
    }
}
