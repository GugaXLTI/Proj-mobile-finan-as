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
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.SessionManager;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DividasActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private RecyclerView rvDividas;
    private TextView tvTotalAPagar, tvTotalPago, tvTotalGeral, tvEmptyDividas;
    private TextView btnCadastrarDivida;
    private DividaAdapter adapter;
    private List<Divida> dividas = new ArrayList<>();

    // Navegação (aba ativa = LinearLayout, demais = ImageView)
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
        dividas = new ArrayList<>(db.dividaDao().listarPorUsuario(session.getUserId()));

        // Não pagas primeiro (por data de vencimento), pagas por último
        dividas.sort(this::compararDividas);

        adapter = new DividaAdapter(dividas, this);
        rvDividas.setAdapter(adapter);

        boolean vazio = dividas.isEmpty();
        tvEmptyDividas.setVisibility(vazio ? View.VISIBLE : View.GONE);
        rvDividas.setVisibility(vazio ? View.GONE : View.VISIBLE);

        atualizarTotais();
    }

    private int compararDividas(Divida a, Divida b) {
        if (a.isPago() != b.isPago()) return a.isPago() ? 1 : -1;
        return compararDatas(a.getVencimento(), b.getVencimento());
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

        for (Divida d : dividas) {
            totalGeral += d.getValorTotal();
            totalPago += d.getValorPago();
            totalAPagar += d.getValorRestante();
        }

        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_BR);
        tvTotalAPagar.setText(nf.format(totalAPagar));
        tvTotalPago.setText(nf.format(totalPago));
        tvTotalGeral.setText(nf.format(totalGeral));
    }

    @Override
    public void onExcluirClick(Divida divida) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir dívida")
                .setMessage("Tem certeza que deseja excluir \"" + divida.getTitulo() + "\"?\n\n" +
                        "A dívida ficará registrada no Histórico como excluída.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    AlarmeHelper.cancelar(this, divida);

                    // SOFT DELETE: marca como excluída
                    divida.setExcluida(true);
                    db.dividaDao().atualizar(divida);

                    carregarDividas();
                    Toast.makeText(this, "Dívida excluída: " + divida.getTitulo(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onEditarClick(Divida divida) {
        Intent intent = new Intent(DividasActivity.this, CadastroDividaActivity.class);
        intent.putExtra("divida_id", divida.getId());
        startActivity(intent);
    }

    /**
     * Pagar = quitar aquele registro (que já é UMA parcela).
     */
    @Override
    public void onPagarClick(Divida divida) {
        if (divida.isPago()) {
            Toast.makeText(this, "Essa parcela já está paga!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlarmeHelper.cancelar(this, divida);

        divida.valorPago = divida.getValorTotal();
        divida.setPago(true);

        db.dividaDao().atualizar(divida);
        carregarDividas();

        Toast.makeText(this,
                "Parcela paga: " + divida.getTitulo() + " (" + divida.getParcela() + ")",
                Toast.LENGTH_SHORT).show();
    }
}