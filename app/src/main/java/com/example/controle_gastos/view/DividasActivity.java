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
            totalPago += d.getValorPago();          // ⭐ Soma só o que foi realmente pago
            totalAPagar += d.getValorRestante();    // ⭐ Soma o que ainda falta
        }

        tvTotalAPagar.setText(String.format(Locale.getDefault(), "R$ %.2f", totalAPagar));
        tvTotalPago.setText(String.format(Locale.getDefault(), "R$ %.2f", totalPago));
        tvTotalGeral.setText(String.format(Locale.getDefault(), "R$ %.2f", totalGeral));
    }

    @Override
    public void onExcluirClick(Divida divida) {
        new AlertDialog.Builder(this)
                .setTitle("Excluir dívida")
                .setMessage("Tem certeza que deseja excluir \"" + divida.getTitulo() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    AlarmeHelper.cancelar(this, divida);
                    db.dividaDao().deletar(divida);
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

    @Override
    public void onPagarClick(Divida divida) {
        // ⭐ Se já está totalmente paga, não faz nada
        if (divida.isPago()) {
            Toast.makeText(this, "Essa dívida já está paga!", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalParcelas = extrairNumeroParcelas(divida.getParcela());
        double valorParcelaReal = divida.getValorTotal() / totalParcelas;

        // ⭐ Soma uma parcela ao valor pago
        divida.valorPago += valorParcelaReal;

        // ⭐ Verifica se quitou tudo (com margem de erro para arredondamento)
        if (divida.valorPago >= divida.getValorTotal() - 0.01) {
            divida.valorPago = divida.getValorTotal();
            divida.setPago(true);
            AlarmeHelper.cancelar(this, divida);
            Toast.makeText(this, "Dívida quitada: " + divida.getTitulo(), Toast.LENGTH_SHORT).show();
        } else {
            int parcelasPagas = (int) Math.round(divida.valorPago / valorParcelaReal);
            Toast.makeText(this,
                    "Parcela " + parcelasPagas + "/" + totalParcelas + " paga!",
                    Toast.LENGTH_SHORT).show();
        }

        db.dividaDao().atualizar(divida);
        carregarDividas();
    }

    /**
     * Extrai o número de parcelas de uma string como "10x" ou "1x (À vista)".
     * Retorna 1 se não conseguir extrair.
     */
    private int extrairNumeroParcelas(String parcela) {
        try {
            String numeros = parcela.replaceAll("[^0-9]", "");
            if (numeros.isEmpty()) return 1;
            int n = Integer.parseInt(numeros);
            return n > 0 ? n : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}