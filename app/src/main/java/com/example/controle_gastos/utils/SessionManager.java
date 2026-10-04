package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "sessao_usuario";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NOME = "user_nome";
    private static final String KEY_USER_EMAIL = "user_email";

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

    // ⭐ NOVO: salva também o e-mail (chamado no login)
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

    // ⭐ NOVO: atualiza o e-mail na sessão
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

    // ⭐ NOVO: retorna o e-mail salvo na sessão
    public String getEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}