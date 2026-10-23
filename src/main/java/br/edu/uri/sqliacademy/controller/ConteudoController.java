package br.edu.uri.sqliacademy.controller;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.ConteudoService;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConteudoController {

    private final UsuarioService usuarioService;
    private final ProgressoService progressoService;
    private final ConteudoService conteudoService;

    public ConteudoController(UsuarioService usuarioService, ProgressoService progressoService,
                              ConteudoService conteudoService) {
        this.usuarioService = usuarioService;
        this.progressoService = progressoService;
        this.conteudoService = conteudoService;
    }

    @GetMapping("/conteudos/{id}")
    public String conteudo(@PathVariable Long id, Authentication authentication, Model model,
                           RedirectAttributes redirectAttributes) {
        Conteudo conteudo = conteudoService.buscarConteudo(id);
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());

        if (!progressoService.podeAcessar(usuario, conteudo.getModulo())) {
            redirectAttributes.addFlashAttribute("aviso", TrilhaController.AVISO_BLOQUEADO);
            return "redirect:/trilha";
        }

        model.addAttribute("conteudo", conteudo);
        model.addAttribute("concluido", conteudoService.estaConcluido(usuario, conteudo));
        model.addAttribute("proximo", conteudoService.proximo(conteudo));
        return "aluno/conteudo";
    }

    @PostMapping("/conteudos/{id}/concluir")
    public String concluir(@PathVariable Long id, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        Conteudo conteudo = conteudoService.buscarConteudo(id);
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());

        // Tambem checado no POST: sem isso daria para marcar conteudo de modulo bloqueado
        if (!progressoService.podeAcessar(usuario, conteudo.getModulo())) {
            redirectAttributes.addFlashAttribute("aviso", TrilhaController.AVISO_BLOQUEADO);
            return "redirect:/trilha";
        }

        conteudoService.marcarConcluido(usuario, conteudo);
        return "redirect:/conteudos/" + id;
    }
}
