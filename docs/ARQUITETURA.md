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

O mesmo foi feito nas entidades `Transacao` e `Categoria`.

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
```

### 3. AppDatabase atualizado para versão 4

Evolução das versões:
- **v1** → App funcional com Room básico
- **v2** → Adicionado `usuarioId` em `Divida` e `Transacao`
- **v3** → Adicionada entidade `Categoria`
- **v4** → Adicionado campo `cor` em `Categoria`

```java
@Database(
        entities = {Usuario.class, Divida.class, Transacao.class, Categoria.class},
        version = 4,
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

## 📌 Arquivos modificados nesta correção

| Arquivo | Mudança |
|---------|---------|
| `model/Divida.java` | Campo `usuarioId` |
| `model/Transacao.java` | Campo `usuarioId` |
| `model/Categoria.java` | Campo `usuarioId` + `cor` |
| `model/Usuario.java` | Setters para edição de perfil |
| `dao/DividaDao.java` | Métodos filtrados por usuário |
| `dao/TransacaoDao.java` | Métodos filtrados por usuário |
| `dao/CategoriaDao.java` | CRUD + contagem de dívidas por categoria |
| `dao/UsuarioDao.java` | Métodos `atualizar()` e `deletar()` |
| `database/AppDatabase.java` | Versão 4 + entidade `Categoria` |
| `utils/SessionManager.java` | Método `atualizarNome()` |
| `utils/CategoriaSeeder.java` | Categorias padrão na primeira execução |
| `view/SplashActivity.java` | Removida chamada ao seeder antigo |
| `view/InicioActivity.java` | Filtro por usuário |
| `view/DividasActivity.java` | Filtro por usuário |
| `view/DashboardActivity.java` | Filtro por usuário |
| `view/CadastroDividaActivity.java` | Salva com `usuarioId` + categorias dinâmicas |
| `view/CategoriasActivity.java` | CRUD de categorias filtrado por usuário |
| `view/EditarPerfilActivity.java` | Edição de perfil e exclusão de conta |
| `view/ConfiguracoesActivity.java` | Botão "Limpar Tudo" + acesso a Categorias/Editar Perfil |
| `adapter/DividaAdapter.java` | Listener recebe `Divida` |
| `adapter/CategoriaAdapter.java` | Novo adapter com cor e contagem |
| `layout/activity_categorias.xml` | Novo layout |
| `layout/item_categoria.xml` | Novo layout |
| `layout/activity_editar_perfil.xml` | Novo layout |
| `layout/activity_cadastro_divida.xml` | Ajustes visuais |
| `layout/activity_cadastro.xml` | Campo "Nome" |

## 🗑️ Arquivos removidos

| Arquivo | Motivo |
|---------|--------|
| `database/DatabaseSeeder.java` | Não é mais usado |
| `utils/DadosMock.java` | Código morto |

---

## 🚧 Próximos passos

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
