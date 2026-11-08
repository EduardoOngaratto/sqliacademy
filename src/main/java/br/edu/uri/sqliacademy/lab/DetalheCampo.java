package br.edu.uri.sqliacademy.lab;

// Feedback de um campo da resposta (ex.: um trecho do Lab 2)
public class DetalheCampo {

    private final boolean correto;
    private final String mensagem;

    public DetalheCampo(boolean correto, String mensagem) {
        this.correto = correto;
        this.mensagem = mensagem;
    }

    public boolean isCorreto() {
        return correto;
    }

    public String getMensagem() {
        return mensagem;
    }
}
