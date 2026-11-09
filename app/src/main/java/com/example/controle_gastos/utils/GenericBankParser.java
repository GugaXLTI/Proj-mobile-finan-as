package com.example.controle_gastos.utils;

import android.util.Log;

import com.example.controle_gastos.model.Transacao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericBankParser {

    private static final String TAG = "GenericBankParser";
    // Regex universal para capturar valores em Reais (ex: R$ 10,50 ou R$ 0,43)
    private static final String REGEX_VALOR = "R\\$\\s*?(\\d{1,3}(\\.\\d{3})*,\\d{2}|\\d+,\\d{2})";

    public static Transacao processarTexto(String textoBruto, int usuarioId, String nomeBanco) {
        if (textoBruto == null || textoBruto.isEmpty()) return null;

        // 1. Extrai o valor
        Pattern patternValor = Pattern.compile(REGEX_VALOR);
        Matcher matcherValor = patternValor.matcher(textoBruto);
        double valorExtraido = 0.0;

        if (matcherValor.find()) {
            String valorLimpo = matcherValor.group(1).replace(".", "").replace(",", ".");
            try {
                valorExtraido = Double.parseDouble(valorLimpo);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (valorExtraido <= 0.0) return null;

        String textoMinusculo = textoBruto.toLowerCase();
        String tipo = "SAIDA"; // Padrão
        String categoria = "Outros";
        String descricao = "Transação " + nomeBanco;

        // ⭐ PALAVRAS-CHAVE REFORÇADAS
        boolean isEntrada = textoMinusculo.contains("recebeu") ||
                textoMinusculo.contains("recebido") ||
                textoMinusculo.contains("recebida") ||
                textoMinusculo.contains("entrada") ||
                textoMinusculo.contains("depósito") ||
                textoMinusculo.contains("credito") ||
                textoMinusculo.contains("crédito") ||
                textoMinusculo.contains("creditado") ||
                textoMinusculo.contains("pix de") ||
                textoMinusculo.contains("transferência de");

        boolean isSaida = textoMinusculo.contains("enviou") ||
                textoMinusculo.contains("enviado") ||
                textoMinusculo.contains("pagamento") ||
                textoMinusculo.contains("compra") ||
                textoMinusculo.contains("débito") ||
                textoMinusculo.contains("debito") ||
                textoMinusculo.contains("debitado") ||
                textoMinusculo.contains("pix para") ||
                textoMinusculo.contains("saque") ||
                textoMinusculo.contains("boleto");

        // Se for ENTRADA, tem prioridade (ignora o "enviou" em notificações de recebimento)
        if (isEntrada) {
            tipo = "ENTRADA";
            categoria = "Receita";
            descricao = "Pix / Depósito Recebido (" + nomeBanco + ")";
        } else if (isSaida) {
            tipo = "SAIDA";

            // Tenta extrair o nome do estabelecimento/beneficiário
            if (textoBruto.contains(" em ")) {
                descricao = textoBruto.split(" em ")[1].trim();
            } else if (textoBruto.contains(" no ")) {
                descricao = textoBruto.split(" no ")[1].trim();
            } else if (textoBruto.contains(" para ")) {
                descricao = textoBruto.split(" para ")[1].trim();
            } else {
                descricao = "Compra / Gasto (" + nomeBanco + ")";
            }

            // Tenta inferir uma categoria básica
            if (textoMinusculo.contains("mercado") || textoMinusculo.contains("supermercado")) {
                categoria = "Alimentação";
            } else if (textoMinusculo.contains("farmácia") || textoMinusculo.contains("drogaria")) {
                categoria = "Saúde";
            } else if (textoMinusculo.contains("posto") || textoMinusculo.contains("uber") || textoMinusculo.contains("99")) {
                categoria = "Transporte";
            } else if (textoMinusculo.contains("netflix") || textoMinusculo.contains("spotify") || textoMinusculo.contains("youtube")) {
                categoria = "Assinaturas";
            }
        } else {
            // Se não achou nenhuma palavra forte, salva como saída genérica para não perder o dado
            tipo = "SAIDA";
            descricao = "Transação " + nomeBanco;
        }

        String dataAtual = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR")).format(new Date());
        Transacao transacao = new Transacao(usuarioId, descricao, valorExtraido, dataAtual, tipo, categoria);

        Log.d(TAG, "Transação genérica processada: " + transacao.tipo + " - R$ " + transacao.valor + " (" + nomeBanco + ")");
        return transacao;
    }
}