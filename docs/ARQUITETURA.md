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

---

## ✅ Solução implementada

### 1. Campo `usuarioId` adicionado nas entidades

```java
@Entity(tableName = "dividas")
public class Divida {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int usuarioId; // ← NOVO CAMPO
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

> **Nota (27/10/2026):** o Dashboard foi redesenhado (ver a seção "Redesign do Dashboard / Relatórios"). Continua somente leitura. Na tela Dívidas, os botões Editar e Excluir agora são ícones e o Pagar é o botão principal (ver "Redesign da Tela Dívidas").

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

### Estilo Visual
- Linha verde, valores em cima dos pontos, eixo Y formatado, destaque em roxo para maior gasto, média tracejada em amarelo, gradiente verde no preenchimento e animação suave.

> **Nota (27/10/2026):** no redesign do Dashboard o cabeçalho passou a ter um ícone de linha no lugar do emoji 📈, e **total e média viraram dois números destacados** (em vez da frase única). O maior gasto agora é destacado em **âmbar** (não mais roxo), a curva usa `HORIZONTAL_BEZIER` (não afunda abaixo de zero), o eixo Y começa em R$ 0, os valores sobre os pontos aparecem só até 6M e, no 12M, os meses saem alternados. O cálculo de período (centrado no mês selecionado) não mudou. Altura do gráfico: 220dp.

---

## 📅 Filtro de Período Customizado no Histórico

### Data de implementação
10/10/2026

### Descrição
Navegação por mês **continua funcionando**, mas o usuário pode aplicar um **filtro customizado** de período.

### Componentes
- Botão **📅** ao lado das setas ← →
- Botão **✕** vermelho (aparece apenas quando filtro ativo)
- Dois `DatePicker`s em sequência
- Título muda para o intervalo selecionado

---

## 💾 Backup Local (Exportar/Restaurar em JSON)

### Data de implementação
10/10/2026

### Descrição
O usuário pode **exportar todos os seus dados** para um arquivo JSON e **restaurá-los** depois.

### Componentes
- `model/BackupData.java` — POJO com os dados
- `utils/BackupHelper.java` — lógica de exportar, ler e aplicar
- `view/ConfiguracoesActivity.java` — diálogo com 3 opções

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

---

## 🔄 Migration Não Destrutiva

### Data de implementação
10/10/2026

### Descrição
Sistema de **migrations reais** para o banco Room, preservando dados entre atualizações de versão.

### Solução
- `database/Migrations.java` (novo) agrupa todas as migrations
- `database/AppDatabase.java` registra as migrations com `.addMigrations()`

---

## 🏠 Agrupamento de Parcelas na Tela Início

### Data de implementação
10/10/2026

### Descrição
As parcelas de uma mesma compra são **agrupadas em um único card** na tela de Início.

### Solução
- `model/VencimentoItem.java` — POJO que agrupa informações
- `adapter/VencimentoAdapter.java` — reescrito para receber `VencimentoItem`
- `view/InicioActivity.java` — novo método `agruparVencimentos()`

> **Nota (25/10/2026):** com o redesign da Início, o `RecyclerView rvVencimentos` foi removido da tela. `VencimentoAdapter` e `VencimentoItem` continuam no projeto, mas sem uso nessa tela. O agrupamento de parcelas agora é feito diretamente nos cards da Início.

---

## 🔔 Alerta Inteligente na Tela Início

### Data de implementação
10/10/2026

### Descrição
O alerta "Atenção Este Mês" agora conta **apenas dívidas que vencem no mês atual**.

### Solução
Novo método em `InicioActivity.atualizarTotais()` que verifica o mês atual e conta apenas as dívidas com vencimento nele.

---

## 🤖 Leitura Automática de Comprovantes (OCR) ⭐

### Data de implementação
18/10/2026

### Descrição
Permite que o usuário compartilhe o comprovante de pagamento (foto, print ou PDF) com o app, e ele preencha automaticamente a tela de Cadastro de Dívida.

### Arquitetura do Fluxo

```
┌─────────────────────────────┐
│ 1. Usuário compartilha      │
│    comprovante de outro app │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│ 2. ReceiptShareActivity     │
│    recebe o Intent          │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│ 3. ReceiptOcr (ML Kit)      │
│    extrai o texto bruto     │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│ 4. ReceiptParser            │
│    interpreta os dados      │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│ 5. CadastroDividaActivity   │
│    abre pré-preenchida      │
└─────────────────────────────┘
```

### Componentes

#### 1. `utils/ReceiptOcr.java`
- Usa a biblioteca **Google ML Kit (Text Recognition)** para extrair texto de imagens.
- Funciona **offline** e sem custo.
- Método `extrairTexto(Uri, Callback)`.

#### 2. `utils/ReceiptParser.java`
- Recebe o texto bruto e tenta extrair informações estruturadas usando Regex.
- Extrai: título, valor, data, banco.
- Trata diferentes formatos de comprovante.

#### 3. `view/ReceiptShareActivity.java`
- Activity criada para receber o compartilhamento (`ACTION_SEND`).
- Exibe tela de carregamento ("Comprovante recebido. Processando...").
- Chama o `ReceiptOcr`, depois o `ReceiptParser`, e por fim redireciona para o `CadastroDividaActivity` com os dados via `Intent`.
- Possui botão "Cancelar" para abortar o processo.

#### 4. `AndroidManifest.xml`
- Filtros de Intent na `ReceiptShareActivity` para receber `image/*` e `application/pdf`.

### Limitações Conhecidas
- O parser pode não identificar 100% dos casos em comprovantes muito diferentes do padrão (ex: bancos menores).
- Se o OCR falhar, o usuário é redirecionado para o formulário em branco.

### Próximos Passos (Futuro)
- Treinar um modelo próprio via Orange/TensorFlow Lite para substituir o parser heurístico por IA.
- Adicionar suporte a mais formatos de PDF.

> **Nota (25/10/2026):** a tela Lançar redesenhada ganhou um segundo caminho de importação — o botão **"Preencher com comprovante"** direto no formulário (ver seção do redesign da Lançar). O fluxo de compartilhamento externo acima continua existindo e alimenta o formulário via `extras` (`extra_prefill_*`).

---

## 🎨 Redesign Visual — Padrão de UI

### Data de implementação
25/10/2026 (Início e Lançar) e 27/10/2026 (Dívidas, Dashboard/Relatórios e Configurações); demais telas pendentes

### Objetivo
Deixar **todas as telas com visual profissional**, no mesmo padrão da tela Início: dark mode fixo, fundo azul-marinho com brilho verde/azul, cards arredondados, verde esmeralda, barra inferior flutuante.

### Status
- ✅ **Início** — redesenhada (antes desta entrega, feita na fase anterior)
- ✅ **Lançar / Editar dívida** (`CadastroDividaActivity`) — redesenhada e entregue
- ✅ **Dívidas** (`DividasActivity`) — redesenhada e entregue
- ✅ **Dashboard / Relatórios** (`DashboardActivity`) — redesenhado e entregue
- ✅ **Configurações** (`ConfiguracoesActivity`) — redesenhada e entregue
- ⏳ Demais telas — ainda no visual antigo

### Build
✅ Compila e roda no Android Studio do Gustavo após a entrega da Lançar e da Dívidas.

### Banco de dados
Nenhuma alteração de schema nestas entregas — Room continua na **v9**.

### Design System atual

| Elemento | Valor |
|----------|-------|
| Fundo de tela | `@drawable/bg_gradient_inicio` (base #0A101C + brilho verde #4010B981 topo-esquerdo + azul #382563EB topo-direito) |
| Card padrão | `@drawable/bg_card_glass` (#111A2E, borda #1E2A44, raio 24dp, padding 18–20dp) |
| Card de atenção | `@drawable/bg_card_atencao` (#15171E, borda #5C4310, raio 24dp) |
| Campo de formulário | `@drawable/bg_lancar_campo` (#111A2E, borda #1E2A44, raio 16dp, altura 52dp) |
| Verde de destaque | #10B981 (botões, bordas) e #34D399 (valores/textos positivos) |
| Vermelho | #EF4444 / #F87171 |
| Âmbar (alerta) | #F59E0B; fundo de badge #3A2C10 |
| Texto | primário #FFFFFF; secundário #94A3B8; apagado #64748B; hint #475569 |
| Título de tela | 26sp, bold, branco; subtítulo 14sp #94A3B8 (as telas Dívidas, Dashboard e Configurações usam 28sp) |
| Label de seção | 14sp #94A3B8; links de ação "+ Novo" 14sp bold #34D399 |
| Título de seção em lista | 12sp bold #94A3B8, caixa alta, letterSpacing 0.08 (ex.: CONTA, PREFERÊNCIAS, DADOS) |
| Botão primário | `MaterialButton`, backgroundTint #10B981, texto preto bold 16sp, altura 56dp, cornerRadius 18dp |
| Botão primário (TextView) | `@drawable/bg_botao_primario` (#10B981, raio 18dp), altura 56dp, texto #052E22 bold 16sp (usado em Dívidas) |
| Botão secundário tracejado | `bg_lancar_comprovante` (verde translúcido) |
| Botão secundário preenchido | `bg_botao_secundario` (#0E1424, borda #1E2A44, raio 16dp, texto #34D399 bold 14sp, altura 52dp) |
| Botão Pagar / Pago | `bg_botao_pagar` (#10B981, raio 14dp, texto #052E22) / `bg_botao_pago` (#123D33, texto #34D399); altura 44dp |
| Ícone de ação (Editar/Excluir) | `bg_icone_acao` (44dp, #0E1424, borda #222D45, raio 14dp); Editar #94A3B8, Excluir #F87171 |
| Botão "Desconectar" | `bg_botao_sair` (#1AEF4444, borda #5C2A2E, raio 18dp), altura 56dp, ícone + texto #F87171 |
| Botão "Editar" do perfil | `bg_botao_editar` (#123D33, borda #10B981, raio 22dp), altura 44dp, texto #34D399 |
| Chip | altura 44dp, raio 24dp, texto 14sp; fundo #111A2E → #123D33, borda #1E2A44 → #10B981, texto #E2E8F0 → #34D399 quando selecionado |
| Pílula de filtro (com seta) | `bg_pill_filtro` (#111A2E, borda #1E2A44, raio 20dp, altura 40dp, ícone `ic_chevron_down` #94A3B8) |
| Bloco de data (mês/dia) | `bg_data_card` (52×56dp, #0E1424, borda #222D45, raio 14dp); mês 10sp bold #94A3B8, dia 20sp bold branco |
| Item de lançamento (Dashboard) | `bg_item_lancamento` (#0B1322, borda #1A2438, raio 16dp) |
| Linha de legenda (rosca) | 48dp de altura; selecionada usa `bg_legenda_ativa` (#0E1424, borda #10B981, raio 14dp) |
| Badge "Atual" | `bg_badge_atual` (#123D33, borda #10B981, raio 12dp, texto #34D399) |
| Ícone de configuração | vetor 24dp (tint claro) dentro de quadrado 40dp com `bg_icone_conf_verde` / `_azul` / `_ambar` / `_roxo` / `_cinza` (fundo translúcido, raio 12dp) |
| Chaves (switches) | `res/color/switch_track.xml` e `switch_thumb.xml`: ligada = trilha #10B981 + botão #FFFFFF; desligada = trilha #2A3650 + botão #94A3B8; desativada = trilha #1E2A44 + botão #475569 |
| Paleta das categorias (rosca) | #10B981, #3B82F6, #F59E0B, #EC4899, #06B6D4, #EF4444, #8B5CF6, #EAB308 (por ordem de valor) |
| Barra inferior | pílula `bg_nav_pill` (#F2101A2B, borda #22304A, raio 32dp), 64dp, margens 20dp laterais e 16dp embaixo, elevation 8dp; aba ativa = ícone + texto #34D399 em `bg_tab_ativo` (#123D33); inativas = só ícone #94A3B8 |
| Ícones | vetores 24dp, stroke 1.75, cor via `app:tint` |
| Formatação de valor (telas novas) | `NumberFormat.getCurrencyInstance(new Locale("pt","BR"))` → `R$ 1.500,00` |
| Fonte | Roboto (padrão atual). A fonte Inter já está no projeto (`res/font/inter_medium.xml`), mas ainda não aplicada globalmente no tema. `abril_fatface` só em telas antigas |

> **Observação:** hoje coexistem **duas paletas** no projeto (a antiga, em `colors.xml`, e a nova, com hex direto nos layouts novos). A unificação está na lista de pendências.

### Lição aprendida
Erros de **"Android resource linking failed"** acontecem quando um drawable referenciado não existe ou está com nome errado. Antes de entregar qualquer layout novo, conferir se **todos os drawables referenciados existem** com o nome exato.

---

## 🏠 Redesign da Tela Início

### Arquivos
- `res/layout/activity_inicio.xml` — reescrito
- `java/.../view/InicioActivity.java` — reescrito
- `res/layout/item_conta_atencao.xml` — novo item de lista

### O que a tela tem hoje
- Cabeçalho com avatar e badge "Sincronizado"
- Card de saldo disponível (com botão de ocultar valores)
- Barra de orçamento
- Cards "Dívidas em aberto" e "Total já pago"
- Card "Atenção este mês" — contas que vencem no mês, "Vence em N dias", total a pagar e % do salário
- Barra inferior flutuante

### Regras da Início (preservadas)
- **Saldo** = salário mensal − `somarGastosDoMes(userId, MM/yyyy)`
  - Cinza se salário ≤ 0; vermelho se negativo; verde se positivo
- **Barra de orçamento** = % do salário gasto (máx. 100)
- **Dívidas em aberto** = soma de `valorRestante` das não pagas
- **Total pago** = soma de `valorTotal` das pagas
- **"Atenção este mês"** = não pagas com vencimento no mês/ano atual, ordenadas por data
  - "Vence em N dias": âmbar até 5 dias, cinza depois, vermelho se vencida (trata hoje/amanhã/ontem)
  - **Total a pagar** = soma de `valorRestante` dessas contas
  - "Compromete X%" do salário e "Sobra/Faltam" comparando com o saldo
- Valores ocultos exibem `R$ ••••`

### Mudanças de IDs e tipos (importante ao migrar)
- `tabLancar`, `tabDividas`, `tabRelatorios`, `tabConfig` mudaram de **`TextView` para `ImageView`** na reforma
- `btnVerTodosVencimentos` mudou de **`Button` para `TextView`**
- `rvVencimentos` (RecyclerView) foi **removido** da tela
- A Início ainda usa **barra inferior própria** no layout (não usa o `layout_bottom_nav` ainda)
- `onResume()` recarrega tudo
- Pede permissão `POST_NOTIFICATIONS` (request code 100)

### Itens de lista usados
- `item_conta_atencao.xml` — IDs: `tvMesConta`, `tvDiaConta`, `tvTituloConta`, `tvVenceConta`, `tvValorConta`, `divisorConta`

---

## ➕ Redesign da Tela Lançar / Editar Dívida (`CadastroDividaActivity`)

### Arquivos
- `res/layout/activity_cadastro_divida.xml` — reescrito do zero
- `java/.../view/CadastroDividaActivity.java` — reescrita completa
- `res/layout/item_chip_selecao.xml` — usado na lista horizontal de seleção (redesenhado)
- `adapter/ChipSelecaoAdapter.java` — adapter dos cards de seleção

### Estrutura do layout
`ConstraintLayout` com fundo `bg_gradient_inicio` → `ScrollView` com `paddingBottom="112dp"` e `clipToPadding="false"` → conteúdo → `<include layout="@layout/layout_bottom_nav" />`.

### IDs principais
`btnVoltar`, `tvTituloTela`, `tvSubtituloTela`, `btnImportarComprovante` (agora `LinearLayout`), `ivComprovante`, `tvComprovante`, `editValor`, `containerParcelas`, `chipGroupParcelas`, `tvResumoParcelas`, `chipGroupTipo`, `containerSelecao`, `tvTituloSelecao`, `btnNovoItem`, `containerListaSelecao`, `rvSelecao`, `btnAdicionarItem`, `containerVazio`, `tvMensagemVazio`, `btnCadastrarAgora`, `editDescricao`, `btnNovaCategoria`, `chipGroupCategoria`, `tvSemCategoria`, `boxDataCompra`, `editDataCompra`, `containerVencimento`, `boxVencimento`, `editVencimento`, `btnSalvarDivida`, `bottomNavigation` (o include).

**IDs removidos do layout antigo:** `spinnerTipoDivida`, `spinnerCategoria`, `spinnerParcelas`, `bottomNavigation` antiga, as `TextView` de abas e o `MaterialButton btnImportarComprovante`.

### O que a tela faz
- **Valor total:** `EditText` grande com máscara de moeda (`R$ 0,00`, limite 11 dígitos, vira centavos)
- **Parcelas:** chips 1x, 2x, 3x, 6x, 12x e **Outro** (diálogo para digitar 1 a 60; o chip passa a mostrar, ex.: "18x")
  - Resumo dinâmico: "Nx de R$ X · à vista/parcelado"
  - Escondidas para tipos à vista e no modo edição
- **Tipo de dívida (7 chips):** Cartão de Crédito, Cartão de Débito, Pix, Empréstimo, Fatura, Boleto, Outros
- **Seleção de cartão / chave Pix / banco:** lista horizontal de cards + card tracejado "Adicionar"
  - Vazio mostra mensagem e "Cadastrar agora" (abre `CartoesActivity`)
- **Categoria:** chips com bolinha colorida (cor do banco); "+ Nova" abre `CategoriasActivity`; se não houver categorias, mostra aviso (`tvSemCategoria`)
- **Datas:** Data da compra e 1º vencimento abrem `DatePickerDialog` (que agora começa na data já digitada)
- **Preencher com comprovante:** `OpenDocument` (imagem/PDF) → cache → bitmap → OCR (`ReceiptOcr`) → parser (`ReceiptParser`) → seleciona o tipo Pix e preenche campos. Rejeita se não achar valor.
- **Prefill por Intent** (vindo de `ReceiptShareActivity`): `extra_prefill_receipt`, `extra_prefill_titulo`, `extra_prefill_valor`, `extra_prefill_data`, `extra_prefill_banco`, `extra_prefill_beneficiario`, `extra_prefill_categoria`, `extra_prefill_parcelas`
- **Modo edição** (`extra divida_id`): preenche os campos, título vira "Editar dívida", botão vira "Atualizar dívida", esconde o importar comprovante
- **Barra inferior:** `<include>` + `BottomNavHelper`

### Padrões de implementação
- Chips criados por código com `new Chip(this)` (método `criarChip`)
- Estilo dos chips via `ColorStateList` (estado `checked`):
  - fundo #111A2E → #123D33
  - borda #1E2A44 → #10B981
  - texto #E2E8F0 → #34D399
- Seleção **manual** (`marcarChips(grupo, tag)`), **não** usa `singleSelection` do `ChipGroup`
- **Tag do chip = valor lógico:** `String` do tipo, `Integer` de parcelas, nome da categoria
- **Estado em variáveis:** `tipoSelecionado`, `parcelasSel`, `categoriaSelecionada`, `chipAdapter`, `dividaEmEdicao`, `bancoEdicaoReconhecido`
- **Fluxo:** `selecionarTipo()` → `atualizarSelecao()` (monta lista de cartões/chaves) → `atualizarResumo()`
- `onResume()` (ignorando a primeira chamada) recarrega categorias e seleção mantendo o que estava escolhido
- **Máscara de valor** via `TextWatcher` com flag `isUpdating`; chama `atualizarResumo()` a cada mudança
- **Salvar** (`salvarDivida()`): valida categoria, descrição, valor > 0 e vencimento (se não for à vista); então:
  - edição → atualiza
  - à vista → insere como pago
  - 1 parcela → insere + agenda alarme
  - N parcelas → cria grupo + N inserts + N alarmes

### Regras de negócio preservadas (NÃO podem mudar)
- **À vista = Pix e Cartão de Débito:**
  - Escondem parcelas e vencimento
  - Salvam já como pago (`valorPago = valor`, `parcela = "1x (À vista)"`)
  - Vencimento = data informada, ou data da compra, ou hoje
- **Crédito / Débito** listam `Cartao` filtrados por tipo ("Crédito"/"Débito")
- **Pix** lista `ChavePix` (vínculo opcional; fallback do banco = nome do tipo)
- Os demais tipos mostram um item "Genérico"
- Cada item de seleção é um `String[] {sigla, nome, valorReal, detalhe}` — o **índice 2 (`valorReal`) vira `Divida.banco`**
- **1 parcela:** grava uma `Divida` não paga e agenda alarme (`AlarmeHelper.agendar`)
- **N parcelas:**
  - `grupoId = dividaDao().maxGrupoId() + 1`
  - Cada parcela com `valorTotal = valorParcela = valor / N`, `valorPago = 0`
  - Vencimento somando meses a partir do 1º vencimento
  - `parcela = "i/N"`
  - Alarme em cada uma
- **Categoria excluída (edição):** abre diálogo para recriar a categoria

### Correções de comportamento feitas junto do redesign
1. Editar uma parcela não apaga mais o identificador "i/N" (antes, ao salvar, `parcela` era sobrescrito por "1x (À vista)")
2. Na edição, `valorParcela` acompanha o valor, e `valorPago` também se a dívida já estiver paga
3. Na edição, o app tenta reconhecer tipo/cartão/chave pelo campo `banco`; se não reconhecer, **preserva o banco original** (antes trocava pelo primeiro cartão da lista)
4. Voltar de Cartões/Categorias recarrega a tela mantendo a seleção (antes a categoria nova só aparecia reabrindo a tela)
5. `DatePicker` abre na data já digitada, não sempre em hoje

---

## 💸 Redesign da Tela Dívidas (`DividasActivity`)

### Data de implementação
27/10/2026

### Arquivos
- `res/layout/activity_dividas.xml` — reescrito
- `res/layout/item_divida.xml` — reescrito
- `java/.../view/DividasActivity.java` — reescrita (mesma lógica de ações)
- `java/.../adapter/DividaAdapter.java` — reescrito

### Estrutura do layout
`ConstraintLayout` com fundo `bg_gradient_inicio` → `ScrollView` (`paddingBottom="112dp"`, `clipToPadding="false"`) → título, filtros, card de totais, `RecyclerView` (`nestedScrollingEnabled=false`), estado vazio e botão "Cadastrar nova dívida" → barra inferior flutuante própria (ainda sem o `<include>`).

### IDs principais
- **Tela:** `btnTodosDevedores`, `btnCategorias`, `tvTotalAPagar`, `tvTotalPago`, `tvTotalGeral`, `rvDividas`, `tvEmptyDividas`, `btnCadastrarDivida`, `bottomNavigation`, `tabInicio`, `tabLancar`, `tabDividas` (aba ativa), `tabRelatorios`, `tabConfig`
- **Item (`item_divida`):** `tvMesDivida`, `tvDiaDivida`, `tvTituloDivida`, `tvDetalheDivida`, `tvValorRestante` (agora mostra o valor da parcela), `tvVencimento` (agora mostra a situação do vencimento), `tvParcela`, `llParcelas`, `tvProgressoParcelas`, `progressParcelas`, `btnEditar`, `btnExcluir`, `btnPagar`

### Mudanças de tipos (importante ao migrar)
- `btnEditar` e `btnExcluir`: de `Button` para **`ImageView`**
- `btnPagar` e `btnCadastrarDivida`: de `Button` para **`TextView`**
- `btnTodosDevedores` e `btnCategorias`: de `Button` para **`LinearLayout`** (pílula com texto + seta)
- `tabDividas` é um `LinearLayout` (aba ativa); `tabInicio`, `tabLancar`, `tabRelatorios` e `tabConfig` são `ImageView`
- O `DividaAdapter.OnDividaActionListener` **não mudou** (`onExcluirClick`, `onEditarClick`, `onPagarClick`)

### Regras preservadas
- **Excluir:** diálogo de confirmação → `AlarmeHelper.cancelar` → **soft delete** (`setExcluida(true)` + `atualizar`)
- **Editar:** abre `CadastroDividaActivity` com `extra divida_id`
- **Pagar:** quita aquele registro (que já é UMA parcela): cancela o alarme, `valorPago = valorTotal`, `setPago(true)`
- **Totais:** `valorTotal`, `valorPago` e `valorRestante` somados sobre as dívidas do usuário (já sem excluídas, pelo DAO)

### Mudanças de comportamento
- **Ordem da lista:** não pagas primeiro (por vencimento), pagas por último. Para voltar à ordem do banco, remover `dividas.sort(this::compararDividas)` e o método `compararDividas` na `DividasActivity`.
- **Situação do vencimento** (não pagas): "Vence em N dias" (âmbar até 5 dias, cinza depois), "Vence hoje", "Vence amanhã" (âmbar), "Venceu ontem" / "Venceu há N dias" (vermelho). Pagas mostram "Paga ✓" e o card fica esmaecido (`alpha 0.75`).
- **Progresso de parcelas:** para `totalParcelas > 1`, mostra "Pagas: X de N" e uma barra. As pagas são contadas **entre as dívidas do mesmo `grupoId` presentes na lista carregada**.
- **Estado vazio:** mostra "Nenhuma dívida cadastrada ainda" e esconde o `RecyclerView`.
- **Valores:** formatados com `NumberFormat` pt-BR (com milhar). O card mostra `getValorTotal()` (valor da parcela).
- **Filtros "Todos devedores" e "Categorias":** continuam **sem ação** (já eram assim antes do redesign). O `DividaDao` já tem `listarPorCategoria`.

---

## 📊 Redesign do Dashboard / Relatórios (`DashboardActivity`)

### Data de implementação
27/10/2026

### Arquivos
- `res/layout/activity_dashboard.xml` — reescrito
- `res/layout/item_divida_dashboard.xml` — reescrito
- `res/layout/item_legenda_categoria.xml` — novo (o `item_legenda.xml` antigo não é usado por esta tela)
- `java/.../view/DashboardActivity.java` — reescrita
- `java/.../adapter/DividaDashboardAdapter.java` — reescrito
- Os gráficos continuam sendo **MPAndroidChart** (`PieChart` e `LineChart`)

### Estrutura do layout
`ConstraintLayout` com fundo `bg_gradient_inicio` → `ScrollView` com: título → seletor de mês → card Balanço do mês → card Rosca + legenda → card Evolução mensal → chips de filtro → card de lançamentos (lista + exportar) → barra inferior flutuante própria (aba ativa `tabRelatorios`).

### IDs principais
- **Mês:** `btnMesAnteriorDash`, `btnMesProximoDash` (agora `ImageView`), `tvMesAtualDash`, `tvBadgeAtualDash`
- **Balanço:** `tvSaldoDash`, `progressBalanco`, `tvBalancoCaption`, `tvSalarioDash`, `tvGastosDash`
- **Rosca:** `pieChart`, `llLegenda`
- **Evolução:** `tvTituloTotalEvolucao`, `tvTotalEvolucao`, `tvMediaEvolucao`, `containerPeriodos`, `lineChart`
- **Lista:** `containerFiltros`, `tvTituloLista`, `tvTotalLista`, `rvLancamentos`, `tvVazioLista`, `btnExportar` (agora `TextView`)
- **Item de lançamento:** `tvMesDash`, `tvDiaDash`, `tvTituloDivida`, `tvDetalheDivida`, `tvParcela`, `tvValorRestante`, `tvVencimento` (agora mostra a situação: "Pago ✓", "Pendente" ou "Atrasada")
- **Item da legenda:** `vCorLegenda`, `tvNomeLegendaCat`, `tvPercentualLegenda`, `tvValorLegenda`
- **Navegação:** `tabRelatorios` é `LinearLayout` (aba ativa); as demais abas são `ImageView`

### Regras preservadas
- Dívidas do mês selecionado = vencimento no mês/ano (`pertenceAoMes`), já sem excluídas
- Navegação por mês (← →) recarrega os dados
- Evolução mensal centrada no mês selecionado (3M/6M/12M)
- Exportação CSV/PDF usa a lista filtrada (`dividasFiltradas`) e a categoria atual (`CsvExportHelper`, `PdfExportHelper`)

### Mudanças de comportamento
- **Rosca:** sem texto nas fatias (acabou o "Alimentação100%" colado), buraco de 72% e total no centro. **Sempre mostra o mês inteiro**; o filtro de categoria afeta só a lista, o título e o total da lista. Antes, filtrar refazia a rosca com uma categoria só (sempre 100%).
- **Legenda:** lista abaixo da rosca com cor, nome, percentual e valor, ordenada do maior para o menor valor. **Tocar numa linha filtra** a lista; tocar de novo volta para "Todos". A linha selecionada recebe `bg_legenda_ativa`.
- **Filtro:** os chips e a legenda estão sincronizados; ao trocar de mês o filtro volta para "Todos".
- **Balanço do mês:** os **gastos vêm das dívidas do mês** (soma de `valorTotal`), mesmo que o usuário não seja encontrado. O salário vem de `usuarioDao().buscarPorId`. Se o salário for 0 ou o usuário não for encontrado, o saldo fica cinza e aparece o aviso "Salário não encontrado. Confira em Configurações."
- **Evolução mensal:** curva `HORIZONTAL_BEZIER`, eixo Y começando em 0, total e média como números destacados, valores nos pontos só até 6M, rótulos de mês alternados no 12M; maior gasto e linha da média em âmbar.
- **Badge "Atual":** usa `INVISIBLE` (não `GONE`) para o nome do mês não se mover ao navegar.
- **Lista vazia:** mostra "Nenhum lançamento neste mês".
- **Paleta das categorias:** por ordem de valor, a lista de 8 cores do Design System; as cores vêm do app (não da cor salva em `Categoria`).

> **Observação:** a cor de cada categoria no Dashboard é atribuída por ordem de valor, **não** pela cor cadastrada na tela de Categorias. Se for preciso casar as duas, o `DashboardActivity` precisa consultar `CategoriaDao`.

---

## ⚙️ Redesign da Tela Configurações (`ConfiguracoesActivity`)

### Data de implementação
27/10/2026

### Arquivos
- `res/layout/activity_configuracoes.xml` — reescrito
- `java/.../view/ConfiguracoesActivity.java` — reescrita (mesmas regras)
- `res/color/switch_track.xml`, `res/color/switch_thumb.xml` — novos (pasta `res/color`)

### Estrutura do layout
Fundo `bg_gradient_inicio` → `ScrollView` → título → card de perfil → seção **CONTA** (cartões, categorias, salário) → seção **PREFERÊNCIAS** (lembretes, biometria) → seção **DADOS** (backup) → botão "Desconectar da conta" → versão → barra inferior flutuante própria (aba ativa `tabConfig`).

### IDs principais
`tvAvatar`, `tvNomeUsuario`, `tvEditar`, `itemCartoes`, `itemCategorias`, `itemSalario`, `tvSalarioAtual`, `itemLembretes`, `switchLembretes`, `itemBiometria`, `tvBiometriaSub`, `switchBiometria`, `itemBackup`, `itemDesconectar`, `bottomNavigation`, `tabInicio`, `tabLancar`, `tabDividas`, `tabRelatorios`, `tabConfig` (aba ativa, `LinearLayout`; as demais são `ImageView`).

### Regras preservadas
- **Lembretes:** `AlarmeHelper.isLembretesAtivos` / `setLembretesAtivos`
- **Biometria:** `BiometricHelper.podeUsarBiometria`; ativar pede autenticação e chama `SessionManager.salvarUsuarioBiometrico`; desativar chama `limparBiometria`; se a autenticação falhar, a chave volta para desligada
- **Salário:** diálogo com máscara de moeda; salva com `usuarioDao().atualizar`
- **Backup:** exportar (`BackupHelper.exportar` + compartilhar), restaurar (`OpenDocument` + confirmação + `aplicarBackup`) e limpar tudo (testes, `db.clearAllTables()`)
- **Desconectar:** diálogo de confirmação → `session.logout()` → Login (limpando a pilha)
- **Editar perfil / Cartões / Categorias:** abrem `EditarPerfilActivity`, `CartoesActivity` e `CategoriasActivity`

### Mudanças de comportamento
- **Chaves (switches):** a trilha deixou de ser verde fixa; cada estado tem sua cor (ver Design System). Isso corrige a impressão de "ligada" quando a chave estava desligada.
- **Linha inteira clicável** para Lembretes e Biometria (alterna a chave). A biometria só alterna se estiver disponível.
- **Salário:** o subtítulo mostra "Atual: R$ X" (ou "Defina seu orçamento base" se não houver salário) e é atualizado em `onResume` e depois de salvar. Se o usuário não for encontrado ao salvar, mostra "Usuário não encontrado. Faça login novamente."
- **Perfil:** nome e avatar também são atualizados em `onResume` (ao voltar de Editar Perfil).
- **Biometria indisponível:** a linha mostra "Indisponível neste aparelho".
- **Textos:** "Face ID" virou "Entrar com digital ou rosto" (Android não tem Face ID); emojis removidos dos diálogos de backup e de restauração.
- **"Desconectar"** saiu da lista e virou um botão separado no final.

---

## 🧭 Barra Inferior Reutilizável (`layout_bottom_nav` + `BottomNavHelper`)

### Arquivos
- `res/layout/layout_bottom_nav.xml` — barra flutuante reutilizável
- `java/.../utils/BottomNavHelper.java` — helper de estado e navegação

### Estrutura do layout
5 abas — `tabInicio`, `tabLancar`, `tabDividas`, `tabRelatorios`, `tabConfig` — cada uma um `LinearLayout` com `ImageView` (`ivTab...`) e `TextView` (`tvTab...`).

### Como usar em qualquer tela

**1.** No layout da tela (dentro de um `ConstraintLayout`, após o `ScrollView`):

```xml
<include
    android:id="@+id/bottomNavigation"
    layout="@layout/layout_bottom_nav"
    android:layout_width="0dp"
    android:layout_height="64dp"
    android:layout_marginHorizontal="20dp"
    android:layout_marginBottom="16dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent" />
```

**2.** O `ScrollView` da tela deve ter `android:paddingBottom="112dp"` e `android:clipToPadding="false"`.

**3.** Na Activity, depois do `setContentView`:

```java
BottomNavHelper.configure(this, BottomNavHelper.DIVIDAS);
```

Constantes disponíveis: `INICIO`, `LANCAR`, `DIVIDAS`, `RELATORIOS`, `CONFIG`.

### O que o helper faz
- Marca a aba ativa por código: ícone + texto em pílula verde (`bg_tab_ativo`, texto #34D399); inativas só ícone (#94A3B8)
- Faz a navegação com `startActivity` + `finish`

### Cuidados
- **Não** declarar `tabInicio` etc. na Activity nem no layout da tela — já estão no `include`
- Os IDs das abas são `LinearLayout` no include. Na **Início**, **Dívidas**, **Dashboard** e **Configurações** (redesenhadas com barra própria) a aba ativa é um `LinearLayout` e as demais abas são `ImageView`; nas telas antigas são `TextView`. Atenção ao tipo ao migrar cada tela (remover os `findViewById` das abas na Activity e chamar o helper).
- A tela **Lançar já usa** o include; as demais ainda têm barra própria no layout

---

## 📐 Convenções de dados (importante não quebrar)

- **Datas:** `vencimento` é `String` no formato `dd/MM/yyyy`, comparada com `SimpleDateFormat`. O mês de referência dos gastos é `MM/yyyy`.
- **Valor monetário na tela:** campo com máscara `R$ 0,00`. Para ler: remover R$, pontos e trocar vírgula por ponto (ou usar só os dígitos divididos por 100).
- **Exibição de valores:** as telas redesenhadas usam `NumberFormat.getCurrencyInstance(new Locale("pt","BR"))` (ex.: `R$ 1.500,00`). Atenção: esse formato usa espaço não separável entre "R$" e o número.
- **Parcelas:** cada parcela é uma linha em `dividas`, ligadas por `grupoId`; o campo `parcela` guarda `i/N`; à vista guarda `1x (À vista)`.
- **Categoria e banco:** são textos na dívida (sem chave estrangeira). Excluir categoria deixa dívidas órfãs — a Lançar trata isso na edição com um diálogo.
- **Tipo de dívida:** **não é salvo como campo** — só é deduzido (ex.: pelo `banco` e por `pago`/`parcela`).
- **Entidades:**
  - `Divida` (`@Entity dividas`): `id`, `usuarioId`, `grupoId` (0 = sem grupo), `titulo`, `valorTotal` (valor desta linha; nas parcelas = valor da parcela), `valorParcela`, `valorPago`, `banco`, `categoria`, `parcela`, `vencimento`, `pago`, `excluida`
  - `Cartao` (`@Entity cartoes`): `id`, `usuarioId`, `instituicao`, `apelido`, `ultimos4Digitos`, `diaVencimento` (String), `limite` (double), `bandeira`, `tipo` ("Credito"/"Debito")
  - `Categoria` (`@Entity categorias`): `id`, `usuarioId`, `nome`, `cor` (hex)
  - `ComprovanteProcessado`: dados salvos do OCR
  - `ParsedReceipt`: POJO do resultado do OCR
  - `Transacao`: (legado/uso futuro)
- **DAOs (métodos vistos):**
  - `dividaDao`: `buscarPorId(int)`, `inserir(Divida) > long`, `atualizar(Divida)`, `maxGrupoId() > int`, `listarPorUsuario(int)`, `somarGastosDoMes(int userId, String MM/yyyy)`
  - `dividaDao` (também): `deletarFisicamente(Divida)`, `listarNaoPagasPorUsuario(int)`, `buscarPorIdDoUsuario(int id, int usuarioId)`, `listarExcluidasPorUsuario(int)`, `listarTodasParaHistorico(int)`, `listarPorCategoria(int usuarioId, String categoria)`, `listarPorGrupo(int usuarioId, int grupoId)`, `deletarTodosDoUsuario(int)`
- **Sessão:** `SessionManager` guarda o usuário logado; todas as consultas usam `session.getUserId()`. Também guarda os dados da biometria (`salvarUsuarioBiometrico`, `limparBiometria`) e o controle do onboarding; o `logout()` **não** limpa o onboarding.

---

## 🗂️ Recursos novos do redesign

### Drawables
**Início:** `bg_gradient_inicio` (reescrito como `layer-list` com dois brilhos radiais), `bg_card_glass`, `bg_card_atencao`, `bg_icone_alerta`, `bg_nav_pill`, `bg_dot_vermelho`, `bg_dot_verde`, `bg_progress_atencao`, `bg_data_card` (reescrito), `bg_badge_atencao` (reescrito), `bg_tab_ativo` (reescrito), `bg_badge_sincronizado`, `bg_badge_aberto`, `bg_badge_verde`, `bg_icone_estado`, `bg_icone_roxo`, `bg_dot_active`, `bg_dot_inactive`, `bg_progress_orcamento`, `bg_avatar`

**Lançar:** `bg_lancar_campo`, `bg_lancar_botao_voltar`, `bg_lancar_comprovante` (tracejado), `bg_lancar_adicionar` (tracejado), `bg_lancar_sigla`

**Dívidas:** `bg_botao_pagar`, `bg_botao_pago`, `bg_botao_primario`, `bg_icone_acao`, `bg_pill_filtro`

**Dashboard:** `bg_badge_atual`, `bg_botao_secundario`, `bg_legenda_ativa`, `bg_item_lancamento` (o `bg_line_chart_gradient` continua verde)

**Configurações:** `bg_icone_conf_verde`, `bg_icone_conf_azul`, `bg_icone_conf_ambar`, `bg_icone_conf_roxo`, `bg_icone_conf_cinza`, `bg_botao_editar`, `bg_botao_sair`

### Cores (pasta `res/color`)
`switch_track.xml`, `switch_thumb.xml` (seletores por estado para as chaves)

### Ícones
**Nav:** `ic_nav_home`, `ic_nav_add`, `ic_nav_card`, `ic_nav_chart`, `ic_nav_settings`
**Lançar:** `ic_lancar_voltar`, `ic_lancar_camera`, `ic_lancar_calendario`, `ic_lancar_mais`
**Dívidas:** `ic_edit`, `ic_trash`, `ic_chevron_down`
**Dashboard:** `ic_chevron_left`, `ic_chevron_right`
**Configurações:** `ic_conf_tag`, `ic_conf_money`, `ic_conf_bell`, `ic_conf_shield`, `ic_conf_database`, `ic_conf_logout` (usa também `ic_chevron_right`, `ic_nav_card` e `ic_nav_settings`)

### Layouts
- `layout_bottom_nav.xml` (novo)
- `item_conta_atencao.xml` (novo)
- `item_legenda_categoria.xml` (novo — legenda da rosca)
- `item_chip_selecao.xml` (redesenhado)
- `item_divida.xml`, `item_divida_dashboard.xml` (redesenhados)
- `activity_inicio.xml`, `activity_cadastro_divida.xml`, `activity_dividas.xml`, `activity_dashboard.xml`, `activity_configuracoes.xml` (reescritos)

---

## 📋 Estado de cada tela

| Tela | Estado |
|------|--------|
| Início | ✅ Redesenhada (barra própria, ainda sem o include) |
| Lançar / Editar dívida | ✅ Redesenhada (com include + helper) |
| Dívidas | ✅ Redesenhada (barra própria, ainda sem o include) |
| Dashboard / Relatórios | ✅ Redesenhado (barra própria, ainda sem o include) |
| Configurações | ✅ Redesenhada (barra própria, ainda sem o include) |
| Editar perfil | ⏳ Antiga |
| Cartões (+ Pix), Categorias | ⏳ Antigas (`activity_categorias.xml` com hex direto e barra antiga) |
| Histórico, Compartilhar comprovante | ⏳ Antigas |
| Splash, Onboarding, Login, Cadastro | ❓ Não vistas |

---

## ⚠️ Dívidas técnicas conhecidas e riscos

1. **Room na main thread** em todas as Activities vistas (risco de travamento com muitos dados).
2. **Sem ViewModel/Repository** — lógica de negócio dentro das Activities.
3. **Duas paletas de cor e textos hardcoded** (sem `strings.xml` em uso).
4. **Barra inferior duplicada** nas telas antigas e nas redesenhadas (Início, Dívidas, Dashboard, Configurações); migrar para o include.
5. **Data da compra não persistida** — o campo existe na tela mas não é salvo (exige novo campo em `Divida` + migração).
6. **Arredondamento das parcelas** na divisão `valor / N`.
7. **Badge "Sincronizado"** da Início é estático (não reflete sincronização real).
8. **OCR:** confirmar em qual thread os callbacks mexem na UI.
9. **Migração do Room:** qualquer mudança em `@Entity` exige subir a versão em `AppDatabase` e criar `Migration` (ou o app quebra/perde dados). Pedir o `AppDatabase.java` antes de mexer.
10. **Datas como String** (`dd/MM/yyyy`) — avaliar migrar para timestamp.
11. **Salário zerado (a verificar):** se `usuarioDao().buscarPorId(userId)` devolver `null`, os textos de salário/saldo ficam com o valor padrão do XML (na Início, por exemplo, "R$ 1.800,00" é só o texto de exemplo). O Dashboard já calcula os gastos pelas dívidas e avisa quando não acha o salário, e a tela Configurações mostra o salário atual no item "Salário mensal" para facilitar o diagnóstico. Falta conferir `Usuario`, `UsuarioDao` e o fluxo de onboarding.
12. **Filtros da tela Dívidas sem ação:** "Todos devedores" e "Categorias" são só visuais (já eram sem `setOnClickListener` antes do redesign).
13. **Cor das categorias no Dashboard:** é atribuída por ordem de valor a partir de uma paleta fixa, e não usa a cor cadastrada em `Categoria`.
14. **Diálogos padrão:** os `AlertDialog` (backup, excluir, desconectar, salário) seguem o tema do sistema e ainda não têm o visual escuro do app.
15. **"Limpar tudo (testes)":** fica dentro do diálogo de Backup e apaga o banco inteiro. Remover antes de publicar o app.

---

## 🧪 Como testar tudo

### Parcelamento, Biometria, Gráfico de Linha, Filtro de Período, Backup, Migration, Agrupamento
*(Instruções mantidas conforme documentação anterior)*

### OCR
1. Faça login no app.
2. Abra outro app (Galeria, WhatsApp ou Drive) e encontre uma imagem ou PDF de comprovante.
3. Toque em **Compartilhar** e selecione o app **Gestão de Finanças**.
4. ✅ O app abre a tela de processamento com o loading.
5. ✅ Após alguns segundos, a tela de Cadastro de Dívida abre **pré-preenchida**.
6. Confira os dados e clique em "Salvar Dívida".

### Redesign (Início e Lançar)
1. Abra a Início: conferir cabeçalho, card de saldo (botão de ocultar), barra de orçamento, cards "Dívidas em aberto"/"Total já pago" e o card "Atenção este mês".
2. Tocar em **Lançar** na barra inferior → tela deve abrir com o novo visual e a aba Lançar marcada.
3. Digitar um valor (máscara R$ 0,00) e conferir o resumo de parcelas mudando.
4. Selecionar **Pix** ou **Cartão de Débito** → parcelas e vencimento devem desaparecer.
5. Selecionar **Cartão de Crédito** → aparecer a lista de cartões (ou o vazio com "Cadastrar agora").
6. Escolher **12x** e depois **Outro** (ex.: 18) → o chip deve mostrar "18x" e o resumo "18x de R$ X · parcelado".
7. Escolher categoria, preencher descrição e datas → salvar.
8. Abrir uma dívida parcelada existente e editar → conferir que o "i/N" foi preservado.
9. Voltar de Cartões/Categorias → a tela deve manter a seleção e mostrar a categoria nova.
10. Tocar em **"Preencher com comprovante"** → escolher imagem/PDF → conferir preenchimento e seleção automática de Pix.

### Redesign (Dívidas, Dashboard e Configurações)
1. **Dívidas:** conferir o card de totais (com ponto de milhar), a ordem da lista (não pagas primeiro, pagas por último e esmaecidas) e o bloco de data de cada card.
2. **Dívidas:** numa compra parcelada, conferir "Parcela X/N" e "Pagas: X de N" com a barra de progresso.
3. **Dívidas:** tocar em **Pagar** → o card passa a "Pago ✓" e os totais atualizam; tocar no ícone da lixeira → confirmação e soft delete; tocar no ícone de lápis → abre a edição com a dívida carregada.
4. **Dívidas:** apagar tudo (ou usar um usuário novo) → deve aparecer "Nenhuma dívida cadastrada ainda".
5. **Dashboard:** conferir que o texto das fatias sumiu e que a legenda mostra cor, nome, percentual e valor.
6. **Dashboard:** tocar numa linha da legenda → a lista filtra e a linha fica destacada; tocar de novo → volta para "Todos"; os chips devem acompanhar.
7. **Dashboard:** navegar com ← e → → o título muda, o badge "Atual" some só no mês atual sem mover o título, e o filtro volta para "Todos".
8. **Dashboard:** alternar 3M / 6M / 12M → total e média atualizam, os valores nos pontos somem no 12M e a curva não afunda abaixo de zero.
9. **Dashboard:** conferir o Balanço do mês; se o salário estiver zerado, deve aparecer o aviso para conferir em Configurações.
10. **Dashboard:** tocar em "Exportar detalhamento" → escolher CSV ou PDF.
11. **Configurações:** conferir as três seções, o nome/avatar do perfil e o subtítulo "Atual: R$ X" do salário.
12. **Configurações:** ligar/desligar Lembretes e Biometria (inclusive tocando na linha inteira) → a cor da chave deve acompanhar o estado; sem biometria no aparelho, a linha fica inativa com "Indisponível neste aparelho".
13. **Configurações:** editar o salário → o subtítulo e os Relatórios devem refletir o novo valor.
14. **Configurações:** testar Backup (exportar/restaurar), "Editar" do perfil (ao voltar, nome e avatar atualizam) e "Desconectar da conta".

---

## 🚧 Próximos passos

- [ ] Redesenhar **Cartões** (+ fragments `fragment_cartoes`, `fragment_chaves_pix`), **Categorias**, **Histórico**, **Editar Perfil**, **Login**, **Cadastro**, **Onboarding**, **Splash**, **ReceiptShare**
- [ ] Migrar todas as telas para `<include layout="@layout/layout_bottom_nav">` + `BottomNavHelper.configure(...)` (Início, Dívidas, Dashboard e Configurações ainda têm barra própria)
- [ ] Ligar os filtros "Todos devedores" e "Categorias" da tela Dívidas
- [ ] Verificar o salário zerado em Relatórios/Início (conferir `Usuario`, `UsuarioDao` e o onboarding)
- [ ] Remover "Limpar tudo (testes)" do diálogo de Backup antes de publicar
- [ ] Criar um tema escuro para os `AlertDialog`
- [ ] Unificar cores em `colors.xml` e textos em `strings.xml`
- [ ] Aplicar a fonte **Inter** (`themes.xml` com `inter_medium.xml`)
- [ ] Tirar o Room da main thread (ViewModel + LiveData ou Executors)
- [ ] Salvar a data da compra (novo campo em `Divida` + migração)
- [ ] Tratar arredondamento de centavos das parcelas
- [ ] Avaliar datas como timestamp
- [ ] Tornar o badge "Sincronizado" dinâmico
- [ ] Testes da Sprint 6 e 7 pelo Israel (CT-14 a CT-29)
- [ ] Testar notificações (CT-10 e CT-11)
- [ ] Autenticação em nuvem (Firebase) — opcional
- [ ] Sincronização entre dispositivos — opcional
- [ ] Exportação XLSX (além de CSV)
- [ ] Melhorias no fluxo de backup

---

## 🧾 Arquivos ainda não vistos (pedir antes de mexer)

- `AndroidManifest.xml` — `app/src/main/`
- `AppDatabase.java`, `DatabaseClient.java`, `Migrations.java` — `java/.../database/`
- Demais DAOs (`UsuarioDao`, `CartaoDao`, `ChavePixDao`, `CategoriaDao`, `TransacaoDao`, `ComprovanteProcessadoDao`) — `java/.../dao/` (o `DividaDao` já foi visto)
- `Usuario.java`, `ChavePix.java`, `ParsedReceipt.java`, `ComprovanteProcessado.java`, `CategoriaResumo.java`, `Transacao.java`, `BackupData.java` — `java/.../model/` (`Divida.java` já foi visto)
- `NotificationHelper`, `AlarmeHelper`, `CategoriaSeeder`, `BackupHelper`, `CsvExportHelper`, `PdfExportHelper`, `ReceiptOcr`, `ReceiptParser`, `ReceiptCacheHelper`, `ReceiptImageLoader` — `java/.../utils/` (`SessionManager` e `BiometricHelper` já foram vistos)
- Activities restantes (Cartões, Categorias, Histórico, Editar Perfil, Login, Cadastro, Onboarding, Splash etc.) — `java/.../view/`
- Adapters restantes — `java/.../adapter/`
- Layouts das telas restantes e `item_*` — `res/layout/`
- `themes.xml`, `strings.xml` — `res/values/` (o `colors.xml` e os `themes.xml` normal/night já foram vistos; sem customizações)
- `build.gradle` raiz e `settings.gradle` — raiz do projeto (o `build.gradle` do módulo `app` já foi visto)
- Conteúdo dos drawables da Lançar (`bg_lancar_*`, `ic_lancar_*`) e `layout_bottom_nav.xml` / `BottomNavHelper.java` — usados por referência nesta documentação

---

## 📚 Referências

- Autor das Sprints 5, 6 e 7: Gustavo Piteira
- Soft Delete: 06/10/2026
- Tela de Histórico: 06/10/2026
- Exportação CSV/PDF: 06/10/2026
- Parcelamento por Mês: 07/10/2026
- Dashboard Somente Leitura: 07/10/2026
- Biometria Real: 08/10/2026
- Gráfico de Linha: 10/10/2026
- Filtro de Período: 10/10/2026
- Backup Local: 10/10/2026
- Migration Não Destrutiva: 10/10/2026
- Agrupamento de Parcelas na Início: 10/10/2026
- Alerta Inteligente: 10/10/2026
- Leitura Automática de Comprovantes (OCR): 18/10/2026
- **Redesign Visual — Início e Lançar + barra inferior reutilizável: 25/10/2026**
- **Redesign Visual — Dívidas, Dashboard/Relatórios e Configurações: 27/10/2026**
