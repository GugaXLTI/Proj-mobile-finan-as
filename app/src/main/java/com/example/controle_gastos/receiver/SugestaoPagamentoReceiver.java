package com.example.controle_gastos.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.AlarmeHelper;

public class SugestaoPagamentoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int dividaId = intent.getIntExtra("EXTRA_DIVIDA_ID", -1);
        int notificacaoId = intent.getIntExtra("EXTRA_NOTIFICACAO_ID", -1);
        double valorRealPago = intent.getDoubleExtra("EXTRA_VALOR_PAGO", 0.0);

        if (dividaId != -1) {
            AppDatabase db = AppDatabase.getInstance(context);
            Divida divida = db.dividaDao().buscarPorId(dividaId);

            if (divida != null && !divida.isPago()) {
                // ⭐ Atualiza o valor da dívida para o valor que realmente saiu da conta
                if (valorRealPago > 0) {
                    divida.valorTotal = valorRealPago;
                    divida.valorParcela = valorRealPago;
                }

                divida.setPago(true);
                divida.valorPago = divida.getValorTotal();
                db.dividaDao().atualizar(divida);

                AlarmeHelper.cancelar(context, divida);

                // Avisa a InicioActivity para atualizar o saldo na hora
                Intent broadcast = new Intent("NOVA_TRANSACAO_RECEBIDA");
                LocalBroadcastManager.getInstance(context).sendBroadcast(broadcast);
            }
        }

        // Remove a notificação da tela
        if (notificacaoId != -1) {
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.cancel(notificacaoId);
        }
    }
}