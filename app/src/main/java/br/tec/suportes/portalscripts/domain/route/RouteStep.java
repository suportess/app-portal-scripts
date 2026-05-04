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
public class RouteStep {

    private String alias;
    private boolean abortarSemDados;
    private RouteCommand comando;
}
