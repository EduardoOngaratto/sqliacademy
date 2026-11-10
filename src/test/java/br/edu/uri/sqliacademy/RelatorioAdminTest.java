package br.edu.uri.sqliacademy;

import br.edu.uri.sqliacademy.config.SecurityConfig;
import br.edu.uri.sqliacademy.controller.admin.RelatorioController;
import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.StatusModulo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes funcionais do relatorio do admin (camada web, services simulados)
@WebMvcTest(controllers = RelatorioController.class)
@Import(SecurityConfig.class)
class RelatorioAdminTest {

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean ProgressoService progressoService;
    @MockitoBean UserDetailsService userDetailsService;

    private final Usuario ana = new Usuario("Ana", "ana@x.com", "hash", Perfil.ALUNO);
    private final Usuario bruno = new Usuario("Bruno", "bruno@x.com", "hash", Perfil.ALUNO);
    private Modulo m1;
    private Modulo m2;

    @BeforeEach
    void setUp() {
        m1 = new Modulo("Introdução", "Descrição", 1);
        m1.setId(1L);
        m2 = new Modulo("Identificação", "Descrição", 2);
        m2.setId(2L);
        when(progressoService.pontuacaoMaxima(any())).thenReturn(200);
    }

    @Test
    void adminVeCadaAlunoComStatusLabsEPontuacao() throws Exception {
        bruno.setAtivo(false);
        List<ProgressoModulo> trilhaAna = List.of(
                new ProgressoModulo(m1, 2, 2, true, StatusModulo.CONCLUIDO),
                new ProgressoModulo(m2, 1, 0, true, StatusModulo.LIBERADO));
        List<ProgressoModulo> trilhaBruno = List.of(
                new ProgressoModulo(m1, 2, 0, false, StatusModulo.LIBERADO),
                new ProgressoModulo(m2, 1, 0, false, StatusModulo.BLOQUEADO));
        when(usuarioService.listarAlunos()).thenReturn(List.of(ana, bruno));
        when(progressoService.calcularTrilha(ana)).thenReturn(trilhaAna);
        when(progressoService.calcularTrilha(bruno)).thenReturn(trilhaBruno);
        when(progressoService.pontuacao(trilhaAna)).thenReturn(200);
        when(progressoService.pontuacao(trilhaBruno)).thenReturn(0);

        mvc.perform(get("/admin").with(user("admin@x.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("2 aluno(s) cadastrado(s)")))
                .andExpect(content().string(containsString("1. Introdução")))
                .andExpect(content().string(containsString("ana@x.com")))
                .andExpect(content().string(containsString(">Concluído<")))
                .andExpect(content().string(containsString(">Bloqueado<")))
                .andExpect(content().string(containsString("Lab: resolvido")))
                .andExpect(content().string(containsString("Lab: pendente")))
                .andExpect(content().string(containsString("2 de 2")))
                .andExpect(content().string(containsString("200 de 200")))
                .andExpect(content().string(containsString("0 de 200")))
                .andExpect(content().string(containsString("Inativo")));
    }

    @Test
    void semAlunosMostraAviso() throws Exception {
        when(usuarioService.listarAlunos()).thenReturn(List.of());

        mvc.perform(get("/admin").with(user("admin@x.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum aluno cadastrado ainda.")))
                .andExpect(content().string(not(containsString("<table"))));
    }

    @Test
    void alunoNaoAcessaORelatorio() throws Exception {
        mvc.perform(get("/admin").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/admin")).andExpect(redirectedUrl("/login"));
        verify(usuarioService, never()).listarAlunos();
    }
}
