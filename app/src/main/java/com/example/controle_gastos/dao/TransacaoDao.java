package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Transacao;

import java.util.List;

@Dao
public interface TransacaoDao {

    @Insert
    long inserir(Transacao transacao);

    @Update
    void atualizar(Transacao transacao);

    @Delete
    void deletar(Transacao transacao);

    // ======== CONSULTAS POR USUÁRIO (novas) ========

    @Query("SELECT * FROM transacoes WHERE usuarioId = :usuarioId")
    List<Transacao> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM transacoes WHERE usuarioId = :usuarioId AND tipo = :tipo")
    List<Transacao> listarPorTipoDoUsuario(int usuarioId, String tipo);
}