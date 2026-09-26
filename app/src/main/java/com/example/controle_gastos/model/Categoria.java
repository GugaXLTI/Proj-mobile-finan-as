package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId; // Cada usuário tem suas próprias categorias

    public String nome;

    public Categoria(int usuarioId, String nome) {
        this.usuarioId = usuarioId;
        this.nome = nome;
    }

    @Ignore
    public Categoria(int id, int usuarioId, String nome) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
    }

    public int getId() { return id; }
    public int getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}