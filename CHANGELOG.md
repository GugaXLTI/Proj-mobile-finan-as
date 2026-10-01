# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 5 (Cartões, Chaves Pix, Integração, BootReceiver e CRUD Completo)

### Adicionado
- **Sistema de BootReceiver (Reagendamento de Alarmes)**
  - `BootReceiver` (BroadcastReceiver) que escuta os eventos `BOOT_COMPLETED` e `MY_PACKAGE_REPLACED`
  - Reagenda automaticamente os alarmes das dívidas não pagas após reiniciar o celular ou atualizar o app
  - Respeita o estado do switch de lembretes (não reagenda se estiver desligado)
  - Verifica se há usuário logado antes de buscar as dívidas no banco
  - Executa a busca no Room em thread separada (evita travar a main thread)
  - Permissão `RECEIVE_BOOT_COMPLETED` adicionada ao Manifest
  - `BootReceiver` registrado no Manifest com os filtros de intent
- **Tela de Cartões & Chaves Pix** com navegação por abas (`TabLayout` + `ViewPager2`)
  - Entidade `Cartao` com campos `usuarioId`, `instituicao`, `apelido`, `ultimos4Digitos`, `diaVencimento`, `limite`, `bandeira` e `tipo` (Crédito/Débito)
  - Entidade `ChavePix` com campos `usuarioId`, `tipoChave`, `chave`, `nomeFavorecido`, `banco` e `apelidoDivida`
  - `CartaoDao` e `ChavePixDao` com CRUD completo e filtros por usuário
  - Fragments `CartoesFragment` e `ChavesPixFragment` para gerenciar cada aba
  - Adapters `CartaoAdapter`, `ChavePixAdapter` e `ChipSelecaoAdapter`
  - Layouts `activity_cartoes.xml`, `item_cartao.xml`, `item_chave_pix.xml`, `fragment_cartoes.xml`, `fragment_chaves_pix.xml` e `item_chip_selecao.xml`
  - Cadastro de cartão com **tipo Crédito/Débito**, instituição, apelido, últimos 4 dígitos e dia de vencimento
  - Cadastro de chave Pix com **tipo** (CNPJ/CPF, Celular, E-mail, Chave Aleatória), chave e nome do favorecido
  - Card de cartão em formato de crédito (bandeira, número mascarado, titular e vencimento)
  - Card de chave Pix com fundo verde (degradê) e badge "Pix Direto"
  - Botão **"Copiar Chave"** que envia a chave Pix para a área de transferência
  - Sigla da bandeira gerada automaticamente (ex: Nubank → NU)
- **Edição de Cartões e Chaves Pix (CRUD Completo)**
  - Adicionado `OnItemClickListener` nos adapters de Cartão e Chave Pix
  - Clique normal em um card = entra em modo edição
  - Campos do formulário são preenchidos com os dados do item selecionado
  - Botão principal muda de "+ Guardar" para "✓ Atualizar" quando em edição
  - Botão **"✕ Cancelar edição"** aparece somente no modo edição
  - Chama `atualizar()` no DAO ao salvar em modo edição
  - Cancela a edição automaticamente se o item em edição for excluído
- **Exclusão de Cartões e Chaves Pix**
  - Adicionado `OnItemLongClickListener` nos adapters de Cartão e Chave Pix
  - Clique longo em um card = abre diálogo de confirmação de exclusão
  - Exclui o item do banco (Room) após confirmação
  - Recarrega a lista automaticamente após exclusão
  - Exibe Toast informativo de sucesso
- **Integração da tela de Lançar Dívida com Cartões e Chaves Pix**
  - Filtro dinâmico: ao escolher "Cartão de Crédito", mostra apenas cartões de crédito cadastrados
  - Ao escolher "Cartão de Débito", mostra apenas cartões de débito cadastrados
  - Ao escolher "Pix", mostra apenas as chaves Pix cadastradas
  - Empty State com mensagem específica e botão **"Cadastrar agora"** quando não há itens
  - Botão de atalho **"+ Novo"** no cabeçalho da seleção
  - Link **"+ Nova Categoria"** no label de categoria
- **Redesign do layout `activity_cadastro_divida.xml`** fiel ao protótipo do Figma
  - Campos **Valor/Parcelas** e **Data da Compra/1º Vencimento** lado a lado
  - Seleção de cartões/Pix em **chips horizontais** com estado de seleção visual
  - Link **"+ Nova Categoria"** alinhado à direita do label
- **Drawables personalizados**
  - `bg_form_field.xml` (fundo de campos e spinners)
  - `bg_button_green.xml` (fundo dos botões verdes)
  - `bg_card_pix.xml` (degradê verde dos cards de chave Pix)
- **Documentação de Testes**
  - Arquivo `docs/TESTES.md` com plano de testes completo
  - 13 categorias de casos de teste (CT-01 a CT-13)
  - Critérios de aceitação para release
  - Fluxo de reporte de bugs (integração com `docs/BUGS.md`)
  - Template de histórico de execuções

### Modificado
- `AppDatabase` atualizado para versão 6 (adiciona `Cartao` e `ChavePix`)
- `AppDatabase` atualizado para versão 7 (adiciona campo `tipo` em `Cartao`)
- `CadastroDividaActivity` agora integra com cartões e chaves Pix cadastrados
- `CadastroDividaActivity` agora filtra cartões por tipo (Crédito/Débito)
- `CartoesFragment` agora permite escolher o **tipo do cartão** (Crédito/Débito)
- `CartoesFragment` agora gerencia modo edição e exclusão com clique longo
- `ChavesPixFragment` agora gerencia modo edição e exclusão com clique longo
- `CartaoAdapter` agora exibe o tipo do cartão junto ao nome do banco
- `CartaoAdapter` recebe o `nomeUsuario` no construtor para exibir como titular
- `CartaoAdapter` agora suporta clique normal (editar) e clique longo (excluir)
- `ChavePixAdapter` agora suporta clique normal (editar) e clique longo (excluir)
- `ConfiguracoesActivity` agora abre a tela `CartoesActivity` no item "Meus Cartões & Bancos"
- `CartoesActivity` registrada no `AndroidManifest.xml`
- `AndroidManifest.xml` agora inclui permissão `RECEIVE_BOOT_COMPLETED`
- `AndroidManifest.xml` agora registra o `BootReceiver`
- Ajuste nas cores dos layouts para usar o padrão do projeto (`brand_green`, `text_gray`, `bg_screen`, etc.)

### Corrigido
- Layout `activity_cartoes.xml` e `item_cartao.xml` ajustados para evitar erro `Android resource linking failed`
- Substituído `ImageView` por `TextView` com seta unicode no botão voltar (evita erro de `app:tint`)
- ~~Limitação: alarmes eram perdidos após reiniciar o celular~~ ✅ Resolvido com o `BootReceiver`

---

## [Não lançado] – Sprint 4 (Persistência de Dados)

### Adicionado
- **Sistema de Lembretes de Fatura (Notificações)**
  - `NotificationHelper` para criar canal de notificação e enviar avisos
  - `LembreteReceiver` (BroadcastReceiver) para receber os alarmes agendados
  - `AlarmeHelper` para agendar e cancelar alarmes com `AlarmManager`
  - Notificação **3 dias antes do vencimento**, às 9h da manhã
  - Solicita permissão `POST_NOTIFICATIONS` automaticamente no Android 13+
  - Cancela o alarme ao pagar ou excluir uma dívida
  - Reagenda automaticamente ao editar uma dívida
  - **Switch funcional de lembretes** em Configurações (ativar/desativar)
  - Permissões `SCHEDULE_EXACT_ALARM` e `USE_EXACT_ALARM` no Manifest
  - Funciona totalmente **offline** (usa alarmes locais)
- **Tela de Categorias** com CRUD completo (criar, listar e excluir)
  - Entidade `Categoria` com campos `usuarioId`, `nome` e `cor`
  - `CategoriaDao` com métodos filtrados por usuário
  - `CategoriaSeeder` com 8 categorias padrão (Alimentação, Transporte, Saúde, Educação, Lazer, Moradia, Assinaturas, Outros)
  - Paleta com 8 cores de identificação
  - Prévia da tag em tempo real
  - Contagem de dívidas por categoria
  - Confirmação ao excluir categoria
- **Tela de Editar Perfil** completa
  - Edição de nome, e-mail e senha
  - Validação de senha atual obrigatória
  - Campo opcional para alterar senha (nova senha + confirmação)
  - Validação de nome (mínimo 2 letras, apenas letras)
  - Validação de e-mail com Regex
  - Verificação de e-mail duplicado
  - Funcionalidade "Eliminar Conta e Limpar Registros"
- Campo `usuarioId` nas entidades `Divida` e `Transacao`
- Campo `valorParcela` na entidade `Divida` (cálculo automático do valor de cada parcela)
- Filtros por usuário nos DAOs (`listarPorUsuario`, `listarNaoPagasPorUsuario`)
- Métodos `atualizar()` e `deletar()` no `UsuarioDao`
- Método `atualizarNome()` no `SessionManager`
- Setters no modelo `Usuario` (setNome, setEmail, setSenha)
- Botão "Limpar Tudo" em Configurações (para testes internos)
- Tela de edição de dívida (abre cadastro em modo edição)
- Diálogo de confirmação ao excluir dívida
- Máscara de valor automática (R$ 0,00)
- Campo Nome no cadastro com validação (mínimo 2 letras)
- Validação de e-mail com Regex
- Documento `docs/BUGS.md` para registro de bugs encontrados e corrigidos
- Documentação em `docs/ARQUITETURA.md`

### Modificado
- `AppDatabase` atualizado para versão 5 (adiciona `valorParcela` em `Divida`)
- `CadastroDividaActivity` agora carrega categorias **dinâmicas** do banco (antes era lista fixa)
- `CadastroDividaActivity` agora **calcula o valor da parcela** ao salvar/editar dívida
- Categorias do usuário aparecem automaticamente no spinner de cadastro de dívida
- `CadastroDividaActivity` agora **agenda notificação** ao salvar/editar dívida
- `DividasActivity` agora **cancela notificação** ao pagar/excluir dívida
- `DividaAdapter` agora exibe o **valor da parcela** (não o valor total)
- `ConfiguracoesActivity` tem **switch funcional** de lembretes (antes era só Toast)
- `InicioActivity` agora **solicita permissão** de notificação no Android 13+
- Botão "Editar" em Configurações agora abre a `EditarPerfilActivity` (antes era Toast)
- `CategoriasActivity` registrada no `AndroidManifest.xml`
- `EditarPerfilActivity` registrada no `AndroidManifest.xml`
- `LembreteReceiver` registrado no `AndroidManifest.xml`
- Todas as Activities agora usam `session.getUserId()` para consultar dados
- `DividaAdapter` agora passa objeto `Divida` em vez de `int position`
- Layout do cadastro de dívida ajustado (label "Devedor" acima do campo)
- Layout do cadastro de usuário adicionado campo Nome + ScrollView

### Removido
- `DatabaseSeeder.java` (não é mais usado)
- `DadosMock.java` (código morto)
- Dados fictícios que apareciam para usuários novos

### Corrigido
- **Problema de arquitetura:** dívidas e transações não estavam vinculadas ao usuário logado
  - Cada usuário agora vê apenas seus próprios dados
  - Isolamento completo entre contas diferentes
- **BUG-001:** valor da parcela não era dividido em dívidas parceladas
  - Adicionado campo `valorParcela` na entidade `Divida`
  - `CadastroDividaActivity` calcula o valor da parcela ao salvar
  - `DividaAdapter` exibe o valor da parcela (ex: R$ 2.000 em 10x → R$ 200,00 por parcela)
- Nova categoria criada agora aparece imediatamente no cadastro de dívidas
- Nome do usuário é atualizado em todas as telas após edição do perfil

---

## [1.0.0] – Sprint 3 (Room + Autenticação) – 2026-09-19

### Adicionado
- Persistência de dados com Room (SQLite)
- Entidades `Usuario`, `Divida` e `Transacao` com `@Entity`
- DAOs (`UsuarioDao`, `DividaDao`, `TransacaoDao`) com CRUD completo
- Classe `AppDatabase` (versão 1)
- Classe `DatabaseClient` (singleton)
- `SessionManager` para gerenciar sessão com SharedPreferences
- Autenticação local com validação no banco
- Sessão persistente (usuário continua logado ao reabrir o app)
- Logout limpando a sessão

### Modificado
- Tela de Login agora valida credenciais no Room
- Tela de Cadastro agora salva usuário no Room
- Tela de Início, Dívidas e Dashboard agora leem dados do Room
- Cadastro de Dívida agora salva no Room

---

## [0.5.0] – Sprint 2 (Telas Navegáveis) – 2026-09-12

### Adicionado
- Bottom Navigation com 5 abas (Início, Lançar, Dívidas, Relatórios, Config)
- Tela de Início (Home) com resumo de dívidas e vencimentos
- Tela de Cadastro de Dívida com formulário completo
- Tela de Configurações com perfil, cartões, categorias, biometria e backup
- Gráfico de rosca (MPAndroidChart) na Dashboard

### Modificado
- Dashboard movida para a aba "Relatórios"
- Aba "Lançar" agora abre o Cadastro de Dívida
- Navegação ajustada em todas as telas

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
