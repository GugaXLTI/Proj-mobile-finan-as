package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import com.example.controle_gastos.utils.SessionManager;

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
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

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
        dividas = db.dividaDao().listarPorUsuario(session.getUserId());
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
            totalPago += d.getValorPago();
            totalAPagar += d.getValorRestante();
        }

        tvTotalAPagar.setText(String.format(Locale.getDefault(), "R$ %.2f", totalAPagar));
        tvTotalPago.setText(String.format(Locale.getDefault(), "R$ %.2f", totalPago));
        tvTotalGeral.setText(String.format(Locale.getDefault(), "R$ %.2f", totalGeral));
    }

    @Override
    public void onExcluirClick(Divida divida) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir dívida")
                .setMessage("Tem certeza que deseja excluir \"" + divida.getTitulo() + "\"?\n\n" +
                        "A dívida ficará registrada no Histórico como excluída.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    // Cancela o alarme
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
     * ⭐ Pagar = quitar aquele registro (que já é UMA parcela).
     * Não divide mais nada, porque cada registro tem seu próprio valorTotal = valorParcela.
     */
    @Override
    public void onPagarClick(Divida divida) {
        if (divida.isPago()) {
            Toast.makeText(this, "Essa parcela já está paga!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cancela o alarme de notificação (não precisa mais avisar)
        AlarmeHelper.cancelar(this, divida);

        // Marca como pago
        divida.valorPago = divida.getValorTotal();
        divida.setPago(true);

        db.dividaDao().atualizar(divida);
        carregarDividas();

        Toast.makeText(this,
                "Parcela paga: " + divida.getTitulo() + " (" + divida.getParcela() + ")",
                Toast.LENGTH_SHORT).show();
    }
}