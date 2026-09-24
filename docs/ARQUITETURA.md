# Documentação de Arquitetura – App Gestão de Finanças

## 🐛 Problema Identificado: Dívidas não vinculadas ao usuário

### Data da descoberta
23/09/2026

### Data da correção
23/09/2026

### Status
✅ **RESOLVIDO**

---

## Descrição do problema

A tabela `Divida` do banco Room **não possuía um campo `usuarioId`** que vinculasse cada dívida ao usuário que a cadastrou. Isso significava que:

- Qualquer usuário que fizesse login no app via **todas as dívidas** cadastradas no banco
- Se duas pessoas diferentes usassem o mesmo celular, cada uma via as dívidas da outra
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

O mesmo foi feito na entidade `Transacao`.

### 2. DAOs atualizados para filtrar por usuário

```java
@Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId")
List<Divida> listarPorUsuario(int usuarioId);

@Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0")
List<Divida> listarNaoPagasPorUsuario(int usuarioId);
```

### 3. AppDatabase atualizado para versão 2

```java
@Database(
        entities = {Usuario.class, Divida.class, Transacao.class},
        version = 2,  // ← MUDOU DE 1 PARA 2
        exportSchema = false
)
```

Com `fallbackToDestructiveMigration()` para recriar o banco do zero (seguro no MVP).

### 4. Activities atualizadas para passar o `userId`

Todas as telas que consultam dívidas agora usam `session.getUserId()`:

- `InicioActivity` → `listarPorUsuario(session.getUserId())`
- `DividasActivity` → `listarPorUsuario(session.getUserId())`
- `DashboardActivity` → `listarPorUsuario(session.getUserId())`
- `CadastroDividaActivity` → salva com `session.getUserId()`

### 5. Remoção do `DatabaseSeeder` e `DadosMock`

Como o usuário agora começa com o banco vazio, os arquivos de seed foram **deletados**:

- `DatabaseSeeder.java` (removido)
- `DadosMock.java` (removido)

### 6. Botão "Limpar Tudo" em Configurações

Para testes internos, foi adicionado um diálogo em Configurações que apaga **todos os dados do banco** (dívidas, transações e usuários).

---

## 🧪 Como testar o isolamento entre contas

1. Desinstale o app (para limpar o banco antigo).
2. Crie uma conta (ex: `tiago@email.com`).
3. Cadastre uma dívida.
4. Faça logout → crie outra conta (ex: `ana@email.com`).
5. **Ana deve ver a lista vazia** (sem as dívidas do Tiago).
6. Cadastre uma dívida na conta da Ana.
7. Faça logout → login como Tiago → **só a dívida do Tiago aparece**.

Se todos esses passos funcionarem, o isolamento por usuário está correto. ✅

---

## 📌 Arquivos modificados nesta correção

| Arquivo | Mudança |
|---------|---------|
| `model/Divida.java` | Campo `usuarioId` |
| `model/Transacao.java` | Campo `usuarioId` |
| `dao/DividaDao.java` | Métodos filtrados por usuário |
| `dao/TransacaoDao.java` | Métodos filtrados por usuário |
| `dao/UsuarioDao.java` | Método `buscarPorId()` |
| `database/AppDatabase.java` | Versão 2 + migração destrutiva |
| `view/SplashActivity.java` | Removida chamada ao seeder |
| `view/InicioActivity.java` | Filtro por usuário |
| `view/DividasActivity.java` | Filtro por usuário |
| `view/DashboardActivity.java` | Filtro por usuário |
| `view/CadastroDividaActivity.java` | Salva com `usuarioId` |
| `view/ConfiguracoesActivity.java` | Botão "Limpar Tudo" |
| `adapter/DividaAdapter.java` | Listener recebe `Divida` |
| `layout/activity_cadastro_divida.xml` | Ajustes visuais |
| `layout/activity_cadastro.xml` | Campo "Nome" |

## 🗑️ Arquivos removidos

| Arquivo | Motivo |
|---------|--------|
| `database/DatabaseSeeder.java` | Não é mais usado |
| `utils/DadosMock.java` | Código morto |

---

## 🚧 Próximos passos

- [ ] Aplicar o mesmo isolamento para a entidade `Transacao` nas próximas telas
- [ ] Implementar `Migration` real (não destrutiva) quando houver usuários reais
- [ ] Exportar/importar dados por usuário
- [ ] Backup na nuvem (Firebase Auth + Firestore – opcional)

---

## 📚 Referências

- Issue relacionada: (criar no GitHub)
- Autor da descoberta: Gustavo Piteira
- Data da resolução: 23/09/2026
