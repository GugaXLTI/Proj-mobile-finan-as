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
- **Tela de Histórico por Mês (navegação, filtros, estados)**
- **Soft Delete de dívidas**
- **Exportação CSV do extrato mensal**
- **Exportação PDF do extrato mensal**

### ❌ O que NÃO está incluído (fora do escopo desta Sprint):
- Autenticação em nuvem (Firebase) — planejada para Sprint 7
- Sincronização entre dispositivos
- Biometria real

---

## 3. Ambiente de Teste

| Item | Descrição |
|------|-----------|
| **Dispositivo** | Celular Android físico (preferencial) ou emulador |
| **Versão do Android** | API 24+ (Android 7.0 ou superior) |
| **Conexão** | Wi-Fi ou dados móveis (o app funciona offline) |
| **Build** | Última versão gerada a partir do branch `master` |
| **Pré-requisitos** | App instalado, notificações permitidas, banco limpo (usuário novo) |

---

## 4. Tipos de Testes Realizados

| Tipo | Descrição |
|------|-----------|
| **Teste Funcional** | Verifica se cada funcionalidade faz o que deveria fazer |
| **Teste de Integração** | Verifica se as telas conversam entre si (ex: Cartões ↔ Dívidas) |
| **Teste de Regressão** | Verifica se uma funcionalidade antiga continua funcionando após mudanças |
| **Teste de Usabilidade** | Verifica se o app é fácil e intuitivo de usar |
| **Teste de Borda (Edge Cases)** | Verifica o comportamento em situações extremas (campos vazios, valores altos, etc.) |

---

## 5. Casos de Teste

> **Legenda:** ✅ `[x]` = Passou | ⏳ `[ ]` = Ainda não testado

### 🔐 CT-01: Splash Screen e Sessão

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-01.1 | Splash aparece ao abrir o app | Logo aparece por 2s e vai para o Login | ✅ [x] |
| CT-01.2 | Sessão persistente | Vai direto para a tela Início | ✅ [x] |
| CT-01.3 | Logout limpa a sessão | Volta para o Login | ✅ [x] |

### 👤 CT-02: Cadastro e Login

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-02.1 | Cadastro válido | Cadastro realizado, redireciona para Início | ✅ [x] |
| CT-02.2 | Cadastro com e-mail duplicado | Erro: "E-mail já cadastrado" | ✅ [x] |
| CT-02.3 | Cadastro com senhas diferentes | Erro: "Senhas não coincidem" | ✅ [x] |
| CT-02.4 | Cadastro com nome inválido | Erro: "Nome inválido" | ✅ [x] |
| CT-02.5 | Cadastro com e-mail inválido | Erro: "E-mail inválido" | ✅ [x] |
| CT-02.6 | Login correto | Redireciona para Início | ✅ [x] |
| CT-02.7 | Login incorreto | Erro: "Credenciais inválidas" | ✅ [x] |
| CT-02.8 | Campos vazios no login | Erro de campo obrigatório | ✅ [x] |

### 🏠 CT-03: Tela Início

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-03.1 | Exibe nome do usuário | Nome aparece no topo da tela | ✅ [x] |
| CT-03.2 | Totais corretos | Total pago + a pagar = geral | ✅ [x] |
| CT-03.3 | Próximos vencimentos | Aparece com valor da parcela e progresso | ✅ [x] |
| CT-03.4 | Avatar dinâmico | Avatar mostra a 1ª letra do nome | ✅ [x] |
| CT-03.5 | Card total clicável | Card "Total de Dívidas" abre o Histórico | ⏳ [ ] |

### 💰 CT-04: Cadastro de Dívida

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-04.1 | Cadastro com Cartão de Crédito | Dívida salva com sucesso | ✅ [x] |
| CT-04.2 | Filtro de Crédito | Mostra apenas o de crédito | ✅ [x] |
| CT-04.3 | Filtro de Débito | Esconde Parcelas/Vencimento e mostra débito | ✅ [x] |
| CT-04.4 | Filtro de Pix | Esconde Parcelas/Vencimento e mostra chaves Pix | ✅ [x] |
| CT-04.5 | Empty State | Aparece mensagem + botão "Cadastrar agora" | ✅ [x] |
| CT-04.6 | Cálculo de parcela | Card mostra "Parcela 10x de R$ 200,00" | ✅ [x] |
| CT-04.7 | Máscara de valor | Formata para "R$ 123,45" (com vírgula) | ✅ [x] |
| CT-04.8 | Validação de campos vazios | Toast de erro | ✅ [x] |

### 📋 CT-05: Tela de Dívidas

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-05.1 | Exibe valor da parcela | Card mostra "Parcela 10x de R$ 200,00" | ✅ [x] |
| CT-05.2 | Total correto | Soma dos valores bate com o total | ✅ [x] |
| CT-05.3 | Editar dívida | Abre tela de cadastro preenchida | ✅ [x] |
| CT-05.4 | Pagar parcela | Paga UMA parcela e mostra "Pagar (1/2)" | ✅ [x] |
| CT-05.5 | Excluir dívida | Diálogo de confirmação → dívida some | ✅ [x] |
| CT-05.6 | Cancelar exclusão | Dívida permanece | ✅ [x] |

### 💳 CT-06: Cartões & Chaves Pix

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-06.1 | Abas funcionam | Alterna entre as abas sem travar | ✅ [x] |
| CT-06.2 | Cadastrar cartão de Crédito | Aparece como "Nubank • Crédito" | ✅ [x] |
| CT-06.3 | Cadastrar cartão de Débito | Aparece como "Itaú • Débito" | ✅ [x] |
| CT-06.4 | Cadastrar chave Pix | Aparece na lista com fundo verde | ✅ [x] |
| CT-06.5 | Editar cartão | Campos preenchidos, botão vira "Atualizar" | ✅ [x] |
| CT-06.6 | Cancelar edição | Campos limpam, botão volta ao normal | ✅ [x] |
| CT-06.7 | Atualizar cartão | Dados atualizados na lista | ✅ [x] |
| CT-06.8 | Excluir cartão | Cartão some da lista | ✅ [x] |
| CT-06.9 | Excluir chave Pix | Chave some da lista | ✅ [x] |
| CT-06.10 | Copiar chave Pix | Toast "Chave copiada!" e chave na área de transferência | ✅ [x] |

### 📊 CT-07: Dashboard / Relatórios

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-07.1 | Gráfico de rosca exibe dados | Gráfico mostra as fatias | ✅ [x] |
| CT-07.2 | Percentuais corretos | Total = 100% | ✅ [x] |
| CT-07.3 | Filtros em pílula funcionam | Gráfico filtra pela categoria | ✅ [x] |

### 🏷️ CT-08: Categorias

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-08.1 | Categorias padrão | 8 categorias padrão aparecem | ✅ [x] |
| CT-08.2 | Criar nova categoria | Categoria aparece na lista | ✅ [x] |
| CT-08.3 | Prévia em tempo real | Tag muda enquanto digita | ✅ [x] |
| CT-08.4 | Excluir categoria sem dívidas | Categoria some | ✅ [x] |
| CT-08.5 | Excluir categoria em uso | Bloqueio com aviso de dívidas | ✅ [x] |

### 👤 CT-09: Editar Perfil

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-09.1 | Alterar nome | Nome novo aparece em todas as telas | ✅ [x] |
| CT-09.2 | Alterar e-mail | E-mail atualizado | ✅ [x] |
| CT-09.3 | E-mail duplicado | Erro: "E-mail já cadastrado" | ✅ [x] |
| CT-09.4 | Alterar senha | Senha atualizada | ✅ [x] |
| CT-09.5 | Senha atual errada | Erro: "Senha atual incorreta" | ✅ [x] |
| CT-09.6 | Excluir conta | Volta ao Login, conta apagada | ✅ [x] |

### 🔔 CT-10: Lembretes de Fatura

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-10.1 | Permissão de notificação | Pede permissão de notificação | ✅ [x] |
| CT-10.2 | Notificação agendada | Nada aparece agora (agendado) | ✅ [x] |
| CT-10.3 | Notificação no dia certo | Notificação aparece às 09h | ✅ [x] |
| CT-10.4 | Cancelar ao pagar | Notificação NÃO aparece | ✅ [x] |
| CT-10.5 | Cancelar ao excluir | Notificação NÃO aparece | ✅ [x] |
| CT-10.6 | Switch de lembretes | Novas notificações não são agendadas | ✅ [x] |

### 🔄 CT-11: BootReceiver (Reagendamento)

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-11.1 | Reagenda após reiniciar | Notificação aparece normalmente | ✅ [x] |
| CT-11.2 | Respeita switch desligado | Nenhuma notificação é agendada | ✅ [x] |

### 🔒 CT-12: Isolamento por Usuário

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-12.1 | Cada usuário vê só seus dados | Conta B vê lista vazia | ✅ [x] |
| CT-12.2 | Sessão ativa | Só dados de B aparecem | ✅ [x] |
| CT-12.3 | BootReceiver respeita usuário | Alarme só reagenda quando A logar | ✅ [x] |

### ⚙️ CT-13: Configurações

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-13.1 | Perfil correto | Nome e avatar corretos | ✅ [x] |
| CT-13.2 | Botão "Cartões & Pix" | Abre CartoesActivity | ✅ [x] |
| CT-13.3 | Botão "Categorias" | Abre CategoriasActivity | ✅ [x] |
| CT-13.4 | Botão "Editar Perfil" | Abre EditarPerfilActivity | ✅ [x] |
| CT-13.5 | Botão "Limpar Tudo" | Diálogo de confirmação | ✅ [x] |
| CT-13.6 | Logout | Volta para Login | ✅ [x] |

### 🗑️ CT-14: Soft Delete de Dívidas ⭐ NOVO

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-14.1 | Exclusão some da lista | Dívida excluída some da tela de Dívidas | ⏳ [ ] |
| CT-14.2 | Exclusão não apaga do banco | Dívida excluída aparece no Histórico com ✗ | ⏳ [ ] |
| CT-14.3 | Exclusão cancela alarme | Notificação da dívida excluída não dispara | ⏳ [ ] |
| CT-14.4 | Exclusão não soma no total | Total da tela Início ignora excluídas | ⏳ [ ] |

### 📜 CT-15: Tela de Histórico ⭐ NOVO

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-15.1 | Abrir Histórico pelo card | Toque no card "Total de Dívidas" abre o Histórico | ⏳ [ ] |
| CT-15.2 | Navegação entre meses | Setas ← e → mudam o mês | ⏳ [ ] |
| CT-15.3 | Badge "Atual" | Aparece apenas no mês atual | ⏳ [ ] |
| CT-15.4 | Card de resumo | Total / Já Pago / Falta Pagar corretos | ⏳ [ ] |
| CT-15.5 | Chips de filtro dinâmicos | Chips mostram apenas categorias do mês | ⏳ [ ] |
| CT-15.6 | Filtro por categoria | Chip filtra a lista corretamente | ⏳ [ ] |
| CT-15.7 | Estado Liquidado | Dívida paga aparece com ✓ verde | ⏳ [ ] |
| CT-15.8 | Estado Parcial | Dívida parcialmente paga mostra "Parcela 1/2" | ⏳ [ ] |
| CT-15.9 | Estado Pendente | Dívida não paga aparece com → amarelo | ⏳ [ ] |
| CT-15.10 | Estado Excluída | Dívida excluída aparece com ✗ vermelho e valor riscado | ⏳ [ ] |

### 📤 CT-16: Exportação CSV ⭐ NOVO

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-16.1 | Diálogo de formato | Botão abre diálogo "CSV ou PDF" | ⏳ [ ] |
| CT-16.2 | Gerar CSV | Arquivo `.csv` é gerado com sucesso | ⏳ [ ] |
| CT-16.3 | Compartilhar via WhatsApp | CSV chega no WhatsApp com acentos corretos | ⏳ [ ] |
| CT-16.4 | Abrir no Excel/Sheets | Planilha abre com colunas corretas | ⏳ [ ] |
| CT-16.5 | Exportar só categoria | Escolher uma categoria → CSV só daquela categoria | ⏳ [ ] |
| CT-16.6 | CSV vazio | Sem dívidas no mês → Toast "Nenhum lançamento" | ⏳ [ ] |

### 📋 CT-17: Exportação PDF ⭐ NOVO

| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-17.1 | Gerar PDF | Arquivo `.pdf` é gerado com sucesso | ⏳ [ ] |
| CT-17.2 | Layout do PDF | Cabeçalho "ORG", card resumo e lista | ⏳ [ ] |
| CT-17.3 | Cores por estado | Estados com cores corretas no PDF | ⏳ [ ] |
| CT-17.4 | Compartilhar via WhatsApp | PDF chega no WhatsApp abrível | ⏳ [ ] |
| CT-17.5 | Exportar só categoria | PDF só da categoria selecionada | ⏳ [ ] |
| CT-17.6 | PDF vazio | Sem dívidas → Toast "Nenhum lançamento" | ⏳ [ ] |

---

## 6. Critérios de Aceitação

O app é considerado **aprovado para release** quando:

- ✅ Todos os casos de teste das seções CT-01 a CT-17 estiverem com status "Passou"
- ✅ Nenhum bug de prioridade 🔴 Alta estiver em aberto
- ✅ Os bugs de prioridade 🟡 Média forem documentados no `docs/BUGS.md`
- ✅ O app não travar (crash) em nenhum fluxo testado
- ✅ A navegação entre telas estiver fluida

---

## 7. Como Reportar um Bug

1. **Anote o CT** que falhou
2. **Anote os passos** exatos
3. **Anote o esperado** vs **o que aconteceu**
4. **Tire um print**
5. **Abra um registro no `docs/BUGS.md`**
6. **Marque como 🔴 Aberto**

---

## 8. Fluxo de Teste Recomendado

1. **Teste de Fumaça (Smoke Test)**
2. **Teste Funcional Completo**
3. **Teste de Borda**
4. **Teste de Regressão**
5. **Registrar bugs** no `BUGS.md`
6. **Enviar relatório final**

---

## 9. Como Testar as Notificações (CT-10 e CT-11)

### Passo a Passo

1. **Desative "Data e hora automáticas"** no Android
2. **Confirme que os lembretes estão ativos no app**
3. **Cadastre uma dívida com vencimento em hoje + 4 dias**
4. **Force o fechamento do app**
5. **Mude a data do celular para amanhã, 08:58**
6. **Aguarde 2 minutos** → notificação deve aparecer às 09:00
7. **Restaurar:** reative "Data e hora automáticas"

### Teste do BootReceiver (CT-11)

1. Cadastre dívida com vencimento em **hoje + 5 dias**
2. **Reinicie o celular**
3. Mude a data para **hoje + 2 dias, 08:58**
4. Aguarde 09:00 → notificação deve aparecer

---

## 10. Como Testar o Soft Delete (CT-14)

1. Cadastre uma dívida com categoria "Lazer"
2. Exclua essa dívida na tela de Dívidas
3. ✅ Ela some da lista de Dívidas
4. ✅ Total da tela Início **não** soma mais ela
5. ✅ Abra o Histórico no mês do vencimento
6. ✅ Aparece com status **"Excluída ✗"** em vermelho

---

## 11. Como Testar a Exportação (CT-16 e CT-17)

1. Abra o Histórico
2. Selecione um mês com dívidas
3. Toque em **"Baixar Resumo"**
4. Escolha **CSV** → compartilhe no WhatsApp/Drive
5. Abra o arquivo no Excel/Sheets → confira os dados
6. Volte e escolha **PDF** → compartilhe e abra

---

## 12. Histórico de Execuções

| Data | Testador | Versão | Total | Passou | Falhou | Bugs Abertos |
|------|----------|--------|-------|--------|--------|--------------|
| 03/10/2026 | Israel Malheiros | Sprint 5 | 68 | 60 | 8 | 0 |
| — | Israel Malheiros | Sprint 6 | 21 | — | — | — |

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

---

## 13. Referências

- **`docs/BUGS.md`** — Registro de bugs encontrados
- **`docs/ARQUITETURA.md`** — Documentação técnica do projeto
- **`CHANGELOG.md`** — Histórico de mudanças por Sprint
