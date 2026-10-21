package br.edu.uri.sqliacademy;

import br.edu.uri.sqliacademy.config.SecurityConfig;
import br.edu.uri.sqliacademy.controller.AuthController;
import br.edu.uri.sqliacademy.controller.HomeController;
import br.edu.uri.sqliacademy.dto.CadastroForm;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.UsuarioRepository;
import br.edu.uri.sqliacademy.service.UsuarioDetailsService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testes funcionais de cadastro, login, logout e controle de acesso (camada web, sem banco)
@WebMvcTest(controllers = {HomeController.class, AuthController.class})
@Import(SecurityConfig.class)
class AuthSecurityTest {

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @MockitoBean UsuarioService usuarioService;
    @MockitoBean UserDetailsService userDetailsService;

    @Test
    void navbarVisitante() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString(">Entrar<")))
                .andExpect(content().string(containsString(">Cadastrar<")))
                .andExpect(content().string(not(containsString(">Sair<"))))
                .andExpect(content().string(not(containsString(">Trilha<"))));
    }

    @Test
    void navbarLogado() throws Exception {
        mvc.perform(get("/").with(user("a@a.com"))).andExpect(status().isOk())
                .andExpect(content().string(containsString(">Sair<")))
                .andExpect(content().string(containsString(">Trilha<")))
                .andExpect(content().string(containsString(">Dashboard<")))
                .andExpect(content().string(not(containsString(">Cadastrar<"))));
    }

    @Test
    void rotaProtegidaVaiProLogin() throws Exception {
        mvc.perform(get("/trilha")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void adminSoParaPerfilAdmin() throws Exception {
        mvc.perform(get("/admin").with(user("aluno@x.com").roles("ALUNO"))).andExpect(status().isForbidden());
        mvc.perform(get("/admin/modulos").with(user("aluno@x.com").roles("ALUNO"))).andExpect(status().isForbidden());
        // Admin passa pela seguranca; 404 porque ainda nao ha controller em /admin
        mvc.perform(get("/admin").with(user("admin@x.com").roles("ADMIN"))).andExpect(status().isNotFound());
        mvc.perform(get("/admin")).andExpect(redirectedUrl("/login"));
    }

    @Test
    void rotasLogadasParaQualquerPerfil() throws Exception {
        // 404 (e nao 403/redirect): passou pela seguranca; a rota ainda nao existe
        mvc.perform(get("/trilha").with(user("aluno@x.com").roles("ALUNO"))).andExpect(status().isNotFound());
        mvc.perform(get("/trilha").with(user("admin@x.com").roles("ADMIN"))).andExpect(status().isNotFound());
    }

    @Test
    void linkAdminSoParaPerfilAdmin() throws Exception {
        mvc.perform(get("/").with(user("admin@x.com").roles("ADMIN")))
                .andExpect(content().string(containsString(">Admin<")));
        mvc.perform(get("/").with(user("aluno@x.com").roles("ALUNO")))
                .andExpect(content().string(not(containsString(">Admin<"))));
        mvc.perform(get("/"))
                .andExpect(content().string(not(containsString(">Admin<"))));
    }

    @Test
    void paginasPublicas() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/cadastro")).andExpect(status().isOk());
        mvc.perform(get("/login").param("error", "")).andExpect(content().string(containsString("E-mail ou senha inválidos.")));
    }

    @Test
    void logadoEmLoginOuCadastroVoltaPraInicio() throws Exception {
        mvc.perform(get("/login").with(user("a@a.com"))).andExpect(redirectedUrl("/"));
        mvc.perform(get("/cadastro").with(user("a@a.com"))).andExpect(redirectedUrl("/"));
    }

    @Test
    void cadastroSemCsrf() throws Exception {
        mvc.perform(post("/cadastro").param("nome", "x")).andExpect(status().isForbidden());
    }

    @Test
    void cadastroInvalido() throws Exception {
        when(usuarioService.emailJaCadastrado("dup@x.com")).thenReturn(true);
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "").param("email", "dup@x.com")
                        .param("senha", "12345678").param("confirmacaoSenha", "outra123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Informe o nome.")))
                .andExpect(content().string(containsString("Este e-mail já está cadastrado.")))
                .andExpect(content().string(containsString("As senhas não conferem.")))
                .andExpect(content().string(not(containsString("12345678"))));
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "A").param("email", "invalido")
                        .param("senha", "curta").param("confirmacaoSenha", "curta"))
                .andExpect(content().string(containsString("Informe um e-mail válido.")))
                .andExpect(content().string(containsString("A senha deve ter entre 8 e 64 caracteres.")));
        verify(usuarioService, never()).cadastrarAluno(any());
    }

    @Test
    void cadastroValidoNormalizaEmail() throws Exception {
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", " Fulano ").param("email", "  Fulano@X.com ")
                        .param("senha", "senhaforte").param("confirmacaoSenha", "senhaforte"))
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("mensagemSucesso", "Cadastro realizado! Faça login para continuar."));
        ArgumentCaptor<CadastroForm> captor = ArgumentCaptor.forClass(CadastroForm.class);
        verify(usuarioService).cadastrarAluno(captor.capture());
        assertEquals("fulano@x.com", captor.getValue().getEmail());
    }

    @Test
    void loginSucessoInativoESenhaErrada() throws Exception {
        when(userDetailsService.loadUserByUsername("ok@x.com")).thenReturn(
                User.withUsername("ok@x.com").password(encoder.encode("senhaforte")).roles("ALUNO").build());
        when(userDetailsService.loadUserByUsername("inativo@x.com")).thenReturn(
                User.withUsername("inativo@x.com").password(encoder.encode("senhaforte")).roles("ALUNO").disabled(true).build());

        mvc.perform(formLogin("/login").user("email", "ok@x.com").password("senha", "senhaforte"))
                .andExpect(authenticated()).andExpect(redirectedUrl("/"));
        mvc.perform(formLogin("/login").user("email", "ok@x.com").password("senha", "errada"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
        mvc.perform(formLogin("/login").user("email", "inativo@x.com").password("senha", "senhaforte"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void logoutVoltaPraInicio() throws Exception {
        mvc.perform(post("/logout").with(csrf()).with(user("a@a.com"))).andExpect(redirectedUrl("/"));
    }

    @Test
    void detailsServiceNormalizaEBloqueiaInativo() {
        UsuarioRepository repo = Mockito.mock(UsuarioRepository.class);
        Usuario u = new Usuario("F", "fulano@x.com", "hash", Perfil.ALUNO);
        u.setAtivo(false);
        when(repo.findByEmail("fulano@x.com")).thenReturn(Optional.of(u));
        UserDetails d = new UsuarioDetailsService(repo).loadUserByUsername("  Fulano@X.com ");
        assertEquals("fulano@x.com", d.getUsername());
        assertFalse(d.isEnabled());
        assertEquals("ROLE_ALUNO", d.getAuthorities().iterator().next().getAuthority());
    }
}
