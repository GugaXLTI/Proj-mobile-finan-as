package com.example.controle_gastos.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Conta;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Renda;
import com.example.controle_gastos.utils.BottomNavHelper;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InicioActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICACAO = 100;
    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final String TAG = "InicioActivity";

    private TextView tvAvatarInicio, tvNomeInicio, tvDataAtualInicio;
    private TextView tvSaldoDisponivelInicio, tvSalarioTotalInicio, tvGastosMesInicio;
    private TextView tvTituloSaldo, tvLabelRecebido;
    private TextView tvTotalDividasInicio, tvTotalPagoInicio;
    private TextView tvProximoRecebimentoData, tvProximoRecebimentoValor, tvProximoCaption, tvProximoTag;
    private ImageView ivProximoIcone;
    private ProgressBar progressOrcamento;
    private ImageView btnToggleSaldo, btnHistorico;
    private boolean isSaldoOculto = false;

    private TextView tvAlertaMes, tvBadgeContas, tvTotalAPagarMes, tvCompromete;
    private LinearLayout llContasMes, llTotalAPagar;
    private ProgressBar progressAtencao;
    private TextView btnVerTodosVencimentos;

    private AppDatabase db;
    private SessionManager session;

    private List<Divida> dividas = new ArrayList<>();
    private final List<Divida> contasDoMes = new ArrayList<>();
    private double totalVencendoMes = 0;
    private double rendaEsperadaMes = 0;
    private double saldoAtual = 0;

    // ⭐ NOVO: Recebe o aviso do Serviço de Notificação
    private final BroadcastReceiver transacaoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Sinal de nova transação recebido! Atualizando tela...");
            try {
                carregarDados();
                atualizarDashboardFinanceiro();
            } catch (Exception e) {
                Log.e(TAG, "Erro ao atualizar via broadcast: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        try {
            NavScrollHelper.attach(this);
            db = AppDatabase.getInstance(this);
            session = new SessionManager(this);
            BottomNavHelper.configurar(this, BottomNavHelper.INICIO);
        } catch (Exception e) {
            Log.e(TAG, "Erro na inicialização de helpers: " + e.getMessage());
        }

        NotificationHelper.criarCanal(this);
        solicitarPermissaoNotificacao();

        tvAvatarInicio = findViewById(R.id.tvAvatarInicio);
        tvNomeInicio = findViewById(R.id.tvNomeInicio);
        tvDataAtualInicio = findViewById(R.id.tvDataAtualInicio);
        tvTituloSaldo = findViewById(R.id.tvTituloSaldo);
        tvLabelRecebido = findViewById(R.id.tvLabelRecebido);
        tvSaldoDisponivelInicio = findViewById(R.id.tvSaldoDisponivelInicio);
        tvSalarioTotalInicio = findViewById(R.id.tvSalarioTotalInicio);
        tvGastosMesInicio = findViewById(R.id.tvGastosMesInicio);
        progressOrcamento = findViewById(R.id.progressOrcamento);
        ivProximoIcone = findViewById(R.id.ivProximoIcone);
        tvProximoCaption = findViewById(R.id.tvProximoCaption);
        tvProximoRecebimentoData = findViewById(R.id.tvProximoRecebimentoData);
        tvProximoRecebimentoValor = findViewById(R.id.tvProximoRecebimentoValor);
        tvProximoTag = findViewById(R.id.tvProximoTag);
        tvTotalDividasInicio = findViewById(R.id.tvTotalDividasInicio);
        tvTotalPagoInicio = findViewById(R.id.tvTotalPagoInicio);
        tvAlertaMes = findViewById(R.id.tvAlertaMes);
        tvBadgeContas = findViewById(R.id.tvBadgeContas);
        llContasMes = findViewById(R.id.llContasMes);
        llTotalAPagar = findViewById(R.id.llTotalAPagar);
        tvTotalAPagarMes = findViewById(R.id.tvTotalAPagarMes);
        tvCompromete = findViewById(R.id.tvCompromete);
        progressAtencao = findViewById(R.id.progressAtencao);
        btnVerTodosVencimentos = findViewById(R.id.btnVerTodosVencimentos);

        btnToggleSaldo = findViewById(R.id.btnToggleSaldo);
        btnHistorico = findViewById(R.id.btnHistorico);

        if (btnToggleSaldo != null) {
            btnToggleSaldo.setOnClickListener(v -> {
                isSaldoOculto = !isSaldoOculto;
                atualizarDashboardFinanceiro();
            });
        }

        if (btnHistorico != null) {
            btnHistorico.setOnClickListener(v -> {
                startActivity(new Intent(this, HistoricoActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }

        atualizarNomeEAvatar();

        if (tvDataAtualInicio != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM 'de' yyyy", LOCALE_BR);
            String mesAno = sdf.format(new Date());
            tvDataAtualInicio.setText(mesAno.substring(0, 1).toUpperCase(LOCALE_BR) + mesAno.substring(1));
        }

        try {
            carregarDados();
            atualizarDashboardFinanceiro();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar dados iniciais: " + e.getMessage());
        }

        if (btnVerTodosVencimentos != null) {
            btnVerTodosVencimentos.setOnClickListener(v -> {
                startActivity(new Intent(this, DividasActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            });
        }
    }

    // ⭐ NOVO: Registra o receptor quando a tela aparece
    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                transacaoReceiver, new IntentFilter("NOVA_TRANSACAO_RECEBIDA"));
    }

    // ⭐ NOVO: Desregistra quando a tela some para não vazar memória
    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(transacaoReceiver);
    }

    private void solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICACAO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Lembretes ativados!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sem permissão, os lembretes não vão funcionar.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarNomeEAvatar();
        try {
            carregarDados();
            atualizarDashboardFinanceiro();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao retomar a tela: " + e.getMessage());
        }
    }

    private void atualizarNomeEAvatar() {
        if (session == null || tvNomeInicio == null || tvAvatarInicio == null) return;
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase(LOCALE_BR));
        }
    }

    private void atualizarDashboardFinanceiro() {
        if (db == null || session == null) return;

        Calendar cal = Calendar.getInstance();
        String mesAnoAtual = String.format(LOCALE_BR, "%02d/%04d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));

        Conta conta = db.contaDao().buscarPorUsuario(session.getUserId());
        double saldoInicial = (conta != null) ? conta.saldoInicial : 0.0;

        double entradas = db.transacaoDao().somarEntradas(session.getUserId());
        double saidasAuto = db.transacaoDao().somarSaidas(session.getUserId());
        double gastosManuais = db.dividaDao().somarGastosDoMes(session.getUserId(), mesAnoAtual);

        double saldoEmConta = saldoInicial + entradas - saidasAuto - gastosManuais;
        saldoAtual = saldoEmConta;

        if (tvTituloSaldo != null) tvTituloSaldo.setText("SALDO EM CONTA");
        if (tvLabelRecebido != null) tvLabelRecebido.setText("Entradas automáticas");

        if (isSaldoOculto) {
            if (btnToggleSaldo != null) btnToggleSaldo.setImageResource(R.drawable.ic_eye_closed);
            if (tvSalarioTotalInicio != null) tvSalarioTotalInicio.setText("R$ ••••");
            if (tvGastosMesInicio != null) tvGastosMesInicio.setText("R$ ••••");
            if (tvSaldoDisponivelInicio != null) tvSaldoDisponivelInicio.setText("R$ ••••");
        } else {
            if (btnToggleSaldo != null) btnToggleSaldo.setImageResource(R.drawable.ic_eye_open);
            if (tvSalarioTotalInicio != null) tvSalarioTotalInicio.setText(moeda(entradas));
            if (tvGastosMesInicio != null) tvGastosMesInicio.setText(moeda(gastosManuais + saidasAuto));
            if (tvSaldoDisponivelInicio != null) {
                tvSaldoDisponivelInicio.setText(moeda(saldoEmConta));
                tvSaldoDisponivelInicio.setTextColor(Color.parseColor(saldoEmConta < 0 ? "#F87171" : "#34D399"));
            }
        }

        List<Renda> rendas = db.rendaDao().listarPorUsuario(session.getUserId());
        Renda proximaFixa = null;
        long menorDias = Long.MAX_VALUE;
        rendaEsperadaMes = 0;

        for (Renda r : rendas) {
            rendaEsperadaMes += r.valor;
            long dias = diasAteProximoRecebimento(r.diaRecebimento);
            if (dias < menorDias) {
                menorDias = dias;
                proximaFixa = r;
            }
        }

        atualizarCardProximoRecebimento(proximaFixa, menorDias);

        if (progressOrcamento != null) {
            if (rendaEsperadaMes > 0) {
                int pct = (int) ((gastosManuais / rendaEsperadaMes) * 100);
                progressOrcamento.setProgress(Math.min(pct, 100));
            } else {
                progressOrcamento.setProgress(0);
            }
        }

        renderizarCardAtencao();
    }

    private void atualizarCardProximoRecebimento(Renda proximaFixa, long dias) {
        if (tvProximoCaption == null || ivProximoIcone == null || tvProximoRecebimentoValor == null || tvProximoRecebimentoData == null || tvProximoTag == null) return;

        if (proximaFixa != null) {
            tvProximoCaption.setText("PREVISÃO DE ENTRADA");
            ivProximoIcone.setImageResource(R.drawable.ic_conf_money);
            tvProximoRecebimentoValor.setText(isSaldoOculto ? "R$ ••••" : moeda(proximaFixa.valor));
            tvProximoRecebimentoData.setText(proximaFixa.descricao + " · dia " + proximaFixa.diaRecebimento);
            tvProximoTag.setText(dias == 1 ? "amanhã" : "em " + dias + " dias");
            tvProximoTag.setVisibility(View.VISIBLE);
        } else {
            tvProximoCaption.setText("PREVISÃO DE ENTRADA");
            ivProximoIcone.setImageResource(R.drawable.ic_conf_money);
            tvProximoRecebimentoValor.setText(moeda(0));
            tvProximoRecebimentoData.setText("Nenhuma renda cadastrada");
            tvProximoTag.setVisibility(View.GONE);
        }
    }

    private long diasAteProximoRecebimento(int diaRecebimento) {
        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);

        int dia = Math.max(diaRecebimento, 1);
        int diaHoje = hoje.get(Calendar.DAY_OF_MONTH);
        int ultimoDiaMesAtual = hoje.getActualMaximum(Calendar.DAY_OF_MONTH);

        Calendar alvo = (Calendar) hoje.clone();
        if (Math.min(dia, ultimoDiaMesAtual) > diaHoje) {
            alvo.set(Calendar.DAY_OF_MONTH, Math.min(dia, ultimoDiaMesAtual));
        } else {
            alvo.set(Calendar.DAY_OF_MONTH, 1);
            alvo.add(Calendar.MONTH, 1);
            alvo.set(Calendar.DAY_OF_MONTH, Math.min(dia, alvo.getActualMaximum(Calendar.DAY_OF_MONTH)));
        }
        return Math.round((alvo.getTimeInMillis() - hoje.getTimeInMillis()) / 86400000.0);
    }

    private void carregarDados() {
        if (db == null || session == null) return;
        dividas = db.dividaDao().listarPorUsuario(session.getUserId());
        atualizarTotais();
    }

    private void atualizarTotais() {
        double totalAPagar = 0;
        double totalPago = 0;
        totalVencendoMes = 0;
        contasDoMes.clear();

        Calendar c = Calendar.getInstance();
        int mesAtual = c.get(Calendar.MONTH) + 1;
        int anoAtual = c.get(Calendar.YEAR);

        for (Divida d : dividas) {
            if (d.isPago()) {
                totalPago += d.getValorTotal();
            } else {
                totalAPagar += d.getValorRestante();
                if (venceNoMes(d.getVencimento(), mesAtual, anoAtual)) {
                    totalVencendoMes += d.getValorRestante();
                    contasDoMes.add(d);
                }
            }
        }

        contasDoMes.sort((a, b) -> compararDatas(a.getVencimento(), b.getVencimento()));

        if (tvTotalDividasInicio != null) tvTotalDividasInicio.setText(moeda(totalAPagar));
        if (tvTotalPagoInicio != null) tvTotalPagoInicio.setText(moeda(totalPago));
    }

    private void renderizarCardAtencao() {
        if (llContasMes == null || tvAlertaMes == null || llTotalAPagar == null || tvBadgeContas == null || tvTotalAPagarMes == null || progressAtencao == null || tvCompromete == null) return;

        llContasMes.removeAllViews();
        int n = contasDoMes.size();

        if (n == 0) {
            tvAlertaMes.setVisibility(View.VISIBLE);
            llTotalAPagar.setVisibility(View.GONE);
            tvBadgeContas.setVisibility(View.GONE);
            return;
        }

        tvAlertaMes.setVisibility(View.GONE);
        llTotalAPagar.setVisibility(View.VISIBLE);
        tvBadgeContas.setVisibility(View.VISIBLE);
        tvBadgeContas.setText(n + (n == 1 ? " conta" : " contas"));

        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR);
        SimpleDateFormat sdfMes = new SimpleDateFormat("MMM", LOCALE_BR);
        SimpleDateFormat sdfDia = new SimpleDateFormat("dd", LOCALE_BR);

        for (int i = 0; i < n; i++) {
            Divida d = contasDoMes.get(i);
            View item = inflater.inflate(R.layout.item_conta_atencao, llContasMes, false);

            TextView tvMes = item.findViewById(R.id.tvMesConta);
            TextView tvDia = item.findViewById(R.id.tvDiaConta);
            TextView tvTitulo = item.findViewById(R.id.tvTituloConta);
            TextView tvVence = item.findViewById(R.id.tvVenceConta);
            TextView tvValor = item.findViewById(R.id.tvValorConta);
            View divisor = item.findViewById(R.id.divisorConta);

            if (tvTitulo != null) {
                String titulo = d.getTitulo();
                int total = extrairTotalParcelas(d.getParcela());
                if (total > 1) titulo += " · parcela " + extrairNumeroParcela(d.getParcela()) + " de " + total;
                tvTitulo.setText(titulo);
            }
            if (tvValor != null) tvValor.setText(moeda(d.getValorRestante()));

            try {
                Date data = sdfData.parse(d.getVencimento());
                if (data != null) {
                    if (tvMes != null) tvMes.setText(sdfMes.format(data).replace(".", "").toUpperCase(LOCALE_BR));
                    if (tvDia != null) tvDia.setText(sdfDia.format(data));

                    long dias = diasAte(data);
                    if (tvVence != null) {
                        if (dias < 0) {
                            tvVence.setText(dias == -1 ? "Venceu ontem" : "Venceu há " + (-dias) + " dias");
                            tvVence.setTextColor(Color.parseColor("#F87171"));
                        } else if (dias == 0) {
                            tvVence.setText("Vence hoje");
                            tvVence.setTextColor(Color.parseColor("#F59E0B"));
                        } else if (dias == 1) {
                            tvVence.setText("Vence amanhã");
                            tvVence.setTextColor(Color.parseColor("#F59E0B"));
                        } else {
                            tvVence.setText("Vence em " + dias + " dias");
                            tvVence.setTextColor(Color.parseColor(dias <= 5 ? "#F59E0B" : "#94A3B8"));
                        }
                    }
                }
            } catch (Exception e) {
                if (tvMes != null) tvMes.setText("--");
                if (tvDia != null) tvDia.setText("--");
                if (tvVence != null) {
                    tvVence.setText(d.getVencimento());
                    tvVence.setTextColor(Color.parseColor("#94A3B8"));
                }
            }

            if (i == n - 1 && divisor != null) divisor.setVisibility(View.GONE);
            llContasMes.addView(item);
        }

        tvTotalAPagarMes.setText(moeda(totalVencendoMes));

        if (rendaEsperadaMes > 0) {
            int pct = (int) Math.round((totalVencendoMes / rendaEsperadaMes) * 100);
            progressAtencao.setProgress(Math.min(pct, 100));

            String texto = "Compromete " + pct + "% da renda mensal";
            if (!isSaldoOculto) {
                double sobra = saldoAtual - totalVencendoMes;
                texto += sobra >= 0 ? " · Sobra " + moeda(sobra) : " · Faltam " + moeda(-sobra);
            }
            tvCompromete.setText(texto);
        } else {
            progressAtencao.setProgress(0);
            tvCompromete.setText("Total das contas que vencem este mês");
        }
    }

    private String moeda(double valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
    }

    private long diasAte(Date data) {
        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);

        Calendar alvo = Calendar.getInstance();
        alvo.setTime(data);
        alvo.set(Calendar.HOUR_OF_DAY, 0);
        alvo.set(Calendar.MINUTE, 0);
        alvo.set(Calendar.SECOND, 0);
        alvo.set(Calendar.MILLISECOND, 0);

        return Math.round((alvo.getTimeInMillis() - hoje.getTimeInMillis()) / 86400000.0);
    }

    private boolean venceNoMes(String vencimento, int mes, int ano) {
        if (vencimento == null || vencimento.isEmpty()) return false;
        try {
            String[] partes = vencimento.split("/");
            if (partes.length != 3) return false;
            return Integer.parseInt(partes[1]) == mes && Integer.parseInt(partes[2]) == ano;
        } catch (Exception e) {
            return false;
        }
    }

    private int compararDatas(String d1, String d2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date data1 = sdf.parse(d1);
            Date data2 = sdf.parse(d2);
            if (data1 == null || data2 == null) return 0;
            return data1.compareTo(data2);
        } catch (Exception e) {
            return 0;
        }
    }

    private int extrairTotalParcelas(String parcela) {
        if (parcela == null) return 1;
        if (parcela.contains("/")) {
            try {
                String[] partes = parcela.split("/");
                String total = partes[1].replaceAll("[^0-9]", "");
                if (!total.isEmpty()) {
                    int n = Integer.parseInt(total);
                    return n > 0 ? n : 1;
                }
            } catch (Exception e) {
                return 1;
            }
        }

        try {
            String numeros = parcela.replaceAll("[^0-9]", "");
            if (numeros.isEmpty()) return 1;
            int n = Integer.parseInt(numeros);
            return n > 0 ? n : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private int extrairNumeroParcela(String parcela) {
        if (parcela == null || !parcela.contains("/")) return 1;
        try {
            String atual = parcela.split("/")[0].replaceAll("[^0-9]", "");
            return atual.isEmpty() ? 1 : Integer.parseInt(atual);
        } catch (Exception e) {
            return 1;
        }
    }
}