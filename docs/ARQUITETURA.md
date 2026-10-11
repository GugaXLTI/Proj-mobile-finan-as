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

### 2. AppDatabase atualizado para versão 9

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

### Componentes
1. **`NotificationHelper`** — canal + envio de notificações
2. **`LembreteReceiver`** — BroadcastReceiver do alarme
3. **`AlarmeHelper`** — agendamento/cancelamento com `AlarmManager`

### Limitações
- **Não notifica se o app for forçado a parar**
- ~~Não notifica se o celular for reiniciado~~ ✅ **RESOLVIDO** com o `BootReceiver`

---

## 🆕 BootReceiver (Reagendamento Automático)

### Data de implementação
29/09/2026

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

### Arquitetura da tela

```
┌────────────────────────────────────────┐
│   CartoesActivity (TabLayout + VP2)   │
├────────────────┬───────────────────────┤
│ CartoesFragment│ ChavesPixFragment     │
└────────────────┴───────────────────────┘
```

---

## 🗑️ Soft Delete de Dívidas

### Data de implementação
06/10/2026

### Descrição
Sistema de **soft delete** (exclusão lógica). Em vez de apagar o registro, o app **marca como excluída**.

### Telas afetadas

| Tela | Comportamento |
|------|---------------|
| **Dívidas** | Ignora excluídas |
| **Início** | Ignora excluídas |
| **Dashboard** | Ignora excluídas |
| **Histórico** | **Mostra** excluídas com ✗ |

---

## 📜 Tela de Histórico

### Data de implementação
06/10/2026

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

---

## 💳 Parcelamento por Mês (grupoId)

### Data de implementação
07/10/2026

### Descrição
Cada parcela é um **registro independente** com seu próprio vencimento.

**Antes (1 registro):**
```
Videogame R$ 3.000 em 4x → 1 registro com vencimento 15/10/2026
```

**Depois (4 registros):**
```
Videogame R$ 3.000 em 4x → 4 registros agrupados por grupoId:
  ├─ Parcela 1/4 → venc 15/10/2026 → R$ 750
  ├─ Parcela 2/4 → venc 15/11/2026 → R$ 750
  ├─ Parcela 3/4 → venc 15/12/2026 → R$ 750
  └─ Parcela 4/4 → venc 15/01/2027 → R$ 750
```

---

## 📊 Dashboard Somente Leitura

### Data de implementação
07/10/2026

### Descrição
Dashboard focado em **análise/relatórios** (sem botões de ação).

| Aspecto | Dashboard | Dívidas |
|---------|-----------|---------|
| Objetivo | Análise/relatórios | Gerenciamento |
| Filtro | Por mês (navegável) | Todas (sem filtro) |
| Botões | ❌ Nenhum | ✅ Excluir, Editar, Pagar |

---

## 📤 Exportação de Dados (CSV e PDF)

### Data de implementação
06/10/2026

### Descrição
- **CSV** — abre no Excel/Sheets com BOM UTF-8
- **PDF** — layout escuro fiel ao Figma com iTextG

---

## 🔐 Autenticação Biométrica

### Data de implementação
08/10/2026

### Descrição
Autenticação via **BiometricPrompt** (Android 9+).

### Arquitetura

```
Configurações → Switch Biometria
    ↓ (ativa)
SessionManager.salvarUsuarioBiometrico(userId, nome, email)
    ↓
Login → Botão "Usar biometria"
    ↓
BiometricHelper.autenticar()
    ↓
Se sucesso → SessionManager.salvarSessao() → Início
```

### Componentes

#### 1. `utils/BiometricHelper.java`
- `podeUsarBiometria(context)` — verifica hardware + digital cadastrada
- `autenticar(activity, titulo, subtitulo, callback)` — dispara o prompt

#### 2. `utils/SessionManager.java`
- Chaves de biometria **sobrevivem ao logout**

#### 3. `view/LoginActivity.java`
- Botão `btnBiometria` (só aparece se ativo + disponível)

#### 4. `view/ConfiguracoesActivity.java`
- Switch de biometria funcional

---

## 📈 Gráfico de Linha — Evolução Mensal

### Data de implementação
10/10/2026

### Descrição
Gráfico de linha no Dashboard mostrando a **evolução dos gastos** em N meses (3M, 6M ou 12M).

### Localização no Dashboard
O gráfico aparece **abaixo do gráfico de rosca**, dentro de um CardView com:
- Cabeçalho "📈 EVOLUÇÃO MENSAL"
- Texto "Total: R$ X,XX em N meses • Média: R$ Y,YY"
- Chips de período (3M / 6M / 12M)
- LineChart com ~200dp de altura

### Comportamento do Cálculo

O gráfico é **centrado no mês selecionado**:
- **3M** → 1 mês atrás + mês atual + 1 mês à frente
- **6M** → 3 meses atrás + mês atual + 2 meses à frente
- **12M** → 6 meses atrás + mês atual + 5 meses à frente

**Exemplo (3M em Outubro):** Set, Out, Nov
**Exemplo (3M em Novembro):** Out, Nov, Dez

### Estilo Visual
- Linha verde (`#10B981`) com curvas bezier
- Valores em cima de cada ponto (R$ 750, R$ 500, etc)
- Eixo Y com labels formatados (R$ 0, R$ 500, R$ 1k)
- **Ponto do maior gasto destacado em roxo** (`#A855F7`)
- **Linha de média mensal tracejada em amarelo** (`#F59E0B`)
- Preenchimento com gradiente verde (`bg_line_chart_gradient.xml`)
- Animação suave ao carregar (`animateX(700)`)

### Lógica de Cálculo

Para cada mês dos N selecionados:
1. Percorre todas as dívidas do usuário (não excluídas)
2. Filtra apenas as do mês (pelo `vencimento`)
3. Soma `valorTotal`
4. Adiciona ponto no gráfico

### Configuração

```java
private static final int[] PERIODOS_DISPONIVEIS = {3, 6, 12};
private static final int PERIODO_PADRAO = 6;
```

---

## 📅 Filtro de Período Customizado no Histórico

### Data de implementação
10/10/2026

### Descrição
Navegação por mês **continua funcionando**, mas o usuário pode aplicar um **filtro customizado** de período.

### Componentes

#### 1. `layout/activity_historico.xml`
- Botão **📅** ao lado das setas ← →
- Botão **✕** vermelho (aparece apenas quando filtro ativo)

#### 2. `view/HistoricoActivity.java`
- Variáveis `dataInicialFiltro`, `dataFinalFiltro`, `filtroAtivo`
- Método `abrirDialogoPeriodo()` — 2 DatePickers em sequência
- Método `aplicarFiltroPeriodo()`, `limparFiltroPeriodo()`
- Método `estaEntreDatas()`, `compararDatas()`

### Comportamento

| Ação | Comportamento |
|------|---------------|
| Toque em 📅 | Abre DatePicker inicial → depois final |
| Escolhe datas | Filtro aplicado, lista filtrada |
| Título | Vira "01/09/2026 a 30/09/2026" |
| Badge "Atual" | Desaparece |
| Botão ✕ | Aparece |
| Toque em ← ou → | Limpa filtro automaticamente |
| Toque em ✕ | Volta ao modo mês |

---

## 💾 Backup Local (Exportar/Restaurar em JSON)

### Data de implementação
10/10/2026

### Descrição
O usuário pode **exportar todos os seus dados** para um arquivo JSON e **restaurá-los** depois.

### Cenários de Uso
1. **Trocar de celular** — exporta no antigo, importa no novo
2. **Reinstalar o app** — restaura o backup mais recente
3. **Recuperação de erro** — volta ao estado anterior
4. **Análise externa** — compartilha com outros

### Componentes

#### 1. `model/BackupData.java`
- POJO com: `versao`, `dataBackup`, `nomeUsuario`, `totalItens`
- Listas: `dividas`, `cartoes`, `chavesPix`, `categorias`

#### 2. `utils/BackupHelper.java`
- `exportar(context, userId)` — gera arquivo JSON
- `compartilharBackup(context, arquivo)` — abre Intent
- `lerArquivo(context, uri)` — lê e valida o JSON
- `aplicarBackup(context, userId, data)` — substitui dados

#### 3. `view/ConfiguracoesActivity.java`
- `ActivityResultLauncher` para selecionar arquivo
- Diálogo com 3 opções: Exportar / Restaurar / Limpar Tudo

### Estrutura do JSON

```json
{
  "versao": 1,
  "dataBackup": "10/10/2026 22:30",
  "nomeUsuario": "Gustavo",
  "totalItens": 15,
  "dividas": [ ... ],
  "cartoes": [ ... ],
  "chavesPix": [ ... ],
  "categorias": [ ... ]
}
```

### Segurança
- ✅ Valida o campo `versao` antes de importar
- ✅ Mostra resumo antes de confirmar
- ✅ Confirmação obrigatória
- ✅ Reagenda alarmes das dívidas não pagas após importar

---

## 🔄 Migration Não Destrutiva

### Data de implementação
10/10/2026

### Descrição
Sistema de **migrations reais** para o banco Room, preservando dados entre atualizações de versão.

### Problema Original
O `AppDatabase` usava apenas `fallbackToDestructiveMigration()`, o que **apagava todos os dados** quando a versão do banco mudava.

### Solução

#### 1. `database/Migrations.java` (novo)
Agrupa todas as migrations:

```java
public static final Migration MIGRATION_8_9 = new Migration(8, 9) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        database.execSQL(
            "ALTER TABLE dividas ADD COLUMN grupoId INTEGER NOT NULL DEFAULT 0"
        );
    }
};
```

#### 2. `database/AppDatabase.java` (atualizado)
Registra as migrations:

```java
Room.databaseBuilder(...)
    .addMigrations(Migrations.MIGRATION_8_9)
    .fallbackToDestructiveMigration()  // rede de segurança
    .build();
```

### Como Adicionar Novas Migrations

Sempre que subir a versão (ex: v9 → v10):

1. Cria `MIGRATION_9_10` em `Migrations.java`
2. Registra em `AppDatabase`: `.addMigrations(MIGRATION_8_9, MIGRATION_9_10)`
3. Muda `version = 10` no `@Database`
4. Testa: instala v9 → cadastra dados → atualiza para v10 → confirma que dados persistem

### Impacto
- ✅ Atualizações do app **preservam os dados**
- ✅ Rede de segurança para versões muito antigas (v1-v7)
- ✅ Padrão profissional de mercado

---

## 🏠 Agrupamento de Parcelas na Tela Início

### Data de implementação
10/10/2026

### Descrição
As parcelas de uma mesma compra são **agrupadas em um único card** na tela de Início, evitando poluição visual.

### Problema Original
O Xbox em 4x aparecia como **3 cards separados** em "Próximos Vencimentos".

### Solução

#### 1. `model/VencimentoItem.java` (novo)
POJO que agrupa informações de um grupo:

```java
public class VencimentoItem {
    public String titulo;
    public String inicial;
    public String proximoVencimento;
    public double valorProxima;
    public double totalRestante;
    public int parcelasRestantes;
    public int totalParcelas;
    public boolean parcelado;
}
```

#### 2. `adapter/VencimentoAdapter.java` (reescrito)
Recebe uma lista de `VencimentoItem` (já agrupada) e renderiza o card.

#### 3. `view/InicioActivity.java`
Novo método `agruparVencimentos()`:
1. Separa dívidas com `grupoId > 0` (parceladas) das sem grupo
2. Para cada grupo, ordena por vencimento
3. Cria um `VencimentoItem` com resumo
4. Ordena os grupos pela próxima data

### Resultado Visual

**Antes:**
```
┌─ Xbox ──────────────────────┐
│ Parcela 2/4   R$ 750        │
├─────────────────────────────┤
├─ Xbox ──────────────────────┤
│ Parcela 3/4   R$ 750        │
├─────────────────────────────┤
├─ Xbox ──────────────────────┤
│ Parcela 4/4   R$ 750        │
└─────────────────────────────┘
```

**Depois:**
```
┌─ Xbox ──────────────────────┐
│ 3 parcelas restantes        │
│ Próxima: 10/12/2026 R$ 750  │
│ Total restante: R$ 2.250    │
└─────────────────────────────┘
```

---

## 🔔 Alerta Inteligente na Tela Início

### Data de implementação
10/10/2026

### Descrição
O alerta "Atenção Este Mês" agora conta **apenas dívidas que vencem no mês atual**.

### Problema Original
O alerta "3 faturas somando R$ 2.250,00" incluía parcelas de meses futuros, confundindo o usuário.

### Solução

Novo método em `InicioActivity.atualizarTotais()`:
1. Verifica o mês atual (Calendar)
2. Conta apenas dívidas com `vencimento` no mês atual
3. Soma o valor dessas dívidas

### Comportamento

| Situação | Mensagem |
|----------|----------|
| Tem dívidas vencendo este mês | "X contas vencendo este mês • R$ Y,YY" |
| Não tem nada vencendo | "Nenhuma conta vence este mês ✓" |

---

## 🧪 Como testar tudo

### Parcelamento
1. Cadastre **Videogame R$ 3.000 em 4x**, 1º vencimento em **Novembro**
2. ✅ Deve criar **4 registros** no banco (1/4, 2/4, 3/4, 4/4)
3. Abra a tela **Dívidas** — mostra as 4 parcelas
4. Abra o **Dashboard** em Novembro — Videogame 1/4

### Biometria
1. Ative em **Configurações → Segurança & Biometria**
2. Faça logout
3. Na tela de Login, toque em **"👤 Usar biometria"**
4. Coloque a digital → ✅ Login direto

### Gráfico de Linha
1. Abra o Dashboard
2. ✅ Aparece gráfico com chips: `3M` `6M` `12M`
3. Toque em cada chip → gráfico se adapta
4. ✅ Valores, média e destaque aparecem

### Filtro de Período
1. Abra o Histórico
2. Toque em **📅**
3. Escolha data inicial e final
4. ✅ Lista filtra pelo período

### Backup
1. Configurações → Backup → **📤 Exportar**
2. Compartilhe via Drive
3. **Limpar Tudo**
4. Configurações → Backup → **📥 Restaurar**
5. ✅ Resumo → confirme → dados voltam

### Migration
1. Instalar a v9 (fresh install)
2. Cadastrar dados
3. Fechar e reabrir → ✅ dados persistem

### Agrupamento na Início
1. Cadastrar Xbox em 4x
2. ✅ Aparece como 1 card com "3 parcelas restantes"

---

## 🚧 Próximos passos

- [ ] Testes da Sprint 6 pelo Israel (CT-14 a CT-22)
- [ ] Testar notificações (CT-10 e CT-11)
- [ ] Autenticação em nuvem (Firebase) — opcional
- [ ] Sincronização entre dispositivos — opcional
- [ ] Exportação XLSX (além de CSV)
- [ ] Melhorias no fluxo de backup (lista de backups internos)

---

## 📚 Referências

- Autor das Sprints 5 e 6: Gustavo Piteira
- Soft Delete: 06/10/2026
- Tela de Histórico: 06/10/2026
- Exportação CSV/PDF: 06/10/2026
- Parcelamento por Mês: 07/10/2026
- Dashboard Somente Leitura: 07/10/2026
- Biometria Real: 08/10/2026
- Gráfico de Linha: 10/10/2026
- Filtro de Período: 10/10/2026
- Backup Local: 10/10/2026
- **Migration Não Destrutiva: 10/10/2026**
- **Agrupamento de Parcelas na Início: 10/10/2026**
- **Alerta Inteligente: 10/10/2026**
