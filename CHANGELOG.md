# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 6 (Biometria, Gráfico de Linha, Filtro de Período e Backup)

### Adicionado
- **Autenticação Biométrica (BiometricPrompt)**
  - Dependência `androidx.biometric:biometric:1.1.0` no `build.gradle.kts`
  - Permissão `USE_BIOMETRIC` no `AndroidManifest.xml`
  - Classe `BiometricHelper` com métodos `podeUsarBiometria()` e `autenticar()`
  - Botão "👤 Usar biometria" no Login (aparece apenas se ativo no dispositivo)
  - Switch funcional de biometria em Configurações
  - Preferências de biometria que **sobrevivem ao logout**
  - Ao ativar: pede autenticação biométrica para confirmar
  - Ao fazer login normal: dados biométricos são atualizados
  - Suporte a digital, face e íris (dependendo do hardware)
- **Gráfico de Linha — Evolução Mensal**
  - Novo `LineChart` no Dashboard com estilo escuro
  - Exibe a evolução dos gastos nos últimos N meses
  - Chips de período: **3M / 6M / 12M**
  - Chip selecionado em roxo, demais em cinza
  - Exibe total acumulado do período selecionado
  - Considera apenas dívidas **não excluídas** (pagas + não pagas)
  - Linha verde com curvas bezier e área preenchida translúcida
- **Filtro de Período Customizado no Histórico**
  - Botão **📅** na barra de navegação do Histórico
  - Dois `DatePicker`s em sequência (data inicial + data final)
  - Botão **✕** vermelho para limpar o filtro (aparece apenas quando ativo)
  - Título muda para "01/09/2026 a 30/09/2026" quando filtro ativo
  - Validação: data final deve ser ≥ data inicial
  - Setas ← → desativam o filtro automaticamente
  - Exportação (CSV/PDF) usa o período no nome do arquivo
- **Backup Local (Exportar/Restaurar em JSON)**
  - Dependência `com.google.code.gson:gson:2.10.1` no `build.gradle.kts`
  - POJO `BackupData` agrupa todos os dados do usuário
  - Classe `BackupHelper` com métodos `exportar()`, `lerArquivo()` e `aplicarBackup()`
  - Método `deletarTodosDoUsuario()` adicionado nos 4 DAOs
  - Diálogo com 3 opções: Exportar / Restaurar / Limpar Tudo
  - Arquivo JSON com data/hora no nome (`backup_org_2026-10-10_14-30.json`)
  - Compartilhamento via FileProvider (Drive, WhatsApp, etc)
  - Validação do campo `versao` antes de importar
  - Diálogo de confirmação com **resumo do backup** antes de substituir dados
  - Reagenda automaticamente alarmes das dívidas não pagas após restaurar

### Modificado
- `DashboardActivity` agora usa `DividaDashboardAdapter` (somente leitura)
- `DashboardActivity` ganhou gráfico de linha + chips de período
- `HistoricoActivity` ganhou filtro de período + labels dinâmicos de percentual
- `ConfiguracoesActivity` — item "Backup dos Dados" agora abre diálogo com 3 opções
- `SessionManager` ganhou métodos de biometria que sobrevivem ao logout
- `DividaDao`, `CartaoDao`, `ChavePixDao`, `CategoriaDao` ganharam `deletarTodosDoUsuario()`
- Layout `activity_dashboard.xml` ganhou CardView com `LineChart` + chips de período
- Layout `activity_historico.xml` ganhou botão 📅 e botão ✕

### Corrigido
- **BUG-011:** Erro de digitação `.sho w()` no `HistoricoActivity` (linha 548)

---

## [Não lançado] – Sprint 6 (Correções e Refinamentos)

### Adicionado
- **Parcelamento por Mês**
  - Campo `grupoId` na entidade `Divida` para agrupar parcelas de uma mesma compra
  - Cadastro de dívida parcelada agora gera **N registros independentes** (um por mês)
  - Cada parcela tem seu próprio vencimento mensal consecutivo
  - Novas queries no `DividaDao`:
    - `maxGrupoId()` — gera próximo ID de grupo
    - `listarPorGrupo()` — lista parcelas de um grupo
- **Navegação por Mês no Dashboard**
  - Setas ← → no topo do Dashboard para navegar entre meses
  - Badge "Atual" quando o mês selecionado é o corrente
  - Título da lista reflete o mês visualizado
- **Adapter somente leitura no Dashboard**
  - Novo layout `item_divida_dashboard.xml` (sem botões)
  - Novo adapter `DividaDashboardAdapter` (só exibição)
  - `DashboardActivity` não implementa mais `OnDividaActionListener`

### Modificado
- `AppDatabase` atualizado para versão 9 (campo `grupoId` em `Divida`)
- `CadastroDividaActivity.salvarDivida()` agora gera N registros para crédito parcelado
- `DividasActivity.onPagarClick()` agora quita o registro inteiro (não divide mais)
- Dashboard filtra por mês **selecionado** (não mais fixo no atual)
- Dashboard mostra **todas** as dívidas do mês (pago + não pago) no gráfico e na lista
- Layout `activity_dashboard.xml` ganhou barra de navegação de mês

### Corrigido
- **BUG-009:** Bug "Parcela 7/14" nos adapters (Divida, Vencimento, Historico)
  - Novo método `extrairTotalParcelas()` entende o formato "X/Y"
  - Cada registro agora é UMA parcela (não divide mais `valorParcelaReal`)
- **BUG-010:** Pagamento na tela Dívidas somava valor errado
  - Agora quita o registro inteiro em uma única ação
  - Cancela alarme de notificação ao pagar
- Dashboard agora exibe dívidas à vista (Pix/Débito) no mês da compra

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
