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
    void deletar(Divida divida);

    // ======== CONSULTAS POR USUÁRIO (novas) ========

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId")
    List<Divida> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND pago = 0")
    List<Divida> listarNaoPagasPorUsuario(int usuarioId);

    @Query("SELECT * FROM dividas WHERE usuarioId = :usuarioId AND id = :id LIMIT 1")
    Divida buscarPorIdDoUsuario(int id, int usuarioId);

    @Query("SELECT * FROM dividas WHERE id = :id LIMIT 1")
    Divida buscarPorId(int id);
}