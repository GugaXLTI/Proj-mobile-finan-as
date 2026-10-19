package com.example.controle_gastos.model;

import java.util.Locale;

/**
 * POJO que representa o resultado da interpretação de um comprovante.
 *
 * NÃO é @Entity — não vai para o banco. É só um transporte de dados
 * entre o ReceiptParser e a ReceiptShareActivity (e futuramente o
 * CadastroDividaActivity).
 */
public class ParsedReceipt {

    private String titulo;
    private Double valor;         // null se não encontrado
    private String data;          // formato dd/MM/yyyy
    private String banco;         // banco de ORIGEM (quem paga)
    private String categoria;     // inferida por palavra-chave
    private String beneficiario;  // quem recebeu
    private int parcelas;         // 1 para Pix

    public ParsedReceipt() {
        this.parcelas = 1;
    }

    // ==== Getters / Setters ====

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getBeneficiario() { return beneficiario; }
    public void setBeneficiario(String beneficiario) { this.beneficiario = beneficiario; }

    public int getParcelas() { return parcelas; }
    public void setParcelas(int parcelas) { this.parcelas = parcelas; }

    // ==== Helpers ====

    public boolean temValor() { return valor != null && valor > 0; }
    public boolean temData() { return data != null && !data.isEmpty(); }
    public boolean temBanco() { return banco != null && !banco.isEmpty(); }
    public boolean temTitulo() { return titulo != null && !titulo.isEmpty(); }

    /**
     * Formata o valor para exibição ("R$ 20,00").
     */
    public String getValorFormatado() {
        if (!temValor()) return "—";
        return String.format(new Locale("pt", "BR"), "R$ %.2f", valor);
    }

    @Override
    public String toString() {
        return "ParsedReceipt{" +
                "titulo='" + titulo + '\'' +
                ", valor=" + valor +
                ", data='" + data + '\'' +
                ", banco='" + banco + '\'' +
                ", categoria='" + categoria + '\'' +
                ", beneficiario='" + beneficiario + '\'' +
                ", parcelas=" + parcelas +
                '}';
    }
}