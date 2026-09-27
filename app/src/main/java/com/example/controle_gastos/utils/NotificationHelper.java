package com.example.controle_gastos.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.controle_gastos.R;

public class NotificationHelper {

    private static final String CANAL_ID = "lembretes_fatura";
    private static final String CANAL_NOME = "Lembretes de Fatura";
    private static final String CANAL_DESCRICAO = "Avisos de vencimento próximo";

    /**
     * Cria o canal de notificação (necessário para Android 8+)
     */
    public static void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CANAL_ID,
                    CANAL_NOME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            canal.setDescription(CANAL_DESCRICAO);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(canal);
            }
        }
    }

    /**
     * Mostra a notificação de lembrete de fatura
     */
    public static void mostrarNotificacao(Context context, int id, String titulo, double valor, String vencimento) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CANAL_ID)
                .setSmallIcon(R.drawable.ic_wallet)
                .setContentTitle("📅 Fatura perto de vencer")
                .setContentText(titulo + " — R$ " + String.format("%.2f", valor) + " vence em " + vencimento)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(titulo + "\nValor: R$ " + String.format("%.2f", valor)
                                + "\nVencimento: " + vencimento))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        try {
            manager.notify(id, builder.build());
        } catch (SecurityException e) {
            // Permissão negada
            e.printStackTrace();
        }
    }
}