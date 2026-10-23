package br.edu.uri.sqliacademy.dto;

import br.edu.uri.sqliacademy.entity.Modulo;

/**
 * Progresso calculado de um aluno em um modulo (nada disso e gravado no banco).
 */
public class ProgressoModulo {

    private final Modulo modulo;
    private final long totalConteudos;
    private final long conteudosLidos;
    private final boolean labResolvido;
    // null quando o aluno ainda nao fez o questionario
    private final Integer melhorPercentual;
    private final boolean questionarioAprovado;
    private final StatusModulo status;

    public ProgressoModulo(Modulo modulo, long totalConteudos, long conteudosLidos, boolean labResolvido,
                           Integer melhorPercentual, boolean questionarioAprovado, StatusModulo status) {
        this.modulo = modulo;
        this.totalConteudos = totalConteudos;
        this.conteudosLidos = conteudosLidos;
        this.labResolvido = labResolvido;
        this.melhorPercentual = melhorPercentual;
        this.questionarioAprovado = questionarioAprovado;
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

    public Integer getMelhorPercentual() {
        return melhorPercentual;
    }

    public boolean isQuestionarioAprovado() {
        return questionarioAprovado;
    }

    public StatusModulo getStatus() {
        return status;
    }
}
