# Padrão de Commits e Fluxo de Desenvolvimento

Este documento define o padrão de commits e o fluxo de trabalho do projeto.
O objetivo é manter o código organizado, padronizado e fácil de entender por qualquer pessoa do time.

---

# 🧱 Padrão de Commits (Conventional Commits)

Os commits seguem um padrão semântico, ou seja, cada mensagem indica claramente o que foi feito.

## Tipos de commit

* `feat` → Adiciona uma nova funcionalidade
* `fix` → Corrige um bug
* `docs` → Alterações na documentação
* `test` → Alterações/criação de testes
* `build` → Mudanças em build ou dependências
* `perf` → Melhorias de performance
* `style` → Formatação de código (lint, espaçamento, etc)
* `refactor` → Refatoração sem alterar comportamento
* `chore` → Configurações e tarefas gerais
* `ci` → Integração contínua
* `raw` → Arquivos de configuração/dados
* `cleanup` → Limpeza de código desnecessário
* `remove` → Remoção de código ou arquivos

## Exemplos

```bash
feat: adiciona logo na tela inicial
fix: corrige erro ao salvar usuário
docs: atualiza README
```

---

# 🌱 Fluxo de Desenvolvimento com Branches

## Conceito

Neste projeto, nunca trabalhamos diretamente na branch `development`.
Toda alteração deve ser feita em uma branch separada.

Isso permite:

* Trabalhar em paralelo com outras pessoas
* Evitar conflitos
* Garantir revisão de código antes de integrar

---

## 🪜 Passo a passo

### 1. Criar uma nova branch

Sempre comece garantindo que sua `development` está atualizada:

```bash
git checkout development
git pull origin development
git checkout -b feat/nome-da-feature
```

Exemplos de nome:

```bash
feat/adiciona-logo
fix/corrige-login
```

---

### 2. Desenvolver

Faça suas alterações normalmente e commit seguindo o padrão:

```bash
git add .
git commit -m "feat: adiciona logo na tela inicial"
```

---

### 3. Atualizar sua branch

Antes de subir sua branch, sincronize com a `development`:

```bash
git add .
git stash
git pull origin development
git stash apply
```

---

### 4. Subir a branch

```bash
git add .
git commit -m "feat: ajustes finais"
git push origin feat/nome-da-feature
```

---

### 5. Abrir Pull Request (PR)

No GitHub:

* Acesse o repositório
* Clique em **"Compare & pull request"**
* Configure:

  * Base: `development`
  * Compare: sua branch
* Crie o PR

---

### 6. Code Review

* Adicione 2 revisores
* Aguarde aprovação
* Faça ajustes se necessário

---

### 7. Merge

Após aprovação:

* O PR será mergeado na `development`
* Sua branch pode ser removida

---

## ⚠️ Boas práticas

* Nunca commitar direto na `development`
* Usar nomes claros nas branches
* Fazer commits pequenos e objetivos
* Sempre seguir o padrão de commit
* Atualizar sua branch antes de abrir PR

---

## 💡 Resumo

* Cada tarefa → uma branch
* Desenvolve → commit → push
* Abre PR → revisão → merge
