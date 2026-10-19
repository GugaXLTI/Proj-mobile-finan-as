# Plano de Testes – App Gestão de Finanças

Documento com o plano de testes, casos de teste e critérios de aceitação do aplicativo. Serve como guia para os testers (membros da equipe) executarem os testes de forma padronizada e reportarem bugs.

---

## 1. Objetivo

Garantir que todas as funcionalidades do aplicativo estejam funcionando conforme o esperado, identificar bugs, validar as integrações entre telas e verificar a experiência do usuário final.

---

## 2. Escopo

### ✅ O que está incluído nos testes:
- Fluxo de autenticação (login, cadastro, logout)
- Cadastro, edição e exclusão de dívidas
- Categorias (criar, listar, excluir)
- Cartões e Chaves Pix (CRUD completo)
- Integração entre Cartões/Pix e Lançar Dívida
- Lembretes de fatura (notificações)
- Reagendamento de alarmes após reinicialização (BootReceiver)
- Edição de perfil
- Relatórios (Dashboard)
- Configurações gerais
- Tela de Histórico por Mês
- Soft Delete de dívidas
- Exportação CSV do extrato mensal
- Exportação PDF do extrato mensal
- Parcelamento por Mês (N registros por compra parcelada)
- Dashboard somente leitura
- **Leitura Automática de Comprovantes (OCR ML Kit)**

### ❌ O que NÃO está incluído:
- Autenticação em nuvem (Firebase)
- Sincronização entre dispositivos
- Biometria real
- Treinamento de IA própria (Orange/TFLite)

---

## 3. Ambiente de Teste

| Item | Descrição |
|------|-----------|
| **Dispositivo** | Celular Android físico |
| **Versão do Android** | API 24+ |
| **Conexão** | Wi-Fi ou dados móveis |
| **Build** | Última versão do branch `master` |
| **Pré-requisitos** | App instalado, notificações permitidas |

---

## 4. Tipos de Testes

| Tipo | Descrição |
|------|-----------|
| **Funcional** | Verifica cada funcionalidade |
| **Integração** | Verifica comunicação entre telas |
| **Regressão** | Verifica funcionalidades antigas |
| **Usabilidade** | Verifica facilidade de uso |
| **Borda** | Situações extremas |

---

## 5. Casos de Teste

> **Legenda:** ✅ `[x]` = Passou | ⏳ `[ ]` = Ainda não testado

### 🔐 CT-01: Splash Screen e Sessão

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-01.1 | Splash aparece ao abrir | Logo aparece por 2s | ✅ [x] |
| CT-01.2 | Sessão persistente | Vai direto para Início | ✅ [x] |
| CT-01.3 | Logout limpa sessão | Volta para Login | ✅ [x] |

### 👤 CT-02: Cadastro e Login

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-02.1 | Cadastro válido | Cadastro realizado | ✅ [x] |
| CT-02.2 | E-mail duplicado | Erro | ✅ [x] |
| CT-02.3 | Senhas diferentes | Erro | ✅ [x] |
| CT-02.4 | Nome inválido | Erro | ✅ [x] |
| CT-02.5 | E-mail inválido | Erro | ✅ [x] |
| CT-02.6 | Login correto | Redireciona | ✅ [x] |
| CT-02.7 | Login incorreto | Erro | ✅ [x] |
| CT-02.8 | Campos vazios | Erro | ✅ [x] |

### 🏠 CT-03: Tela Início

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-03.1 | Exibe nome | Nome no topo | ✅ [x] |
| CT-03.2 | Totais corretos | Pago + a pagar = geral | ✅ [x] |
| CT-03.3 | Próximos vencimentos | Valor da parcela + progresso | ✅ [x] |
| CT-03.4 | Avatar dinâmico | 1ª letra do nome | ✅ [x] |
| CT-03.5 | Card total clicável | Abre o Histórico | ✅ [x] |

### 💰 CT-04: Cadastro de Dívida

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-04.1 | Cartão de Crédito | Dívida salva | ✅ [x] |
| CT-04.2 | Filtro Crédito | Só crédito | ✅ [x] |
| CT-04.3 | Filtro Débito | Esconde Parcelas/Vencimento | ✅ [x] |
| CT-04.4 | Filtro Pix | Esconde Parcelas/Vencimento | ✅ [x] |
| CT-04.5 | Empty State | Botão "Cadastrar agora" | ✅ [x] |
| CT-04.6 | Cálculo de parcela | Card mostra valor da parcela | ✅ [x] |
| CT-04.7 | Máscara de valor | R$ 123,45 com vírgula | ✅ [x] |
| CT-04.8 | Campos vazios | Toast de erro | ✅ [x] |

### 📋 CT-05: Tela de Dívidas

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-05.1 | Exibe valor da parcela | Card mostra valor | ✅ [x] |
| CT-05.2 | Total correto | Soma bate | ✅ [x] |
| CT-05.3 | Editar dívida | Abre cadastro preenchido | ✅ [x] |
| CT-05.4 | Pagar parcela | Paga o registro inteiro | ✅ [x] |
| CT-05.5 | Excluir dívida | Diálogo → some | ✅ [x] |
| CT-05.6 | Cancelar exclusão | Dívida permanece | ✅ [x] |

### 💳 CT-06: Cartões & Chaves Pix

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-06.1 | Abas funcionam | Alterna sem travar | ✅ [x] |
| CT-06.2 | Cadastrar Crédito | "Nubank • Crédito" | ✅ [x] |
| CT-06.3 | Cadastrar Débito | "Itaú • Débito" | ✅ [x] |
| CT-06.4 | Cadastrar Pix | Fundo verde | ✅ [x] |
| CT-06.5 | Editar cartão | Botão vira "Atualizar" | ✅ [x] |
| CT-06.6 | Cancelar edição | Botão volta | ✅ [x] |
| CT-06.7 | Atualizar cartão | Dados atualizados | ✅ [x] |
| CT-06.8 | Excluir cartão | Some da lista | ✅ [x] |
| CT-06.9 | Excluir Pix | Some da lista | ✅ [x] |
| CT-06.10 | Copiar chave | Toast "Chave copiada!" | ✅ [x] |

### 📊 CT-07: Dashboard / Relatórios

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-07.1 | Gráfico exibe dados | Fatias visíveis | ✅ [x] |
| CT-07.2 | Percentuais corretos | Total = 100% | ✅ [x] |
| CT-07.3 | Filtros em pílula | Gráfico filtra | ✅ [x] |

### 🏷️ CT-08: Categorias

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-08.1 | Categorias padrão | 8 categorias | ✅ [x] |
| CT-08.2 | Criar categoria | Aparece na lista | ✅ [x] |
| CT-08.3 | Prévia em tempo real | Tag muda | ✅ [x] |
| CT-08.4 | Excluir sem dívidas | Some | ✅ [x] |
| CT-08.5 | Excluir em uso | Bloqueio com aviso | ✅ [x] |

### 👤 CT-09: Editar Perfil

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-09.1 | Alterar nome | Nome em todas as telas | ✅ [x] |
| CT-09.2 | Alterar e-mail | E-mail atualizado | ✅ [x] |
| CT-09.3 | E-mail duplicado | Erro | ✅ [x] |
| CT-09.4 | Alterar senha | Senha atualizada | ✅ [x] |
| CT-09.5 | Senha errada | Erro | ✅ [x] |
| CT-09.6 | Excluir conta | Volta ao Login | ✅ [x] |

### 🔔 CT-10: Lembretes de Fatura

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-10.1 | Permissão | Pede permissão | ✅ [x] |
| CT-10.2 | Notificação agendada | Agendada | ✅ [x] |
| CT-10.3 | Notificação no dia | Aparece às 09h | ✅ [x] |
| CT-10.4 | Cancelar ao pagar | Não aparece | ✅ [x] |
| CT-10.5 | Cancelar ao excluir | Não aparece | ✅ [x] |
| CT-10.6 | Switch | Novas não são agendadas | ✅ [x] |

### 🔄 CT-11: BootReceiver

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-11.1 | Reagenda após reiniciar | Notificação aparece | ✅ [x] |
| CT-11.2 | Respeita switch | Nada é agendado | ✅ [x] |

### 🔒 CT-12: Isolamento por Usuário

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-12.1 | Cada usuário vê só seus dados | Conta B vê lista vazia | ✅ [x] |
| CT-12.2 | Sessão ativa | Só dados de B | ✅ [x] |
| CT-12.3 | BootReceiver respeita | Alarme só com A logado | ✅ [x] |

### ⚙️ CT-13: Configurações

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-13.1 | Perfil correto | Nome e avatar | ✅ [x] |
| CT-13.2 | Botão Cartões & Pix | Abre tela | ✅ [x] |
| CT-13.3 | Botão Categorias | Abre tela | ✅ [x] |
| CT-13.4 | Botão Editar Perfil | Abre tela | ✅ [x] |
| CT-13.5 | Botão Limpar Tudo | Diálogo | ✅ [x] |
| CT-13.6 | Logout | Volta para Login | ✅ [x] |

### 🗑️ CT-14: Soft Delete de Dívidas

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-14.1 | Exclusão some da lista | Some de Dívidas | ✅ [x] |
| CT-14.2 | Exclusão não apaga | Aparece no Histórico ✗ | ✅ [x] |
| CT-14.3 | Exclusão cancela alarme | Não dispara | ✅ [x] |
| CT-14.4 | Não soma no total | Início ignora | ✅ [x] |

### 📜 CT-15: Tela de Histórico

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-15.1 | Abrir pelo card | Abre o Histórico | ✅ [x] |
| CT-15.2 | Navegação entre meses | Setas mudam | ✅ [x] |
| CT-15.3 | Badge "Atual" | Só no mês atual | ✅ [x] |
| CT-15.4 | Card de resumo | Total/Já Pago/Falta | ✅ [x] |
| CT-15.5 | Chips dinâmicos | Só categorias do mês | ✅ [x] |
| CT-15.6 | Filtro por categoria | Filtra lista | ✅ [x] |
| CT-15.7 | Estado Liquidado | ✓ verde | ✅ [x] |
| CT-15.8 | Estado Pendente | → amarelo | ✅ [x] |
| CT-15.9 | Estado Excluída | ✗ vermelho e riscado | ✅ [x] |
| CT-15.10 | Parcela no mês correto | Videogame em 4 meses | ✅ [x] |

### 📤 CT-16: Exportação CSV

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-16.1 | Diálogo de formato | "CSV ou PDF" | ✅ [x] |
| CT-16.2 | Gerar CSV | Arquivo .csv criado | ✅ [x] |
| CT-16.3 | Compartilhar WhatsApp | Chega com acentos | ✅ [x] |
| CT-16.4 | Abrir no Excel/Sheets | Colunas corretas | ✅ [x] |
| CT-16.5 | Exportar só categoria | Só daquela categoria | ✅ [x] |
| CT-16.6 | CSV vazio | Toast | ✅ [x] |

### 📋 CT-17: Exportação PDF

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-17.1 | Gerar PDF | Arquivo .pdf criado | ✅ [x] |
| CT-17.2 | Layout do PDF | Cabeçalho, resumo, lista | ✅ [x] |
| CT-17.3 | Cores por estado | Corretas | ✅ [x] |
| CT-17.4 | Compartilhar WhatsApp | PDF abrível | ✅ [x] |
| CT-17.5 | Exportar só categoria | Só aquela | ✅ [x] |
| CT-17.6 | PDF vazio | Toast | ✅ [x] |

### 🎯 CT-18: Parcelamento por Mês

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-18.1 | Gerar N registros | 4x cria 4 registros | ✅ [x] |
| CT-18.2 | Vencimentos consecutivos | Out, Nov, Dez, Jan | ✅ [x] |
| CT-18.3 | Valor por parcela | Cada registro R$ 750 | ✅ [x] |
| CT-18.4 | Pix/Débito sem parcelas | 1 registro pago | ✅ [x] |
| CT-18.5 | Número da parcela | "Parcela 1/4", "2/4" | ✅ [x] |
| CT-18.6 | Pagamento individual | Cada parcela paga sozinha | ✅ [x] |

### 📊 CT-19: Dashboard Mensal e Somente Leitura

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-19.1 | Navegação entre meses | Setas mudam | ✅ [x] |
| CT-19.2 | Badge "Atual" | Só no mês atual | ✅ [x] |
| CT-19.3 | Filtro por mês | Só do mês | ✅ [x] |
| CT-19.4 | Sem botões de ação | Sem Excluir/Editar/Pagar | ✅ [x] |
| CT-19.5 | Pix/Débito aparecem | No gráfico do mês | ✅ [x] |
| CT-19.6 | Gráfico + Lista consistentes | Totais batem | ✅ [x] |

---

## 📱 CT-20 a CT-32: Feature de OCR e Leitura de Comprovantes

### 🗄️ CT-20: Migration (v9 → v10)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-20.1 | App abre após update da v9 para v10 | App abre sem crashar | ✅ [x] |
| CT-20.2 | Dívidas antigas preservadas | Dívidas antigas continuam na lista | ✅ [x] |
| CT-20.3 | Categorias antigas preservadas | Categorias antigas continuam funcionando | ✅ [x] |
| CT-20.4 | Cartões e chaves Pix preservados | Dados antigos intactos | ✅ [x] |
| CT-20.5 | Nova tabela `comprovantes_processados` | Cache grava e lê normalmente | ✅ [x] |

### 📥 CT-21: Recebimento e Preparação

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-21.1 | Compartilhar imagem da Galeria | App aparece como "Cadastrar dívida..." | ✅ [x] |
| CT-21.2 | Compartilhar imagem do WhatsApp | App abre a tela de análise | ✅ [x] |
| CT-21.3 | Compartilhar PDF | App processa a 1ª página | ✅ [x] |
| CT-21.4 | Compartilhar imagem de 12 MP | App processa sem travar (bitmap reduzido) | ✅ [x] |
| CT-21.5 | Compartilhar com o app aberto (singleTop)| Activity reaproveitada, sem empilhar | ✅ [x] |
| CT-21.6 | Importar pela aba Lançar | Abre o seletor de arquivos | ✅ [x] |
| CT-21.7 | Selecionar imagem no seletor interno | Botão vira "Analisando...", depois volta | ✅ [x] |
| CT-21.8 | Cancelar seletor de arquivos | Botão volta sem erro | ✅ [x] |

### 🔍 CT-22: OCR (Extração de Texto)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-22.1 | Comprovante Nubank nítido | Texto extraído >300 caracteres | ✅ [x] |
| CT-22.2 | Comprovante com acentos | Acentos preservados | ✅ [x] |
| CT-22.3 | Imagem sem texto (paisagem) | "Nenhum valor encontrado" | ✅ [x] |
| CT-22.4 | Foto com pouca luz | Extrai texto ou erro amigável | ✅ [x] |
| CT-22.5 | PDF (1ª página) | Texto extraído corretamente (Bradesco OK) | ✅ [x] |
| CT-22.6 | Primeira execução do app | ML Kit baixa modelo e processa | ✅ [x] |
| CT-22.7 | Segunda execução (offline) | Processa offline normalmente | ✅ [x] |

### 🧠 CT-23: Parsing (Campos Detectados)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-23.1 | Extrair valor (R$ 20,00) | Campo "Valor: R$ 20,00" | ✅ [x] |
| CT-23.2 | Extrair valor (R$ 159,50) | Campo "Valor: R$ 159,50" | ✅ [x] |
| CT-23.3 | Extrair data "08 OUT 2026" | Campo "Data: 08/10/2026" | ✅ [x] |
| CT-23.4 | Extrair data numérica "18/10/2026" | Campo "Data: 18/10/2026" | ✅ [x] |
| CT-23.5 | Extrair banco Nubank | Campo "Banco: Nubank" | ✅ [x] |
| CT-23.6 | Extrair banco Bradesco (prioridade) | Campo "Banco: Bradesco" mesmo com Nubank | ✅ [x] |
| CT-23.7 | Extrair beneficiário (Pessoa) | Campo "Beneficiário: Adson Taveira..." | ✅ [x] |
| CT-23.8 | Não confundir usuário logado | Nome do usuário NÃO aparece como beneficiário | ✅ [x] |
| CT-23.9 | Não aceitar rótulos como beneficiário | Bloqueia "Informações adicionais", "Dados de..." | ✅ [x] |
| CT-23.10 | Título com texto válido | Título é nome da loja/pessoa (cai no fallback) | ✅ [x] |
| CT-23.11 | Inferir categoria "Transporte" (Uber) | Campo "Categoria: Transporte" | ✅ [x] |
| CT-23.12 | Inferir categoria "Alimentação" (iFood) | Campo "Categoria: Alimentação" | ✅ [x] |
| CT-23.13 | Fallback "Outros" | Campo "Categoria: Outros" | ✅ [x] |
| CT-23.14 | Parcelas sempre 1x em Pix | Campo "Parcelas: 1x" | ✅ [x] |

### 🔄 CT-24: Integração com Cadastro

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-24.1 | Compartilhar → "Confirmar e cadastrar" | CadastroDividaActivity abre preenchida | ✅ [x] |
| CT-24.2 | Importar pela aba Lançar | Campos preenchidos na mesma tela | ✅ [x] |
| CT-24.3 | Tipo de dívida = "Pix" | Spinner em "Pix" | ✅ [x] |
| CT-24.4 | Descrição preenchida | Campo "Descrição" preenchido | ✅ [x] |
| CT-24.5 | Valor preenchido | Campo "Valor" = R$ X,XX | ✅ [x] |
| CT-24.6 | Data preenchida | Campo "Data" preenchido | ✅ [x] |
| CT-24.7 | Vencimento preenchido | Campo "Vencimento" = mesma data | ✅ [x] |
| CT-24.8 | Categoria pré-selecionada | Spinner no item correto | ✅ [x] |
| CT-24.9 | Categoria inexistente | Spinner cai no 1º item | ✅ [x] |
| CT-24.10 | Editar campos antes de salvar | Campo aceita edição | ✅ [x] |
| CT-24.11 | Salvar dívida pré-preenchida | Aparece em "Dívidas" | ✅ [x] |
| CT-24.12 | Salvar com categoria alterada | Dívida salva com nova categoria | ✅ [x] |
| CT-24.13 | Botão "Confirmar" só após parser | Botão só aparece após "Campos detectados" | ✅ [x] |
| CT-24.14 | Toast de boas-vindas | "Comprovante do [Banco] pré-preenchido." | ✅ [x] |
| CT-24.15 | Voltar sem salvar | Nada é salvo, volta sem crash | ✅ [x] |

### 🚨 CT-25: Tratamento de Erros

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-25.1 | Arquivo corrompido | "Não conseguimos abrir esse arquivo..." | ✅ [x] |
| CT-25.2 | Importar arquivo corrompido | Mesma mensagem | ✅ [x] |
| CT-25.3 | Cancelar durante "Analisando..." | Tela fecha, nada é salvo | ✅ [x] |
| CT-25.4 | Cancelar após "Campos detectados" | Tela fecha, nada é salvo | ✅ [x] |
| CT-25.5 | Parser não acha valor/data | Tela abre com campos vazios | ✅ [x] |
| CT-25.6 | Imagem ilegível | "O comprovante está ilegível..." | ✅ [x] |

### 🔄 CT-26: Feedback Visual (Spinners)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-26.1 | Botão "Importar" exibe spinner | Ícone gira + "Analisando..." | ✅ [x] |
| CT-26.2 | Botão fica desabilitado | Não pode tocar 2x | ✅ [x] |
| CT-26.3 | Spinner some após sucesso | Ícone volta a câmera verde | ✅ [x] |
| CT-26.4 | Spinner some após erro | Botão fica habilitado | ✅ [x] |
| CT-26.5 | Spinner some se cancelar | Botão volta ao normal | ✅ [x] |
| CT-26.6 | Botão "Confirmar" exibe spinner | Texto "Abrindo cadastro..." + spinner | ✅ [x] |
| CT-26.7 | Botão "Confirmar" fica desabilitado | Não pode tocar 2x | ✅ [x] |
| CT-26.8 | Spinner não aparece antes do parser | Só aparece após "Campos detectados" | ✅ [x] |

### 💬 CT-27: Mensagens de Erro Melhoradas

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-27.1 | Verificar duração do Toast | Toast permanece ~3,5 segundos | ✅ [x] |
| CT-27.2 | Verificar sugestão "preencha manualmente" | Todas as mensagens terminam com sugestão | ✅ [x] |

### 🏦 CT-28: Bancos e Categorias Ampliados

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-28.1 | "NUBANK S.A." | Banco: Nubank | ✅ [x] |
| CT-28.2 | "BANCO ITAÚ S.A." | Banco: Itaú | ✅ [x] |
| CT-28.3 | "bradesco" (minúsculo) | Banco: Bradesco | ✅ [x] |
| CT-28.4 | Palavra "Pizzaria" | Categoria: Alimentação | ✅ [x] |
| CT-28.5 | Palavra "Bemol" | Categoria: Educação | ✅ [x] |
| CT-28.6 | Palavra "Uber" | Categoria: Transporte | ✅ [x] |
| CT-28.7 | Palavra "Netflix" | Categoria: Lazer | ✅ [x] |

### 💾 CT-29: Cache de OCR (Funcionalidade)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-29.1 | Primeira vez processando imagem | Logcat: `Cache MISS` e OCR roda | ✅ [x] |
| CT-29.2 | Segunda vez com a MESMA imagem (Galeria)| Logcat: `Cache HIT` e OCR não roda | ✅ [x] |
| CT-29.3 | Segunda vez é mais rápida | Campos aparecem em <1 segundo | ✅ [x] |
| CT-29.4 | Cache pela aba Lançar | `MISS` na 1ª, `HIT` na 2ª | ✅ [x] |
| CT-29.5 | Cache pelo compartilhamento externo | `MISS` na 1ª, `HIT` na 2ª | ✅ [x] |
| CT-29.6 | Cache entre origens (Lançar -> Share) | `HIT` | ✅ [x] |
| CT-29.7 | Cache de PDF | PDF também é cacheado (se URI fixa) | ✅ [x] |
| CT-29.8 | Cache sobrevive a fechar/abrir o app | Após reabrir, `HIT` continua | ✅ [x] |
| CT-29.9 | Cache sobrevive a reboot do celular | Após reiniciar, `HIT` continua | ✅ [x] |

### 🔒 CT-30: Cache (Segurança e Isolamento)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-30.1 | Cache não vaza entre usuários | User A não vê cache do User B | ✅ [x] |
| CT-30.2 | Mesma URI, usuários diferentes | Cada user tem seu próprio cache | ✅ [x] |
| CT-30.3 | Texto cacheado não corrompe campos | Campos parsed permanecem corretos | ✅ [x] |

### 🛡️ CT-31: Regressão (Fluxos Antigos)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-31.1 | Cadastro manual de dívida | Funciona como antes | ✅ [x] |
| CT-31.2 | Editar dívida existente | Tela abre com dados | ✅ [x] |
| CT-31.3 | Excluir dívida | Vai para "Excluídas" | ✅ [x] |
| CT-31.4 | Marcar dívida como paga | Dívida vira "Pago" | ✅ [x] |
| CT-31.5 | Adicionar nova categoria | Aparece no spinner | ✅ [x] |
| CT-31.6 | Backup e restauração | Funciona como antes | ✅ [x] |
| CT-31.7 | Login / Logout | Funciona como antes | ✅ [x] |
| CT-31.8 | Modo edição esconde botão importar | Botão não aparece ao editar | ✅ [x] |
| CT-31.9 | Isolamento por usuário | Compartilhar com user A não afeta B | ✅ [x] |

---

## 6. Critérios de Aceitação

O app é considerado **aprovado para release** quando:

- ✅ Todos os CTs de CT-01 a CT-31 estiverem "Passou"
- ✅ Nenhum bug 🔴 Alta em aberto
- ✅ Bugs 🟡 Média documentados no `docs/BUGS.md`
- ✅ App não travar em nenhum fluxo testado
- ✅ Navegação fluida entre telas

---

## 7. Como Reportar um Bug

1. Anote o **CT** que falhou
2. Anote os **passos** exatos
3. Anote o **esperado** vs **o que aconteceu**
4. Tire um **print**
5. Abra um registro no `docs/BUGS.md`
6. Marque como 🔴 **Aberto**

---

## 8. Fluxo de Teste Recomendado

1. **Teste de Fumaça**
2. **Funcional Completo**
3. **Borda**
4. **Regressão**
5. **Registrar bugs** no `BUGS.md`
6. **Relatório final**

---

## 9. Como Testar as Notificações

1. **Desative "Data e hora automáticas"**
2. **Confirme lembretes ativos no app**
3. **Cadastre dívida com vencimento em hoje + 4 dias**
4. **Force fechamento do app**
5. **Mude data para amanhã, 08:58**
6. **Aguarde 2 minutos** → notificação às 09:00
7. **Restaurar:** reative "Data e hora automáticas"

### BootReceiver

1. Cadastre dívida com vencimento em **hoje + 5 dias**
2. **Reinicie o celular**
3. Mude data para **hoje + 2 dias, 08:58**
4. Aguarde 09:00 → notificação aparece

---

## 10. Como Testar o Soft Delete

1. Cadastre dívida com categoria "Lazer"
2. Exclua na tela Dívidas
3. ✅ Some da lista de Dívidas
4. ✅ Total do Início **não** soma mais
5. ✅ Histórico do mês: **"Excluída ✗"** vermelho

---

## 11. Como Testar a Exportação

1. Abra o Histórico em um mês com dívidas
2. Toque em "Baixar Resumo"
3. Escolha **CSV** → compartilhe
4. Abra no Excel/Sheets
5. Volte e escolha **PDF** → compartilhe e abra

---

## 12. Como Testar o Parcelamento

1. Cadastre **Videogame R$ 3.000 em 4x** com 1º vencimento em Novembro
2. ✅ 4 registros criados no banco
3. Tela **Dívidas**: 4 parcelas de R$ 750
4. **Dashboard** Novembro: Videogame 1/4
5. **Dashboard** Dezembro: Videogame 2/4
6. Pague 1/4 em Dívidas → botão vira "Pago"

---

## 13. Como Testar o Dashboard Somente Leitura

1. Abra o Dashboard
2. ✅ Cards **sem botões** Excluir/Editar/Pagar
3. ✅ Setas ← → funcionam
4. ✅ Filtros por categoria funcionam
5. Para pagar, vá em **Dívidas**

---

## 14. Histórico de Execuções

| Data | Testador | Versão | Total | Passou | Falhou | Bugs Abertos |
|------|----------|--------|-------|--------|--------|--------------|
| 03/10/2026 | Israel Malheiros | Sprint 5 | 68 | 60 | 8 | 0 |
| 07/10/2026 | Israel Malheiros | Sprint 6 | 33 | — | — | — |
| **17/10/2026** | **Israel Malheiros** | **Sprint 7 (OCR)** | **103** | **103** | **0** | **0** |

### Bugs Encontrados e Corrigidos

| Bug | CT | Descrição | Status |
|-----|-----|-----------|--------|
| BUG-001 | CT-05.1 | Valor da parcela não dividido | ✅ Corrigido |
| BUG-002 | CT-04.4 | Pix/Débito pediam parcelas/vencimento | ✅ Corrigido |
| BUG-003 | CT-04.7 | Máscara de valor não formatava | ✅ Corrigido |
| BUG-004 | CT-04.6 | Card sem valor da parcela | ✅ Corrigido |
| BUG-005 | CT-03.3 | Vencimentos mostravam valor total | ✅ Corrigido |
| BUG-006 | CT-06.1 | Lançar sem barra de navegação | ✅ Corrigido |
| BUG-007 | CT-08.4/08.5 | Exclusão de categorias em uso | ✅ Corrigido |
| BUG-008 | CT-09.1/09.6 | Erro ao carregar dados do usuário | ✅ Corrigido |
| BUG-009 | CT-18.x | Bug "Parcela 7/14" nos adapters | ✅ Corrigido |
| BUG-010 | CT-05.4 | Pagamento somava valor errado | ✅ Corrigido |
| **BUG-011** | **CT-23.1** | **OCR não lia valor se tivesse espaços diferentes** | ✅ Corrigido |
| **BUG-012** | **CT-22.3** | **Parser tentava ler foto de paisagem** | ✅ Corrigido |
| **BUG-013** | **CT-23.6** | **Parser pegava banco errado (Nubank vs Bradesco)** | ✅ Corrigido |
| **BUG-014** | **CT-23.4** | **Parser não lia data no formato DD/MM/AAAA** | ✅ Corrigido |
| **BUG-015** | **CT-23.9** | **Título/Beneficiário pegava rótulos do banco** | ✅ Corrigido |
| **BUG-016** | **CT-29.7** | **Cache falhava em PDFs com URI dinâmica** | ✅ Corrigido |

---

## 15. Referências

- **`docs/BUGS.md`** — Registro de bugs encontrados
- **`docs/ARQUITETURA.md`** — Documentação técnica do projeto
- **`CHANGELOG.md`** — Histórico de mudanças por Sprint
