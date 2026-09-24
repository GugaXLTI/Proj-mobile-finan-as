# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 4 (Persistência de Dados)

### Adicionado
- Campo `usuarioId` nas entidades `Divida` e `Transacao`
- Filtros por usuário nos DAOs (`listarPorUsuario`, `listarNaoPagasPorUsuario`)
- Botão "Limpar Tudo" em Configurações (para testes internos)
- Tela de edição de dívida (abre cadastro em modo edição)
- Diálogo de confirmação ao excluir dívida
- Máscara de valor automática (R$ 0,00)
- Campo Nome no cadastro com validação (mínimo 2 letras)
- Validação de e-mail com Regex
- Documentação em `docs/ARQUITETURA.md`

### Modificado
- `AppDatabase` atualizado para versão 2 com `fallbackToDestructiveMigration`
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
