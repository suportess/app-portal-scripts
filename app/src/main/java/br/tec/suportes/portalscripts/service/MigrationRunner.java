package br.tec.suportes.portalscripts.service;

import br.tec.suportes.portalscripts.domain.command.CommandRequest;
import br.tec.suportes.portalscripts.domain.database.DatabaseRequest;
import br.tec.suportes.portalscripts.domain.migration.MigrationScript;
import br.tec.suportes.portalscripts.domain.route.RouteRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Orquestra a execução das migrations:
 * 1. Carrega scripts do classpath (ordem por versão)
 * 2. Compara com histórico — pula scripts já executados com sucesso
 * 3. Aplica cada script no portal (commands ou routes)
 * 4. Registra resultado no histórico
 *
 * Nunca reaplica um script cuja versão já conste como sucesso no histórico.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationRunner {

    private final ScriptLoader scriptLoader;
    private final HistoryService historyService;
    private final PortalClient portalClient;
    private final ObjectMapper objectMapper;

    public void run() {
        log.info("═══════════════════════════════════════════");
        log.info("  Portal Scripts — Migration Runner");
        log.info("═══════════════════════════════════════════");

        List<MigrationScript> scripts;
        try {
            scripts = scriptLoader.loadAll();
        } catch (Exception e) {
            log.error("Erro ao carregar scripts de migration: {}", e.getMessage());
            throw new RuntimeException("Falha ao carregar scripts", e);
        }

        if (scripts.isEmpty()) {
            log.info("Nenhum script de migration encontrado em classpath:migrations/");
            return;
        }

        Set<String> executedVersions = historyService.loadExecutedVersions();
        log.info("Scripts no classpath: {} | Já executados: {}", scripts.size(), executedVersions.size());

        int applied = 0;
        int skipped = 0;
        int failed = 0;

        for (MigrationScript script : scripts) {
            if (executedVersions.contains(script.getVersion())) {
                if (historyService.checksumChanged(script.getVersion(), script.getContent())) {
                    log.warn("⚠ Script {} ({}) foi alterado após execução. Checksum diverge — ignorando alteração.",
                            script.getVersion(), script.getFilename());
                }
                log.debug("  ↷ {} já executado — pulando", script.getVersion());
                skipped++;
                continue;
            }

            log.info("▶ Executando {} ({}) ...", script.getVersion(), script.getFilename());

            try {
                applyScript(script);
                historyService.recordSuccess(
                        script.getVersion(),
                        script.getFilename(),
                        script.getType().name(),
                        script.getContent()
                );
                applied++;
            } catch (Exception e) {
                log.error("✘ Falha em {} ({}): {}", script.getVersion(), script.getFilename(), e.getMessage());
                historyService.recordFailure(
                        script.getVersion(),
                        script.getFilename(),
                        script.getType().name(),
                        script.getContent(),
                        e.getMessage()
                );
                failed++;
                log.error("Pipeline interrompido por falha em {}. Corrija e re-execute.", script.getFilename());
                break;
            }
        }

        log.info("═══════════════════════════════════════════");
        log.info("  Concluído — Aplicados: {} | Pulados: {} | Falhas: {}", applied, skipped, failed);
        log.info("═══════════════════════════════════════════");

        if (failed > 0) {
            throw new RuntimeException("Migration finalizada com " + failed + " falha(s). Verifique os logs.");
        }
    }

    private void applyScript(MigrationScript script) throws Exception {
        switch (script.getType()) {
            case DATABASE -> applyDatabases(script);
            case COMMAND  -> applyCommands(script);
            case ROUTE    -> applyRoutes(script);
        }
    }

    private void applyDatabases(MigrationScript script) throws Exception {
        List<DatabaseRequest> databases = objectMapper.readValue(
                script.getContent(), new TypeReference<List<DatabaseRequest>>() {});
        log.info("  {} database(s) encontrado(s)", databases.size());
        for (DatabaseRequest db : databases) {
            log.info("  → Database: {}", db.getKey());
            portalClient.createDatabase(db);
        }
    }

    private void applyCommands(MigrationScript script) throws Exception {
        List<CommandRequest> commands = objectMapper.readValue(
                script.getContent(), new TypeReference<List<CommandRequest>>() {});
        log.info("  {} comando(s) encontrado(s)", commands.size());
        for (CommandRequest cmd : commands) {
            log.info("  → Command: {}", cmd.getChave());
            portalClient.createCommand(cmd);
        }
    }

    private void applyRoutes(MigrationScript script) throws Exception {
        List<RouteRequest> routes = objectMapper.readValue(
                script.getContent(), new TypeReference<List<RouteRequest>>() {});
        log.info("  {} rota(s) encontrada(s)", routes.size());
        for (RouteRequest route : routes) {
            log.info("  → Route: {} {}", route.getMetodo(), route.getCaminho());
            portalClient.createRoute(route);
        }
    }
}
