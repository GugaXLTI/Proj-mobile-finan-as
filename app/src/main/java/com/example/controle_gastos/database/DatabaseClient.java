package com.example.controle_gastos.database;

import android.content.Context;

public class DatabaseClient {

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = AppDatabase.getInstance(context);
        }
        return instance;
    }
}