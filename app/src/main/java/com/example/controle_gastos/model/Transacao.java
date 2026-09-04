package com.example.controle_gastos.model;

public class Transacao {
    private String id;
    private String descricao;
    private double valor;
    private String data;
    private String tipo; // "RECEITA" ou "DESPESA"
    private String categoria; // "Alimentação", "Transporte", etc.

    public Transacao() {
    }

    public Transacao(String id, String descricao, double valor, String data, String tipo, String categoria) {
        this.id = id;
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.tipo = tipo;
        this.categoria = categoria;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}