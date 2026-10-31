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
    // Regex para capturar formatos como R$ 45,90 ou R$ 1.250,00
    private static final String REGEX_VALOR = "R\\$\\s*?(\\d{1,3}(\\.\\d{3})*,\\d{2}|\\d+,\\d{2})";

    public static Transacao processarTexto(String textoBruto, int usuarioId) {
        if (textoBruto == null || textoBruto.isEmpty()) return null;

        // 1. Extração do Valor Monetário
        Pattern patternValor = Pattern.compile(REGEX_VALOR);
        Matcher matcherValor = patternValor.matcher(textoBruto);
        double valorExtraido = 0.0;

        if (matcherValor.find()) {
            String valorLimpo = matcherValor.group(1).replace(".", "").replace(",", ".");
            valorExtraido = Double.parseDouble(valorLimpo);
        }

        if (valorExtraido <= 0.0) return null; // Aborta se não achar valor válido

        // 2. Classificação de Regra de Negócio (Entrada vs Saída)
        String textoMinusculo = textoBruto.toLowerCase();
        String tipo;
        String descricao;
        String categoria;

        if (textoMinusculo.contains("recebido") || textoMinusculo.contains("transferência recebida") || textoMinusculo.contains("rendimento")) {
            tipo = "ENTRADA";
            descricao = "Pix / Depósito Recebido";
            categoria = "Receita";
        } else {
            tipo = "SAIDA";
            // Tenta isolar o nome do local
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

        // ⭐ CORREÇÃO: Usando o construtor direto em vez do objeto vazio
        Transacao transacao = new Transacao(usuarioId, descricao, valorExtraido, dataAtual, tipo, categoria);

        Log.d(TAG, "Transação processada: " + transacao.tipo + " - R$ " + transacao.valor);
        return transacao;
    }
}