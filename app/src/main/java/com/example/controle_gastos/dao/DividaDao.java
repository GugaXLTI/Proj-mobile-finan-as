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

    // ⚠️ Use com cuidado: apaga de verdade do banco.
    // Só use no "Limpar Tudo" das Configurações.
    @Delete
    void deletarFisicamente(Divida divida);

    // ======== CONSULTAS POR USUÁRIO (agora ignoram excluídas) ========

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 0")
    List<Divida> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0 AND excluida = 0")
    List<Divida> listarNaoPagasPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND id = :id LIMIT 1")
    Divida buscarPorIdDoUsuario(int id, int usuarioId);

    @Query("SELECT * FROM dividas WHERE id = :id LIMIT 1")
    Divida buscarPorId(int id);

    // ======== CONSULTAS PARA O HISTÓRICO (Sprint 6) ========

    // ⭐ Lista APENAS as dívidas excluídas (para mostrar no histórico)
    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND excluida = 1 ORDER BY vencimento DESC")
    List<Divida> listarExcluidasPorUsuario(int usuarioId);

    // ⭐ Lista TODAS (incluindo excluídas) — usado no Histórico
    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId ORDER BY vencimento DESC")
    List<Divida> listarTodasParaHistorico(int usuarioId);

    // ⭐ Lista por categoria (para o Histórico filtrado)
    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND categoria = :categoria ORDER BY vencimento DESC")
    List<Divida> listarPorCategoria(int usuarioId, String categoria);
}