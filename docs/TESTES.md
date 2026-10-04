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

### ❌ O que NÃO está incluído (fora do escopo desta Sprint):
- Autenticação em nuvem (Firebase) — planejada para Sprint 6
- Sincronização entre dispositivos
- Biometria real
- Exportação de dados (PDF/CSV)

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

> **Como usar:** Siga os passos na ordem, marque ✅ se passar ou ❌ se falhar. Se falhar, abra um BUG no `docs/BUGS.md`.

### 🔐 CT-01: Splash Screen e Sessão

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-01.1 | Splash aparece ao abrir o app | Abrir o app com conta deslogada | Logo aparece por 2s e vai para o Login | [ ] |
| CT-01.2 | Sessão persistente | Fazer login → fechar o app → abrir de novo | Vai direto para a tela Início (sem pedir login) | [ ] |
| CT-01.3 | Logout limpa a sessão | Fazer logout → fechar o app → abrir | Volta para o Login | [ ] |

### 👤 CT-02: Cadastro e Login

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-02.1 | Cadastro válido | Preencher nome, e-mail válido e senhas iguais | Cadastro realizado, redireciona para Início | [ ] |
| CT-02.2 | Cadastro com e-mail duplicado | Tentar cadastrar com e-mail já existente | Erro: "E-mail já cadastrado" | [ ] |
| CT-02.3 | Cadastro com senhas diferentes | Digitar senhas diferentes | Erro: "Senhas não coincidem" | [ ] |
| CT-02.4 | Cadastro com nome inválido | Digitar "A" ou "123" no nome | Erro: "Nome inválido" | [ ] |
| CT-02.5 | Cadastro com e-mail inválido | Digitar "teste" no e-mail | Erro: "E-mail inválido" | [ ] |
| CT-02.6 | Login correto | E-mail + senha corretos | Redireciona para Início | [ ] |
| CT-02.7 | Login incorreto | E-mail válido + senha errada | Erro: "Credenciais inválidas" | [ ] |
| CT-02.8 | Campos vazios no login | Clicar em Entrar com campos vazios | Erro de campo obrigatório | [ ] |

### 🏠 CT-03: Tela Início

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-03.1 | Exibe nome do usuário | Fazer login | Nome aparece no topo da tela | [ ] |
| CT-03.2 | Totais corretos | Cadastrar 2 dívidas, verificar totais | Total pago + a pagar = geral | [ ] |
| CT-03.3 | Próximos vencimentos | Cadastrar dívida com vencimento próximo | Aparece na lista com valor da parcela e progresso | [ ] |
| CT-03.4 | Avatar dinâmico | Verificar avatar com inicial do nome | Avatar mostra a 1ª letra do nome em maiúscula | [ ] |

### 💰 CT-04: Cadastro de Dívida (com Integração)

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-04.1 | Cadastro com Cartão de Crédito | Escolher "Cartão de Crédito" → selecionar cartão → preencher | Dívida salva com sucesso | [ ] |
| CT-04.2 | Filtro de Crédito | Ter 1 cartão de crédito e 1 de débito. Escolher "Cartão de Crédito" | Mostra apenas o de crédito | [ ] |
| CT-04.3 | Filtro de Débito | Escolher "Cartão de Débito" | Esconde Parcelas/Vencimento e mostra apenas débito | [ ] |
| CT-04.4 | Filtro de Pix | Escolher "Pix" | Esconde Parcelas/Vencimento e mostra chaves Pix | [ ] |
| CT-04.5 | Empty State | Apagar todos os cartões → escolher "Cartão de Crédito" | Aparece mensagem + botão "Cadastrar agora" | [ ] |
| CT-04.6 | Cálculo de parcela | Cadastrar R$ 2.000 em 10x | Card mostra "Parcela 10x de R$ 200,00" | [ ] |
| CT-04.7 | Máscara de valor | Digitar "12345" no campo valor | Formata para "R$ 123,45" (com vírgula) | [ ] |
| CT-04.8 | Validação de campos vazios | Tentar salvar sem preencher | Toast de erro | [ ] |

### 📋 CT-05: Tela de Dívidas

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-05.1 | Exibe valor da parcela | Cadastrar dívida em 10x | Card mostra "Parcela 10x de R$ 200,00" | [ ] |
| CT-05.2 | Total correto | Cadastrar 2 dívidas | Soma dos valores bate com o total | [ ] |
| CT-05.3 | Editar dívida | Clicar em "Editar" em uma dívida | Abre tela de cadastro preenchida | [ ] |
| CT-05.4 | Pagar parcela | Clicar em "Pagar" em dívida 2x | Paga UMA parcela e mostra "Pagar (1/2)" | [ ] |
| CT-05.5 | Excluir dívida | Clicar em "Excluir" | Diálogo de confirmação → dívida some | [ ] |
| CT-05.6 | Cancelar exclusão | Clicar em "Excluir" → "Cancelar" | Dívida permanece | [ ] |

### 💳 CT-06: Cartões & Chaves Pix

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-06.1 | Abas funcionam | Tocar em "Cartão Crédito/Débito" e "Chave Pix" | Alterna entre as abas sem travar | [ ] |
| CT-06.2 | Cadastrar cartão de Crédito | Preencher todos os campos | Aparece na lista como "Nubank • Crédito" | [ ] |
| CT-06.3 | Cadastrar cartão de Débito | Escolher "Débito" | Aparece na lista como "Itaú • Débito" | [ ] |
| CT-06.4 | Cadastrar chave Pix | Preencher todos os campos | Aparece na lista com fundo verde | [ ] |
| CT-06.5 | Editar cartão | **Clique normal** em um cartão | Campos do form preenchidos, botão vira "Atualizar" | [ ] |
| CT-06.6 | Cancelar edição | Clicar em "Cancelar edição" | Campos limpam, botão volta ao normal | [ ] |
| CT-06.7 | Atualizar cartão | Editar → salvar | Dados atualizados na lista | [ ] |
| CT-06.8 | Excluir cartão | **Clique longo** em um cartão → confirmar | Cartão some da lista | [ ] |
| CT-06.9 | Excluir chave Pix | **Clique longo** em uma chave → confirmar | Chave some da lista | [ ] |
| CT-06.10 | Copiar chave Pix | Clicar em "Copiar Chave" | Toast "Chave copiada!" e chave na área de transferência | [ ] |

### 📊 CT-07: Dashboard / Relatórios

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-07.1 | Gráfico de rosca exibe dados | Cadastrar dívidas em categorias diferentes | Gráfico mostra as fatias | [ ] |
| CT-07.2 | Percentuais corretos | Somar fatias | Total = 100% | [ ] |
| CT-07.3 | Filtros em pílula funcionam | Clicar em uma pílula | Gráfico filtra pela categoria | [ ] |

### 🏷️ CT-08: Categorias

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-08.1 | Categorias padrão | Abrir Categorias pela 1ª vez | 8 categorias padrão aparecem | [ ] |
| CT-08.2 | Criar nova categoria | Digitar nome + escolher cor | Categoria aparece na lista | [ ] |
| CT-08.3 | Prévia em tempo real | Digitar nome | Tag muda enquanto digita | [ ] |
| CT-08.4 | Excluir categoria sem dívidas | Clicar em excluir em categoria sem uso → confirmar | Categoria some | [ ] |
| CT-08.5 | Excluir categoria em uso | Tentar excluir categoria com dívida | Bloqueio: "Não é possível excluir — X dívidas usando" | [ ] |

### 👤 CT-09: Editar Perfil

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-09.1 | Alterar nome | Mudar o nome → salvar | Nome novo aparece em todas as telas | [ ] |
| CT-09.2 | Alterar e-mail | Mudar para e-mail novo | E-mail atualizado | [ ] |
| CT-09.3 | E-mail duplicado | Tentar mudar para e-mail de outro usuário | Erro: "E-mail já cadastrado" | [ ] |
| CT-09.4 | Alterar senha | Informar senha atual + nova | Senha atualizada | [ ] |
| CT-09.5 | Senha atual errada | Informar senha atual incorreta | Erro: "Senha atual incorreta" | [ ] |
| CT-09.6 | Excluir conta | Clicar em "Excluir Conta" → confirmar | Volta ao Login, conta apagada | [ ] |

### 🔔 CT-10: Lembretes de Fatura

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-10.1 | Permissão de notificação | Abrir app pela 1ª vez no Android 13+ | Pede permissão de notificação | [ ] |
| CT-10.2 | Notificação agendada | Cadastrar dívida com vencimento +5 dias | Nada aparece agora (agendado) | [ ] |
| CT-10.3 | Notificação no dia certo | Mudar data do celular para -3 dias antes, 08h59 | Notificação aparece às 09h | [ ] |
| CT-10.4 | Cancelar ao pagar | Pagar dívida antes do lembrete | Notificação NÃO aparece | [ ] |
| CT-10.5 | Cancelar ao excluir | Excluir dívida antes do lembrete | Notificação NÃO aparece | [ ] |
| CT-10.6 | Switch de lembretes | Desligar switch em Configurações | Novas notificações não são agendadas | [ ] |

### 🔄 CT-11: BootReceiver (Reagendamento)

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-11.1 | Reagenda após reiniciar | Cadastrar dívida → reiniciar celular → mudar data para -3 dias | Notificação aparece normalmente | [ ] |
| CT-11.2 | Respeita switch desligado | Desligar switch → reiniciar celular → cadastrar dívida | Nenhuma notificação é agendada | [ ] |

### 🔒 CT-12: Isolamento por Usuário

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-12.1 | Cada usuário vê só seus dados | Criar conta A → cadastrar dívida → logout → conta B | Conta B vê lista vazia | [ ] |
| CT-12.2 | Sessão ativa | Logar como A → logout → logar como B | Só dados de B aparecem | [ ] |
| CT-12.3 | BootReceiver respeita usuário | Logar como A, cadastrar dívida, logout, reiniciar | Alarme só reagenda quando A logar | [ ] |

### ⚙️ CT-13: Configurações

| ID | Descrição | Passos | Resultado Esperado | Status |
|----|-----------|--------|---------------------|--------|
| CT-13.1 | Perfil correto | Abrir Configurações | Nome e avatar corretos | [ ] |
| CT-13.2 | Botão "Cartões & Pix" | Tocar em "Meus Cartões & Bancos" | Abre CartoesActivity | [ ] |
| CT-13.3 | Botão "Categorias" | Tocar em "Categorias de Dívida" | Abre CategoriasActivity | [ ] |
| CT-13.4 | Botão "Editar Perfil" | Tocar em "Editar" no card do usuário | Abre EditarPerfilActivity | [ ] |
| CT-13.5 | Botão "Limpar Tudo" | Tocar em "Backup dos Dados" | Diálogo de confirmação (testes internos) | [ ] |
| CT-13.6 | Logout | Tocar em "Desconectar" → confirmar | Volta para Login | [ ] |

---

## 6. Critérios de Aceitação

O app é considerado **aprovado para release** quando:

- ✅ Todos os casos de teste das seções CT-01 a CT-13 estiverem com status "Passou"
- ✅ Nenhum bug de prioridade 🔴 Alta estiver em aberto
- ✅ Os bugs de prioridade 🟡 Média forem documentados no `docs/BUGS.md`
- ✅ O app não travar (crash) em nenhum fluxo testado
- ✅ A navegação entre telas estiver fluida

---

## 7. Como Reportar um Bug

Ao encontrar um problema durante os testes:

1. **Anote o CT (Caso de Teste)** que falhou (ex: CT-04.2)
2. **Anote os passos** exatos que você fez
3. **Anote o resultado esperado** e **o que aconteceu de fato**
4. **Tire um print** da tela
5. **Abra um registro no `docs/BUGS.md`** com:
   - Um ID novo (ex: BUG-002)
   - Data, Sprint, Tela, Prioridade
   - Descrição, passos, comportamento esperado, comportamento atual
6. **Marque como 🔴 Aberto** no `BUGS.md`

> **Importante:** Quanto mais detalhado, mais fácil fica para o desenvolvedor corrigir.

---

## 8. Fluxo de Teste Recomendado

1. **Teste de Fumaça (Smoke Test)** — passar rapidamente por todas as telas para ver se nada quebrou
2. **Teste Funcional Completo** — rodar todos os CTs na ordem
3. **Teste de Borda** — repetir os CTs com dados extremos (valores altos, textos longos, campos vazios)
4. **Teste de Regressão** — verificar se bugs antigos corrigidos continuam corrigidos
5. **Registrar bugs** no `BUGS.md`
6. **Enviar relatório final** para o líder do projeto (Gustavo Piteira)

---

## 9. Como Testar as Notificações (CT-10 e CT-11)

As notificações usam o `AlarmManager` do Android, que segue o relógio interno do celular. Para testar sem esperar semanas, é possível "enganar" o sistema mudando a data do celular.

### Passo a Passo

1. **Desative o "Data e hora automáticas":**
   - Configurações do Android → Sistema → Data e hora
   - Desligar "Definir data e hora automaticamente"

2. **Confirme que os lembretes estão ativos:**
   - App → Configurações → Lembretes de Fatura → switch ligado

3. **Cadastre uma dívida de teste:**
   - Tipo: Cartão de Crédito
   - Descrição: "Teste Notificação"
   - Valor: R$ 100
   - Parcelas: 1x
   - 1º Vencimento: **hoje + 4 dias**
   - (A notificação será agendada para **hoje + 1 dia, às 09h**)

4. **Force o fechamento do app:**
   - Configurações do Android → Apps → Controle Gastos → Forçar parada

5. **Mude a data do celular:**
   - Data: **amanhã (hoje + 1)**
   - Hora: **08:58**

6. **Aguarde 2 minutos** sem mexer em nada
7. **Às 09:00** a notificação deve aparecer na barra de status

### Restaurar Configuração

Depois do teste:
- Reative "Definir data e hora automaticamente"
- A data volta ao normal sozinha

### ⚠️ Observações Importantes

- O alarme só é agendado se `vencimento - 3 dias` estiver **no futuro**
- Vencimento em **hoje + 2 dias** não funciona (o alarme cairia no passado)
- Vencimento em **hoje + 3 dias** funciona apenas se ainda não passou das 09h de hoje
- Vencimento em **hoje + 4 dias** é o mais confiável para o teste

### Teste do BootReceiver (CT-11)

Depois de dominar o teste básico:
1. Cadastre uma dívida com vencimento em **hoje + 5 dias**
2. **Reinicie o celular** (isso apaga todos os alarmes do Android)
3. Depois que ligar, mude a data para **hoje + 2 dias, 08:58**
4. Aguarde 09:00 → a notificação deve aparecer
5. ✅ Se aparecer, o `BootReceiver` funcionou corretamente

---

## 10. Histórico de Execuções

| Data | Testador | Versão | Total de CTs | Passou | Falhou | Bugs Abertos |
|------|----------|--------|--------------|--------|--------|--------------|
| 03/10/2026 | Israel Malheiros | Sprint 5 | 68 | 51 | 11 | 0 (todos corrigidos) |

> **Como usar:** Adicione uma linha a cada rodada de testes executada.

### Bugs Encontrados e Corrigidos

| Bug | CT | Descrição | Status |
|-----|-----|-----------|--------|
| BUG-001 | CT-05.1 | Valor da parcela não era dividido em dívidas parceladas | ✅ Corrigido |
| BUG-002 | CT-04.4 | Pix e Débito pediam parcelas/vencimento | ✅ Corrigido |
| BUG-003 | CT-04.7 | Máscara de valor não formatava corretamente | ✅ Corrigido |
| BUG-004 | CT-04.6 | Card não mostrava valor da parcela | ✅ Corrigido |
| BUG-005 | CT-03.3 | Vencimentos mostravam valor total como pagamento único | ✅ Corrigido |
| BUG-006 | CT-06.1 | Tela Lançar sem barra de navegação | ✅ Corrigido |
| BUG-007 | CT-08.4 / 08.5 | Exclusão de categorias em uso | ✅ Corrigido |
| BUG-008 | CT-09.1 a 09.6 | Erro ao carregar dados do usuário | ✅ Corrigido |

---

## 11. Referências

- **`docs/BUGS.md`** — Registro de bugs encontrados
- **`docs/ARQUITETURA.md`** — Documentação técnica do projeto
- **`CHANGELOG.md`** — Histórico de mudanças por Sprint
