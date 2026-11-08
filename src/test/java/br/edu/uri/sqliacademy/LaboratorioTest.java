package br.edu.uri.sqliacademy;

import br.edu.uri.sqliacademy.config.SecurityConfig;
import br.edu.uri.sqliacademy.controller.LaboratorioController;
import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.StatusModulo;
import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.TentativaLaboratorio;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.lab.DetalheCampo;
import br.edu.uri.sqliacademy.lab.Lab1Validador;
import br.edu.uri.sqliacademy.lab.Lab2Validador;
import br.edu.uri.sqliacademy.lab.Lab3Validador;
import br.edu.uri.sqliacademy.lab.ResultadoValidacao;
import br.edu.uri.sqliacademy.repository.LaboratorioRepository;
import br.edu.uri.sqliacademy.repository.TentativaLaboratorioRepository;
import br.edu.uri.sqliacademy.service.LaboratorioService;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes funcionais do Lab 1 (camada web + LaboratorioService e Lab1Validador reais, repositories simulados)
@WebMvcTest(controllers = LaboratorioController.class)
@Import({SecurityConfig.class, LaboratorioService.class, Lab1Validador.class, Lab2Validador.class,
        Lab3Validador.class})
class LaboratorioTest {

    private static final String XSS = "<script>alert(1)</script>";

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean ProgressoService progressoService;
    @MockitoBean LaboratorioRepository laboratorioRepository;
    @MockitoBean TentativaLaboratorioRepository tentativaLaboratorioRepository;
    @MockitoBean UserDetailsService userDetailsService;

    private final Usuario aluno = new Usuario("Aluno", "aluno@x.com", "hash", Perfil.ALUNO);
    private Modulo m1;
    private Laboratorio lab1;

    @BeforeEach
    void setUp() {
        m1 = new Modulo("Introdução", "Descrição", 1);
        m1.setId(1L);
        lab1 = new Laboratorio(m1, "LAB1", "Burlando um login", "Enunciado do lab 1",
                "<p>Explicação com <strong>PreparedStatement</strong></p>", 100);
        lab1.setId(1L);

        when(usuarioService.buscarPorEmail("aluno@x.com")).thenReturn(aluno);
        when(laboratorioRepository.findByCodigo("LAB1")).thenReturn(Optional.of(lab1));
        when(progressoService.podeAcessar(aluno, m1)).thenReturn(true);
        when(progressoService.calcularModulo(aluno, m1)).thenReturn(progresso(0, false));
    }

    @Test
    void paginaMostraFormularioQueryDicasEAviso() throws Exception {
        mvc.perform(get("/laboratorios/LAB1").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Enunciado do lab 1")))
                .andExpect(content().string(containsString("action=\"/laboratorios/LAB1\"")))
                .andExpect(content().string(containsString("name=\"usuario\"")))
                .andExpect(content().string(containsString("maxlength=\"100\"")))
                .andExpect(content().string(containsString("id=\"query-preview\"")))
                .andExpect(content().string(containsString("Mostrar dica (0 de 3)")))
                .andExpect(content().string(containsString("/js/lab1.js")))
                .andExpect(content().string(containsString("Recomendamos ler os conteúdos")))
                .andExpect(content().string(not(containsString("Entenda o que aconteceu"))));
    }

    @Test
    void codigoEmMinusculaTambemFunciona() throws Exception {
        mvc.perform(get("/laboratorios/lab1").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk());
    }

    @Test
    void labSemValidadorOuInexistenteDa404() throws Exception {
        mvc.perform(get("/laboratorios/LAB9").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/laboratorios/XYZ").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void labDeModuloBloqueadoRedirecionaENaoGrava() throws Exception {
        when(progressoService.podeAcessar(aluno, m1)).thenReturn(false);

        mvc.perform(get("/laboratorios/LAB1").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/trilha"))
                .andExpect(flash().attribute("aviso", "Conclua o módulo anterior para liberar este."));
        mvc.perform(post("/laboratorios/LAB1").param("usuario", "admin' --").param("senha", "")
                        .with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/trilha"));
        verify(tentativaLaboratorioRepository, never()).save(any());
    }

    @Test
    void acertoGravaTentativaComSucessoERedireciona() throws Exception {
        MvcResult result = mvc.perform(post("/laboratorios/LAB1").param("usuario", "admin' --").param("senha", "x")
                        .with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/laboratorios/LAB1"))
                .andReturn();

        ResultadoValidacao resultado = (ResultadoValidacao) result.getFlashMap().get("resultado");
        assertTrue(resultado.isSucesso());

        ArgumentCaptor<TentativaLaboratorio> captor = ArgumentCaptor.forClass(TentativaLaboratorio.class);
        verify(tentativaLaboratorioRepository).save(captor.capture());
        assertTrue(captor.getValue().isSucesso());
        assertEquals(lab1, captor.getValue().getLaboratorio());
        assertEquals(aluno, captor.getValue().getUsuario());
        // So os campos do lab (sem _csrf)
        assertEquals("usuario: admin' -- | senha: x", captor.getValue().getEntrada());
    }

    @Test
    void erroGravaTentativaSemSucesso() throws Exception {
        mvc.perform(post("/laboratorios/LAB1").param("usuario", "admin").param("senha", "errada")
                        .with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/laboratorios/LAB1"));

        ArgumentCaptor<TentativaLaboratorio> captor = ArgumentCaptor.forClass(TentativaLaboratorio.class);
        verify(tentativaLaboratorioRepository).save(captor.capture());
        assertFalse(captor.getValue().isSucesso());
    }

    @Test
    void entradaMaiorQueOLimiteNaoGrava() throws Exception {
        MvcResult result = mvc.perform(post("/laboratorios/LAB1").param("usuario", "a".repeat(101)).param("senha", "")
                        .with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/laboratorios/LAB1"))
                .andReturn();

        ResultadoValidacao resultado = (ResultadoValidacao) result.getFlashMap().get("resultado");
        assertFalse(resultado.isSucesso());
        assertTrue(resultado.getFeedback().contains("no máximo 100"));
        verify(tentativaLaboratorioRepository, never()).save(any());
    }

    @Test
    void postSemCsrfDa403() throws Exception {
        mvc.perform(post("/laboratorios/LAB1").param("usuario", "admin' --")
                        .with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isForbidden());
        verify(tentativaLaboratorioRepository, never()).save(any());
    }

    @Test
    void entradaDoAlunoEExibidaEscapada() throws Exception {
        String query = "SELECT * FROM usuario WHERE login = '" + XSS + "' AND senha = ''";
        mvc.perform(get("/laboratorios/LAB1").with(user("aluno@x.com").roles("ALUNO"))
                        .flashAttr("campos", Map.of("usuario", XSS, "senha", XSS))
                        .flashAttr("resultado", new ResultadoValidacao(false, query, "Feedback " + XSS)))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(XSS))))
                .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
    }

    @Test
    void acertoMostraResultadoSimuladoEExplicacao() throws Exception {
        when(progressoService.calcularModulo(aluno, m1)).thenReturn(progresso(1, true));
        ResultadoValidacao acerto = new ResultadoValidacao(true, "SELECT ...", "Login realizado",
                List.of(List.of("1", "admin", "Administrador", "Adm#2026!")));

        mvc.perform(get("/laboratorios/LAB1").with(user("aluno@x.com").roles("ALUNO"))
                        .flashAttr("resultado", acerto))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Resultado da consulta (simulado)")))
                .andExpect(content().string(containsString("<td>Administrador</td>")))
                .andExpect(content().string(containsString(">Resolvido<")))
                .andExpect(content().string(containsString("Entenda o que aconteceu")))
                .andExpect(content().string(containsString("<strong>PreparedStatement</strong>")))
                .andExpect(content().string(not(containsString("Recomendamos ler os conteúdos"))));
    }

    @Test
    void lab2MostraTrechosComLinhasClicaveisEscapadas() throws Exception {
        usarLab2();

        mvc.perform(get("/laboratorios/LAB2").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Trecho 1 — Login")))
                .andExpect(content().string(containsString("Trecho 4 — Pedidos por status")))
                .andExpect(content().string(containsString("name=\"trecho1\" value=\"3\"")))
                .andExpect(content().string(containsString("name=\"trecho4\" value=\"0\"")))
                .andExpect(content().string(containsString("Nenhuma (código seguro)")))
                // Codigo exibido com th:text (aspas escapadas)
                .andExpect(content().string(containsString("request.getParameter(&quot;login&quot;)")))
                .andExpect(content().string(containsString("Mostrar dica (0 de 3)")))
                .andExpect(content().string(containsString("/js/lab2.js")))
                .andExpect(content().string(containsString("/js/dicas.js")))
                .andExpect(content().string(not(containsString("checked=\"checked\""))));
    }

    @Test
    void lab2AcertoGravaTentativaComTodasAsRespostas() throws Exception {
        usarLab2();

        MvcResult result = mvc.perform(post("/laboratorios/LAB2")
                        .param("trecho1", "3").param("trecho2", "0").param("trecho3", "2").param("trecho4", "0")
                        .with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/laboratorios/LAB2"))
                .andReturn();

        assertTrue(((ResultadoValidacao) result.getFlashMap().get("resultado")).isSucesso());
        ArgumentCaptor<TentativaLaboratorio> captor = ArgumentCaptor.forClass(TentativaLaboratorio.class);
        verify(tentativaLaboratorioRepository).save(captor.capture());
        assertTrue(captor.getValue().isSucesso());
        assertEquals("trecho1: 3 | trecho2: 0 | trecho3: 2 | trecho4: 0", captor.getValue().getEntrada());
    }

    @Test
    void lab2MantemEscolhasEMostraFeedbackPorTrecho() throws Exception {
        usarLab2();
        ResultadoValidacao erro = new ResultadoValidacao(false, null, "Você acertou 3 de 4 trechos.", List.of(),
                Map.of("trecho1", new DetalheCampo(true, "Correto!"),
                        "trecho3", new DetalheCampo(false, "Executar a consulta não é o problema")));

        mvc.perform(get("/laboratorios/LAB2").with(user("aluno@x.com").roles("ALUNO"))
                        .flashAttr("campos", Map.of("trecho1", "3", "trecho2", "0", "trecho3", "4", "trecho4", "0"))
                        .flashAttr("resultado", erro))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Você acertou 3 de 4 trechos.")))
                .andExpect(content().string(containsString("name=\"trecho3\" value=\"4\" checked=\"checked\"")))
                .andExpect(content().string(containsString("Executar a consulta não é o problema")))
                .andExpect(content().string(containsString("Correto!")));
    }

    @Test
    void lab3MostraOriginalEVersoesSemADemonstracaoAntesDeResolver() throws Exception {
        usarLab3();

        mvc.perform(get("/laboratorios/LAB3").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Código vulnerável (o login do Lab 1)")))
                .andExpect(content().string(containsString("name=\"versao\" required")))
                .andExpect(content().string(containsString("value=\"A\"")))
                .andExpect(content().string(containsString("value=\"D\"")))
                .andExpect(content().string(containsString("No navegador (JavaScript):")))
                .andExpect(content().string(containsString("String.format(")))
                .andExpect(content().string(containsString("Mostrar dica (0 de 3)")))
                .andExpect(content().string(containsString("/js/lab3.js")))
                .andExpect(content().string(not(containsString("id=\"demonstracao\""))));
    }

    @Test
    void lab3AcertoGravaTentativa() throws Exception {
        usarLab3();

        mvc.perform(post("/laboratorios/LAB3").param("versao", "C")
                        .with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/laboratorios/LAB3"));

        ArgumentCaptor<TentativaLaboratorio> captor = ArgumentCaptor.forClass(TentativaLaboratorio.class);
        verify(tentativaLaboratorioRepository).save(captor.capture());
        assertTrue(captor.getValue().isSucesso());
        assertEquals("versao: C", captor.getValue().getEntrada());
    }

    @Test
    void lab3ResolvidoMostraDemonstracaoEMantemEscolha() throws Exception {
        usarLab3();
        when(progressoService.calcularModulo(aluno, m1)).thenReturn(progresso(1, true));

        mvc.perform(get("/laboratorios/LAB3").with(user("aluno@x.com").roles("ALUNO"))
                        .flashAttr("campos", Map.of("versao", "C"))
                        .flashAttr("resultado", new ResultadoValidacao(true, null, "Correto!")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"C\" checked=\"checked\"")))
                .andExpect(content().string(containsString("id=\"demonstracao\"")))
                .andExpect(content().string(containsString("Repita o ataque do Lab 1 contra a versão segura")))
                .andExpect(content().string(containsString("id=\"demo-param1\"")));
    }

    @Test
    void labExigeLogin() throws Exception {
        mvc.perform(get("/laboratorios/LAB1")).andExpect(redirectedUrl("/login"));
    }

    private void usarLab2() {
        Laboratorio lab2 = new Laboratorio(m1, "LAB2", "Encontre a linha vulnerável", "Enunciado do lab 2", null, 100);
        when(laboratorioRepository.findByCodigo("LAB2")).thenReturn(Optional.of(lab2));
    }

    private void usarLab3() {
        Laboratorio lab3 = new Laboratorio(m1, "LAB3", "Código vulnerável x código seguro", "Enunciado do lab 3",
                null, 100);
        when(laboratorioRepository.findByCodigo("LAB3")).thenReturn(Optional.of(lab3));
    }

    private ProgressoModulo progresso(int conteudosLidos, boolean labResolvido) {
        return new ProgressoModulo(m1, 1, conteudosLidos, labResolvido, StatusModulo.LIBERADO);
    }
}
