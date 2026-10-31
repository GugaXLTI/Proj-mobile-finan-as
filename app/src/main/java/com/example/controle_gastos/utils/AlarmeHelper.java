package com.example.controle_gastos.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.receiver.LembreteReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmeHelper {

    private static final String TAG = "AlarmeHelper";
    private static final String PREF_NAME = "prefs_lembretes";
    private static final String KEY_ATIVO = "lembretes_ativo";
    private static final int DIAS_ANTES = 3;

    public static boolean isLembretesAtivos(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ATIVO, true);
    }

    public static void setLembretesAtivos(Context context, boolean ativo) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ATIVO, ativo).apply();
    }

    public static void agendar(Context context, Divida divida) {
        if (!isLembretesAtivos(context)) return;
        if (divida.isPago()) return;

        // ⭐ PROTEÇÃO: Try/Catch geral para o app nunca crashar por causa do alarme
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date dataVencimento = sdf.parse(divida.getVencimento());
            if (dataVencimento == null) return;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataVencimento);
            calendar.add(Calendar.DAY_OF_MONTH, -DIAS_ANTES);
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) return;

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            PendingIntent pendingIntent = criarPendingIntent(context, divida);

            try {
                // Tenta agendar alarme exato (Android 12+)
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } catch (SecurityException e) {
                // Sem permissão de alarme exato, usa alarme comum
                Log.w(TAG, "Sem permissão de alarme exato, usando set()");
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (Exception e) {
            // Se der QUALQUER outro erro, apenas loga e segue a vida.
            Log.e(TAG, "Erro ao agendar alarme: " + e.getMessage());
        }
    }

    public static void cancelar(Context context, Divida divida) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;
            PendingIntent pendingIntent = criarPendingIntent(context, divida);
            alarmManager.cancel(pendingIntent);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao cancelar alarme: " + e.getMessage());
        }
    }

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