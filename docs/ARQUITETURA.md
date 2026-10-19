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

---

## 🔔 Alerta Inteligente na Tela Início

### Data de implementação
10/10/2026

### Descrição
O alerta "Atenção Este Mês" agora conta **apenas dívidas que vencem no mês atual**.

### Solução
Novo método em `InicioActivity.atualizarTotais()` que verifica o mês atual e conta apenas as dívidas com vencimento nele.

---

## 🤖 Leitura Automática de Comprovantes (OCR) ⭐ NOVO

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
│ 2. ComprovanteActivity      │
│    recebe o Intent          │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│ 3. OcrHelper (ML Kit)       │
│    extrai o texto bruto     │
└─────────────┬───────────────┘
              │
              ▼
┌─────────────────────────────┐
│ 4. ComprovanteParser        │
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

#### 1. `utils/OcrHelper.java`
- Usa a biblioteca **Google ML Kit (Text Recognition)** para extrair texto de imagens.
- Funciona **offline** e sem custo.
- Método `extrairTexto(Uri, Callback)`.

#### 2. `utils/ComprovanteParser.java`
- Recebe o texto bruto e tenta extrair informações estruturadas usando Regex.
- Extrai: título, valor, data, banco.
- Trata diferentes formatos de comprovante.

#### 3. `view/ComprovanteActivity.java`
- Activity criada para receber o compartilhamento (`ACTION_SEND`).
- Exibe tela de carregamento ("Comprovante recebido. Processando...").
- Chama o `OcrHelper`, depois o `ComprovanteParser`, e por fim redireciona para o `CadastroDividaActivity` com os dados via `Intent`.
- Possui botão "Cancelar" para abortar o processo.

#### 4. `AndroidManifest.xml`
- Filtros de Intent na `ComprovanteActivity` para receber `image/*` e `application/pdf`.

### Limitações Conhecidas
- O parser pode não identificar 100% dos casos em comprovantes muito diferentes do padrão (ex: bancos menores).
- Se o OCR falhar, o usuário é redirecionado para o formulário em branco.

### Próximos Passos (Futuro)
- Treinar um modelo próprio via Orange/TensorFlow Lite para substituir o parser heurístico por IA.
- Adicionar suporte a mais formatos de PDF.

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

---

## 🚧 Próximos passos

- [ ] Testes da Sprint 6 e 7 pelo Israel (CT-14 a CT-29)
- [ ] Testar notificações (CT-10 e CT-11)
- [ ] Autenticação em nuvem (Firebase) — opcional
- [ ] Sincronização entre dispositivos — opcional
- [ ] Exportação XLSX (além de CSV)
- [ ] Melhorias no fluxo de backup

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
- **Leitura Automática de Comprovantes (OCR): 18/10/2026**
