---

## 11. Execuções Realizadas

### Rodada 1 — 03/10/2026 (Sprint 5)

**Testador:** Israel Malheiros  
**Versão:** Sprint 5 (com correções de bugs)  
**Ambiente:** Celular Android físico

| Bloco | Total de CTs | Passou | Falhou | Bugs Abertos |
|-------|--------------|--------|--------|--------------|
| CT-01 (Splash/Sessão) | 3 | 3 | 0 | 0 |
| CT-02 (Cadastro/Login) | 8 | 8 | 0 | 0 |
| CT-03 (Início) | 4 | 4 | 0 | 0 |
| CT-04 (Cadastro Dívida) | 8 | 6 | 2 | 2 (corrigidos) |
| CT-05 (Dívidas) | 6 | 6 | 0 | 0 |
| CT-06 (Cartões/Pix) | 10 | 9 | 1 | 1 (corrigido) |
| CT-07 (Dashboard) | 3 | 3 | 0 | 0 |
| CT-08 (Categorias) | 5 | 3 | 2 | 2 (corrigidos) |
| CT-09 (Editar Perfil) | 6 | 0 | 6 | 1 (corrigido) |
| CT-10 (Lembretes) | 6 | — | — | Pendente |
| CT-11 (BootReceiver) | 2 | — | — | Pendente |
| CT-12 (Isolamento) | 3 | 3 | 0 | 0 |
| CT-13 (Configurações) | 6 | 6 | 0 | 0 |

### Bugs Encontrados e Corrigidos

| Bug | CT | Descrição | Status |
|-----|-----|-----------|--------|
| BUG-002 | CT-04.4 | Pix e Débito pediam parcelas/vencimento | ✅ Corrigido |
| BUG-003 | CT-04.7 | Máscara de valor não formatava corretamente | ✅ Corrigido |
| BUG-004 | CT-04.6 | Card não mostrava valor da parcela | ✅ Corrigido |
| BUG-005 | CT-03.3 | Vencimentos mostravam valor total como pagamento único | ✅ Corrigido |
| BUG-006 | CT-06.1 | Tela Lançar sem barra de navegação | ✅ Corrigido |
| BUG-007 | CT-08.4 / 08.5 | Exclusão de categorias em uso | ✅ Corrigido |
| BUG-008 | CT-09.1 a 09.6 | Erro ao carregar dados do usuário | ✅ Corrigido |

### Próximos Passos

- Executar CT-10 (Lembretes) com mudança de data do celular
- Executar CT-11 (BootReceiver) reiniciando o celular
- Testar notificações após reinicialização

---

## 12. Histórico de Execuções

| Data | Testador | Versão | Total de CTs | Passou | Falhou | Bugs Abertos |
|------|----------|--------|--------------|--------|--------|--------------|
| 03/10/2026 | Israel Malheiros | Sprint 5 | 68 | 51 | 11 | 0 (todos corrigidos) |

> **Como usar:** Adicione uma linha a cada rodada de testes executada.

---
