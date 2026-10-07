package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "dividas")
public class Divida {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId;

    public String titulo;
    public double valorTotal;
    public double valorParcela;
    public double valorPago;
    public String banco;
    public String categoria;
    public String parcela;
    public String vencimento;
    public boolean pago;
    public boolean excluida; // ⭐ NOVO: soft delete

    // ============================================================
    // Construtor COMPLETO (usado pelo Room para ler do banco)
    // ============================================================
    public Divida(int id, int usuarioId, String titulo, double valorTotal, double valorParcela, double valorPago,
                  String banco, String categoria, String parcela, String vencimento, boolean pago, boolean excluida) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.valorParcela = valorParcela;
        this.valorPago = valorPago;
        this.banco = banco;
        this.categoria = categoria;
        this.parcela = parcela;
        this.vencimento = vencimento;
        this.pago = pago;
        this.excluida = excluida;
    }

    // ============================================================
    // Construtor SEM ID e SEM excluida (conveniência para criação)
    // Assume excluida = false automaticamente
    // ⭐ Use este na maioria dos casos (nova dívida sempre nasce ativa)
    // ============================================================
    @Ignore
    public Divida(int usuarioId, String titulo, double valorTotal, double valorParcela, double valorPago,
                  String banco, String categoria, String parcela, String vencimento, boolean pago) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.valorParcela = valorParcela;
        this.valorPago = valorPago;
        this.banco = banco;
        this.categoria = categoria;
        this.parcela = parcela;
        this.vencimento = vencimento;
        this.pago = pago;
        this.excluida = false; // ⭐ Padrão: sempre nasce ativa
    }

    // ============================================================
    // Construtor SEM ID COM excluida (para casos especiais)
    // Use apenas se você PRECISA criar uma dívida já excluída
    // ============================================================
    @Ignore
    public Divida(int usuarioId, String titulo, double valorTotal, double valorParcela, double valorPago,
                  String banco, String categoria, String parcela, String vencimento, boolean pago, boolean excluida) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.valorParcela = valorParcela;
        this.valorPago = valorPago;
        this.banco = banco;
        this.categoria = categoria;
        this.parcela = parcela;
        this.vencimento = vencimento;
        this.pago = pago;
        this.excluida = excluida;
    }

    public int getId() { return id; }
    public int getUsuarioId() { return usuarioId; }
    public String getTitulo() { return titulo; }
    public double getValorTotal() { return valorTotal; }
    public double getValorParcela() { return valorParcela; }
    public double getValorPago() { return valorPago; }
    public double getValorRestante() { return valorTotal - valorPago; }
    public String getBanco() { return banco; }
    public String getCategoria() { return categoria; }
    public String getParcela() { return parcela; }
    public String getVencimento() { return vencimento; }
    public boolean isPago() { return pago; }
    public boolean isExcluida() { return excluida; }
    public void setPago(boolean pago) { this.pago = pago; }
    public void setExcluida(boolean excluida) { this.excluida = excluida; }
}