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

    // ======== CONSULTAS POR USUÁRIO ========

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 0")
    List<Divida> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0 AND excluida = 0")
    List<Divida> listarNaoPagasPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND id = :id LIMIT 1")
    Divida buscarPorIdDoUsuario(int id, int usuarioId);

    @Query("SELECT * FROM dividas WHERE id = :id LIMIT 1")
    Divida buscarPorId(int id);

    // ======== HISTÓRICO ========

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 1 ORDER BY vencimento DESC")
    List<Divida> listarExcluidasPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId ORDER BY vencimento DESC")
    List<Divida> listarTodasParaHistorico(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND categoria = :categoria ORDER BY vencimento DESC")
    List<Divida> listarPorCategoria(int usuarioId, String categoria);

    // ======== SPRINT 6 — PARCELAMENTO ========

    @Query("SELECT COALESCE(MAX(grupoId), 0) FROM dividas")
    int maxGrupoId();

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND grupoId = :grupoId AND excluida = 0 ORDER BY vencimento ASC")
    List<Divida> listarPorGrupo(int usuarioId, int grupoId);

    // ======== BACKUP (Sprint 6) ========

    // ⭐ Apaga todas as dívidas do usuário (usado ao restaurar backup)
    @Query("DELETE FROM dividas WHERE usuarioId = :usuarioId")
    void deletarTodosDoUsuario(int usuarioId);
}