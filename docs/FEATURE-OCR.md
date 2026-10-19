# FEATURE: Leitura Automática de Comprovantes

## 1. Resumo

Permite que o usuário cadastre uma dívida a partir de um comprovante (imagem ou PDF), sem precisar digitar manualmente. O app extrai o texto via OCR, interpreta os campos (valor, data, banco de origem, beneficiário, categoria) e pré-preenche a tela de cadastro. O sistema conta com um Cache de OCR local para evitar reprocessamento.

Existem duas formas de acionar a feature:
1. Compartilhar de fora — O usuário compartilha uma imagem/PDF de outro app (Galeria, WhatsApp) e escolhe "Cadastrar dívida por comprovante".
2. Importar de dentro — O usuário abre a aba "Lançar" e toca em "Importar comprovante".

Em ambos os casos, o fluxo é: Cache/OCR → Parser → Pré-preenchimento → Revisão → Salvar.

---

## 2. Fluxo Técnico


graph TD
    A[1. ENTRADA<br>ACTION_SEND ou Botão Importar] --> B{2. CACHE DE OCR<br>ReceiptCacheHelper.buscarTexto}
    B -- Cache HIT (Encontrado) --> D[4. PARSER]
    B -- Cache MISS (Não encontrado) --> C[3. BITMAP + OCR<br>ReceiptImageLoader + ML Kit]
    C --> D[4. PARSER<br>ReceiptParser]
    D --> E[5. PRÉ-PREENCHIMENTO<br>CadastroDividaActivity]
    
    style A fill:#f9f9f9,stroke:#333,stroke-width:2px
    style B fill:#d4edda,stroke:#28a745,stroke-width:2px
    style C fill:#fff3cd,stroke:#ffc107,stroke-width:2px
    style D fill:#cce5ff,stroke:#007bff,stroke-width:2px
    style E fill:#d1ecf1,stroke:#17a2b8,stroke-width:2px

## 3. Arquivos Envolvidos

### Arquivos NOVOS (Feature OCR)
| Arquivo | Pacote | Responsabilidade |
|---------|--------|------------------|
| ReceiptShareActivity.java | view | Recebe o ACTION_SEND e orquestra o fluxo |
| ReceiptImageLoader.java | utils | Converte Uri em Bitmap (imagem ou PDF) |
| ReceiptOcr.java | utils | Wrapper do ML Kit Text Recognition |
| ReceiptParser.java | utils | Interpreta o texto e devolve ParsedReceipt |
| ReceiptCacheHelper.java | utils | Gerencia o cache de OCR (busca/salva) |
| ParsedReceipt.java | model | POJO de transporte (não é entidade) |
| ComprovanteProcessado.java | model | Entidade do cache (usuarioId, uri, texto) |
| ComprovanteProcessadoDao.java | dao | Queries do Room para o cache |
| activity_receipt_share.xml | layout | Tela "Analisando comprovante..." |
| ic_loading.xml | drawable | Ícone de spinner animado |

### Arquivos MODIFICADOS
| Arquivo | O que mudou |
|---------|-------------|
| AndroidManifest.xml | Registro da ReceiptShareActivity (ACTION_SEND) |
| CadastroDividaActivity.java | Botão importar, aplicarParsedReceipt e verificação de CT-24.3 |
| activity_cadastro_divida.xml | Botão "Importar comprovante" |
| AppDatabase.java | Versão 10, inclusão da entidade de cache e DAO |
| Migrations.java | Criação da MIGRATION_9_10 (tabela de cache) |

---

## 4. Dependências

Adicionada no arquivo app/build.gradle.kts:

implementation("com.google.mlkit:text-recognition:16.0.0")

---

## 5. Como o Parser Funciona (Regras Finais)

| Campo | Estratégia |
|-------|-----------|
| Valor | Regex R\$\s*(\d{1,3}(?:\.\d{3})*,\d{2}). Aceita pontos em milhares e vírgula decimal. |
| Data | Regex híbrida: aceita dd MMM yyyy (ex: 08 OUT 2026) e formato numérico dd/MM/yyyy ou dd/MM/yy. |
| Banco (origem) | O parser varre o texto e retorna o banco que aparece primeiro no comprovante (mais perto do topo), garantindo que a origem (ex: Bradesco) vença o destino (ex: Nubank). |
| Beneficiário | Primeira linha que "parece nome próprio" (2+ palavras, capitalizadas), ignorando o próprio usuário, e bloqueando rótulos (ex: "Dados de quem recebeu"). |
| Título | Primeira linha "útil" após o beneficiário; senão, usa o beneficiário como fallback. |
| Categoria | Dicionário expandido com ~150 palavras (marcas, tipos de comércio, serviços e assinaturas). |
| Parcelas | Sempre 1 (foco atual em Pix). |

**Proteção de Contexto:** Se o OCR não encontrar nenhum valor no texto (parsed.getValor() == null), a imagem é descartada como "inválida" para evitar que fotos aleatórias (ex: paisagem) gerem campos fantasmas.

---

## 6. Decisões de Design

- **ML Kit (on-device):** Privacidade total (LGPD), gratuito e offline.
- **Cache no Room:** Economiza bateria e processamento. A chave primária é usuarioId + uri. O isolamento entre usuários é garantido pelas queries do DAO.
- **ParsedReceipt POJO:** Não vai para o banco, serve apenas para transportar os dados do Parser para a tela.
- **Banco de Origem:** O app é de finanças pessoais, logo, o gasto é do usuário (banco de origem) e não do recebedor.

---

## 7. Tratamento de Erros

| Situação | Mensagem |
|----------|----------|
| Arquivo inválido | "Não conseguimos abrir esse arquivo. Tente outro comprovante ou preencha manualmente." |
| Imagem ilegível | "O comprovante está ilegível. Tente uma foto mais nítida ou preencha manualmente." |
| Imagem sem valor | "Nenhum valor encontrado. A imagem não parece ser um comprovante válido." |
| Erro no OCR | "Não conseguimos analisar o comprovante. Você pode preencher manualmente." |

---

## 8. Privacidade

- Todo o processamento é local (on-device).
- Nenhuma imagem ou texto sai do dispositivo do usuário.
- O cache fica no banco SQLite local (Room), isolado por conta de usuário.
- LGPD-friendly.

---

## 9. Evoluções Futuras (Fase 2)

- **Suporte a Cartão de Crédito:** Extração de "Estabelecimento", "Parcelas (X/Y)" e "Cartão final XXXX".
- **Dados Sintéticos e IA:** Gerar comprovantes sintéticos (Faker) e treinar um modelo no Orange Data Mining para exportar para TensorFlow Lite, reduzindo a dependência de Regex.
- **Melhoria Contínua:** Guardar as correções do usuário para retroalimentar o parser.
---

### 2º Passo: Arquivo `TESTES.md`
Vá no menu à esquerda do GitHub, clique em `TESTES.md`. Apague tudo o que está aí e cole o bloco abaixo:

# Plano de Testes – App Gestão de Finanças

Documento com o plano de testes, casos de teste e critérios de aceitação do aplicativo. Serve como guia para os testers (membros da equipe) executarem os testes de forma padronizada e reportarem bugs.

---

## 1. Objetivo

Garantir que todas as funcionalidades do aplicativo estejam funcionando conforme o esperado, identificar bugs, validar as integrações entre telas e verificar a experiência do usuário final.

---

## 2. Escopo

### O que está incluído nos testes:
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
- **Leitura automática de comprovantes (OCR + Cache)**

### O que NÃO está incluído:
- Autenticação em nuvem (Firebase)
- Sincronização entre dispositivos
- Biometria real
- Comprovantes de Cartão de Crédito (Fase 2)

---

## 3. Ambiente de Teste

| Item | Descrição |
|------|-----------|
| Dispositivo | Celular Android físico |
| Versão do Android | API 24+ |
| Conexão | Wi-Fi ou dados móveis |
| Build | Última versão do branch master |
| Pré-requisitos | App instalado, notificações permitidas |

---

## 4. Tipos de Testes

| Tipo | Descrição |
|------|-----------|
| Funcional | Verifica cada funcionalidade |
| Integração | Verifica comunicação entre telas |
| Regressão | Verifica funcionalidades antigas |
| Usabilidade | Verifica facilidade de uso |
| Borda | Situações extremas |

---

## 5. Casos de Teste

> Legenda: [x] = Passou | [ ] = Ainda não testado

### CT-01 ao CT-19 (Originais)
Todos os testes anteriores foram validados e estão passando.

### CT-23: Recebimento e Preparação (OCR)
| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-23.1 | Compartilhar imagem da Galeria | App aparece na lista de compartilhar | [x] |
| CT-23.2 | Compartilhar imagem do WhatsApp | Abre a tela "Analisando..." | [x] |
| CT-23.3 | Compartilhar PDF | Processa a 1ª página | [x] |
| CT-23.4 | Compartilhar imagem 12 MP | Processa sem travar | [x] |
| CT-23.5 | Compartilhar com app já aberto (singleTop) | Activity reaproveitada | [x] |
| CT-23.6 | Importar pela aba Lançar | Abre seletor de arquivos | [x] |
| CT-23.7 | Selecionar imagem no seletor interno | Botão vira spinner "Analisando..." | [x] |
| CT-23.8 | Cancelar o seletor de arquivos | Botão volta ao normal | [x] |

### CT-24: OCR (Extração de Texto)
| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-24.1 | Comprovante Nubank nítido | Texto extraído com >300 caracteres | [x] |
| CT-24.2 | Comprovante com acentos | Acentos preservados | [x] |
| CT-24.3 | Imagem sem texto (paisagem) | App bloqueia e não avança | [x] |
| CT-24.4 | Foto com pouca luz | Extrai ou mostra erro amigável | [x] |
| CT-24.5 | PDF (1ª página Bradesco) | Texto extraído corretamente | [x] |
| CT-24.6 | Primeira execução do app | ML Kit baixa modelo e processa | [x] |
| CT-24.7 | Segunda execução (offline) | Processa offline normalmente | [x] |

### CT-25: Parsing (Campos Detectados)
| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-25.1 | Extrair valor (R$ 20,00) | Campo "Valor: R$ 20,00" | [x] |
| CT-25.2 | Extrair valor (R$ 159,50) | Campo "Valor: R$ 159,50" | [x] |
| CT-25.3 | Extrair data "08 OUT 2026" | Campo "Data: 08/10/2026" | [x] |
| CT-25.4 | Extrair data "18/10/2026" | Campo "Data: 18/10/2026" | [x] |
| CT-25.5 | Extrair banco de origem (topo do texto) | Campo "Banco: Bradesco" (e não Nubank) | [x] |
| CT-25.6 | Extrair banco Inter (origem) | Campo "Banco: Inter" | [x] |
| CT-25.7 | Extrair beneficiário | Campo "Beneficiário: Adson..." | [x] |
| CT-25.8 | Não confundir usuário logado | Nome do usuário não aparece | [x] |
| CT-25.9 | Não aceitar rótulos como nome | Bloqueia "Informações adicionais" | [x] |
| CT-25.10| Título sem números/símbolos | Título é texto válido | [x] |
| CT-25.11| Inferir "Transporte" (Uber) | Campo "Categoria: Transporte" | [x] |
| CT-25.12| Inferir "Alimentação" (iFood) | Campo "Categoria: Alimentação" | [x] |
| CT-25.13| Fallback "Outros" | Campo "Categoria: Outros" | [x] |
| CT-25.14| Parcelas sempre 1x em Pix | Campo "Parcelas: 1x" | [x] |

### CT-26: Integração com Cadastro
| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-26.1 | Compartilhar -> "Confirmar e cadastrar" | CadastroDividaActivity abre preenchida | [x] |
| CT-26.2 | Importar pela aba Lançar | Campos preenchidos na mesma tela | [x] |
| CT-26.3 | Tipo de dívida = "Pix" | Spinner em "Pix" | [x] |
| CT-26.4 | Descrição preenchida | Campo preenchido | [x] |
| CT-26.5 | Valor preenchido | Campo "R$ X,XX" | [x] |
| CT-26.6 | Data da Compra preenchida | Campo preenchido | [x] |
| CT-26.7 | Vencimento preenchido (Pix) | Campo = mesma data | [x] |
| CT-26.8 | Categoria pré-selecionada | Spinner no item correto | [x] |
| CT-26.9 | Categoria inexistente | Cai no 1º item | [x] |
| CT-26.10| Editar campos antes de salvar | Campo aceita edição | [x] |
| CT-26.11| Salvar dívida pré-preenchida | Dívida aparece em "Dívidas" | [x] |
| CT-26.12| Salvar com categoria alterada | Dívida salva com nova categoria | [x] |
| CT-26.13| Botão "Confirmar" só após parser | Botão não aparece antes | [x] |
| CT-26.14| Toast de boas-vindas | "Comprovante pré-preenchido..." | [x] |
| CT-26.15| Voltar sem salvar | Nada é salvo | [x] |

### CT-27 a CT-32 (Erros, Spinners, Cache, Migration)
| ID | Descrição | Resultado Esperado | Status |
|----|-----------|---------------------|--------|
| CT-27 | Tratamento de erros | Mensagens amigáveis e sem crash | [x] |
| CT-28 | Regressão de Dívidas | Fluxos antigos intactos | [x] |
| CT-29 | Feedback Visual (Spinners) | Botões mostram progresso | [x] |
| CT-30 | Mensagens de Erro | Toasts com tempo de leitura | [x] |
| CT-31 | Bancos/Categorias Ampliados | Novos dicionários funcionam | [x] |
| CT-32.1 | Cache MISS | Primeira importação roda OCR | [x] |
| CT-32.2 | Cache HIT | Segunda importação (Galeria) é instantânea | [x] |
| CT-32.7 | Migration v9 -> v10 | App abre, dados antigos intactos | [x] |
| CT-32.11| Tabela de Cache | Cache grava e lê corretamente | [x] |
| CT-32.12| Isolamento do Cache | User A não vê cache do User B | [x] |
| CT-32.17| Cache pós-reboot | Cache HIT após reiniciar o celular | [x] |

---

## 6. Critérios de Aceitação

O app é considerado aprovado para release quando:

- Todos os CTs estiverem "Passou"
- Nenhum bug Alta em aberto
- Bugs Média documentados no docs/BUGS.md
- App não travar em nenhum fluxo testado
- Navegação fluida entre telas

---

## 7. Como Reportar um Bug

1. Anote o CT que falhou
2. Anote os passos exatos
3. Anote o esperado vs o que aconteceu
4. Tire um print
5. Abra um registro no docs/BUGS.md
6. Marque como Aberto

---

## 14. Histórico de Execuções

| Data | Testador | Versão | Total | Passou | Falhou | Bugs Abertos |
|------|----------|--------|-------|--------|--------|--------------|
| 03/10/2026 | Israel Malheiros | Sprint 5 | 68 | 60 | 8 | 0 |
| 07/10/2026 | Israel Malheiros | Sprint 6 | 33 | 33 | 0 | 0 |
| 18/10/2026 | Israel Malheiros | Sprint 7 (OCR) | 45 | 45 | 0 | 0 |

### Bugs Encontrados e Corrigidos

| Bug | CT | Descrição | Status |
|-----|-----|-----------|--------|
| BUG-001 ao BUG-010 | Vários | Bugs da Sprint 5/6 | Corrigido |
| BUG-011 | CT-24.3 | OCR lia paisagem e preenchia campos falsos | Corrigido |
| BUG-012 | CT-24.5 | Data numérica (dd/MM/yyyy) não era lida | Corrigido |
| BUG-013 | CT-24.5 | Banco de destino (Nubank) sobrepunha a origem (Bradesco) | Corrigido |
| BUG-014 | CT-25.1 | Regex não lia valores com milhar/espaços | Corrigido |

---

## 15. Referências

- docs/BUGS.md - Registro de bugs encontrados
- docs/ARQUITETURA.md - Documentação técnica do projeto
- docs/FEATURE-OCR.md - Documentação da feature de OCR
- CHANGELOG.md - Histórico de mudanças por Sprint
