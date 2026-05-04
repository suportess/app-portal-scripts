package br.tec.suportes.portalscripts.service;

import br.tec.suportes.portalscripts.domain.migration.MigrationScript;
import br.tec.suportes.portalscripts.domain.migration.MigrationScript.ScriptType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Carrega os scripts de migration do classpath (migrations/*.json)
 * e os ordena pela versão.
 *
 * Convenção de nome:
 *   V{nn}__{tipo}_{descricao}.json
 *   onde tipo = commands | routes
 *
 * Exemplos:
 *   V01__commands_produto.json
 *   V02__commands_entrada.json
 *   V03__routes_produto.json
 */
@Slf4j
@Service
public class ScriptLoader {

    private static final Pattern FILENAME_PATTERN =
            Pattern.compile("^(V\\d+)__(databases|commands|routes)_(.+)\\.json$", Pattern.CASE_INSENSITIVE);

    private final PathMatchingResourcePatternResolver resolver =
            new PathMatchingResourcePatternResolver();

    /**
     * Descobre, ordena e retorna todos os scripts de migration do classpath.
     */
    public List<MigrationScript> loadAll() throws IOException {
        Resource[] resources = resolver.getResources("classpath:migrations/*.json");

        log.info("Scripts encontrados no classpath:migrations: {}", resources.length);

        return Arrays.stream(resources)
                .map(this::parse)
                .filter(s -> s != null)
                .sorted(Comparator.comparing(MigrationScript::getVersion))
                .toList();
    }

    private MigrationScript parse(Resource resource) {
        String filename = resource.getFilename();
        if (filename == null) return null;

        Matcher matcher = FILENAME_PATTERN.matcher(filename);
        if (!matcher.matches()) {
            log.warn("Arquivo ignorado (nome fora do padrão V{{nn}}__{{tipo}}_{{descricao}}.json): {}", filename);
            return null;
        }

        String version = matcher.group(1).toUpperCase();
        String typeStr = matcher.group(2).toUpperCase();
        String description = matcher.group(3);

        ScriptType type = switch (typeStr) {
            case "DATABASES" -> ScriptType.DATABASE;
            case "COMMANDS"  -> ScriptType.COMMAND;
            default          -> ScriptType.ROUTE;
        };

        try {
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            return MigrationScript.builder()
                    .version(version)
                    .description(description)
                    .filename(filename)
                    .type(type)
                    .content(content)
                    .build();
        } catch (IOException e) {
            log.error("Erro ao ler script {}: {}", filename, e.getMessage());
            return null;
        }
    }
}
