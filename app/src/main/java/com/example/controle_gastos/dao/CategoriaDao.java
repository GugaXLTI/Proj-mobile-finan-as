package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Categoria;

import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    long inserir(Categoria categoria);

    @Update
    void atualizar(Categoria categoria);

    @Delete
    void deletar(Categoria categoria);

    @Query("SELECT * FROM categorias WHERE usuarioId = :usuarioId ORDER BY nome ASC")
    List<Categoria> listarPorUsuario(int usuarioId);

    @Query("SELECT * FROM categorias WHERE id = :id LIMIT 1")
    Categoria buscarPorId(int id);

    @Query("SELECT * FROM categorias WHERE usuarioId = :usuarioId AND nome = :nome LIMIT 1")
    Categoria buscarPorNome(int usuarioId, String nome);

    // Conta quantas categorias o usuário tem (para saber se precisa popular)
    @Query("SELECT COUNT(*) FROM categorias WHERE usuarioId = :usuarioId")
    int contarPorUsuario(int usuarioId);
}