package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.DividaAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;

import java.util.List;
import java.util.Locale;

public class DividasActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private RecyclerView rvDividas;
    private TextView tvTotalAPagar, tvTotalPago, tvTotalGeral;
    private Button btnCadastrarDivida;
    private DividaAdapter adapter;
    private List<Divida> dividas;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        db = AppDatabase.getInstance(this);

        rvDividas = findViewById(R.id.rvDividas);
        tvTotalAPagar = findViewById(R.id.tvTotalAPagar);
        tvTotalPago = findViewById(R.id.tvTotalPago);
        tvTotalGeral = findViewById(R.id.tvTotalGeral);
        btnCadastrarDivida = findViewById(R.id.btnCadastrarDivida);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvDividas.setLayoutManager(new LinearLayoutManager(this));

        carregarDividas();

        btnCadastrarDivida.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, CadastroDividaActivity.class);
            startActivity(intent);
        });

        tabInicio.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        });

        tabLancar.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, CadastroDividaActivity.class);
            startActivity(intent);
        });

        tabDividas.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Dívidas", Toast.LENGTH_SHORT).show());

        tabRelatorios.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        tabConfig.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, ConfiguracoesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarDividas();
    }

    private void carregarDividas() {
        dividas = db.dividaDao().listarTodas();
        adapter = new DividaAdapter(dividas, this);
        rvDividas.setAdapter(adapter);
        atualizarTotais();
    }

    private void atualizarTotais() {
        double totalAPagar = 0;
        double totalPago = 0;
        double totalGeral = 0;

        for (Divida d : dividas) {
            totalGeral += d.getValorTotal();
            if (d.isPago()) {
                totalPago += d.getValorTotal();
            } else {
                totalAPagar += d.getValorRestante();
            }
        }

        tvTotalAPagar.setText(String.format(Locale.getDefault(), "R$ %.2f", totalAPagar));
        tvTotalPago.setText(String.format(Locale.getDefault(), "R$ %.2f", totalPago));
        tvTotalGeral.setText(String.format(Locale.getDefault(), "R$ %.2f", totalGeral));
    }

    // ========== Ações dos botões dos cards ==========

    @Override
    public void onExcluirClick(int position) {
        Divida d = dividas.get(position);
        db.dividaDao().deletar(d);
        carregarDividas();
        Toast.makeText(this, "Dívida excluída: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditarClick(int position) {
        Divida d = dividas.get(position);
        Toast.makeText(this, "Editar: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPagarClick(int position) {
        Divida d = dividas.get(position);
        d.setPago(true);
        db.dividaDao().atualizar(d);
        carregarDividas();
        Toast.makeText(this, "Pagamento registrado: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}