package br.tec.suportes.portalscripts.domain.route;

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
public class RouteCommand {

    private String tipo;
    private String database;
    private String nome;
    private boolean retornarResultado;
    private List<RouteParameter> parametros;
}
