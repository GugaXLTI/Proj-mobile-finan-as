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

    private static final long TEMPO_DEBOUNCE_MS = 5000;
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
        Bundle extras = sbn.getNotification().extras;

        // ⭐ LOG DE DEBUG: Mostra TUDO que chega no celular
        if (extras != null) {
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

            Log.d(TAG, "--- NOVA NOTIFICAÇÃO ---");
            Log.d(TAG, "Pacote: " + packageName);
            Log.d(TAG, "Título: " + (title != null ? title : "Nulo"));
            Log.d(TAG, "Texto: " + (text != null ? text : "Nulo"));
            Log.d(TAG, "BigText: " + (bigText != null ? bigText : "Nulo"));
        }

        SessionManager session = new SessionManager(this);
        int userId = session.getUserId();

        if (userId == -1) return;

        AppDatabase db = AppDatabase.getInstance(this);
        List<Cartao> cartoesDoUsuario = db.cartaoDao().listarPorUsuario(userId);

        if (cartoesDoUsuario.isEmpty()) {
            Log.d(TAG, "Nenhum cartão cadastrado pelo usuário. Ignorando.");
            return;
        }

        String bancoDetectado = null;
        for (Cartao c : cartoesDoUsuario) {
            String pacoteDoBanco = BancoHelper.getPackageName(c.instituicao);
            // Usa contains para evitar erro de pacotes com sufixos diferentes
            if (pacoteDoBanco != null && (packageName.equals(pacoteDoBanco) || packageName.contains(pacoteDoBanco))) {
                bancoDetectado = c.instituicao;
                break;
            }
        }

        if (bancoDetectado == null) {
            Log.d(TAG, "Pacote " + packageName + " ignorado (Banco não cadastrado no ORG ou pacote não bate com o BancoHelper).");
            return;
        }

        if (extras == null) return;

        CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
        CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);

        // Se o texto normal for nulo ou muito curto, tenta pegar o texto longo
        if (text == null || text.length() < 10) {
            if (bigText != null) {
                text = bigText;
            }
        }

        if (text == null) return;

        String textoCompleto = (title != null ? title : "") + " " + text;

        String hashNotificacao = packageName + "_" + textoCompleto;
        long agora = System.currentTimeMillis();

        Iterator<Map.Entry<String, Long>> iterator = cacheNotificacoes.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            if (agora - entry.getValue() > TEMPO_DEBOUNCE_MS) {
                iterator.remove();
            }
        }

        if (cacheNotificacoes.containsKey(hashNotificacao)) {
            Log.d(TAG, "Notificação duplicada ignorada: " + textoCompleto);
            return;
        }

        cacheNotificacoes.put(hashNotificacao, agora);

        Log.d(TAG, "⭐ NOTIFICAÇÃO DO BANCO CAPTURADA (" + bancoDetectado + "): " + textoCompleto);

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
        } else {
            Log.d(TAG, "O Parser não conseguiu gerar uma transação com o texto acima.");
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