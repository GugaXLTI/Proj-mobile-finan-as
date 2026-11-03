package com.example.controle_gastos.utils;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FinancialReminderWorker extends Worker {

    public FinancialReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        SessionManager session = new SessionManager(context);

        int userId = session.getUserId();
        if (userId == -1) return Result.success();

        if (!AlarmeHelper.isLembretesAtivos(context)) {
            return Result.success();
        }

        Calendar cal = Calendar.getInstance();
        int hora = cal.get(Calendar.HOUR_OF_DAY);
        if (hora >= 22 || hora < 8) {
            return Result.success();
        }

        Locale localeBR = new Locale("pt", "BR");
        NumberFormat nf = NumberFormat.getCurrencyInstance(localeBR);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", localeBR);

        // ⭐ 1. NOVA PRIORIDADE: Checar dívidas atrasadas não pagas
        List<Divida> dividasAbertas = db.dividaDao().listarAbertasParaChecarAtraso(userId);
        Divida dividaAtrasada = null;

        Date hoje = new Date();
        for (Divida d : dividasAbertas) {
            try {
                Date vencimento = sdf.parse(d.getVencimento());
                if (vencimento != null && vencimento.before(hoje)) {
                    dividaAtrasada = d;
                    break; // Pega a primeira que encontrar para não lotar de notificações
                }
            } catch (Exception e) {
                // Ignora erros de data
            }
        }

        if (dividaAtrasada != null) {
            // Manda a notificação de "Você já pagou?"
            NotificationHelper.mostrarNotificacaoAtraso(context, dividaAtrasada.getTitulo(), dividaAtrasada.getVencimento());
            return Result.success(); // Se achou atrasada, encerra o worker para não mandar insight no mesmo dia
        }

        // ⭐ 2. Se não tem atrasadas, segue a lógica normal de Insights
        String mesAnoAtual = String.format(localeBR, "%02d/%04d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
        List<Divida> todasDividas = db.dividaDao().listarPorUsuario(userId);

        Map<String, Double> gastosPorCategoria = new HashMap<>();
        double totalGasto = 0;

        for (Divida d : todasDividas) {
            if (d.getVencimento() == null || d.getVencimento().isEmpty()) continue;

            String[] partes = d.getVencimento().split("/");
            if (partes.length == 3) {
                String mesAnoDivida = partes[1] + "/" + partes[2];
                if (mesAnoDivida.equals(mesAnoAtual)) {
                    String cat = d.getCategoria() != null ? d.getCategoria() : "Outros";
                    gastosPorCategoria.put(cat, gastosPorCategoria.getOrDefault(cat, 0.0) + d.getValorTotal());
                    totalGasto += d.getValorTotal();
                }
            }
        }

        String categoriaTop = null;
        double valorTop = 0;

        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            if (entry.getValue() > valorTop) {
                valorTop = entry.getValue();
                categoriaTop = entry.getKey();
            }
        }

        if (categoriaTop != null && totalGasto > 0) {
            String msgInsight = "Você sabia que já gastou " + nf.format(valorTop) + " em " + categoriaTop + " este mês?";
            NotificationHelper.mostrarNotificacaoInsight(context, msgInsight);
        } else {
            NotificationHelper.mostrarNotificacaoLembrete(context, "Não esqueça de cadastrar suas novas despesas no ORG hoje!");
        }

        return Result.success();
    }
}