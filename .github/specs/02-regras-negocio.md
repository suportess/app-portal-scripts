# 02 — Regras de Negócio

## 1. Idempotência e histórico

### 1.1 Arquivo de histórico

O runner persiste um arquivo JSON (`portal_scripts_history.json`) com o registro de cada script executado. Estrutura de cada entrada:

```json
{
  "version": "V01",
  "filename": "V01__commands_produto.json",
  "type": "COMMAND",
  "executedAt": "2025-05-01T22:00:00",
  "success": true,
  "checksum": "a3f8d2c1..."
}
```

### 1.2 Regras de execução

| Situação | Comportamento |
|---|---|
| Versão **não consta** no histórico | Script é executado normalmente |
| Versão consta com `success: true` | Script é **ignorado** (pulado) |
| Versão consta com `success: false` | Script é **tentado novamente** |
| Checksum diverge em versão já executada com sucesso | **Alerta no log**, script **não é reexecutado** |

### 1.3 Checksum

O checksum SHA-256 do conteúdo JSON é calculado no momento da execução e armazenado no histórico. Na próxima execução, se o checksum do arquivo atual for diferente do registrado, o runner emite um aviso — mas **não reexecuta** o script (proteção contra modificação acidental de scripts já aplicados).

---

## 2. Ordenação e atomicidade

- Scripts são executados em **ordem crescente de versão** (`V01` → `V02` → … → `Vnn`).
- A execução é **sequencial** — sem paralelismo entre scripts.
- Se um script falhar, o pipeline é **interrompido imediatamente**: scripts posteriores **não são executados**.
- O script com falha é registrado com `success: false` e **será tentado novamente** na próxima execução.

---

## 3. Comunicação com o Portal

### 3.1 Autenticação

Todas as requisições ao Portal incluem o cabeçalho:

```
Authorization: Bearer {portal.api-key}
```

A chave deve corresponder à variável `GATEWAY_API_KEY` configurada no Portal.

### 3.2 Endpoints utilizados

| Operação | Método | Endpoint |
|---|---|---|
| Criar command | `POST` | `{portal.url}/commands` |
| Criar route | `POST` | `{portal.url}/routes` |
| Verificar command existente | `GET` | `{portal.url}/commands?key={chave}` |
| Verificar route existente | `GET` | `{portal.url}/routes?key={chave}` |

### 3.3 Tratamento de conflito (HTTP 409)

Se o Portal retornar `409 Conflict` ao criar um command ou route (recurso já existe), o runner emite um **aviso no log** e **continua** sem falhar. Isso torna cada item individual idempotente: o script inteiro pode ser reexecutado após correção parcial sem duplicar recursos.

### 3.4 Tratamento de outros erros HTTP

Qualquer status `4xx` (exceto `409`) ou `5xx` é tratado como **falha**:
- A exceção `PortalClientException` é lançada com a mensagem de erro e o body da resposta.
- O `MigrationRunner` captura a exceção, registra a falha no histórico e interrompe o pipeline.

---

## 4. Descoberta de scripts (ScriptLoader)

- O `ScriptLoader` usa `PathMatchingResourcePatternResolver` do Spring para descobrir todos os arquivos `*.json` em `classpath:migrations/`.
- Arquivos que **não seguem** o padrão `V{nn}__{tipo}_{descricao}.json` são **ignorados com aviso** no log.
- Tipo inválido (não é `commands` nem `routes`) também resulta em ignorar o arquivo.
- A lista retornada já vem **ordenada por versão**.

---

## 5. Fluxo completo de execução

```
MigrationApplicationRunner.run()
  └─ MigrationRunner.run()
       ├─ ScriptLoader.loadAll()           → lista ordenada de scripts
       ├─ HistoryService.loadExecutedVersions() → set de versões já executadas
       └─ para cada script:
            ├─ versão já executada? → pular
            ├─ checksum mudou?      → avisar no log
            ├─ applyScript()
            │    ├─ COMMAND → PortalClient.createCommand() para cada item
            │    └─ ROUTE   → PortalClient.createRoute()   para cada item
            ├─ sucesso → HistoryService.recordSuccess()
            └─ falha   → HistoryService.recordFailure() + break
```

---

## 6. Saída da aplicação

A aplicação encerra com:

| Código | Situação |
|---|---|
| `0` | Todos os scripts executados com sucesso (ou pulados) |
| `1` | Ao menos um script falhou |

Isso permite integração direta com pipelines CI/CD (GitHub Actions, Jenkins, etc.).

---

## 7. Logs

| Nível | Situação |
|---|---|
| `INFO` | Script aplicado com sucesso, command/route criado, resumo final |
| `WARN` | Script pulado (409 no portal), checksum alterado |
| `ERROR` | Falha ao executar script, erro de comunicação com o Portal |
| `DEBUG` | Detalhes de requisições HTTP (ativado com `logging.level.org.springframework.web.client=DEBUG`) |

Logs são gravados no console e no arquivo `portal-scripts.log`, com rotação diária (7 dias de retenção).
