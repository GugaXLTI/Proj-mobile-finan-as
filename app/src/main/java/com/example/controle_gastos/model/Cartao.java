package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "cartoes")
public class Cartao {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int usuarioId;
    public String instituicao;     // Ex: Nubank, Inter
    public String apelido;         // Ex: Cartão Principal
    public String ultimos4Digitos;
    public String diaVencimento;
    public double limite;
    public String bandeira;        // Ex: Mastercard, Visa
    public String tipo;            // ⭐ NOVO: "Crédito" ou "Débito"

    public Cartao() {}
}