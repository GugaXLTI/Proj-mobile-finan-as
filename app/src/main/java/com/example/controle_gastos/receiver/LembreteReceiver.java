package com.example.controle_gastos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.controle_gastos.utils.NotificationHelper;

public class LembreteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Recupera os dados da dívida enviados pelo PendingIntent
        int id = intent.getIntExtra("divida_id", 0);
        String titulo = intent.getStringExtra("titulo");
        double valor = intent.getDoubleExtra("valor", 0.0);
        String vencimento = intent.getStringExtra("vencimento");

        // Mostra a notificação
        NotificationHelper.mostrarNotificacao(context, id, titulo, valor, vencimento);
    }
}