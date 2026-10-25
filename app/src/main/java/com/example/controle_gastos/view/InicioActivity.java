package com.example.controle_gastos.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Usuario;
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

    // Cabeçalho / saldo
    private TextView tvAvatarInicio, tvNomeInicio, tvDataAtualInicio;
    private TextView tvSaldoDisponivelInicio, tvSalarioTotalInicio, tvGastosMesInicio;
    private TextView tvTotalDividasInicio, tvTotalPagoInicio;
    private ProgressBar progressOrcamento;
    private ImageView btnToggleSaldo;
    private boolean isSaldoOculto = false;

    // Card "Atenção este mês"
    private TextView tvAlertaMes, tvBadgeContas, tvTotalAPagarMes, tvCompromete;
    private LinearLayout llContasMes, llTotalAPagar;
    private ProgressBar progressAtencao;
    private TextView btnVerTodosVencimentos;

    // Navegação (aba ativa = LinearLayout, demais = ImageView)
    private LinearLayout tabInicio;
    private ImageView tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;

    private List<Divida> dividas = new ArrayList<>();
    private final List<Divida> contasDoMes = new ArrayList<>();
    private double totalVencendoMes = 0;
    private double salarioAtual = 0;
    private double saldoAtual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        NotificationHelper.criarCanal(this);
        solicitarPermissaoNotificacao();

        tvAvatarInicio = findViewById(R.id.tvAvatarInicio);
        tvNomeInicio = findViewById(R.id.tvNomeInicio);
        tvDataAtualInicio = findViewById(R.id.tvDataAtualInicio);

        tvSaldoDisponivelInicio = findViewById(R.id.tvSaldoDisponivelInicio);
        tvSalarioTotalInicio = findViewById(R.id.tvSalarioTotalInicio);
        tvGastosMesInicio = findViewById(R.id.tvGastosMesInicio);
        progressOrcamento = findViewById(R.id.progressOrcamento);
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
        btnToggleSaldo.setOnClickListener(v -> {
            isSaldoOculto = !isSaldoOculto;
            atualizarDashboardFinanceiro();
        });

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        atualizarNomeEAvatar();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM 'de' yyyy", LOCALE_BR);
        String mesAno = sdf.format(new Date());
        tvDataAtualInicio.setText(mesAno.substring(0, 1).toUpperCase(LOCALE_BR) + mesAno.substring(1));

        carregarDados();
        atualizarDashboardFinanceiro();

        btnVerTodosVencimentos.setOnClickListener(v ->
                startActivity(new Intent(this, DividasActivity.class)));

        tabInicio.setOnClickListener(v ->
                Toast.makeText(this, "Você já está no Início", Toast.LENGTH_SHORT).show());
        tabLancar.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroDividaActivity.class)));
        tabDividas.setOnClickListener(v ->
                startActivity(new Intent(this, DividasActivity.class)));
        tabRelatorios.setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        tabConfig.setOnClickListener(v ->
                startActivity(new Intent(this, ConfiguracoesActivity.class)));
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
        carregarDados();
        atualizarDashboardFinanceiro();
    }

    private void atualizarNomeEAvatar() {
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase(LOCALE_BR));
        }
    }

    // ------------------------------------------------------------------
    // SALDO
    // ------------------------------------------------------------------
    private void atualizarDashboardFinanceiro() {
        Usuario user = db.usuarioDao().buscarPorId(session.getUserId());
        if (user != null) {
            double salario = user.getSalarioMensal();

            Calendar cal = Calendar.getInstance();
            String mesAnoAtual = String.format(LOCALE_BR, "%02d/%04d",
                    cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));

            double gastosMes = db.dividaDao().somarGastosDoMes(session.getUserId(), mesAnoAtual);
            double saldoDisponivel = salario - gastosMes;

            salarioAtual = salario;
            saldoAtual = saldoDisponivel;

            if (isSaldoOculto) {
                btnToggleSaldo.setImageResource(R.drawable.ic_eye_closed);
                tvSalarioTotalInicio.setText("R$ ••••");
                tvGastosMesInicio.setText("R$ ••••");
                tvSaldoDisponivelInicio.setText("R$ ••••");
            } else {
                btnToggleSaldo.setImageResource(R.drawable.ic_eye_open);
                tvSalarioTotalInicio.setText(moeda(salario));
                tvGastosMesInicio.setText(moeda(gastosMes));
                tvSaldoDisponivelInicio.setText(moeda(saldoDisponivel));

                if (salario <= 0) {
                    tvSaldoDisponivelInicio.setTextColor(Color.parseColor("#64748B"));
                } else if (saldoDisponivel < 0) {
                    tvSaldoDisponivelInicio.setTextColor(Color.parseColor("#F87171"));
                } else {
                    tvSaldoDisponivelInicio.setTextColor(Color.parseColor("#34D399"));
                }
            }

            if (salario > 0) {
                int pct = (int) ((gastosMes / salario) * 100);
                progressOrcamento.setProgress(Math.min(pct, 100));
            } else {
                progressOrcamento.setProgress(0);
            }
        }
        renderizarCardAtencao();
    }

    // ------------------------------------------------------------------
    // DÍVIDAS / TOTAIS
    // ------------------------------------------------------------------
    private void carregarDados() {
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

        tvTotalDividasInicio.setText(moeda(totalAPagar));
        tvTotalPagoInicio.setText(moeda(totalPago));
    }

    // ------------------------------------------------------------------
    // CARD "ATENÇÃO ESTE MÊS"
    // ------------------------------------------------------------------
    private void renderizarCardAtencao() {
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

            String titulo = d.getTitulo();
            int total = extrairTotalParcelas(d.getParcela());
            if (total > 1) {
                titulo += " · parcela " + extrairNumeroParcela(d.getParcela()) + " de " + total;
            }
            tvTitulo.setText(titulo);
            tvValor.setText(moeda(d.getValorRestante()));

            try {
                Date data = sdfData.parse(d.getVencimento());
                if (data != null) {
                    tvMes.setText(sdfMes.format(data).replace(".", "").toUpperCase(LOCALE_BR));
                    tvDia.setText(sdfDia.format(data));

                    long dias = diasAte(data);
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
            } catch (Exception e) {
                tvMes.setText("--");
                tvDia.setText("--");
                tvVence.setText(d.getVencimento());
                tvVence.setTextColor(Color.parseColor("#94A3B8"));
            }

            if (i == n - 1) divisor.setVisibility(View.GONE);
            llContasMes.addView(item);
        }

        tvTotalAPagarMes.setText(moeda(totalVencendoMes));

        if (salarioAtual > 0) {
            int pct = (int) Math.round((totalVencendoMes / salarioAtual) * 100);
            progressAtencao.setProgress(Math.min(pct, 100));

            String texto = "Compromete " + pct + "% do salário";
            if (!isSaldoOculto) {
                double sobra = saldoAtual - totalVencendoMes;
                texto += sobra >= 0
                        ? " · Sobra " + moeda(sobra)
                        : " · Faltam " + moeda(-sobra);
            }
            tvCompromete.setText(texto);
        } else {
            progressAtencao.setProgress(0);
            tvCompromete.setText("Total das contas que vencem este mês");
        }
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private String moeda(double valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
    }

    private long diasAte(Date data) {
        Calendar hoje = Calendar.getInstance();
        zerarHora(hoje);
        Calendar alvo = Calendar.getInstance();
        alvo.setTime(data);
        zerarHora(alvo);
        return Math.round((alvo.getTimeInMillis() - hoje.getTimeInMillis()) / 86400000.0);
    }

    private void zerarHora(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
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