package com.example.controle_gastos.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.BackupData;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.BackupHelper;
import com.example.controle_gastos.utils.BiometricHelper;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.OnboardingStore;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class ConfiguracoesActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private LinearLayout itemCartoes, itemCategorias, itemBackup, itemDesconectar, itemSalario;
    private LinearLayout itemLembretes, itemBiometria;
    private SwitchMaterial switchLembretes, switchBiometria;
    private TextView tvEditar, tvNomeUsuario, tvAvatar, tvSalarioAtual, tvBiometriaSub;

    private ImageView tabInicio, tabLancar, tabDividas, tabRelatorios;
    private LinearLayout tabConfig;

    private SessionManager session;
    private AppDatabase db;

    private boolean biometriaEstadoAtual = false;

    private ActivityResultLauncher<String[]> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        NavScrollHelper.attach(this);

        session = new SessionManager(this);
        db = AppDatabase.getInstance(this);

        NotificationHelper.criarCanal(this);

        itemCartoes = findViewById(R.id.itemCartoes);
        itemCategorias = findViewById(R.id.itemCategorias);
        itemSalario = findViewById(R.id.itemSalario);
        itemLembretes = findViewById(R.id.itemLembretes);
        itemBiometria = findViewById(R.id.itemBiometria);
        itemBackup = findViewById(R.id.itemBackup);
        itemDesconectar = findViewById(R.id.itemDesconectar);

        switchLembretes = findViewById(R.id.switchLembretes);
        switchBiometria = findViewById(R.id.switchBiometria);
        tvEditar = findViewById(R.id.tvEditar);
        tvNomeUsuario = findViewById(R.id.tvNomeUsuario);
        tvAvatar = findViewById(R.id.tvAvatar);
        tvSalarioAtual = findViewById(R.id.tvSalarioAtual);
        tvBiometriaSub = findViewById(R.id.tvBiometriaSub);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        processarImportacao(uri);
                    }
                }
        );

        atualizarPerfil();
        atualizarSaldoInicialSubtitulo();

        switchLembretes.setChecked(AlarmeHelper.isLembretesAtivos(this));

        biometriaEstadoAtual = session.isBiometriaAtiva();
        boolean biometriaDisponivel = BiometricHelper.podeUsarBiometria(this);

        if (!biometriaDisponivel) {
            switchBiometria.setEnabled(false);
            switchBiometria.setAlpha(0.5f);
            switchBiometria.setChecked(false);
            tvBiometriaSub.setText("Indisponível neste aparelho");
        } else {
            switchBiometria.setChecked(biometriaEstadoAtual);
        }

        tvEditar.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, EditarPerfilActivity.class)));

        itemCartoes.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, CartoesActivity.class)));

        itemCategorias.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, CategoriasActivity.class)));

        itemSalario.setOnClickListener(v -> {
            Intent i = new Intent(ConfiguracoesActivity.this, OnboardingActivity.class);
            i.putExtra(OnboardingActivity.EXTRA_EDITANDO, true);
            startActivity(i);
        });

        itemBackup.setOnClickListener(v -> mostrarDialogoBackup());

        itemLembretes.setOnClickListener(v -> switchLembretes.toggle());
        itemBiometria.setOnClickListener(v -> {
            if (switchBiometria.isEnabled()) switchBiometria.toggle();
        });

        switchLembretes.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AlarmeHelper.setLembretesAtivos(ConfiguracoesActivity.this, isChecked);
            String msg = isChecked ? "Lembretes ativados" : "Lembretes desativados";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                ativarBiometria();
            } else {
                desativarBiometria();
            }
        });

        itemDesconectar.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
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

        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(ConfiguracoesActivity.this, InicioActivity.class));
            finish();
        });

        tabLancar.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, CadastroDividaActivity.class)));

        tabDividas.setOnClickListener(v -> {
            startActivity(new Intent(ConfiguracoesActivity.this, DividasActivity.class));
            finish();
        });

        tabRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(ConfiguracoesActivity.this, DashboardActivity.class));
            finish();
        });

        tabConfig.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Configurações", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarPerfil();
        atualizarSaldoInicialSubtitulo();
    }

    private void atualizarPerfil() {
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeUsuario.setText(nome);
            tvAvatar.setText(String.valueOf(nome.charAt(0)).toUpperCase(LOCALE_BR));
        }
    }

    private void atualizarSaldoInicialSubtitulo() {
        int userId = session.getUserId();
        if (OnboardingStore.temSaldoInicial(this, userId)) {
            tvSalarioAtual.setText("Saldo inicial: "
                    + NumberFormat.getCurrencyInstance(LOCALE_BR)
                    .format(OnboardingStore.getSaldoInicial(this, userId)));
        } else {
            tvSalarioAtual.setText("Informe quanto você tem na conta");
        }
    }

    private void mostrarDialogoBackup() {
        String[] opcoes = {
                "Exportar backup (JSON)",
                "Restaurar backup",
                "Limpar tudo (testes)"
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Backup dos dados")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        exportarBackup();
                    } else if (which == 1) {
                        solicitarArquivoBackup();
                    } else {
                        mostrarDialogoLimparTudo();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void exportarBackup() {
        File arquivo = BackupHelper.exportar(this, session.getUserId());
        if (arquivo == null) {
            Toast.makeText(this, "Erro ao gerar o backup.", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Backup gerado com sucesso!", Toast.LENGTH_SHORT).show();
        BackupHelper.compartilharBackup(this, arquivo);
    }

    private void solicitarArquivoBackup() {
        try {
            filePickerLauncher.launch(new String[]{"application/json", "text/*"});
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível abrir o seletor de arquivos.", Toast.LENGTH_SHORT).show();
        }
    }

    private void processarImportacao(Uri uri) {
        BackupData data = BackupHelper.lerArquivo(this, uri);
        if (data == null) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Backup inválido")
                    .setMessage("O arquivo selecionado não é um backup válido do ORG.\n\nVerifique se escolheu o arquivo correto e tente novamente.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        int totalDividas = data.dividas != null ? data.dividas.size() : 0;
        int totalCartoes = data.cartoes != null ? data.cartoes.size() : 0;
        int totalPix = data.chavesPix != null ? data.chavesPix.size() : 0;
        int totalCategorias = data.categorias != null ? data.categorias.size() : 0;

        int totalContas = data.contas != null ? data.contas.size() : 0;
        int totalTransacoes = data.transacoes != null ? data.transacoes.size() : 0;

        String mensagem = "Backup de " + data.dataBackup + "\n" +
                "Usuário: " + data.nomeUsuario + "\n\n" +
                "Contém:\n" +
                "• " + totalDividas + " dívidas\n" +
                "• " + totalCartoes + " cartões\n" +
                "• " + totalPix + " chaves Pix\n" +
                "• " + totalCategorias + " categorias\n" +
                (totalContas > 0 ? "• " + totalContas + " conta(s) bancária(s)\n" : "") +
                (totalTransacoes > 0 ? "• " + totalTransacoes + " transações automáticas\n" : "") + "\n" +
                "Atenção: isso vai SUBSTITUIR todos os seus dados atuais.\n" +
                "Deseja continuar?";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Restaurar backup")
                .setMessage(mensagem)
                .setPositiveButton("Sim, restaurar", (d, w) -> aplicarBackup(data))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void aplicarBackup(BackupData data) {
        boolean sucesso = BackupHelper.aplicarBackup(this, session.getUserId(), data);
        if (sucesso) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Backup restaurado")
                    .setMessage("Seus dados foram restaurados com sucesso!")
                    .setPositiveButton("OK", (d, w) -> {
                        Intent intent = new Intent(this, InicioActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        } else {
            Toast.makeText(this, "Erro ao restaurar o backup.", Toast.LENGTH_LONG).show();
        }
    }

    private void ativarBiometria() {
        BiometricHelper.autenticar(
                this,
                "Ativar biometria",
                "Confirme sua identidade para ativar o login biométrico",
                new BiometricHelper.Callback() {
                    @Override
                    public void onSucesso() {
                        session.salvarUsuarioBiometrico(
                                session.getUserId(),
                                session.getNome(),
                                session.getEmail()
                        );
                        biometriaEstadoAtual = true;
                        Toast.makeText(ConfiguracoesActivity.this, "Biometria ativada com sucesso!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFalha(String motivo) {
                        switchBiometria.setOnCheckedChangeListener(null);
                        switchBiometria.setChecked(false);
                        switchBiometria.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) ativarBiometria();
                            else desativarBiometria();
                        });
                        Toast.makeText(ConfiguracoesActivity.this, "Não foi possível ativar a biometria.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void desativarBiometria() {
        session.limparBiometria();
        biometriaEstadoAtual = false;
        Toast.makeText(this, "Biometria desativada.", Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoLimparTudo() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Limpar tudo (testes)")
                .setMessage("Esta opção apaga TODAS as dívidas, transações e usuários cadastrados. Use apenas para testes. Tem certeza?")
                .setPositiveButton("Sim, limpar tudo", (dialog, which) -> {
                    db.clearAllTables();
                    OnboardingStore.limparTudo(this);
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