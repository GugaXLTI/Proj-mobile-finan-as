package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.example.controle_gastos.model.DividaAgrupada;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DividasActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private RecyclerView rvDividas;
    private TextView tvTotalAPagar, tvTotalPago, tvTotalGeral, tvEmptyDividas;
    private TextView btnCadastrarDivida;
    private DividaAdapter adapter;

    private List<DividaAgrupada> listaAgrupada = new ArrayList<>();

    private ImageView tabInicio, tabLancar, tabRelatorios, tabConfig;
    private LinearLayout tabDividas;

    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        NavScrollHelper.attach(this);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        rvDividas = findViewById(R.id.rvDividas);
        tvTotalAPagar = findViewById(R.id.tvTotalAPagar);
        tvTotalPago = findViewById(R.id.tvTotalPago);
        tvTotalGeral = findViewById(R.id.tvTotalGeral);
        tvEmptyDividas = findViewById(R.id.tvEmptyDividas);
        btnCadastrarDivida = findViewById(R.id.btnCadastrarDivida);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvDividas.setLayoutManager(new LinearLayoutManager(this));

        carregarDividas();

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

        tabConfig.setOnClickListener(v ->
                startActivity(new Intent(DividasActivity.this, ConfiguracoesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDividas();
    }

    private void carregarDividas() {
        List<Divida> todas = db.dividaDao().listarPorUsuario(session.getUserId());

        // ⭐ LÓGICA DE AGRUPAMENTO
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

        listaAgrupada.clear();

        // Adiciona os grupos
        for (List<Divida> grupo : grupos.values()) {
            // Ordena as parcelas do grupo pela data para garantir consistência
            grupo.sort((a, b) -> compararDatas(a.getVencimento(), b.getVencimento()));
            listaAgrupada.add(new DividaAgrupada(grupo));
        }

        // Adiciona as avulsas
        for (Divida avulsa : avulsas) {
            listaAgrupada.add(new DividaAgrupada(Collections.singletonList(avulsa)));
        }

        // Ordena a lista final: Não pagas primeiro (por data), pagas por último
        listaAgrupada.sort(this::compararAgrupadas);

        adapter = new DividaAdapter(listaAgrupada, this);
        rvDividas.setAdapter(adapter);

        boolean vazio = listaAgrupada.isEmpty();
        tvEmptyDividas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvDividas.setVisibility(vazio ? View.GONE : View.VISIBLE);

        atualizarTotais();
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

        for (DividaAgrupada a : listaAgrupada) {
            totalGeral += a.getValorTotalGrupo();
            totalPago += (a.getValorTotalGrupo() - a.getValorRestanteGrupo());
            totalAPagar += a.getValorRestanteGrupo();
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_BR);
        tvTotalAPagar.setText(nf.format(totalAPagar));
        tvTotalPago.setText(nf.format(totalPago));
        tvTotalGeral.setText(nf.format(totalGeral));
    }

    @Override
    public void onExcluirClick(DividaAgrupada agrupada) {
        new AlertDialog.Builder(this)
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
        // Abre a edição da primeira parcela do grupo
        Intent intent = new Intent(DividasActivity.this, CadastroDividaActivity.class);
        intent.putExtra("divida_id", agrupada.getPrimeiroId());
        startActivity(intent);
    }

    @Override
    public void onPagarClick(DividaAgrupada agrupada) {
        Divida proxima = agrupada.getProximaParcela();

        if (proxima.isPago()) {
            Toast.makeText(this, "Todas as parcelas já foram pagas!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlarmeHelper.cancelar(this, proxima);

        proxima.valorPago = proxima.getValorTotal();
        proxima.setPago(true);

        db.dividaDao().atualizar(proxima);
        carregarDividas();

        Toast.makeText(this,
                "Parcela paga: " + proxima.getTitulo() + " (" + proxima.getParcela() + ")",
                Toast.LENGTH_SHORT).show();
    }
}