# App Gestão de Finanças

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 5 (Cartões, Chaves Pix, Integração, BootReceiver e CRUD Completo) – Room + Autenticação + Categorias + Editar Perfil + Lembretes + Cartões + Chaves Pix** 🚀

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
- ✅ **BootReceiver (reagenda alarmes após reiniciar o celular)**
- ✅ **Tela de Cartões & Chaves Pix (com abas e CRUD completo)**
- ✅ **Cadastro de Cartão com tipo Crédito/Débito**
- ✅ **Cadastro de Chave Pix com tipo (CPF, Celular, E-mail, Aleatória)**
- ✅ **Editar e excluir cartões e chaves Pix (clique normal = editar, clique longo = excluir)**
- ✅ **Integração da tela Lançar Dívida com Cartões e Chaves Pix**
- ✅ **Filtro dinâmico: cartões de crédito, débito ou chaves Pix conforme tipo de dívida**
- ✅ **Empty State com botão "Cadastrar agora" quando não há itens**
- ✅ Bottom Navigation funcional
- ✅ Persistência de dados com Room (SQLite)
- ✅ Sessão persistente com SharedPreferences
- ✅ CRUD completo de dívidas no banco
- ✅ Isolamento por usuário (cada conta vê apenas seus dados)
- ✅ Editar dívida funcional
- ✅ Confirmação ao excluir dívida
- ✅ Máscara de valor automática (R$ 0,00)
- ✅ Categorias dinâmicas no cadastro de dívida
- ✅ Cálculo automático do valor das parcelas
- ✅ Validação de e-mail com Regex
- ✅ Validação de nome e senha
- ✅ Exibição dinâmica do usuário logado
- ✅ **Plano de Testes documentado (docs/TESTES.md)**

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
- Gerenciar dívidas (cadastrar, editar, pagar, excluir)
- Criar e personalizar categorias com cores próprias
- Editar perfil (nome, e-mail, senha) e excluir conta
- Receber notificações 3 dias antes do vencimento das faturas
- **Receber notificações mesmo após reiniciar o celular (BootReceiver)**
- **Cadastrar, editar e excluir cartões de crédito/débito**
- **Cadastrar, editar e excluir chaves Pix de credores**
- **Copiar chave Pix com um toque (área de transferência)**
- **Selecionar o cartão ou chave Pix ao lançar uma nova dívida**
- Manter seus dados isolados por conta (cada usuário vê apenas seus dados)
- Configurar preferências do sistema (biometria, lembretes, backup)

---

## 🖥️ Tecnologias e Ferramentas
- **Linguagem:** Java
- **IDE:** Android Studio Iguana (2023.2.1)
- **Versionamento:** Git + GitHub
- **Sistema Operacional:** Android (mínimo API 24 – Android 7.0)
- **Design:** Figma (protótipos desenvolvidos pela equipe)
- **Bibliotecas:** Room (SQLite), MPAndroidChart, Material Design Components, RecyclerView, CardView, ViewPager2, SharedPreferences, AlarmManager

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
Tela de abertura com logo, fontes personalizadas (Abril Fatface e Lato), timer de 2 segundos e verificação de sessão ativa. Redireciona o usuário logado para a tela de Início, ou o novo usuário para o Login.

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
Resumo de dívidas acumuladas, total já pago, alerta de faturas do mês e lista dos próximos vencimentos lidos do Room. Exibe o **nome e avatar do usuário logado** no topo. Solicita **permissão de notificação** no Android 13+ e cria o canal de notificação na primeira execução.

### Dashboard (Relatórios)
Gráfico de rosca (MPAndroidChart) com distribuição de dívidas por categoria, cores personalizadas do Figma, percentuais dentro das fatias, total no centro, filtros em formato de pílula e lista de dívidas não pagas – tudo lido do banco e filtrado pelo usuário logado.

### Tela de Dívidas
Cards com título, **valor da parcela** (calculado automaticamente), banco/categoria, número da parcela e vencimento. Resumo com totais (a pagar, pago, geral) e botões de ação:
- **Excluir** – com diálogo de confirmação + cancelamento do alarme
- **Editar** – abre a tela de cadastro em modo edição
- **Pagar** – marca a dívida como paga, cancela o alarme e atualiza os totais

### Cadastro de Dívida
Formulário redesenhado fiel ao Figma com:
- **Tipo de dívida** (Cartão de Crédito, Cartão de Débito, Pix, Empréstimo, Fatura, Boleto, Outros)
- **Seleção visual em chips** dos cartões ou chaves Pix cadastrados (filtrados pelo tipo escolhido)
- **Empty State** com mensagem e botão "Cadastrar agora" quando não há itens
- Botão de atalho **"+ Novo"** e link **"+ Nova Categoria"**
- Campos **Valor/Parcelas** e **Data da Compra/1º Vencimento** lado a lado
- **Cálculo automático do valor de cada parcela**
- **Agendamento de notificação** 3 dias antes do vencimento

### Tela de Categorias
- Exibe **8 categorias padrão** automaticamente (Alimentação, Transporte, Saúde, Educação, Lazer, Moradia, Assinaturas, Outros)
- Usuário pode **criar** novas categorias com nome e **cor personalizada** (8 cores disponíveis)
- **Prévia da tag** atualiza em tempo real enquanto digita
- **Contagem de dívidas** por categoria
- **Excluir** categoria com diálogo de confirmação (avisa se há dívidas usando)

### Tela de Cartões & Chaves Pix ⭐ NOVO
Tela com navegação por abas (TabLayout + ViewPager2):

**Aba "Cartão Crédito/Débito":**
- Cadastro de cartão com **tipo** (Crédito/Débito), instituição, apelido, últimos 4 dígitos, dia de vencimento
- Lista de cartões em formato de cartão de crédito (com bandeira, número mascarado, titular e vencimento)
- Sigla da bandeira gerada automaticamente (ex: Nubank → NU)
- **Clique normal** = entra em modo edição (botão muda para "Atualizar Cartão")
- **Clique longo** = excluir com diálogo de confirmação
- Botão **"Cancelar edição"** aparece somente no modo edição

**Aba "Chave Pix de Dívida":**
- Cadastro de chave Pix com **tipo** (CNPJ/CPF, Celular, E-mail, Chave Aleatória), chave, nome do favorecido
- Lista de chaves com fundo verde (degradê) e badge "Pix Direto"
- Botão **"Copiar Chave"** que envia para a área de transferência
- **Clique normal** = entra em modo edição
- **Clique longo** = excluir com diálogo de confirmação

### Editar Perfil
- **Alterar nome completo** (validação: mínimo 2 letras, apenas letras)
- **Alterar e-mail** (validação Regex + verificação de duplicado)
- **Alterar senha** (opcional, requer senha atual + nova senha + confirmação)
- **Eliminar conta** e apagar todos os dados (com confirmação)

### Lembretes de Fatura
- **Notificações 3 dias antes do vencimento** de cada dívida, às 9h da manhã
- Solicita **permissão** automaticamente no Android 13+
- Alarme é **cancelado automaticamente** quando a dívida é paga ou excluída
- **Switch em Configurações** permite ativar/desativar todos os lembretes
- Funciona **offline** (usa AlarmManager local)
- **BootReceiver** reagenda automaticamente os alarmes após reiniciar o celular

### Configurações
Tela com perfil do usuário (nome e avatar dinâmicos), gerenciamento de **cartões e chaves Pix**, categorias, **switch funcional de lembretes**, biometria, limpeza de dados (para testes) e desconexão de sessão.

### Bottom Navigation
Barra inferior com abas: Início, Lançar, Dívidas, Relatórios e Config – com navegação funcional entre as telas.

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
│   ├── Usuario.java              # Entidade de usuário (@Entity)
│   ├── Divida.java               # Entidade de dívida (@Entity, com usuarioId e valorParcela)
│   ├── Transacao.java            # Entidade de transação (@Entity, com usuarioId)
│   ├── Categoria.java            # Entidade de categoria (@Entity, com usuarioId e cor)
│   ├── Cartao.java               # Entidade de cartão (@Entity, com tipo Crédito/Débito)
│   ├── ChavePix.java             # Entidade de chave Pix (@Entity)
│   └── CategoriaResumo.java      # Resumo por categoria
├── dao/
│   ├── UsuarioDao.java           # CRUD + login + buscarPorEmail + deletar
│   ├── DividaDao.java            # CRUD + filtros por usuário
│   ├── TransacaoDao.java         # CRUD + filtros por usuário
│   ├── CategoriaDao.java         # CRUD + contagem de dívidas por categoria
│   ├── CartaoDao.java            # CRUD de cartões
│   └── ChavePixDao.java          # CRUD de chaves Pix
├── database/
│   ├── AppDatabase.java          # Classe principal do Room (versão 7)
│   └── DatabaseClient.java       # Singleton de acesso ao banco
├── utils/
│   ├── SessionManager.java       # Sessão + atualização de nome
│   ├── CategoriaSeeder.java      # Categorias padrão na primeira execução
│   ├── NotificationHelper.java   # Canal + envio de notificações
│   └── AlarmeHelper.java         # Agendamento/cancelamento de alarmes
├── receiver/
│   ├── LembreteReceiver.java     # BroadcastReceiver para notificações
│   └── BootReceiver.java         # ⭐ NOVO: Reagenda alarmes após reinicialização
├── adapter/
│   ├── LancamentoAdapter.java    # Adapter de lançamentos
│   ├── LegendaAdapter.java       # Adapter de legendas
│   ├── DividaAdapter.java        # Adapter de dívidas
│   ├── VencimentoAdapter.java    # Adapter de vencimentos (tela Início)
│   ├── CategoriaAdapter.java     # Adapter de categorias
│   ├── CartaoAdapter.java        # Adapter de cartões
│   ├── ChavePixAdapter.java      # Adapter de chaves Pix
│   └── ChipSelecaoAdapter.java   # Adapter de seleção em chips
└── view/
    ├── SplashActivity.java           # Tela de abertura (2s + verificação de sessão)
    ├── LoginActivity.java            # Tela de Login
    ├── CadastroActivity.java         # Tela de Cadastro
    ├── InicioActivity.java           # Tela de Início (Home)
    ├── DashboardActivity.java        # Dashboard/Relatórios
    ├── DividasActivity.java          # Tela de Dívidas
    ├── CadastroDividaActivity.java   # Cadastro de Dívida (integrado com Cartões/Pix)
    ├── CategoriasActivity.java       # Gerenciamento de Categorias
    ├── EditarPerfilActivity.java     # Edição de Perfil
    ├── ConfiguracoesActivity.java    # Configurações + Logout
    ├── CartoesActivity.java          # Tela de Cartões & Chaves Pix (com abas)
    ├── CartoesFragment.java          # Fragment da aba de Cartões
    └── ChavesPixFragment.java        # Fragment da aba de Chaves Pix
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
- `fix: vincula dívidas e transações ao usuário logado`
- `feat: adiciona entidade Categoria, DAO e atualiza AppDatabase para v3`
- `feat: cria CategoriaSeeder com categorias padrão`
- `feat: implementa tela de Categorias com CRUD completo`
- `feat: prepara back-end para edição de perfil`
- `feat: implementa tela de Editar Perfil com validações e exclusão de conta`
- `feat: implementa lembretes de fatura com notificações`
- `fix: corrige cálculo do valor das parcelas nas dívidas`
- `feat: adiciona entidades Cartao e ChavePix, DAOs e atualiza AppDatabase para v6`
- `feat: implementa tela de Cartões e Chaves Pix com abas e CRUD completo`
- `feat: adiciona tipo ao Cartão e integra tela de Lançar Dívida com Cartões/Pix`
- `feat: adiciona BootReceiver para reagendar alarmes após reinicialização`
- `feat: adiciona exclusão de Cartões e Chaves Pix com clique longo`
- `feat: adiciona edição de Cartões e Chaves Pix (CRUD completo)`
- `docs: adiciona plano de testes completo (casos de teste e critérios de aceitação)`

---

## 🚧 Próximos Passos

- Tela de gerenciamento de cartões com opção de editar e excluir (✅ concluído)
- Exportação de dados (PDF/CSV)
- Biometria real (BiometricPrompt)
- Simulador de pagamento com QR Code (para depois do MVP)
- Autenticação em nuvem (Firebase Auth) – opcional para o MVP final
- Sincronização entre dispositivos – opcional para o MVP final

---

## 📄 Documentação Adicional

- **[docs/TESTES.md](docs/TESTES.md)** – Plano de testes, casos de teste e critérios de aceitação
- **[docs/ARQUITETURA.md](docs/ARQUITETURA.md)** – Documentação técnica sobre a arquitetura e decisões de projeto
- **[docs/BUGS.md](docs/BUGS.md)** – Registro de bugs encontrados e corrigidos
- **[CHANGELOG.md](CHANGELOG.md)** – Histórico detalhado de mudanças por versão/Sprint
- **[Protótipo no Figma](https://www.figma.com/design/BED5loI0pi57B5nkPPufij/ORG---TELA?node-id=71-1192&t=CZH8LT7ObgHAeZla-1)** – Design e protótipo navegável do app

---

> **Observação:** Este README é atualizado ao final de cada Sprint com novos recursos, mudanças e instruções.
