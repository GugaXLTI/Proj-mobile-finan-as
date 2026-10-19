# FEATURE: Leitura Automática de Comprovantes

## 1. Resumo

Permite que o usuário cadastre uma dívida a partir de um comprovante (imagem ou PDF),
sem precisar digitar manualmente. O app extrai o texto, interpreta os campos
(valor, data, banco, beneficiário) e pré-preenche a tela de cadastro.

Existem **duas formas** de acionar a feature:

1. **Compartilhar de fora** — o usuário compartilha uma imagem/PDF de outro app
   (Galeria, WhatsApp, Gmail) e escolhe "Cadastrar dívida por comprovante".
2. **Importar de dentro** — o usuário abre a aba "Lançar" e toca em "Importar comprovante".

Em ambos os casos, o fluxo é o mesmo: **OCR → parser → pré-preenchimento → revisão → salvar**.

---

## 2. Fluxo Técnico

┌─────────────────────────────────────────────────────────────┐
│ 1. ENTRADA                                                  │
│    a) ACTION_SEND (imagem/PDF) → ReceiptShareActivity       │
│    b) Botão "Importar comprovante" → seletor de arquivos    │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ 2. BITMAP                                                   │
│    ReceiptImageLoader.carregarImagem() ou carregarPdf()     │
│    → Bitmap reduzido (máx. 2048 px no maior lado)           │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ 3. OCR — ML Kit Text Recognition (on-device)                │
│    ReceiptOcr.extrairTexto(bitmap, callback)                │
│    → texto bruto (String)                                   │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ 4. PARSER — ReceiptParser.parse(texto, nomeUsuario)         │
│    → ParsedReceipt { titulo, valor, data, banco,            │
│                       categoria, beneficiario, parcelas }   │
└─────────────────────┬───────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────┐
│ 5. PRÉ-PREENCHIMENTO                                        │
│    CadastroDividaActivity.aplicarParsedReceipt()            │
│    → campos preenchidos, usuário revisa e salva             │
└─────────────────────────────────────────────────────────────┘

---

## 3. Arquivos Envolvidos

### Arquivos NOVOS

| Arquivo | Pacote | Responsabilidade |
|---------|--------|------------------|
| ReceiptShareActivity.java | view | Recebe o ACTION_SEND e orquestra o fluxo |
| ReceiptImageLoader.java | utils | Converte Uri em Bitmap (imagem ou PDF) |
| ReceiptOcr.java | utils | Wrapper do ML Kit Text Recognition |
| ReceiptParser.java | utils | Interpreta o texto bruto e devolve ParsedReceipt |
| ParsedReceipt.java | model | POJO (não é @Entity) com os campos extraídos |
| activity_receipt_share.xml | layout | Tela "Analisando comprovante..." |
| ic_loading.xml | drawable | Ícone de spinner animado |

### Arquivos MODIFICADOS

| Arquivo | O que mudou |
|---------|-------------|
| AndroidManifest.xml | Registro da ReceiptShareActivity com filtro ACTION_SEND |
| CadastroDividaActivity.java | Novo botão "Importar comprovante" + método aplicarParsedReceipt + extras |
| activity_cadastro_divida.xml | Botão "Importar comprovante" adicionado no topo |

### Arquivos NÃO modificados

- AppDatabase.java e Migrations.java — nenhuma mudança no banco.
- DividaDao.java — nenhuma query nova.
- Divida.java — nenhum campo novo.

---

## 4. Dependências

Adicionada no app/build.gradle.kts:

    implementation("com.google.mlkit:text-recognition:16.0.0")

Biblioteca gratuita, offline e on-device (não envia dados para servidor).

---

## 5. Como o Parser Funciona

### Regras de extração

| Campo | Estratégia |
|-------|-----------|
| Valor | Regex R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2}) |
| Data | Regex (\d{2})\s+([A-Za-z]{3})\s+(\d{4}) + mapa de meses (JAN, FEV, ...) |
| Banco (origem) | Dicionário de ~34 bancos, casando por trecho normalizado |
| Beneficiário | Primeira linha que "parece nome próprio" (2+ palavras, 60% capitalizadas) e não é o próprio usuário |
| Título | Primeira linha "útil" após o beneficiário; senão, usa o beneficiário como fallback |
| Categoria | Dicionário de ~105 palavras-chave em 10 categorias |
| Parcelas | Sempre 1 (Pix e transferências não são parcelados) |

### Categorias suportadas

Transporte, Alimentação, Saúde, Lazer, Moradia, Educação, Serviços, Vestuário, Pet, Outros.

### Limitações conhecidas

- Foco em comprovantes de Pix. Boletos e comprovantes de cartão de crédito ainda não têm tratamento específico.
- Categoria automática depende de palavra-chave. Se o texto não tiver "uber", "ifood", "netflix", etc., cai em "Outros".
- Título costuma ser o nome do beneficiário. Em Pix, não há um "título de compra" — o parser usa o que tem.
- Comprovantes em inglês ou de bancos muito diferentes podem ter campos não reconhecidos.

---

## 6. Decisões de Design

### Por que ML Kit e não Tesseract?
- ML Kit é mais preciso em comprovantes.
- Roda on-device (privacidade).
- Gratuito.
- Integração simples em Java.

### Por que parser local com regex/dicionário e não IA própria (Orange/TFLite)?
- MVP rápido (dias, não semanas).
- Sem necessidade de dataset rotulado.
- Funciona offline.
- IA própria fica para Fase 2 (quando houver dados).

### Por que ParsedReceipt é POJO e não @Entity?
- Não precisa persistir. É só transporte entre parser e tela.
- Evita Migration desnecessária.
- Mesmo padrão de VencimentoItem e BackupData.

### Por que banco de origem (quem paga) e não destino?
- O gasto é do usuário — o banco que importa é de onde o dinheiro saiu.
- O destino (quem recebeu) ainda é extraído como "Beneficiário".

---

## 7. Fluxo de UX

### Fluxo A — Compartilhar de fora

1. Usuário abre Galeria → Compartilhar → "Cadastrar dívida por comprovante".
2. ReceiptShareActivity abre com spinner "Analisando comprovante...".
3. Campos extraídos aparecem na tela.
4. Usuário toca em "Confirmar e cadastrar".
5. CadastroDividaActivity abre pré-preenchida.
6. Usuário revisa, edita se quiser, e salva.

### Fluxo B — Importar de dentro

1. Usuário abre o app → aba Lançar.
2. Toca em "Importar comprovante".
3. Seletor de arquivos do Android abre.
4. Usuário escolhe imagem ou PDF.
5. Botão vira spinner "Analisando...".
6. Campos são preenchidos na mesma tela.
7. Usuário revisa, edita se quiser, e salva.

---

## 8. Tratamento de Erros

| Situação | Mensagem |
|----------|----------|
| Arquivo inválido | "Não conseguimos abrir esse arquivo. Tente outro comprovante ou preencha manualmente." |
| Imagem ilegível | "O comprovante está ilegível. Tente uma foto mais nítida ou preencha manualmente." |
| Erro no OCR | "Não conseguimos analisar o comprovante. Você pode preencher manualmente." |
| Usuário cancela | Fecha sem salvar nada |

Padrão: sempre sugerir o caminho de fuga ("preencha manualmente").

---

## 9. Privacidade

- Todo o processamento é local. O ML Kit roda on-device.
- Nenhuma imagem ou texto sai do dispositivo.
- Não requer permissões de runtime (usa ACTION_SEND e OpenDocument).
- LGPD-friendly por padrão.

---

## 10. Como Testar

Ver documento TESTES.md com os casos de teste.

Pré-requisitos:
- Celular físico (emulador limita Galeria).
- 2+ comprovantes em mãos (Pix de bancos diferentes).
- 1 PDF qualquer (contrato, boleto).

Comando rápido de debug:
- Logcat filtrado por ReceiptShare, ReceiptOcr, ReceiptParser.

---

## 11. Evoluções Futuras

### Curto prazo
- Mais palavras-chave de categorias.
- Tratar comprovante de cartão de crédito (Estabelecimento, Parcelas, Cartão final).
- Cache de OCR (evitar reprocessar a mesma imagem).

### Médio prazo
- Feedback do usuário (guardar correções para melhorar o parser).
- Suporte a mais bancos e formatos.

### Longo prazo (Fase 2 do briefing)
- Coletar centenas de comprovantes rotulados.
- Treinar modelo próprio com Orange.
- Exportar para TensorFlow Lite.
- Substituir o parser regex por IA própria.

---

## 12. Referências

- Briefing original: docs/BRIEFING-OCR.md
- Casos de teste: docs/TESTES.md
- Estrutura do app: docs/ARQUITETURA.md
- ML Kit Text Recognition: https://developers.google.com/ml-kit/vision/text-recognition