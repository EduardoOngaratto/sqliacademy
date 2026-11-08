package br.edu.uri.sqliacademy.lab;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Lab 3 - escolher, entre 4 versoes "corrigidas" do login do Lab 1, a unica realmente segura.
 * As erradas sao armadilhas comuns: String.format, replace manual de aspas e validacao so no JavaScript.
 */
@Component
public class Lab3Validador implements ValidadorLaboratorio {

    static final String RESPOSTA = "C";
    static final String SEM_RESPOSTA = "Escolha uma das versões.";
    static final String ACERTO = "Correto! Na versão C, o SQL vai para o banco só com os marcadores ? "
            + "e os valores seguem separados, como dados. Agora repita o ataque do Lab 1 contra ela, logo abaixo.";

    private static final String ORIGINAL = """
            String sql = "SELECT * FROM usuario WHERE login = '" + login
                    + "' AND senha = '" + senha + "'";
            Statement st = conexao.createStatement();
            ResultSet rs = st.executeQuery(sql);""";

    private static final List<VersaoCodigo> VERSOES = List.of(
            new VersaoCodigo("A", """
                    String sql = String.format(
                            "SELECT * FROM usuario WHERE login = '%s' AND senha = '%s'", login, senha);
                    Statement st = conexao.createStatement();
                    ResultSet rs = st.executeQuery(sql);""",
                    null,
                    "String.format é só outra forma de concatenar: o %s é trocado pelo texto digitado antes de a "
                            + "consulta ir para o banco. A aspa do ataque continua fechando o texto, e o resto vira código."),
            new VersaoCodigo("B", """
                    String loginLimpo = login.replace("'", "''");
                    String senhaLimpa = senha.replace("'", "''");
                    String sql = "SELECT * FROM usuario WHERE login = '" + loginLimpo
                            + "' AND senha = '" + senhaLimpa + "'";
                    Statement st = conexao.createStatement();
                    ResultSet rs = st.executeQuery(sql);""",
                    null,
                    "Trocar ' por '' parece resolver, mas é frágil: no MySQL a barra invertida também escapa caracteres "
                            + "(com \\' a aspa seguinte volta a fechar o texto), campos numéricos nem usam aspas (lembre do "
                            + "Lab 2) e basta esquecer um campo. Escapar na mão é refazer, com falhas, o que o banco já sabe fazer."),
            new VersaoCodigo("C", """
                    String sql = "SELECT * FROM usuario WHERE login = ? AND senha = ?";
                    PreparedStatement ps = conexao.prepareStatement(sql);
                    ps.setString(1, login);
                    ps.setString(2, senha);
                    ResultSet rs = ps.executeQuery();""",
                    null,
                    null),
            new VersaoCodigo("D", """
                    String sql = "SELECT * FROM usuario WHERE login = '" + login
                            + "' AND senha = '" + senha + "'";
                    Statement st = conexao.createStatement();
                    ResultSet rs = st.executeQuery(sql);""",
                    """
                    formulario.addEventListener('submit', function (evento) {
                        if (/['";-]/.test(login.value) || /['";-]/.test(senha.value)) {
                            evento.preventDefault();
                            alert('Caracteres inválidos!');
                        }
                    });""",
                    "A validação no navegador ajuda o usuário comum, mas não protege o servidor: o atacante desativa o "
                            + "JavaScript ou envia a requisição direto (pelas ferramentas do navegador, por exemplo), e o "
                            + "servidor continua concatenando a entrada no SQL."));

    private static final List<String> DICAS = List.of(
            "Pergunte, para cada versão: a entrada do usuário ainda vira parte do texto do SQL?",
            "Tudo o que roda no navegador pode ser desligado ou contornado pelo atacante. "
                    + "A proteção precisa estar no servidor.",
            "Escapar aspas na mão costuma deixar brechas (outros caracteres de escape, campos numéricos). "
                    + "Procure a versão em que o SQL e os valores viajam separados.");

    @Override
    public String getCodigo() {
        return "LAB3";
    }

    @Override
    public List<String> getCampos() {
        return List.of("versao");
    }

    @Override
    public List<String> getDicas() {
        return DICAS;
    }

    @Override
    public Map<String, Object> getModelo() {
        return Map.of("original", ORIGINAL, "versoes", VERSOES);
    }

    @Override
    public ResultadoValidacao validar(Map<String, String> campos) {
        String escolha = campos.getOrDefault("versao", "");
        if (RESPOSTA.equals(escolha)) {
            return new ResultadoValidacao(true, null, ACERTO);
        }
        for (VersaoCodigo versao : VERSOES) {
            if (versao.getLetra().equals(escolha)) {
                return new ResultadoValidacao(false, null, "Versão " + escolha + ": " + versao.explicacaoErro());
            }
        }
        return new ResultadoValidacao(false, null, SEM_RESPOSTA);
    }
}
