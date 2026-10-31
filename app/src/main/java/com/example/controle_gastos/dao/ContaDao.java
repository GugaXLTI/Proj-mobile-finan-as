package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Conta;

@Dao
public interface ContaDao {

    @Insert
    long inserir(Conta conta);

    @Update
    void atualizar(Conta conta);

    @Query("SELECT * FROM contas WHERE usuarioId = :usuarioId LIMIT 1")
    Conta buscarPorUsuario(int usuarioId);

    @Query("DELETE FROM contas WHERE usuarioId = :usuarioId")
    void deletarPorUsuario(int usuarioId);
}