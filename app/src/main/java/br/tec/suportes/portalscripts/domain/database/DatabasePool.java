package br.tec.suportes.portalscripts.domain.database;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabasePool {

    private Integer maxOpenConns;
    private Integer maxIdleConns;

    @JsonProperty("connMaxLifetimeSec")
    private Integer connMaxLifetimeSec;

    @JsonProperty("connMaxIdleTimeSec")
    private Integer connMaxIdleTimeSec;
}
