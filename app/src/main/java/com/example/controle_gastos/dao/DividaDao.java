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

    @Query("SELECT * FROM dividas")
    List<Divida> listarTodas();

    @Query("SELECT * FROM dividas WHERE pago = 0")
    List<Divida> listarNaoPagas();

    @Query("SELECT * FROM dividas WHERE id = :id LIMIT 1")
    Divida buscarPorId(int id);
}