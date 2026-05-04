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
public class SqlParameter {

    private String nome;
    private String tipo;
    private String operador;
    private boolean obrigatorio;
    private Boolean jaAdicionado;
}
