# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 6 (Histórico, Soft Delete e Exportação)

### Adicionado
- **Soft Delete de Dívidas**
  - Campo `excluida` na entidade `Divida` (marcação em vez de exclusão física)
  - Dívidas excluídas somem das telas principais mas permanecem no banco
  - Aparecem no Histórico com status "Excluída ✗" e valor riscado
  - Exclusão em `DividasActivity` e `DashboardActivity` agora usa soft delete
  - Alarme de notificação é cancelado automaticamente ao excluir
- **Tela de Histórico por Mês**
  - `HistoricoActivity` com navegação entre meses (← Setembro 2026 →)
  - Badge "Atual" quando o mês selecionado é o atual
  - Card de resumo do mês: Total, Já Pago, Falta Pagar (com percentuais)
  - Chips de filtro por categoria (dinâmicos)
  - Lista de lançamentos com 4 estados visuais
  - `HistoricoAdapter` com cores e ícones por estado:
    - ✅ Liquidado (verde)
    - ↻ Parcial (azul)
    - ⏳ Pendente (amarelo)
    - ✗ Excluída (vermelho, valor riscado)
  - Layout `activity_historico.xml` fiel ao Figma
  - Layout `item_historico.xml` com card multi-estado
  - Botão "Baixar Resumo do Mês" integrado
- **Exportação CSV**
  - `CsvExportHelper` para geração e compartilhamento de arquivos CSV
  - BOM UTF-8 para abrir corretamente no Excel (acentos)
  - Escape de campos com `;` ou aspas
  - Coluna de status (Liquidado, Parcial, Pendente, Excluída)
  - Compartilhamento via WhatsApp, Email, Drive, etc
- **Exportação PDF**
  - Dependência iTextG 5.5.10 no `build.gradle.kts`
  - `PdfExportHelper` com geração de PDF estilizado
  - Cabeçalho "ORG", card de resumo e lista de lançamentos
  - Cores por estado (verde, vermelho, azul)
  - Compartilhamento via Intent
- **Diálogo de escolha de formato**
  - Botão "Baixar Resumo" agora abre diálogo: CSV ou PDF
- **FileProvider**
  - Configuração de `file_paths.xml` para compartilhar arquivos
  - Registro no `AndroidManifest.xml`
- **Integração na Tela Início**
  - Card "TOTAL DE DÍVIDAS ACUMULADAS" agora é clicável
  - Indicador visual "Ver histórico →"
  - Abre o Histórico ao tocar

### Modificado
- `AppDatabase` atualizado para versão 8 (campo `excluida` em `Divida`)
- Entidade `Divida` agora tem 3 construtores:
  - Completo (Room)
  - Sem ID (assume `excluida = false`)
  - Sem ID com `excluida` (casos especiais)
- Todas as queries do `DividaDao` agora ignoram dívidas com `excluida = 1`
- Adicionadas queries para o Histórico:
  - `listarExcluidasPorUsuario`
  - `listarTodasParaHistorico`
  - `listarPorCategoria`
- `DividasActivity` — método `deletarFisicamente` renomeado para uso restrito
- `DashboardActivity` — `onPagarClick` agora paga uma parcela por vez
- `InicioActivity` — card de total é clicável
- `HistoricoActivity` — botão "Baixar Resumo" abre diálogo CSV/PDF

### Corrigido
- Exclusão de dívidas agora é reversível (soft delete)
- Valores monetários padronizados com locale pt-BR no Dashboard

---

## [Não lançado] – Sprint 5 (Cartões, Chaves Pix, Integração, BootReceiver, CRUD Completo e Correções)

### Adicionado
- **Sistema de BootReceiver (Reagendamento de Alarmes)**
  - `BootReceiver` que escuta `BOOT_COMPLETED` e `MY_PACKAGE_REPLACED`
  - Reagenda alarmes das dívidas não pagas após reiniciar o celular
  - Respeita o estado do switch de lembretes
- **Tela de Cartões & Chaves Pix** com navegação por abas
  - Entidade `Cartao` com tipo (Crédito/Débito)
  - Entidade `ChavePix` com tipo (CNPJ/CPF, Celular, E-mail, Aleatória)
  - CRUD completo com clique normal (editar) e clique longo (excluir)
  - Botão "Copiar Chave" para a área de transferência
- **Integração da tela Lançar Dívida com Cartões e Pix**
  - Filtro dinâmico por tipo
  - Empty State com botão "Cadastrar agora"
- **Documentação de Testes** (`docs/TESTES.md`)
- **Spinner de categoria com bolinha colorida**
- **Barra de navegação inferior na tela Lançar**

### Modificado
- `AppDatabase` atualizado para versão 6 (Cartões e Chaves Pix)
- `AppDatabase` atualizado para versão 7 (campo `tipo` em `Cartao`)
- Pix e Cartão de Débito tratados como à vista (sem parcelas/vencimento)
- Máscara de valor força locale pt-BR
- Cards de dívidas exibem valor da parcela e progresso

### Corrigido
- BUG-002: Pix/Débito pediam parcelas/vencimento
- BUG-003: Máscara de valor não formatava corretamente
- BUG-004: Card sem valor da parcela
- BUG-005: Vencimentos mostravam valor total como pagamento único
- BUG-006: Tela Lançar sem barra de navegação
- BUG-007: Exclusão de categorias em uso
- BUG-008: Erro ao carregar dados na tela Editar Perfil

---

## [Não lançado] – Sprint 4 (Persistência de Dados)

### Adicionado
- **Sistema de Lembretes de Fatura (Notificações)**
  - `NotificationHelper`, `LembreteReceiver`, `AlarmeHelper`
  - Notificação 3 dias antes do vencimento, às 9h
  - Switch funcional em Configurações
- **Tela de Categorias** com CRUD completo
- **Tela de Editar Perfil** completa
- Campo `usuarioId` nas entidades `Divida` e `Transacao`
- Campo `valorParcela` na entidade `Divida`
- Documento `docs/BUGS.md` e `docs/ARQUITETURA.md`

### Corrigido
- Problema de arquitetura: dívidas/transações não vinculadas ao usuário
- BUG-001: Valor da parcela não dividido em dívidas parceladas

---

## [1.0.0] – Sprint 3 (Room + Autenticação) – 2026-09-19

### Adicionado
- Persistência com Room (SQLite)
- Entidades `Usuario`, `Divida`, `Transacao`
- `AppDatabase` (versão 1) e `DatabaseClient`
- `SessionManager` para gerenciar sessão
- Autenticação local com validação no banco

### Modificado
- Login e Cadastro agora usam Room
- Telas Início, Dívidas e Dashboard leem do Room

---

## [0.5.0] – Sprint 2 (Telas Navegáveis) – 2026-09-12

### Adicionado
- Bottom Navigation com 5 abas
- Tela de Início (Home)
- Tela de Cadastro de Dívida
- Tela de Configurações
- Gráfico de rosca (MPAndroidChart)

---

## [0.3.0] – Sprint 1 (Telas Iniciais) – 2026-09-05

### Adicionado
- Splash Screen
- Tela de Login e Cadastro
- Dashboard com gráfico
- Tela de Dívidas com cards
- Fontes personalizadas (Abril Fatface e Lato)

---

## [0.1.0] – Kickoff do Projeto – 2026-09-03

### Adicionado
- Estrutura inicial do projeto Android em Java
- Repositório no GitHub
- Pacotes `model`, `view`, `adapter` e `utils`
