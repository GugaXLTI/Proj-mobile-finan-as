# Changelog

Todas as mudanças importantes do projeto serão documentadas neste arquivo.

O formato é baseado em [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/).

---

## [Não lançado] – Redesign Visual (Início e Lançar)

### Adicionado
- **Novo padrão visual dark mode fixo** aplicado às telas Início e Lançar:
  - Fundo azul-marinho com brilho verde/azul (`bg_gradient_inicio` reescrito como `layer-list` com dois brilhos radiais)
  - Cards arredondados (`bg_card_glass`, `bg_card_atencao`)
  - Verde esmeralda de destaque e âmbar para alertas
- **Barra inferior reutilizável**
  - `res/layout/layout_bottom_nav.xml` — 5 abas (`tabInicio`, `tabLancar`, `tabDividas`, `tabRelatorios`, `tabConfig`), cada uma um `LinearLayout` com `ImageView` e `TextView`
  - `utils/BottomNavHelper.java` — marca a aba ativa (ícone + texto em pílula verde; inativas só ícone) e faz a navegação via `startActivity` + `finish`
  - Uso padronizado: `<include layout="@layout/layout_bottom_nav" />` + `BottomNavHelper.configure(this, BottomNavHelper.X)`
- **Redesign da tela Início** (`activity_inicio.xml` + `InicioActivity.java`)
  - Cabeçalho com avatar e badge "Sincronizado"
  - Card de saldo disponível com botão de ocultar valores
  - Barra de orçamento
  - Cards "Dívidas em aberto" e "Total já pago"
  - Card "Atenção este mês" (contas que vencem no mês, "Vence em N dias", total a pagar, % do salário)
  - Barra inferior flutuante
- **Redesign da tela Lançar / Cadastro de Dívida** (`activity_cadastro_divida.xml` + `CadastroDividaActivity.java`)
  - Botão voltar redondo, título "Lançar nova dívida"
  - Valor total em card grande com chips de parcelas (1x, 2x, 3x, 6x, 12x, Outro) e resumo dinâmico ("Nx de R$ X · à vista/parcelado")
  - Tipo de dívida e Categoria como chips (categoria com bolinha colorida), substituindo os `Spinner`
  - Seleção de cartão / chave Pix / banco em lista horizontal de cards + card tracejado "Adicionar"
  - Campos de data com ícone de calendário
  - Botão "Preencher com comprovante" tracejado
  - Barra inferior via `<include>` + `BottomNavHelper`
- **Novos drawables (redesign)**
  - `bg_lancar_campo.xml`, `bg_lancar_botao_voltar.xml`, `bg_lancar_comprovante.xml`, `bg_lancar_adicionar.xml`, `bg_lancar_sigla.xml`
  - `bg_data_card.xml`, `bg_badge_atencao.xml`, `bg_tab_ativo.xml` (reescritos)
  - `bg_card_glass.xml`, `bg_card_atencao.xml`, `bg_icone_alerta.xml`, `bg_nav_pill.xml`, `bg_dot_vermelho.xml`, `bg_dot_verde.xml`, `bg_progress_atencao.xml`
- **Novos ícones vetoriais**
  - `ic_lancar_voltar.xml`, `ic_lancar_camera.xml`, `ic_lancar_calendario.xml`, `ic_lancar_mais.xml`
  - `ic_nav_home.xml`, `ic_nav_add.xml`, `ic_nav_card.xml`, `ic_nav_chart.xml`, `ic_nav_settings.xml`
- **Novo item de lista**
  - `res/layout/item_conta_atencao.xml` — card "Atenção este mês" da Início

### Modificado
- `activity_inicio.xml` e `InicioActivity.java` — reescritos com o novo visual; `rvVencimentos` (RecyclerView) removido da tela
- `activity_cadastro_divida.xml` — reescrito do zero
  - IDs removidos: `spinnerTipoDivida`, `spinnerCategoria`, `spinnerParcelas`, `bottomNavigation` (agora via `<include>`), `TextView`s de abas e `MaterialButton btnImportarComprovante`
  - IDs novos: `chipGroupTipo`, `chipGroupParcelas`, `chipGroupCategoria`, `btnVoltar`, `tvTituloTela`, `tvSubtituloTela`, `tvResumoParcelas`, `ivComprovante`, `tvComprovante`, `containerListaSelecao`, `btnAdicionarItem`, `boxDataCompra`, `boxVencimento`, `tvSemCategoria`
  - `btnImportarComprovante` passou de `MaterialButton` para `LinearLayout`
- `CadastroDividaActivity.java` — reescrita completa casada com o XML novo (chips programáticos, resumo de parcelas, diálogo "Outro", `BottomNavHelper`, `onResume` recarregando categorias/cartões)
- `bg_gradient_inicio.xml` — agora `layer-list` com dois brilhos radiais
- IDs `tabLancar`, `tabDividas`, `tabRelatorios`, `tabConfig` mudaram de `TextView` para `ImageView`; `btnVerTodosVencimentos` mudou de `Button` para `TextView`

### Corrigido
- Editar uma parcela não apaga mais o identificador "i/N" (antes era sobrescrito por "1x (À vista)")
- Na edição, `valorParcela` acompanha o valor (e `valorPago` também, se a dívida já estiver paga)
- Na edição, o app tenta reconhecer tipo/cartão/chave pelo campo `banco`; se não reconhecer, preserva o banco original (antes trocava pelo primeiro cartão da lista)
- Voltar de Cartões/Categorias recarrega a tela mantendo a seleção escolhida
- `DatePickerDialog` abre na data já digitada (não fixa mais em "hoje")

### Removido
- `RecyclerView rvVencimentos` da tela Início (`VencimentoAdapter` e `VencimentoItem` continuam no projeto, mas sem uso nessa tela)

---

## [Não lançado] – Sprint 7 (Leitura Automática de Comprovantes - OCR)

### Adicionado
- **Integração com Google ML Kit (Text Recognition)**
  - Dependência `com.google.mlkit:text-recognition:16.0.0` no `build.gradle.kts`
  - Classe `utils/ReceiptOcr.java` para processar imagens e extrair texto (on-device)
  - Funciona offline e de forma gratuita
- **Recebimento de Compartilhamento Externo (ACTION_SEND)**
  - Filtros de Intent no `AndroidManifest.xml` para receber `image/*` e `application/pdf`
  - Tela `view/ReceiptShareActivity.java` criada para receber o arquivo compartilhado
  - Layout `res/layout/activity_receipt_share.xml` com loading e botão cancelar
- **Parsing e Pré-preenchimento Automático**
  - Classe `utils/ReceiptParser.java` para interpretar o texto bruto extraído
  - Extração de valor, data, banco e título via Regex e heurística
  - `ReceiptShareActivity` processa os dados e redireciona para `CadastroDividaActivity` com os campos preenchidos via `Intent`
- **Adaptação no Cadastro de Dívida**
  - `CadastroDividaActivity` agora aceita `extras` (título, valor, vencimento, banco)
  - Preenchimento automático dos campos ao abrir a tela
- **Modelos de suporte ao OCR**
  - `model/ComprovanteProcessado.java`, `model/ParsedReceipt.java` e `dao/ComprovanteProcessadoDao.java`

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
