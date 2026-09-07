package com.example.controle_gastos.model;

public class CategoriaResumo {
    private String nome;
    private double total;
    private int quantidade;

    public CategoriaResumo(String nome, double total, int quantidade) {
        this.nome = nome;
        this.total = total;
        this.quantidade = quantidade;
    }

    public String getNome() { return nome; }
    public double getTotal() { return total; }
    public int getQuantidade() { return quantidade; }
}