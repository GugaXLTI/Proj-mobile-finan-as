# App Gestão de Finanças

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 4 (Persistência de Dados) – Room + Autenticação implementados** 🚀

- ✅ Splash Screen (com verificação de sessão e seed do banco)
- ✅ Tela de Login (autenticação no Room)
- ✅ Tela de Cadastro (nome, e-mail, senha, confirmação)
- ✅ Tela de Início (Home com nome do usuário logado)
- ✅ Dashboard (Relatórios)
- ✅ Tela de Dívidas
- ✅ Tela de Cadastro de Dívida
- ✅ Tela de Configurações (com logout)
- ✅ Bottom Navigation funcional
- ✅ Persistência de dados com Room (SQLite)
- ✅ Sessão persistente com SharedPreferences
- ✅ CRUD completo de dívidas no banco
- ✅ Validação de e-mail com Regex
- ✅ Validação de nome e senha
- ✅ Exibição dinâmica do usuário logado

> **Observação:** o MVP final será entregue no Sprint 6, com autenticação em nuvem e sincronização.

---

## Descrição
Aplicativo de gestão de finanças pessoais desenvolvido para a disciplina de **[Nome da Disciplina]** no curso de **[Nome do Curso]**.

O app permite ao usuário:
- Criar conta com nome, e-mail e senha (autenticação local)
- Fazer login com validação no banco de dados
- Manter a sessão ativa entre aberturas do app
- Visualizar o resumo de dívidas e vencimentos na tela de Início
- Acompanhar gastos por categoria com gráfico de rosca
- Gerenciar dívidas (cadastrar, pagar, excluir)
- Configurar preferências do sistema (biometria, lembretes, backup)

---

## 🖥️ Tecnologias e Ferramentas
- **Linguagem:** Java
- **IDE:** Android Studio Iguana (2023.2.1)
- **Versionamento:** Git + GitHub
- **Sistema Operacional:** Android (mínimo API 24 – Android 7.0)
- **Design:** Figma (protótipos desenvolvidos pela equipe)
- **Bibliotecas:** Room (SQLite), MPAndroidChart, Material Design Components, RecyclerView, CardView, SharedPreferences

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
Tela de abertura com logo, fontes personalizadas (Abril Fatface e Lato), timer de 2 segundos, população inicial do banco (DatabaseSeeder) e verificação de sessão ativa.

### Tela de Login
Formulário com campos de e-mail e senha, validação de campos obrigatórios, verificação de credenciais no Room, link para cadastro e navegação para a tela de Início.

### Tela de Cadastro
Formulário com campos de **nome**, e-mail, senha e confirmação de senha. Validações:
- Campos obrigatórios
- Nome com no mínimo 2 caracteres e apenas letras
- E-mail com formato válido (Regex: `nome@dominio.com`)
- Senha com no mínimo 6 caracteres
- Senhas coincidentes
- Verificação de e-mail duplicado no banco

### Tela de Início (Home)
Resumo de dívidas acumuladas, total já pago, alerta de faturas do mês e lista dos próximos vencimentos lidos do Room. Exibe o **nome e avatar do usuário logado** no topo.

### Dashboard (Relatórios)
Gráfico de rosca (MPAndroidChart) com distribuição de dívidas por categoria, cores personalizadas do Figma, percentuais dentro das fatias, total no centro, filtros em formato de pílula e lista de dívidas não pagas – tudo lido do banco de dados.

### Tela de Dívidas
Cards com título, valor restante, banco/categoria, parcela e vencimento. Resumo com totais (a pagar, pago, geral) e botões de ação (Excluir, Editar, Pagar) que operam diretamente no banco Room.

### Cadastro de Dívida
Formulário com tipo de dívida, banco, devedor, descrição, categoria, valor, parcelas, data da compra e 1º vencimento. Salva a nova dívida diretamente no Room.

### Configurações
Tela com perfil do usuário (nome e avatar dinâmicos), gerenciamento de cartões e categorias, lembretes de fatura, biometria, backup de dados e desconexão de sessão (logout limpa a sessão e redireciona para o Login).

### Bottom Navigation
Barra inferior com abas: Início, Lançar, Dívidas, Relatórios e Config – com navegação funcional entre as telas. A aba "Lançar" abre o cadastro de dívida.

---

## 🔧 Como Clonar e Executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/GugaXLTI/Proj-mobile-finan-as.git
   ```

2. **Abra o projeto no Android Studio** (versão Iguana 2023.2.1 ou superior).

3. **Aguarde o Gradle sincronizar** e baixar as dependências (incluindo Room).

4. **Execute o app** em um emulador ou dispositivo físico (API 24+).

5. **Para atualizar o código:**
   ```bash
   git pull origin master
   ```

---

## 📂 Estrutura do Projeto (resumida)

```
app/src/main/java/com/example/controle_gastos/
├── model/
│   ├── Usuario.java            # Entidade de usuário (@Entity)
│   ├── Divida.java             # Entidade de dívida (@Entity)
│   ├── Transacao.java          # Entidade de transação (@Entity)
│   └── CategoriaResumo.java    # Resumo por categoria
├── dao/
│   ├── UsuarioDao.java         # DAO de usuário (insert, login, buscarPorEmail)
│   ├── DividaDao.java          # DAO de dívidas (CRUD completo)
│   └── TransacaoDao.java       # DAO de transações (CRUD completo)
├── database/
│   ├── AppDatabase.java        # Classe principal do Room
│   ├── DatabaseClient.java     # Singleton de acesso ao banco
│   └── DatabaseSeeder.java     # Popula o banco na primeira execução
├── utils/
│   ├── DadosMock.java          # Dados de exemplo (seed)
│   └── SessionManager.java     # Gerenciamento de sessão (SharedPreferences)
├── adapter/
│   ├── LancamentoAdapter.java  # Adapter de lançamentos
│   ├── LegendaAdapter.java     # Adapter de legendas
│   ├── DividaAdapter.java      # Adapter de dívidas
│   └── VencimentoAdapter.java  # Adapter de vencimentos (tela Início)
└── view/
    ├── SplashActivity.java     # Tela de abertura (2s + verificação de sessão)
    ├── LoginActivity.java      # Tela de Login (autenticação no Room)
    ├── CadastroActivity.java   # Tela de Cadastro (validações)
    ├── InicioActivity.java     # Tela de Início (Home com usuário logado)
    ├── DashboardActivity.java  # Dashboard/Relatórios
    ├── DividasActivity.java    # Tela de Dívidas
    ├── CadastroDividaActivity.java  # Cadastro de Dívida
    └── ConfiguracoesActivity.java   # Configurações + Logout
```

---

## 📌 Histórico de Commits (resumo)

- `feat: estrutura inicial do app com Java e classe Transacao`
- `feat: adiciona DadosMock com transações iniciais`
- `feat: adiciona Splash Screen com timer e estilização`
- `style: aplica fontes Abril Fatface e Lato`
- `feat: finaliza tela de Login com logo e estilização`
- `feat: implementa tela de Cadastro com validações`
- `feat: adiciona Dashboard com gráfico e filtros`
- `feat: implementa tela de Dívidas com cards e totais`
- `feat: adiciona tela de Cadastro de Dívida`
- `feat: implementa tela de Configurações e navegação`
- `feat: implementa tela Início como nova home e ajusta navegação`
- `feat: padroniza gráfico de dívidas e conecta aba Lançar ao cadastro`
- `feat: implementa Room e autenticação local`
- `feat: integra telas de dívidas com Room`
- `feat: adiciona campo nome, validações e exibe usuário logado nas telas`

---

## 🚧 Próximos Passos

- Edição de dívidas (tela de editar funcional)
- Tela de gerenciamento de cartões e bancos
- Tela de gerenciamento de categorias
- Exportação de dados (PDF/CSV)
- Notificações de vencimento (AlarmManager ou WorkManager)
- Simulador de pagamento com QR Code (para depois do MVP)
- Autenticação em nuvem (Firebase Auth) – opcional para o MVP final
- Sincronização entre dispositivos – opcional para o MVP final

---

> **Observação:** Este README será atualizado ao final de cada Sprint com novos prints, funcionalidades e instruções.
