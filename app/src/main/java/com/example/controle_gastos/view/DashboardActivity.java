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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.controle_gastos.model.Renda;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.CsvExportHelper;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.PdfExportHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
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

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private static final int[] PERIODOS_DISPONIVEIS = {3, 6, 12};
    private static final int PERIODO_PADRAO = 6;

    private static final String[] MESES = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};

    private static final int[] CORES = {
            Color.parseColor("#10B981"),
            Color.parseColor("#3B82F6"),
            Color.parseColor("#F59E0B"),
            Color.parseColor("#EC4899"),
            Color.parseColor("#06B6D4"),
            Color.parseColor("#EF4444"),
            Color.parseColor("#8B5CF6"),
            Color.parseColor("#EAB308")
    };

    private PieChart pieChart;
    private LineChart lineChart;
    private RecyclerView rvLancamentos;
    private LinearLayout containerFiltros, containerPeriodos, llLegenda;
    private TextView btnExportar;
    private TextView tvTituloLista, tvTotalLista, tvVazioLista;
    private TextView tvTotalEvolucao, tvMediaEvolucao, tvTituloTotalEvolucao;

    private ImageView btnMesAnteriorDash, btnMesProximoDash;
    private TextView tvMesAtualDash, tvBadgeAtualDash;

    private TextView tvSalarioDash, tvGastosDash, tvSaldoDash, tvBalancoCaption;
    private ProgressBar progressBalanco;

    private ImageView tabInicio, tabLancar, tabDividas, tabConfig;
    private LinearLayout tabRelatorios;

    private List<Divida> todasDividas = new ArrayList<>();
    private final List<Divida> dividasFiltradas = new ArrayList<>();

    private final Map<String, TextView> chipsFiltro = new LinkedHashMap<>();
    private final Map<String, View> legendaRows = new HashMap<>();
    private final Map<String, Integer> coresPorCategoria = new HashMap<>();

    private AppDatabase db;
    private SessionManager session;

    private int mesSelecionado;
    private int anoSelecionado;
    private int mesesVisiveis = PERIODO_PADRAO;
    private String categoriaAtual = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        NavScrollHelper.attach(this);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        pieChart = findViewById(R.id.pieChart);
        lineChart = findViewById(R.id.lineChart);
        rvLancamentos = findViewById(R.id.rvLancamentos);
        containerFiltros = findViewById(R.id.containerFiltros);
        containerPeriodos = findViewById(R.id.containerPeriodos);
        llLegenda = findViewById(R.id.llLegenda);
        btnExportar = findViewById(R.id.btnExportar);
        tvTituloLista = findViewById(R.id.tvTituloLista);
        tvTotalLista = findViewById(R.id.tvTotalLista);
        tvVazioLista = findViewById(R.id.tvVazioLista);
        tvTotalEvolucao = findViewById(R.id.tvTotalEvolucao);
        tvMediaEvolucao = findViewById(R.id.tvMediaEvolucao);
        tvTituloTotalEvolucao = findViewById(R.id.tvTituloTotalEvolucao);

        btnMesAnteriorDash = findViewById(R.id.btnMesAnteriorDash);
        btnMesProximoDash = findViewById(R.id.btnMesProximoDash);
        tvMesAtualDash = findViewById(R.id.tvMesAtualDash);
        tvBadgeAtualDash = findViewById(R.id.tvBadgeAtualDash);

        tvSalarioDash = findViewById(R.id.tvSalarioDash);
        tvGastosDash = findViewById(R.id.tvGastosDash);
        tvSaldoDash = findViewById(R.id.tvSaldoDash);
        tvBalancoCaption = findViewById(R.id.tvBalancoCaption);
        progressBalanco = findViewById(R.id.progressBalanco);

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
        configurarExportar();
        configurarNavegacao();

        btnMesAnteriorDash.setOnClickListener(v -> {
            mesSelecionado--;
            if (mesSelecionado < 1) { mesSelecionado = 12; anoSelecionado--; }
            carregarDados();
        });

        btnMesProximoDash.setOnClickListener(v -> {
            mesSelecionado++;
            if (mesSelecionado > 12) { mesSelecionado = 1; anoSelecionado++; }
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
        todasDividas.sort((a, b) -> compararDatas(a.getVencimento(), b.getVencimento()));

        categoriaAtual = "Todos";

        atualizarTituloMes();
        montarGraficoCategorias();
        configurarFiltros();
        atualizarLista();
        atualizarBalancoMensal();
        atualizarGraficoLinha();
    }

    private void atualizarTituloMes() {
        tvMesAtualDash.setText(MESES[mesSelecionado - 1] + " " + anoSelecionado);

        Calendar c = Calendar.getInstance();
        boolean ehMesAtual = mesSelecionado == c.get(Calendar.MONTH) + 1
                && anoSelecionado == c.get(Calendar.YEAR);
        tvBadgeAtualDash.setVisibility(ehMesAtual ? View.VISIBLE : View.INVISIBLE);
    }

    private void atualizarBalancoMensal() {
        double gastos = 0;
        for (Divida d : todasDividas) {
            gastos += d.getValorTotal();
        }

        // ⭐ NOVO: Calcula o salário esperado do mês baseado na tabela Renda
        double salarioTotalMes = 0;
        List<Renda> rendas = db.rendaDao().listarPorUsuario(session.getUserId());
        for (Renda r : rendas) {
            salarioTotalMes += r.valor;
        }

        // Fallback: se não tem renda na tabela nova, usa o salarioMensal antigo
        if (rendas.isEmpty()) {
            Usuario user = db.usuarioDao().buscarPorId(session.getUserId());
            if (user != null) salarioTotalMes = user.getSalarioMensal();
        }

        tvSalarioDash.setText(moeda(salarioTotalMes));
        tvGastosDash.setText(moeda(gastos));

        if (salarioTotalMes > 0) {
            double saldo = salarioTotalMes - gastos;
            tvSaldoDash.setText(moeda(saldo));
            tvSaldoDash.setTextColor(Color.parseColor(saldo < 0 ? "#F87171" : "#34D399"));

            int pct = (int) Math.round((gastos / salarioTotalMes) * 100);
            progressBalanco.setProgress(Math.min(pct, 100));
            tvBalancoCaption.setText(pct + "% da renda mensal comprometida");
        } else {
            tvSaldoDash.setText(moeda(0));
            tvSaldoDash.setTextColor(Color.parseColor("#64748B"));
            progressBalanco.setProgress(0);
            tvBalancoCaption.setText("Renda não encontrada. Confira em Configurações.");
        }
    }

    private void configurarPieChart() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(4, 4, 4, 4);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.parseColor("#111A2E"));
        pieChart.setHoleRadius(72f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.getLegend().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setNoDataText("");
    }

    private void montarGraficoCategorias() {
        Map<String, Double> gastos = new HashMap<>();
        double total = 0;
        for (Divida d : todasDividas) {
            String cat = categoriaDe(d);
            gastos.put(cat, gastos.getOrDefault(cat, 0.0) + d.getValorTotal());
            total += d.getValorTotal();
        }

        List<Map.Entry<String, Double>> ordenado = new ArrayList<>(gastos.entrySet());
        ordenado.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        coresPorCategoria.clear();
        legendaRows.clear();
        llLegenda.removeAllViews();

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> cores = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < ordenado.size(); i++) {
            final String cat = ordenado.get(i).getKey();
            double valor = ordenado.get(i).getValue();
            int cor = CORES[i % CORES.length];

            coresPorCategoria.put(cat, cor);
            entries.add(new PieEntry((float) valor));
            cores.add(cor);

            View row = inflater.inflate(R.layout.item_legenda_categoria, llLegenda, false);
            View dot = row.findViewById(R.id.vCorLegenda);
            TextView tvNome = row.findViewById(R.id.tvNomeLegendaCat);
            TextView tvPerc = row.findViewById(R.id.tvPercentualLegenda);
            TextView tvValor = row.findViewById(R.id.tvValorLegenda);

            GradientDrawable bolinha = new GradientDrawable();
            bolinha.setShape(GradientDrawable.OVAL);
            bolinha.setColor(cor);
            dot.setBackground(bolinha);

            double pct = total > 0 ? (valor / total) * 100.0 : 0;
            tvNome.setText(cat);
            tvPerc.setText(String.format(LOCALE_BR, "%.0f%%", pct));
            tvValor.setText(moeda(valor));

            row.setOnClickListener(v ->
                    selecionarCategoria(cat.equals(categoriaAtual) ? "Todos" : cat));

            legendaRows.put(cat, row);
            llLegenda.addView(row);
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(1f));
            cores.add(Color.parseColor("#1E2A44"));

            TextView vazio = new TextView(this);
            vazio.setText("Sem lançamentos neste mês");
            vazio.setTextColor(Color.parseColor("#94A3B8"));
            vazio.setTextSize(14f);
            vazio.setGravity(Gravity.CENTER);
            vazio.setPadding(0, dp(8), 0, dp(8));
            llLegenda.addView(vazio);
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(cores);
        dataSet.setSliceSpace(3f);
        dataSet.setDrawValues(false);
        pieChart.setData(new PieData(dataSet));

        String labelTotal = "TOTAL\n";
        SpannableStringBuilder centro = new SpannableStringBuilder();
        centro.append(labelTotal).append(moeda(total));
        centro.setSpan(new ForegroundColorSpan(Color.parseColor("#94A3B8")), 0, labelTotal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        centro.setSpan(new RelativeSizeSpan(0.75f), 0, labelTotal.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        centro.setSpan(new ForegroundColorSpan(Color.WHITE), labelTotal.length(), centro.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        centro.setSpan(new StyleSpan(Typeface.BOLD), labelTotal.length(), centro.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        pieChart.setCenterText(centro);
        pieChart.setCenterTextSize(18f);
        pieChart.animateY(600);
        pieChart.invalidate();
    }

    private void configurarFiltros() {
        containerFiltros.removeAllViews();
        chipsFiltro.clear();

        Map<String, Integer> contagem = new HashMap<>();
        for (Divida d : todasDividas) {
            String cat = categoriaDe(d);
            contagem.put(cat, contagem.getOrDefault(cat, 0) + 1);
        }

        adicionarChipFiltro("Todos", "Todos (" + todasDividas.size() + ")");

        List<String> categorias = new ArrayList<>(contagem.keySet());
        categorias.sort(String::compareToIgnoreCase);
        for (String cat : categorias) {
            adicionarChipFiltro(cat, cat + " (" + contagem.get(cat) + ")");
        }

        atualizarEstiloFiltros();
    }

    private void adicionarChipFiltro(String categoria, String texto) {
        TextView chip = criarChip(texto);
        chip.setOnClickListener(v -> selecionarCategoria(categoria));
        chipsFiltro.put(categoria, chip);
        containerFiltros.addView(chip);
    }

    private void selecionarCategoria(String categoria) {
        categoriaAtual = categoria;
        atualizarEstiloFiltros();
        atualizarSelecaoLegenda();
        atualizarLista();
    }

    private void atualizarEstiloFiltros() {
        for (Map.Entry<String, TextView> e : chipsFiltro.entrySet()) {
            estilizarChip(e.getValue(), e.getKey().equals(categoriaAtual));
        }
    }

    private void atualizarSelecaoLegenda() {
        for (Map.Entry<String, View> e : legendaRows.entrySet()) {
            boolean sel = e.getKey().equals(categoriaAtual);
            e.getValue().setBackgroundResource(sel ? R.drawable.bg_legenda_ativa : 0);
        }
    }

    private void atualizarLista() {
        dividasFiltradas.clear();
        double total = 0;
        for (Divida d : todasDividas) {
            if (categoriaAtual.equals("Todos") || categoriaDe(d).equals(categoriaAtual)) {
                dividasFiltradas.add(d);
                total += d.getValorTotal();
            }
        }

        rvLancamentos.setAdapter(new DividaDashboardAdapter(dividasFiltradas));

        boolean vazio = dividasFiltradas.isEmpty();
        tvVazioLista.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvLancamentos.setVisibility(vazio ? View.GONE : View.VISIBLE);

        String nomeMes = MESES[mesSelecionado - 1].toUpperCase(LOCALE_BR);
        tvTituloLista.setText(categoriaAtual.equals("Todos")
                ? "DÍVIDAS DE " + nomeMes
                : "DÍVIDAS DE " + nomeMes + " · " + categoriaAtual.toUpperCase(LOCALE_BR));
        tvTotalLista.setText(moeda(total));
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
        lineChart.setExtraOffsets(12f, 20f, 16f, 8f);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setTextColor(Color.parseColor("#94A3B8"));
        xAxis.setTextSize(11f);
        xAxis.setGranularity(1f);

        YAxis yAxisLeft = lineChart.getAxisLeft();
        yAxisLeft.setDrawGridLines(true);
        yAxisLeft.setGridColor(Color.parseColor("#1E2A44"));
        yAxisLeft.setGridLineWidth(1f);
        yAxisLeft.setDrawAxisLine(false);
        yAxisLeft.setTextColor(Color.parseColor("#94A3B8"));
        yAxisLeft.setTextSize(10f);
        yAxisLeft.setAxisMinimum(0f);
        yAxisLeft.setSpaceTop(30f);
        yAxisLeft.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatarCurto(value, true);
            }
        });

        lineChart.getAxisRight().setEnabled(false);
    }

    private void criarChipsPeriodo() {
        containerPeriodos.removeAllViews();

        for (int periodo : PERIODOS_DISPONIVEIS) {
            TextView chip = criarChip(periodo + "M");
            chip.setTag(periodo);
            estilizarChip(chip, periodo == mesesVisiveis);

            chip.setOnClickListener(v -> {
                mesesVisiveis = periodo;
                for (int i = 0; i < containerPeriodos.getChildCount(); i++) {
                    View child = containerPeriodos.getChildAt(i);
                    if (child instanceof TextView && child.getTag() instanceof Integer) {
                        estilizarChip((TextView) child, ((Integer) child.getTag()) == mesesVisiveis);
                    }
                }
                atualizarGraficoLinha();
            });

            containerPeriodos.addView(chip);
        }
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
                if (pertenceAoMes(d, mesRef, anoRef)) {
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
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircleHole(true);
        dataSet.setCircleHoleColor(Color.parseColor("#111A2E"));
        dataSet.setCircleHoleRadius(3f);
        dataSet.setCircleRadius(5f);

        List<Integer> coresCirculos = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            boolean destaque = (i == indexMaior && maiorValor > 0);
            coresCirculos.add(Color.parseColor(destaque ? "#F59E0B" : "#10B981"));
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

        dataSet.setDrawValues(mesesVisiveis <= 6);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatarCurto(value, false);
            }
        });

        lineChart.setData(new LineData(dataSet));

        final int visiveis = mesesVisiveis;
        lineChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int idx = Math.round(value);
                if (idx < 0 || idx >= labelsMeses.size()) return "";
                if (visiveis > 6 && idx % 2 != 0) return "";
                return labelsMeses.get(idx);
            }
        });
        lineChart.getXAxis().setLabelCount(mesesVisiveis, true);

        lineChart.getAxisLeft().removeAllLimitLines();
        if (media > 0.01) {
            LimitLine linhaMedia = new LimitLine((float) media,
                    String.format(LOCALE_BR, "Média R$ %.0f", media));
            linhaMedia.setLineColor(Color.parseColor("#F59E0B"));
            linhaMedia.setLineWidth(1.5f);
            linhaMedia.enableDashedLine(12, 8, 0);
            linhaMedia.setTextColor(Color.parseColor("#F59E0B"));
            linhaMedia.setTextSize(10f);
            linhaMedia.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            lineChart.getAxisLeft().addLimitLine(linhaMedia);
        }

        lineChart.animateX(700);
        lineChart.invalidate();

        tvTituloTotalEvolucao.setText("Total em " + mesesVisiveis + " meses");
        tvTotalEvolucao.setText(moeda(totalPeriodo));
        tvMediaEvolucao.setText(moeda(media));
    }

    private void configurarExportar() {
        btnExportar.setOnClickListener(v -> mostrarDialogoExportacao());
    }

    private void mostrarDialogoExportacao() {
        String[] opcoes = {"Exportar como CSV (Excel)", "Exportar como PDF (Relatório)"};

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
            Toast.makeText(this, "Nenhum lançamento para exportar neste mês.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mesAno = getMesAnoString();
        String sufixo = categoriaAtual.equals("Todos") ? "" : "_" + categoriaAtual.toLowerCase().replace(" ", "_");

        File arquivo = CsvExportHelper.gerarCsv(this, dividasFiltradas, mesAno + sufixo);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o arquivo CSV.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "CSV gerado com sucesso!", Toast.LENGTH_SHORT).show();
        CsvExportHelper.compartilharCsv(this, arquivo);
    }

    private void exportarPdf() {
        if (dividasFiltradas.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar neste mês.", Toast.LENGTH_SHORT).show();
            return;
        }

        String mesAno = getMesAnoString();
        File arquivo = PdfExportHelper.gerarPdf(this, dividasFiltradas, mesAno, categoriaAtual);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o PDF.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show();
        PdfExportHelper.compartilharPdf(this, arquivo);
    }

    private String getMesAnoString() {
        return MESES[mesSelecionado - 1] + " " + anoSelecionado;
    }

    private void configurarNavegacao() {
        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(DashboardActivity.this, InicioActivity.class));
            finish();
        });

        tabLancar.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, CadastroDividaActivity.class)));

        tabDividas.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, DividasActivity.class)));

        tabRelatorios.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Relatórios", Toast.LENGTH_SHORT).show());

        tabConfig.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ConfiguracoesActivity.class)));
    }

    private TextView criarChip(String texto) {
        TextView chip = new TextView(this);
        chip.setText(texto);
        chip.setTextSize(14f);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(16), 0, dp(16), 0);
        chip.setClickable(true);
        chip.setFocusable(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(44));
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);

        estilizarChip(chip, false);
        return chip;
    }

    private void estilizarChip(TextView chip, boolean selecionado) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(22));

        if (selecionado) {
            d.setColor(Color.parseColor("#123D33"));
            d.setStroke(dp(1), Color.parseColor("#10B981"));
            chip.setTextColor(Color.parseColor("#34D399"));
            chip.setTypeface(null, Typeface.BOLD);
        } else {
            d.setColor(Color.parseColor("#0E1424"));
            d.setStroke(dp(1), Color.parseColor("#1E2A44"));
            chip.setTextColor(Color.parseColor("#C7CEDB"));
            chip.setTypeface(null, Typeface.NORMAL);
        }
        chip.setBackground(d);
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private String moeda(double valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
    }

    private String formatarCurto(float value, boolean mostrarZero) {
        if (value <= 0.01f) return mostrarZero ? "R$ 0" : "";
        if (value >= 1000) return String.format(LOCALE_BR, "R$ %.1fk", value / 1000f);
        return String.format(LOCALE_BR, "R$ %.0f", value);
    }

    private String categoriaDe(Divida d) {
        String cat = d.getCategoria();
        return (cat == null || cat.trim().isEmpty()) ? "Sem categoria" : cat.trim();
    }

    private boolean pertenceAoMes(Divida d, int mes, int ano) {
        String venc = d.getVencimento();
        if (venc == null || venc.isEmpty()) return false;
        try {
            String[] partes = venc.split("/");
            if (partes.length != 3) return false;
            return Integer.parseInt(partes[1]) == mes && Integer.parseInt(partes[2]) == ano;
        } catch (Exception e) {
            return false;
        }
    }

    private int compararDatas(String d1, String d2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR);
            Date data1 = sdf.parse(d1);
            Date data2 = sdf.parse(d2);
            if (data1 == null || data2 == null) return 0;
            return data1.compareTo(data2);
        } catch (Exception e) {
            return 0;
        }
    }
}