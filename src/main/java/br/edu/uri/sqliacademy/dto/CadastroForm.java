package br.edu.uri.sqliacademy.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public class CadastroForm {

    @NotBlank(message = "Informe o nome.")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
    private String nome;

    @NotBlank(message = "Informe o e-mail.")
    @Email(message = "Informe um e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    // Maximo de 64 porque o BCrypt so aceita ate 72 bytes (acentos ocupam 2 bytes)
    @NotNull(message = "Informe a senha.")
    @Size(min = 8, max = 64, message = "A senha deve ter entre 8 e 64 caracteres.")
    private String senha;

    @NotBlank(message = "Confirme a senha.")
    private String confirmacaoSenha;

    public CadastroForm() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    // Normaliza antes da validacao: sem espacos nas pontas e em minusculas
    public void setEmail(String email) {
        this.email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }
}
