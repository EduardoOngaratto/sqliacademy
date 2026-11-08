package br.edu.uri.sqliacademy.config;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Laboratorio;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.entity.Perfil;
import br.edu.uri.sqliacademy.entity.Usuario;
import br.edu.uri.sqliacademy.repository.ConteudoRepository;
import br.edu.uri.sqliacademy.repository.LaboratorioRepository;
import br.edu.uri.sqliacademy.repository.ModuloRepository;
import br.edu.uri.sqliacademy.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

/**
 * Carga inicial de dados, executada a cada inicializacao.
 * Idempotente: so cria o que ainda nao existe e nao altera registros existentes
 * (unica excecao: preencher a explicacao de um laboratorio que ainda esteja vazia).
 */
@Component
public class CargaInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private static final int PONTOS_LABORATORIO = 100;

    // Exibida ao resolver o Lab 1 (HTML confiavel, renderizado com th:utext)
    private static final String EXPLICACAO_LAB1 = """
            <h3 class="h6">O que aconteceu</h3>
            <p>O sistema montou a consulta juntando (concatenando) o texto digitado com o código SQL:</p>
            <pre><code class="language-java">String sql = "SELECT * FROM usuario WHERE login = '" + login
                    + "' AND senha = '" + senha + "'";</code></pre>
            <p>Ao digitar uma aspa simples, você fechou o texto antes da hora e passou a escrever
            <strong>código SQL</strong> dentro da consulta. Com uma condição sempre verdadeira (como
            <code>OR 1=1</code>) ou com um comentário (<code>--</code> ou <code>#</code>) que descarta a checagem
            da senha, o <code>WHERE</code> passou a aceitar um usuário sem a senha correta.</p>
            <p>Detalhe importante: o <code>AND</code> é avaliado antes do <code>OR</code>. Por isso
            <code>' OR '1'='1</code> funciona no campo senha (fica como a última condição), mas não sozinho no
            campo usuário, onde a checagem da senha continua valendo.</p>
            <p>Observação: no MySQL, o comentário <code>--</code> exige um espaço depois dos traços
            (<code>--&nbsp;</code>); sem o espaço, a consulta dá erro de sintaxe. Neste laboratório as duas formas
            são aceitas, como em outros bancos. O <code>#</code> também é comentário no MySQL.</p>
            <h3 class="h6">Impacto em um sistema real</h3>
            <ul>
                <li>Entrar na conta de qualquer usuário, inclusive a do administrador, sem saber a senha.</li>
                <li>Ver dados de outros usuários: repare que a consulta devolveu até as senhas, guardadas em texto puro.</li>
                <li>A partir daí, alterar ou apagar dados, conforme as permissões do usuário do banco usado pela aplicação.</li>
            </ul>
            <h3 class="h6">Como o código seguro evitaria</h3>
            <p>Com <code>PreparedStatement</code>, a consulta vai para o banco com marcadores (<code>?</code>) e os
            valores são enviados separados. O banco trata a entrada sempre como dado, nunca como código: a aspa
            digitada vira apenas mais um caractere do login, e o ataque não funciona.</p>
            <pre><code class="language-java">String sql = "SELECT * FROM usuario WHERE login = ? AND senha = ?";
            PreparedStatement ps = conexao.prepareStatement(sql);
            ps.setString(1, login);
            ps.setString(2, senha);
            ResultSet rs = ps.executeQuery();</code></pre>
            <p>Além disso, senhas nunca devem ser guardadas em texto puro: o correto é armazenar um hash
            (por exemplo, BCrypt) e conferir a senha no código.</p>
            """;

    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final ConteudoRepository conteudoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminNome;
    private final String adminEmail;
    private final String adminSenha;

    public CargaInicial(UsuarioRepository usuarioRepository,
                        ModuloRepository moduloRepository,
                        LaboratorioRepository laboratorioRepository,
                        ConteudoRepository conteudoRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${sqliacademy.admin.nome}") String adminNome,
                        @Value("${sqliacademy.admin.email}") String adminEmail,
                        @Value("${sqliacademy.admin.senha}") String adminSenha) {
        this.usuarioRepository = usuarioRepository;
        this.moduloRepository = moduloRepository;
        this.laboratorioRepository = laboratorioRepository;
        this.conteudoRepository = conteudoRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminNome = adminNome;
        this.adminEmail = adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminSenha = adminSenha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarAdmin();

        Modulo introducao = criarModulo(1, "Introdução",
                "O que é SQL Injection, como a falha acontece e por que ela é perigosa.");
        criarLaboratorio(introducao, "LAB1", "Burlando um login",
                "O login de uma loja fictícia monta a consulta SQL concatenando o usuário e a senha digitados. "
                        + "Preencha os campos de forma que a consulta resultante encontre um usuário, "
                        + "entrando sem conhecer a senha. A consulta é apenas montada e exibida: nada é executado no banco.",
                EXPLICACAO_LAB1);
        criarConteudoProvisorio(introducao);

        Modulo identificacao = criarModulo(2, "Identificação",
                "Como reconhecer código vulnerável a SQL Injection.");
        criarLaboratorio(identificacao, "LAB2", "Encontre a linha vulnerável",
                "Analise os trechos de código apresentados e aponte, em cada um, a linha que permite SQL Injection.",
                null);
        criarConteudoProvisorio(identificacao);

        Modulo prevencao = criarModulo(3, "Prevenção",
                "Como escrever consultas seguras com PreparedStatement e outras boas práticas.");
        criarLaboratorio(prevencao, "LAB3", "Código vulnerável x código seguro",
                "Compare uma implementação vulnerável com outra que usa PreparedStatement "
                        + "e identifique qual delas impede o SQL Injection e por quê.",
                null);
        criarConteudoProvisorio(prevencao);
    }

    private void criarAdmin() {

        if (usuarioRepository.existsByEmail(adminEmail)) {
            return;
        }
        // Mesmas regras de senha do cadastro (8 a 64 caracteres)
        if (adminSenha == null || adminSenha.length() < 8 || adminSenha.length() > 64) {
            log.warn("Admin {} NAO criado: defina a variavel de ambiente ADMIN_PASSWORD (8 a 64 caracteres).", adminEmail);
            return;
        }
        usuarioRepository.save(new Usuario(adminNome, adminEmail, passwordEncoder.encode(adminSenha), Perfil.ADMIN));
        log.info("Carga inicial: admin {} criado.", adminEmail);
    }

    // Busca pela ordem (e nao pelo titulo), pois o titulo podera ser editado pelo admin
    private Modulo criarModulo(int ordem, String titulo, String descricao) {
        return moduloRepository.findByOrdem(ordem).orElseGet(() -> {
            log.info("Carga inicial: modulo {} - {} criado.", ordem, titulo);
            return moduloRepository.save(new Modulo(titulo, descricao, ordem));
        });
    }

    // Excecao a regra de nao alterar: a explicacao e preenchida em lab existente se ainda estiver vazia
    private void criarLaboratorio(Modulo modulo, String codigo, String titulo, String enunciado, String explicacao) {
        Optional<Laboratorio> existente = laboratorioRepository.findByCodigo(codigo);
        if (existente.isEmpty()) {
            laboratorioRepository.save(new Laboratorio(modulo, codigo, titulo, enunciado, explicacao, PONTOS_LABORATORIO));
            log.info("Carga inicial: laboratorio {} criado.", codigo);
            return;
        }
        Laboratorio laboratorio = existente.get();
        if (laboratorio.getExplicacao() == null && explicacao != null) {
            laboratorio.setExplicacao(explicacao);
            laboratorioRepository.save(laboratorio);
            log.info("Carga inicial: explicacao do laboratorio {} preenchida.", codigo);
        }
    }

    // Provisorio: os textos definitivos entram na tarefa 13
    private void criarConteudoProvisorio(Modulo modulo) {
        if (conteudoRepository.existsByModulo(modulo)) {
            return;
        }
        conteudoRepository.save(new Conteudo(modulo, "Conteúdo em construção",
                "<p>O conteúdo deste módulo será disponibilizado em breve.</p>", 1));
        log.info("Carga inicial: conteudo provisorio do modulo {} criado.", modulo.getOrdem());
    }
}
