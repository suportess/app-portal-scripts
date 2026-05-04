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
public class BodyField {

    private String nome;
    private String tipo;
    private boolean obrigatorio;
    private Integer maximo;
    private Integer minimo;
    private String converterPara;
    private String nomeChave;
    private String tipoParametro;
}
