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
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.DadosMock;

import java.util.List;
import java.util.Locale;

public class DividasActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private RecyclerView rvDividas;
    private TextView tvTotalAPagar, tvTotalPago, tvTotalGeral;
    private Button btnCadastrarDivida;
    private DividaAdapter adapter;
    private List<Divida> dividas;

    // Bottom Navigation
    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        // Vincular componentes principais
        rvDividas = findViewById(R.id.rvDividas);
        tvTotalAPagar = findViewById(R.id.tvTotalAPagar);
        tvTotalPago = findViewById(R.id.tvTotalPago);
        tvTotalGeral = findViewById(R.id.tvTotalGeral);
        btnCadastrarDivida = findViewById(R.id.btnCadastrarDivida);

        // Vincular Bottom Navigation
        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        // Carregar dados
        dividas = DadosMock.getDividasIniciais();

        // Configurar RecyclerView
        rvDividas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DividaAdapter(dividas, this);
        rvDividas.setAdapter(adapter);

        // Atualizar totais
        atualizarTotais();

        // ======== NAVEGAÇÃO ========

        // Botão Cadastrar nova dívida
        btnCadastrarDivida.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, CadastroDividaActivity.class);
            startActivity(intent);
        });

        // Aba "Início" → volta para a Dashboard
        tabInicio.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Aba "Lançar"
        tabLancar.setOnClickListener(v -> {
            Toast.makeText(this, "Funcionalidade em breve!", Toast.LENGTH_SHORT).show();
        });

        // Aba "Dívidas" (já está aqui)
        tabDividas.setOnClickListener(v -> {
            Toast.makeText(this, "Você já está em Dívidas", Toast.LENGTH_SHORT).show();
        });

        // Aba "Relatórios" → volta para a Dashboard
        tabRelatorios.setOnClickListener(v -> {
            Intent intent = new Intent(DividasActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        // Aba "Config" (quando a tela existir)
        tabConfig.setOnClickListener(v -> {
            Toast.makeText(this, "Abrir Configurações (em breve)", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar dados ao voltar
        dividas = DadosMock.getDividasIniciais();
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
        DadosMock.removerDivida(position);
        dividas = DadosMock.getDividasIniciais();
        adapter = new DividaAdapter(dividas, this);
        rvDividas.setAdapter(adapter);
        atualizarTotais();
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
        adapter.notifyItemChanged(position);
        atualizarTotais();
        Toast.makeText(this, "Pagamento registrado: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
    }
}