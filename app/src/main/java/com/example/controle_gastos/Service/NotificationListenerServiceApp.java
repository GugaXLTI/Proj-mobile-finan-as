package com.example.controle_gastos.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.utils.BancoHelper;
import com.example.controle_gastos.utils.GenericBankParser;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.NubankParser;
import com.example.controle_gastos.utils.SessionManager;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NotificationListenerServiceApp extends NotificationListenerService {

    private static final String TAG = "NotifListener";
    private static final String CHANNEL_ID_FEEDBACK = "feedback_transacao";
    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    // ⭐ NOVO: Cache para evitar notificações duplicadas
    private static final long TEMPO_DEBOUNCE_MS = 5000; // 5 segundos
    private static final Map<String, Long> cacheNotificacoes = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
        criarCanalFeedback();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String packageName = sbn.getPackageName();
        SessionManager session = new SessionManager(this);
        int userId = session.getUserId();

        if (userId == -1) return;

        AppDatabase db = AppDatabase.getInstance(this);
        List<Cartao> cartoesDoUsuario = db.cartaoDao().listarPorUsuario(userId);

        if (cartoesDoUsuario.isEmpty()) return;

        String bancoDetectado = null;
        for (Cartao c : cartoesDoUsuario) {
            String pacoteDoBanco = BancoHelper.getPackageName(c.instituicao);
            if (pacoteDoBanco != null && pacoteDoBanco.equals(packageName)) {
                bancoDetectado = c.instituicao;
                break;
            }
        }

        if (bancoDetectado == null) return;

        Bundle extras = sbn.getNotification().extras;
        if (extras == null) return;

        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);

        if (text == null) return;

        String textoCompleto = (title != null ? title : "") + " " + text;

        // ⭐ PROTEÇÃO ANTI-DUPLICAÇÃO AQUI
        String hashNotificacao = packageName + "_" + textoCompleto;
        long agora = System.currentTimeMillis();

        // Limpa o cache antigo (para não ficar na memória pra sempre)
        Iterator<Map.Entry<String, Long>> iterator = cacheNotificacoes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (agora - entry.getValue() > TEMPO_DEBOUNCE_MS) {
                iterator.remove();
            }
        }

        // Se a notificação está no cache e foi vista há menos de 5 segundos, ignora!
        if (cacheNotificacoes.containsKey(hashNotificacao)) {
            Log.d(TAG, "Notificação duplicada ignorada: " + textoCompleto);
            return; // Sai do método e não salva no banco
        }

        // Se não está duplicada, coloca no cache e continua o processamento
        cacheNotificacoes.put(hashNotificacao, agora);

        Log.d(TAG, "Notificação de " + bancoDetectado + " capturada: " + textoCompleto);

        // Escolhe qual Parser usar baseado no banco
        Transacao novaTransacao;
        if (bancoDetectado.toLowerCase().contains("nubank")) {
            novaTransacao = NubankParser.processarTexto(textoCompleto, userId);
        } else {
            novaTransacao = GenericBankParser.processarTexto(textoCompleto, userId, bancoDetectado);
        }

        if (novaTransacao != null) {
            long id = db.transacaoDao().inserir(novaTransacao);
            Log.d(TAG, "Transação salva no banco com sucesso! ID: " + id);

            if (id > 0) {
                // GATILHO DO ESPIÃO INTELIGENTE (Sugestão de Pagamento)
                if ("SAIDA".equals(novaTransacao.tipo) || "SAÍDA".equals(novaTransacao.tipo)) {
                    double valorLido = novaTransacao.valor;

                    Divida dividaParaSugerir = db.dividaDao().buscarDividaAbertaPorValor(userId, valorLido);

                    if (dividaParaSugerir == null) {
                        dividaParaSugerir = db.dividaDao().buscarDividaAproximadaPorValor(userId, valorLido);
                    }

                    if (dividaParaSugerir != null) {
                        NotificationHelper.mostrarNotificacaoSugestao(getApplicationContext(), dividaParaSugerir, valorLido);
                    }
                }

                notificarTransacao(novaTransacao, (int) id);

                Intent intent = new Intent("NOVA_TRANSACAO_RECEBIDA");
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    private void notificarTransacao(Transacao t, int notificationId) {
        boolean isEntrada = "ENTRADA".equals(t.tipo);
        String acao = isEntrada ? "adicionado ao" : "removido do";
        String sinal = isEntrada ? "+" : "-";

        String valorFormatado = NumberFormat.getCurrencyInstance(LOCALE_BR).format(t.valor);

        String titulo = isEntrada ? "Entrada detectada! 🟢" : "Saída detectada! 🔴";
        String texto = String.format("%s%s %s saldo.", sinal, valorFormatado, acao);

        if (t.descricao != null && !t.descricao.equals("Compra / Gasto Cartão") && !t.descricao.equals("Pix / Depósito Recebido")) {
            texto += "\n(" + t.descricao + ")";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_FEEDBACK)
                .setSmallIcon(R.drawable.ic_nav_home)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(texto))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            Log.e(TAG, "Sem permissão para notificar: " + e.getMessage());
        }
    }

    private void criarCanalFeedback() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Feedback de Transações";
            String description = "Notificações quando uma transação é capturada";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID_FEEDBACK, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}