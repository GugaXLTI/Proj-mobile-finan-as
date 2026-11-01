package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.controle_gastos.model.Transacao;

import java.util.List;

@Dao
public interface TransacaoDao {

    // ⭐ CORREÇÃO: Mudou de void para long para retornar o ID gerado
    @Insert
    long inserir(Transacao transacao);

    @Query("SELECT * FROM transacoes WHERE usuarioId = :usuarioId ORDER BY data DESC")
    List<Transacao> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM transacoes WHERE usuarioId = :usuarioId AND substr(data, 4, 7) = :mesAno ORDER BY data DESC")
    List<Transacao> listarPorMes(int usuarioId, String mesAno);

    @Query("SELECT COALESCE(SUM(valor), 0.0) FROM transacoes WHERE usuarioId = :usuarioId AND tipo = 'ENTRADA'")
    double somarEntradas(int usuarioId);

    @Query("SELECT COALESCE(SUM(valor), 0.0) FROM transacoes WHERE usuarioId = :usuarioId AND tipo = 'SAIDA'")
    double somarSaidas(int usuarioId);

    @Query("DELETE FROM transacoes WHERE usuarioId = :usuarioId")
    void deletarTodosDoUsuario(int usuarioId);
}