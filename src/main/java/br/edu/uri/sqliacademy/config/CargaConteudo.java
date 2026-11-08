package br.edu.uri.sqliacademy.config;

import br.edu.uri.sqliacademy.entity.Conteudo;
import br.edu.uri.sqliacademy.entity.Modulo;
import br.edu.uri.sqliacademy.repository.ConteudoRepository;
import br.edu.uri.sqliacademy.repository.ProgressoConteudoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carrega os conteudos a partir de arquivos em src/main/resources/conteudos/.
 * Chamada pela CargaInicial, dentro da mesma transacao.
 *
 * Um arquivo por conteudo, "modulo{N}-{ordem}.html" (ex.: modulo1-2.html).
 * O primeiro <h1> do arquivo vira o titulo e sai do corpo. Os arquivos sao a fonte do texto:
 * conteudo novo e criado e conteudo existente (mesmo modulo e ordem) e atualizado se mudou.
 */
@Component
public class CargaConteudo {

    static final String TITULO_PROVISORIO = "Conteúdo em construção";

    private static final Logger log = LoggerFactory.getLogger(CargaConteudo.class);
    private static final Pattern NOME_ARQUIVO = Pattern.compile("modulo(\\d+)-(\\d+)\\.html");
    private static final Pattern TITULO = Pattern.compile("<h1[^>]*>(.*?)</h1>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    private final ConteudoRepository conteudoRepository;
    private final ProgressoConteudoRepository progressoConteudoRepository;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public CargaConteudo(ConteudoRepository conteudoRepository,
                         ProgressoConteudoRepository progressoConteudoRepository) {
        this.conteudoRepository = conteudoRepository;
        this.progressoConteudoRepository = progressoConteudoRepository;
    }

    public void carregarConteudos(Modulo modulo) {
        List<Resource> arquivos = arquivosDoModulo(modulo);
        if (arquivos.isEmpty()) {
            return;
        }
        removerProvisorio(modulo);

        for (Resource arquivo : arquivos) {
            int ordem = ordem(arquivo);
            String html = ler(arquivo);
            Matcher titulo = TITULO.matcher(html);
            if (!titulo.find()) {
                log.warn("Carga de conteudo: {} ignorado (falta o <h1> com o titulo).", arquivo.getFilename());
                continue;
            }
            String textoTitulo = titulo.group(1).replaceAll("<[^>]+>", "").strip();
            String corpo = (html.substring(0, titulo.start()) + html.substring(titulo.end())).strip();

            Optional<Conteudo> existente = conteudoRepository.findByModuloAndOrdem(modulo, ordem);
            if (existente.isEmpty()) {
                conteudoRepository.save(new Conteudo(modulo, textoTitulo, corpo, ordem));
                log.info("Carga de conteudo: {} criado.", arquivo.getFilename());
            } else if (!existente.get().getTitulo().equals(textoTitulo) || !existente.get().getCorpo().equals(corpo)) {
                Conteudo conteudo = existente.get();
                conteudo.setTitulo(textoTitulo);
                conteudo.setCorpo(corpo);
                conteudoRepository.save(conteudo);
                log.info("Carga de conteudo: {} atualizado.", arquivo.getFilename());
            }
        }
    }

    // O provisorio sai quando o modulo ganha conteudo de verdade; antes, o progresso dele (FK)
    private void removerProvisorio(Modulo modulo) {
        for (Conteudo conteudo : conteudoRepository.findByModuloOrderByOrdemAsc(modulo)) {
            if (TITULO_PROVISORIO.equals(conteudo.getTitulo())) {
                progressoConteudoRepository.deleteByConteudo(conteudo);
                conteudoRepository.delete(conteudo);
                conteudoRepository.flush();
                log.info("Carga de conteudo: conteudo provisorio do modulo {} removido.", modulo.getOrdem());
            }
        }
    }

    private List<Resource> arquivosDoModulo(Modulo modulo) {
        try {
            List<Resource> arquivos = new ArrayList<>();
            for (Resource r : resolver.getResources("classpath*:conteudos/modulo" + modulo.getOrdem() + "-*.html")) {
                if (r.getFilename() != null && NOME_ARQUIVO.matcher(r.getFilename()).matches()) {
                    arquivos.add(r);
                }
            }
            arquivos.sort(Comparator.comparingInt(CargaConteudo::ordem));
            return arquivos;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int ordem(Resource arquivo) {
        Matcher m = NOME_ARQUIVO.matcher(arquivo.getFilename());
        m.matches();
        return Integer.parseInt(m.group(2));
    }

    private static String ler(Resource arquivo) {
        try {
            return arquivo.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
