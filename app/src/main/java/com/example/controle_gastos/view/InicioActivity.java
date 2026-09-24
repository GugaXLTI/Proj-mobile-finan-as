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
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InicioActivity extends AppCompatActivity {

    private TextView tvAvatarInicio, tvNomeInicio, tvTotalDividasInicio, tvTotalPagoInicio, tvAlertaMes;
    private RecyclerView rvVencimentos;
    private Button btnVerTodosVencimentos;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;
    private List<Divida> dividas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        tvAvatarInicio = findViewById(R.id.tvAvatarInicio);
        tvNomeInicio = findViewById(R.id.tvNomeInicio);
        tvTotalDividasInicio = findViewById(R.id.tvTotalDividasInicio);
        tvTotalPagoInicio = findViewById(R.id.tvTotalPagoInicio);
        tvAlertaMes = findViewById(R.id.tvAlertaMes);
        rvVencimentos = findViewById(R.id.rvVencimentos);
        btnVerTodosVencimentos = findViewById(R.id.btnVerTodosVencimentos);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        // ======== MOSTRAR O NOME DO USUÁRIO LOGADO ========
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }

        rvVencimentos.setLayoutManager(new LinearLayoutManager(this));

        carregarDados();

        btnVerTodosVencimentos.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, DividasActivity.class);
            startActivity(intent);
        });

        tabInicio.setOnClickListener(v ->
                Toast.makeText(this, "Você já está no Início", Toast.LENGTH_SHORT).show());

        tabLancar.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, CadastroDividaActivity.class);
            startActivity(intent);
        });

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
        // Atualiza o nome sempre que voltar para a tela
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }
        carregarDados();
    }

    private void carregarDados() {
        // ⭐ FILTRA POR USUÁRIO LOGADO
        dividas = db.dividaDao().listarPorUsuario(session.getUserId());

        // Lista de vencimentos (não pagas, até 3)
        List<Divida> vencimentos = new ArrayList<>();
        for (Divida d : dividas) {
            if (!d.isPago()) vencimentos.add(d);
        }
        if (vencimentos.size() > 3) {
            vencimentos = vencimentos.subList(0, 3);
        }

        rvVencimentos.setAdapter(new VencimentoAdapter(vencimentos));

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