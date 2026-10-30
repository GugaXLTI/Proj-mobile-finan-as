package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Renda;

import java.util.List;

@Dao
public interface RendaDao {

    @Insert
    long inserir(Renda renda);

    @Update
    void atualizar(Renda renda);

    @Delete
    void deletar(Renda renda);

    @Query("SELECT * FROM rendas WHERE usuarioId = :usuarioId")
    List<Renda> listarPorUsuario(int usuarioId);

    // ⭐ Busca apenas as rendas que já caíram na conta até o dia de hoje
    @Query("SELECT * FROM rendas WHERE usuarioId = :usuarioId AND diaRecebimento <= :diaAtual")
    List<Renda> listarRendasRecebidasAteHoje(int usuarioId, int diaAtual);

    @Query("DELETE FROM rendas WHERE usuarioId = :usuarioId")
    void deletarTodosDoUsuario(int usuarioId);
}