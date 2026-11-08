package br.edu.uri.sqliacademy.lab;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lab 2 - identificar a linha vulneravel (ou "nenhuma") em trechos de codigo Java.
 * Resolvido quando o aluno acerta todos os trechos.
 */
@Component
public class Lab2Validador implements ValidadorLaboratorio {

    private static final String LER_PARAMETRO = "Ler o parâmetro da requisição, sozinho, não é perigoso. "
            + "O problema aparece quando o valor é usado para montar o SQL.";
    private static final String CRIAR_STATEMENT = "Criar o Statement não é a falha em si. "
            + "Observe como o texto da consulta foi montado antes.";
    private static final String EXECUTAR = "Executar a consulta não é o problema: ela já chega aqui pronta. "
            + "Procure onde ela foi montada.";
    private static final String NAO_E_SEGURO = "Este trecho não é seguro: há entrada do usuário indo direto "
            + "para o texto do SQL.";
    private static final String PREPARE_SEGURO = "prepareStatement recebe a consulta com marcadores ?, "
            + "sem nenhum dado do usuário: esse é o jeito seguro.";
    private static final String EXECUTAR_SEGURO = "Executar um PreparedStatement com parâmetros é seguro.";

    static final String SEM_RESPOSTA = "Escolha uma linha ou \"Nenhuma\".";
    static final String CORRETO = "Correto!";

    private static final List<Trecho> TRECHOS = List.of(
            new Trecho("trecho1", "Trecho 1 — Login",
                    List.of("String login = request.getParameter(\"login\");",
                            "String senha = request.getParameter(\"senha\");",
                            "String sql = \"SELECT * FROM usuario WHERE login = '\" + login + \"' AND senha = '\" + senha + \"'\";",
                            "Statement st = conexao.createStatement();",
                            "ResultSet rs = st.executeQuery(sql);"),
                    3,
                    Map.of(1, LER_PARAMETRO, 2, LER_PARAMETRO, 4, CRIAR_STATEMENT, 5, EXECUTAR,
                            Trecho.NENHUMA, NAO_E_SEGURO),
                    "Linha 3: login e senha são concatenados dentro das aspas do SQL. Uma aspa digitada fecha "
                            + "o texto e o resto vira código, como no Lab 1."),
            new Trecho("trecho2", "Trecho 2 — Busca de produto",
                    List.of("String termo = request.getParameter(\"busca\");",
                            "String sql = \"SELECT nome, preco FROM produto WHERE nome LIKE ?\";",
                            "PreparedStatement ps = conexao.prepareStatement(sql);",
                            "ps.setString(1, \"%\" + termo + \"%\");",
                            "ResultSet rs = ps.executeQuery();"),
                    Trecho.NENHUMA,
                    Map.of(1, "Ler o parâmetro não é perigoso por si só. Veja como o valor chega ao banco.",
                            2, "Esta consulta usa o marcador ?: não há entrada do usuário no texto do SQL.",
                            3, PREPARE_SEGURO,
                            4, "A concatenação aqui monta só o valor do parâmetro (o termo entre os %), e não o SQL. "
                                    + "O PreparedStatement envia esse valor separado, como dado.",
                            5, EXECUTAR_SEGURO),
                    "Nenhuma: a consulta usa ? e o termo vai por setString. A concatenação da linha 4 só monta o "
                            + "valor buscado. Observação: % e _ digitados no termo funcionam como coringas do LIKE "
                            + "(buscar % traz todos os produtos). Isso muda o resultado da busca, mas não é SQL "
                            + "Injection, porque a estrutura da consulta não muda."),
            new Trecho("trecho3", "Trecho 3 — Filtro por id",
                    List.of("String id = request.getParameter(\"id\");",
                            "String sql = \"SELECT * FROM pedido WHERE id = \" + id;",
                            "Statement st = conexao.createStatement();",
                            "ResultSet rs = st.executeQuery(sql);"),
                    2,
                    Map.of(1, LER_PARAMETRO, 3, CRIAR_STATEMENT, 4, EXECUTAR,
                            Trecho.NENHUMA, "Este trecho não é seguro. Repare: mesmo sem aspas na consulta, "
                                    + "a entrada vai direto para o texto do SQL."),
                    "Linha 2: o id é concatenado sem aspas. Nem é preciso fechar aspa: digitar 1 OR 1=1 já "
                            + "devolve todos os pedidos. Campo numérico também é vulnerável."),
            new Trecho("trecho4", "Trecho 4 — Pedidos por status",
                    List.of("int clienteId = usuarioLogado.getId();",
                            "String sql = \"SELECT * FROM pedido WHERE cliente_id = ? AND status = ?\";",
                            "PreparedStatement ps = conexao.prepareStatement(sql);",
                            "ps.setInt(1, clienteId);",
                            "ps.setString(2, request.getParameter(\"status\"));",
                            "ResultSet rs = ps.executeQuery();"),
                    Trecho.NENHUMA,
                    Map.of(1, "O id vem do usuário logado (do servidor), não da requisição, e ainda vai por parâmetro.",
                            2, "A consulta só tem marcadores ?: não há dado do usuário no texto do SQL.",
                            3, PREPARE_SEGURO,
                            4, "setInt envia o valor como parâmetro, separado do SQL.",
                            5, "Passar request.getParameter direto para o setString é seguro: o valor vai como "
                                    + "parâmetro, nunca como código.",
                            6, EXECUTAR_SEGURO),
                    "Nenhuma: os dois valores vão por setInt e setString. Até o parâmetro lido direto da "
                            + "requisição (linha 5) é tratado só como dado."));

    private static final List<String> DICAS = List.of(
            "Procure onde a entrada do usuário encontra o texto do SQL. Ler o parâmetro ou executar a consulta "
                    + "não é, por si só, o problema.",
            "Se a consulta usa ? e os valores vão por setString ou setInt, o banco recebe a entrada separada, "
                    + "como dado.",
            "Concatenar nem sempre é perigoso: importa se a concatenação monta o SQL ou só o valor de um parâmetro. "
                    + "E um campo numérico, sem aspas, também pode ser atacado.");

    @Override
    public String getCodigo() {
        return "LAB2";
    }

    @Override
    public List<String> getCampos() {
        return TRECHOS.stream().map(Trecho::getCampo).toList();
    }

    @Override
    public List<String> getDicas() {
        return DICAS;
    }

    @Override
    public Map<String, Object> getModelo() {
        return Map.of("trechos", TRECHOS);
    }

    @Override
    public ResultadoValidacao validar(Map<String, String> campos) {
        Map<String, Integer> opcoes = new LinkedHashMap<>();
        int acertos = 0;
        for (Trecho trecho : TRECHOS) {
            Integer opcao = opcao(campos.get(trecho.getCampo()), trecho);
            opcoes.put(trecho.getCampo(), opcao);
            if (opcao != null && opcao == trecho.resposta()) {
                acertos++;
            }
        }
        boolean sucesso = acertos == TRECHOS.size();

        Map<String, DetalheCampo> detalhes = new LinkedHashMap<>();
        for (Trecho trecho : TRECHOS) {
            Integer opcao = opcoes.get(trecho.getCampo());
            if (opcao == null) {
                detalhes.put(trecho.getCampo(), new DetalheCampo(false, SEM_RESPOSTA));
            } else if (opcao == trecho.resposta()) {
                // O porque so aparece quando o lab inteiro foi resolvido
                detalhes.put(trecho.getCampo(), new DetalheCampo(true, sucesso ? trecho.porque() : CORRETO));
            } else {
                detalhes.put(trecho.getCampo(), new DetalheCampo(false, trecho.explicacaoErro(opcao)));
            }
        }

        String feedback = sucesso
                ? "Você acertou os " + TRECHOS.size() + " trechos!"
                : "Você acertou " + acertos + " de " + TRECHOS.size()
                        + " trechos. Veja as observações em cada trecho e tente de novo.";
        return new ResultadoValidacao(sucesso, null, feedback, List.of(), detalhes);
    }

    // Numero da linha escolhida ou NENHUMA; null se ausente ou invalido
    private static Integer opcao(String valor, Trecho trecho) {
        if (valor == null || !valor.matches("\\d{1,2}")) {
            return null;
        }
        int opcao = Integer.parseInt(valor);
        return opcao <= trecho.getLinhas().size() ? opcao : null;
    }
}
