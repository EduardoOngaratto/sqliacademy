package br.edu.uri.sqliacademy.controller;

import br.edu.uri.sqliacademy.dto.CadastroForm;
import br.edu.uri.sqliacademy.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // O POST /login e tratado pelo proprio Spring Security
    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null) {
            return "redirect:/";
        }
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Authentication authentication, Model model) {
        if (authentication != null) {
            return "redirect:/";
        }
        model.addAttribute("cadastroForm", new CadastroForm());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(Authentication authentication,
                            @Valid @ModelAttribute("cadastroForm") CadastroForm form,
                            BindingResult result,
                            RedirectAttributes redirectAttributes) {
        if (authentication != null) {
            return "redirect:/";
        }

        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            result.rejectValue("confirmacaoSenha", "senha.diferente", "As senhas não conferem.");
        }
        if (!result.hasFieldErrors("email") && usuarioService.emailJaCadastrado(form.getEmail())) {
            result.rejectValue("email", "email.duplicado", "Este e-mail já está cadastrado.");
        }
        if (result.hasErrors()) {
            return "auth/cadastro";
        }

        usuarioService.cadastrarAluno(form);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cadastro realizado! Faça login para continuar.");
        return "redirect:/login";
    }
}
