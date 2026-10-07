---

## 🗑️ Soft Delete de Dívidas

### Data de implementação
06/10/2026

### Descrição

Foi implementado um sistema de **soft delete** (exclusão lógica) para dívidas. Em vez de apagar o registro do banco, o app agora **marca como excluída**. Isso permite que a dívida continue aparecendo no Histórico, mesmo depois de excluída.

### Por que fazer isso?

1. **Histórico fiel ao Figma:** o usuário vê o que foi excluído
2. **Auditabilidade:** sistemas financeiros nunca apagam registros, só marcam como inativos
3. **Padrão de mercado:** é o comportamento de apps bancários reais

### Como funciona

```
Antes (exclusão física):
┌──────────────────┐
│ Dívida "Almoço"  │ → db.deletar() → 🗑️ Desaparece pra sempre
└──────────────────┘

Depois (soft delete):
┌──────────────────┐
│ Dívida "Almoço"  │ → d.excluida = true → 💾 Fica no banco
└──────────────────┘                    mas invisível nas telas
                                        principais
```

### Componentes

#### 1. `model/Divida.java`
- Novo campo: `public boolean excluida;`
- 3 construtores:
  - **Completo** (usado pelo Room)
  - **Sem ID** (assume `excluida = false` — o caso mais comum)
  - **Sem ID com excluida** (casos especiais)

#### 2. `dao/DividaDao.java`
- Todas as queries existentes filtram `excluida = 0`
- Novas queries para o Histórico:
  - `listarExcluidasPorUsuario(userId)` — só excluídas
  - `listarTodasParaHistorico(userId)` — todas (incluindo excluídas)
  - `listarPorCategoria(userId, categoria)` — filtro por categoria
- Renomeado `deletar()` para `deletarFisicamente()` (uso restrito)

#### 3. Telas afetadas

| Tela | Comportamento |
|------|---------------|
| **Dívidas** | Ignora excluídas (query filtrada) |
| **Início** | Ignora excluídas |
| **Dashboard** | Ignora excluídas |
| **Histórico** | **Mostra** excluídas com ✗ vermelho |

#### 4. Exclusão

Toda exclusão agora chama:
```java
divida.setExcluida(true);
db.dividaDao().atualizar(divida);
```

Em vez de `deletarFisicamente()`.

### Versão do banco

- **v7** → Sprint 5 (Cartões, Pix)
- **v8** → Sprint 6 (Soft Delete)

---

## 📜 Tela de Histórico

### Data de implementação
06/10/2026

### Descrição

Tela que mostra o **histórico de dívidas mês a mês**, com navegação, filtros e exportação.

### Arquitetura

```
┌──────────────────────────────────────────┐
│  HistoricoActivity                       │
├──────────────────────────────────────────┤
│  1. Navegação de mês (← Setembro 2026 →) │
│  2. Card de resumo (Total/Pago/Falta)    │
│  3. Chips de filtro por categoria        │
│  4. RecyclerView de lançamentos          │
│  5. Botão "Baixar Resumo" (CSV/PDF)      │
└─────────────┬────────────────────────────┘
              │
              ▼
┌──────────────────────────────────────────┐
│  HistoricoAdapter                        │
│  (4 estados visuais por item)            │
└──────────────────────────────────────────┘
```

### 4 Estados de cada Dívida

| Estado | Condição | Cor | Ícone |
|--------|----------|-----|-------|
| ✅ Liquidado | `pago = true` | Verde | ✓ |
| ↻ Parcial | `valorPago > 0` e `pago = false` | Azul | ↻ |
| ⏳ Pendente | `valorPago = 0` e `pago = false` | Amarelo | → |
| ✗ Excluída | `excluida = true` | Vermelho | ✗ |

### Filtro por mês

A dívida entra no mês da sua **data de vencimento** (parse de `dd/MM/yyyy`).

### Filtros por categoria

Os chips são **gerados dinamicamente** com base nas categorias que têm dívidas no mês selecionado.

### Integração com a tela Início

O card **"Total de Dívidas Acumuladas"** agora é clicável. Ao tocá-lo, o usuário é levado para o Histórico.

---

## 📤 Exportação de Dados

### Data de implementação
06/10/2026

### Descrição

Foi implementada exportação do extrato mensal em **CSV** e **PDF**.

### CSV (`CsvExportHelper.java`)

- Gera arquivo `.csv` com separador `;`
- BOM UTF-8 para o Excel reconhecer acentos
- Colunas: Título, Categoria, Banco, Parcela, Vencimento, Valor Total, Valor Pago, Falta, Status
- Compartilhamento via WhatsApp, Email, Drive

### PDF (`PdfExportHelper.java`)

- Biblioteca **iTextG 5.5.10**
- Layout escuro fiel ao Figma (fundo #131C2E, cores do app)
- Cabeçalho "ORG" + Gestão Financeira
- Card de resumo (Total, Já Pago, Falta Pagar)
- Lista de lançamentos com cores por estado

### FileProvider

Configuração necessária para compartilhar arquivos via Intent:

**`res/xml/file_paths.xml`:**
```xml
<paths>
    <cache-path name="exports" path="exports/" />
</paths>
```

**`AndroidManifest.xml`:**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### Fluxo de uso

1. Usuário abre o Histórico
2. Seleciona o mês (e opcionalmente a categoria)
3. Toca em "Baixar Resumo"
4. Diálogo: **CSV (Excel)** ou **PDF (Relatório)**
5. Escolhe o formato → arquivo é gerado na pasta `cache/exports/`
6. Intent de compartilhamento é aberto
7. Usuário escolhe o app (WhatsApp, Gmail, Drive, etc)

### Arquivos criados

| Arquivo | Tipo |
|---------|------|
| `utils/CsvExportHelper.java` | Helper CSV |
| `utils/PdfExportHelper.java` | Helper PDF |
| `res/xml/file_paths.xml` | Config FileProvider |

### Arquivos modificados

| Arquivo | Mudança |
|---------|---------|
| `build.gradle.kts` | Dependência iTextG |
| `AndroidManifest.xml` | Registro do FileProvider |
| `view/HistoricoActivity.java` | Diálogo CSV/PDF + exportação |

---

## 🧪 Como testar Soft Delete e Histórico

1. Cadastre uma dívida normal (com categoria "Lazer")
2. **Exclua** essa dívida na tela de Dívidas
3. ✅ A dívida some da tela de Dívidas
4. ✅ Abra **Início → Histórico** e navegue até o mês do vencimento
5. ✅ A dívida aparece com status **"Excluída ✗"** em vermelho e valor riscado
6. ✅ O card de resumo **não soma** o valor da dívida excluída

---

## 🧪 Como testar Exportação

### CSV
1. Abra o Histórico em um mês com dívidas
2. Toque em "Baixar Resumo" → **CSV**
3. Compartilhe via WhatsApp ou Drive
4. Abra no Excel/Sheets → deve mostrar resumo + lista

### PDF
1. Mesmo fluxo, mas escolhe **PDF**
2. Compartilhe e abra
3. ✅ Deve ter o cabeçalho "ORG", o card de resumo e a lista formatada

---

## 🚧 Próximos passos

- [ ] Testes de notificação pelo Israel
- [ ] Aplicar Migration real (não destrutiva) no futuro
- [ ] Autenticação em nuvem (Firebase) — opcional
- [ ] Biometria real

---

## 📚 Referências

- Autor da Sprint 6: Gustavo Piteira
- Data de implementação: 06/10/2026
- Soft Delete: 06/10/2026
- Tela de Histórico: 06/10/2026
- Exportação CSV: 06/10/2026
- Exportação PDF: 06/10/2026
