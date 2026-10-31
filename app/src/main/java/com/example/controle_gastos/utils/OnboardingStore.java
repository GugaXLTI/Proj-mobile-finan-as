package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.controle_gastos.database.AppDatabase;

/**
 * Guarda, por usuário, o saldo inicial informado no onboarding e se ele foi concluído.
 * Solução temporária até a tabela Conta existir (aí o saldo migra para o Room).
 */
public final class OnboardingStore {

    private static final String PREFS = "onboarding_store";

    private OnboardingStore() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    // ---- Saldo inicial (guardado em centavos para não perder precisão) ----

    public static void salvarSaldoInicial(Context c, int userId, double valor) {
        prefs(c).edit().putLong("saldo_inicial_" + userId, Math.round(valor * 100)).apply();
    }

    public static boolean temSaldoInicial(Context c, int userId) {
        return prefs(c).contains("saldo_inicial_" + userId);
    }

    public static double getSaldoInicial(Context c, int userId) {
        return prefs(c).getLong("saldo_inicial_" + userId, 0L) / 100.0;
    }

    // ---- Onboarding concluído (por usuário, não por aparelho) ----

    public static void setConcluido(Context c, int userId, boolean concluido) {
        prefs(c).edit().putBoolean("concluido_" + userId, concluido).apply();
    }

    public static boolean isConcluido(Context c, int userId) {
        return prefs(c).getBoolean("concluido_" + userId, false);
    }

    /**
     * Precisa passar pelo onboarding quando ainda não concluiu E não tem rendas antigas.
     * (Quem já tinha rendas do fluxo anterior não é obrigado a refazer.)
     */
    public static boolean precisaOnboarding(Context c, AppDatabase db, int userId) {
        if (isConcluido(c, userId)) return false;
        return db.rendaDao().listarPorUsuario(userId).isEmpty();
    }

    // ---- Limpeza (evita herdar dados de um id reaproveitado) ----

    /** Apaga os dados de onboarding de um usuário (use ao excluir a conta ou criar uma nova). */
    public static void limparUsuario(Context c, int userId) {
        prefs(c).edit()
                .remove("saldo_inicial_" + userId)
                .remove("concluido_" + userId)
                .apply();
    }

    /** Apaga tudo (use junto com "Limpar tudo" do banco). */
    public static void limparTudo(Context c) {
        prefs(c).edit().clear().apply();
    }
}