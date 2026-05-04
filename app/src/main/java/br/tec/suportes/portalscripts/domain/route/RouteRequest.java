package br.tec.suportes.portalscripts.domain.route;

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
public class RouteRequest {

    private String chave;
    private String caminho;
    private String metodo;
    private RouteResponse resposta;
    private RouteService servico;
}
