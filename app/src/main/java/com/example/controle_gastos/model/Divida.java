package com.example.controle_gastos.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "dividas")
public class Divida {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int usuarioId;
    public int grupoId; // ⭐ NOVO: 0 = sem grupo, >0 = parcela de um grupo

    public String titulo;
    public double valorTotal;
    public double valorParcela;
    public double valorPago;
    public String banco;
    public String categoria;
    public String parcela;
    public String vencimento;
    public boolean pago;
    public boolean excluida;

    // Construtor COMPLETO (Room)
    public Divida(int id, int usuarioId, int grupoId, String titulo, double valorTotal, double valorParcela,
                  double valorPago, String banco, String categoria, String parcela, String vencimento,
                  boolean pago, boolean excluida) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.grupoId = grupoId;
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

    // Construtor SEM ID/SEM grupoId (conveniência)
    @Ignore
    public Divida(int usuarioId, String titulo, double valorTotal, double valorParcela, double valorPago,
                  String banco, String categoria, String parcela, String vencimento, boolean pago) {
        this.usuarioId = usuarioId;
        this.grupoId = 0;
        this.titulo = titulo;
        this.valorTotal = valorTotal;
        this.valorParcela = valorParcela;
        this.valorPago = valorPago;
        this.banco = banco;
        this.categoria = categoria;
        this.parcela = parcela;
        this.vencimento = vencimento;
        this.pago = pago;
        this.excluida = false;
    }

    // Construtor SEM ID, COM grupoId
    @Ignore
    public Divida(int usuarioId, int grupoId, String titulo, double valorTotal, double valorParcela,
                  double valorPago, String banco, String categoria, String parcela, String vencimento,
                  boolean pago, boolean excluida) {
        this.usuarioId = usuarioId;
        this.grupoId = grupoId;
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
    public int getGrupoId() { return grupoId; }
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