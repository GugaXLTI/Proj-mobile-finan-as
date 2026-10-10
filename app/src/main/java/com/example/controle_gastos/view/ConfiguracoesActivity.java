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
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.BiometricHelper;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ConfiguracoesActivity extends AppCompatActivity {

    private LinearLayout itemCartoes, itemCategorias, itemBackup, itemDesconectar;
    private SwitchMaterial switchLembretes, switchBiometria;
    private TextView tvEditar, tvNomeUsuario, tvAvatar;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private SessionManager session;
    private AppDatabase db;

    // ⭐ Guarda o último estado para reverter em caso de falha
    private boolean biometriaEstadoAtual = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        session = new SessionManager(this);
        db = AppDatabase.getInstance(this);

        NotificationHelper.criarCanal(this);

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

        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeUsuario.setText(nome);
            tvAvatar.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }

        // ⭐ Switch de Lembretes
        switchLembretes.setChecked(AlarmeHelper.isLembretesAtivos(this));

        // ⭐ Switch de Biometria
        biometriaEstadoAtual = session.isBiometriaAtiva();

        // Verifica se o aparelho suporta biometria
        boolean biometriaDisponivel = BiometricHelper.podeUsarBiometria(this);

        if (!biometriaDisponivel) {
            switchBiometria.setEnabled(false);
            switchBiometria.setAlpha(0.5f);
            switchBiometria.setChecked(false);
            tvBiometriaAviso();
        } else {
            switchBiometria.setChecked(biometriaEstadoAtual);
        }

        // ======== AÇÕES DOS ITENS ========

        tvEditar.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, EditarPerfilActivity.class);
            startActivity(intent);
        });

        itemCartoes.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, CartoesActivity.class);
            startActivity(intent);
        });

        itemCategorias.setOnClickListener(v -> {
            Intent intent = new Intent(ConfiguracoesActivity.this, CategoriasActivity.class);
            startActivity(intent);
        });

        itemBackup.setOnClickListener(v -> mostrarDialogoLimparTudo());

        // ⭐ Switch de Lembretes Funcional
        switchLembretes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AlarmeHelper.setLembretesAtivos(ConfiguracoesActivity.this, isChecked);
            String msg = isChecked ? "Lembretes ativados" : "Lembretes desativados";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        // ⭐ Switch de Biometria Funcional
        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ativarBiometria();
            } else {
                desativarBiometria();
            }
        });

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

    /**
     * ⭐ Pede autenticação biométrica e, se sucesso, salva a preferência.
     */
    private void ativarBiometria() {
        BiometricHelper.autenticar(
                this,
                "Ativar biometria",
                "Confirme sua identidade para ativar o login biométrico",
                new BiometricHelper.Callback() {
                    @Override
                    public void onSucesso() {
                        // Salva a preferência + dados do usuário logado
                        session.salvarUsuarioBiometrico(
                                session.getUserId(),
                                session.getNome(),
                                session.getEmail()
                        );

                        biometriaEstadoAtual = true;

                        Toast.makeText(ConfiguracoesActivity.this,
                                "Biometria ativada com sucesso!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFalha(String motivo) {
                        // Reverte o switch sem disparar o listener de novo
                        switchBiometria.setOnCheckedChangeListener(null);
                        switchBiometria.setChecked(false);
                        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) ativarBiometria();
                            else desativarBiometria();
                        });

                        Toast.makeText(ConfiguracoesActivity.this,
                                "Não foi possível ativar a biometria.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * ⭐ Desativa a biometria.
     */
    private void desativarBiometria() {
        session.limparBiometria();
        biometriaEstadoAtual = false;

        Toast.makeText(this, "Biometria desativada.", Toast.LENGTH_SHORT).show();
    }

    private void tvBiometriaAviso() {
        Toast.makeText(this,
                "Biometria não disponível neste dispositivo. Cadastre uma digital nas configurações do Android.",
                Toast.LENGTH_LONG).show();
    }

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