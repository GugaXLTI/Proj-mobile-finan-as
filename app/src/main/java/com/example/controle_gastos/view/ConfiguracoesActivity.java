package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfiguracoesActivity extends AppCompatActivity {

    private LinearLayout itemCartoes, itemCategorias, itemBackup, itemDesconectar;
    private SwitchMaterial switchLembretes, switchBiometria;
    private TextView tvEditar;

    // Bottom Navigation
    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        // Inicializa a sessão
        session = new SessionManager(this);

        itemCartoes = findViewById(R.id.itemCartoes);
        itemCategorias = findViewById(R.id.itemCategorias);
        itemBackup = findViewById(R.id.itemBackup);
        itemDesconectar = findViewById(R.id.itemDesconectar);
        switchLembretes = findViewById(R.id.switchLembretes);
        switchBiometria = findViewById(R.id.switchBiometria);
        tvEditar = findViewById(R.id.tvEditar);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        // ======== AÇÕES DOS ITENS ========

        tvEditar.setOnClickListener(v ->
                Toast.makeText(this, "Editar perfil (em breve)", Toast.LENGTH_SHORT).show());

        itemCartoes.setOnClickListener(v ->
                Toast.makeText(this, "Gerenciar cartões (em breve)", Toast.LENGTH_SHORT).show());

        itemCategorias.setOnClickListener(v ->
                Toast.makeText(this, "Gerenciar categorias (em breve)", Toast.LENGTH_SHORT).show());

        itemBackup.setOnClickListener(v ->
                Toast.makeText(this, "Exportar dados (em breve)", Toast.LENGTH_SHORT).show());

        switchLembretes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Lembretes ativados" : "Lembretes desativados";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Biometria ativada" : "Biometria desativada";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // ⭐ DESCONECTAR / LIMPAR SESSÃO (COM SESSION MANAGER)
        itemDesconectar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Desconectar")
                    .setMessage("Tem certeza que deseja sair da sua conta?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        // Limpa a sessão
                        session.logout();

                        Intent intent = new Intent(ConfiguracoesActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        // ======== NAVEGAÇÃO ========

        tabInicio.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        });

        tabLancar.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, CadastroDividaActivity.class);
            startActivity(intent);
        });

        tabDividas.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, DividasActivity.class);
            startActivity(intent);
            finish();
        });

        tabRelatorios.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        });

        tabConfig.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Configurações", Toast.LENGTH_SHORT).show());
    }
}