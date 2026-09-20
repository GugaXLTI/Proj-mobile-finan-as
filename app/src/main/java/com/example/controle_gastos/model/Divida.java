package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "dividas")
public class Divida {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String titulo;
    public double valorTotal;
    public double valorPago;
    public String banco;
    public String categoria;
    public String parcela;
    public String vencimento;
    public boolean pago;

    // Construtor completo (usado pelo Room)
    public Divida(int id, String titulo, double valorTotal, double valorPago,
                  String banco, String categoria, String parcela, String vencimento, boolean pago) {
        this.id = id;
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
        this.banco = banco;
        this.categoria = categoria;
        this.parcela = parcela;
        this.vencimento = vencimento;
        this.pago = pago;
    }

    // Construtor sem ID (usado quando o Room gera o ID automaticamente)
    @Ignore
    public Divida(String titulo, double valorTotal, double valorPago,
                  String banco, String categoria, String parcela, String vencimento, boolean pago) {
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
        this.banco = banco;
        this.categoria = categoria;
        this.parcela = parcela;
        this.vencimento = vencimento;
        this.pago = pago;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public double getValorTotal() { return valorTotal; }
    public double getValorPago() { return valorPago; }
    public double getValorRestante() { return valorTotal - valorPago; }
    public String getBanco() { return banco; }
    public String getCategoria() { return categoria; }
    public String getParcela() { return parcela; }
    public String getVencimento() { return vencimento; }
    public boolean isPago() { return pago; }
    public void setPago(boolean pago) { this.pago = pago; }
}