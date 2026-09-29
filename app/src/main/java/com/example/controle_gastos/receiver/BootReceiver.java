package com.example.controle_gastos.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.example.controle_gastos.dao.DividaDao;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.SessionManager;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Verifica se o evento é o de boot completado ou de app atualizado
        if (intent == null || intent.getAction() == null) return;

        String action = intent.getAction();
        if (!action.equals(Intent.ACTION_BOOT_COMPLETED) &&
                !action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            return;
        }

        // Se os lembretes estiverem desligados, nem perde tempo
        if (!AlarmeHelper.isLembretesAtivos(context)) return;

        SessionManager session = new SessionManager(context);
        int userId = session.getUserId();

        // Se ninguém estiver logado, não reagenda nada
        if (userId == -1) return;

        // Reagendar em thread separada (Room não pode rodar na main thread aqui)
        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            DividaDao dao = db.dividaDao();
            List<Divida> dividasNaoPagas = dao.listarNaoPagasPorUsuario(userId);

            for (Divida d : dividasNaoPagas) {
                AlarmeHelper.agendar(context, d);
            }
        }).start();
    }
}