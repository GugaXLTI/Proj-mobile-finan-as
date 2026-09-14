# App Gestão de Finanças

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 2 (Telas Navegáveis) – Protótipo funcional completo** 🚀

- ✅ Splash Screen
- ✅ Tela de Login
- ✅ Tela de Cadastro
- ✅ Tela de Início (Home)
- ✅ Dashboard (Relatórios)
- ✅ Tela de Dívidas
- ✅ Tela de Cadastro de Dívida
- ✅ Tela de Configurações
- ✅ Bottom Navigation funcional

> **Observação:** o MVP final será entregue no Sprint 6, com persistência de dados e autenticação real.

---

## Descrição
Aplicativo de gestão de finanças pessoais desenvolvido para a disciplina de **[Nome da Disciplina]** no curso de **[Nome do Curso]**.

O app permite ao usuário:
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
- **Bibliotecas:** MPAndroidChart, Material Design Components, RecyclerView, CardView

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
Tela de abertura com logo, fontes personalizadas (Abril Fatface e Lato), timer de 2 segundos e redirecionamento automático para o Login.

### Tela de Login
Formulário com campos de e-mail e senha, validação de campos obrigatórios, link para cadastro e navegação para a tela de Início.

### Tela de Cadastro
Formulário com campos de nome, e-mail, senha e confirmação de senha. Validações: campos obrigatórios, senhas coincidentes e tamanho mínimo de 6 caracteres.

### Tela de Início (Home)
Resumo de dívidas acumuladas, total já pago, alerta de faturas do mês e lista dos próximos vencimentos. Botão para acessar todas as contas e cartões.

### Dashboard (Relatórios)
Gráfico de rosca (MPAndroidChart) com distribuição de dívidas por categoria, cores personalizadas do Figma, percentuais dentro das fatias, total no centro, filtros em formato de pílula e lista de dívidas não pagas.

### Tela de Dívidas
Cards com título, valor restante, banco/categoria, parcela e vencimento. Resumo com totais (a pagar, pago, geral) e botões de ação (Excluir, Editar, Pagar).

### Cadastro de Dívida
Formulário com tipo de dívida, banco, devedor, descrição, categoria, valor, parcelas, data da compra e 1º vencimento. Utiliza DatePickerDialog e Spinners.

### Configurações
Tela com perfil do usuário, gerenciamento de cartões e categorias, lembretes de fatura, biometria, backup de dados e desconexão de sessão.

### Bottom Navigation
Barra inferior com abas: Início, Lançar, Dívidas, Relatórios e Config – com navegação funcional entre as telas. A aba "Lançar" abre o cadastro de dívida.

---

## 🔧 Como Clonar e Executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/GugaXLTI/Proj-mobile-finan-as.git
   ```

2. **Abra o projeto no Android Studio** (versão Iguana 2023.2.1 ou superior).

3. **Aguarde o Gradle sincronizar** e baixar as dependências.

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
│   ├── Transacao.java          # Classe de dados de transações
│   ├── Divida.java             # Classe de dados de dívidas
│   └── CategoriaResumo.java    # Resumo por categoria
├── utils/
│   └── DadosMock.java          # Dados de exemplo (transações e dívidas)
├── adapter/
│   ├── LancamentoAdapter.java  # Adapter de lançamentos
│   ├── LegendaAdapter.java     # Adapter de legendas
│   ├── DividaAdapter.java      # Adapter de dívidas
│   └── VencimentoAdapter.java  # Adapter de vencimentos (tela Início)
└── view/
    ├── SplashActivity.java     # Tela de abertura (2s)
    ├── LoginActivity.java      # Tela de Login
    ├── CadastroActivity.java   # Tela de Cadastro
    ├── InicioActivity.java     # Tela de Início (Home)
    ├── DashboardActivity.java  # Dashboard/Relatórios
    ├── DividasActivity.java    # Tela de Dívidas
    ├── CadastroDividaActivity.java  # Cadastro de Dívida
    └── ConfiguracoesActivity.java   # Configurações
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

---

## 🚧 Próximos Passos

- Persistência de dados (Room ou Firebase)
- Autenticação real (Firebase Auth)
- Edição de dívidas
- Exportação de dados (PDF/CSV)
- Melhorias na tela de Configurações (cartões e categorias)

---

> **Observação:** Este README será atualizado ao final de cada Sprint com novos prints, funcionalidades e instruções.
