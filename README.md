# App Gestão de Finanças

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 6 (Histórico, Soft Delete, Exportação, Biometria, Gráfico de Linha, Backup e Migration) – Room + Autenticação + Categorias + Editar Perfil + Lembretes + Cartões + Chaves Pix + Histórico + CSV/PDF + Biometria + Backup Local** 🚀

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
- ✅ **Exportação CSV/PDF também no Dashboard**
- ✅ Card "Total de Dívidas" na tela Início abre o Histórico
- ✅ **Autenticação Biométrica (BiometricPrompt)**
- ✅ **Gráfico de Linha (Evolução Mensal) com seletor 3M/6M/12M**
- ✅ **Gráfico com valores, média mensal e destaque do maior gasto**
- ✅ **Filtro de Período Customizado no Histórico**
- ✅ **Backup Local (Exportar/Restaurar em JSON)**
- ✅ **Migration não destrutiva (v8 → v9)**
- ✅ **Agrupamento de parcelas na tela Início (Xbox 4x = 1 card)**
- ✅ **Alerta inteligente na tela Início (conta só o que vence no mês)**
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

> **Observação:** o MVP final será entregue no Sprint 7, com autenticação em nuvem e sincronização.

---

## Descrição
Aplicativo de gestão de finanças pessoais desenvolvido para a disciplina de **[Nome da Disciplina]** no curso de **[Nome do Curso]**.

O app permite ao usuário:
- Criar conta com nome, e-mail e senha (autenticação local)
- Fazer login com validação no banco de dados
- Manter a sessão ativa entre aberturas do app
- **Entrar com biometria (digital, face ou íris)**
- Visualizar o resumo de dívidas e vencimentos na tela de Início
- Acompanhar gastos por categoria com gráfico de rosca
- **Acompanhar a evolução mensal com gráfico de linha (3M/6M/12M)**
- Gerenciar dívidas (cadastrar, editar, pagar, excluir)
- Criar e personalizar categorias com cores próprias
- Editar perfil (nome, e-mail, senha) e excluir conta
- Receber notificações 3 dias antes do vencimento das faturas
- Receber notificações mesmo após reiniciar o celular (BootReceiver)
- Cadastrar, editar e excluir cartões de crédito/débito
- Cadastrar, editar e excluir chaves Pix de credores
- Navegar pelo histórico mensal de dívidas
- **Filtrar histórico por período customizado**
- Visualizar os 4 estados de cada dívida no histórico
- Exportar o extrato mensal em CSV (Excel/Sheets)
- Exportar o extrato mensal em PDF (relatório estilizado)
- **Fazer backup local em JSON (exportar/restaurar)**
- Manter seus dados isolados por conta

---

## 🖥️ Tecnologias e Ferramentas
- **Linguagem:** Java
- **IDE:** Android Studio Iguana (2023.2.1)
- **Versionamento:** Git + GitHub
- **Sistema Operacional:** Android (mínimo API 24 – Android 7.0)
- **Design:** Figma
- **Bibliotecas:** Room (SQLite), MPAndroidChart, Material Design Components, RecyclerView, CardView, ViewPager2, SharedPreferences, AlarmManager, iTextG (PDF), BiometricPrompt, Gson

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
Tela de abertura com logo, fontes personalizadas (Abril Fatface e Lato), timer de 2 segundos e verificação de sessão ativa.

### Tela de Login / Cadastro
Validação de campos, verificação no Room, e-mail com Regex, senhas coincidentes. **Suporta biometria quando ativada nas Configurações.**

### Tela de Início (Home)
Resumo de dívidas, total já pago, **alerta inteligente (conta só o que vence no mês)** e próximos vencimentos. **Parcelas do mesmo grupo são agrupadas em um único card.** O card "Total de Dívidas Acumuladas" é clicável e abre o Histórico.

### Dashboard (Relatórios)
- Gráfico de rosca com distribuição por categoria
- **Gráfico de linha com evolução mensal (3M/6M/12M)**
- **Valores em cima dos pontos, média mensal e destaque em roxo do maior gasto**
- **Navegação por mês (← Setembro 2026 →)**
- **Botão de exportação CSV/PDF integrado**
- Filtros em pílula por categoria
- Adapter somente leitura (sem botões de ação)

### Tela de Dívidas
Cards com título, valor da parcela e progresso. Botões: Excluir (soft delete), Editar, Pagar (parcela por parcela).

### Cadastro de Dívida
Formulário fiel ao Figma com tipo, banco, descrição, categoria (com bolinha colorida), valor, parcelas, datas. Pix e Débito são à vista. **Crédito parcelado gera N registros independentes (1 por mês).**

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
- **Switch de biometria**
- **Backup dos Dados (Exportar / Restaurar / Limpar Tudo)**
- Logout

### Bottom Navigation
Barra inferior: Início, Lançar, Dívidas, Relatórios e Config.

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
├── model/          → Entidades (@Entity) + BackupData + VencimentoItem
├── dao/            → DAOs (acesso ao banco)
├── database/       → AppDatabase (versão 9) + Migrations
├── utils/          → SessionManager, CategoriaSeeder, NotificationHelper,
│                     AlarmeHelper, CsvExportHelper, PdfExportHelper,
│                     BiometricHelper, BackupHelper
├── receiver/       → LembreteReceiver, BootReceiver
├── adapter/        → Adapters (dívidas, categorias, cartões, chips,
│                     histórico, divida dashboard, vencimento)
└── view/           → 15+ Activities (Splash, Login, Início, Histórico, etc)
```

**Novos na Sprint 6:**
- `utils/BiometricHelper.java`
- `utils/BackupHelper.java`
- `model/BackupData.java`
- `model/VencimentoItem.java`
- `database/Migrations.java`
- `adapter/DividaDashboardAdapter.java`
- `adapter/HistoricoAdapter.java`
- `view/HistoricoActivity.java`
- `res/drawable/bg_line_chart_gradient.xml`
- `res/xml/file_paths.xml`

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
- `fix: gráfico de evolução mensal agora mostra meses futuros centrados`
- `fix: capricha gráfico de evolução mensal com valores, média e destaque`
- `fix: corrige lógica de pagamento parcelado na tela de Dívidas`
- `fix: corrige bug .sho w() no HistoricoActivity`

---

## 🚧 Próximos Passos

- Autenticação em nuvem (Firebase Auth) – opcional para o MVP final
- Sincronização entre dispositivos – opcional para o MVP final
- Exportação XLSX (além de CSV)
- Simulador de pagamento com QR Code
- Melhorias no fluxo de backup (lista de backups internos)

---

## 📄 Documentação Adicional

- **[docs/TESTES.md](docs/TESTES.md)** – Plano de testes
- **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)** – Documentação técnica
- **[docs/BUGS.md](docs/BUGS.md)** – Registro de bugs
- **[CHANGELOG.md](CHANGELOG.md)** – Histórico de mudanças
- **[Protótipo no Figma](https://www.figma.com/design/BED5loI0pi57B5nkPPufij/ORG---TELA?node-id=71-1192&t=CZH8LT7ObgHAeZla-1)**

---

> **Observação:** Este README é atualizado ao final de cada Sprint.
