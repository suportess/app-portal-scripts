# 03 — Catálogo de Scripts de Migration

Todos os scripts incluídos na versão inicial (`V01`–`V13`), com descrição dos commands/routes e tabelas envolvidas.

---

## V01 — commands_produto

**Tipo:** COMMAND | **Tabela:** `"DBAMV".produto`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `produto-listar` | QUERY | Lista produtos com filtro por `nm_produto`, `sn_consignado`, `sn_lote`; paginação |
| `produto-buscar-por-id` | QUERY | Busca produto pelo `cd_produto` |
| `produto-inserir` | INSERT | Insere novo produto (`nm_produto`, `sn_consignado`, `sn_lote`, `sn_validade`) |
| `produto-atualizar` | UPDATE | Atualiza dados do produto por `cd_produto` |
| `produto-excluir` | DELETE | Remove produto por `cd_produto` |

---

## V02 — commands_estoque

**Tipo:** COMMAND | **Tabelas:** `"DBAMV".estoque`, `"DBAMV".est_pro`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `estoque-listar` | QUERY | Lista estoques com filtro por `tp_estoque`, `nm_estoque`, `cd_empresa` |
| `estoque-buscar-por-id` | QUERY | Busca estoque pelo `cd_estoque` |
| `estoque-saldo-por-produto` | QUERY | Saldo de todos os produtos de um estoque com paginação |
| `estoque-saldo-produto-especifico` | QUERY | Saldo de produto específico em um estoque |

---

## V03 — commands_fornecedor

**Tipo:** COMMAND | **Tabela:** `"DBAMV".fornecedor`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `fornecedor-listar` | QUERY | Lista fornecedores com filtro por `nm_fornecedor`; paginação |
| `fornecedor-buscar-por-id` | QUERY | Busca fornecedor pelo `cd_fornecedor` |
| `fornecedor-inserir` | INSERT | Insere novo fornecedor |
| `fornecedor-atualizar` | UPDATE | Atualiza `nm_fornecedor` por `cd_fornecedor` |
| `fornecedor-excluir` | DELETE | Remove fornecedor por `cd_fornecedor` |

---

## V04 — commands_entrada

**Tipo:** COMMAND | **Tabelas:** `"DBAMV".ent_pro`, `"DBAMV".itent_pro`, `"DBAMV".itlot_ent`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `entrada-listar` | QUERY | Lista entradas com filtros e paginação |
| `entrada-buscar-por-id` | QUERY | Busca entrada pelo `cd_ent_pro` |
| `entrada-inserir` | INSERT | Cria cabeçalho da entrada |
| `entrada-concluir` | UPDATE | Preenche `dt_conclusao` — dispara trigger de saldo |
| `entrada-estornar` | UPDATE | Remove `dt_conclusao` — estorna saldo via trigger |
| `entrada-excluir` | DELETE | Remove entrada sem `dt_conclusao` |
| `entrada-item-listar` | QUERY | Lista itens de uma entrada |
| `entrada-item-inserir` | INSERT | Insere item na entrada |
| `entrada-item-excluir` | DELETE | Remove item da entrada |
| `entrada-lote-listar` | QUERY | Lista lotes de um item de entrada |
| `entrada-lote-inserir` | INSERT | Insere lote para item de entrada |
| `entrada-lote-excluir` | DELETE | Remove lote de entrada |

---

## V05 — commands_movimento

**Tipo:** COMMAND | **Tabelas:** `"DBAMV".mvto_estoque`, `"DBAMV".itmvto_estoque`, procedure `sp_processar_mvto_transferencia`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `movimento-listar` | QUERY | Lista movimentos com filtros e paginação |
| `movimento-buscar-por-id` | QUERY | Busca movimento pelo `cd_mvto_estoque` |
| `movimento-inserir` | INSERT | Cria cabeçalho do movimento |
| `movimento-concluir` | UPDATE | Preenche `dt_conclusao` — confirma movimentação |
| `movimento-excluir` | DELETE | Remove movimento sem `dt_conclusao` |
| `movimento-item-listar` | QUERY | Lista itens de um movimento |
| `movimento-item-inserir` | INSERT | Insere item no movimento |
| `movimento-item-excluir` | DELETE | Remove item do movimento |
| `movimento-processar-transferencia` | PROCEDURE | Chama `sp_processar_mvto_transferencia(p_cd_mvto, p_sinal)` |

---

## V06 — commands_auxiliares

**Tipo:** COMMAND | **Tabelas:** `paciente`, `setor`, `empresa`, `baixa`, `uni_pro`, `lot_pro`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `paciente-listar` | QUERY | Lista pacientes com filtro por nome; paginação |
| `paciente-buscar-por-id` | QUERY | Busca paciente pelo `cd_paciente` |
| `paciente-inserir` | INSERT | Insere novo paciente |
| `setor-listar` | QUERY | Lista setores com filtro por nome |
| `setor-buscar-por-id` | QUERY | Busca setor pelo `cd_setor` |
| `empresa-listar` | QUERY | Lista todas as empresas |
| `empresa-buscar-por-id` | QUERY | Busca empresa pelo `cd_empresa` |
| `baixa-listar` | QUERY | Lista motivos de baixa |
| `uni-pro-listar-por-produto` | QUERY | Lista unidades de medida de um produto |
| `lot-pro-listar` | QUERY | Saldo por lote de um produto em um estoque (somente saldo > 0) |

---

## V07 — commands_cirurgia_devolucao

**Tipo:** COMMAND | **Tabelas:** `cirurgia`, `devolucao`, `itdevolucao`, procedure `sp_processar_devolucao`, function `fn_saldo_produto`

| Chave | Tipo SQL | Descrição |
|---|---|---|
| `cirurgia-listar` | QUERY | Lista cirurgias com filtros e paginação |
| `cirurgia-buscar-por-id` | QUERY | Busca cirurgia pelo `cd_cirurgia` |
| `cirurgia-inserir` | INSERT | Registra nova cirurgia |
| `devolucao-listar` | QUERY | Lista devoluções com filtros e paginação |
| `devolucao-buscar-por-id` | QUERY | Busca devolução pelo `cd_devolucao` |
| `devolucao-inserir` | INSERT | Cria cabeçalho da devolução (apenas consignados) |
| `devolucao-concluir` | UPDATE | Preenche `dt_conclusao` — debita saldo via trigger |
| `devolucao-estornar` | UPDATE | Remove `dt_conclusao` — estorna saldo via trigger |
| `devolucao-item-listar` | QUERY | Lista itens de uma devolução |
| `devolucao-item-inserir` | INSERT | Insere item na devolução |
| `devolucao-processar` | PROCEDURE | Chama `sp_processar_devolucao(p_cd_devolucao, p_sinal)` |
| `saldo-produto-historico` | QUERY | Histórico de saldo via `fn_saldo_produto(estoque, produto, data, lote, validade)` |

---

## V08 — routes_produto

**Tipo:** ROUTE | **Base path:** `/api/produtos`

| Chave | Método | Caminho | Command |
|---|---|---|---|
| `GET-produtos` | GET | `/api/produtos` | `produto-listar` |
| `GET-produto-por-id` | GET | `/api/produtos/{cd_produto}` | `produto-buscar-por-id` |
| `POST-produto` | POST | `/api/produtos` | `produto-inserir` |
| `PUT-produto` | PUT | `/api/produtos/{cd_produto}` | `produto-atualizar` |
| `DELETE-produto` | DELETE | `/api/produtos/{cd_produto}` | `produto-excluir` |

---

## V09 — routes_estoque

**Tipo:** ROUTE | **Base path:** `/api/estoques`

| Chave | Método | Caminho | Command |
|---|---|---|---|
| `GET-estoques` | GET | `/api/estoques` | `estoque-listar` |
| `GET-estoque-por-id` | GET | `/api/estoques/{cd_estoque}` | `estoque-buscar-por-id` |
| `GET-estoque-saldo` | GET | `/api/estoques/{cd_estoque}/saldo` | `estoque-saldo-por-produto` |
| `GET-estoque-saldo-produto` | GET | `/api/estoques/{cd_estoque}/saldo/{cd_produto}` | `estoque-saldo-produto-especifico` |

---

## V10 — routes_entrada

**Tipo:** ROUTE | **Base path:** `/api/entradas`

| Chave | Método | Caminho | Command |
|---|---|---|---|
| `GET-entradas` | GET | `/api/entradas` | `entrada-listar` |
| `GET-entrada-por-id` | GET | `/api/entradas/{cd_ent_pro}` | `entrada-buscar-por-id` |
| `POST-entrada` | POST | `/api/entradas` | `entrada-inserir` |
| `PUT-entrada-concluir` | PUT | `/api/entradas/{cd_ent_pro}/concluir` | `entrada-concluir` |
| `PUT-entrada-estornar` | PUT | `/api/entradas/{cd_ent_pro}/estornar` | `entrada-estornar` |
| `DELETE-entrada` | DELETE | `/api/entradas/{cd_ent_pro}` | `entrada-excluir` |
| `GET-entrada-itens` | GET | `/api/entradas/{cd_ent_pro}/itens` | `entrada-item-listar` |
| `POST-entrada-item` | POST | `/api/entradas/{cd_ent_pro}/itens` | `entrada-item-inserir` |
| `DELETE-entrada-item` | DELETE | `/api/entradas/itens/{cd_itent_pro}` | `entrada-item-excluir` |
| `GET-entrada-item-lotes` | GET | `/api/entradas/itens/{cd_itent_pro}/lotes` | `entrada-lote-listar` |
| `POST-entrada-item-lote` | POST | `/api/entradas/itens/{cd_itent_pro}/lotes` | `entrada-lote-inserir` |
| `DELETE-entrada-lote` | DELETE | `/api/entradas/lotes/{cd_itlot_ent}` | `entrada-lote-excluir` |

---

## V11 — routes_movimento

**Tipo:** ROUTE | **Base path:** `/api/movimentos`

| Chave | Método | Caminho | Command |
|---|---|---|---|
| `GET-movimentos` | GET | `/api/movimentos` | `movimento-listar` |
| `GET-movimento-por-id` | GET | `/api/movimentos/{cd_mvto_estoque}` | `movimento-buscar-por-id` |
| `POST-movimento` | POST | `/api/movimentos` | `movimento-inserir` |
| `PUT-movimento-concluir` | PUT | `/api/movimentos/{cd_mvto_estoque}/concluir` | `movimento-concluir` |
| `DELETE-movimento` | DELETE | `/api/movimentos/{cd_mvto_estoque}` | `movimento-excluir` |
| `GET-movimento-itens` | GET | `/api/movimentos/{cd_mvto_estoque}/itens` | `movimento-item-listar` |
| `POST-movimento-item` | POST | `/api/movimentos/{cd_mvto_estoque}/itens` | `movimento-item-inserir` |
| `DELETE-movimento-item` | DELETE | `/api/movimentos/itens/{cd_itmvto_estoque}` | `movimento-item-excluir` |
| `POST-movimento-transferencia-processar` | POST | `/api/movimentos/{cd_mvto_estoque}/processar-transferencia` | `movimento-processar-transferencia` |

---

## V12 — routes_auxiliares

**Tipo:** ROUTE

| Chave | Método | Caminho | Command |
|---|---|---|---|
| `GET-fornecedores` | GET | `/api/fornecedores` | `fornecedor-listar` |
| `GET-fornecedor-por-id` | GET | `/api/fornecedores/{cd_fornecedor}` | `fornecedor-buscar-por-id` |
| `POST-fornecedor` | POST | `/api/fornecedores` | `fornecedor-inserir` |
| `PUT-fornecedor` | PUT | `/api/fornecedores/{cd_fornecedor}` | `fornecedor-atualizar` |
| `DELETE-fornecedor` | DELETE | `/api/fornecedores/{cd_fornecedor}` | `fornecedor-excluir` |
| `GET-pacientes` | GET | `/api/pacientes` | `paciente-listar` |
| `GET-paciente-por-id` | GET | `/api/pacientes/{cd_paciente}` | `paciente-buscar-por-id` |
| `POST-paciente` | POST | `/api/pacientes` | `paciente-inserir` |
| `GET-setores` | GET | `/api/setores` | `setor-listar` |
| `GET-setor-por-id` | GET | `/api/setores/{cd_setor}` | `setor-buscar-por-id` |
| `GET-empresas` | GET | `/api/empresas` | `empresa-listar` |
| `GET-empresa-por-id` | GET | `/api/empresas/{cd_empresa}` | `empresa-buscar-por-id` |
| `GET-baixas` | GET | `/api/baixas` | `baixa-listar` |
| `GET-uni-pro-por-produto` | GET | `/api/produtos/{cd_produto}/unidades` | `uni-pro-listar-por-produto` |
| `GET-lot-pro` | GET | `/api/estoques/{cd_estoque}/lotes/{cd_produto}` | `lot-pro-listar` |

---

## V13 — routes_cirurgia_devolucao

**Tipo:** ROUTE

| Chave | Método | Caminho | Command |
|---|---|---|---|
| `GET-cirurgias` | GET | `/api/cirurgias` | `cirurgia-listar` |
| `GET-cirurgia-por-id` | GET | `/api/cirurgias/{cd_cirurgia}` | `cirurgia-buscar-por-id` |
| `POST-cirurgia` | POST | `/api/cirurgias` | `cirurgia-inserir` |
| `GET-devolucoes` | GET | `/api/devolucoes` | `devolucao-listar` |
| `GET-devolucao-por-id` | GET | `/api/devolucoes/{cd_devolucao}` | `devolucao-buscar-por-id` |
| `POST-devolucao` | POST | `/api/devolucoes` | `devolucao-inserir` |
| `PUT-devolucao-concluir` | PUT | `/api/devolucoes/{cd_devolucao}/concluir` | `devolucao-concluir` |
| `PUT-devolucao-estornar` | PUT | `/api/devolucoes/{cd_devolucao}/estornar` | `devolucao-estornar` |
| `GET-devolucao-itens` | GET | `/api/devolucoes/{cd_devolucao}/itens` | `devolucao-item-listar` |
| `POST-devolucao-item` | POST | `/api/devolucoes/{cd_devolucao}/itens` | `devolucao-item-inserir` |
| `POST-devolucao-processar` | POST | `/api/devolucoes/{cd_devolucao}/processar` | `devolucao-processar` |
| `GET-saldo-historico` | GET | `/api/estoques/{cd_estoque}/historico/{cd_produto}` | `saldo-produto-historico` |
