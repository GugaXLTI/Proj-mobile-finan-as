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
import com.example.controle_gastos.adapter.VencimentoAdapter;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.DadosMock;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InicioActivity extends AppCompatActivity {

    private TextView tvNomeInicio, tvTotalDividasInicio, tvTotalPagoInicio, tvAlertaMes;
    private RecyclerView rvVencimentos;
    private Button btnVerTodosVencimentos;

    // Bottom Navigation
    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private List<Divida> dividas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        // Vincular componentes
        tvNomeInicio = findViewById(R.id.tvNomeInicio);
        tvTotalDividasInicio = findViewById(R.id.tvTotalDividasInicio);
        tvTotalPagoInicio = findViewById(R.id.tvTotalPagoInicio);
        tvAlertaMes = findViewById(R.id.tvAlertaMes);
        rvVencimentos = findViewById(R.id.rvVencimentos);
        btnVerTodosVencimentos = findViewById(R.id.btnVerTodosVencimentos);

        // Bottom Navigation
        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        // Carregar dados
        dividas = DadosMock.getDividasIniciais();

        // Configurar lista de vencimentos (apenas os não pagos, limitado a 3)
        List<Divida> vencimentos = new ArrayList<>();
        for (Divida d : dividas) {
            if (!d.isPago()) vencimentos.add(d);
        }
        if (vencimentos.size() > 3) {
            vencimentos = vencimentos.subList(0, 3);
        }

        rvVencimentos.setLayoutManager(new LinearLayoutManager(this));
        rvVencimentos.setAdapter(new VencimentoAdapter(vencimentos));

        // Atualizar totais
        atualizarTotais();

        // ======== AÇÕES ========

        // Botão "Ver Todas as Contas e Cartões"
        btnVerTodosVencimentos.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, DividasActivity.class);
            startActivity(intent);
        });

        // ======== NAVEGAÇÃO ========

        tabInicio.setOnClickListener(v ->
                Toast.makeText(this, "Você já está no Início", Toast.LENGTH_SHORT).show());

        tabLancar.setOnClickListener(v ->
                Toast.makeText(this, "Funcionalidade em breve!", Toast.LENGTH_SHORT).show());

        tabDividas.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, DividasActivity.class);
            startActivity(intent);
        });

        tabRelatorios.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        tabConfig.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, ConfiguracoesActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar os dados ao voltar
        dividas = DadosMock.getDividasIniciais();
        atualizarTotais();
    }

    private void atualizarTotais() {
        double totalAPagar = 0;
        double totalPago = 0;
        int contadorAPagar = 0;

        for (Divida d : dividas) {
            if (d.isPago()) {
                totalPago += d.getValorTotal();
            } else {
                totalAPagar += d.getValorRestante();
                contadorAPagar++;
            }
        }

        tvTotalDividasInicio.setText(String.format(Locale.getDefault(), "R$ %.2f", totalAPagar));
        tvTotalPagoInicio.setText(String.format(Locale.getDefault(), "R$ %.2f", totalPago));

        String alerta = contadorAPagar + (contadorAPagar == 1 ? " fatura somando R$ " : " faturas somando R$ ")
                + String.format(Locale.getDefault(), "%.2f", totalAPagar);
        tvAlertaMes.setText(alerta);
    }
}