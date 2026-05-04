package br.tec.suportes.portalscripts.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "portal")
public class PortalProperties {

    /** URL base do portal, ex: http://localhost:8080 */
    private String url = "http://localhost:8080";

    /** Bearer token (GATEWAY_API_KEY configurado no portal) */
    private String apiKey = "gateway-default-api-key-2025";

    /** Timeout em segundos para chamadas HTTP ao portal */
    private int timeoutSeconds = 30;

    /** Diretório onde ficam os scripts de migration (relativo ao classpath ou absoluto) */
    private String scriptsDir = "classpath:migrations";
}
