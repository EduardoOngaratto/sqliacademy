package br.edu.uri.sqliacademy.lab;

/**
 * Versao "corrigida" do login no Lab 3. Letra e codigo tem getter publico (usados no template);
 * a explicacao do erro fica acessivel apenas ao validador.
 */
public class VersaoCodigo {

    private final String letra;
    private final String codigo;
    // Codigo que roda no navegador (null quando a versao nao tem)
    private final String codigoNavegador;
    // Explicacao exibida quando o aluno escolhe esta versao e ela nao e a segura (null na segura)
    private final String explicacaoErro;

    public VersaoCodigo(String letra, String codigo, String codigoNavegador, String explicacaoErro) {
        this.letra = letra;
        this.codigo = codigo;
        this.codigoNavegador = codigoNavegador;
        this.explicacaoErro = explicacaoErro;
    }

    public String getLetra() {
        return letra;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCodigoNavegador() {
        return codigoNavegador;
    }

    String explicacaoErro() {
        return explicacaoErro;
    }
}
