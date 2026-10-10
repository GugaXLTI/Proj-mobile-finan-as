package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.ChavePix;

import java.util.List;

@Dao
public interface ChavePixDao {

    @Insert
    void inserir(ChavePix chavePix);

    @Update
    void atualizar(ChavePix chavePix);

    @Delete
    void deletar(ChavePix chavePix);

    @Query("SELECT * FROM chaves_pix WHERE usuarioId = :usuarioId ORDER BY id DESC")
    List<ChavePix> listarPorUsuario(int usuarioId);

    // ⭐ Apaga todas as chaves Pix do usuário (usado ao restaurar backup)
    @Query("DELETE FROM chaves_pix WHERE usuarioId = :usuarioId")
    void deletarTodosDoUsuario(int usuarioId);
}