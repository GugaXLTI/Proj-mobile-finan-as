package com.example.controle_gastos.utils;

import java.util.HashMap;
import java.util.Map;

public class BancoHelper {

    // Dicionário que mapeia o que o usuário digita (minúsculo) para o pacote do Android
    private static final Map<String, String> BANCOS_PACOTES = new HashMap<>();

    static {
        // Adicione mais bancos aqui conforme necessário
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
    }

    /**
     * Retorna o pacote do app (ex: com.nu.production) baseado no nome do banco (ex: Nubank).
     * Retorna null se o banco não for conhecido.
     */
    public static String getPackageName(String nomeBanco) {
        if (nomeBanco == null || nomeBanco.trim().isEmpty()) return null;
        String chave = nomeBanco.toLowerCase().trim();

        // Busca exata
        if (BANCOS_PACOTES.containsKey(chave)) {
            return BANCOS_PACOTES.get(chave);
        }

        // Busca parcial (ex: "Cartão Nubank" -> contém "nubank")
        for (Map.Entry<String, String> entry : BANCOS_PACOTES.entrySet()) {
            if (chave.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }
}