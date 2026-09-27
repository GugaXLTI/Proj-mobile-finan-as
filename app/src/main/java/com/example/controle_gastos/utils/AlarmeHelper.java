package com.example.controle_gastos.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.receiver.LembreteReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmeHelper {

    private static final String PREF_NAME = "prefs_lembretes";
    private static final String KEY_ATIVO = "lembretes_ativo";
    private static final int DIAS_ANTES = 3;

    // ============ PREFERÊNCIA ============

    public static boolean isLembretesAtivos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ATIVO, true); // padrão: ativo
    }

    public static void setLembretesAtivos(Context context, boolean ativo) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ATIVO, ativo).apply();
    }

    // ============ AGENDAR ============

    public static void agendar(Context context, Divida divida) {
        if (!isLembretesAtivos(context)) return;
        if (divida.isPago()) return;

        // Parse da data de vencimento
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date dataVencimento;
        try {
            dataVencimento = sdf.parse(divida.getVencimento());
        } catch (ParseException e) {
            return;
        }
        if (dataVencimento == null) return;

        // Data do alarme = vencimento - 3 dias, às 9h
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dataVencimento);
        calendar.add(Calendar.DAY_OF_MONTH, -DIAS_ANTES);
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Se a data já passou, não agenda
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) return;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        PendingIntent pendingIntent = criarPendingIntent(context, divida);
        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        } catch (SecurityException e) {
            // Sem permissão de alarme exato, usa alarme comum
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    // ============ CANCELAR ============

    public static void cancelar(Context context, Divida divida) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        PendingIntent pendingIntent = criarPendingIntent(context, divida);
        alarmManager.cancel(pendingIntent);
    }

    // ============ INTERNO ============

    private static PendingIntent criarPendingIntent(Context context, Divida divida) {
        Intent intent = new Intent(context, LembreteReceiver.class);
        intent.putExtra("divida_id", divida.getId());
        intent.putExtra("titulo", divida.getTitulo());
        intent.putExtra("valor", divida.getValorTotal());
        intent.putExtra("vencimento", divida.getVencimento());

        return PendingIntent.getBroadcast(
                context,
                divida.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}