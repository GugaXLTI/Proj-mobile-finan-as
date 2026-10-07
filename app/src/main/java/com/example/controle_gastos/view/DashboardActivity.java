package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.DividaAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.SessionManager;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private com.github.mikephil.charting.charts.PieChart pieChart;
    private RecyclerView rvLancamentos;
    private LinearLayout containerFiltros;
    private Button btnExportar;
    private TextView tvTituloLista, tvTotalLista;

    // ⭐ Navegação de mês
    private TextView btnMesAnteriorDash, btnMesProximoDash, tvMesAtualDash, tvBadgeAtualDash;

    private List<Divida> todasDividas;
    private List<Divida> dividasFiltradas;
    private DividaAdapter dividaAdapter;

    private float totalParaPercentual = 0f;

    private AppDatabase db;
    private SessionManager session;

    // ⭐ Mês/ano sendo visualizado
    private int mesSelecionado;
    private int anoSelecionado;

    private final int[] CORES_FIGMA = {
            Color.parseColor("#A855F7"),
            Color.parseColor("#10B981"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#EAB308"),
            Color.parseColor("#3B82F6"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#06B6D4")
    };

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        pieChart = findViewById(R.id.pieChart);
        rvLancamentos = findViewById(R.id.rvLancamentos);
        containerFiltros = findViewById(R.id.containerFiltros);
        btnExportar = findViewById(R.id.btnExportar);
        tvTituloLista = findViewById(R.id.tvTituloLista);
        tvTotalLista = findViewById(R.id.tvTotalLista);

        // ⭐ Navegação de mês
        btnMesAnteriorDash = findViewById(R.id.btnMesAnteriorDash);
        btnMesProximoDash = findViewById(R.id.btnMesProximoDash);
        tvMesAtualDash = findViewById(R.id.tvMesAtualDash);
        tvBadgeAtualDash = findViewById(R.id.tvBadgeAtualDash);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvLancamentos.setLayoutManager(new LinearLayoutManager(this));

        // Inicia no mês atual
        Calendar c = Calendar.getInstance();
        mesSelecionado = c.get(Calendar.MONTH) + 1;
        anoSelecionado = c.get(Calendar.YEAR);

        configurarPieChart();
        carregarDados();
        configurarExportar();
        configurarNavegacao();

        // ⭐ Listeners das setas
        btnMesAnteriorDash.setOnClickListener(v -> {
            mesSelecionado--;
            if (mesSelecionado < 1) {
                mesSelecionado = 12;
                anoSelecionado--;
            }
            carregarDados();
        });

        btnMesProximoDash.setOnClickListener(v -> {
            mesSelecionado++;
            if (mesSelecionado > 12) {
                mesSelecionado = 1;
                anoSelecionado++;
            }
            carregarDados();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDados();
    }

    /**
     * ⭐ Carrega as dívidas do mês SELECIONADO (não mais "atual" fixo).
     */
    private void carregarDados() {
        List<Divida> todas = db.dividaDao().listarPorUsuario(session.getUserId());
        todasDividas = new ArrayList<>();

        for (Divida d : todas) {
            if (pertenceAoMes(d, mesSelecionado, anoSelecionado)) {
                todasDividas.add(d);
            }
        }

        dividasFiltradas = new ArrayList<>(todasDividas);
        atualizarTituloMes();
        atualizarDashboard("Todos");
        configurarFiltros();
    }

    /**
     * Atualiza o título do mês e mostra/esconde o badge "Atual".
     */
    private void atualizarTituloMes() {
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        tvMesAtualDash.setText(meses[mesSelecionado - 1] + " " + anoSelecionado);

        Calendar c = Calendar.getInstance();
        int mesAtual = c.get(Calendar.MONTH) + 1;
        int anoAtual = c.get(Calendar.YEAR);

        if (mesSelecionado == mesAtual && anoSelecionado == anoAtual) {
            tvBadgeAtualDash.setVisibility(View.VISIBLE);
        } else {
            tvBadgeAtualDash.setVisibility(View.GONE);
        }
    }

    private boolean pertenceAoMes(Divida d, int mes, int ano) {
        String venc = d.getVencimento();
        if (venc == null || venc.isEmpty()) return false;
        try {
            String[] partes = venc.split("/");
            if (partes.length != 3) return false;
            int m = Integer.parseInt(partes[1]);
            int a = Integer.parseInt(partes[2]);
            return m == mes && a == ano;
        } catch (Exception e) {
            return false;
        }
    }

    private void configurarPieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(12, 12, 12, 12);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#131C2E"));
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setTransparentCircleColor(Color.parseColor("#131C2E"));
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.setDrawEntryLabels(false);
    }

    private void atualizarDashboard(String categoriaFiltro) {
        dividasFiltradas.clear();
        for (Divida d : todasDividas) {
            if (categoriaFiltro.equals("Todos") || d.getCategoria().equals(categoriaFiltro)) {
                dividasFiltradas.add(d);
            }
        }

        Map<String, Double> gastosPorCategoria = new HashMap<>();
        for (Divida d : dividasFiltradas) {
            double valor = gastosPorCategoria.getOrDefault(d.getCategoria(), 0.0);
            gastosPorCategoria.put(d.getCategoria(), valor + d.getValorTotal());
        }

        totalParaPercentual = 0f;
        for (Double v : gastosPorCategoria.values()) {
            totalParaPercentual += v.floatValue();
        }

        final Map<Float, String> labelsPorValor = new HashMap<>();
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            float valor = entry.getValue().floatValue();
            labelsPorValor.put(valor, entry.getKey());
            entries.add(new PieEntry(valor, entry.getKey()));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(100f, "Sem dados"));
            labelsPorValor.put(100f, "Sem dados");
            totalParaPercentual = 100f;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(CORES_FIGMA);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(13f);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);

        dataSet.setValueFormatter(new ValueFormatter() {

            private String montarTexto(float value, String label) {
                if (label == null) label = "";
                float percentual = totalParaPercentual > 0 ? (value / totalParaPercentual) * 100f : 0f;
                return label + "\n" + String.format(LOCALE_BR, "%.0f%%", percentual);
            }

            @Override
            public String getFormattedValue(float value) {
                return montarTexto(value, labelsPorValor.get(value));
            }

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                String label = null;
                if (entry instanceof PieEntry) {
                    PieEntry pe = (PieEntry) entry;
                    if (pe.getLabel() != null) label = pe.getLabel();
                }
                if (label == null) label = labelsPorValor.get(value);
                return montarTexto(value, label);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        double totalMes = 0;
        for (Divida d : dividasFiltradas) {
            totalMes += d.getValorTotal();
        }

        SpannableStringBuilder centerText = new SpannableStringBuilder();
        String labelTotal = "TOTAL\n";
        String valorTotal = "R$ " + String.format(LOCALE_BR, "%.2f", totalMes);
        centerText.append(labelTotal);
        centerText.append(valorTotal);

        centerText.setSpan(new ForegroundColorSpan(Color.parseColor("#94A3B8")),
                0, labelTotal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        centerText.setSpan(new RelativeSizeSpan(0.7f),
                0, labelTotal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        centerText.setSpan(new ForegroundColorSpan(Color.WHITE),
                labelTotal.length(), centerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        centerText.setSpan(new StyleSpan(Typeface.BOLD),
                labelTotal.length(), centerText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        pieChart.setCenterText(centerText);
        pieChart.setCenterTextSize(15f);
        pieChart.invalidate();

        dividaAdapter = new DividaAdapter(dividasFiltradas, this);
        rvLancamentos.setAdapter(dividaAdapter);

        // ⭐ Título com o mês selecionado
        String[] meses = {"JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO",
                "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"};
        String nomeMes = meses[mesSelecionado - 1];

        tvTituloLista.setText(categoriaFiltro.equals("Todos")
                ? "DÍVIDAS DE " + nomeMes
                : "DÍVIDAS DE " + nomeMes + " • " + categoriaFiltro.toUpperCase());
        tvTotalLista.setText("Total: R$ " + String.format(LOCALE_BR, "%.2f", totalMes));
    }

    private void configurarFiltros() {
        containerFiltros.removeAllViews();

        Map<String, Integer> categoriasCount = new HashMap<>();
        for (Divida d : todasDividas) {
            int count = categoriasCount.getOrDefault(d.getCategoria(), 0);
            categoriasCount.put(d.getCategoria(), count + 1);
        }

        criarBotaoFiltro("Todos", todasDividas.size(), true);
        for (Map.Entry<String, Integer> entry : categoriasCount.entrySet()) {
            criarBotaoFiltro(entry.getKey(), entry.getValue(), false);
        }
    }

    private void criarBotaoFiltro(String categoria, int count, boolean selecionado) {
        Button btn = new Button(this);
        String texto = categoria.equals("Todos")
                ? "Todos"
                : categoria + (count > 1 ? " (" + count + ")" : "");
        btn.setText(texto);
        btn.setAllCaps(false);
        btn.setPadding(40, 12, 40, 12);
        btn.setTextSize(13f);

        aplicarEstiloBotao(btn, selecionado);

        btn.setOnClickListener(v -> {
            for (int i = 0; i < containerFiltros.getChildCount(); i++) {
                View child = containerFiltros.getChildAt(i);
                if (child instanceof Button) {
                    aplicarEstiloBotao((Button) child, false);
                }
            }
            aplicarEstiloBotao(btn, true);
            atualizarDashboard(categoria);
        });

        containerFiltros.addView(btn);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btn.getLayoutParams();
        params.setMargins(0, 0, 12, 0);
        btn.setLayoutParams(params);
    }

    private void aplicarEstiloBotao(Button btn, boolean selecionado) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(50f);

        if (selecionado) {
            drawable.setColor(Color.parseColor("#4C1D95"));
            drawable.setStroke(1, Color.parseColor("#A855F7"));
            btn.setTextColor(Color.WHITE);
        } else {
            drawable.setColor(Color.parseColor("#131C2E"));
            drawable.setStroke(1, Color.parseColor("#334155"));
            btn.setTextColor(Color.parseColor("#CBD5E1"));
        }
        btn.setBackground(drawable);
    }

    private void configurarExportar() {
        btnExportar.setOnClickListener(v ->
                Toast.makeText(this, "Exportação em breve!", Toast.LENGTH_SHORT).show());
    }

    private void configurarNavegacao() {
        tabInicio.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        });

        tabLancar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CadastroDividaActivity.class);
            startActivity(intent);
        });

        tabDividas.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DividasActivity.class);
            startActivity(intent);
        });

        tabRelatorios.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Relatórios", Toast.LENGTH_SHORT).show());

        tabConfig.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ConfiguracoesActivity.class);
            startActivity(intent);
        });
    }

    // ========== Ações dos botões dos cards ==========

    @Override
    public void onExcluirClick(Divida divida) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir dívida")
                .setMessage("Tem certeza que deseja excluir \"" + divida.getTitulo() + "\"?\n\n" +
                        "A dívida ficará registrada no Histórico como excluída.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    divida.setExcluida(true);
                    db.dividaDao().atualizar(divida);
                    carregarDados();
                    Toast.makeText(this, "Dívida excluída: " + divida.getTitulo(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditarClick(Divida divida) {
        Intent intent = new Intent(DashboardActivity.this, CadastroDividaActivity.class);
        intent.putExtra("divida_id", divida.getId());
        startActivity(intent);
    }

    @Override
    public void onPagarClick(Divida divida) {
        if (divida.isPago()) {
            Toast.makeText(this, "Essa dívida já está paga!", Toast.LENGTH_SHORT).show();
            return;
        }

        divida.valorPago = divida.getValorTotal();
        divida.setPago(true);

        db.dividaDao().atualizar(divida);
        carregarDados();
        Toast.makeText(this, "Parcela paga: " + divida.getTitulo() + " (" + divida.getParcela() + ")",
                Toast.LENGTH_SHORT).show();
    }
}