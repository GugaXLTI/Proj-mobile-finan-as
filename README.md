# App Gestão de Finanças

## Status do Projeto
🚀 **Em desenvolvimento – Sprint 2 (Telas Navegáveis)** 🚀

- ✅ Splash Screen concluída
- ⏳ Tela de Login (em andamento)
- ⏳ Tela de Cadastro (em andamento)
- ⏳ Dashboard e Lista de Lançamentos (planejadas)

## Descrição
Aplicativo de gestão de finanças pessoais desenvolvido para a disciplina de **[Nome da Disciplina]** no curso de **[Nome do Curso]**.

O app permite ao usuário:
- Registrar receitas e despesas
- Visualizar saldo total
- Acompanhar o histórico de transações
- (Funcionalidades futuras: gráficos, filtros e relatórios)

## 🖥️ Tecnologias e Ferramentas
- **Linguagem:** Java
- **IDE:** Android Studio Iguana (2023.2.1)
- **Versionamento:** Git + GitHub
- **Sistema Operacional:** Android (mínimo API 24 – Android 7.0)
- **Design:** Figma (protótipos desenvolvidos pela equipe)

## 👥 Equipe
| Nome | Função |
|------|--------|
| **Gustavo Piteira** | Líder do Projeto / Desenvolvedor Back-End |
| **Israel Malheiros** | Desenvolvedor Front-End / Design Figma / UX & UI |
| **Gustavo Marques** | Desenvolvedor (XML e Interface) |
| **Francisco Andrade** | Desenvolvedor (Lógica e Integração) |

## 📱 Telas do App (em desenvolvimento)

### Splash Screen
![Splash Screen](logo.png)

A tela de abertura do app, exibida por 2 segundos, com logo, título estilizado (fonte Abril Fatface) e subtítulo (fonte Lato). Redireciona automaticamente para a tela de Login.

*(Em breve: prints das telas de Login, Cadastro, Dashboard e Lista)*

## 🔧 Como Clonar e Executar

1. **Clone o repositório:**
   ```bash
   git clone https://github.com/GugaXLTI/Proj-mobile-finan-as.git

2. **Abra o projeto no Android Studio (versão Iguana 2023.2.1 ou superior).**

3. **Aguarde o Gradle sincronizar e baixar as dependências.**

4. **Execute o app em um emulador ou dispositivo físico (API 24+).**

5. **Para atualizar o código (quando houver novas alterações):**

git pull origin master

## 📂 Estrutura do Projeto (resumida)
app/src/main/java/com/example/controle_gastos/
├── model/
│   └── Transacao.java          # Classe de dados
├── utils/
│   └── DadosMock.java          # Lista de transações de exemplo
├── view/
│   ├── SplashActivity.java     # Tela de abertura (2s)
│   ├── LoginActivity.java      # (em desenvolvimento)
│   └── CadastroActivity.java   # (em desenvolvimento)
└── adapter/                    # (futuro RecyclerView)

## 📌 Histórico de commits

. feat: estrutura inicial do app com Java e classe Transacao
. feat: adiciona DadosMock com transações iniciais
. feat: adiciona Splash Screen com timer e estilização
. style: aplica fontes Abril Fatface e Lato
. feat: adiciona logo na Splash Screen

**OBSERVAÇÕES**

Observação: Este README será atualizado ao final de cada Sprint com novos prints, funcionalidades e instruções.
