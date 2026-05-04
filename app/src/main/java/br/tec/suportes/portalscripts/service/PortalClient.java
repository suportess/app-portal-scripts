package br.tec.suportes.portalscripts.service;

import br.tec.suportes.portalscripts.config.PortalProperties;
import br.tec.suportes.portalscripts.domain.command.CommandRequest;
import br.tec.suportes.portalscripts.domain.database.DatabaseRequest;
import br.tec.suportes.portalscripts.domain.route.RouteRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente HTTP para o Portal.
 * Realiza POST /commands e POST /routes com autenticação Bearer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortalClient {

    private final PortalProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Cria ou atualiza uma conexão de banco no portal (upsert).
     * POST /databases — se 409, busca o ID e faz DELETE + POST (databases não têm PUT).
     */
    public void createDatabase(DatabaseRequest request) {
        String baseUrl = properties.getUrl() + "/databases";
        log.debug("→ POST {} | key={}", baseUrl, request.getKey());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, buildEntity(request), String.class);
            log.info("  ✔ Database criado: {} (HTTP {})", request.getKey(), response.getStatusCode().value());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String id = findDatabaseId(request.getKey());
                if (id != null) {
                    restTemplate.exchange(baseUrl + "/" + id, HttpMethod.DELETE, buildEmptyEntity(), String.class);
                    restTemplate.exchange(baseUrl, HttpMethod.POST, buildEntity(request), String.class);
                    log.info("  ✔ Database atualizado: {} (DELETE+POST {})", request.getKey(), id);
                } else {
                    log.warn("  ⚠ Database '{}' já existe mas ID não encontrado — pulando", request.getKey());
                }
                return;
            }
            String msg = String.format("Erro ao criar database '%s': HTTP %s — %s",
                    request.getKey(), e.getStatusCode().value(), e.getResponseBodyAsString());
            log.error("  ✘ {}", msg);
            throw new PortalClientException(msg);
        }
    }

    /**
     * Cria ou atualiza um comando no portal (upsert).
     * POST /commands — se 409, busca o ID e faz PUT /commands/{id}.
     */
    public void createCommand(CommandRequest request) {
        String baseUrl = properties.getUrl() + "/commands";
        log.debug("→ POST {} | chave={}", baseUrl, request.getChave());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, buildEntity(request), String.class);
            log.info("  ✔ Comando criado: {} (HTTP {})", request.getChave(), response.getStatusCode().value());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String id = findCommandId(request.getChave());
                if (id != null) {
                    String putUrl = baseUrl + "/" + id;
                    restTemplate.exchange(putUrl, HttpMethod.PUT, buildEntity(request), String.class);
                    log.info("  ✔ Comando atualizado: {} (PUT {})", request.getChave(), id);
                } else {
                    log.warn("  ⚠ Comando '{}' já existe mas ID não encontrado — pulando", request.getChave());
                }
                return;
            }
            String msg = String.format("Erro ao criar command '%s': HTTP %s — %s",
                    request.getChave(), e.getStatusCode().value(), e.getResponseBodyAsString());
            log.error("  ✘ {}", msg);
            throw new PortalClientException(msg);
        }
    }

    /**
     * Cria ou atualiza uma rota no portal (upsert).
     * POST /routes — se 409, busca o ID e faz DELETE + POST (rotas não têm PUT).
     */
    public void createRoute(RouteRequest request) {
        String baseUrl = properties.getUrl() + "/routes";
        log.debug("→ POST {} | chave={}", baseUrl, request.getChave());

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, buildEntity(request), String.class);
            log.info("  ✔ Rota criada: {} {} (HTTP {})",
                    request.getMetodo(), request.getCaminho(), response.getStatusCode().value());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                String id = findRouteId(request.getChave());
                if (id != null) {
                    restTemplate.exchange(baseUrl + "/" + id, HttpMethod.DELETE, buildEmptyEntity(), String.class);
                    restTemplate.exchange(baseUrl, HttpMethod.POST, buildEntity(request), String.class);
                    log.info("  ✔ Rota atualizada: {} {} (DELETE+POST {})",
                            request.getMetodo(), request.getCaminho(), id);
                } else {
                    log.warn("  ⚠ Rota '{}' já existe mas ID não encontrado — pulando", request.getChave());
                }
                return;
            }
            String msg = String.format("Erro ao criar route '%s': HTTP %s — %s",
                    request.getChave(), e.getStatusCode().value(), e.getResponseBodyAsString());
            log.error("  ✘ {}", msg);
            throw new PortalClientException(msg);
        }
    }

    /**
     * Verifica se um comando já existe consultando GET /commands?key={chave}.
     */
    public boolean commandExists(String chave) {
        String url = properties.getUrl() + "/commands?key=" + chave;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, buildEmptyEntity(), String.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return false;
            }
            JsonNode node = objectMapper.readTree(response.getBody());
            return node.isArray() && !node.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica se uma rota já existe consultando GET /routes?key={chave}.
     */
    public boolean routeExists(String chave) {
        String url = properties.getUrl() + "/routes?key=" + chave;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, buildEmptyEntity(), String.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                return false;
            }
            JsonNode node = objectMapper.readTree(response.getBody());
            return node.isArray() && !node.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // ---- private ----

    private String findDatabaseId(String key) {
        String url = properties.getUrl() + "/databases?key=" + key;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, buildEmptyEntity(), String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            if (node.isArray() && !node.isEmpty()) {
                return node.get(0).path("id").asText(null);
            }
        } catch (Exception e) {
            log.warn("  ⚠ Não foi possível obter ID do database '{}': {}", key, e.getMessage());
        }
        return null;
    }

    private String findCommandId(String chave) {
        String url = properties.getUrl() + "/commands?key=" + chave;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, buildEmptyEntity(), String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            if (node.isArray() && !node.isEmpty()) {
                return node.get(0).path("id").asText(null);
            }
        } catch (Exception e) {
            log.warn("  ⚠ Não foi possível obter ID do command '{}': {}", chave, e.getMessage());
        }
        return null;
    }

    private String findRouteId(String chave) {
        String url = properties.getUrl() + "/routes?key=" + chave;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, buildEmptyEntity(), String.class);
            JsonNode node = objectMapper.readTree(response.getBody());
            if (node.isArray() && !node.isEmpty()) {
                return node.get(0).path("id").asText(null);
            }
        } catch (Exception e) {
            log.warn("  ⚠ Não foi possível obter ID da route '{}': {}", chave, e.getMessage());
        }
        return null;
    }

    private <T> HttpEntity<T> buildEntity(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<Void> buildEmptyEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(properties.getApiKey());
        return new HttpEntity<>(headers);
    }


    public static class PortalClientException extends RuntimeException {
        public PortalClientException(String message) {
            super(message);
        }
    }
}
