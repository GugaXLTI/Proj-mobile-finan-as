package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfiguracoesActivity extends AppCompatActivity {

    private LinearLayout itemCartoes, itemCategorias, itemBackup, itemDesconectar;
    private SwitchMaterial switchLembretes, switchBiometria;
    private TextView tvEditar, tvNomeUsuario, tvAvatar;

    // Bottom Navigation
    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private SessionManager session;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        // Inicializa a sessão e o banco
        session = new SessionManager(this);
        db = AppDatabase.getInstance(this);

        // Vincula componentes
        itemCartoes = findViewById(R.id.itemCartoes);
        itemCategorias = findViewById(R.id.itemCategorias);
        itemBackup = findViewById(R.id.itemBackup);
        itemDesconectar = findViewById(R.id.itemDesconectar);
        switchLembretes = findViewById(R.id.switchLembretes);
        switchBiometria = findViewById(R.id.switchBiometria);
        tvEditar = findViewById(R.id.tvEditar);
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        tvAvatar = findViewById(R.id.tvAvatar);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        // ======== MOSTRAR O NOME DO USUÁRIO LOGADO ========
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeUsuario.setText(nome);
            tvAvatar.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }

        // ======== AÇÕES DOS ITENS ========

        tvEditar.setOnClickListener(v ->
                Toast.makeText(this, "Editar perfil (em breve)", Toast.LENGTH_SHORT).show());

        itemCartoes.setOnClickListener(v ->
                Toast.makeText(this, "Gerenciar cartões (em breve)", Toast.LENGTH_SHORT).show());

        itemCategorias.setOnClickListener(v ->
                Toast.makeText(this, "Gerenciar categorias (em breve)", Toast.LENGTH_SHORT).show());

        // BACKUP: agora serve como "Limpar Tudo" (para testes internos)
        itemBackup.setOnClickListener(v -> mostrarDialogoLimparTudo());

        switchLembretes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Lembretes ativados" : "Lembretes desativados";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String msg = isChecked ? "Biometria ativada" : "Biometria desativada";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // DESCONECTAR / LIMPAR SESSÃO
        itemDesconectar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Desconectar")
                    .setMessage("Tem certeza que deseja sair da sua conta?")
                    .setPositiveButton("Sim", (dialog, which) -> {
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

    // ======== DIÁLOGO "LIMPAR TUDO" (para testes internos) ========
    private void mostrarDialogoLimparTudo() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar Tudo (Testes)")
                .setMessage("Esta opção apaga TODAS as dívidas, transações e usuários cadastrados. " +
                        "Use apenas para testes. Tem certeza?")
                .setPositiveButton("Sim, limpar tudo", (dialog, which) -> {
                    db.clearAllTables();
                    session.logout();

                    Toast.makeText(this, "Banco de dados limpo com sucesso!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ConfiguracoesActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}