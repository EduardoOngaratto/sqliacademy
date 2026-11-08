package br.edu.uri.sqliacademy.lab;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lab 1 - login ficticio de uma loja que monta a consulta concatenando usuario e senha.
 *
 * A query e montada como texto e o WHERE e avaliado por regras em Java sobre uma tabela ficticia
 * (aspas, comentarios -- e #, AND/OR com a precedencia real e comparacoes simples).
 * Nada e executado no banco.
 */
@Component
public class Lab1Validador implements ValidadorLaboratorio {

    // Mesmo modelo exibido em tempo real pelo static/js/lab1.js
    static final String INICIO = "SELECT * FROM usuario WHERE ";
    static final String CONDICAO_INICIO = "login = '";
    static final String CONDICAO_MEIO = "' AND senha = '";
    static final String CONDICAO_FIM = "'";

    // Tabela ficticia da loja: id, login, nome, senha (texto puro, de proposito)
    private static final List<String> COLUNAS = List.of("id", "login", "nome", "senha");
    private static final List<List<String>> USUARIOS = List.of(
            List.of("1", "admin", "Administrador", "Adm#2026!"),
            List.of("2", "maria", "Maria Souza", "maria123"),
            List.of("3", "joao", "João Lima", "joao@2026"));

    private static final List<String> DICAS = List.of(
            "Observe a consulta montada: tudo o que você digita fica entre aspas simples. "
                    + "O que acontece se você digitar uma aspa (')?",
            "Depois de fechar a aspa, o que vem em seguida vira código SQL. "
                    + "Uma condição sempre verdadeira, ligada com OR, muda o resultado do WHERE.",
            "A checagem da senha ainda atrapalha? Em SQL, -- e # transformam o resto da linha em comentário. "
                    + "Lembre também que o AND é avaliado antes do OR: faz diferença em qual campo você escreve a condição.");

    static final String SEM_INJECAO = "Login inválido. Sua entrada ficou inteira entre aspas e foi tratada como "
            + "texto comum: o sistema só procurou um usuário com esse login e essa senha.";
    static final String SENHA_CORRETA = "Você entrou com a senha correta, mas o objetivo é entrar sem conhecer a senha, "
            + "alterando a consulta.";
    static final String ASPA_ABERTA = "Você fechou a aspa, mas a sintaxe quebrou: sobrou uma aspa aberta no fim "
            + "da consulta. Repare no que vem depois do trecho que você digitou.";
    static final String SINTAXE_INVALIDA = "Você fechou a aspa, mas a sintaxe quebrou: a consulta montada não é um "
            + "SQL válido. Confira o que sobrou depois do trecho que você digitou.";
    static final String COMANDO_EXTRA = "Este laboratório simula uma única consulta: comandos extras depois do ';' "
            + "não são aceitos.";
    static final String COMENTARIO_SEM_USUARIO = "Você neutralizou o resto da consulta, mas a condição que sobrou "
            + "não encontra nenhum usuário.";
    static final String RESTO_NAO_NEUTRALIZADO = "Sua condição com OR não bastou: o resto da consulta "
            + "(a checagem da senha) ainda está sendo avaliado e derruba o resultado.";
    static final String SEM_RESULTADO = "A consulta continua válida, mas não encontra nenhum usuário. "
            + "É preciso mudar a lógica do WHERE.";
    static final String ASPAS_TIPOGRAFICAS = "Atenção: você usou aspas tipográficas (‘ ’ ou ´). "
            + "No SQL, a aspa que delimita texto é a aspa simples (').";

    // MySQL converte texto em numero pelo prefixo numerico ('1abc' = 1, 'abc' = 0)
    private static final Pattern NUMERO_INICIAL = Pattern.compile("\\s*[-+]?\\d+(\\.\\d+)?");
    private static final Pattern ASPA_TIPOGRAFICA = Pattern.compile("[‘’´]");

    @Override
    public String getCodigo() {
        return "LAB1";
    }

    @Override
    public List<String> getCampos() {
        return List.of("usuario", "senha");
    }

    @Override
    public List<String> getDicas() {
        return DICAS;
    }

    @Override
    public ResultadoValidacao validar(Map<String, String> campos) {
        String usuario = campos.getOrDefault("usuario", "");
        String senha = campos.getOrDefault("senha", "");
        String condicao = CONDICAO_INICIO + usuario + CONDICAO_MEIO + senha + CONDICAO_FIM;
        String query = INICIO + condicao;
        boolean injecao = usuario.contains("'") || senha.contains("'");

        if (!injecao && ASPA_TIPOGRAFICA.matcher(usuario + senha).find()) {
            return new ResultadoValidacao(false, query, ASPAS_TIPOGRAFICAS);
        }

        List<Token> tokens = new ArrayList<>();
        List<List<String>> linhas = new ArrayList<>();
        boolean comentario;
        try {
            comentario = tokenizar(condicao, tokens);
            for (List<String> linha : USUARIOS) {
                if (verdadeiro(new Avaliador(tokens, linha).avaliar())) {
                    linhas.add(linha);
                }
            }
        } catch (ErroSintaxe e) {
            return new ResultadoValidacao(false, query, e.getMessage());
        }

        if (linhas.isEmpty()) {
            if (!injecao) {
                return new ResultadoValidacao(false, query, SEM_INJECAO);
            }
            if (comentario) {
                return new ResultadoValidacao(false, query, COMENTARIO_SEM_USUARIO);
            }
            boolean usouOr = tokens.stream().anyMatch(t -> t.tipo() == Tipo.NOME && t.texto().equals("or"));
            return new ResultadoValidacao(false, query, usouOr ? RESTO_NAO_NEUTRALIZADO : SEM_RESULTADO);
        }
        if (!injecao) {
            return new ResultadoValidacao(false, query, SENHA_CORRETA);
        }
        // Como num sistema real, o login aceita a primeira linha devolvida
        String feedback = "Login realizado como \"" + linhas.get(0).get(1) + "\" sem saber a senha! A consulta devolveu "
                + linhas.size() + " linha(s) e o sistema aceitou a primeira.";
        return new ResultadoValidacao(true, query, feedback, List.copyOf(linhas));
    }

    // Quebra a condicao em tokens. Devolve true se parte dela virou comentario.
    private static boolean tokenizar(String sql, List<Token> tokens) {
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (c == '#' || sql.startsWith("--", i)) {
                // Aceita "--" com ou sem espaco (no MySQL real o espaco e obrigatorio)
                return true;
            } else if (c == ';') {
                String resto = sql.substring(i + 1).strip();
                if (resto.isEmpty()) {
                    return false;
                }
                if (resto.startsWith("#") || resto.startsWith("--")) {
                    return true;
                }
                throw new ErroSintaxe(COMANDO_EXTRA);
            } else if (c == '\'') {
                StringBuilder texto = new StringBuilder();
                i++;
                while (true) {
                    if (i >= sql.length()) {
                        throw new ErroSintaxe(ASPA_ABERTA);
                    }
                    char d = sql.charAt(i);
                    if (d == '\'') {
                        // '' dentro do texto e uma aspa escapada
                        if (i + 1 < sql.length() && sql.charAt(i + 1) == '\'') {
                            texto.append('\'');
                            i += 2;
                            continue;
                        }
                        i++;
                        break;
                    }
                    texto.append(d);
                    i++;
                }
                tokens.add(new Token(Tipo.TEXTO, texto.toString()));
            } else if (Character.isDigit(c)) {
                int inicio = i;
                while (i < sql.length() && Character.isDigit(sql.charAt(i))) {
                    i++;
                }
                if (i + 1 < sql.length() && sql.charAt(i) == '.' && Character.isDigit(sql.charAt(i + 1))) {
                    i++;
                    while (i < sql.length() && Character.isDigit(sql.charAt(i))) {
                        i++;
                    }
                }
                tokens.add(new Token(Tipo.NUMERO, sql.substring(inicio, i)));
            } else if (Character.isLetter(c) || c == '_') {
                int inicio = i;
                while (i < sql.length() && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    i++;
                }
                tokens.add(new Token(Tipo.NOME, sql.substring(inicio, i).toLowerCase(Locale.ROOT)));
            } else if (sql.startsWith("<>", i) || sql.startsWith("!=", i)
                    || sql.startsWith("<=", i) || sql.startsWith(">=", i)) {
                tokens.add(new Token(Tipo.OPERADOR, sql.substring(i, i + 2)));
                i += 2;
            } else if (c == '=' || c == '<' || c == '>') {
                tokens.add(new Token(Tipo.OPERADOR, String.valueOf(c)));
                i++;
            } else if (c == '(' || c == ')') {
                tokens.add(new Token(c == '(' ? Tipo.ABRE : Tipo.FECHA, String.valueOf(c)));
                i++;
            } else {
                throw new ErroSintaxe(SINTAXE_INVALIDA);
            }
        }
        return false;
    }

    private static boolean comparar(Object a, String operador, Object b) {
        int c;
        if (a instanceof String x && b instanceof String y) {
            c = x.compareToIgnoreCase(y); // collation padrao do MySQL ignora maiusculas
        } else {
            c = numero(a).compareTo(numero(b));
        }
        return switch (operador) {
            case "=" -> c == 0;
            case "<>", "!=" -> c != 0;
            case "<" -> c < 0;
            case ">" -> c > 0;
            case "<=" -> c <= 0;
            default -> c >= 0;
        };
    }

    private static BigDecimal numero(Object valor) {
        if (valor instanceof Boolean b) {
            return b ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal n) {
            return n;
        }
        Matcher m = NUMERO_INICIAL.matcher((String) valor);
        return m.lookingAt() ? new BigDecimal(m.group().strip()) : BigDecimal.ZERO;
    }

    private static boolean verdadeiro(Object valor) {
        return numero(valor).signum() != 0;
    }

    private enum Tipo { TEXTO, NUMERO, NOME, OPERADOR, ABRE, FECHA }

    private record Token(Tipo tipo, String texto) {
    }

    private static class ErroSintaxe extends RuntimeException {
        ErroSintaxe(String mensagem) {
            super(mensagem);
        }
    }

    /**
     * Avalia o WHERE para uma linha da tabela ficticia, respeitando a precedencia real:
     * ou := e (OR e)* ; e := comparacao (AND comparacao)* ; comparacao := valor (operador valor)?
     */
    private static class Avaliador {

        private final List<Token> tokens;
        private final List<String> linha;
        private int pos;

        Avaliador(List<Token> tokens, List<String> linha) {
            this.tokens = tokens;
            this.linha = linha;
        }

        Object avaliar() {
            Object valor = ou();
            if (pos < tokens.size()) {
                throw new ErroSintaxe(SINTAXE_INVALIDA);
            }
            return valor;
        }

        private Object ou() {
            Object valor = e();
            while (aceitar("or")) {
                Object direita = e();
                valor = verdadeiro(valor) || verdadeiro(direita);
            }
            return valor;
        }

        private Object e() {
            Object valor = comparacao();
            while (aceitar("and")) {
                Object direita = comparacao();
                valor = verdadeiro(valor) && verdadeiro(direita);
            }
            return valor;
        }

        private Object comparacao() {
            Object valor = valor();
            if (pos < tokens.size() && tokens.get(pos).tipo() == Tipo.OPERADOR) {
                String operador = tokens.get(pos++).texto();
                return comparar(valor, operador, valor());
            }
            return valor;
        }

        private Object valor() {
            if (pos >= tokens.size()) {
                throw new ErroSintaxe(SINTAXE_INVALIDA);
            }
            Token t = tokens.get(pos++);
            switch (t.tipo()) {
                case TEXTO:
                    return t.texto();
                case NUMERO:
                    return new BigDecimal(t.texto());
                case ABRE:
                    Object valor = ou();
                    if (pos >= tokens.size() || tokens.get(pos).tipo() != Tipo.FECHA) {
                        throw new ErroSintaxe(SINTAXE_INVALIDA);
                    }
                    pos++;
                    return valor;
                case NOME:
                    if (t.texto().equals("true") || t.texto().equals("false")) {
                        return t.texto().equals("true");
                    }
                    int coluna = COLUNAS.indexOf(t.texto());
                    if (coluna < 0) {
                        throw new ErroSintaxe(SINTAXE_INVALIDA);
                    }
                    return coluna == 0 ? new BigDecimal(linha.get(0)) : linha.get(coluna);
                default:
                    throw new ErroSintaxe(SINTAXE_INVALIDA);
            }
        }

        private boolean aceitar(String palavra) {
            if (pos < tokens.size() && tokens.get(pos).tipo() == Tipo.NOME && tokens.get(pos).texto().equals(palavra)) {
                pos++;
                return true;
            }
            return false;
        }
    }
}
