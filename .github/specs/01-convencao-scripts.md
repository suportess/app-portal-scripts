# 01 — Convenção de Scripts

## Nomenclatura dos arquivos

```
V{nn}__{tipo}_{descricao}.json
```

| Parte | Obrigatório | Regra | Exemplo |
|---|---|---|---|
| `V{nn}` | Sim | Número inteiro com zero-fill, define a ordem | `V01`, `V09`, `V13` |
| `__` | Sim | Dois underscores separando versão do resto | — |
| `{tipo}` | Sim | `commands` ou `routes` (case-insensitive) | `commands` |
| `_` | Sim | Um underscore separando tipo da descrição | — |
| `{descricao}` | Sim | Snake_case livre, sem espaços | `produto`, `entrada_itens` |
| `.json` | Sim | Extensão obrigatória | — |

**Exemplos válidos:**
```
V01__commands_produto.json
V08__routes_produto.json
V10__routes_entrada.json
V13__routes_cirurgia_devolucao.json
```

**Exemplos inválidos** (serão ignorados com aviso no log):
```
commands_produto.json          ← sem versão
V1_commands_produto.json       ← underscore simples
V01__produto.json              ← sem tipo
V01__outros_produto.json       ← tipo inválido (não é commands/routes)
```

---

## Estrutura JSON — Commands

O arquivo deve conter um **array** de objetos `CommandRequest`. Cada objeto representa um command a ser criado via `POST /commands` no Portal.

### Campos disponíveis

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `chave` | string | Sim | Identificador único do command |
| `descricao` | string | Não | Descrição legível |
| `tipo` | string | Sim | Ver tabela de tipos abaixo |
| `tipoBanco` | string | Cond. | Obrigatório para tipos SQL |
| `sql` | string | Cond. | SQL completo (mutuamente exclusivo com `tabela`) |
| `tabela` | string | Cond. | Nome da tabela para INSERT/UPDATE/DELETE simples |
| `rota` | string | Cond. | Obrigatório para tipos HTTP |
| `tipoConteudo` | string | Não | Content-Type para HTTP |
| `nomeCertificado` | string | Não | Certificado mTLS a usar |
| `corpo` | CommandBody | Não | Campos do body para INSERT/UPDATE/HTTP |
| `consulta` | CommandBody | Não | Campos de query para HTTP |
| `parametros` | SqlParameter[] | Não | Parâmetros de filtro SQL (WHERE dinâmico) |
| `ordenacao` | SqlOrder | Não | `nomeColuna` + `decrescente` |
| `paginacao` | Pagination | Não | Configuração de LIMIT/OFFSET automático |

### Tipos de command

| Valor | Descrição |
|---|---|
| `QUERY` | SELECT com filtros dinâmicos |
| `INSERT` | INSERT baseado em tabela ou SQL |
| `UPDATE` | UPDATE com WHERE por parâmetro |
| `DELETE` | DELETE com WHERE por parâmetro |
| `PROCEDURE` | Chamada de stored procedure/function |
| `HTTP_GET` | Chamada HTTP GET para rota externa |
| `HTTP_POST` | Chamada HTTP POST para rota externa |
| `HTTP_PUT` | Chamada HTTP PUT para rota externa |
| `HTTP_DELETE` | Chamada HTTP DELETE para rota externa |

### Tipos de banco (`tipoBanco`)

| Valor | Banco |
|---|---|
| `postgres` | PostgreSQL |
| `mysql` | MySQL / MariaDB |
| `sqlserver` | SQL Server |
| `oracle` | Oracle |

### Exemplo completo — QUERY com paginação

```json
[
  {
    "chave": "produto-listar",
    "descricao": "Lista produtos com filtros opcionais",
    "tipo": "QUERY",
    "tipoBanco": "postgres",
    "sql": "SELECT cd_produto, nm_produto FROM \"DBAMV\".produto",
    "parametros": [
      { "nome": "nm_produto",    "tipo": "string", "obrigatorio": false, "operador": "LIKE" },
      { "nome": "sn_consignado", "tipo": "string", "obrigatorio": false, "operador": "="    }
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

### Exemplo completo — INSERT com corpo

```json
[
  {
    "chave": "produto-inserir",
    "tipo": "INSERT",
    "tipoBanco": "postgres",
    "tabela": "\"DBAMV\".produto",
    "corpo": {
      "campos": [
        { "nome": "nm_produto",    "tipo": "string", "obrigatorio": true,  "maximo": 100 },
        { "nome": "sn_consignado", "tipo": "string", "obrigatorio": true,  "maximo": 1   }
      ]
    }
  }
]
```

### Exemplo completo — PROCEDURE

```json
[
  {
    "chave": "movimento-processar-transferencia",
    "tipo": "PROCEDURE",
    "tipoBanco": "postgres",
    "sql": "CALL \"DBAMV\".sp_processar_mvto_transferencia(:p_cd_mvto, :p_sinal)",
    "parametros": [
      { "nome": "p_cd_mvto", "tipo": "number", "obrigatorio": true },
      { "nome": "p_sinal",   "tipo": "number", "obrigatorio": true }
    ]
  }
]
```

---

## Estrutura JSON — Routes

O arquivo deve conter um **array** de objetos `RouteRequest`. Cada objeto representa uma rota dinâmica a ser registrada via `POST /routes` no Portal.

### Campos disponíveis

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `chave` | string | Sim | Identificador único da rota |
| `caminho` | string | Sim | Path HTTP, deve começar com `/` |
| `metodo` | string | Sim | `GET`, `POST`, `PUT`, `DELETE`, `PATCH` |
| `resposta.status` | int | Sim | HTTP status padrão de resposta |
| `resposta.tipoConteudo` | string | Não | Content-Type da resposta |
| `servico.threadUnica` | bool | Sim | Execução em thread única (mutex) |
| `servico.resultadoUnico` | bool | Sim | Retorna objeto em vez de array |
| `servico.passos` | RouteStep[] | Sim | Mínimo 1 passo |

### Campos de RouteStep

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `alias` | string | Não | Nome do resultado no response |
| `abortarSemDados` | bool | Sim | Se true, retorna 404 quando o command não retorna dados |
| `comando.tipo` | string | Sim | Tipo do command |
| `comando.database` | string | Cond. | Chave do database no Portal (obrigatório para SQL) |
| `comando.nome` | string | Sim | `chave` do command cadastrado no Portal |
| `comando.retornarResultado` | bool | Sim | Inclui resultado no response |
| `comando.parametros` | RouteParameter[] | Não | Mapeamento de parâmetros |

### Campos de RouteParameter

| Campo | Tipo | Descrição |
|---|---|---|
| `nome` | string | Nome do parâmetro no command |
| `tipo` | string | Origem: `path`, `query`, `body`, `header` |
| `extrair` | bool | Se true, extrai valor de resultado de step anterior |
| `valor` | any | Valor fixo (quando não vem da requisição) |
| `campo` | string | Nome alternativo do campo na origem |

### Exemplo completo — rota GET com path param

```json
[
  {
    "chave": "GET-produto-por-id",
    "caminho": "/api/produtos/{cd_produto}",
    "metodo": "GET",
    "resposta": { "status": 200 },
    "servico": {
      "threadUnica": false,
      "resultadoUnico": true,
      "passos": [
        {
          "alias": "produto",
          "abortarSemDados": true,
          "comando": {
            "tipo": "QUERY",
            "database": "dbamv",
            "nome": "produto-buscar-por-id",
            "retornarResultado": true,
            "parametros": [
              { "nome": "cd_produto", "tipo": "path", "extrair": false }
            ]
          }
        }
      ]
    }
  }
]
```

### Exemplo completo — rota POST multi-step

```json
[
  {
    "chave": "POST-entrada-com-conclusao",
    "caminho": "/api/entradas/completa",
    "metodo": "POST",
    "resposta": { "status": 201 },
    "servico": {
      "threadUnica": true,
      "resultadoUnico": false,
      "passos": [
        {
          "alias": "entrada",
          "abortarSemDados": false,
          "comando": {
            "tipo": "INSERT",
            "database": "dbamv",
            "nome": "entrada-inserir",
            "retornarResultado": true,
            "parametros": []
          }
        },
        {
          "alias": "conclusao",
          "abortarSemDados": false,
          "comando": {
            "tipo": "UPDATE",
            "database": "dbamv",
            "nome": "entrada-concluir",
            "retornarResultado": true,
            "parametros": [
              { "nome": "cd_ent_pro", "tipo": "body", "extrair": true, "campo": "cd_ent_pro" }
            ]
          }
        }
      ]
    }
  }
]
```
