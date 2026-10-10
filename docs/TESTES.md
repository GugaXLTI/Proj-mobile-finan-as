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

### ❌ O que NÃO está incluído:
- Autenticação em nuvem (Firebase)
- Sincronização entre dispositivos
- Biometria real

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
| CT-03.5 | Card total clicável | Abre o Histórico | ⏳ [ ] |

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
| CT-14.1 | Exclusão some da lista | Some de Dívidas | ⏳ [ ] |
| CT-14.2 | Exclusão não apaga | Aparece no Histórico ✗ | ⏳ [ ] |
| CT-14.3 | Exclusão cancela alarme | Não dispara | ⏳ [ ] |
| CT-14.4 | Não soma no total | Início ignora | ⏳ [ ] |

### 📜 CT-15: Tela de Histórico

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-15.1 | Abrir pelo card | Abre o Histórico | ⏳ [ ] |
| CT-15.2 | Navegação entre meses | Setas mudam | ⏳ [ ] |
| CT-15.3 | Badge "Atual" | Só no mês atual | ⏳ [ ] |
| CT-15.4 | Card de resumo | Total/Já Pago/Falta | ⏳ [ ] |
| CT-15.5 | Chips dinâmicos | Só categorias do mês | ⏳ [ ] |
| CT-15.6 | Filtro por categoria | Filtra lista | ⏳ [ ] |
| CT-15.7 | Estado Liquidado | ✓ verde | ⏳ [ ] |
| CT-15.8 | Estado Pendente | → amarelo | ⏳ [ ] |
| CT-15.9 | Estado Excluída | ✗ vermelho e riscado | ⏳ [ ] |
| CT-15.10 | Parcela no mês correto | Videogame em 4 meses | ⏳ [ ] |

### 📤 CT-16: Exportação CSV

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-16.1 | Diálogo de formato | "CSV ou PDF" | ⏳ [ ] |
| CT-16.2 | Gerar CSV | Arquivo .csv criado | ⏳ [ ] |
| CT-16.3 | Compartilhar WhatsApp | Chega com acentos | ⏳ [ ] |
| CT-16.4 | Abrir no Excel/Sheets | Colunas corretas | ⏳ [ ] |
| CT-16.5 | Exportar só categoria | Só daquela categoria | ⏳ [ ] |
| CT-16.6 | CSV vazio | Toast | ⏳ [ ] |

### 📋 CT-17: Exportação PDF

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-17.1 | Gerar PDF | Arquivo .pdf criado | ⏳ [ ] |
| CT-17.2 | Layout do PDF | Cabeçalho, resumo, lista | ⏳ [ ] |
| CT-17.3 | Cores por estado | Corretas | ⏳ [ ] |
| CT-17.4 | Compartilhar WhatsApp | PDF abrível | ⏳ [ ] |
| CT-17.5 | Exportar só categoria | Só aquela | ⏳ [ ] |
| CT-17.6 | PDF vazio | Toast | ⏳ [ ] |

### 🎯 CT-18: Parcelamento por Mês

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-18.1 | Gerar N registros | 4x cria 4 registros | ⏳ [ ] |
| CT-18.2 | Vencimentos consecutivos | Out, Nov, Dez, Jan | ⏳ [ ] |
| CT-18.3 | Valor por parcela | Cada registro R$ 750 | ⏳ [ ] |
| CT-18.4 | Pix/Débito sem parcelas | 1 registro pago | ⏳ [ ] |
| CT-18.5 | Número da parcela | "Parcela 1/4", "2/4" | ⏳ [ ] |
| CT-18.6 | Pagamento individual | Cada parcela paga sozinha | ⏳ [ ] |

### 📊 CT-19: Dashboard Mensal e Somente Leitura

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-19.1 | Navegação entre meses | Setas mudam | ⏳ [ ] |
| CT-19.2 | Badge "Atual" | Só no mês atual | ⏳ [ ] |
| CT-19.3 | Filtro por mês | Só do mês | ⏳ [ ] |
| CT-19.4 | Sem botões de ação | Sem Excluir/Editar/Pagar | ⏳ [ ] |
| CT-19.5 | Pix/Débito aparecem | No gráfico do mês | ⏳ [ ] |
| CT-19.6 | Gráfico + Lista consistentes | Totais batem | ⏳ [ ] |

---

## 6. Critérios de Aceitação

O app é considerado **aprovado para release** quando:

- ✅ Todos os CTs de CT-01 a CT-19 estiverem "Passou"
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

---

## 15. Referências

- **`docs/BUGS.md`** — Registro de bugs encontrados
- **`docs/ARQUITETURA.md`** — Documentação técnica do projeto
- **`CHANGELOG.md`** — Histórico de mudanças por Sprint
