package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "contas")
public class Conta {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId; // ⭐ Isolamento por usuário
    public String nomeBanco; // Ex: "Nubank"
    public double saldoInicial; // Valor digitado no onboarding

    public Conta(int usuarioId, String nomeBanco, double saldoInicial) {
        this.usuarioId = usuarioId;
        this.nomeBanco = nomeBanco;
        this.saldoInicial = saldoInicial;
    }
}