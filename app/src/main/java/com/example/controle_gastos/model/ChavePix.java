package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chaves_pix")
public class ChavePix {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int usuarioId;
    public String tipoChave; // Ex: CNPJ/CPF, Celular, E-mail, Aleatória
    public String chave; // O valor da chave
    public String nomeFavorecido;
    public String banco;
    public String apelidoDivida;

    public ChavePix() {}
}