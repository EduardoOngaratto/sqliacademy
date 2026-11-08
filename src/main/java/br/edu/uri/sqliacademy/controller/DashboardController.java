package br.edu.uri.sqliacademy.controller;

import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.StatusModulo;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final UsuarioService usuarioService;
    private final ProgressoService progressoService;

    public DashboardController(UsuarioService usuarioService, ProgressoService progressoService) {
        this.usuarioService = usuarioService;
        this.progressoService = progressoService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        List<ProgressoModulo> trilha = progressoService.calcularTrilha(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("trilha", trilha);
        model.addAttribute("pontuacao", progressoService.pontuacao(trilha));
        model.addAttribute("pontuacaoMaxima", progressoService.pontuacaoMaxima(trilha));
        model.addAttribute("concluidos", trilha.stream().filter(p -> p.getStatus() == StatusModulo.CONCLUIDO).count());
        model.addAttribute("labsResolvidos", trilha.stream().filter(ProgressoModulo::isLabResolvido).count());
        // Proximo passo do aluno: o modulo liberado (ainda nao concluido), se houver
        model.addAttribute("proximo", trilha.stream()
                .filter(p -> p.getStatus() == StatusModulo.LIBERADO)
                .findFirst().orElse(null));
        return "aluno/dashboard";
    }
}
