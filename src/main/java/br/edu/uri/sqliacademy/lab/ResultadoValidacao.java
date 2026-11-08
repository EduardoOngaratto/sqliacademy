package br.edu.uri.sqliacademy.lab;

import java.util.List;

public class ResultadoValidacao {

    private final boolean sucesso;
    // Query montada com a entrada do aluno (null quando o lab nao monta query)
    private final String query;
    private final String feedback;
    // "Resultado" simulado da consulta, com dados ficticios (vazio quando nao se aplica)
    private final List<List<String>> linhas;

    public ResultadoValidacao(boolean sucesso, String query, String feedback) {
        this(sucesso, query, feedback, List.of());
    }

    public ResultadoValidacao(boolean sucesso, String query, String feedback, List<List<String>> linhas) {
        this.sucesso = sucesso;
        this.query = query;
        this.feedback = feedback;
        this.linhas = linhas;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public String getQuery() {
        return query;
    }

    public String getFeedback() {
        return feedback;
    }

    public List<List<String>> getLinhas() {
        return linhas;
    }
}
