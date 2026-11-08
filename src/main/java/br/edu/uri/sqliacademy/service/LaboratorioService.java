package br.edu.uri.sqliacademy.service;

import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.TentativaLaboratorio;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.lab.ResultadoValidacao;
import br.edu.uri.sqliacademy.lab.ValidadorLaboratorio;
import br.edu.uri.sqliacademy.repository.LaboratorioRepository;
import br.edu.uri.sqliacademy.repository.TentativaLaboratorioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LaboratorioService {

    // Limite de caracteres por campo de entrada dos labs
    public static final int TAMANHO_MAXIMO = 100;

    private final LaboratorioRepository laboratorioRepository;
    private final TentativaLaboratorioRepository tentativaLaboratorioRepository;
    private final Map<String, ValidadorLaboratorio> validadores = new HashMap<>();

    public LaboratorioService(LaboratorioRepository laboratorioRepository,
                              TentativaLaboratorioRepository tentativaLaboratorioRepository,
                              List<ValidadorLaboratorio> validadores) {
        this.laboratorioRepository = laboratorioRepository;
        this.tentativaLaboratorioRepository = tentativaLaboratorioRepository;
        for (ValidadorLaboratorio validador : validadores) {
            this.validadores.put(validador.getCodigo(), validador);
        }
    }

    // Lab inexistente ou ainda sem validador implementado vira 404
    public Laboratorio buscarPorCodigo(String codigo) {
        String normalizado = codigo.toUpperCase(Locale.ROOT);
        if (!validadores.containsKey(normalizado)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return laboratorioRepository.findByCodigo(normalizado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Laboratorio buscarDoModulo(Modulo modulo) {
        return laboratorioRepository.findByModulo(modulo).orElse(null);
    }

    public boolean disponivel(Laboratorio laboratorio) {
        return laboratorio != null && validadores.containsKey(laboratorio.getCodigo());
    }

    public List<String> dicas(Laboratorio laboratorio) {
        return validadores.get(laboratorio.getCodigo()).getDicas();
    }

    // So os campos esperados pelo lab (ignora _csrf e extras); campo ausente vira ""
    public Map<String, String> campos(Laboratorio laboratorio, Map<String, String> parametros) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (String nome : validadores.get(laboratorio.getCodigo()).getCampos()) {
            campos.put(nome, parametros.getOrDefault(nome, ""));
        }
        return campos;
    }

    // Valida por regras e grava a tentativa. A entrada nunca e executada no banco.
    public ResultadoValidacao tentar(Usuario usuario, Laboratorio laboratorio, Map<String, String> parametros) {
        Map<String, String> campos = campos(laboratorio, parametros);
        for (String valor : campos.values()) {
            if (valor.length() > TAMANHO_MAXIMO) {
                return new ResultadoValidacao(false, null,
                        "Cada campo aceita no máximo " + TAMANHO_MAXIMO + " caracteres.");
            }
        }

        ResultadoValidacao resultado = validadores.get(laboratorio.getCodigo()).validar(campos);
        String entrada = campos.entrySet().stream()
                .map(c -> c.getKey() + ": " + c.getValue())
                .collect(Collectors.joining(" | "));
        tentativaLaboratorioRepository.save(
                new TentativaLaboratorio(usuario, laboratorio, entrada, resultado.isSucesso()));
        return resultado;
    }
}
