package com.example.controle_gastos.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.controle_gastos.model.Usuario;

@Dao
public interface UsuarioDao {

    @Insert
    long inserir(Usuario usuario);

    @Update
    void atualizar(Usuario usuario);

    @Delete
    void deletar(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE email = :email AND senha = :senha LIMIT 1")
    Usuario login(String email, String senha);

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    Usuario buscarPorEmail(String email);

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    Usuario buscarPorId(int id);

    // ⭐ Fallback: busca por nome (usado quando a sessão perde o ID)
    @Query("SELECT * FROM usuarios WHERE nome = :nome LIMIT 1")
    Usuario buscarPorNome(String nome);

    // ⭐ Último recurso: pega o único usuário do banco (útil no MVP)
    @Query("SELECT * FROM usuarios LIMIT 1")
    Usuario buscarPrimeiroUsuario();

    // ⭐ Conta quantos usuários existem (para o fallback não pegar o errado)
    @Query("SELECT COUNT(*) FROM usuarios")
    int contarUsuarios();
}