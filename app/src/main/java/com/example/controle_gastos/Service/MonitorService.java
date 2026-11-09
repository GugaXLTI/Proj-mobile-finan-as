package com.example.controle_gastos.Service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.controle_gastos.utils.NotificationHelper;

public class MonitorService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Cria a notificação fixa
        android.app.Notification notification = NotificationHelper.getForegroundNotification(this);

        // Inicia o serviço em primeiro plano (Imortal)
        startForeground(NotificationHelper.FOREGROUND_NOTIFICATION_ID, notification);

        return START_STICKY; // Se o Android matar, ele reinicia sozinho
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}