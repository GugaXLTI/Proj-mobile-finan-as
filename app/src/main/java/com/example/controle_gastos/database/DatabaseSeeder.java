package com.example.controle_gastos.database;

import android.content.Context;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.utils.DadosMock;

import java.util.List;

public class DatabaseSeeder {

    public static void popularSeVazio(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);

        // Popula dívidas
        if (db.dividaDao().listarTodas().isEmpty()) {
            List<Divida> dividas = DadosMock.getDividasIniciais();
            for (Divida d : dividas) {
                db.dividaDao().inserir(d);
            }
        }

        // Popula transações
        if (db.transacaoDao().listarTodas().isEmpty()) {
            List<Transacao> transacoes = DadosMock.getTransacoesIniciais();
            for (Transacao t : transacoes) {
                db.transacaoDao().inserir(t);
            }
        }
    }
}