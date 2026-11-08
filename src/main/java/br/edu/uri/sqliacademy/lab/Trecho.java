package br.edu.uri.sqliacademy.lab;

import java.util.List;
import java.util.Map;

/**
 * Trecho de codigo do Lab 2. So campo, titulo e linhas tem getter publico (usados no template);
 * a resposta e as explicacoes ficam acessiveis apenas ao validador.
 */
public class Trecho {

    // Opcao "Nenhuma (codigo seguro)"
    public static final int NENHUMA = 0;

    private final String campo;
    private final String titulo;
    private final List<String> linhas;
    private final int resposta;
    // Explicacao para cada opcao errada (numero da linha ou NENHUMA), sem revelar a certa
    private final Map<Integer, String> explicacoesErro;
    // Por que a resposta certa e a certa (exibido ao acertar)
    private final String porque;

    public Trecho(String campo, String titulo, List<String> linhas, int resposta,
                  Map<Integer, String> explicacoesErro, String porque) {
        this.campo = campo;
        this.titulo = titulo;
        this.linhas = linhas;
        this.resposta = resposta;
        this.explicacoesErro = explicacoesErro;
        this.porque = porque;
    }

    public String getCampo() {
        return campo;
    }

    public String getTitulo() {
        return titulo;
    }

    public List<String> getLinhas() {
        return linhas;
    }

    int resposta() {
        return resposta;
    }

    String explicacaoErro(int opcao) {
        return explicacoesErro.get(opcao);
    }

    String porque() {
        return porque;
    }
}
