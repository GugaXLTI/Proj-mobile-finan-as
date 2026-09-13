<img width="897" height="63" alt="image" src="https://github.com/user-attachments/assets/a661e4b9-83ad-4259-9c5d-acce3515d721" /># App Gestão de Finanças

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 2 (Telas Navegáveis) – MVP concluído** 🚀

- ✅ Splash Screen
- ✅ Tela de Login
- ✅ Tela de Cadastro
- ✅ Dashboard (Relatórios)
- ✅ Tela de Dívidas
- ✅ Tela de Cadastro de Dívida
- ✅ Tela de Configurações
- ✅ Bottom Navigation funcional

  ----------------------------------------------------------------------------------

## Descrição
Aplicativo de gestão de finanças pessoais desenvolvido para a disciplina de **[Nome da Disciplina]** no curso de **[Nome do Curso]**.

O app permite ao usuário:
- Registrar receitas e despesas
- Visualizar saldo total e gráficos por categoria
- Acompanhar o histórico de transações
- Gerenciar dívidas e vencimentos
- Configurar preferências do sistema (biometria, lembretes, backup)

----------------------------------------------------------------------------------

## 🖥️ Tecnologias e Ferramentas
- **Linguagem:** Java
- **IDE:** Android Studio Iguana (2023.2.1)
- **Versionamento:** Git + GitHub
- **Sistema Operacional:** Android (mínimo API 24 – Android 7.0)
- **Design:** Figma (protótipos desenvolvidos pela equipe)
- **Bibliotecas:** MPAndroidChart, Material Design Components, RecyclerView, CardView

----------------------------------------------------------------------------------

## 👥 Equipe
| Nome | Função |
|------|--------|
| **Gustavo Piteira** | Líder do Projeto / Desenvolvedor Back-End |
| **Israel Malheiros** | Desenvolvedor Front-End / Design Figma / UX & UI |
| **Gustavo Marques** | Desenvolvedor (XML e Interface) |
| **Francisco Andrade** | Desenvolvedor (Lógica e Integração) |

----------------------------------------------------------------------------------

## 📱 Telas do App

### Splash Screen
![Splash Screen](logo.png)

Tela de abertura com logo, fontes personalizadas (Abril Fatface e Lato), timer de 2 segundos e redirecionamento automático para o Login.

### Tela de Login
Formulário com campos de e-mail e senha, validação de campos obrigatórios, link para cadastro e navegação para a Dashboard.

### Tela de Cadastro
Formulário com campos de nome, e-mail, senha e confirmação de senha. Validações: campos obrigatórios, senhas coincidentes e tamanho mínimo de 6 caracteres.

### Dashboard (Relatórios)
Gráfico de pizza (MPAndroidChart) com distribuição de gastos por categoria, legendas interativas, filtros dinâmicos, lista de lançamentos e botão de exportação (mock).

### Tela de Dívidas
Cards com título, valor restante, banco/categoria, parcela e vencimento. Resumo com totais (a pagar, pago, geral) e botões de ação (Excluir, Editar, Pagar).

### Cadastro de Dívida
Formulário com tipo de dívida, banco, devedor, descrição, categoria, valor, parcelas, data da compra e 1º vencimento. Utiliza DatePickerDialog e Spinners.

### Configurações
Tela com perfil do usuário, gerenciamento de cartões e categorias, lembretes de fatura, biometria, backup de dados e desconexão de sessão.

### Bottom Navigation
Barra inferior com abas: Início, Lançar, Dívidas, Relatórios e Config – com navegação funcional entre as telas.

----------------------------------------------------------------------------------

## 🔧 Como Clonar e Executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/GugaXLTI/Proj-mobile-finan-as.git

2. **Abra o projeto no Android Studio (versão Iguana 2023.2.1 ou superior).**

3. **Aguarde o Gradle sincronizar e baixar as dependências.**

4. **Execute o app em um emulador ou dispositivo físico (API 24+).**

5. **Para atualizar o código:**

(Dar PULL via terminal no Android Studio) - "git pull origin master"
----------------------------------------------------------------------------------


## 📂 Estrutura do Projeto (resumida)

```text
app/src/main/java/com/example/controle_gastos/
├── model/
│   ├── Transacao.java          # Classe de dados de transações
│   ├── Divida.java             # Classe de dados de dívidas
│   └── CategoriaResumo.java    # Resumo por categoria (legendas)
├── utils/
│   └── DadosMock.java          # Dados de exemplo (transações e dívidas)
├── adapter/
│   ├── LancamentoAdapter.java  # Adapter de lançamentos
│   ├── LegendaAdapter.java     # Adapter de legendas
│   └── DividaAdapter.java      # Adapter de dívidas
└── view/
    ├── SplashActivity.java     # Tela de abertura (2s)
    ├── LoginActivity.java      # Tela de Login
    ├── CadastroActivity.java   # Tela de Cadastro
    ├── DashboardActivity.java  # Dashboard/Relatórios
    ├── DividasActivity.java    # Tela de Dívidas
    ├── CadastroDividaActivity.java  # Cadastro de Dívida
    └── ConfiguracoesActivity.java   # Configurações
```
    ----------------------------------------------------------------------------------

## 📌 Histórico de Commits (resumo)

- feat: estrutura inicial do app com Java e classe Transacao

- feat: adiciona DadosMock com transações iniciais

- feat: adiciona Splash Screen com timer e estilização

- style: aplica fontes Abril Fatface e Lato

- feat: finaliza tela de Login com logo e estilização

- feat: implementa tela de Cadastro com validações

- feat: adiciona Dashboard com gráfico e filtros

- feat: implementa tela de Dívidas com cards e totais

- feat: adiciona tela de Cadastro de Dívida

- feat: implementa tela de Configurações e navegação

----------------------------------------------------------------------------------

## 🚧 Próximos Passos

- Persistência de dados (Room ou Firebase)

- Autenticação real (Firebase Auth)

- Implementação da tela "Início" (resumo de dívidas)

- Edição de dívidas

- Exportação de dados (PDF/CSV)
