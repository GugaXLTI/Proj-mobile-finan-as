package com.example.controle_gastos.model;

public class Divida {
    private int id;
    private String titulo;
    private double valorTotal;
    private double valorPago;
    private String banco;      // ex: Nubank, Inter
    private String categoria;  // ex: Cartão de Crédito, Assinatura
    private String parcela;    // ex: "1/1", "2/3"
    private String vencimento; // ex: "15/10/2026"
    private boolean pago;

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

    // Getters e Setters
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