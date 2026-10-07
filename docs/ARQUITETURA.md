---

## 🐛 Correções de Bugs da Sprint 5

### Data da implementação
03/10/2026

### Resumo

Foram corrigidos **7 bugs** identificados durante a execução dos testes do Plano de Testes (`docs/TESTES.md`). Abaixo está o detalhamento técnico de cada um.

### 🐛 BUG-002: Pix e Cartão de Débito com parcelas/vencimento

**Problema:** Compras via Pix e Débito exibiam campos de Parcelas e Vencimento, que não fazem sentido (são pagamentos à vista).

**Solução:**
- Novo helper `isTipoAVista(String tipo)` que cobre Pix e Cartão de Débito
- Esconde containers de Parcelas e Vencimento
- Força `parcela = "1x (À vista)"` e `vencimento = dataCompra`
- Marca como pago automaticamente: `pago = true`, `valorPago = valorTotal`
- Não agenda alarme de notificação

**Arquivos:** `CadastroDividaActivity.java`, `activity_cadastro_divida.xml`

---

### 🐛 BUG-003: Máscara de valor não formata corretamente

**Problema:** Em celulares configurados em inglês, a máscara exibia "R$ 123.45" (ponto) em vez de "R$ 123,45" (vírgula).

**Solução:**
- Constante `LOCALE_BR = new Locale("pt", "BR")`
- Forçado em todos os `String.format` de valores monetários
- Limite de 11 dígitos para evitar overflow

**Arquivos:** `CadastroDividaActivity.java`, `DividaAdapter.java`, `VencimentoAdapter.java`, `InicioActivity.java`

---

### 🐛 BUG-004: Card de dívidas não mostrava valor da parcela

**Problema:** O card mostrava apenas "Parcela 3x" sem informar o valor de cada parcela.

**Solução:**
- Exibe "Parcela 3x de R$ 66,67" quando há mais de 1 parcela

**Arquivos:** `DividaAdapter.java`

---

### 🐛 BUG-005: Vencimentos na tela Início mostravam valor total como pagamento único

**Problema:** Os cards de vencimento exibiam o valor total da compra, dando a impressão de que o usuário precisava pagar tudo de uma vez.

**Solução:**
- Adiciona linha de progresso: "Parcela 2 de 10"
- Exibe o valor da parcela em destaque
- Adiciona linha com valor total em letras menores
- Esconde as duas linhas quando a dívida é à vista

**Arquivos:** `VencimentoAdapter.java`, `item_vencimento.xml`, `InicioActivity.java`

---

### 🐛 BUG-006: Tela Lançar Dívida sem barra de navegação

**Problema:** O usuário ficava "preso" na tela de Lançar Dívida, sem conseguir navegar para outras abas.

**Solução:**
- Envolve o layout em `ConstraintLayout` com `bottomNavigation`
- Adiciona a barra de navegação inferior (Início, Lançar, Dívidas, Relatórios, Config)
- Destaca a aba "Lançar" como ativa (verde)

**Arquivos:** `activity_cadastro_divida.xml`, `CadastroDividaActivity.java`

---

### 🐛 BUG-007: Exclusão de categorias em uso

**Problema:** Ao excluir uma categoria usada por dívidas, as dívidas ficavam com o nome da categoria órfã ("fantasma") em Início, Dívidas e Dashboard.

**Solução:**
- Bloqueia a exclusão quando a categoria está em uso
- Exibe diálogo informativo com contagem de dívidas afetadas
- Detecta categoria órfã ao editar dívida
- Abre diálogo pedindo novo nome (pré-preenchido)
- Cria a categoria automaticamente com cor cinza padrão

**Arquivos:** `CategoriasActivity.java`, `CadastroDividaActivity.java`

---

### 🐛 BUG-008: Erro ao carregar dados do usuário na tela Editar Perfil

**Problema:** Quando a sessão perdia a sincronia com o banco (após reinstalação ou migração), o `buscarPorId()` retornava `null` e a tela mostrava "Erro ao carregar dados do Usuário".

**Solução:**
- **Fallback 1:** busca por ID (sessão)
- **Fallback 2:** busca por nome (sessão)
- **Fallback 3:** se só existe 1 usuário no banco, usa ele
- Se nada funcionar, força logout e redireciona para Login
- Novos métodos no `UsuarioDao`: `buscarPorNome`, `buscarPrimeiroUsuario`, `contarUsuarios`
- `SessionManager` agora salva e atualiza o e-mail na sessão

**Arquivos:** `EditarPerfilActivity.java`, `UsuarioDao.java`, `SessionManager.java`

---

## 🎨 Melhorias de UX

### Spinner de categoria com bolinha colorida

**Data:** 03/10/2026

**Descrição:** O spinner de categoria agora exibe uma bolinha colorida ao lado do nome, refletindo a cor escolhida pelo usuário.

**Componentes criados:**
- Layout `item_spinner_categoria.xml`
- Adapter `CategoriaSpinnerAdapter.java`

---

## 📌 Arquivos modificados na Sprint 5 (Correções)

| Arquivo | Mudança |
|---------|---------|
| `view/CadastroDividaActivity.java` | Pix/Débito à vista, máscara pt-BR, barra de navegação, categoria órfã |
| `view/CategoriasActivity.java` | Bloqueio de exclusão de categorias em uso |
| `view/EditarPerfilActivity.java` | 3 fallbacks para carregar usuário |
| `view/InicioActivity.java` | Locale pt-BR |
| `adapter/DividaAdapter.java` | Exibe valor da parcela |
| `adapter/VencimentoAdapter.java` | Exibe progresso de parcelas e valor da parcela |
| `dao/UsuarioDao.java` | Novos métodos `buscarPorNome`, `buscarPrimeiroUsuario`, `contarUsuarios` |
| `utils/SessionManager.java` | Salva e atualiza e-mail |
| `layout/activity_cadastro_divida.xml` | Barra de navegação inferior |
| `layout/item_vencimento.xml` | Linha de progresso e valor total |
| `layout/item_spinner_categoria.xml` | **Novo** – bolinha colorida + nome |
| `adapter/CategoriaSpinnerAdapter.java` | **Novo** – adapter do spinner |

---

## 🚧 Próximos passos

- [ ] Aplicar o mesmo padrão de isolamento caso novas entidades sejam criadas
- [ ] Implementar `Migration` real (não destrutiva) quando houver usuários reais
- [ ] Exportar/importar dados por usuário
- [ ] Backup na nuvem (Firebase Auth + Firestore – opcional)
- [ ] Testar as notificações com mudança de data do celular

---

## 📚 Referências

- Issue relacionada: (criar no GitHub)
- Autor da descoberta: Gustavo Piteira / Israel Malheiros
- Data da resolução: 23/09/2026 a 03/10/2026
- Expansão para Categoria: 26/09/2026
- Implementação dos Lembretes: 27/09/2026
- Correção do Cálculo de Parcelas (BUG-001): 27/09/2026
- Implementação de Cartões e Chaves Pix: 28/09/2026
- Integração de Cartões/Pix com Lançar Dívida: 28/09/2026
- Implementação do BootReceiver: 29/09/2026
- CRUD Completo de Cartões/Pix: 29/09/2026
- Plano de Testes: 29/09/2026
- **Correção dos BUGs 002 a 008: 03/10/2026**
