# Documentação de Arquitetura – App Gestão de Finanças

## 🐛 Problema Identificado: Dados não vinculados ao usuário

### Data da descoberta
23/09/2026

### Data da correção inicial
23/09/2026

### Data da expansão
26/09/2026

### Status
✅ **RESOLVIDO e EXPANDIDO**

---

## Descrição do problema original

As tabelas `Divida` e `Transacao` do banco Room **não possuíam um campo `usuarioId`** que vinculasse cada registro ao usuário que o cadastrou. Isso significava que:

- Qualquer usuário que fizesse login no app via **todos os dados** cadastrados no banco
- Se duas pessoas diferentes usassem o mesmo celular, cada uma via os dados da outra
- Os dados não eram isolados por conta
- Usuários novos viam dados fictícios (mock) que nunca cadastraram

### Impacto

| Cenário | Comportamento antes | Comportamento depois |
|---------|---------------------|----------------------|
| Tiago cadastra dívidas | Salvas no banco | Salvas e vinculadas ao Tiago |
| Ana faz login no mesmo celular | Vê as dívidas do Tiago | Vê apenas as dívidas dela |
| Tiago faz logout e Ana entra | Ana vê os dados do Tiago | Ana começa do zero |
| Usuário novo instala o app | Vê dívidas fictícias (mock) | Começa com o banco vazio |

### Por que isso aconteceu

Foi uma **decisão de MVP** para simplificar o desenvolvimento inicial. As dívidas foram criadas como entidades globais, sem pensar na segregação por usuário. Isso é comum em protótipos, mas precisava ser corrigido antes de uma versão de produção.

---

## ✅ Solução implementada

### 1. Campo `usuarioId` adicionado nas entidades

```java
@Entity(tableName = "dividas")
public class Divida {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public int usuarioId; // ← NOVO CAMPO
    
    // ... outros campos
}
```

O mesmo foi feito nas entidades `Transacao`, `Categoria`, `Cartao` e `ChavePix`.

### 2. DAOs atualizados para filtrar por usuário

```java
@Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId")
List<Divida> listarPorUsuario(int usuarioId);

@Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0")
List<Divida> listarNaoPagasPorUsuario(int usuarioId);

@Query("SELECT * FROM transacoes WHERE usuarioId = :usuarioId")
List<Transacao> listarPorUsuario(int usuarioId);

@Query("SELECT * FROM categorias WHERE usuarioId = :usuarioId ORDER BY nome ASC")
List<Categoria> listarPorUsuario(int usuarioId);

@Query("SELECT * FROM cartoes WHERE usuarioId = :usuarioId ORDER BY id DESC")
List<Cartao> listarPorUsuario(int usuarioId);

@Query("SELECT * FROM chaves_pix WHERE usuarioId = :usuarioId ORDER BY id DESC")
List<ChavePix> listarPorUsuario(int usuarioId);
```

### 3. AppDatabase atualizado para versão 7

Evolução das versões:
- **v1** → App funcional com Room básico
- **v2** → Adicionado `usuarioId` em `Divida` e `Transacao`
- **v3** → Adicionada entidade `Categoria`
- **v4** → Adicionado campo `cor` em `Categoria`
- **v5** → Adicionado campo `valorParcela` em `Divida` (correção de bug)
- **v6** → Adicionadas entidades `Cartao` e `ChavePix`
- **v7** → Adicionado campo `tipo` em `Cartao` (Crédito/Débito)

```java
@Database(
        entities = {Usuario.class, Divida.class, Transacao.class, Categoria.class,
                    Cartao.class, ChavePix.class},
        version = 7,
        exportSchema = false
)
```

Com `fallbackToDestructiveMigration()` para recriar o banco do zero (seguro no MVP).

### 4. Activities atualizadas para passar o `userId`

Todas as telas que consultam dados do banco agora usam `session.getUserId()`:

- `InicioActivity` → `listarPorUsuario(session.getUserId())`
- `DividasActivity` → `listarPorUsuario(session.getUserId())`
- `DashboardActivity` → `listarPorUsuario(session.getUserId())`
- `CadastroDividaActivity` → salva com `session.getUserId()` + categorias dinâmicas
- `CategoriasActivity` → CRUD de categorias filtrado por usuário
- `EditarPerfilActivity` → busca e atualiza usuário por `session.getUserId()`
- `CartoesFragment` → lista cartões por usuário
- `ChavesPixFragment` → lista chaves Pix por usuário

### 5. Remoção do `DatabaseSeeder` e `DadosMock`

Como o usuário agora começa com o banco vazio, os arquivos de seed foram **deletados**:

- `DatabaseSeeder.java` (removido)
- `DadosMock.java` (removido)

### 6. CategoriaSeeder (categorias padrão)

Para melhorar a experiência do usuário, foi criado um `CategoriaSeeder` que cria **8 categorias padrão** quando o usuário abre a tela de Categorias pela primeira vez:

- Alimentação (vermelho)
- Transporte (verde)
- Saúde (roxo)
- Educação (azul)
- Lazer (rosa)
- Moradia (laranja)
- Assinaturas (ciano)
- Outros (cinza)

### 7. Botão "Limpar Tudo" em Configurações

Para testes internos, foi adicionado um diálogo em Configurações que apaga **todos os dados do banco** (dívidas, transações, categorias e usuários).

### 8. Editar Perfil

Tela que permite editar nome, e-mail e senha do usuário logado, além de excluir a conta completamente. Requer senha atual para confirmar alterações.

---

## 🐛 Correção do Cálculo de Parcelas (BUG-001)

### Data da correção
27/09/2026

### Descrição do problema

Ao cadastrar uma dívida parcelada (ex: R$ 2.000 em 10x), o card na tela de Dívidas exibia o **valor total** em vez do **valor de cada parcela**, induzindo o usuário ao erro.

### Solução implementada

- Adicionado campo `valorParcela` na entidade `Divida`
- `CadastroDividaActivity` agora calcula `valorParcela = valorTotal / numParcelas` ao salvar
- `DividaAdapter` passou a exibir o `valorParcela` no card
- `AppDatabase` atualizado para versão 5

### Impacto

| Antes | Depois |
|-------|--------|
| Card exibia "Falta: R$ 2.000,00" (total) | Card exibe "Falta: R$ 200,00" (parcela) |
| Usuário poderia achar que era uma cobrança de R$ 2.000/mês | Valor correto por parcela |

---

## 🔔 Sistema de Lembretes de Fatura (Notificações)

### Data de implementação
27/09/2026

### Descrição

Foi implementado um sistema de **notificações locais** que avisa o usuário **3 dias antes** do vencimento de cada dívida, às 9h da manhã.

### Arquitetura

O sistema é composto por 3 componentes principais que trabalham em conjunto:

```
┌──────────────────────┐
│  CadastroDivida      │  → Agenda alarme ao salvar dívida
│  Activity            │
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  AlarmeHelper        │  → Cria PendingIntent com dados da dívida
│  (utils)             │     e agenda no AlarmManager
└──────────┬───────────┘
           │
           ▼ (no horário agendado)
┌──────────────────────┐
│  LembreteReceiver    │  → Recebe o broadcast do Android
│  (receiver)          │     e chama o NotificationHelper
└──────────┬───────────┘
           │
           ▼
┌──────────────────────┐
│  NotificationHelper  │  → Cria o canal e envia a notificação
│  (utils)             │     visível para o usuário
└──────────────────────┘
```

### Componentes

#### 1. `NotificationHelper.java` (utils)
- Cria o **canal de notificação** (`lembretes_fatura`) no Android 8+
- Envia a notificação com título, valor e data de vencimento
- Usa `NotificationCompat` para compatibilidade com todas as versões

#### 2. `LembreteReceiver.java` (receiver)
- `BroadcastReceiver` que escuta o alarme agendado
- Extrai os dados da dívida do `Intent` (id, título, valor, vencimento)
- Chama `NotificationHelper.mostrarNotificacao()`

#### 3. `AlarmeHelper.java` (utils)
- **Agenda** alarmes usando `AlarmManager.setExactAndAllowWhileIdle()`
- **Cancela** alarmes quando a dívida é paga ou excluída
- Gerencia o **estado do switch** de lembretes em `SharedPreferences`
- Calcula a data do alarme: `vencimento - 3 dias, às 9h`

### Fluxo de uso

| Ação | Comportamento |
|------|---------------|
| Usuário cadastra dívida | Alarme agendado para 3 dias antes do vencimento |
| Usuário paga dívida | Alarme cancelado (não notifica) |
| Usuário exclui dívida | Alarme cancelado |
| Usuário edita dívida | Alarme antigo cancelado + novo agendado |
| Chegou o dia do alarme | Notificação aparece às 9h |
| Usuário desliga o switch | Novos alarmes não são agendados |

### Permissões necessárias

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

A permissão `POST_NOTIFICATIONS` (Android 13+) é solicitada automaticamente quando o usuário abre a tela **Início** pela primeira vez.

### Tratamento de erros

- Se a data do alarme já passou → **não agenda** (evita notificação imediata)
- Se o dispositivo não tem permissão de alarme exato → cai para `alarmManager.set()` (não exato)
- Se o usuário negou permissão de notificação → o app exibe um Toast informativo
- Se a dívida está paga → **não agenda** alarme

### Limitações conhecidas

- **Não notifica se o app for forçado a parar** (limitação do Android)
- **Não notifica se o celular for reiniciado** (alarmes são perdidos)
- Para resolver isso no futuro, seria necessário um `BootReceiver` para reagendar após reinicialização

---

## 🧪 Como testar o isolamento entre contas

1. Desinstale o app (para limpar o banco antigo).
2. Crie uma conta (ex: `tiago@email.com`).
3. Cadastre uma dívida e uma categoria.
4. Faça logout → crie outra conta (ex: `ana@email.com`).
5. **Ana deve ver a lista vazia** (sem dados do Tiago).
6. Cadastre uma dívida na conta da Ana.
7. Faça logout → login como Tiago → **só os dados do Tiago aparecem**.

Se todos esses passos funcionarem, o isolamento por usuário está correto. ✅

---

## 🧪 Como testar os lembretes de fatura

1. Desinstale o app.
2. Crie uma conta e faça login.
3. **Permita as notificações** quando solicitado.
4. Cadastre uma dívida com vencimento em **hoje + 4 dias** (ex: 01/10).
5. **Para testar rapidamente**, mude o horário do celular para **hoje + 3 dias, às 08h59**.
6. Aguarde 1 minuto → a notificação deve aparecer.
7. Teste o cancelamento: pague a dívida antes do horário → notificação não aparece.
8. Teste o switch: desligue em Configurações → nenhum alarme é agendado.

---

## 💳 Integração de Cartões e Chaves Pix

### Data da implementação
28/09/2026

### Descrição

Foi implementada a **Tela de Cartões & Chaves Pix**, com navegação por abas e CRUD completo. Além disso, foi feita a **integração dessa tela com o Cadastro de Dívida**, permitindo que o usuário selecione o cartão ou chave Pix ao lançar uma nova dívida.

### Arquitetura da tela

A tela usa **TabLayout + ViewPager2** para alternar entre duas abas:

```
┌────────────────────────────────────────┐
│   CartoesActivity (TabLayout + VP2)   │
├────────────────┬───────────────────────┤
│ CartoesFragment│ ChavesPixFragment     │
├────────────────┼───────────────────────┤
│ Cadastro +     │ Cadastro +            │
│ Lista de       │ Lista de              │
│ Cartões        │ Chaves Pix            │
└────────────────┴───────────────────────┘
```

Cada Fragment possui:
- Um **formulário de cadastro** (topo)
- Uma **lista (RecyclerView)** de itens cadastrados (embaixo)

### Fluxo de integração com Dívida

```
┌──────────────────────────┐
│ CadastroDividaActivity   │
└──────────┬───────────────┘
           │
           ▼
┌──────────────────────────┐
│ Spinner "Tipo de Dívida" │
└──────────┬───────────────┘
           │
   ┌───────┼────────┬──────────────┐
   ▼       ▼        ▼              ▼
Crédito  Débito   Pix         Outros
   │       │        │              │
   ▼       ▼        ▼              ▼
CartaoDao  CartaoDao  ChavePixDao  (Genérico)
(filtro    (filtro    (todas as
 "Crédito") "Débito")  chaves)
   │       │        │
   └───────┴────────┘
           │
           ▼
┌──────────────────────────┐
│ ChipSelecaoAdapter       │
│ (lista horizontal)       │
└──────────┬───────────────┘
           │
   ┌───────┴───────┐
   ▼               ▼
Tem itens?     Não tem?
   │               │
   ▼               ▼
Mostra chips   Empty State +
de seleção     botão "Cadastrar agora"
```

### Componentes criados

#### 1. `model/Cartao.java` (@Entity)
- Campos: `id`, `usuarioId`, `instituicao`, `apelido`, `ultimos4Digitos`, `diaVencimento`, `limite`, `bandeira`, `tipo`
- Campo `tipo` diferencia **Crédito** de **Débito** (usado no filtro da tela de dívida)

#### 2. `model/ChavePix.java` (@Entity)
- Campos: `id`, `usuarioId`, `tipoChave`, `chave`, `nomeFavorecido`, `banco`, `apelidoDivida`

#### 3. `dao/CartaoDao.java` e `dao/ChavePixDao.java`
- CRUD completo com filtros por `usuarioId`

#### 4. `ChipSelecaoAdapter.java`
- Adapter de lista horizontal que renderiza os chips de seleção
- Mantém o estado da posição selecionada
- Expõe o item selecionado via `getItemSelecionado()`

#### 5. `CartoesActivity.java`
- Orquestra o `TabLayoutMediator` entre as abas
- Botão "Voltar" fecha a Activity

#### 6. `CartoesFragment.java` e `ChavesPixFragment.java`
- Cada um gerencia seu próprio formulário e lista
- Salvam no Room em threads separadas
- Recarregam a lista automaticamente após cadastro

### Fluxo do usuário

| Ação | Comportamento |
|------|---------------|
| Usuário abre Configurações → Meus Cartões & Bancos | Vai para `CartoesActivity` |
| Cadastra um cartão de Crédito | Salvo no Room com `tipo = "Crédito"` |
| Cadastra um cartão de Débito | Salvo no Room com `tipo = "Débito"` |
| Cadastra uma chave Pix | Salvo no Room com `tipoChave` e `chave` |
| Volta para Lançar Dívida e escolhe "Cartão de Crédito" | Mostra apenas cartões com `tipo = "Crédito"` |
| Escolhe "Pix" | Mostra apenas as chaves Pix cadastradas |
| Não tem nenhum item do tipo selecionado | Mostra Empty State + botão "Cadastrar agora" |

### Arquivos criados nesta implementação

| Arquivo | Tipo |
|---------|------|
| `model/Cartao.java` | Entidade |
| `model/ChavePix.java` | Entidade |
| `dao/CartaoDao.java` | DAO |
| `dao/ChavePixDao.java` | DAO |
| `view/CartoesActivity.java` | Activity |
| `view/CartoesFragment.java` | Fragment |
| `view/ChavesPixFragment.java` | Fragment |
| `adapter/CartaoAdapter.java` | Adapter |
| `adapter/ChavePixAdapter.java` | Adapter |
| `adapter/ChipSelecaoAdapter.java` | Adapter |
| `layout/activity_cartoes.xml` | Layout |
| `layout/item_cartao.xml` | Layout |
| `layout/item_chave_pix.xml` | Layout |
| `layout/fragment_cartoes.xml` | Layout |
| `layout/fragment_chaves_pix.xml` | Layout |
| `layout/item_chip_selecao.xml` | Layout |
| `drawable/bg_form_field.xml` | Drawable |
| `drawable/bg_button_green.xml` | Drawable |
| `drawable/bg_card_pix.xml` | Drawable |

### Arquivos modificados nesta implementação

| Arquivo | Mudança |
|---------|---------|
| `database/AppDatabase.java` | Versão 7 + entidades `Cartao` e `ChavePix` |
| `view/CadastroDividaActivity.java` | Integração com Cartões/Pix + filtro por tipo |
| `view/ConfiguracoesActivity.java` | Abre `CartoesActivity` no item "Meus Cartões & Bancos" |
| `view/CartoesFragment.java` | Adiciona seletor de tipo (Crédito/Débito) |
| `adapter/CartaoAdapter.java` | Recebe `nomeUsuario` + exibe `tipo` |
| `layout/fragment_cartoes.xml` | Adiciona `spinnerTipoCartao` |
| `layout/activity_cadastro_divida.xml` | Redesign fiel ao Figma |
| `AndroidManifest.xml` | Registra `CartoesActivity` |

### Como testar a integração

1. Faça login no app.
2. Vá em **Configurações → Meus Cartões & Bancos**.
3. Cadastre um cartão de **Crédito** (ex: Nubank 4892).
4. Cadastre um cartão de **Débito** (ex: Itaú 1234).
5. Mude para a aba **Chave Pix de Dívida** e cadastre uma chave (ex: CNPJ).
6. Volte para **Lançar Dívida**.
7. Escolha o tipo **"Cartão de Crédito"** → deve mostrar **apenas o Nubank**.
8. Escolha **"Cartão de Débito"** → deve mostrar **apenas o Itaú**.
9. Escolha **"Pix"** → deve mostrar a chave cadastrada.
10. Desinstale e reinstale o app → faça login → escolha **"Cartão de Crédito"** sem ter cadastrado nada → deve mostrar a **Empty State** com botão "Cadastrar agora".

Se todos esses passos funcionarem, a integração está correta. ✅

---

## 📌 Arquivos modificados na Sprint 4

| Arquivo | Mudança |
|---------|---------|
| `model/Divida.java` | Campo `usuarioId` + campo `valorParcela` |
| `model/Transacao.java` | Campo `usuarioId` |
| `model/Categoria.java` | Campo `usuarioId` + `cor` |
| `model/Usuario.java` | Setters para edição de perfil |
| `dao/DividaDao.java` | Métodos filtrados por usuário |
| `dao/TransacaoDao.java` | Métodos filtrados por usuário |
| `dao/CategoriaDao.java` | CRUD + contagem de dívidas por categoria |
| `dao/UsuarioDao.java` | Métodos `atualizar()` e `deletar()` |
| `database/AppDatabase.java` | Versão 5 + entidade `Categoria` + `valorParcela` |
| `utils/SessionManager.java` | Método `atualizarNome()` |
| `utils/CategoriaSeeder.java` | Categorias padrão na primeira execução |
| `utils/NotificationHelper.java` | **Novo** – canal + envio de notificações |
| `utils/AlarmeHelper.java` | **Novo** – agendamento/cancelamento de alarmes |
| `receiver/LembreteReceiver.java` | **Novo** – BroadcastReceiver de notificações |
| `view/SplashActivity.java` | Removida chamada ao seeder antigo |
| `view/InicioActivity.java` | Filtro por usuário + permissão de notificação |
| `view/DividasActivity.java` | Filtro por usuário + cancelamento de alarme |
| `view/DashboardActivity.java` | Filtro por usuário |
| `view/CadastroDividaActivity.java` | Salva com `usuarioId` + agendamento de alarme + cálculo de parcela |
| `view/CategoriasActivity.java` | CRUD de categorias filtrado por usuário |
| `view/EditarPerfilActivity.java` | Edição de perfil e exclusão de conta |
| `view/ConfiguracoesActivity.java` | Switch funcional de lembretes |
| `adapter/DividaAdapter.java` | Listener recebe `Divida` + exibe valor da parcela |
| `adapter/CategoriaAdapter.java` | Novo adapter com cor e contagem |
| `layout/activity_categorias.xml` | Novo layout |
| `layout/item_categoria.xml` | Novo layout |
| `layout/activity_editar_perfil.xml` | Novo layout |
| `layout/activity_cadastro_divida.xml` | Ajustes visuais |
| `layout/activity_cadastro.xml` | Campo "Nome" |
| `AndroidManifest.xml` | Permissões de notificação + receiver |

## 🗑️ Arquivos removidos

| Arquivo | Motivo |
|---------|--------|
| `database/DatabaseSeeder.java` | Não é mais usado |
| `utils/DadosMock.java` | Código morto |

---

## 🚧 Próximos passos

- [ ] Criar `BootReceiver` para reagendar alarmes após reinicialização
- [ ] Aplicar o mesmo padrão de isolamento caso novas entidades sejam criadas
- [ ] Implementar `Migration` real (não destrutiva) quando houver usuários reais
- [ ] Exportar/importar dados por usuário
- [ ] Backup na nuvem (Firebase Auth + Firestore – opcional)

---

## 📚 Referências

- Issue relacionada: (criar no GitHub)
- Autor da descoberta: Gustavo Piteira
- Data da resolução: 23/09/2026
- Expansão para Categoria: 26/09/2026
- Implementação dos Lembretes: 27/09/2026
- Correção do Cálculo de Parcelas: 27/09/2026
- Implementação de Cartões e Chaves Pix: 28/09/2026
- Integração de Cartões/Pix com Lançar Dívida: 28/09/2026
