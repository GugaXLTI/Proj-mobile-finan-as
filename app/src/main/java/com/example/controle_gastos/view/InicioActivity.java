package com.example.controle_gastos.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.VencimentoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InicioActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICACAO = 100;

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

        // ⭐ Cria o canal de notificação
        NotificationHelper.criarCanal(this);

        // ⭐ Solicita permissão de notificação no Android 13+
        solicitarPermissaoNotificacao();

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

    // ⭐ SOLICITA PERMISSÃO DE NOTIFICAÇÃO (Android 13+)
    private void solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICACAO
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Lembretes ativados!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sem permissão, os lembretes não vão funcionar.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }
        carregarDados();
    }

    private void carregarDados() {
        dividas = db.dividaDao().listarPorUsuario(session.getUserId());

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