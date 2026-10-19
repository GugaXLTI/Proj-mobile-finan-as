# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Sprint 7 (Leitura Automática de Comprovantes - OCR)

### Adicionado
- **Integração com Google ML Kit (Text Recognition)**
  - Dependência `com.google.mlkit:text-recognition:16.0.0` no `build.gradle.kts`
  - Classe `OcrHelper` para processar imagens e extrair texto (on-device)
  - Funciona offline e de forma gratuita
- **Recebimento de Compartilhamento Externo (ACTION_SEND)**
  - Filtros de Intent no `AndroidManifest.xml` para receber `image/*` e `application/pdf`
  - Tela `ComprovanteActivity` criada para receber o arquivo compartilhado
  - Layout `activity_processando_comprovante.xml` com loading e botão cancelar
- **Parsing e Pré-preenchimento Automático**
  - Classe `ComprovanteParser` para interpretar o texto bruto extraído
  - Extração de valor, data, banco e título via Regex e heurística
  - `ComprovanteActivity` processa os dados e redireciona para `CadastroDividaActivity` com os campos preenchidos via `Intent`
- **Adaptação no Cadastro de Dívida**
  - `CadastroDividaActivity` agora aceita `extras` (título, valor, vencimento, banco)
  - Preenchimento automático dos campos ao abrir a tela

### Modificado
- `AndroidManifest.xml` — novos filtros de Intent e Activity de OCR
- `build.gradle.kts` — dependências do ML Kit adicionadas
- `view/CadastroDividaActivity.java` — suporte a pré-preenchimento via OCR

---

## [Não lançado] – Sprint 6 (Refinamentos, Migration e Correções Finais)

### Adicionado
- **Gráfico de Linha Aprimorado** (valores, média, destaque)
- **Agrupamento de Parcelas na Tela Início** (`VencimentoItem`)
- **Alerta Inteligente na Tela Início**
- **Exportação CSV/PDF no Dashboard**
- **Migration Não Destrutiva** (v8 → v9)

### Modificado
- `AppDatabase` mantém versão 9, mas com migration real
- `DashboardActivity` agora exporta CSV/PDF
- Layout `activity_inicio.xml` — card do Xbox único
- `VencimentoAdapter` reescrito

### Corrigido
- BUG-011 (Erro `.sho w()`), BUG-012 (Gráfico olhando só pra trás), BUG-013 (Alerta contando meses futuros)

---

## [Não lançado] – Sprint 6 (Biometria, Gráfico de Linha, Filtro de Período e Backup)

### Adicionado
- **Autenticação Biométrica (BiometricPrompt)**
- **Gráfico de Linha — Evolução Mensal**
- **Filtro de Período Customizado no Histórico**
- **Backup Local (Exportar/Restaurar em JSON)**

### Modificado
- `SessionManager` ganhou métodos de biometria que sobrevivem ao logout
- `DividaDao`, `CartaoDao`, `ChavePixDao`, `CategoriaDao` ganharam `deletarTodosDoUsuario()`
- Layouts atualizados

### Corrigido
- BUG-009 (Bug "Parcela 7/14"), BUG-010 (Pagamento somando errado)

---

## [Não lançado] – Sprint 6 (Correções e Refinamentos)

### Adicionado
- Parcelamento por Mês (campo `grupoId`)
- Navegação por Mês no Dashboard
- Adapter somente leitura no Dashboard

### Modificado
- `AppDatabase` atualizado para versão 9
- `CadastroDividaActivity.salvarDivida()` gera N registros
- `DividasActivity.onPagarClick()` quita o registro inteiro

---

## [Não lançado] – Sprint 6 (Histórico, Soft Delete e Exportação)

### Adicionado
- Soft Delete de Dívidas
- Tela de Histórico por Mês (com 4 estados)
- Exportação CSV e PDF
- FileProvider e Integração na Tela Início

### Modificado
- `AppDatabase` atualizado para versão 8
- Queries filtram dívidas excluídas

---

## [Não lançado] – Sprint 5 (Cartões, Chaves Pix, Integração, BootReceiver, CRUD Completo e Correções)

### Adicionado
- BootReceiver, Tela de Cartões & Chaves Pix, Integração com Lançar, docs/TESTES.md

### Modificado
- `AppDatabase` v6 e v7, Pix/Débito à vista, máscara pt-BR, cards com valor da parcela

### Corrigido
- BUG-002 a BUG-008

---

## [Não lançado] – Sprint 4 (Persistência de Dados)

### Adicionado
- Lembretes, Categorias, Editar Perfil, `usuarioId`, `valorParcela`

### Corrigido
- Problema de arquitetura (isolamento) e BUG-001

---

## [1.0.0] – Sprint 3 (Room + Autenticação) – 2026-09-19

### Adicionado
- Room, Autenticação local, SessionManager

---

## [0.5.0] – Sprint 2 (Telas Navegáveis) – 2026-09-12

### Adicionado
- Bottom Navigation, Início, Cadastro de Dívida, Configurações, Gráfico

---

## [0.3.0] – Sprint 1 (Telas Iniciais) – 2026-09-05

### Adicionado
- Splash, Login, Cadastro, Dashboard, Dívidas, Fontes

---

## [0.1.0] – Kickoff do Projeto – 2026-09-03

### Adicionado
- Estrutura inicial do projeto Android em Java
