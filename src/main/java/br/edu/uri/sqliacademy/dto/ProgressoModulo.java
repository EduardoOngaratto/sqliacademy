package br.edu.uri.sqliacademy.dto;

import br.edu.uri.sqliacademy.entity.Modulo;

/**
 * Progresso calculado de um aluno em um modulo (nada disso e gravado no banco).
 */
public class ProgressoModulo {

    // Escala de pontuacao do CLAUDE.md: 100 pontos por laboratorio resolvido
    public static final int PONTOS_LABORATORIO = 100;

    private final Modulo modulo;
    private final long totalConteudos;
    private final long conteudosLidos;
    private final boolean labResolvido;
    private final StatusModulo status;

    public ProgressoModulo(Modulo modulo, long totalConteudos, long conteudosLidos, boolean labResolvido,
                           StatusModulo status) {
        this.modulo = modulo;
        this.totalConteudos = totalConteudos;
        this.conteudosLidos = conteudosLidos;
        this.labResolvido = labResolvido;
        this.status = status;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public long getTotalConteudos() {
        return totalConteudos;
    }

    public long getConteudosLidos() {
        return conteudosLidos;
    }

    public boolean isLabResolvido() {
        return labResolvido;
    }

    public StatusModulo getStatus() {
        return status;
    }

    public int getPontos() {
        return labResolvido ? PONTOS_LABORATORIO : 0;
    }
}
