package com.example.controle_gastos.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.controle_gastos.R;
import com.example.controle_gastos.view.InicioActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_LEMBRETES = "lembretes_financeiros";
    private static final String CHANNEL_INSIGHTS = "insights_financeiros";
    private static final String CHANNEL_VENCIMENTOS = "vencimentos_contas"; // ⭐ Novo canal para o Receiver

    public static void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            // 1. Canal de Lembretes diários (WorkManager)
            NotificationChannel canalLembretes = new NotificationChannel(
                    CHANNEL_LEMBRETES,
                    "Lembretes Diários",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            canalLembretes.setDescription("Lembretes para cadastrar despesas");
            manager.createNotificationChannel(canalLembretes);

            // 2. Canal de Insights (WorkManager)
            NotificationChannel canalInsights = new NotificationChannel(
                    CHANNEL_INSIGHTS,
                    "Insights Financeiros",
                    NotificationManager.IMPORTANCE_LOW
            );
            canalInsights.setDescription("Resumo dos seus gastos por categoria");
            manager.createNotificationChannel(canalInsights);

            // 3. Canal de Vencimentos (AlarmeHelper/Receiver)
            NotificationChannel canalVencimentos = new NotificationChannel(
                    CHANNEL_VENCIMENTOS,
                    "Avisos de Vencimento",
                    NotificationManager.IMPORTANCE_HIGH
            );
            canalVencimentos.setDescription("Avisos de contas que estão vencendo");
            manager.createNotificationChannel(canalVencimentos);
        }
    }

    // ⭐ MÉTODO RESTAURADO: Usado pelo LembreteReceiver para contas a vencer
    public static void mostrarNotificacao(Context context, int id, String titulo, double valor, String vencimento) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String mensagem = "A conta \"" + titulo + "\" no valor de " + nf.format(valor) + " vence em " + vencimento + ".";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_VENCIMENTOS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ORG · Conta a vencer")
                .setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build());
        } catch (SecurityException e) {
            // Permissão negada
        }
    }

    // Método novo: Insight de gastos
    public static void mostrarNotificacaoInsight(Context context, String mensagem) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INSIGHTS)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ORG · Insight Financeiro")
                .setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(2, builder.build());
        } catch (SecurityException e) {
            // Permissão negada
        }
    }

    // Método novo: Lembrete amigável
    public static void mostrarNotificacaoLembrete(Context context, String mensagem) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LEMBRETES)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ORG · Organize suas finanças")
                .setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(1, builder.build());
        } catch (SecurityException e) {
            // Permissão negada
        }
    }
}