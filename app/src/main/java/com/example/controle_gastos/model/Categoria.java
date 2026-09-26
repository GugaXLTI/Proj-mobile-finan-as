package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId;   // Cada usuário tem suas categorias
    public String nome;
    public String cor;      // ⭐ Cor em hexadecimal (ex: "#A855F7")

    public Categoria(int usuarioId, String nome, String cor) {
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.cor = cor;
    }

    @Ignore
    public Categoria(int id, int usuarioId, String nome, String cor) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nome = nome;
        this.cor = cor;
    }

    public int getId() { return id; }
    public int getUsuarioId() { return usuarioId; }
    public String getNome() { return nome; }
    public String getCor() { return cor; }

    public void setNome(String nome) { this.nome = nome; }
    public void setCor(String cor) { this.cor = cor; }
}