package br.tec.suportes.portalscripts.runner;

import br.tec.suportes.portalscripts.service.MigrationRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Ponto de entrada do Spring Boot.
 * Executa as migrations assim que o contexto estiver carregado
 * e encerra a aplicação ao término (sucesso ou falha).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MigrationApplicationRunner implements ApplicationRunner {

    private final MigrationRunner migrationRunner;

    @Override
    public void run(ApplicationArguments args) {
        try {
            migrationRunner.run();
            log.info("Portal Scripts finalizado com sucesso.");
            System.exit(0);
        } catch (Exception e) {
            log.error("Portal Scripts finalizado com erro: {}", e.getMessage());
            System.exit(1);
        }
    }
}
