# 00 — Visão Geral: portal-scripts

## Propósito

`portal-scripts` é um **migration runner** que automatiza a criação de *commands* e *routes* no [Portal](../../portal). Funciona de forma análoga ao Flyway/Liquibase: lê arquivos JSON versionados, aplica cada um no Portal via API REST e registra o resultado em um arquivo de histórico local — garantindo que o mesmo script **nunca seja reexecutado**.

---

## Stack tecnológica

| Tecnologia | Versão | Uso |
|---|---|---|
| Java | 21 | Linguagem |
| Spring Boot | 3.2.x | Framework principal |
| Spring Web | 3.2.x | `RestTemplate` para HTTP |
| Jackson | 2.17.x | Serialização/deserialização JSON |
| Lombok | 1.18.x | Redução de boilerplate |
| Maven | 3.9+ | Build e dependências |
| JUnit 5 + Mockito | — | Testes unitários |

---

## Conceito central: Migration

Cada arquivo JSON em `src/main/resources/migrations/` representa uma **migration**:

- Versão monotônica (`V01`, `V02`, …)
- Tipo: `commands` ou `routes`
- Conteúdo: array JSON de payloads válidos para o Portal

O runner executa as migrations **em ordem crescente de versão**, registra o resultado e **nunca reaplica** uma versão que já foi executada com sucesso.

---

## Estrutura de pastas

```
portal-scripts/
├── README.md
└── app/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/br/tec/suportes/portalscripts/
        │   │   ├── PortalScriptsApplication.java   ← entry point Spring Boot
        │   │   ├── config/
        │   │   │   ├── AppConfig.java              ← beans RestTemplate, ObjectMapper
        │   │   │   └── PortalProperties.java       ← @ConfigurationProperties
        │   │   ├── domain/
        │   │   │   ├── command/                    ← DTOs de command
        │   │   │   ├── route/                      ← DTOs de route
        │   │   │   └── migration/                  ← MigrationScript, MigrationHistory
        │   │   ├── runner/
        │   │   │   └── MigrationApplicationRunner  ← ApplicationRunner (disparo)
        │   │   └── service/
        │   │       ├── MigrationRunner.java        ← orquestração
        │   │       ├── ScriptLoader.java           ← descoberta e parse dos JSONs
        │   │       ├── PortalClient.java           ← HTTP client para o Portal
        │   │       └── HistoryService.java         ← persistência do histórico
        │   └── resources/
        │       ├── application.properties
        │       ├── logback-spring.xml
        │       └── migrations/                     ← scripts JSON versionados
        └── test/
            └── java/br/tec/suportes/portalscripts/
                └── service/
                    └── MigrationRunnerTest.java
```

---

## Variáveis de configuração

| Propriedade | Padrão | Descrição |
|---|---|---|
| `portal.url` | `http://localhost:8080` | URL base do Portal |
| `portal.api-key` | `gateway-default-api-key-2025` | Bearer token (`GATEWAY_API_KEY` do Portal) |
| `portal.timeout-seconds` | `30` | Timeout de conexão e leitura HTTP |
| `portal.history-file` | `portal_scripts_history.json` | Caminho do arquivo de histórico |

Todas as propriedades podem ser sobrescritas via args `--portal.url=...` ou variáveis de ambiente `PORTAL_URL=...`.

---

## Como executar

```bash
# Desenvolvimento
cd portal-scripts/app
mvn spring-boot:run

# Produção
mvn clean package -DskipTests
java -jar target/portal-scripts-1.0.0.jar \
  --portal.url=http://meu-portal:8080 \
  --portal.api-key=minha-chave
```

A aplicação executa as migrations e **encerra com `exit 0`** em sucesso ou **`exit 1`** em falha — ideal para pipelines CI/CD.

---

## Docs relacionados

- [01-convencao-scripts.md](./01-convencao-scripts.md) — Convenção de nomenclatura e estrutura dos JSON
- [02-regras-negocio.md](./02-regras-negocio.md) — Regras de execução, histórico e idempotência
- [03-scripts-migrations.md](./03-scripts-migrations.md) — Catálogo de todos os scripts incluídos (V01–V13)
