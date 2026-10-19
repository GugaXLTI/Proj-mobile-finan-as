package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.controle_gastos.model.ComprovanteProcessado;

/**
 * DAO do cache de OCR.
 *
 * Todas as queries filtram por usuarioId para garantir isolamento
 * entre contas (Regra de Ouro #3).
 */
@Dao
public interface ComprovanteProcessadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long inserir(ComprovanteProcessado c);

    @Query("SELECT * FROM comprovantes_processados WHERE usuarioId = :userId AND uri = :uri LIMIT 1")
    ComprovanteProcessado buscarPorUri(int userId, String uri);

    @Query("SELECT COUNT(*) FROM comprovantes_processados WHERE usuarioId = :userId")
    int contarPorUsuario(int userId);

    @Query("DELETE FROM comprovantes_processados WHERE usuarioId = :userId")
    void deletarTodosDoUsuario(int userId);

    @Query("DELETE FROM comprovantes_processados WHERE timestamp < :limite")
    void deletarAntigos(long limite);
}