package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.DividaDashboardAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.CsvExportHelper;
import com.example.controle_gastos.utils.PdfExportHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.github.mikephil.charting.components.LimitLine;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

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

    private int mesesVisiveis = PERIODO_PADRAO;

    // ⭐ Guarda a categoria selecionada para a exportação
    private String categoriaAtual = "Todos";

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
        lineChart.setExtraOffsets(8f, 16f, 8f, 8f);

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
        yAxisLeft.setGridLineWidth(1f);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.parseColor("#94A3B8"));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setDrawLabels(true);
        yAxisLeft.setSpaceTop(30f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value <= 0.01f) return "R$ 0";
                if (value >= 1000) return String.format(LOCALE_BR, "R$ %.1fk", value / 1000f);
                return String.format(LOCALE_BR, "R$ %.0f", value);
            }
        });

        lineChart.getAxisRight().setEnabled(false);
    }

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

    private void atualizarGraficoLinha() {
        List<Divida> todas = db.dividaDao().listarPorUsuario(session.getUserId());

        int mesesPassados = mesesVisiveis / 2;

        Calendar c = Calendar.getInstance();
        c.set(anoSelecionado, mesSelecionado - 1, 1);
        c.add(Calendar.MONTH, -mesesPassados);

        List<Entry> entries = new ArrayList<>();
        final List<String> labelsMeses = new ArrayList<>();
        String[] nomesMeses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

        double totalPeriodo = 0;

        for (int i = 0; i < mesesVisiveis; i++) {
            int mesRef = c.get(Calendar.MONTH) + 1;
            int anoRef = c.get(Calendar.YEAR);

            double totalMes = 0;
            for (Divida d : todas) {
                if (pertenceMesmoMes(d, mesRef, anoRef)) {
                    totalMes += d.getValorTotal();
                }
            }

            entries.add(new Entry(i, (float) totalMes));
            labelsMeses.add(nomesMeses[mesRef - 1]);
            totalPeriodo += totalMes;

            c.add(Calendar.MONTH, 1);
        }

        double media = totalPeriodo / Math.max(mesesVisiveis, 1);
        float maiorValor = 0;
        int indexMaior = -1;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).getY() > maiorValor) {
                maiorValor = entries.get(i).getY();
                indexMaior = i;
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Gastos");
        dataSet.setColor(Color.parseColor("#10B981"));
        dataSet.setLineWidth(3f);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.parseColor("#131C2E"));
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleRadius(5f);

        List<Integer> coresCirculos = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            if (i == indexMaior && maiorValor > 0) {
                coresCirculos.add(Color.parseColor("#A855F7"));
            } else {
                coresCirculos.add(Color.parseColor("#10B981"));
            }
        }
        dataSet.setCircleColors(coresCirculos);

        Drawable gradiente = ContextCompat.getDrawable(this, R.drawable.bg_line_chart_gradient);
        if (gradiente != null) {
            dataSet.setFillDrawable(gradiente);
        } else {
            dataSet.setFillColor(Color.parseColor("#10B981"));
            dataSet.setFillAlpha(40);
        }
        dataSet.setDrawFilled(true);

        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value <= 0.01f) return "";
                if (value >= 1000) return String.format(LOCALE_BR, "R$ %.1fk", value / 1000f);
                return String.format(LOCALE_BR, "R$ %.0f", value);
            }
        });

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

        if (mesesVisiveis <= 6) {
            lineChart.getXAxis().setLabelCount(mesesVisiveis, true);
        } else {
            lineChart.getXAxis().setLabelCount(6, true);
        }

        lineChart.getAxisLeft().removeAllLimitLines();
        if (media > 0.01) {
            LimitLine linhaMedia = new LimitLine((float) media,
                    String.format(LOCALE_BR, "Média: R$ %.0f", media));
            linhaMedia.setLineColor(Color.parseColor("#F59E0B"));
            linhaMedia.setLineWidth(1.5f);
            linhaMedia.enableDashedLine(12, 8, 0);
            linhaMedia.setTextColor(Color.parseColor("#F59E0B"));
            linhaMedia.setTextSize(10f);
            linhaMedia.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            lineChart.getAxisLeft().addLimitLine(linhaMedia);
        }

        lineChart.animateX(700);

        tvTotalEvolucao.setText(String.format(LOCALE_BR,
                "Total: R$ %.2f em %d meses  •  Média: R$ %.2f",
                totalPeriodo, mesesVisiveis, media));
    }

    private boolean pertenceMesmoMes(Divida d, int mes, int ano) {
        return pertenceAoMes(d, mes, ano);
    }

    private void atualizarDashboard(String categoriaFiltro) {
        categoriaAtual = categoriaFiltro;

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

    // ==========================================
    // ⭐ EXPORTAÇÃO (CSV e PDF)
    // ==========================================

    private void configurarExportar() {
        btnExportar.setOnClickListener(v -> mostrarDialogoExportacao());
    }

    private void mostrarDialogoExportacao() {
        String[] opcoes = {"📄  Exportar como CSV (Excel)", "📋  Exportar como PDF (Relatório)"};

        new AlertDialog.Builder(this)
                .setTitle("Escolha o formato")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        exportarCsv();
                    } else {
                        exportarPdf();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void exportarCsv() {
        if (dividasFiltradas.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar neste mês.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String mesAno = getMesAnoString();
        String sufixo = categoriaAtual.equals("Todos")
                ? ""
                : "_" + categoriaAtual.toLowerCase().replace(" ", "_");

        File arquivo = CsvExportHelper.gerarCsv(this, dividasFiltradas, mesAno + sufixo);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o arquivo CSV.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "CSV gerado com sucesso!", Toast.LENGTH_SHORT).show();
        CsvExportHelper.compartilharCsv(this, arquivo);
    }

    private void exportarPdf() {
        if (dividasFiltradas.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar neste mês.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String mesAno = getMesAnoString();
        File arquivo = PdfExportHelper.gerarPdf(this, dividasFiltradas, mesAno, categoriaAtual);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o PDF.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show();
        PdfExportHelper.compartilharPdf(this, arquivo);
    }

    /**
     * ⭐ Retorna "Outubro 2026" (mesmo formato usado no Histórico).
     */
    private String getMesAnoString() {
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        return meses[mesSelecionado - 1] + " " + anoSelecionado;
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