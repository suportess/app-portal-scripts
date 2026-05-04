package br.tec.suportes.portalscripts.domain.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa um script de migration carregado do classpath.
 * Convenção de nome: V{versão}__{descrição}.json
 * Ex: V01__commands_produto.json, V02__routes_produto.json
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MigrationScript {

    /** Versão extraída do nome do arquivo (ex: "V01") */
    private String version;

    /** Descrição extraída do nome do arquivo (ex: "commands_produto") */
    private String description;

    /** Nome completo do arquivo */
    private String filename;

    /** Tipo do script: DATABASE, COMMAND ou ROUTE */
    private ScriptType type;

    /** Conteúdo JSON bruto do arquivo */
    private String content;

    public enum ScriptType {
        DATABASE, COMMAND, ROUTE
    }
}
