# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 5 (Cartões, Chaves Pix, Integração, BootReceiver, CRUD Completo e Correções)

### Adicionado
- **Sistema de BootReceiver (Reagendamento de Alarmes)**
  - `BootReceiver` (BroadcastReceiver) que escuta os eventos `BOOT_COMPLETED` e `MY_PACKAGE_REPLACED`
  - Reagenda automaticamente os alarmes das dívidas não pagas após reiniciar o celular ou atualizar o app
  - Respeita o estado do switch de lembretes
  - Executa a busca no Room em thread separada
  - Permissão `RECEIVE_BOOT_COMPLETED` adicionada ao Manifest
- **Tela de Cartões & Chaves Pix** com navegação por abas (`TabLayout` + `ViewPager2`)
  - Entidade `Cartao` com campos `usuarioId`, `instituicao`, `apelido`, `ultimos4Digitos`, `diaVencimento`, `limite`, `bandeira` e `tipo`
  - Entidade `ChavePix` com campos `usuarioId`, `tipoChave`, `chave`, `nomeFavorecido`, `banco` e `apelidoDivida`
  - `CartaoDao` e `ChavePixDao` com CRUD completo
  - Fragments `CartoesFragment` e `ChavesPixFragment`
  - Adapters `CartaoAdapter`, `ChavePixAdapter` e `ChipSelecaoAdapter`
  - Cadastro de cartão com **tipo Crédito/Débito**
  - Cadastro de chave Pix com **tipo** (CNPJ/CPF, Celular, E-mail, Chave Aleatória)
  - Botão **"Copiar Chave"** que envia a chave Pix para a área de transferência
- **Edição e Exclusão de Cartões e Chaves Pix (CRUD Completo)**
  - Clique normal em um card = entra em modo edição
  - Clique longo em um card = abre diálogo de confirmação de exclusão
  - Botão principal muda de "+ Guardar" para "✓ Atualizar" quando em edição
  - Botão **"✕ Cancelar edição"** aparece somente no modo edição
- **Integração da tela de Lançar Dívida com Cartões e Chaves Pix**
  - Filtro dinâmico por tipo (Crédito, Débito, Pix)
  - Empty State com botão **"Cadastrar agora"** quando não há itens
  - Botão de atalho **"+ Novo"** e link **"+ Nova Categoria"**
- **Redesign do layout `activity_cadastro_divida.xml`** fiel ao protótipo do Figma
- **Drawables personalizados** (`bg_form_field`, `bg_button_green`, `bg_card_pix`)
- **Documentação de Testes**
  - Arquivo `docs/TESTES.md` com plano completo de testes
  - 13 categorias de casos de teste (CT-01 a CT-13)
- **Spinner de categoria com bolinha colorida**
  - Novo layout `item_spinner_categoria.xml`
  - Novo adapter `CategoriaSpinnerAdapter`
  - Exibe a cor da categoria ao lado do nome no spinner
- **Barra de navegação inferior na tela de Lançar Dívida**
  - Aba "Lançar" destacada em verde

### Modificado
- `AppDatabase` atualizado para versão 6 (adiciona `Cartao` e `ChavePix`)
- `AppDatabase` atualizado para versão 7 (adiciona campo `tipo` em `Cartao`)
- `CadastroDividaActivity` agora integra com cartões e chaves Pix cadastrados
- `CartoesFragment` e `ChavesPixFragment` agora gerenciam modo edição e exclusão
- `CartaoAdapter` e `ChavePixAdapter` agora suportam clique normal e clique longo
- `CadastroDividaActivity` agora esconde Parcelas/Vencimento para Pix e Débito
- `CadastroDividaActivity` agora tem barra de navegação inferior
- `DividaAdapter` agora exibe o valor da parcela (ex: "Parcela 3x de R$ 66,67")
- `VencimentoAdapter` agora exibe progresso de parcelas e valor da parcela
- `InicioActivity` agora usa locale pt-BR em todos os valores monetários
- `CategoriasActivity` agora bloqueia exclusão de categorias em uso
- `EditarPerfilActivity` agora tem 3 fallbacks para carregar o usuário
- `UsuarioDao` agora tem métodos `buscarPorNome`, `buscarPrimeiroUsuario` e `contarUsuarios`
- `SessionManager` agora salva e atualiza o e-mail do usuário
- `AppDatabase` agora inclui permissão `RECEIVE_BOOT_COMPLETED` e registra o `BootReceiver`
- Ajuste nas cores dos layouts para usar o padrão do projeto

### Corrigido
- **BUG-002:** Pix e Cartão de Débito não deveriam ter parcelas/vencimento
  - Esconde os containers de Parcelas e Vencimento para esses tipos
  - Marca automaticamente como pago (pago = true, valorPago = valorTotal)
  - Não agenda alarme de notificação para compras à vista
- **BUG-003:** Máscara de valor não formatava corretamente em celulares em inglês
  - Locale pt-BR forçado em todos os `String.format` de valores monetários
  - Agora sempre exibe "R$ 123,45" (com vírgula)
- **BUG-004:** Card de dívidas não mostrava o valor da parcela
  - Exibe "Parcela 3x de R$ 66,67" quando há mais de 1 parcela
- **BUG-005:** Vencimentos na tela Início mostravam o valor total como se fosse pagamento único
  - Adiciona linha de progresso: "Parcela 2 de 10"
  - Exibe valor da parcela em destaque e o total em letras menores
- **BUG-006:** Tela Lançar Dívida não tinha barra de navegação
  - Envolve layout em ConstraintLayout com bottomNavigation
- **BUG-007:** Exclusão de categorias em uso por dívidas criava categorias órfãs
  - Bloqueia exclusão quando há dívidas usando a categoria
  - Cria nova categoria automaticamente ao editar dívida com categoria órfã
- **BUG-008:** Erro "Erro ao carregar dados do Usuário" na tela Editar Perfil
  - Adiciona 3 fallbacks: por ID, por nome e por único usuário
  - Força logout e redireciona para Login quando sessão está corrompida
- Layout `activity_cartoes.xml` e `item_cartao.xml` ajustados para evitar erro `Android resource linking failed`

---

## [Não lançado] – Sprint 4 (Persistência de Dados)

### Adicionado
- **Sistema de Lembretes de Fatura (Notificações)**
  - `NotificationHelper`, `LembreteReceiver` e `AlarmeHelper`
  - Notificação **3 dias antes do vencimento**, às 9h da manhã
  - Switch funcional de lembretes em Configurações
  - Funciona totalmente offline
- **Tela de Categorias** com CRUD completo
  - Entidade `Categoria` com campos `usuarioId`, `nome` e `cor`
  - `CategoriaSeeder` com 8 categorias padrão
- **Tela de Editar Perfil** completa
- Campo `usuarioId` nas entidades `Divida` e `Transacao`
- Campo `valorParcela` na entidade `Divida`
- Documento `docs/BUGS.md`
- Documentação em `docs/ARQUITETURA.md`

### Modificado
- `AppDatabase` atualizado para versão 5
- `CadastroDividaActivity` agora carrega categorias dinâmicas
- `CadastroDividaActivity` agora calcula o valor da parcela
- `ConfiguracoesActivity` tem switch funcional de lembretes
- `InicioActivity` agora solicita permissão de notificação

### Removido
- `DatabaseSeeder.java`
- `DadosMock.java`

### Corrigido
- **Problema de arquitetura:** dívidas e transações não estavam vinculadas ao usuário logado
- **BUG-001:** valor da parcela não era dividido em dívidas parceladas

---

## [1.0.0] – Sprint 3 (Room + Autenticação) – 2026-09-19

### Adicionado
- Persistência de dados com Room (SQLite)
- Entidades `Usuario`, `Divida` e `Transacao`
- DAOs com CRUD completo
- `AppDatabase` (versão 1) e `DatabaseClient`
- `SessionManager` para gerenciar sessão
- Autenticação local com validação no banco

### Modificado
- Tela de Login agora valida credenciais no Room
- Tela de Cadastro agora salva usuário no Room

---

## [0.5.0] – Sprint 2 (Telas Navegáveis) – 2026-09-12

### Adicionado
- Bottom Navigation com 5 abas
- Tela de Início (Home) com resumo
- Tela de Cadastro de Dívida com formulário completo
- Tela de Configurações
- Gráfico de rosca (MPAndroidChart) na Dashboard

### Modificado
- Dashboard movida para a aba "Relatórios"
- Aba "Lançar" agora abre o Cadastro de Dívida

---

## [0.3.0] – Sprint 1 (Telas Iniciais) – 2026-09-05

### Adicionado
- Splash Screen com logo e timer de 2 segundos
- Tela de Login com validação de campos
- Tela de Cadastro com confirmação de senha
- Dashboard com gráfico e filtros por categoria
- Tela de Dívidas com cards e totais
- Fontes personalizadas (Abril Fatface e Lato)

### Modificado
- Estrutura de cores centralizada em `colors.xml`

---

## [0.1.0] – Kickoff do Projeto – 2026-09-03

### Adicionado
- Estrutura inicial do projeto Android em Java
- Repositório no GitHub com README e .gitignore
- Pacotes `model`, `view`, `adapter` e `utils`
- Classe `Transacao` (modelo de dados)
