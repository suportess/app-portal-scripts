package br.tec.suportes.portalscripts.domain.database;

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
public class DatabaseRequest {

    private String key;
    private String driver;
    private String host;
    private Integer port;
    private String dbName;
    private String serviceName;
    private String sid;
    private String connectString;
    private String user;
    private String password;
    private DatabasePool pool;
}
