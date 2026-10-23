package br.edu.uri.sqliacademy.dto;

public enum StatusModulo {

    CONCLUIDO("Concluído"),
    LIBERADO("Liberado"),
    BLOQUEADO("Bloqueado");

    private final String rotulo;

    StatusModulo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
