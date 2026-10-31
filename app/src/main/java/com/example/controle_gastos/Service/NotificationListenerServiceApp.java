package com.example.controle_gastos.Service; // ⭐ CORREÇÃO: S maiúsculo

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.utils.NubankParser;
import com.example.controle_gastos.utils.SessionManager;

public class NotificationListenerServiceApp extends NotificationListenerService {

    private static final String TAG = "NotifListener";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String packageName = sbn.getPackageName();

        if (!packageName.equals("com.nu.production") &&
                !packageName.equals("com.bancointer.android") &&
                !packageName.equals("com.itau")) {
            return;
        }

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

        if (text == null) return;

        // ⭐ CORREÇÃO: Removido o .toString() desnecessário do title
        String textoCompleto = (title != null ? title : "") + " " + text;
        Log.d(TAG, "Notificação capturada do " + packageName + ": " + textoCompleto);

        SessionManager session = new SessionManager(this);
        int userId = session.getUserId();
        if (userId == -1) return;

        Transacao novaTransacao = NubankParser.processarTexto(textoCompleto, userId);

        if (novaTransacao != null) {
            AppDatabase db = AppDatabase.getInstance(this);
            db.transacaoDao().inserir(novaTransacao);
            Log.d(TAG, "Transação salva no banco com sucesso!");
        }
    }
}