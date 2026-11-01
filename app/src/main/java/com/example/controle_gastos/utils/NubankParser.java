package com.example.controle_gastos.utils;

import android.util.Log;

import com.example.controle_gastos.model.Transacao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NubankParser {

    private static final String TAG = "NubankParser";
    private static final String REGEX_VALOR = "R\\$\\s*?(\\d{1,3}(\\.\\d{3})*,\\d{2}|\\d+,\\d{2})";

    public static Transacao processarTexto(String textoBruto, int usuarioId) {
        if (textoBruto == null || textoBruto.isEmpty()) return null;

        Pattern patternValor = Pattern.compile(REGEX_VALOR);
        Matcher matcherValor = patternValor.matcher(textoBruto);
        double valorExtraido = 0.0;

        if (matcherValor.find()) {
            String valorLimpo = matcherValor.group(1).replace(".", "").replace(",", ".");
            valorExtraido = Double.parseDouble(valorLimpo);
        }

        if (valorExtraido <= 0.0) return null;

        String textoMinusculo = textoBruto.toLowerCase();
        String tipo;
        String descricao;
        String categoria;

        if (textoMinusculo.contains("recebido") || textoMinusculo.contains("transferência recebida") || textoMinusculo.contains("rendimento") || textoMinusculo.contains("pix recebido")) {
            tipo = "ENTRADA";
            descricao = "Pix / Depósito Recebido";
            categoria = "Receita";
        } else {
            tipo = "SAIDA";
            if (textoBruto.contains(" em ")) {
                descricao = textoBruto.split(" em ")[1].trim();
            } else if (textoBruto.contains(" no ")) {
                descricao = textoBruto.split(" no ")[1].trim();
            } else {
                descricao = "Compra / Gasto Cartão";
            }
            categoria = "Outros";
        }

        String dataAtual = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR")).format(new Date());
        Transacao transacao = new Transacao(usuarioId, descricao, valorExtraido, dataAtual, tipo, categoria);

        Log.d(TAG, "Transação processada: " + transacao.tipo + " - R$ " + transacao.valor);
        return transacao;
    }
}