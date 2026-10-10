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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.DividaDashboardAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.SessionManager;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

public class DashboardActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    // ⭐ Períodos disponíveis no gráfico de linha
    private static final int[] PERIODOS_DISPONIVEIS = {3, 6, 12};
    private static final int PERIODO_PADRAO = 6;

    private com.github.mikephil.charting.charts.PieChart pieChart;
    private com.github.mikephil.charting.charts.LineChart lineChart;
    private RecyclerView rvLancamentos;
    private LinearLayout containerFiltros, containerPeriodos;
    private Button btnExportar;
    private TextView tvTituloLista, tvTotalLista, tvTotalEvolucao;

    private TextView btnMesAnteriorDash, btnMesProximoDash, tvMesAtualDash, tvBadgeAtualDash;

    private List<Divida> todasDividas;
    private List<Divida> dividasFiltradas;
    private DividaDashboardAdapter dividaAdapter;

    private float totalParaPercentual = 0f;

    private AppDatabase db;
    private SessionManager session;

    private int mesSelecionado;
    private int anoSelecionado;

    // ⭐ Quantidade de meses visíveis no gráfico de linha
    private int mesesVisiveis = PERIODO_PADRAO;

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
        lineChart = findViewById(R.id.lineChart);
        tvTotalEvolucao = findViewById(R.id.tvTotalEvolucao);
        rvLancamentos = findViewById(R.id.rvLancamentos);
        containerFiltros = findViewById(R.id.containerFiltros);
        containerPeriodos = findViewById(R.id.containerPeriodos);
        btnExportar = findViewById(R.id.btnExportar);
        tvTituloLista = findViewById(R.id.tvTituloLista);
        tvTotalLista = findViewById(R.id.tvTotalLista);

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

        Calendar c = Calendar.getInstance();
        mesSelecionado = c.get(Calendar.MONTH) + 1;
        anoSelecionado = c.get(Calendar.YEAR);

        configurarPieChart();
        configurarGraficoLinha();
        criarChipsPeriodo();
        carregarDados();
        configurarExportar();
        configurarNavegacao();

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
        atualizarGraficoLinha();
        configurarFiltros();
    }

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

    /**
     * ⭐ Configura a aparência do gráfico de linha.
     */
    private void configurarGraficoLinha() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setNoDataText("Sem dados para exibir");
        lineChart.setNoDataTextColor(Color.parseColor("#64748B"));

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setTextSize(11f);
        xAxis.setGranularity(1f);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setGridColor(Color.parseColor("#1E293B"));
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.parseColor("#94A3B8"));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setDrawLabels(false);

        lineChart.getAxisRight().setEnabled(false);
    }

    /**
     * ⭐ Cria os chips de período (3M / 6M / 12M).
     */
    private void criarChipsPeriodo() {
        containerPeriodos.removeAllViews();

        for (int periodo : PERIODOS_DISPONIVEIS) {
            TextView chip = new TextView(this);
            chip.setText(periodo + "M");
            chip.setTextSize(13f);
            chip.setPadding(40, 16, 40, 16);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 0);
            chip.setLayoutParams(params);

            boolean selecionado = (periodo == mesesVisiveis);
            aplicarEstiloChipPeriodo(chip, selecionado);

            chip.setOnClickListener(v -> {
                mesesVisiveis = periodo;
                // Atualiza a aparência dos chips
                for (int i = 0; i < containerPeriodos.getChildCount(); i++) {
                    View child = containerPeriodos.getChildAt(i);
                    if (child instanceof TextView) {
                        TextView c = (TextView) child;
                        String txt = c.getText().toString().replace("M", "");
                        try {
                            int p = Integer.parseInt(txt);
                            aplicarEstiloChipPeriodo(c, p == mesesVisiveis);
                        } catch (NumberFormatException ignored) { }
                    }
                }
                atualizarGraficoLinha();
            });

            containerPeriodos.addView(chip);
        }
    }

    private void aplicarEstiloChipPeriodo(TextView chip, boolean selecionado) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(50f);

        if (selecionado) {
            d.setColor(Color.parseColor("#4C1D95"));
            d.setStroke(1, Color.parseColor("#A855F7"));
            chip.setTextColor(Color.WHITE);
        } else {
            d.setColor(Color.parseColor("#0B1220"));
            d.setStroke(1, Color.parseColor("#334155"));
            chip.setTextColor(Color.parseColor("#CBD5E1"));
        }
        chip.setBackground(d);
    }

    /**
     * ⭐ Calcula o total dos últimos N meses e popula o gráfico de linha.
     * N = mesesVisiveis (3, 6 ou 12).
     */
    private void atualizarGraficoLinha() {
        List<Divida> todas = db.dividaDao().listarPorUsuario(session.getUserId());

        Calendar c = Calendar.getInstance();
        c.set(anoSelecionado, mesSelecionado - 1, 1);

        List<Entry> entries = new ArrayList<>();
        final List<String> labelsMeses = new ArrayList<>();
        String[] nomesMeses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

        double totalPeriodo = 0;

        for (int i = mesesVisiveis - 1; i >= 0; i--) {
            Calendar cMes = (Calendar) c.clone();
            cMes.add(Calendar.MONTH, -i);
            int mesRef = cMes.get(Calendar.MONTH) + 1;
            int anoRef = cMes.get(Calendar.YEAR);

            double totalMes = 0;
            for (Divida d : todas) {
                if (pertenceAoMes(d, mesRef, anoRef)) {
                    totalMes += d.getValorTotal();
                }
            }

            entries.add(new Entry(mesesVisiveis - 1 - i, (float) totalMes));
            labelsMeses.add(nomesMeses[mesRef - 1]);
            totalPeriodo += totalMes;
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gastos");
        dataSet.setColor(Color.parseColor("#10B981"));
        dataSet.setLineWidth(2.5f);
        dataSet.setCircleColor(Color.parseColor("#10B981"));
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleColor(Color.parseColor("#131C2E"));
        dataSet.setCircleHoleRadius(2f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#10B981"));
        dataSet.setFillAlpha(40);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircleHole(true);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = (int) value;
                if (idx >= 0 && idx < labelsMeses.size()) {
                    return labelsMeses.get(idx);
                }
                return "";
            }
        });

        // Ajusta a densidade de labels conforme o período
        if (mesesVisiveis <= 6) {
            lineChart.getXAxis().setLabelCount(mesesVisiveis, true);
        } else {
            lineChart.getXAxis().setLabelCount(6, true); // 12 meses: mostra 6 labels
        }

        lineChart.invalidate();

        tvTotalEvolucao.setText(String.format(LOCALE_BR,
                "Total: R$ %.2f em %d meses", totalPeriodo, mesesVisiveis));
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

        dividaAdapter = new DividaDashboardAdapter(dividasFiltradas);
        rvLancamentos.setAdapter(dividaAdapter);

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
}