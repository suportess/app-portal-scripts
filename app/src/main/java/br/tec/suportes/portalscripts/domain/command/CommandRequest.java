package br.tec.suportes.portalscripts.domain.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommandRequest {

    private String chave;
    private String descricao;
    private String tipo;
    private String tipoBanco;
    private String sql;
    private String tabela;
    private String rota;
    private String tipoConteudo;
    private String nomeCertificado;
    private CommandBody corpo;
    private CommandBody consulta;
    private List<SqlParameter> parametros;
    private SqlOrder ordenacao;
    private Pagination paginacao;
}
