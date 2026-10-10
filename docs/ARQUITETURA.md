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

Foi uma **decisão de MVP** para simplificar o desenvolvimento inicial.

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

### Diferença: Dashboard vs Dívidas

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

Autenticação via **BiometricPrompt** (Android 9+). O usuário pode ativar nas Configurações e usar o botão **"👤 Usar biometria"** no Login.

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
- Callback com `onSucesso()` e `onFalha(motivo)`

#### 2. `utils/SessionManager.java`
- Chaves de biometria **sobrevivem ao logout**
- `isBiometriaAtiva()`, `setBiometriaAtiva()`
- `salvarUsuarioBiometrico()`, `limparBiometria()`
- `getBiometriaUserId()`, `getBiometriaNome()`, `getBiometriaEmail()`

#### 3. `view/LoginActivity.java`
- Botão `btnBiometria` (só aparece se ativo + disponível)
- Método `realizarLoginBiometrico()` — autentica e restaura sessão

#### 4. `view/ConfiguracoesActivity.java`
- Switch de biometria funcional
- Ao **ativar:** pede autenticação para confirmar
- Ao **desativar:** limpa os dados biométricos
- Se o aparelho não tem hardware → switch desabilitado

### Versões Suportadas

- **Android 9+ (API 28+)** → `BiometricPrompt` (recomendado)
- Requer permissão `USE_BIOMETRIC` no Manifest

---

## 📈 Gráfico de Linha — Evolução Mensal

### Data de implementação
10/10/2026

### Descrição

Novo gráfico de linha no Dashboard mostrando a **evolução dos gastos** nos últimos N meses.

### Componentes

#### 1. `layout/activity_dashboard.xml`
- Novo `CardView` com `LineChart`
- Cabeçalho "📈 EVOLUÇÃO MENSAL"
- Texto "Total: R$ X,XX em N meses"
- `HorizontalScrollView` com chips de período (3M / 6M / 12M)

#### 2. `view/DashboardActivity.java`
- Constante `PERIODOS_DISPONIVEIS = {3, 6, 12}`
- Variável `mesesVisiveis` (default 6)
- Método `configurarGraficoLinha()` — configura aparência escura
- Método `criarChipsPeriodo()` — monta os 3 chips
- Método `atualizarGraficoLinha()` — calcula totais dos N meses

### Lógica de Cálculo

Para cada mês dos N selecionados:
1. Percorre todas as dívidas do usuário
2. Filtra apenas as do mês atual (pelo `vencimento`)
3. Soma `valorTotal`
4. Adiciona ponto no gráfico

### Considerações

- Considera apenas dívidas **não excluídas** (pagas + não pagas)
- Base: mês selecionado na navegação do Dashboard
- Chips: 3M / 6M / 12M (configurável em `PERIODOS_DISPONIVEIS`)

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
- Método `aplicarFiltroPeriodo()` — ativa filtro
- Método `limparFiltroPeriodo()` — volta ao modo mês
- Método `estaEntreDatas()` — verifica se vencimento está no intervalo
- Método `compararDatas()` — valida data final ≥ inicial

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

### Exportação

- CSV/PDF usa o período no nome do arquivo quando filtro ativo

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
- Método `contarItens()`

#### 2. `utils/BackupHelper.java`
- `exportar(context, userId)` — gera arquivo JSON
- `compartilharBackup(context, arquivo)` — abre Intent de compartilhamento
- `lerArquivo(context, uri)` — lê e valida o JSON
- `aplicarBackup(context, userId, data)` — substitui dados

#### 3. DAOs atualizados
- `deletarTodosDoUsuario()` adicionado nos 4 DAOs
- Usado ao restaurar backup (apaga atual antes de inserir)

#### 4. `view/ConfiguracoesActivity.java`
- `ActivityResultLauncher` para selecionar arquivo
- `mostrarDialogoBackup()` — 3 opções: Exportar / Restaurar / Limpar Tudo
- `processarImportacao(uri)` — valida e mostra resumo
- `aplicarBackup(data)` — aplica e redireciona para Início

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
- ✅ Mostra resumo (X dívidas, Y cartões...) antes de confirmar
- ✅ Confirmação obrigatória antes de substituir dados
- ✅ Se arquivo inválido → Toast de erro, não toca no banco
- ✅ Reagenda alarmes das dívidas não pagas após importar

### Fluxo UX

**Exportar:**
1. Configurações → "Backup dos Dados"
2. Toca em **"📤 Exportar Backup (JSON)"**
3. Arquivo `backup_org_YYYY-MM-DD_HH-mm.json` é gerado
4. Menu de compartilhamento abre

**Restaurar:**
1. Configurações → "Backup dos Dados"
2. Toca em **"📥 Restaurar Backup"**
3. Seletor de arquivo abre
4. Escolhe o JSON
5. Diálogo mostra resumo + confirmação
6. Confirma → dados substituídos → redireciona para Início

---

## 🧪 Como testar o Parcelamento

1. Cadastre uma dívida: **Videogame R$ 3.000 em 4x**, 1º vencimento **em Novembro**
2. ✅ Deve criar **4 registros** no banco (1/4, 2/4, 3/4, 4/4)
3. Abra a tela **Dívidas** — deve mostrar as 4 parcelas
4. Abra o **Dashboard** e navegue até **Novembro** — Videogame 1/4
5. Pague a parcela 1/4 em **Dívidas** — botão vira "Pago"

---

## 🧪 Como testar a Biometria

1. Ative em **Configurações → Segurança & Biometria**
2. Faça logout
3. Na tela de Login, toque em **"👤 Usar biometria"**
4. Coloque a digital
5. ✅ Login direto para Início

---

## 🧪 Como testar o Gráfico de Linha

1. Abra o Dashboard (aba Relatórios)
2. ✅ Aparece o gráfico com 3 chips: `3M` `6M` `12M`
3. Toque em cada chip → ✅ gráfico se adapta
4. ✅ Total no topo muda conforme o período

---

## 🧪 Como testar o Filtro de Período

1. Abra o Histórico
2. Toque em **📅**
3. Escolha data inicial e final
4. ✅ Lista filtra pelo período
5. ✅ Título muda para o intervalo
6. Toque em **✕** → ✅ volta ao modo mês

---

## 🧪 Como testar o Backup

1. Configurações → Backup dos Dados → **📤 Exportar**
2. Compartilhe o arquivo via Drive
3. **Limpar Tudo** (para zerar)
4. Configurações → Backup → **📥 Restaurar**
5. Escolha o arquivo
6. ✅ Resumo aparece → confirme
7. ✅ Dados voltam ao Início

---

## 🚧 Próximos passos

- [ ] Testes da Sprint 6 pelo Israel (CT-14 a CT-21)
- [ ] Testar notificações (CT-10 e CT-11)
- [ ] Aplicar Migration real (não destrutiva)
- [ ] Autenticação em nuvem (Firebase) — opcional
- [ ] Sincronização entre dispositivos — opcional

---

## 📚 Referências

- Autor das Sprints 5 e 6: Gustavo Piteira
- Soft Delete: 06/10/2026
- Tela de Histórico: 06/10/2026
- Exportação CSV/PDF: 06/10/2026
- Parcelamento por Mês: 07/10/2026
- Dashboard Somente Leitura: 07/10/2026
- **Biometria Real: 08/10/2026**
- **Gráfico de Linha: 10/10/2026**
- **Filtro de Período: 10/10/2026**
- **Backup Local: 10/10/2026**
