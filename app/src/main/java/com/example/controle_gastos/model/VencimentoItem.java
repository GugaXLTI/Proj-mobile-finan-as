package com.example.controle_gastos.model;

/**
 * Item de vencimento já AGRUPADO por grupoId.
 * Usado na tela de Início para mostrar o Xbox (4x) como 1 item, não 4.
 */
public class VencimentoItem {
    public String titulo;
    public String inicial;              // letra do banco
    public String proximoVencimento;    // dd/MM/yyyy da próxima parcela
    public double valorProxima;         // valor da próxima parcela
    public double totalRestante;        // soma das parcelas restantes do grupo
    public int parcelasRestantes;       // quantas faltam
    public int totalParcelas;           // total do grupo (ex: 4)
    public boolean parcelado;           // true se totalParcelas > 1

    public VencimentoItem() {}
}