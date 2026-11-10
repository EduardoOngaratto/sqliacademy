package br.edu.uri.sqliacademy.controller.admin;

import br.edu.uri.sqliacademy.dto.ProgressoModulo;
import br.edu.uri.sqliacademy.dto.RelatorioAluno;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

// Acesso restrito ao ADMIN pelo SecurityConfig (/admin/**)
@Controller
public class RelatorioController {

    private final UsuarioService usuarioService;
    private final ProgressoService progressoService;

    public RelatorioController(UsuarioService usuarioService, ProgressoService progressoService) {
        this.usuarioService = usuarioService;
        this.progressoService = progressoService;
    }

    @GetMapping("/admin")
    public String relatorio(Model model) {
        List<RelatorioAluno> linhas = new ArrayList<>();
        for (Usuario aluno : usuarioService.listarAlunos()) {
            List<ProgressoModulo> trilha = progressoService.calcularTrilha(aluno);
            linhas.add(new RelatorioAluno(aluno, trilha,
                    trilha.stream().filter(ProgressoModulo::isLabResolvido).count(),
                    progressoService.pontuacao(trilha)));
        }

        model.addAttribute("linhas", linhas);
        // Cabecalho das colunas de modulo (mesma trilha para todos os alunos)
        List<ProgressoModulo> modulos = linhas.isEmpty() ? List.of() : linhas.get(0).getTrilha();
        model.addAttribute("modulos", modulos);
        model.addAttribute("pontuacaoMaxima", progressoService.pontuacaoMaxima(modulos));
        return "admin/relatorio";
    }
}
