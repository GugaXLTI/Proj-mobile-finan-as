package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "transacoes")
public class Transacao {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String descricao;
    public double valor;
    public String data;
    public String tipo;
    public String categoria;

    public Transacao(int id, String descricao, double valor, String data, String tipo, String categoria) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.categoria = categoria;
    }

    @Ignore
    public Transacao(String descricao, double valor, String data, String tipo, String categoria) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.categoria = categoria;
    }

    public int getId() { return id; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public String getData() { return data; }
    public String getTipo() { return tipo; }
    public String getCategoria() { return categoria; }
}