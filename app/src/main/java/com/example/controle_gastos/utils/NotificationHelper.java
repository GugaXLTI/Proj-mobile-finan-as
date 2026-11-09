package com.example.controle_gastos.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.receiver.SugestaoPagamentoReceiver;
import com.example.controle_gastos.view.InicioActivity;

import java.text.NumberFormat;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_LEMBRETES = "lembretes_financeiros";
    private static final String CHANNEL_INSIGHTS = "insights_financeiros";
    private static final String CHANNEL_VENCIMENTOS = "vencimentos_contas";
    private static final String CHANNEL_ATRASOS = "atrasos_contas";
    private static final String CHANNEL_SUGESTAO = "sugestao_pagamento";

    // ⭐ NOVO CANAL FOREGROUND
    private static final String CHANNEL_FOREGROUND = "foreground_service";
    public static final int FOREGROUND_NOTIFICATION_ID = 1001;

    public static void criarCanal(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            manager.createNotificationChannel(new NotificationChannel(CHANNEL_LEMBRETES, "Lembretes Diários", NotificationManager.IMPORTANCE_DEFAULT));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_INSIGHTS, "Insights Financeiros", NotificationManager.IMPORTANCE_LOW));
            manager.createNotificationChannel(new NotificationChannel(CHANNEL_VENCIMENTOS, "Avisos de Vencimento", NotificationManager.IMPORTANCE_HIGH));

            NotificationChannel canalAtrasos = new NotificationChannel(CHANNEL_ATRASOS, "Avisos de Contas Atrasadas", NotificationManager.IMPORTANCE_HIGH);
            canalAtrasos.setDescription("Avisos para contas que passaram do vencimento e você esqueceu de marcar como paga");
            manager.createNotificationChannel(canalAtrasos);

            NotificationChannel canalSugestao = new NotificationChannel(CHANNEL_SUGESTAO, "Sugestões do Espião", NotificationManager.IMPORTANCE_HIGH);
            canalSugestao.setDescription("Quando o app detecta um pagamento que parece ser uma conta sua");
            manager.createNotificationChannel(canalSugestao);

            // ⭐ CANAL SILENCIOSO PARA O FOREGROUND SERVICE
            NotificationChannel canalForeground = new NotificationChannel(CHANNEL_FOREGROUND, "Monitoramento Ativo", NotificationManager.IMPORTANCE_LOW);
            canalForeground.setDescription("Mantém o ORG lendo suas notificações em segundo plano");
            canalForeground.setShowBadge(false);
            manager.createNotificationChannel(canalForeground);
        }
    }

    // ⭐ NOVO MÉTODO: Retorna a notificação fixa para o Foreground Service
    public static Notification getForegroundNotification(Context context) {
        Intent intent = new Intent(context, InicioActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, CHANNEL_FOREGROUND)
                .setSmallIcon(R.drawable.ic_nav_home)
                .setContentTitle("ORG está ativo")
                .setContentText("Monitorando suas notificações para o fluxo de caixa.")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // ⭐ Faz a notificação ser fixa (usuário não consegue apagar)
                .setContentIntent(pendingIntent)
                .build();
    }

    public static void mostrarNotificacaoSugestao(Context context, Divida divida, double valorRealPago) {
        Intent intentMarcarPago = new Intent(context, SugestaoPagamentoReceiver.class);
        intentMarcarPago.putExtra("EXTRA_DIVIDA_ID", divida.getId());
        intentMarcarPago.putExtra("EXTRA_NOTIFICACAO_ID", 99);
        intentMarcarPago.putExtra("EXTRA_VALOR_PAGO", valorRealPago);

        PendingIntent pendingMarcarPago = PendingIntent.getBroadcast(
                context, 99, intentMarcarPago,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Intent intentAbrirApp = new Intent(context, InicioActivity.class);
        PendingIntent pendingAbrirApp = PendingIntent.getActivity(
                context, 100, intentAbrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        String mensagem;
        if (Math.abs(valorRealPago - divida.getValorTotal()) > 0.01) {
            mensagem = "Detectamos um pagamento de " + nf.format(valorRealPago) + ". É a conta \"" + divida.getTitulo() + "\" (estimada em " + nf.format(divida.getValorTotal()) + ")?";
        } else {
            mensagem = "Detectamos um pagamento de " + nf.format(valorRealPago) + ". É a conta \"" + divida.getTitulo() + "\"?";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SUGESTAO)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ORG · Pagamento Detectado")
                .setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingAbrirApp)
                .addAction(0, "Sim, atualizar e pagar", pendingMarcarPago)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(99, builder.build());
        } catch (SecurityException e) { }
    }

    public static void mostrarNotificacaoAtraso(Context context, String tituloDivida, String vencimento) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 3, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String mensagem = "A conta \"" + tituloDivida + "\" venceu em " + vencimento + ". Você já pagou? Não esqueça de marcar como paga no ORG!";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ATRASOS)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("ORG · Conta Atrasada").setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem)).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent).setAutoCancel(true);
        try { NotificationManagerCompat.from(context).notify(3, builder.build()); } catch (SecurityException e) { }
    }

    public static void mostrarNotificacao(Context context, int id, String titulo, double valor, String vencimento) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        String mensagem = "A conta \"" + titulo + "\" no valor de " + nf.format(valor) + " vence em " + vencimento + ".";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_VENCIMENTOS)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("ORG · Conta a vencer").setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem)).setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent).setAutoCancel(true);
        try { NotificationManagerCompat.from(context).notify(id, builder.build()); } catch (SecurityException e) { }
    }

    public static void mostrarNotificacaoInsight(Context context, String mensagem) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INSIGHTS)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("ORG · Insight Financeiro").setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem)).setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent).setAutoCancel(true);
        try { NotificationManagerCompat.from(context).notify(2, builder.build()); } catch (SecurityException e) { }
    }

    public static void mostrarNotificacaoLembrete(Context context, String mensagem) {
        Intent intent = new Intent(context, InicioActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LEMBRETES)
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("ORG · Organize suas finanças").setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem)).setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent).setAutoCancel(true);
        try { NotificationManagerCompat.from(context).notify(1, builder.build()); } catch (SecurityException e) { }
    }
}