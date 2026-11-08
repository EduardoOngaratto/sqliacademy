package br.edu.uri.sqliacademy;

import br.edu.uri.sqliacademy.config.SecurityConfig;
import br.edu.uri.sqliacademy.controller.ConteudoController;
import br.edu.uri.sqliacademy.controller.TrilhaController;
import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.StatusModulo;
import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.ConteudoService;
import br.edu.uri.sqliacademy.service.LaboratorioService;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
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

// Testes funcionais da trilha, modulo e conteudo (camada web, services simulados)
@WebMvcTest(controllers = {TrilhaController.class, ConteudoController.class})
@Import(SecurityConfig.class)
class TrilhaConteudoTest {

    private static final String AVISO = "Conclua o módulo anterior para liberar este.";

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean ProgressoService progressoService;
    @MockitoBean ConteudoService conteudoService;
    @MockitoBean LaboratorioService laboratorioService;
    @MockitoBean UserDetailsService userDetailsService;

    private final Usuario aluno = new Usuario("Aluno", "aluno@x.com", "hash", Perfil.ALUNO);
    private Modulo m1;
    private Modulo m2;
    private Conteudo c1;
    private Conteudo c2;

    @BeforeEach
    void setUp() {
        m1 = new Modulo("Introdução", "Descrição do módulo 1", 1);
        m1.setId(1L);
        m2 = new Modulo("Identificação", "Descrição do módulo 2", 2);
        m2.setId(2L);
        c1 = new Conteudo(m1, "O que é SQL Injection",
                "<p>Exemplo:</p><pre><code class=\"language-sql\">SELECT * FROM usuario WHERE email = '' OR '1'='1'</code></pre>", 1);
        c1.setId(10L);
        c2 = new Conteudo(m1, "Segundo conteúdo", "<p>Texto</p>", 2);
        c2.setId(11L);

        when(usuarioService.buscarPorEmail("aluno@x.com")).thenReturn(aluno);
        when(conteudoService.buscarModulo(1L)).thenReturn(m1);
        when(conteudoService.buscarModulo(2L)).thenReturn(m2);
        when(conteudoService.buscarModulo(99L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        when(conteudoService.buscarConteudo(10L)).thenReturn(c1);
        when(conteudoService.buscarConteudo(11L)).thenReturn(c2);
        when(progressoService.podeAcessar(aluno, m1)).thenReturn(true);
        when(progressoService.podeAcessar(aluno, m2)).thenReturn(false);
    }

    @Test
    void trilhaMostraModulosComStatusEProgresso() throws Exception {
        when(progressoService.calcularTrilha(aluno)).thenReturn(List.of(
                new ProgressoModulo(m1, 2, 1, false, 50, false, StatusModulo.LIBERADO),
                new ProgressoModulo(m2, 1, 0, false, null, false, StatusModulo.BLOQUEADO)));

        mvc.perform(get("/trilha").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Módulo 1 — Introdução")))
                .andExpect(content().string(containsString("Módulo 2 — Identificação")))
                .andExpect(content().string(containsString(">Liberado<")))
                .andExpect(content().string(containsString(">Bloqueado<")))
                .andExpect(content().string(containsString("Conteúdos lidos: 1 de 2")))
                .andExpect(content().string(containsString("Questionário: pendente (melhor nota 50%)")))
                .andExpect(content().string(containsString("href=\"/modulos/1\"")))
                .andExpect(content().string(not(containsString("href=\"/modulos/2\""))));
    }

    @Test
    void moduloLiberadoMostraConteudosComLidos() throws Exception {
        when(progressoService.calcularModulo(aluno, m1))
                .thenReturn(new ProgressoModulo(m1, 2, 1, false, null, false, StatusModulo.LIBERADO));
        when(conteudoService.listarPorModulo(m1)).thenReturn(List.of(c1, c2));
        when(conteudoService.idsConcluidos(aluno, m1)).thenReturn(Set.of(10L));

        mvc.perform(get("/modulos/1").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Conteúdos (1 de 2 lidos)")))
                .andExpect(content().string(containsString("href=\"/conteudos/10\"")))
                .andExpect(content().string(containsString("✓ Lido")))
                .andExpect(content().string(containsString("Disponível em breve")));
    }

    @Test
    void moduloComLabDisponivelMostraAcessarEResolvido() throws Exception {
        Laboratorio lab = new Laboratorio(m1, "LAB1", "Burlando um login", "Enunciado", null, 100);
        when(progressoService.calcularModulo(aluno, m1))
                .thenReturn(new ProgressoModulo(m1, 2, 2, true, null, false, StatusModulo.LIBERADO));
        when(conteudoService.listarPorModulo(m1)).thenReturn(List.of(c1, c2));
        when(conteudoService.idsConcluidos(aluno, m1)).thenReturn(Set.of(10L, 11L));
        when(laboratorioService.buscarDoModulo(m1)).thenReturn(lab);
        when(laboratorioService.disponivel(lab)).thenReturn(true);

        mvc.perform(get("/modulos/1").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Laboratório: Burlando um login")))
                .andExpect(content().string(containsString("Resolvido")))
                .andExpect(content().string(containsString("href=\"/laboratorios/LAB1\"")));
    }

    @Test
    void moduloBloqueadoRedirecionaPraTrilhaComAviso() throws Exception {
        mvc.perform(get("/modulos/2").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/trilha"))
                .andExpect(flash().attribute("aviso", AVISO));
    }

    @Test
    void moduloInexistenteDa404() throws Exception {
        mvc.perform(get("/modulos/99").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isNotFound());
    }

    @Test
    void conteudoMostraTextoCodigoEProximo() throws Exception {
        when(conteudoService.proximo(c1)).thenReturn(c2);

        mvc.perform(get("/conteudos/10").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<code class=\"language-sql\">")))
                .andExpect(content().string(containsString("Marcar como concluído")))
                .andExpect(content().string(containsString("action=\"/conteudos/10/concluir\"")))
                .andExpect(content().string(containsString("href=\"/conteudos/11\"")))
                .andExpect(content().string(containsString("highlight.min.js")));
    }

    @Test
    void conteudoConcluidoMostraSeloEBotaoDesabilitado() throws Exception {
        when(conteudoService.estaConcluido(aluno, c1)).thenReturn(true);

        mvc.perform(get("/conteudos/10").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(content().string(containsString(">Concluído<")))
                .andExpect(content().string(containsString("Concluído ✓")))
                .andExpect(content().string(containsString("disabled")));
    }

    @Test
    void conteudoDeModuloBloqueadoRedireciona() throws Exception {
        Conteudo bloqueado = new Conteudo(m2, "Bloqueado", "<p>secreto</p>", 1);
        bloqueado.setId(20L);
        when(conteudoService.buscarConteudo(20L)).thenReturn(bloqueado);

        mvc.perform(get("/conteudos/20").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/trilha"))
                .andExpect(flash().attribute("aviso", AVISO));
    }

    @Test
    void concluirMarcaEVoltaProConteudo() throws Exception {
        mvc.perform(post("/conteudos/10/concluir").with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/conteudos/10"));
        verify(conteudoService).marcarConcluido(aluno, c1);
    }

    @Test
    void concluirConteudoDeModuloBloqueadoNaoMarca() throws Exception {
        Conteudo bloqueado = new Conteudo(m2, "Bloqueado", "<p>secreto</p>", 1);
        bloqueado.setId(20L);
        when(conteudoService.buscarConteudo(20L)).thenReturn(bloqueado);

        mvc.perform(post("/conteudos/20/concluir").with(csrf()).with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(redirectedUrl("/trilha"));
        verify(conteudoService, never()).marcarConcluido(any(), any());
    }

    @Test
    void concluirSemCsrfDa403() throws Exception {
        mvc.perform(post("/conteudos/10/concluir").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isForbidden());
        verify(conteudoService, never()).marcarConcluido(any(), any());
    }

    @Test
    void trilhaExigeLogin() throws Exception {
        mvc.perform(get("/trilha")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/conteudos/10")).andExpect(redirectedUrl("/login"));
    }
}
