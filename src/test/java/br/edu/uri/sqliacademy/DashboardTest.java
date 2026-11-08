package br.edu.uri.sqliacademy;

import br.edu.uri.sqliacademy.config.SecurityConfig;
import br.edu.uri.sqliacademy.controller.DashboardController;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes funcionais do dashboard do aluno (camada web, services simulados)
@WebMvcTest(controllers = DashboardController.class)
@Import(SecurityConfig.class)
class DashboardTest {

    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean ProgressoService progressoService;
    @MockitoBean UserDetailsService userDetailsService;

    private final Usuario aluno = new Usuario("Maria Aluna", "aluno@x.com", "hash", Perfil.ALUNO);
    private Modulo m1;
    private Modulo m2;
    private Modulo m3;

    @BeforeEach
    void setUp() {
        m1 = modulo(1L, 1, "Introdução");
        m2 = modulo(2L, 2, "Identificação");
        m3 = modulo(3L, 3, "Prevenção");
        when(usuarioService.buscarPorEmail("aluno@x.com")).thenReturn(aluno);
        when(progressoService.pontuacaoMaxima(any())).thenReturn(300);
    }

    @Test
    void mostraPontuacaoProgressoEContinuar() throws Exception {
        trilha(List.of(
                new ProgressoModulo(m1, 2, 2, true, StatusModulo.CONCLUIDO),
                new ProgressoModulo(m2, 3, 1, false, StatusModulo.LIBERADO),
                new ProgressoModulo(m3, 1, 0, false, StatusModulo.BLOQUEADO)), 100);

        mvc.perform(get("/dashboard").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Olá, Maria Aluna!")))
                .andExpect(content().string(containsString("100 de 300")))
                .andExpect(content().string(containsString("width: 33%")))
                .andExpect(content().string(containsString("1. Introdução")))
                .andExpect(content().string(containsString("1 de 3")))
                .andExpect(content().string(containsString(">Bloqueado<")))
                .andExpect(content().string(containsString("Continuar: Módulo 2 — Identificação")))
                .andExpect(content().string(containsString("href=\"/modulos/2\"")))
                .andExpect(content().string(not(containsString("Parabéns"))));
    }

    @Test
    void trilhaConcluidaMostraParabens() throws Exception {
        trilha(List.of(
                new ProgressoModulo(m1, 2, 2, true, StatusModulo.CONCLUIDO),
                new ProgressoModulo(m2, 1, 1, true, StatusModulo.CONCLUIDO),
                new ProgressoModulo(m3, 1, 1, true, StatusModulo.CONCLUIDO)), 300);

        mvc.perform(get("/dashboard").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("300 de 300")))
                .andExpect(content().string(containsString("3 de 3")))
                .andExpect(content().string(containsString("Parabéns! Você concluiu todos os módulos")))
                .andExpect(content().string(not(containsString("Continuar:"))));
    }

    @Test
    void dashboardExigeLogin() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(redirectedUrl("/login"));
    }

    private void trilha(List<ProgressoModulo> trilha, int pontuacao) {
        when(progressoService.calcularTrilha(aluno)).thenReturn(trilha);
        when(progressoService.pontuacao(trilha)).thenReturn(pontuacao);
    }

    private static Modulo modulo(Long id, int ordem, String titulo) {
        Modulo m = new Modulo(titulo, "Descrição", ordem);
        m.setId(id);
        return m;
    }
}
