# Documentação de Arquitetura – App Gestão de Finanças

## 🐛 Problema Identificado: Dados não vinculados ao usuário

### Data da descoberta
23/09/2026

### Data da correção inicial
23/09/2026

### Data da expansão
26/09/2026

### Status
✅ **RESOLVIDO e EXPANDIDO**

---

## Descrição do problema original

As tabelas `Divida` e `Transacao` do banco Room **não possuíam um campo `usuarioId`** que vinculasse cada registro ao usuário que o cadastrou. Isso significava que:

- Qualquer usuário que fizesse login no app via **todos os dados** cadastrados no banco
- Se duas pessoas diferentes usassem o mesmo celular, cada uma via os dados da outra
- Os dados não eram isolados por conta
- Usuários novos viam dados fictícios (mock) que nunca cadastraram

### Impacto

| Cenário | Comportamento antes | Comportamento depois |
|---------|---------------------|----------------------|
| Tiago cadastra dívidas | Salvas no banco | Salvas e vinculadas ao Tiago |
| Ana faz login no mesmo celular | Vê as dívidas do Tiago | Vê apenas as dívidas dela |
| Tiago faz logout e Ana entra | Ana vê os dados do Tiago | Ana começa do zero |
| Usuário novo instala o app | Vê dívidas fictícias (mock) | Começa com o banco vazio |

### Por que isso aconteceu

Foi uma **decisão de MVP** para simplificar o desenvolvimento inicial. As dívidas foram criadas como entidades globais, sem pensar na segregação por usuário. Isso é comum em protótipos, mas precisava ser corrigido antes de uma versão de produção.

---

## ✅ Solução implementada

### 1. Campo `usuarioId` adicionado nas entidades

```java
@Entity(tableName = "dividas")
public class Divida {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int usuarioId; // ← NOVO CAMPO
    
    // ... outros campos
}
```

O mesmo foi feito nas entidades `Transacao`, `Categoria`, `Cartao` e `ChavePix`.

### 2. DAOs atualizados para filtrar por usuário

```java
@Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId")
List<Divida> listarPorUsuario(int usuarioId);
```

### 3. AppDatabase atualizado para versão 9

Evolução das versões:
- **v1** → App funcional com Room básico
- **v2** → Adicionado `usuarioId` em `Divida` e `Transacao`
- **v3** → Adicionada entidade `Categoria`
- **v4** → Adicionado campo `cor` em `Categoria`
- **v5** → Adicionado campo `valorParcela` em `Divida`
- **v6** → Adicionadas entidades `Cartao` e `ChavePix`
- **v7** → Adicionado campo `tipo` em `Cartao`
- **v8** → Adicionado campo `excluida` em `Divida` (soft delete)
- **v9** → Adicionado campo `grupoId` em `Divida` (parcelamento)

---

## 🐛 Correção do Cálculo de Parcelas (BUG-001)

### Data da correção
27/09/2026

### Descrição

Ao cadastrar uma dívida parcelada (ex: R$ 2.000 em 10x), o card exibia o **valor total** em vez do **valor de cada parcela**.

### Solução
- Adicionado campo `valorParcela` na entidade `Divida`
- `DividaAdapter` passou a exibir o `valorParcela`

---

## 🔔 Sistema de Lembretes de Fatura (Notificações)

### Data de implementação
27/09/2026

### Descrição

Sistema de **notificações locais** que avisa o usuário **3 dias antes** do vencimento de cada dívida, às 9h da manhã.

### Componentes

1. **`NotificationHelper`** — canal + envio de notificações
2. **`LembreteReceiver`** — BroadcastReceiver do alarme
3. **`AlarmeHelper`** — agendamento/cancelamento com `AlarmManager`

### Fluxo

| Ação | Comportamento |
|------|---------------|
| Usuário cadastra dívida | Alarme agendado para 3 dias antes |
| Usuário paga dívida | Alarme cancelado |
| Usuário exclui dívida | Alarme cancelado |
| Usuário edita dívida | Alarme antigo cancelado + novo agendado |

### Limitações

- **Não notifica se o app for forçado a parar**
- ~~Não notifica se o celular for reiniciado~~ ✅ **RESOLVIDO** com o `BootReceiver`

---

## 🆕 BootReceiver (Reagendamento Automático)

### Data de implementação
29/09/2026

### Descrição

Reagenda automaticamente os alarmes após reinicialização do celular ou atualização do app.

### Como funciona

1. Escuta `BOOT_COMPLETED` e `MY_PACKAGE_REPLACED`
2. Verifica se lembretes estão ativos
3. Verifica se há usuário logado
4. Busca dívidas não pagas do usuário
5. Reagenda cada uma

---

## 💳 Integração de Cartões e Chaves Pix

### Data da implementação
28/09/2026

### Descrição

Tela de **Cartões & Chaves Pix** com navegação por abas e CRUD completo.

### Arquitetura

A tela usa **TabLayout + ViewPager2**:

```
┌────────────────────────────────────────┐
│   CartoesActivity (TabLayout + VP2)   │
├────────────────┬───────────────────────┤
│ CartoesFragment│ ChavesPixFragment     │
├────────────────┼───────────────────────┤
│ Cadastro +     │ Cadastro +            │
│ Lista de       │ Lista de              │
│ Cartões        │ Chaves Pix            │
└────────────────┴───────────────────────┘
```

### Fluxo de integração com Dívida

```
CadastroDividaActivity
  ↓ Spinner "Tipo de Dívida"
  ├─ Crédito → CartaoDao (filtro "Crédito")
  ├─ Débito → CartaoDao (filtro "Débito")
  ├─ Pix → ChavePixDao
  └─ Outros → genérico
  ↓ ChipSelecaoAdapter
  ↓ Lista horizontal de chips
```

---

## 🗑️ Soft Delete de Dívidas

### Data de implementação
06/10/2026

### Descrição

Sistema de **soft delete** (exclusão lógica). Em vez de apagar o registro, o app **marca como excluída**. Isso permite que a dívida continue aparecendo no Histórico.

### Como funciona

```
Antes (exclusão física):
┌──────────────────┐
│ Dívida "Almoço"  │ → db.deletar() → 🗑️ Desaparece
└──────────────────┘

Depois (soft delete):
┌──────────────────┐
│ Dívida "Almoço"  │ → d.excluida = true → 💾 Fica no banco
└──────────────────┘                    mas invisível nas telas
                                        principais
```

### Componentes

#### 1. `model/Divida.java`
- Novo campo: `public boolean excluida;`

#### 2. `dao/DividaDao.java`
- Todas as queries existentes filtram `excluida = 0`
- Novas queries:
  - `listarExcluidasPorUsuario(userId)`
  - `listarTodasParaHistorico(userId)`
  - `listarPorCategoria(userId, categoria)`

#### 3. Telas afetadas

| Tela | Comportamento |
|------|---------------|
| **Dívidas** | Ignora excluídas |
| **Início** | Ignora excluídas |
| **Dashboard** | Ignora excluídas |
| **Histórico** | **Mostra** excluídas com ✗ |

#### 4. Exclusão

```java
divida.setExcluida(true);
db.dividaDao().atualizar(divida);
```

---

## 📜 Tela de Histórico

### Data de implementação
06/10/2026

### Descrição

Tela que mostra o **histórico de dívidas mês a mês**, com navegação, filtros e exportação.

### Arquitetura

```
┌──────────────────────────────────────────┐
│  HistoricoActivity                       │
├──────────────────────────────────────────┤
│  1. Navegação de mês (← Setembro 2026 →) │
│  2. Card de resumo (Total/Pago/Falta)    │
│  3. Chips de filtro por categoria        │
│  4. RecyclerView de lançamentos          │
│  5. Botão "Baixar Resumo" (CSV/PDF)      │
└──────────────────────────────────────────┘
```

### 4 Estados de cada Dívida

| Estado | Condição | Cor | Ícone |
|--------|----------|-----|-------|
| ✅ Liquidado | `pago = true` | Verde | ✓ |
| ⏳ Pendente | `valorPago = 0` e `pago = false` | Amarelo | → |
| ✗ Excluída | `excluida = true` | Vermelho | ✗ |

### Integração com a tela Início

O card **"Total de Dívidas Acumuladas"** é clicável e abre o Histórico.

---

## 💳 Parcelamento por Mês (grupoId)

### Data de implementação
07/10/2026

### Descrição

Foi refatorada a lógica de **parcelamento de dívidas**. Antes, uma compra de R$ 3.000 em 4x gerava **um único registro** com `vencimento` fixo. Isso impedia o app de mostrar as parcelas nos meses seguintes (Novembro, Dezembro, Janeiro).

Agora, cada parcela é um **registro independente** com seu próprio vencimento.

### Comparação

**Antes (1 registro):**
```
Videogame R$ 3.000 em 4x → 1 registro com vencimento 15/10/2026
→ Só aparece em Outubro
→ Dashboard, Histórico e Início só veem 1 mês
```

**Depois (4 registros):**
```
Videogame R$ 3.000 em 4x → 4 registros agrupados por grupoId:
  ├─ Parcela 1/4 → venc 15/10/2026 → R$ 750
  ├─ Parcela 2/4 → venc 15/11/2026 → R$ 750
  ├─ Parcela 3/4 → venc 15/12/2026 → R$ 750
  └─ Parcela 4/4 → venc 15/01/2027 → R$ 750
```

### Componentes

#### 1. `model/Divida.java`
- Novo campo: `public int grupoId;`

#### 2. `dao/DividaDao.java`
- `maxGrupoId()` — retorna o maior `grupoId`
- `listarPorGrupo(userId, grupoId)` — lista parcelas de um grupo

#### 3. `CadastroDividaActivity.salvarDivida()`
Ao cadastrar dívida parcelada:
1. Calcula `valorParcela = valorTotal / numParcelas`
2. Obtém `grupoId = maxGrupoId() + 1`
3. Loop de `0` a `numParcelas - 1`:
   - Cria registro com `parcela = "X/Y"`
   - `valorTotal = valorParcela`
   - `vencimento = vencimentoBase + i meses`
   - Agenda alarme individual

#### 4. Adapters
- `DividaAdapter`, `VencimentoAdapter`, `HistoricoAdapter`
- Todos usam `extrairTotalParcelas()` que entende "X/Y"
- **Não dividem mais** `valorTotal` por número de parcelas

### Impacto nas telas

| Tela | Comportamento |
|------|---------------|
| **Cadastro** | Gera N registros para crédito parcelado |
| **Início** | Mostra próximos vencimentos (parcela a parcela) |
| **Dívidas** | Lista todas as parcelas do usuário |
| **Dashboard** | Filtra por mês — cada parcela aparece no mês certo |
| **Histórico** | Mostra a parcela no mês correspondente |

---

## 📊 Dashboard Somente Leitura

### Data de implementação
07/10/2026

### Descrição

O Dashboard foi refatorado para ser uma tela **puramente informativa**. Antes, ele tinha botões de **Excluir**, **Editar** e **Pagar**, o que causava confusão com a tela de Dívidas.

### Mudança de comportamento

**Antes:**
- Cards com 3 botões (Excluir, Editar, Pagar)
- Usuário podia gerenciar dívidas no Dashboard
- Confuso: dois lugares para fazer a mesma coisa

**Depois:**
- Cards **sem botões** — apenas exibem dados
- Ações ficam exclusivamente na tela **Dívidas**
- Dashboard foca em **análise/relatórios**

### Componentes criados

- `layout/item_divida_dashboard.xml` — card sem botões
- `adapter/DividaDashboardAdapter.java` — adapter somente leitura
- `DashboardActivity` não implementa mais `OnDividaActionListener`

### Navegação por mês

Foi adicionada uma barra de navegação (← Mês →) no Dashboard:
- Botões ← e → para mudar o mês visualizado
- Badge "Atual" quando o mês selecionado é o corrente
- Título da lista reflete o mês visualizado

### Diferença: Dashboard vs Dívidas

| Aspecto | Dashboard | Dívidas |
|---------|-----------|---------|
| Objetivo | Análise/relatórios | Gerenciamento |
| Filtro | Por mês (navegável) | Todas (sem filtro) |
| Botões | ❌ Nenhum | ✅ Excluir, Editar, Pagar |
| Gráfico | ✅ Rosca por categoria | ❌ Não tem |

---

## 📤 Exportação de Dados

### Data de implementação
06/10/2026

### Descrição

Exportação do extrato mensal em **CSV** e **PDF**.

### CSV (`CsvExportHelper.java`)
- Arquivo `.csv` com separador `;`
- BOM UTF-8 para Excel reconhecer acentos
- Colunas: Título, Categoria, Banco, Parcela, Vencimento, Valor Total, Valor Pago, Falta, Status

### PDF (`PdfExportHelper.java`)
- Biblioteca **iTextG 5.5.10**
- Layout escuro fiel ao Figma
- Cabeçalho "ORG" + Gestão Financeira
- Card de resumo + lista de lançamentos

### FileProvider

Configuração para compartilhar arquivos via Intent.

---

## 🧪 Como testar o Parcelamento

1. Cadastre uma dívida: **Videogame R$ 3.000 em 4x**, 1º vencimento **em Novembro**
2. ✅ Deve criar **4 registros** no banco (1/4, 2/4, 3/4, 4/4)
3. Abra a tela **Dívidas** — deve mostrar as 4 parcelas
4. Abra o **Dashboard** e navegue até **Novembro** — Videogame 1/4
5. Pague a parcela 1/4 em **Dívidas** — botão vira "Pago"

---

## 🚧 Próximos passos

- [ ] Testes da Sprint 6 pelo Israel
- [ ] Aplicar Migration real (não destrutiva)
- [ ] Autenticação em nuvem (Firebase) — opcional
- [ ] Biometria real

---

## 📚 Referências

- Autor das Sprints 5 e 6: Gustavo Piteira
- Soft Delete: 06/10/2026
- Tela de Histórico: 06/10/2026
- Exportação CSV/PDF: 06/10/2026
- Parcelamento por Mês: 07/10/2026
- Dashboard Somente Leitura: 07/10/2026
