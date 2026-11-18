package com.example.controle_gastos.utils;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;

public class BancoHelper {

    private static final Map<String, String> BANCOS_PACOTES = new HashMap<>();

    static {
        BANCOS_PACOTES.put("nubank", "com.nu.production");
        BANCOS_PACOTES.put("nu", "com.nu.production");
        BANCOS_PACOTES.put("inter", "com.bancointer.android");
        BANCOS_PACOTES.put("itaú", "com.itau");
        BANCOS_PACOTES.put("itau", "com.itau");
        BANCOS_PACOTES.put("bradesco", "com.bradesco");
        BANCOS_PACOTES.put("santander", "com.santander.app");
        BANCOS_PACOTES.put("caixa", "br.com.gabba.Caixa");
        BANCOS_PACOTES.put("banco do brasil", "br.com.bb.android");
        BANCOS_PACOTES.put("bb", "br.com.bb.android");
        BANCOS_PACOTES.put("c6", "com.c6bank.app");
        BANCOS_PACOTES.put("c6 bank", "com.c6bank.app");
        BANCOS_PACOTES.put("picpay", "com.picpay");
        BANCOS_PACOTES.put("mercado pago", "com.mercadopago.wallet");
        BANCOS_PACOTES.put("pix", "com.pix"); // Adicionado para mapeamento
    }

    public static String getPackageName(String nomeBanco) {
        if (nomeBanco == null || nomeBanco.trim().isEmpty()) return null;
        String chave = nomeBanco.toLowerCase().trim();

        if (BANCOS_PACOTES.containsKey(chave)) {
            return BANCOS_PACOTES.get(chave);
        }

        for (Map.Entry<String, String> entry : BANCOS_PACOTES.entrySet()) {
            if (chave.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    // ⭐ MÉTODO ATUALIZADO: Adicionada a cor do PIX
    public static int getCorBanco(String nomeBanco) {
        if (nomeBanco == null) return Color.parseColor("#64748B");
        String chave = nomeBanco.toLowerCase().trim();

        if (chave.contains("pix")) return Color.parseColor("#32BCAD"); // Turquesa PIX
        if (chave.contains("nubank") || chave.equals("nu")) return Color.parseColor("#820AD1"); // Roxo Nubank
        if (chave.contains("inter")) return Color.parseColor("#FF7A00"); // Laranja Inter
        if (chave.contains("itaú") || chave.contains("itau")) return Color.parseColor("#EC7000"); // Laranja Itaú
        if (chave.contains("bradesco")) return Color.parseColor("#CC092F"); // Vermelho Bradesco
        if (chave.contains("santander")) return Color.parseColor("#EC0000"); // Vermelho Santander
        if (chave.contains("caixa")) return Color.parseColor("#0070AF"); // Azul Caixa
        if (chave.contains("c6")) return Color.parseColor("#242424"); // Preto C6
        if (chave.contains("picpay")) return Color.parseColor("#11C76F"); // Verde PicPay
        if (chave.contains("mercado pago")) return Color.parseColor("#009EE3"); // Azul Mercado Pago

        return Color.parseColor("#64748B"); // Cinza padrão para "Genérico" ou desconhecido
    }
}