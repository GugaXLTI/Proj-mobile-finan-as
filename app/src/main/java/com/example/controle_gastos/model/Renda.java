package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "rendas")
public class Renda {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId;
    public String descricao;
    public double valor;
    public int diaRecebimento;
    public String tipo;

    // ⭐ IMPORTANTE: Construtor vazio que o Room exige
    public Renda() {
    }

    // Construtor para nós usarmos no código
    @Ignore
    public Renda(int usuarioId, String descricao, double valor, int diaRecebimento, String tipo) {
        this.usuarioId = usuarioId;
        this.descricao = descricao;
        this.valor = valor;
        this.diaRecebimento = diaRecebimento;
        this.tipo = tipo;
    }
}