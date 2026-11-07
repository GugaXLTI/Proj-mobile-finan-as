package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.DividaAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.DividaAgrupada;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DividasActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private RecyclerView rvDividas;
    private TextView tvTotalAPagar, tvTotalPago, tvTotalGeral, tvEmptyDividas;
    private TextView btnCadastrarDivida;
    private DividaAdapter adapter;

    private ChipGroup chipGroupFiltros;
    private TextView btnFiltroCategoria;
    private String filtroAtual = "Todas";
    private String categoriaFiltro = "Todas";
    private List<DividaAgrupada> listaAgrupadaCompleta = new ArrayList<>();
    private List<DividaAgrupada> listaAgrupada = new ArrayList<>();

    private ImageView tabInicio, tabLancar, tabRelatorios, tabConfig;
    private LinearLayout tabDividas;

    private AppDatabase db;
    private SessionManager session;

    private final String[] CORES = {
            "#10B981", "#34D399", "#3B82F6", "#06B6D4",
            "#F59E0B", "#FCD34D", "#EF4444", "#EC4899",
            "#8B5CF6", "#A855F7", "#64748B", "#94A3B8"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(DividasActivity.this, InicioActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        NavScrollHelper.attach(this);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        rvDividas = findViewById(R.id.rvDividas);
        tvTotalAPagar = findViewById(R.id.tvTotalAPagar);
        tvTotalPago = findViewById(R.id.tvTotalPago);
        tvTotalGeral = findViewById(R.id.tvTotalGeral);
        tvEmptyDividas = findViewById(R.id.tvEmptyDividas);
        btnCadastrarDivida = findViewById(R.id.btnCadastrarDivida);
        chipGroupFiltros = findViewById(R.id.chipGroupFiltros);
        btnFiltroCategoria = findViewById(R.id.btnFiltroCategoria);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvDividas.setLayoutManager(new LinearLayoutManager(this));

        configurarFiltros();
        carregarDividas();

        btnFiltroCategoria.setOnClickListener(v -> abrirBottomSheetCategorias());

        btnCadastrarDivida.setOnClickListener(v ->
                startActivity(new Intent(DividasActivity.this, CadastroDividaActivity.class)));

        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(DividasActivity.this, InicioActivity.class));
            finish();
        });

        tabLancar.setOnClickListener(v ->
                startActivity(new Intent(DividasActivity.this, CadastroDividaActivity.class)));

        tabDividas.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Dívidas", Toast.LENGTH_SHORT).show());

        tabRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(DividasActivity.this, DashboardActivity.class));
            finish();
        });

        tabConfig.setOnClickListener(v -> {
            startActivity(new Intent(DividasActivity.this, ConfiguracoesActivity.class));
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDividas();
    }

    private void configurarFiltros() {
        chipGroupFiltros.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipTodas) filtroAtual = "Todas";
            else if (id == R.id.chipFixas) filtroAtual = "Fixas";
            else if (id == R.id.chipNormais) filtroAtual = "Normais";
            aplicarFiltro();
        });
    }

    private void abrirBottomSheetCategorias() {
        List<Categoria> categoriasBanco = db.categoriaDao().listarPorUsuario(session.getUserId());
        Map<String, String> mapaCores = new HashMap<>();
        for (Categoria c : categoriasBanco) {
            if (c.getNome() != null && c.getCor() != null) {
                mapaCores.put(c.getNome().trim(), c.getCor());
            }
        }

        Set<String> categoriasSet = new LinkedHashSet<>();
        categoriasSet.add("Todas");

        for (Categoria c : categoriasBanco) {
            if (c.getNome() != null && !c.getNome().trim().isEmpty()) {
                categoriasSet.add(c.getNome().trim());
            }
        }
        for (DividaAgrupada a : listaAgrupadaCompleta) {
            Divida p = a.getProximaParcela();
            if (p != null && p.getCategoria() != null && !p.getCategoria().trim().isEmpty()) {
                categoriasSet.add(p.getCategoria().trim());
            }
        }

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_categorias, null);
        bottomSheetDialog.setContentView(view);

        bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog.getBehavior().setSkipCollapsed(true);

        LinearLayout llCategorias = view.findViewById(R.id.llCategoriasBottomSheet);
        TextView btnAdicionarCategoriaSheet = view.findViewById(R.id.btnAdicionarCategoriaSheet);

        for (String catNome : categoriasSet) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(24), dp(16), dp(24), dp(16));
            row.setClickable(true);
            row.setFocusable(true);

            android.util.TypedValue outValue = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            row.setBackgroundResource(outValue.resourceId);

            View bolinha = new View(this);
            LinearLayout.LayoutParams paramsBolinha = new LinearLayout.LayoutParams(dp(16), dp(16));
            paramsBolinha.setMargins(0, 0, dp(12), 0);
            bolinha.setLayoutParams(paramsBolinha);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);

            if (catNome.equals("Todas")) {
                drawable.setColor(Color.parseColor("#64748B"));
            } else {
                String corHex = mapaCores.get(catNome);
                if (corHex != null) {
                    try {
                        drawable.setColor(Color.parseColor(corHex));
                    } catch (Exception e) {
                        drawable.setColor(Color.parseColor("#64748B"));
                    }
                } else {
                    drawable.setColor(Color.parseColor("#64748B"));
                }
            }
            bolinha.setBackground(drawable);

            TextView tvCategoria = new TextView(this);
            tvCategoria.setText(catNome);
            tvCategoria.setTextSize(16f);
            tvCategoria.setTextColor(Color.parseColor("#E2E8F0"));

            if (catNome.equals(categoriaFiltro)) {
                tvCategoria.setTextColor(Color.parseColor("#34D399"));
                tvCategoria.setText("✓  " + catNome);
                tvCategoria.setTypeface(null, android.graphics.Typeface.BOLD);
            }

            row.addView(bolinha);
            row.addView(tvCategoria);

            row.setOnClickListener(v -> {
                categoriaFiltro = catNome;
                btnFiltroCategoria.setText(categoriaFiltro.equals("Todas") ? "Todas as categorias" : categoriaFiltro);
                aplicarFiltro();
                bottomSheetDialog.dismiss();
            });

            llCategorias.addView(row);

            View divider = new View(this);
            LinearLayout.LayoutParams paramsDivider = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1);
            paramsDivider.setMargins(dp(24), 0, dp(24), 0);
            divider.setLayoutParams(paramsDivider);
            divider.setBackgroundColor(Color.parseColor("#1E2A44"));
            llCategorias.addView(divider);
        }

        btnAdicionarCategoriaSheet.setOnClickListener(v -> {
            mostrarDialogoNovaCategoria(bottomSheetDialog);
        });

        bottomSheetDialog.show();
    }

    private void mostrarDialogoNovaCategoria(BottomSheetDialog sheetDialog) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_nova_categoria, null);
        EditText editNome = dialogView.findViewById(R.id.editNomeCategoria);
        GridLayout llPaletaCores = dialogView.findViewById(R.id.llPaletaCores);

        final String[] corSelecionada = {CORES[0]};

        llPaletaCores.removeAllViews();

        for (String corHex : CORES) {
            View bolinha = new View(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(36);
            params.height = dp(36);
            params.setMargins(dp(6), dp(6), dp(6), dp(6));
            bolinha.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(corHex));

            if (corHex.equals(corSelecionada[0])) {
                drawable.setStroke(dp(3), Color.WHITE);
            }

            bolinha.setBackground(drawable);
            bolinha.setClickable(true);
            bolinha.setFocusable(true);

            bolinha.setOnClickListener(v -> {
                corSelecionada[0] = corHex;
                for (int i = 0; i < llPaletaCores.getChildCount(); i++) {
                    View v2 = llPaletaCores.getChildAt(i);
                    GradientDrawable d2 = new GradientDrawable();
                    d2.setShape(GradientDrawable.OVAL);
                    d2.setColor(Color.parseColor(CORES[i]));
                    if (CORES[i].equals(corSelecionada[0])) {
                        d2.setStroke(dp(3), Color.WHITE);
                    }
                    v2.setBackground(d2);
                }
            });

            llPaletaCores.addView(bolinha);
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Nova Categoria")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = editNome.getText().toString().trim();
                    if (!nome.isEmpty()) {
                        Categoria novaCat = new Categoria(session.getUserId(), nome, corSelecionada[0]);
                        long id = db.categoriaDao().inserir(novaCat);

                        if (id > 0) {
                            categoriaFiltro = nome;
                            btnFiltroCategoria.setText(categoriaFiltro);
                            aplicarFiltro();

                            sheetDialog.dismiss();
                            Toast.makeText(DividasActivity.this, "Categoria \"" + nome + "\" criada!", Toast.LENGTH_SHORT).show();

                            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                            if (imm != null) imm.hideSoftInputFromWindow(editNome.getWindowToken(), 0);
                        } else {
                            Toast.makeText(DividasActivity.this, "Erro ao criar a categoria.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(DividasActivity.this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }

    private void carregarDividas() {
        List<Divida> todas = db.dividaDao().listarPorUsuario(session.getUserId());

        Map<Integer, List<Divida>> grupos = new HashMap<>();
        List<Divida> avulsas = new ArrayList<>();

        for (Divida d : todas) {
            if (d.getGrupoId() > 0) {
                if (!grupos.containsKey(d.getGrupoId())) {
                    grupos.put(d.getGrupoId(), new ArrayList<>());
                }
                grupos.get(d.getGrupoId()).add(d);
            } else {
                avulsas.add(d);
            }
        }

        listaAgrupadaCompleta.clear();

        for (List<Divida> grupo : grupos.values()) {
            grupo.sort((a, b) -> compararDatas(a.getVencimento(), b.getVencimento()));
            listaAgrupadaCompleta.add(new DividaAgrupada(grupo));
        }

        for (Divida avulsa : avulsas) {
            listaAgrupadaCompleta.add(new DividaAgrupada(Collections.singletonList(avulsa)));
        }

        listaAgrupadaCompleta.sort(this::compararAgrupadas);

        aplicarFiltro();
    }

    private void aplicarFiltro() {
        listaAgrupada.clear();

        for (DividaAgrupada agrupada : listaAgrupadaCompleta) {
            Divida proxima = agrupada.getProximaParcela();
            if (proxima == null) continue;

            if (!isRelevanteParaMesAtual(agrupada)) {
                continue;
            }

            String cat = proxima.getCategoria() != null ? proxima.getCategoria().trim() : "";
            String banco = proxima.getBanco() != null ? proxima.getBanco().toLowerCase() : "";
            boolean isContaFixa = cat.toLowerCase().contains("fixa") || banco.contains("fixa");

            boolean passaFiltroTipo = false;
            if (filtroAtual.equals("Todas")) passaFiltroTipo = true;
            else if (filtroAtual.equals("Fixas") && isContaFixa) passaFiltroTipo = true;
            else if (filtroAtual.equals("Normais") && !isContaFixa) passaFiltroTipo = true;

            boolean passaFiltroCategoria = categoriaFiltro.equals("Todas") || cat.equalsIgnoreCase(categoriaFiltro);

            if (passaFiltroTipo && passaFiltroCategoria) {
                listaAgrupada.add(agrupada);
            }
        }

        if (adapter == null) {
            adapter = new DividaAdapter(listaAgrupada, this);
            rvDividas.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        boolean vazio = listaAgrupada.isEmpty();
        tvEmptyDividas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvDividas.setVisibility(vazio ? View.GONE : View.VISIBLE);

        if (vazio && (!filtroAtual.equals("Todas") || !categoriaFiltro.equals("Todas"))) {
            tvEmptyDividas.setText("Nenhuma conta encontrada com esses filtros.");
        } else {
            tvEmptyDividas.setText("Você ainda não tem dívidas cadastradas.");
        }

        atualizarTotais();
    }

    private boolean isRelevanteParaMesAtual(DividaAgrupada agrupada) {
        Calendar hoje = Calendar.getInstance();
        int mesAtual = hoje.get(Calendar.MONTH) + 1;
        int anoAtual = hoje.get(Calendar.YEAR);

        for (Divida d : agrupada.parcelas) {
            if (ehMesmoMesOuAtrasado(d.getVencimento(), mesAtual, anoAtual)) {
                return true;
            }
        }
        return false;
    }

    private int compararAgrupadas(DividaAgrupada a, DividaAgrupada b) {
        boolean pagoA = a.isTotalmentePago();
        boolean pagoB = b.isTotalmentePago();
        if (pagoA != pagoB) return pagoA ? 1 : -1;

        return compararDatas(a.getProximaParcela().getVencimento(), b.getProximaParcela().getVencimento());
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

    private void atualizarTotais() {
        double totalAPagar = 0;
        double totalPago = 0;
        double totalGeral = 0;

        // Soma os totais baseados na lista filtrada (que é o que o usuário vê)
        for (DividaAgrupada a : listaAgrupada) {
            Divida p = a.getParcelaParaLista(); // Pega a parcela que está visível na tela
            if (p != null) {
                totalGeral += p.getValorTotal(); // O Total Geral agora bate com o que está na tela

                if (p.isPago()) {
                    totalPago += p.getValorTotal();
                } else {
                    totalAPagar += p.getValorRestante();
                }
            }
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_BR);
        tvTotalAPagar.setText(nf.format(totalAPagar));
        tvTotalPago.setText(nf.format(totalPago));
        tvTotalGeral.setText(nf.format(totalGeral));
    }

    private boolean ehMesmoMesOuAtrasado(String vencimento, int mesAtual, int anoAtual) {
        if (vencimento == null || vencimento.isEmpty()) return false;
        try {
            String[] partes = vencimento.split("/");
            if (partes.length != 3) return false;
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);
            return (ano < anoAtual) || (ano == anoAtual && mes <= mesAtual);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onExcluirClick(DividaAgrupada agrupada) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Excluir dívida")
                .setMessage("Tem certeza que deseja excluir \"" + agrupada.getTitulo() + "\"?\n\n" +
                        "Todas as parcelas deste grupo serão canceladas e ficarão registradas no Histórico.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    for (Divida d : agrupada.parcelas) {
                        AlarmeHelper.cancelar(this, d);
                        d.setExcluida(true);
                        db.dividaDao().atualizar(d);
                    }
                    carregarDividas();
                    Toast.makeText(this, "Dívida excluída: " + agrupada.getTitulo(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditarClick(DividaAgrupada agrupada) {
        Intent intent = new Intent(DividasActivity.this, CadastroDividaActivity.class);
        intent.putExtra("divida_id", agrupada.getPrimeiroId());
        startActivity(intent);
    }

    @Override
    public void onPagarClick(DividaAgrupada agrupada) {
        Divida proxima = agrupada.getParcelaParaLista(); // Usa a parcela visível

        if (proxima == null || proxima.isPago()) {
            Toast.makeText(this, "Essa parcela já foi paga!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlarmeHelper.cancelar(this, proxima);

        proxima.valorPago = proxima.getValorTotal();
        proxima.setPago(true);

        // ⭐ Grava a data de hoje no momento do pagamento
        String hoje = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).format(new Date());
        proxima.setDataPagamento(hoje);

        db.dividaDao().atualizar(proxima);
        carregarDividas();

        Toast.makeText(this, "Parcela paga: " + proxima.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}