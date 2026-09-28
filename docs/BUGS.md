# Registro de Bugs – App Gestão de Finanças

Documento para registrar os bugs encontrados durante os testes do app, com descrição, prioridade, status e solução aplicada.

## Status dos Bugs

| Status | Significado |
|--------|-------------|
| 🔴 **Aberto** | Bug reportado, ainda não corrigido |
| 🟡 **Em andamento** | Bug em processo de correção |
| 🟢 **Corrigido** | Bug resolvido e testado |
| ⚫ **Fechado** | Bug validado como resolvido pelo time |

---

## 🐛 BUG-001: Valor da parcela não é dividido em dívidas parceladas

| Campo | Informação |
|-------|------------|
| **Data** | 27/09/2026 |
| **Reportado por** | Israel Malheiros |
| **Tela** | Tela de Dívidas |
| **Prioridade** | 🔴 Alta |
| **Status** | 🟢 Corrigido |

### Descrição
Ao cadastrar uma dívida parcelada (ex: R$ 2.000 em 10x), o app exibia o **valor total** no card em vez do **valor de cada parcela**.

### Como reproduzir
1. Abrir o app e fazer login
2. Cadastrar uma dívida de R$ 2.000 com 10 parcelas
3. Ir para a tela de Dívidas

### Comportamento esperado
O card deveria mostrar **"Falta: R$ 200,00"** (valor de cada parcela).

### Comportamento atual (antes da correção)
O card mostrava **"Falta: R$ 2.000,00"** (valor total), induzindo o usuário ao erro.

### Solução aplicada
- Adicionado campo `valorParcela` na entidade `Divida`
- `CadastroDividaActivity` agora calcula `valorParcela = valorTotal / numParcelas` ao salvar
- `DividaAdapter` passou a exibir o `valorParcela` em vez do `valorTotal`
- `AppDatabase` atualizado para versão 5

### Arquivos modificados
- `model/Divida.java`
- `view/CadastroDividaActivity.java`
- `adapter/DividaAdapter.java`
- `database/AppDatabase.java`

---

## 📊 Resumo Geral

| Total | 🔴 Abertos | 🟡 Em andamento | 🟢 Corrigidos | ⚫ Fechados |
|-------|------------|-----------------|---------------|-------------|
| 1 | 0 | 0 | 1 | 0 |

---

## 🚧 Próximos Bugs a Investigar

*(Este espaço será preenchido conforme novos bugs forem encontrados durante os testes)*

---

> **Observação:** Todo bug corrigido deve ser testado novamente e marcado como "Fechado" antes do final da sprint.
