package br.edu.uri.sqliacademy.lab;

import java.util.List;
import java.util.Map;

/**
 * Regras de um laboratorio, ligado ao registro da tabela laboratorio pelo codigo (LAB1, LAB2, LAB3).
 * A entrada do aluno e apenas analisada como texto: nunca e executada no banco.
 */
public interface ValidadorLaboratorio {

    String getCodigo();

    // Nomes dos campos do formulario do lab (os demais parametros sao ignorados)
    List<String> getCampos();

    // Dicas progressivas, da mais vaga para a mais especifica
    List<String> getDicas();

    ResultadoValidacao validar(Map<String, String> campos);

    // Atributos extras para o template do lab (ex.: os trechos de codigo do Lab 2)
    default Map<String, Object> getModelo() {
        return Map.of();
    }
}
