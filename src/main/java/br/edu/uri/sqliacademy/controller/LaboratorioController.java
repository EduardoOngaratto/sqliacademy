package br.edu.uri.sqliacademy.controller;

import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.LaboratorioService;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.Map;

@Controller
public class LaboratorioController {

    private final UsuarioService usuarioService;
    private final ProgressoService progressoService;
    private final LaboratorioService laboratorioService;

    public LaboratorioController(UsuarioService usuarioService, ProgressoService progressoService,
                                 LaboratorioService laboratorioService) {
        this.usuarioService = usuarioService;
        this.progressoService = progressoService;
        this.laboratorioService = laboratorioService;
    }

    @GetMapping("/laboratorios/{codigo}")
    public String laboratorio(@PathVariable String codigo, Authentication authentication, Model model,
                              RedirectAttributes redirectAttributes) {
        Laboratorio laboratorio = laboratorioService.buscarPorCodigo(codigo);
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());

        if (!progressoService.podeAcessar(usuario, laboratorio.getModulo())) {
            redirectAttributes.addFlashAttribute("aviso", TrilhaController.AVISO_BLOQUEADO);
            return "redirect:/trilha";
        }

        // "resultado" e "campos" chegam como flash attributes depois do POST
        model.addAttribute("laboratorio", laboratorio);
        model.addAttribute("progresso", progressoService.calcularModulo(usuario, laboratorio.getModulo()));
        model.addAttribute("dicas", laboratorioService.dicas(laboratorio));
        model.addAttribute("tamanhoMaximo", LaboratorioService.TAMANHO_MAXIMO);
        return "lab/" + laboratorio.getCodigo().toLowerCase(Locale.ROOT);
    }

    // POST -> redirect (com flash) para o F5 nao gravar a tentativa de novo
    @PostMapping("/laboratorios/{codigo}")
    public String tentar(@PathVariable String codigo, @RequestParam Map<String, String> parametros,
                         Authentication authentication, RedirectAttributes redirectAttributes) {
        Laboratorio laboratorio = laboratorioService.buscarPorCodigo(codigo);
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());

        if (!progressoService.podeAcessar(usuario, laboratorio.getModulo())) {
            redirectAttributes.addFlashAttribute("aviso", TrilhaController.AVISO_BLOQUEADO);
            return "redirect:/trilha";
        }

        redirectAttributes.addFlashAttribute("resultado", laboratorioService.tentar(usuario, laboratorio, parametros));
        redirectAttributes.addFlashAttribute("campos", laboratorioService.campos(laboratorio, parametros));
        return "redirect:/laboratorios/" + laboratorio.getCodigo();
    }
}
