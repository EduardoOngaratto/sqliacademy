package br.edu.uri.sqliacademy.controller;

import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.service.ConteudoService;
import br.edu.uri.sqliacademy.service.LaboratorioService;
import br.edu.uri.sqliacademy.service.ProgressoService;
import br.edu.uri.sqliacademy.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TrilhaController {

    static final String AVISO_BLOQUEADO = "Conclua o módulo anterior para liberar este.";

    private final UsuarioService usuarioService;
    private final ProgressoService progressoService;
    private final ConteudoService conteudoService;
    private final LaboratorioService laboratorioService;

    public TrilhaController(UsuarioService usuarioService, ProgressoService progressoService,
                            ConteudoService conteudoService, LaboratorioService laboratorioService) {
        this.usuarioService = usuarioService;
        this.progressoService = progressoService;
        this.conteudoService = conteudoService;
        this.laboratorioService = laboratorioService;
    }

    @GetMapping("/trilha")
    public String trilha(Authentication authentication, Model model) {
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());
        model.addAttribute("trilha", progressoService.calcularTrilha(usuario));
        model.addAttribute("admin", usuario.getPerfil() == Perfil.ADMIN);
        return "aluno/trilha";
    }

    @GetMapping("/modulos/{id}")
    public String modulo(@PathVariable Long id, Authentication authentication, Model model,
                         RedirectAttributes redirectAttributes) {
        Modulo modulo = conteudoService.buscarModulo(id);
        Usuario usuario = usuarioService.buscarPorEmail(authentication.getName());

        // Bloqueio checado no servidor (acesso direto pela URL)
        if (!progressoService.podeAcessar(usuario, modulo)) {
            redirectAttributes.addFlashAttribute("aviso", AVISO_BLOQUEADO);
            return "redirect:/trilha";
        }

        model.addAttribute("progresso", progressoService.calcularModulo(usuario, modulo));
        model.addAttribute("conteudos", conteudoService.listarPorModulo(modulo));
        model.addAttribute("idsConcluidos", conteudoService.idsConcluidos(usuario, modulo));
        Laboratorio laboratorio = laboratorioService.buscarDoModulo(modulo);
        model.addAttribute("laboratorio", laboratorio);
        model.addAttribute("labDisponivel", laboratorioService.disponivel(laboratorio));
        return "aluno/modulo";
    }
}
