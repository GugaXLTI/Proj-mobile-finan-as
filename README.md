# App Gestão de Finanças — ORG

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 7 (OCR) + Redesign Visual – Room + Autenticação + Categorias + Editar Perfil + Lembretes + Cartões + Chaves Pix + Histórico + CSV/PDF + Biometria + Backup + Migration + OCR + Novo Visual** 🚀

- ✅ Splash Screen (com verificação de sessão)
- ✅ Tela de Login (autenticação no Room)
- ✅ Tela de Cadastro (nome, e-mail, senha, confirmação)
- ✅ Tela de Início (Home com nome do usuário logado)
- ✅ Dashboard (Relatórios)
- ✅ Tela de Dívidas
- ✅ Tela de Cadastro de Dívida
- ✅ Tela de Configurações (com logout)
- ✅ Tela de Categorias (criar, listar e excluir)
- ✅ Tela de Editar Perfil (nome, e-mail, senha e exclusão de conta)
- ✅ Lembretes de Fatura (notificações 3 dias antes do vencimento)
- ✅ BootReceiver (reagenda alarmes após reiniciar o celular)
- ✅ Tela de Cartões & Chaves Pix (com abas e CRUD completo)
- ✅ Cadastro de Cartão com tipo Crédito/Débito
- ✅ Cadastro de Chave Pix com tipo (CPF, Celular, E-mail, Aleatória)
- ✅ Integração da tela Lançar Dívida com Cartões e Chaves Pix
- ✅ Tela de Histórico por Mês (navegação, filtros e 4 estados)
- ✅ Soft Delete de dívidas (excluídas ficam registradas no histórico)
- ✅ Exportação de extrato em CSV (Excel/Sheets)
- ✅ Exportação de extrato em PDF (relatório estilizado)
- ✅ Exportação CSV/PDF também no Dashboard
- ✅ Card "Total de Dívidas" na tela Início abre o Histórico
- ✅ Autenticação Biométrica (BiometricPrompt)
- ✅ Gráfico de Linha (Evolução Mensal) com seletor 3M/6M/12M
- ✅ Gráfico com valores, média mensal e destaque do maior gasto
- ✅ Filtro de Período Customizado no Histórico
- ✅ Backup Local (Exportar/Restaurar em JSON)
- ✅ Migration não destrutiva (v8 → v9)
- ✅ Agrupamento de parcelas na tela Início (Xbox 4x = 1 card)
- ✅ Alerta inteligente na tela Início (conta só o que vence no mês)
- ✅ **Leitura Automática de Comprovantes (OCR) com Google ML Kit**
- ✅ **Recebimento de compartilhamento externo (imagem/PDF)**
- ✅ **Pré-preenchimento automático do Cadastro de Dívida**
- ✅ **Redesign visual da tela Início (dark mode fixo, cards arredondados, barra flutuante)**
- ✅ **Redesign visual da tela Lançar / Cadastro de Dívida (chips, cards de seleção, resumo de parcelas)**
- ✅ **Barra inferior reutilizável (`layout_bottom_nav` + `BottomNavHelper`)**
- ✅ Bottom Navigation funcional
- ✅ Persistência de dados com Room (SQLite)
- ✅ Sessão persistente com SharedPreferences
- ✅ CRUD completo de dívidas no banco
- ✅ Isolamento por usuário (cada conta vê apenas seus dados)
- ✅ Máscara de valor automática (R$ 0,00)
- ✅ Categorias dinâmicas no cadastro de dívida
- ✅ Cálculo automático do valor das parcelas
- ✅ Validação de e-mail com Regex
- ✅ Pix e Cartão de Débito tratados como à vista
- ✅ Cards de dívidas exibem valor da parcela e progresso
- ✅ Bloqueio de exclusão de categorias em uso
- ✅ Plano de Testes documentado (docs/TESTES.md)

> **Observação:** o MVP final será entregue no Sprint 8 (se houver), com autenticação em nuvem e sincronização. O redesign visual das telas restantes (Dívidas, Dashboard, Configurações, Cartões, Categorias, Histórico etc.) está em andamento.

---

## Descrição
Aplicativo de gestão de finanças pessoais desenvolvido para a disciplina de **[Nome da Disciplina]** no curso de **[Nome do Curso]**.

O app permite ao usuário:
- Criar conta com nome, e-mail e senha (autenticação local)
- Fazer login com validação no banco de dados
- Manter a sessão ativa entre aberturas do app
- Entrar com biometria (digital, face ou íris)
- **Lançar dívidas automaticamente a partir do compartilhamento de um comprovante (OCR)**
- Visualizar o resumo de dívidas e vencimentos na tela de Início
- Acompanhar gastos por categoria com gráfico de rosca
- Acompanhar a evolução mensal com gráfico de linha (3M/6M/12M)
- Gerenciar dívidas (cadastrar, editar, pagar, excluir)
- Criar e personalizar categorias com cores próprias
- Editar perfil (nome, e-mail, senha) e excluir conta
- Receber notificações 3 dias antes do vencimento das faturas
- Receber notificações mesmo após reiniciar o celular (BootReceiver)
- Cadastrar, editar e excluir cartões de crédito/débito
- Cadastrar, editar e excluir chaves Pix de credores
- Navegar pelo histórico mensal de dívidas
- Filtrar histórico por período customizado
- Visualizar os 4 estados de cada dívida no histórico
- Exportar o extrato mensal em CSV (Excel/Sheets)
- Exportar o extrato mensal em PDF (relatório estilizado)
- Fazer backup local em JSON (exportar/restaurar)
- Manter seus dados isolados por conta

---

## 🖥️ Tecnologias e Ferramentas
- **Linguagem:** Java
- **IDE:** Android Studio Iguana (2023.2.1)
- **Versionamento:** Git + GitHub
- **Sistema Operacional:** Android (mínimo API 24 – Android 7.0)
- **Design:** Figma
- **Bibliotecas:** Room (SQLite), MPAndroidChart, Material Design Components, RecyclerView, CardView, ViewPager2, SharedPreferences, AlarmManager, iTextG (PDF), BiometricPrompt, Gson, **Google ML Kit (OCR)**

---

## 👥 Equipe
| Nome | Função |
|------|--------|
| **Gustavo Piteira** | Líder do Projeto / Desenvolvedor Back-End |
| **Israel Malheiros** | Desenvolvedor Front-End / Design Figma / UX & UI |
| **Gustavo Marques** | Desenvolvedor (XML e Interface) |
| **Francisco Andrade** | Desenvolvedor (Lógica e Integração) |

---

## 📱 Telas do App

### Splash Screen
Tela de abertura com logo, fontes personalizadas (Abril Fatface e Inter), timer de 2 segundos e verificação de sessão ativa.

### Tela de Login / Cadastro
Validação de campos, verificação no Room, e-mail com Regex, senhas coincidentes. Suporta biometria quando ativada nas Configurações.

### Tela de Início (Home)
Redesenhada com o novo padrão visual: fundo azul-marinho com brilho verde/azul, cabeçalho com avatar e badge "Sincronizado", card de saldo disponível (com botão de ocultar valores), barra de orçamento, cards "Dívidas em aberto" e "Total já pago", card "Atenção este mês" (contas que vencem no mês, "Vence em N dias", total a pagar, % do salário) e barra inferior flutuante. Parcelas do mesmo grupo são agrupadas em um único card. O card "Total de Dívidas Acumuladas" é clicável e abre o Histórico.

### Dashboard (Relatórios)
- Gráfico de rosca com distribuição por categoria
- Gráfico de linha com evolução mensal (3M/6M/12M)
- Valores em cima dos pontos, média mensal e destaque em roxo do maior gasto
- Navegação por mês (← Setembro 2026 →)
- Botão de exportação CSV/PDF integrado
- Filtros em pílula por categoria
- Adapter somente leitura (sem botões de ação)

### Tela de Dívidas
Cards com título, valor da parcela e progresso. Botões: Excluir (soft delete), Editar, Pagar (parcela por parcela).

### Cadastro de Dívida
Redesenhada no novo padrão visual. Botão voltar redondo, título "Lançar nova dívida", valor total em card grande com chips de parcelas (1x, 2x, 3x, 6x, 12x, Outro) e resumo dinâmico ("Nx de R$ X · à vista/parcelado"). Tipo de dívida e Categoria viram chips (categoria com bolinha colorida). Seleção de cartão / chave Pix / banco em lista horizontal de cards com card tracejado "Adicionar". Campos de data com ícone de calendário e botão "Preencher com comprovante" tracejado. Pix e Débito são à vista; Crédito parcelado gera N registros independentes (1 por mês). Aceita pré-preenchimento automático via OCR. Barra inferior reutilizável via `<include>` + `BottomNavHelper`.

### Tela de Processamento OCR (ReceiptShareActivity)
Tela que recebe o comprovante compartilhado de outros apps, extrai o texto via ML Kit (`ReceiptOcr`), interpreta os dados (`ReceiptParser`) e redireciona para o Cadastro de Dívida já preenchido. Possui tela de loading.

### Tela de Histórico
Navegação por mês, badge "Atual", card de resumo (Total, Já Pago, Falta Pagar), chips de filtro por categoria e lista de lançamentos com 4 estados:
- ✅ Liquidado
- ⏳ Pendente
- ✗ Excluída

**Filtro de Período:** botão 📅 permite escolher um intervalo customizado (data inicial + final). Botão ✕ limpa o filtro.

**Exportação:** botão "Baixar Resumo" abre diálogo para escolher CSV ou PDF.

### Tela de Categorias
8 categorias padrão, criação com cor personalizada, contagem de dívidas, bloqueio de exclusão em uso.

### Tela de Cartões & Chaves Pix
Abas com CRUD completo. Clique normal = editar. Clique longo = excluir. Botão "Copiar Chave" para Pix.

### Editar Perfil
Alteração de nome, e-mail, senha (com senha atual) e exclusão de conta.

### Lembretes de Fatura
Notificações 3 dias antes do vencimento, às 9h. Funciona offline com AlarmManager. BootReceiver reagenda após reinicialização.

### Configurações
- Perfil dinâmico
- Gerenciamento de cartões/Pix
- Categorias
- Switch de lembretes
- Switch de biometria
- Backup dos Dados (Exportar / Restaurar / Limpar Tudo)
- Logout

### Bottom Navigation
Barra inferior: Início, Lançar, Dívidas, Relatórios e Config. A partir do redesign, foi extraída para um layout reutilizável (`layout_bottom_nav.xml`) com um helper (`BottomNavHelper`) que marca a aba ativa e faz a navegação. A tela Lançar já usa o `<include>`; a Início ainda mantém a barra própria e as demais telas serão migradas.

---

## 🎨 Design System (Redesign Visual)

- **Fundo de tela:** `@drawable/bg_gradient_inicio` (base #0A101C + brilho verde #4010B981 no topo-esquerdo + azul #382563EB no topo-direito).
- **Card padrão:** `@drawable/bg_card_glass` (#111A2E, borda #1E2A44, raio 24dp).
- **Card de atenção:** `@drawable/bg_card_atencao` (#15171E, borda #5C4310, raio 24dp).
- **Campo de formulário:** `@drawable/bg_lancar_campo` (#111A2E, borda #1E2A44, raio 16dp, altura 52dp).
- **Cores de destaque:** verde #10B981 / #34D399; vermelho #EF4444 / #F87171; âmbar #F59E0B (alerta).
- **Texto:** primário #FFFFFF; secundário #94A3B8; apagado #64748B; hint #475569.
- **Botão primário:** MaterialButton, `backgroundTint` #10B981, texto preto bold 16sp, altura 56dp, cornerRadius 18dp.
- **Botão secundário tracejado:** `bg_lancar_comprovante`.
- **Chip:** altura 44dp, raio 24dp, texto 14sp (fundo #111A2E → #123D33; borda #1E2A44 → #10B981; texto #E2E8F0 → #34D399 quando selecionado).
- **Barra inferior:** pílula `bg_nav_pill` (#F2101A2B, borda #22304A, raio 32dp), 64dp, margens 20dp laterais e 16dp embaixo, elevation 8dp; aba ativa = ícone + texto #34D399 em `bg_tab_ativo` (#123D33); inativas = só ícone #94A3B8.
- **Ícones:** vetores 24dp, stroke 1.75, cor via `app:tint`.
- **Fonte:** Roboto (padrão). A fonte Inter já está no projeto (`res/font/inter_medium.xml`) e será aplicada no tema em breve. `abril_fatface` permanece em telas antigas.

---

## 🔧 Como Clonar e Executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/GugaXLTI/Proj-mobile-finan-as.git
   ```

2. **Abra no Android Studio** (Iguana 2023.2.1 ou superior).

3. **Aguarde o Gradle sincronizar**.

4. **Execute o app** em emulador ou dispositivo físico (API 24+).

5. **Para atualizar:**
   ```bash
   git pull origin master
   ```

---

## 📂 Estrutura do Projeto (resumida)

```
app/src/main/java/com/example/controle_gastos/
├── adapter/        → Adapters (cartão, categoria, chave pix, chip, dívida, dashboard, histórico, lançamento, legenda, vencimento)
├── dao/            → DAOs (CartaoDao, CategoriaDao, ChavePixDao, ComprovanteProcessadoDao, DividaDao, TransacaoDao, UsuarioDao)
├── database/       → AppDatabase, DatabaseClient, Migrations
├── model/          → Entidades (@Entity) + BackupData, CategoriaResumo, ComprovanteProcessado, ParsedReceipt, VencimentoItem
├── receiver/       → BootReceiver, LembreteReceiver
├── utils/          → AlarmeHelper, BackupHelper, BiometricHelper, BottomNavHelper, CategoriaSeeder,
│                     CsvExportHelper, NotificationHelper, PdfExportHelper, ReceiptCacheHelper,
│                     ReceiptImageLoader, ReceiptOcr, ReceiptParser, SessionManager
└── view/           → Activities e Fragments (Splash, Login, Início, Histórico, ReceiptShare, etc)
```

**Novos na Sprint 6/7 + Redesign Visual:**
- `utils/BiometricHelper.java`, `utils/BackupHelper.java`, `utils/BottomNavHelper.java`, `utils/CsvExportHelper.java`, `utils/PdfExportHelper.java`, `utils/ReceiptOcr.java`, `utils/ReceiptParser.java`, `utils/ReceiptCacheHelper.java`, `utils/ReceiptImageLoader.java`
- `model/BackupData.java`, `model/VencimentoItem.java`, `model/ParsedReceipt.java`, `model/ComprovanteProcessado.java`, `model/CategoriaResumo.java`
- `database/Migrations.java`, `database/DatabaseClient.java`
- `dao/ComprovanteProcessadoDao.java`
- `adapter/DividaDashboardAdapter.java`, `adapter/HistoricoAdapter.java`, `adapter/LancamentoAdapter.java`, `adapter/LegendaAdapter.java`
- `view/HistoricoActivity.java`, `view/ReceiptShareActivity.java`
- `res/drawable/bg_line_chart_gradient.xml`, `res/xml/file_paths.xml`
- `res/layout/layout_bottom_nav.xml` (barra inferior reutilizável via `<include>`)
- `res/layout/item_conta_atencao.xml` (card "Atenção este mês" da Início)
- `res/drawable/` (diversos novos: `bg_lancar_campo`, `bg_card_glass`, `bg_nav_pill`, `ic_nav_*`, `ic_lancar_*`, etc.)
- Reescritos: `activity_inicio.xml`, `InicioActivity.java`, `activity_cadastro_divida.xml`, `CadastroDividaActivity.java`

---

## 📌 Histórico de Commits (resumo)

- `feat: implementa soft delete e cria tela de Histórico por Mês`
- `feat: implementa exportação do histórico em CSV e PDF`
- `feat: implementa autenticação biométrica com BiometricPrompt`
- `feat: adiciona gráfico de linha com seletor de período no Dashboard`
- `feat: adiciona filtro de período customizado no Histórico`
- `feat: implementa backup local com exportação e importação em JSON`
- `feat: adiciona migration não destrutiva para o banco de dados`
- `feat: agrupa parcelas por grupoId e melhora alerta mensal na tela Início`
- `feat: implementa exportação CSV e PDF no Dashboard`
- `feat: implementa leitura automática de comprovantes via OCR (ML Kit)`
- `feat: redesenha tela Início com novo padrão visual (dark mode, cards, barra flutuante)`
- `feat: redesenha tela Lançar com chips, cards de seleção e resumo de parcelas`
- `feat: cria barra inferior reutilizável (layout_bottom_nav + BottomNavHelper)`
- `fix: edição de parcela preserva o identificador i/N`
- `fix: recarrega categorias e cartões ao voltar de Cartões/Categorias`
- `fix: DatePicker abre na data já digitada`
- `fix: gráfico de evolução mensal agora mostra meses futuros centrados`
- `fix: capricha gráfico de evolução mensal com valores, média e destaque`
- `fix: corrige lógica de pagamento parcelado na tela de Dívidas`
- `fix: corrige bug .show() no HistoricoActivity`

---

## 🚧 Próximos Passos

- **Redesenhar as telas restantes no novo padrão visual** (Dívidas, Dashboard/Relatórios, Configurações, Cartões + Chaves Pix, Categorias, Histórico, Editar Perfil, Login, Cadastro, Onboarding, Splash, Compartilhar comprovante)
- **Migrar todas as telas para o `<include layout="@layout/layout_bottom_nav">` + `BottomNavHelper.configure(...)`** (a Início ainda usa barra própria; a Lançar já migrou)
- **Unificar cores em `colors.xml` e textos em `strings.xml`** (hoje duas paletas convivem e há textos hardcoded)
- **Aplicar a fonte Inter no tema** (`themes.xml` usando `inter_medium.xml` que já está em `res/font/`)
- **Tirar o Room da main thread** (ViewModel + LiveData ou Executors)
- **Persistir a data da compra** (exige novo campo em `Divida` + migração do Room)
- **Tratar o arredondamento de centavos das parcelas** (`valor / N`)
- **Avaliar guardar datas como timestamp em vez de String `dd/MM/yyyy`**
- **Tornar o badge "Sincronizado" da Início dinâmico** (hoje é estático)
- Autenticação em nuvem (Firebase Auth) – opcional para o MVP final
- Sincronização entre dispositivos – opcional para o MVP final
- Exportação XLSX (além de CSV)
- Simulador de pagamento com QR Code
- Melhorias no fluxo de backup (lista de backups internos)
- Melhorar o parser do OCR para suportar mais layouts de bancos

---

## 📄 Documentação Adicional

- **[docs/TESTES.md](docs/TESTES.md)** – Plano de testes
- **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)** – Documentação técnica
- **[docs/BUGS.md](docs/BUGS.md)** – Registro de bugs
- **[CHANGELOG.md](CHANGELOG.md)** – Histórico de mudanças
- **[Protótipo no Figma](https://www.figma.com/design/BED5loI0pi57B5nkPPufij/ORG---TELA?node-id=71-1192&t=CZH8LT7ObgHAeZla-1)**

---

> **Observação:** Este README é atualizado ao final de cada Sprint.
