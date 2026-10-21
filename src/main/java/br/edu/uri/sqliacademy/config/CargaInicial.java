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

/**
 * Carga inicial de dados, executada a cada inicializacao.
 * Idempotente: so cria o que ainda nao existe e nunca altera registros existentes.
 */
@Component
public class CargaInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private static final int PONTOS_LABORATORIO = 100;

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
                "Um sistema de login fictício monta a consulta SQL concatenando o e-mail e a senha digitados. "
                        + "Preencha os campos de forma que a consulta resultante sempre encontre um usuário, "
                        + "entrando sem conhecer a senha. A consulta é apenas montada e exibida: nada é executado no banco.");
        criarConteudoProvisorio(introducao);

        Modulo identificacao = criarModulo(2, "Identificação",
                "Como reconhecer código vulnerável a SQL Injection.");
        criarLaboratorio(identificacao, "LAB2", "Encontre a linha vulnerável",
                "Analise os trechos de código apresentados e aponte, em cada um, a linha que permite SQL Injection.");
        criarConteudoProvisorio(identificacao);

        Modulo prevencao = criarModulo(3, "Prevenção",
                "Como escrever consultas seguras com PreparedStatement e outras boas práticas.");
        criarLaboratorio(prevencao, "LAB3", "Código vulnerável x código seguro",
                "Compare uma implementação vulnerável com outra que usa PreparedStatement "
                        + "e identifique qual delas impede o SQL Injection e por quê.");
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

    private void criarLaboratorio(Modulo modulo, String codigo, String titulo, String enunciado) {
        if (laboratorioRepository.existsByCodigo(codigo)) {
            return;
        }
        // Explicacao fica vazia ate a tarefa 18
        laboratorioRepository.save(new Laboratorio(modulo, codigo, titulo, enunciado, null, PONTOS_LABORATORIO));
        log.info("Carga inicial: laboratorio {} criado.", codigo);
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
