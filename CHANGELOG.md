# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 6 (Refinamentos, Migration e Correções Finais)

### Adicionado
- **Gráfico de Linha Aprimorado**
  - Valores em cima de cada ponto (ex: R$ 750, R$ 500)
  - Eixo Y com labels numéricos (R$ 0, R$ 500, R$ 1k)
  - Ponto do maior gasto destacado em roxo
  - Linha de média mensal tracejada em amarelo
  - Preenchimento com gradiente verde (topo → transparente)
  - Drawable `bg_line_chart_gradient.xml`
  - Animação suave ao carregar
  - Texto do topo mostra total + média
- **Agrupamento de Parcelas na Tela Início**
  - Novo modelo `VencimentoItem`
  - Adapter `VencimentoAdapter` reescrito
  - Parcelas do mesmo `grupoId` agrupadas em 1 card
  - Exibe "X parcelas restantes" + próxima + total restante
- **Alerta Inteligente na Tela Início**
  - Conta apenas dívidas que vencem **no mês atual**
  - Mensagem positiva "Nenhuma conta vence este mês ✓"
- **Exportação CSV/PDF no Dashboard**
  - Botão "Exportar Detalhamento" agora abre diálogo CSV/PDF
  - Exporta apenas dívidas do mês selecionado
  - Se houver filtro de categoria, adiciona ao nome do arquivo
- **Migration Não Destrutiva**
  - Classe `Migrations.java` com migração v8 → v9 (campo `grupoId`)
  - Registrada em `AppDatabase` via `.addMigrations()`
  - Fallback destrutivo mantido apenas como rede de segurança
  - A partir de agora, atualizações do banco preservam dados

### Modificado
- `AppDatabase` mantém versão 9, mas com migration real
- `DashboardActivity` agora exporta CSV/PDF (antes era "Em breve")
- Layout `activity_inicio.xml` — card do Xbox agora é único
- `VencimentoAdapter` — reescrito para receber `VencimentoItem`

### Corrigido
- **BUG-011:** Erro de digitação `.sho w()` no `HistoricoActivity` (linha 548)
- **BUG-012:** Gráfico de linha olhava apenas para trás
  - Agora mostra N/2 meses passados + mês atual + N/2 meses futuros
  - Mês selecionado fica centralizado no gráfico
  - Dívidas futuras (parcelas) aparecem corretamente
- **BUG-013:** Alerta "X faturas somando R$ Y" contava parcelas futuras
  - Agora conta apenas o que vence no mês atual

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
  - Suporte a digital, face e íris (dependendo do hardware)
- **Gráfico de Linha — Evolução Mensal**
  - Novo `LineChart` no Dashboard com estilo escuro
  - Chips de período: **3M / 6M / 12M**
  - Considera apenas dívidas **não excluídas** (pagas + não pagas)
- **Filtro de Período Customizado no Histórico**
  - Botão **📅** na barra de navegação do Histórico
  - Dois `DatePicker`s em sequência (data inicial + data final)
  - Botão **✕** vermelho para limpar o filtro
  - Título muda para "01/09/2026 a 30/09/2026" quando filtro ativo
  - Validação: data final deve ser ≥ data inicial
- **Backup Local (Exportar/Restaurar em JSON)**
  - Dependência `com.google.code.gson:gson:2.10.1` no `build.gradle.kts`
  - POJO `BackupData` agrupa todos os dados do usuário
  - Classe `BackupHelper` com métodos `exportar()`, `lerArquivo()` e `aplicarBackup()`
  - Método `deletarTodosDoUsuario()` adicionado nos 4 DAOs
  - Diálogo com 3 opções: Exportar / Restaurar / Limpar Tudo
  - Arquivo JSON com data/hora no nome
  - Compartilhamento via FileProvider (Drive, WhatsApp, etc)
  - Validação do campo `versao` antes de importar
  - Reagenda automaticamente alarmes das dívidas não pagas após restaurar

### Modificado
- `SessionManager` ganhou métodos de biometria que sobrevivem ao logout
- `DividaDao`, `CartaoDao`, `ChavePixDao`, `CategoriaDao` ganharam `deletarTodosDoUsuario()`
- Layout `activity_dashboard.xml` ganhou CardView com `LineChart` + chips
- Layout `activity_historico.xml` ganhou botão 📅 e botão ✕

### Corrigido
- **BUG-009:** Bug "Parcela 7/14" nos adapters (Divida, Vencimento, Historico)
  - Novo método `extrairTotalParcelas()` entende o formato "X/Y"
  - Cada registro agora é UMA parcela (não divide mais `valorParcelaReal`)
- **BUG-010:** Pagamento na tela Dívidas somava valor errado
  - Agora quita o registro inteiro em uma única ação
  - Cancela alarme de notificação ao pagar
- Dashboard agora exibe dívidas à vista (Pix/Débito) no mês da compra

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
- **Adapter somente leitura no Dashboard**

### Modificado
- `AppDatabase` atualizado para versão 9 (campo `grupoId` em `Divida`)
- `CadastroDividaActivity.salvarDivida()` agora gera N registros para crédito parcelado
- `DividasActivity.onPagarClick()` agora quita o registro inteiro
- Dashboard filtra por mês **selecionado** (não mais fixo no atual)

---

## [Não lançado] – Sprint 6 (Histórico, Soft Delete e Exportação)

### Adicionado
- **Soft Delete de Dívidas** (campo `excluida`)
- **Tela de Histórico por Mês** com 4 estados (Liquidado, Parcial, Pendente, Excluída)
- **Exportação CSV** (BOM UTF-8, escape de campos)
- **Exportação PDF** (iTextG, cabeçalho "ORG", resumo + lista)
- **Diálogo de escolha de formato** (CSV/PDF)
- **FileProvider** (`file_paths.xml` + registro no Manifest)
- **Integração da Tela Início** (card de total clicável)

### Modificado
- `AppDatabase` atualizado para versão 8 (campo `excluida` em `Divida`)
- Queries do `DividaDao` ignoram dívidas excluídas
- `DividasActivity` — soft delete
- `DashboardActivity` — soft delete

---

## [Não lançado] – Sprint 5 (Cartões, Chaves Pix, Integração, BootReceiver, CRUD Completo e Correções)

### Adicionado
- **Sistema de BootReceiver (Reagendamento de Alarmes)**
- **Tela de Cartões & Chaves Pix** com navegação por abas
- **Integração da tela Lançar Dívida com Cartões e Pix**
- **Documentação de Testes** (`docs/TESTES.md`)
- **Spinner de categoria com bolinha colorida**
- **Barra de navegação inferior na tela Lançar**

### Modificado
- `AppDatabase` v6 e v7 (Cartões, Chaves Pix, campo `tipo`)
- Pix e Cartão de Débito tratados como à vista
- Máscara de valor força locale pt-BR
- Cards de dívidas exibem valor da parcela e progresso

### Corrigido
- BUG-002 a BUG-008 (ver lista completa nas versões anteriores do CHANGELOG)

---

## [Não lançado] – Sprint 4 (Persistência de Dados)

### Adicionado
- **Sistema de Lembretes de Fatura (Notificações)**
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
