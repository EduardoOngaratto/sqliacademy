package br.edu.uri.sqliacademy.dto;

import br.edu.uri.sqliacademy.entity.Usuario;

import java.util.List;

/**
 * Linha do relatorio do admin: um aluno e o progresso calculado em cada modulo.
 */
public class RelatorioAluno {

    private final Usuario usuario;
    private final List<ProgressoModulo> trilha;
    private final long labsResolvidos;
    private final int pontuacao;

    public RelatorioAluno(Usuario usuario, List<ProgressoModulo> trilha, long labsResolvidos, int pontuacao) {
        this.usuario = usuario;
        this.trilha = trilha;
        this.labsResolvidos = labsResolvidos;
        this.pontuacao = pontuacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<ProgressoModulo> getTrilha() {
        return trilha;
    }

    public long getLabsResolvidos() {
        return labsResolvidos;
    }

    public int getPontuacao() {
        return pontuacao;
    }
}
