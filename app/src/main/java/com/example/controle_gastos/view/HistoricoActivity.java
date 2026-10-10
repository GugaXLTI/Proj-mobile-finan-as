package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoricoActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private TextView btnVoltarHistorico, btnMesAnterior, btnMesProximo;
    private TextView tvMesAtual, tvBadgeAtual;
    private TextView tvTotalMes, tvJaPagoMes, tvFaltaPagarMes;
    private TextView tvLabelTotal, tvLabelPago, tvLabelFalta;
    private TextView tvTituloListaHistorico;
    private TextView btnFiltroPeriodo, btnLimparFiltro;
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

    // ⭐ Filtro de período
    private String dataInicialFiltro = null; // "dd/MM/yyyy"
    private String dataFinalFiltro = null;
    private boolean filtroAtivo = false;

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
        tvLabelTotal = findViewById(R.id.tvLabelTotal);
        tvLabelPago = findViewById(R.id.tvLabelPago);
        tvLabelFalta = findViewById(R.id.tvLabelFalta);
        tvTituloListaHistorico = findViewById(R.id.tvTituloListaHistorico);
        btnFiltroPeriodo = findViewById(R.id.btnFiltroPeriodo);
        btnLimparFiltro = findViewById(R.id.btnLimparFiltro);
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
            // Se estiver filtrando, volta para o modo mês
            if (filtroAtivo) limparFiltroPeriodo();

            mesSelecionado--;
            if (mesSelecionado < 1) {
                mesSelecionado = 12;
                anoSelecionado--;
            }
            categoriaSelecionada = "Todos";
            atualizarTela();
        });

        btnMesProximo.setOnClickListener(v -> {
            if (filtroAtivo) limparFiltroPeriodo();

            mesSelecionado++;
            if (mesSelecionado > 12) {
                mesSelecionado = 1;
                anoSelecionado++;
            }
            categoriaSelecionada = "Todos";
            atualizarTela();
        });

        // ⭐ Filtro de período
        btnFiltroPeriodo.setOnClickListener(v -> abrirDialogoPeriodo());
        btnLimparFiltro.setOnClickListener(v -> {
            limparFiltroPeriodo();
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
        if (filtroAtivo) {
            // Modo filtro: mostra o intervalo de datas
            tvMesAtual.setText(dataInicialFiltro + " a " + dataFinalFiltro);
            tvBadgeAtual.setVisibility(View.GONE);
            btnFiltroPeriodo.setVisibility(View.GONE);
            btnLimparFiltro.setVisibility(View.VISIBLE);
            return;
        }

        // Modo mês
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

        btnFiltroPeriodo.setVisibility(View.VISIBLE);
        btnLimparFiltro.setVisibility(View.GONE);
    }

    private void filtrarDividasDoMes() {
        dividasDoMes.clear();

        if (filtroAtivo) {
            // ⭐ Modo filtro: aplica filtro de período
            for (Divida d : todasDividas) {
                if (estaEntreDatas(d.getVencimento(), dataInicialFiltro, dataFinalFiltro)) {
                    dividasDoMes.add(d);
                }
            }
            return;
        }

        // Modo mês: filtra pelo mês/ano selecionado
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

    /**
     * ⭐ Verifica se a data de vencimento está entre as duas datas do filtro.
     */
    private boolean estaEntreDatas(String dataVenc, String dataIni, String dataFim) {
        if (dataVenc == null || dataIni == null || dataFim == null) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date venc = sdf.parse(dataVenc);
            Date ini = sdf.parse(dataIni);
            Date fim = sdf.parse(dataFim);
            if (venc == null || ini == null || fim == null) return false;

            // Considera o fim do dia do filtro (23h59)
            Calendar fimCal = Calendar.getInstance();
            fimCal.setTime(fim);
            fimCal.set(Calendar.HOUR_OF_DAY, 23);
            fimCal.set(Calendar.MINUTE, 59);
            fimCal.set(Calendar.SECOND, 59);

            return !venc.before(ini) && !venc.after(fimCal.getTime());
        } catch (Exception e) {
            return false;
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

        int percPago = total > 0 ? (int) Math.round((pago / total) * 100) : 0;
        int percFalta = 100 - percPago;

        tvTotalMes.setText(String.format(LOCALE_BR, "R$ %.2f", total));
        tvJaPagoMes.setText(String.format(LOCALE_BR, "R$ %.2f", pago));
        tvFaltaPagarMes.setText(String.format(LOCALE_BR, "R$ %.2f", faltaPagar));

        tvLabelTotal.setText("100% faturas");
        tvLabelPago.setText(percPago + "% liquidado");
        tvLabelFalta.setText(percFalta + "% em aberto");
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

        String nomePeriodo;
        if (filtroAtivo) {
            nomePeriodo = dataInicialFiltro + " A " + dataFinalFiltro;
        } else {
            nomePeriodo = tvMesAtual.getText().toString().toUpperCase();
        }

        String titulo = categoriaSelecionada.equals("Todos")
                ? "LANÇAMENTOS DE " + nomePeriodo
                : "LANÇAMENTOS DE " + nomePeriodo + ": " + categoriaSelecionada.toUpperCase();
        tvTituloListaHistorico.setText(titulo);

        adapter = new HistoricoAdapter(dividasFiltradas);
        rvLancamentosHistorico.setAdapter(adapter);
    }

    // ==========================================
    // ⭐ FILTRO DE PERÍODO
    // ==========================================

    /**
     * Abre um diálogo com 2 DatePickers em sequência:
     * 1. Data inicial
     * 2. Data final
     */
    private void abrirDialogoPeriodo() {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialogInicial = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dataInicial = String.format(LOCALE_BR, "%02d/%02d/%04d",
                            dayOfMonth, month + 1, year);

                    // Após escolher a inicial, abre o picker da final
                    abrirPickerDataFinal(dataInicial);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        dialogInicial.setTitle("Data inicial");
        dialogInicial.show();
    }

    /**
     * Abre o segundo DatePicker (data final).
     */
    private void abrirPickerDataFinal(String dataInicial) {
        Calendar c = Calendar.getInstance();

        DatePickerDialog dialogFinal = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String dataFinal = String.format(LOCALE_BR, "%02d/%02d/%04d",
                            dayOfMonth, month + 1, year);

                    // Valida: data final deve ser >= inicial
                    if (compararDatas(dataFinal, dataInicial) < 0) {
                        Toast.makeText(this,
                                "A data final deve ser depois da inicial!",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    aplicarFiltroPeriodo(dataInicial, dataFinal);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        dialogFinal.setTitle("Data final (a partir de " + dataInicial + ")");
        dialogFinal.show();
    }

    /**
     * Aplica o filtro e atualiza a tela.
     */
    private void aplicarFiltroPeriodo(String dataInicial, String dataFinal) {
        dataInicialFiltro = dataInicial;
        dataFinalFiltro = dataFinal;
        filtroAtivo = true;
        categoriaSelecionada = "Todos";

        atualizarTela();

        Toast.makeText(this,
                "Filtro aplicado: " + dataInicial + " a " + dataFinal,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Remove o filtro e volta ao modo mês.
     */
    private void limparFiltroPeriodo() {
        dataInicialFiltro = null;
        dataFinalFiltro = null;
        filtroAtivo = false;
    }

    /**
     * Compara duas datas no formato "dd/MM/yyyy".
     * Retorna negativo se d1 < d2, 0 se iguais, positivo se d1 > d2.
     */
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

    // ==========================================
    // ⭐ EXPORTAÇÃO (CSV e PDF)
    // ==========================================

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
        List<Divida> paraExportar = new ArrayList<>();
        for (Divida d : dividasDoMes) {
            if (categoriaSelecionada.equals("Todos") || d.getCategoria().equals(categoriaSelecionada)) {
                paraExportar.add(d);
            }
        }

        if (paraExportar.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String nomePeriodo;
        if (filtroAtivo) {
            nomePeriodo = dataInicialFiltro.replace("/", "-") + "_a_" + dataFinalFiltro.replace("/", "-");
        } else {
            nomePeriodo = tvMesAtual.getText().toString();
        }

        String sufixo = categoriaSelecionada.equals("Todos")
                ? ""
                : "_" + categoriaSelecionada.toLowerCase().replace(" ", "_");

        File arquivo = CsvExportHelper.gerarCsv(this, paraExportar, nomePeriodo + sufixo);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o arquivo CSV.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "CSV gerado com sucesso!", Toast.LENGTH_SHORT).show();
        CsvExportHelper.compartilharCsv(this, arquivo);
    }

    private void exportarPdf() {
        List<Divida> paraExportar = new ArrayList<>();
        for (Divida d : dividasDoMes) {
            if (categoriaSelecionada.equals("Todos") || d.getCategoria().equals(categoriaSelecionada)) {
                paraExportar.add(d);
            }
        }

        if (paraExportar.isEmpty()) {
            Toast.makeText(this, "Nenhum lançamento para exportar.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String nomePeriodo;
        if (filtroAtivo) {
            nomePeriodo = dataInicialFiltro + " a " + dataFinalFiltro;
        } else {
            nomePeriodo = tvMesAtual.getText().toString();
        }

        File arquivo = PdfExportHelper.gerarPdf(this, paraExportar, nomePeriodo, categoriaSelecionada);

        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o PDF.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "PDF gerado com sucesso!", Toast.LENGTH_SHORT).show();
        PdfExportHelper.compartilharPdf(this, arquivo);
    }
}