package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "sessao_usuario";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NOME = "user_nome";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_ONBOARDING_CONCLUIDO = "onboarding_concluido";
    private static final String KEY_BIOMETRIA_ATIVA = "biometria_ativa";
    private static final String KEY_BIOMETRIA_USER_ID = "biometria_user_id";
    private static final String KEY_BIOMETRIA_NOME = "biometria_user_nome";
    private static final String KEY_BIOMETRIA_EMAIL = "biometria_user_email";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void salvarSessao(int userId, String nome) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NOME, nome);
        editor.apply();
    }

    public void salvarSessao(int userId, String nome, String email) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NOME, nome);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public void atualizarNome(String novoNome) {
        editor.putString(KEY_USER_NOME, novoNome);
        editor.apply();
    }

    public void atualizarEmail(String novoEmail) {
        editor.putString(KEY_USER_EMAIL, novoEmail);
        editor.apply();
    }

    public boolean isLogado() {
        return prefs.getInt(KEY_USER_ID, -1) != -1;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getNome() {
        return prefs.getString(KEY_USER_NOME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // ⭐ CORREÇÃO: Limpar a flag de onboarding ao deslogar
    public void logout() {
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_NOME);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_ONBOARDING_CONCLUIDO); // Limpa a flag global
        editor.apply();
    }

    public boolean isOnboardingConcluido() {
        return prefs.getBoolean(KEY_ONBOARDING_CONCLUIDO, false);
    }

    public void setOnboardingConcluido(boolean concluido) {
        editor.putBoolean(KEY_ONBOARDING_CONCLUIDO, concluido);
        editor.apply();
    }

    public boolean isBiometriaAtiva() {
        return prefs.getBoolean(KEY_BIOMETRIA_ATIVA, false);
    }

    public void setBiometriaAtiva(boolean ativa) {
        editor.putBoolean(KEY_BIOMETRIA_ATIVA, ativa);
        editor.apply();
    }

    public void salvarUsuarioBiometrico(int userId, String nome, String email) {
        editor.putInt(KEY_BIOMETRIA_USER_ID, userId);
        editor.putString(KEY_BIOMETRIA_NOME, nome);
        editor.putString(KEY_BIOMETRIA_EMAIL, email);
        editor.putBoolean(KEY_BIOMETRIA_ATIVA, true);
        editor.apply();
    }

    public void limparBiometria() {
        editor.remove(KEY_BIOMETRIA_USER_ID);
        editor.remove(KEY_BIOMETRIA_NOME);
        editor.remove(KEY_BIOMETRIA_EMAIL);
        editor.putBoolean(KEY_BIOMETRIA_ATIVA, false);
        editor.apply();
    }

    public int getBiometriaUserId() {
        return prefs.getInt(KEY_BIOMETRIA_USER_ID, -1);
    }

    public String getBiometriaNome() {
        return prefs.getString(KEY_BIOMETRIA_NOME, "");
    }

    public String getBiometriaEmail() {
        return prefs.getString(KEY_BIOMETRIA_EMAIL, "");
    }
}