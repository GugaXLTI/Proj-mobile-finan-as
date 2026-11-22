package com.example.controle_gastos.utils;

import android.graphics.Color;
import com.example.controle_gastos.R;

import java.util.HashMap;
import java.util.Map;

public class BancoHelper {

    private static final Map<String, String> BANCOS_PACOTES = new HashMap<>();

    static {
        BANCOS_PACOTES.put("nubank", "com.nu.production");
        BANCOS_PACOTES.put("nu", "com.nu.production");
        BANCOS_PACOTES.put("inter", "br.com.intermedium"); // Corrigido para o Inter
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
        BANCOS_PACOTES.put("pix", "com.pix");
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

    public static int getCorBanco(String nomeBanco) {
        if (nomeBanco == null) return Color.parseColor("#64748B");
        String chave = nomeBanco.toLowerCase().trim();

        if (chave.contains("pix")) return Color.parseColor("#32BCAD");
        if (chave.contains("nubank") || chave.equals("nu")) return Color.parseColor("#820AD1");
        if (chave.contains("inter")) return Color.parseColor("#FF7A00");
        if (chave.contains("itaú") || chave.contains("itau")) return Color.parseColor("#EC7000");
        if (chave.contains("bradesco")) return Color.parseColor("#CC092F");
        if (chave.contains("santander")) return Color.parseColor("#EC0000");
        if (chave.contains("caixa")) return Color.parseColor("#0070AF");
        if (chave.contains("c6")) return Color.parseColor("#242424");
        if (chave.contains("picpay")) return Color.parseColor("#11C76F");
        if (chave.contains("mercado pago")) return Color.parseColor("#009EE3");

        return Color.parseColor("#64748B");
    }

    public static int getLogoBanco(String nomeBanco) {
        if (nomeBanco == null) return 0;
        String chave = nomeBanco.toLowerCase().trim();

        if (chave.contains("nubank") || chave.equals("nu")) return R.drawable.logo_nubank;

        // ⭐ ATENÇÃO: Descomente a linha do banco APENAS se você tiver certeza que a imagem
        // (ex: logo_inter.png) está na pasta drawable. Se descomentar e não tiver a imagem, o app não compila.

         if (chave.contains("inter")) return R.drawable.logo_inter;
         if (chave.contains("itaú") || chave.contains("itau")) return R.drawable.logo_itau;
         if (chave.contains("bradesco")) return R.drawable.logo_bradesco;
         if (chave.contains("santander")) return R.drawable.logo_santander;
         if (chave.contains("caixa")) return R.drawable.logo_caixa;
        // if (chave.contains("banco do brasil") || chave.equals("bb")) return R.drawable.logo_bb;
        // if (chave.contains("c6")) return R.drawable.logo_c6;
        // if (chave.contains("picpay")) return R.drawable.logo_picpay;
        // if (chave.contains("mercado pago")) return R.drawable.logo_mercadopago;

        return 0;
    }
}