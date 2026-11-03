package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Divida;

import java.util.List;

@Dao
public interface DividaDao {

    @Insert
    long inserir(Divida divida);

    @Update
    void atualizar(Divida divida);

    @Delete
    void deletarFisicamente(Divida divida);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 0")
    List<Divida> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0 AND excluida = 0")
    List<Divida> listarNaoPagasPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND id = :id LIMIT 1")
    Divida buscarPorIdDoUsuario(int id, int usuarioId);

    @Query("SELECT * FROM dividas WHERE id = :id LIMIT 1")
    Divida buscarPorId(int id);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 1 ORDER BY vencimento DESC")
    List<Divida> listarExcluidasPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId ORDER BY vencimento DESC")
    List<Divida> listarTodasParaHistorico(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND categoria = :categoria ORDER BY vencimento DESC")
    List<Divida> listarPorCategoria(int usuarioId, String categoria);

    @Query("SELECT COALESCE(MAX(grupoId), 0) FROM dividas")
    int maxGrupoId();

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND grupoId = :grupoId AND excluida = 0 ORDER BY vencimento ASC")
    List<Divida> listarPorGrupo(int usuarioId, int grupoId);

    @Query("DELETE FROM dividas WHERE usuarioId = :usuarioId")
    void deletarTodosDoUsuario(int usuarioId);

    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM dividas " +
            "WHERE usuarioId = :usuarioId AND excluida = 0 AND pago = 1 " +
            "AND substr(vencimento, 4, 7) = :mesAno")
    double somarGastosPagosDoMes(int usuarioId, String mesAno);

    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM dividas " +
            "WHERE usuarioId = :usuarioId AND excluida = 0 AND pago = 1")
    double somarGastosPagosTotal(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 0 AND pago = 0")
    List<Divida> listarAbertasParaChecarAtraso(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0 AND excluida = 0 AND valorTotal = :valor LIMIT 1")
    Divida buscarDividaAbertaPorValor(int usuarioId, double valor);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0 AND excluida = 0 " +
            "AND valorTotal BETWEEN (:valor - 30.0) AND (:valor + 30.0) LIMIT 1")
    Divida buscarDividaAproximadaPorValor(int usuarioId, double valor);

    // ⭐ NOVO: Soma o valor em aberto APENAS das contas que vencem no mês atual ou já atrasadas
    @Query("SELECT COALESCE(SUM(valorTotal), 0.0) FROM dividas " +
            "WHERE usuarioId = :usuarioId AND excluida = 0 AND pago = 0 " +
            "AND substr(vencimento, 4, 7) <= :mesAnoAtual")
    double somarDividasAbertasAteOMes(int usuarioId, String mesAnoAtual);
}