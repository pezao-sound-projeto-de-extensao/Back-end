# StockFlow API — Collection Bruno

Collection nativa do [Bruno](https://www.usebruno.com/) (arquivos `.bru` em texto puro, versionáveis
no git) cobrindo todos os endpoints do backend, com asserções de status e das regras de negócio.

```
bruno/StockFlow-API/
├── bruno.json                      # manifesto da collection
├── collection.bru                  # auth Bearer {{accessToken}} + script global (runId, hoje)
├── environments/StockFlow-Local.bru
├── fixtures/                       # imagem.png e nota.pdf usados nos uploads
└── 00 Health … 99 Encerramento/    # uma pasta por recurso, na ordem de execução
```

## Pré-requisitos

Subir o stack completo (API + MySQL + S3 local + front + nginx):

```bash
cd ../DevOps-AWS/dev
docker compose up -d --build
```

A API fica em `http://localhost:8080/api` (via nginx). O prefixo `/api` é obrigatório: o `WebConfig`
o adiciona a todo `@RestController`.

Usuário administrador do seed: `adm@email.com` / `senha` (todas as 10 permissões).

## Como usar

**Bruno (app):** *Open Collection* → selecione `bruno/StockFlow-API` → escolha o environment
**StockFlow-Local** → *Run Collection* (ou execute as pastas na ordem numérica).

**CLI:**

```bash
npm i -g @usebruno/cli
cd bruno/StockFlow-API
bru run --env StockFlow-Local
```

Não é preciso importar nada: a pasta **é** a collection.

## Environment `StockFlow-Local`

| Variável | Valor | Uso |
|---|---|---|
| `baseUrl` | `http://localhost:8080/api` | URL base, com `/api` |
| `adminEmail` / `adminSenha` | `adm@email.com` / `senha` | login do admin |
| `usuarioSenhaNova` | `NovaSenha123` | senha usada no teste de troca de senha |

O environment só tem configuração fixa. Tokens e ids capturados (`accessToken`, `refreshToken`,
`cargoId`, `itemId`, `orcamentoId`, `encomendaId`, …) são **variáveis de runtime** (`bru.setVar`),
visíveis no painel *Runtime Variables* do app. Assim o arquivo `.bru` do environment nunca é
regravado com JWTs e ids de uma execução antiga. Se você gravasse números com `bru.setEnvVar`, o app
passaria a salvar anotações `@number` que o `bru` CLI 3.0.x não consegue ler. Como as variáveis de
runtime somem quando o app é fechado, ao reabrir rode **01 Auth / Login admin** antes de executar
requests avulsas.

Variáveis de runtime definidas no `collection.bru`:

- `runId` — sufixo único por execução, usado em nomes e e-mails. Evita 409 em campos únicos
  (cargo, categoria, unidade, item, e-mail), então dá para rodar a collection várias vezes no mesmo banco;
- `hoje` e `inicioAno` — datas `aaaa-MM-dd` para os filtros de movimentações e relatórios.

## Fluxo

| Pasta | O que cobre |
|---|---|
| 00 Health | smoke test público |
| 01 Auth | login (200/401/400), `/auth/me`, refresh com rotação, reúso de refresh token (400) |
| 02 Permissoes | as 10 permissões do sistema |
| 03 Cargos | CRUD, nome duplicado (409), permissão inexistente (400), exclusão |
| 04 Usuarios | cadastro com senha padrão `Pezao_<id>`, e-mail duplicado (409), filtros, toggle de ativo, login de usuário inativo (401), exclusão de cargo em uso (409) |
| 05 Auth - usuario de teste | troca de senha, login com o usuário de teste, **403** para quem não tem permissão, reset de senha |
| 06 Categorias / 07 Unidades | CRUD, duplicados (409), validações (400), exclusão |
| 08 Itens | movimentação automática do estoque inicial, `quantidadeAtual` ignorada na edição, filtros, upload/download/remoção de imagem, inativar/reativar, edição de item inativo (400) |
| 09 Alertas | lista e filtro por `zerado` / `estoque_baixo` |
| 10 Movimentacoes | entrada/saída com saldo antes/depois, saída acima do estoque (400), validações, filtros, nota fiscal, exclusão com reversão do estoque, reversão bloqueada de entrada já consumida (400) |
| 11 Dashboard e Relatorios | KPIs, itens de atenção, relatório por período (datas obrigatórias) |
| 12 Clientes | CRUD e busca |
| 13 Orcamentos | cliente existente ou novo (XOR), produto de catálogo ou novo (XOR), edição/aceite/rejeição só para PENDENTE |
| 14 Encomendas | PENDENTE → RECEBIDA (entrada) → CONCLUIDA (saída), orçamento vira CONCLUIDO quando todas terminam, recebimento de produto novo exige `itemId` |
| 99 Encerramento | logout e refresh após logout (400) |

A senha do `adm@email.com` nunca é alterada: a troca e o reset de senha usam o usuário criado em
**04 Usuarios**.

## Pontos de atenção

- **Cookie tem prioridade sobre o header.** O login grava o JWT no cookie `access_token`, e o
  `JwtAuthenticationFilter` lê o cookie antes do `Authorization`. Como o Bruno guarda cookies, depois
  de logar com outro usuário as requests seguintes seguem autenticadas como ele. Por isso a pasta 05
  termina com *Login admin de volta*. Se algo der 403 inesperado, rode **01 Auth / Login admin**.
- **Uploads** usam os arquivos de `fixtures/` e precisam do S3 local (floci) do stack `dev`.
- **`PATCH /encomendas/{id}/receber`** precisa de `Content-Type: application/json`, mesmo sem
  `itemId`. As requests mandam `{}`; sem corpo a API responde 415.
- **`/relatorios`** devolve o `tipo` do histórico em MAIÚSCULO (`ENTRADA`), diferente de
  `/movimentacoes` (`entrada`).
- Access token dura 15 min. Se expirar no meio do uso manual, rode **Login admin** ou **Refresh**.
