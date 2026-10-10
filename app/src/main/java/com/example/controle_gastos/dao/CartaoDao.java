package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Cartao;

import java.util.List;

@Dao
public interface CartaoDao {

    @Insert
    void inserir(Cartao cartao);

    @Update
    void atualizar(Cartao cartao);

    @Delete
    void deletar(Cartao cartao);

    @Query("SELECT * FROM cartoes WHERE usuarioId = :usuarioId ORDER BY id DESC")
    List<Cartao> listarPorUsuario(int usuarioId);

    // ⭐ Apaga todos os cartões do usuário (usado ao restaurar backup)
    @Query("DELETE FROM cartoes WHERE usuarioId = :usuarioId")
    void deletarTodosDoUsuario(int usuarioId);
}