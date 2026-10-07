package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
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
import com.example.controle_gastos.adapter.HistoricoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.CsvExportHelper;
import com.example.controle_gastos.utils.PdfExportHelper;
import com.example.controle_gastos.utils.SessionManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoricoActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private TextView btnVoltarHistorico, btnMesAnterior, btnMesProximo;
    private TextView tvMesAtual, tvBadgeAtual;
    private TextView tvTotalMes, tvJaPagoMes, tvFaltaPagarMes;
    private TextView tvTituloListaHistorico;
    private Button btnBaixarResumo;
    private LinearLayout containerFiltrosHistorico;
    private RecyclerView rvLancamentosHistorico;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;
    private HistoricoAdapter adapter;

    private List<Divida> todasDividas = new ArrayList<>();
    private List<Divida> dividasDoMes = new ArrayList<>();
    private List<Divida> dividasFiltradas = new ArrayList<>();

    private int mesSelecionado;
    private int anoSelecionado;
    private String categoriaSelecionada = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historico);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        btnVoltarHistorico = findViewById(R.id.btnVoltarHistorico);
        btnMesAnterior = findViewById(R.id.btnMesAnterior);
        btnMesProximo = findViewById(R.id.btnMesProximo);
        tvMesAtual = findViewById(R.id.tvMesAtual);
        tvBadgeAtual = findViewById(R.id.tvBadgeAtual);
        tvTotalMes = findViewById(R.id.tvTotalMes);
        tvJaPagoMes = findViewById(R.id.tvJaPagoMes);
        tvFaltaPagarMes = findViewById(R.id.tvFaltaPagarMes);
        tvTituloListaHistorico = findViewById(R.id.tvTituloListaHistorico);
        btnBaixarResumo = findViewById(R.id.btnBaixarResumo);
        containerFiltrosHistorico = findViewById(R.id.containerFiltrosHistorico);
        rvLancamentosHistorico = findViewById(R.id.rvLancamentosHistorico);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvLancamentosHistorico.setLayoutManager(new LinearLayoutManager(this));

        Calendar c = Calendar.getInstance();
        mesSelecionado = c.get(Calendar.MONTH) + 1;
        anoSelecionado = c.get(Calendar.YEAR);

        carregarDividas();
        atualizarTela();

        btnVoltarHistorico.setOnClickListener(v -> finish());
        btnMesAnterior.setOnClickListener(v -> {
            mesSelecionado--;
            if (mesSelecionado < 1) {
                mesSelecionado = 12;
                anoSelecionado--;
            }
            categoriaSelecionada = "Todos";
            atualizarTela();
        });
        btnMesProximo.setOnClickListener(v -> {
            mesSelecionado++;
            if (mesSelecionado > 12) {
                mesSelecionado = 1;
                anoSelecionado++;
            }
            categoriaSelecionada = "Todos";
            atualizarTela();
        });

        // ⭐ Diálogo CSV/PDF
        btnBaixarResumo.setOnClickListener(v -> mostrarDialogoExportacao());

        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(this, InicioActivity.class));
            finish();
        });
        tabLancar.setOnClickListener(v -> startActivity(new Intent(this, CadastroDividaActivity.class)));
        tabDividas.setOnClickListener(v -> {
            startActivity(new Intent(this, DividasActivity.class));
            finish();
        });
        tabRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        tabConfig.setOnClickListener(v -> {
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            finish();
        });
    }

    private void carregarDividas() {
        todasDividas = db.dividaDao().listarTodasParaHistorico(session.getUserId());
    }

    private void atualizarTela() {
        atualizarTituloMes();
        filtrarDividasDoMes();
        atualizarResumo();
        montarFiltros();
        atualizarLista();
    }

    private void atualizarTituloMes() {
        String[] meses = {"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"};
        tvMesAtual.setText(meses[mesSelecionado - 1] + " " + anoSelecionado);

        Calendar c = Calendar.getInstance();
        int mesAtual = c.get(Calendar.MONTH) + 1;
        int anoAtual = c.get(Calendar.YEAR);

        if (mesSelecionado == mesAtual && anoSelecionado == anoAtual) {
            tvBadgeAtual.setVisibility(View.VISIBLE);
        } else {
            tvBadgeAtual.setVisibility(View.GONE);
        }
    }

    private void filtrarDividasDoMes() {
        dividasDoMes.clear();
        for (Divida d : todasDividas) {
            String venc = d.getVencimento();
            if (venc == null || venc.isEmpty()) continue;

            try {
                String[] partes = venc.split("/");
                if (partes.length == 3) {
                    int mes = Integer.parseInt(partes[1]);
                    int ano = Integer.parseInt(partes[2]);
                    if (mes == mesSelecionado && ano == anoSelecionado) {
                        dividasDoMes.add(d);
                    }
                }
            } catch (Exception e) {
                // ignora vencimentos inválidos
            }
        }
    }

    private void atualizarResumo() {
        double total = 0;
        double pago = 0;

        for (Divida d : dividasDoMes) {
            if (d.isExcluida()) continue;
            total += d.getValorTotal();
            pago += d.getValorPago();
        }

        double faltaPagar = total - pago;

        tvTotalMes.setText(String.format(LOCALE_BR, "R$ %.2f", total));
        tvJaPagoMes.setText(String.format(LOCALE_BR, "R$ %.2f", pago));
        tvFaltaPagarMes.setText(String.format(LOCALE_BR, "R$ %.2f", faltaPagar));
    }

    private void montarFiltros() {
        containerFiltrosHistorico.removeAllViews();

        Map<String, Integer> categoriasCount = new HashMap<>();
        for (Divida d : dividasDoMes) {
            int count = categoriasCount.getOrDefault(d.getCategoria(), 0);
            categoriasCount.put(d.getCategoria(), count + 1);
        }

        criarChip("Todos", dividasDoMes.size(), "Todos".equals(categoriaSelecionada));
        for (Map.Entry<String, Integer> entry : categoriasCount.entrySet()) {
            criarChip(entry.getKey(), entry.getValue(), entry.getKey().equals(categoriaSelecionada));
        }
    }

    private void criarChip(String categoria, int count, boolean selecionado) {
        TextView chip = new TextView(this);
        String texto = categoria.equals("Todos")
                ? "Todos"
                : categoria + (count > 1 ? " (" + count + ")" : "");
        chip.setText(texto);
        chip.setTextSize(13f);
        chip.setPadding(40, 20, 40, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 12, 0);
        chip.setLayoutParams(params);

        aplicarEstiloChip(chip, selecionado);

        chip.setOnClickListener(v -> {
            categoriaSelecionada = categoria;
            atualizarTela();
        });

        containerFiltrosHistorico.addView(chip);
    }

    private void aplicarEstiloChip(TextView chip, boolean selecionado) {
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(50f);

        if (selecionado) {
            d.setColor(Color.parseColor("#4C1D95"));
            d.setStroke(1, Color.parseColor("#A855F7"));
            chip.setTextColor(Color.WHITE);
        } else {
            d.setColor(Color.parseColor("#131C2E"));
            d.setStroke(1, Color.parseColor("#334155"));
            chip.setTextColor(Color.parseColor("#CBD5E1"));
        }
        chip.setBackground(d);
    }

    private void atualizarLista() {
        dividasFiltradas.clear();
        for (Divida d : dividasDoMes) {
            if (categoriaSelecionada.equals("Todos") || d.getCategoria().equals(categoriaSelecionada)) {
                dividasFiltradas.add(d);
            }
        }

        String nomeMes = tvMesAtual.getText().toString().toUpperCase();
        String titulo = categoriaSelecionada.equals("Todos")
                ? "LANÇAMENTOS DE " + nomeMes
                : "LANÇAMENTOS DE " + nomeMes + ": " + categoriaSelecionada.toUpperCase();
        tvTituloListaHistorico.setText(titulo);

        adapter = new HistoricoAdapter(dividasFiltradas);
        rvLancamentosHistorico.setAdapter(adapter);
    }

    // ==========================================
    // ⭐ EXPORTAÇÃO (CSV e PDF)
    // ==========================================

    /**
     * Mostra diálogo perguntando se quer exportar em CSV ou PDF.
     */
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

    /**
     * Exporta o resumo do mês em CSV.
     */
    private void exportarCsv() {
        List<Divida> paraExportar = new ArrayList<>();
        for (Divida d : dividasDoMes) {
            if (categoriaSelecionada.equals("Todos") || d.getCategoria().equals(categoriaSelecionada)) {
                paraExportar.add(d);
            }
        }

        if (paraExportar.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar neste mês.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String mesAno = tvMesAtual.getText().toString();
        String sufixo = categoriaSelecionada.equals("Todos")
                ? ""
                : "_" + categoriaSelecionada.toLowerCase().replace(" ", "_");

        File arquivo = CsvExportHelper.gerarCsv(this, paraExportar, mesAno + sufixo);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o arquivo CSV.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "CSV gerado com sucesso!", Toast.LENGTH_SHORT).show();
        CsvExportHelper.compartilharCsv(this, arquivo);
    }

    /**
     * Exporta o resumo do mês em PDF.
     */
    private void exportarPdf() {
        List<Divida> paraExportar = new ArrayList<>();
        for (Divida d : dividasDoMes) {
            if (categoriaSelecionada.equals("Todos") || d.getCategoria().equals(categoriaSelecionada)) {
                paraExportar.add(d);
            }
        }

        if (paraExportar.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar neste mês.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String mesAno = tvMesAtual.getText().toString();
        File arquivo = PdfExportHelper.gerarPdf(this, paraExportar, mesAno, categoriaSelecionada);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o PDF.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show();
        PdfExportHelper.compartilharPdf(this, arquivo);
    }
}