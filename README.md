# portal-scripts

Migration runner para criação de **databases**, **commands** e **routes** no [Portal](../portal).

Funciona de forma análoga ao Flyway/Liquibase: lê arquivos JSON versionados do classpath, aplica cada um no Portal via API REST e registra o resultado em um arquivo de histórico local — garantindo que o mesmo script **nunca seja executado duas vezes**.

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java | 21 |
| Maven | 3.9+ |
| Portal em execução | qualquer |

---

## Configuração

Edite `src/main/resources/application.properties`:

```properties
# URL base do portal
portal.url=http://localhost:8080

# GATEWAY_API_KEY configurado no portal (.env)
portal.api-key=gateway-default-api-key-2025

# Timeout HTTP em segundos
portal.timeout-seconds=30

# Arquivo de histórico (relativo ao diretório de execução)
portal.history-file=portal_scripts_history.json
```

Ou sobrescreva via variáveis de ambiente / argumentos JVM:

```bash
java -jar portal-scripts.jar \
  --portal.url=http://meu-portal:8080 \
  --portal.api-key=minha-chave \
  --portal.history-file=/data/history.json
```

---

## Convenção de nomenclatura dos scripts

Os arquivos devem ficar em `src/main/resources/migrations/` e seguir o padrão:

```
V{nn}__{tipo}_{descricao}.json
```

| Parte | Descrição | Exemplo |
|---|---|---|
| `V{nn}` | Versão com zero-fill, define a ordem de execução | `V01`, `V02`, `V13` |
| `{tipo}` | `databases`, `commands` ou `routes` | `databases`, `commands`, `routes` |
| `{descricao}` | Descrição livre com underscores | `oracle`, `produto`, `entrada_itens` |

**Exemplos:**
```
V01__databases_oracle.json
V02__commands_produto.json
V03__routes_produto.json
```

---

## Estrutura dos scripts JSON

### databases (array de DatabaseRequest)

```json
[
  {
    "key": "oracle-prod",
    "driver": "oracle",
    "host": "localhost",
    "port": 1521,
    "dbName": "XEPDB1",
    "user": "DBAMV",
    "password": "DBAMV",
    "pool": {
      "maxOpenConns": 5,
      "maxIdleConns": 2,
      "connMaxLifetimeSec": 90,
      "connMaxIdleTimeSec": 30
    }
  }
]
```

**Drivers suportados pelo portal:** `oracle`, `postgres`, `mysql`, `sqlserver`

### commands (array de CommandRequest)

```json
[
  {
    "chave": "produto-listar",
    "descricao": "Lista todos os produtos",
    "tipo": "QUERY",
    "tipoBanco": "postgres",
    "sql": "SELECT cd_produto, nm_produto FROM \"DBAMV\".produto",
    "parametros": [
      { "nome": "nm_produto", "tipo": "string", "obrigatorio": false, "operador": "LIKE" }
    ],
    "ordenacao": { "nomeColuna": "nm_produto", "decrescente": false },
    "paginacao": {
      "paramPagina": "page",
      "paramTamanhoPagina": "pageSize",
      "tamanhoPaginaPadrao": 20,
      "tamanhoPaginaMaximo": 100
    }
  }
]
```

**Tipos de command aceitos pelo portal:** `QUERY`, `INSERT`, `UPDATE`, `DELETE`, `PROCEDURE`, `HTTP_GET`, `HTTP_POST`, `HTTP_PUT`, `HTTP_DELETE`

### routes (array de RouteRequest)

```json
[
  {
    "chave": "GET-produtos",
    "caminho": "/api/produtos",
    "metodo": "GET",
    "resposta": { "status": 200 },
    "servico": {
      "threadUnica": false,
      "resultadoUnico": false,
      "passos": [
        {
          "alias": "produtos",
          "abortarSemDados": false,
          "comando": {
            "tipo": "QUERY",
            "database": "dbamv",
            "nome": "produto-listar",
            "retornarResultado": true,
            "parametros": [
              { "nome": "nm_produto", "tipo": "query", "extrair": false }
            ]
          }
        }
      ]
    }
  }
]
```

**Tipos de parâmetro na rota:** `path`, `query`, `body`, `header`

---

## Scripts incluídos

| Versão | Tipo | Domínio |
|---|---|---|
| V01 | databases | Oracle MV (oracle-prod) |
| V02 | commands | Multi Empresas — consulta MV |
| V03 | routes | `GET /mv/api/multiempresas` |

---

## Como executar

### Via Maven (desenvolvimento)

```bash
cd portal-scripts/app
mvn spring-boot:run
```

### Via JAR (produção)

```bash
# Build
mvn clean package -DskipTests

# Execução
java -jar target/portal-scripts-1.0.0.jar
```

---

## Arquivo de histórico

Após a primeira execução, um arquivo `portal_scripts_history.json` é criado no diretório de trabalho:

```json
[
  {
    "version": "V01",
    "filename": "V01__commands_produto.json",
    "type": "COMMAND",
    "executedAt": "2025-05-01T22:00:00",
    "success": true,
    "checksum": "a3f8d2..."
  }
]
```

- Scripts com `success: true` **nunca são reexecutados**.
- Se um script falhar, ele ficará com `success: false` no histórico e **será tentado novamente** na próxima execução.
- Se o conteúdo de um script já executado for alterado (checksum diferente), um **aviso** é emitido no log mas o script **não é reexecutado**.

---

## Adicionar novos scripts

1. Crie o arquivo em `src/main/resources/migrations/` seguindo a convenção de nome.
2. O número de versão deve ser maior que o maior já existente (ex: `V04__commands_xyz.json`).
3. O tipo pode ser `databases`, `commands` ou `routes`.
4. Execute o projeto — apenas os novos scripts serão aplicados.

---

## Estrutura do projeto

```
portal-scripts/
└── app/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/br/tec/suportes/portalscripts/
        │   │   ├── PortalScriptsApplication.java
        │   │   ├── config/
        │   │   │   ├── AppConfig.java
        │   │   │   └── PortalProperties.java
        │   │   ├── domain/
        │   │   │   ├── command/   (CommandRequest, CommandBody, SqlParameter, ...)
        │   │   │   ├── database/  (DatabaseRequest, DatabasePool)
        │   │   │   ├── route/     (RouteRequest, RouteStep, RouteCommand, ...)
        │   │   │   └── migration/ (MigrationScript, MigrationHistory)
        │   │   ├── runner/
        │   │   │   └── MigrationApplicationRunner.java
        │   │   └── service/
        │   │       ├── MigrationRunner.java
        │   │       ├── ScriptLoader.java
        │   │       ├── PortalClient.java
        │   │       └── HistoryService.java
        │   └── resources/
        │       ├── application.properties
        │       ├── logback-spring.xml
        │       └── migrations/
        │           ├── V01__databases_oracle.json
        │           ├── V02__commands_multiempresas.json
        │           └── V03__routes_multiempresas.json
        └── test/
            └── java/br/tec/suportes/portalscripts/
                └── service/
                    └── MigrationRunnerTest.java
```
