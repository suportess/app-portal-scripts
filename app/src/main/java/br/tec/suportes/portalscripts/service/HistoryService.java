package br.tec.suportes.portalscripts.service;

import br.tec.suportes.portalscripts.domain.migration.MigrationHistory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gerencia o histórico de migrations executadas.
 * Persiste em um arquivo JSON local: portal_scripts_history.json
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final ObjectMapper objectMapper;

    @Value("${portal.history-file:portal_scripts_history.json}")
    private String historyFile;

    /**
     * Retorna o conjunto de versões já executadas com sucesso.
     */
    public Set<String> loadExecutedVersions() {
        List<MigrationHistory> history = loadHistory();
        return history.stream()
                .filter(MigrationHistory::isSuccess)
                .map(MigrationHistory::getVersion)
                .collect(Collectors.toSet());
    }

    /**
     * Registra uma migration executada com sucesso.
     */
    public void recordSuccess(String version, String filename, String type, String content) {
        List<MigrationHistory> history = loadHistory();
        history.add(MigrationHistory.builder()
                .version(version)
                .filename(filename)
                .type(type)
                .executedAt(LocalDateTime.now())
                .success(true)
                .checksum(sha256(content))
                .build());
        saveHistory(history);
        log.info("✔ Histórico atualizado: {} [{}]", filename, version);
    }

    /**
     * Registra uma migration que falhou.
     */
    public void recordFailure(String version, String filename, String type, String content, String errorMessage) {
        List<MigrationHistory> history = loadHistory();
        history.add(MigrationHistory.builder()
                .version(version)
                .filename(filename)
                .type(type)
                .executedAt(LocalDateTime.now())
                .success(false)
                .errorMessage(errorMessage)
                .checksum(sha256(content))
                .build());
        saveHistory(history);
        log.warn("✘ Falha registrada no histórico: {} — {}", filename, errorMessage);
    }

    /**
     * Verifica se o conteúdo de um script já executado mudou (checksum diferente).
     * Usado para alertar o desenvolvedor sobre alterações indevidas.
     */
    public boolean checksumChanged(String version, String currentContent) {
        return loadHistory().stream()
                .filter(h -> h.getVersion().equals(version) && h.isSuccess())
                .findFirst()
                .map(h -> !h.getChecksum().equals(sha256(currentContent)))
                .orElse(false);
    }

    // ---- private ----

    private List<MigrationHistory> loadHistory() {
        File file = new File(historyFile);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(file, new TypeReference<List<MigrationHistory>>() {});
        } catch (IOException e) {
            log.error("Erro ao ler histórico de migrations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private void saveHistory(List<MigrationHistory> history) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(historyFile), history);
        } catch (IOException e) {
            log.error("Erro ao salvar histórico de migrations: {}", e.getMessage());
            throw new RuntimeException("Falha ao persistir histórico de migrations", e);
        }
    }

    private String sha256(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 não disponível", e);
        }
    }
}
