# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

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
- Documentação em `docs/ARQUITETURA.md`

### Modificado
- `AppDatabase` atualizado para versão 4 (adiciona `Categoria`)
- `CadastroDividaActivity` agora carrega categorias **dinâmicas** do banco (antes era lista fixa)
- Categorias do usuário aparecem automaticamente no spinner de cadastro de dívida
- `CadastroDividaActivity` agora **agenda notificação** ao salvar/editar dívida
- `DividasActivity` agora **cancela notificação** ao pagar/excluir dívida
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
