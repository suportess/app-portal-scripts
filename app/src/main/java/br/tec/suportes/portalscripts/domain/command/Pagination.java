package br.tec.suportes.portalscripts.domain.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Pagination {

    private String paramPagina;
    private String paramTamanhoPagina;
    private Integer tamanhoPaginaPadrao;
    private Integer tamanhoPaginaMaximo;
}
