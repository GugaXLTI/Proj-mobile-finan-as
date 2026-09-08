package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.LancamentoAdapter;
import com.example.controle_gastos.adapter.LegendaAdapter;
import com.example.controle_gastos.model.CategoriaResumo;
import com.example.controle_gastos.model.Transacao;
import com.example.controle_gastos.utils.DadosMock;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private com.github.mikephil.charting.charts.PieChart pieChart;
    private RecyclerView rvLegendas, rvLancamentos;
    private LinearLayout containerFiltros;
    private Button btnExportar, btnVerDividas;
    private TextView tvTituloLista, tvTotalLista;

    private List<Transacao> todasTransacoes;
    private List<Transacao> transacoesFiltradas;
    private LancamentoAdapter lancamentoAdapter;
    private LegendaAdapter legendaAdapter;

    // Bottom Navigation
    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Vincular componentes principais
        pieChart = findViewById(R.id.pieChart);
        rvLegendas = findViewById(R.id.rvLegendas);
        rvLancamentos = findViewById(R.id.rvLancamentos);
        containerFiltros = findViewById(R.id.containerFiltros);
        btnExportar = findViewById(R.id.btnExportar);
        btnVerDividas = findViewById(R.id.btnVerDividas);
        tvTituloLista = findViewById(R.id.tvTituloLista);
        tvTotalLista = findViewById(R.id.tvTotalLista);

        // Vincular Bottom Navigation
        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        // Carregar dados mock
        todasTransacoes = DadosMock.getTransacoesIniciais();
        transacoesFiltradas = new ArrayList<>(todasTransacoes);

        // Configurar RecyclerViews
        rvLegendas.setLayoutManager(new LinearLayoutManager(this));
        rvLancamentos.setLayoutManager(new LinearLayoutManager(this));

        // Configurar gráfico
        configurarPieChart();

        // Atualizar Dashboard
        atualizarDashboard("Todos");
        configurarFiltros();
        configurarExportar();

        // ======== NAVEGAÇÃO ========

        // Botão "Ver Dívidas"
        btnVerDividas.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DividasActivity.class);
            startActivity(intent);
        });

        // Aba "Início"
        tabInicio.setOnClickListener(v -> {
            Toast.makeText(this, "Você já está no Início", Toast.LENGTH_SHORT).show();
        });

        // Aba "Lançar" (se não tiver tela, pode usar Toast)
        tabLancar.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade em breve!", Toast.LENGTH_SHORT).show();
        });

        // Aba "Dívidas"
        tabDividas.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, DividasActivity.class);
            startActivity(intent);
        });

        // Aba "Relatórios"
        tabRelatorios.setOnClickListener(v -> {
            Toast.makeText(this, "Você já está em Relatórios", Toast.LENGTH_SHORT).show();
        });

        // Aba "Config" (quando a tela de Configurações existir, substitua o Toast pelo Intent)
        tabConfig.setOnClickListener(v -> {
            Toast.makeText(this, "Abrir Configurações (em breve)", Toast.LENGTH_SHORT).show();
        });
    }

    private void configurarPieChart() {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.WHITE);
    }

    private void atualizarDashboard(String categoriaFiltro) {
        // Filtrar transações
        transacoesFiltradas.clear();
        for (Transacao t : todasTransacoes) {
            if (categoriaFiltro.equals("Todos") || t.getCategoria().equals(categoriaFiltro)) {
                transacoesFiltradas.add(t);
            }
        }

        // Atualizar gráfico de pizza (apenas despesas)
        Map<String, Double> gastosPorCategoria = new HashMap<>();
        for (Transacao t : transacoesFiltradas) {
            if (t.getTipo().equals("DESPESA")) {
                double valor = gastosPorCategoria.getOrDefault(t.getCategoria(), 0.0);
                gastosPorCategoria.put(t.getCategoria(), valor + t.getValor());
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(100f, "Sem dados"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setValueLineColor(Color.WHITE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        double totalDespesas = 0;
        for (Transacao t : transacoesFiltradas) {
            if (t.getTipo().equals("DESPESA")) totalDespesas += t.getValor();
        }
        pieChart.setCenterText("Total\nR$ " + String.format(Locale.getDefault(), "%.2f", totalDespesas));
        pieChart.invalidate();

        // Atualizar legendas
        Map<String, CategoriaResumo> resumoMap = new HashMap<>();
        for (Transacao t : transacoesFiltradas) {
            if (t.getTipo().equals("DESPESA")) {
                CategoriaResumo resumo = resumoMap.get(t.getCategoria());
                if (resumo == null) {
                    resumoMap.put(t.getCategoria(), new CategoriaResumo(t.getCategoria(), t.getValor(), 1));
                } else {
                    resumoMap.put(t.getCategoria(),
                            new CategoriaResumo(t.getCategoria(),
                                    resumo.getTotal() + t.getValor(),
                                    resumo.getQuantidade() + 1));
                }
            }
        }

        List<CategoriaResumo> categorias = new ArrayList<>(resumoMap.values());
        legendaAdapter = new LegendaAdapter(categorias);
        rvLegendas.setAdapter(legendaAdapter);

        // Atualizar lista de lançamentos
        lancamentoAdapter = new LancamentoAdapter(transacoesFiltradas);
        rvLancamentos.setAdapter(lancamentoAdapter);

        // Atualizar título e total da lista
        double totalLista = 0;
        for (Transacao t : transacoesFiltradas) {
            totalLista += t.getValor();
        }
        tvTituloLista.setText(categoriaFiltro.equals("Todos") ? "LANÇAMENTOS" : "LANÇAMENTOS EM " + categoriaFiltro.toUpperCase());
        tvTotalLista.setText("Total: R$ " + String.format(Locale.getDefault(), "%.2f", totalLista));
    }

    private void configurarFiltros() {
        Map<String, Integer> categoriasCount = new HashMap<>();
        for (Transacao t : todasTransacoes) {
            if (t.getTipo().equals("DESPESA")) {
                int count = categoriasCount.getOrDefault(t.getCategoria(), 0);
                categoriasCount.put(t.getCategoria(), count + 1);
            }
        }

        criarBotaoFiltro("Todos", todasTransacoes.size());

        for (Map.Entry<String, Integer> entry : categoriasCount.entrySet()) {
            criarBotaoFiltro(entry.getKey(), entry.getValue());
        }
    }

    private void criarBotaoFiltro(String categoria, int count) {
        Button btn = new Button(this);
        btn.setText(categoria + (categoria.equals("Todos") ? "" : " (" + count + ")"));
        btn.setPadding(24, 8, 24, 8);
        btn.setTextColor(getResources().getColor(android.R.color.white, getTheme()));

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(getResources().getColor(R.color.card_bg, getTheme()));
        drawable.setCornerRadius(20f);
        drawable.setStroke(1, getResources().getColor(R.color.text_gray, getTheme()));
        btn.setBackground(drawable);

        btn.setOnClickListener(v -> atualizarDashboard(categoria));

        containerFiltros.addView(btn);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btn.getLayoutParams();
        params.setMargins(0, 0, 12, 0);
        btn.setLayoutParams(params);
    }

    private void configurarExportar() {
        btnExportar.setOnClickListener(v -> {
            Toast.makeText(this, "Exportação em breve!", Toast.LENGTH_SHORT).show();
        });
    }
}