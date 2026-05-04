package br.tec.suportes.portalscripts.domain.migration;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Registro de uma migration já executada — persistido no arquivo de histórico.
 * Funciona como o flyway_schema_history: impede reexecução do mesmo script.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MigrationHistory {

    /** Versão do script (ex: "V01") */
    private String version;

    /** Nome completo do arquivo executado */
    private String filename;

    /** Tipo: COMMAND ou ROUTE */
    private String type;

    /** Data/hora da execução */
    private LocalDateTime executedAt;

    /** true = executado com sucesso; false = falhou */
    private boolean success;

    /** Mensagem de erro, se houver */
    private String errorMessage;

    /** Checksum SHA-256 do conteúdo JSON para detectar alterações */
    private String checksum;
}
