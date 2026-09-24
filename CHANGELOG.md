# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado]

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

## [1.0.0] - 2026-09-19

### Adicionado
- Persistência de dados com Room (SQLite)
- Autenticação local (cadastro + login)
- Sessão persistente com SharedPreferences
- CRUD completo de dívidas
- Tela de Início (Home)
- Dashboard com gráfico de rosca
- Tela de Dívidas
- Cadastro de Dívida
- Tela de Configurações
- Bottom Navigation funcional

---

## [0.1.0] - 2026-09-03

### Adicionado
- Estrutura inicial do projeto Android
- Splash Screen
- Tela de Login
- Tela de Cadastro
- Navegação básica entre telas
- Dados mock para testes
