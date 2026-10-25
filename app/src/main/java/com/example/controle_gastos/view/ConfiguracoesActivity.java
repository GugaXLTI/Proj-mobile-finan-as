package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.BackupData;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.BackupHelper;
import com.example.controle_gastos.utils.BiometricHelper;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.util.Locale;

public class ConfiguracoesActivity extends AppCompatActivity {

    private LinearLayout itemCartoes, itemCategorias, itemBackup, itemDesconectar, itemSalario;
    private SwitchMaterial switchLembretes, switchBiometria;
    private TextView tvEditar, tvNomeUsuario, tvAvatar;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private SessionManager session;
    private AppDatabase db;
    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private boolean biometriaEstadoAtual = false;

    private ActivityResultLauncher<String[]> filePickerLauncher;

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

        // ⭐ NOVO (T144): Encontrando o item de salário
        itemSalario = findViewById(R.id.itemSalario);

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

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        processarImportacao(uri);
                    }
                }
        );

        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeUsuario.setText(nome);
            tvAvatar.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }

        switchLembretes.setChecked(AlarmeHelper.isLembretesAtivos(this));

        biometriaEstadoAtual = session.isBiometriaAtiva();
        boolean biometriaDisponivel = BiometricHelper.podeUsarBiometria(this);

        if (!biometriaDisponivel) {
            switchBiometria.setEnabled(false);
            switchBiometria.setAlpha(0.5f);
            switchBiometria.setChecked(false);
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

        // ⭐ NOVO (T144): Ação do botão editar salário
        itemSalario.setOnClickListener(v -> mostrarDialogoEditarSalario());

        itemBackup.setOnClickListener(v -> mostrarDialogoBackup());

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

    // ⭐ NOVO (T144): Diálogo para editar o salário com máscara
    private void mostrarDialogoEditarSalario() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextColor(Color.parseColor("#FFFFFF"));
        input.setHintTextColor(Color.parseColor("#475569"));
        input.setHint("R$ 0,00");
        input.setGravity(Gravity.CENTER);
        input.setTextSize(24f);

        Usuario user = db.usuarioDao().buscarPorId(session.getUserId());
        if (user != null && user.getSalarioMensal() > 0) {
            String salarioFormatado = String.format(LOCALE_BR, "R$ %.2f", user.getSalarioMensal());
            input.setText(salarioFormatado);
            input.setSelection(salarioFormatado.length());
        }

        // Máscara de Moeda
        input.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                String text = s.toString().replaceAll("[^0-9]", "");
                if (text.isEmpty()) {
                    input.setText("");
                    isUpdating = false;
                    return;
                }
                if (text.length() > 11) text = text.substring(0, 11);
                double valor = Double.parseDouble(text) / 100.0;
                String formatted = String.format(LOCALE_BR, "R$ %.2f", valor);
                input.setText(formatted);
                input.setSelection(formatted.length());
                isUpdating = false;
            }
        });

        // Padding no EditText
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        params.setMargins(padding, padding, padding, padding);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Editar Salário Mensal")
                .setMessage("Informe o novo valor do seu salário para recalcularmos seu orçamento:")
                .setView(container)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String salarioStr = input.getText().toString().replace("R$", "").replace(".", "").replace(",", ".").trim();
                    if (salarioStr.isEmpty() || Double.parseDouble(salarioStr) <= 0) {
                        Toast.makeText(ConfiguracoesActivity.this, "Informe um valor válido.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double novoSalario = Double.parseDouble(salarioStr);

                    if (user != null) {
                        user.setSalarioMensal(novoSalario);
                        db.usuarioDao().atualizar(user);
                        Toast.makeText(ConfiguracoesActivity.this, "Salário atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ==========================================
    // BACKUP — DIÁLOGO E AÇÕES
    // ==========================================

    private void mostrarDialogoBackup() {
        String[] opcoes = {
                "📤  Exportar Backup (JSON)",
                "📥  Restaurar Backup",
                "🗑️  Limpar Tudo (Testes)"
        };

        new AlertDialog.Builder(this)
                .setTitle("Backup dos Dados")
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
            new AlertDialog.Builder(this)
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

        String mensagem = "Backup de " + data.dataBackup + "\n" +
                "Usuário: " + data.nomeUsuario + "\n\n" +
                "📋 Contém:\n" +
                "• " + totalDividas + " dívidas\n" +
                "• " + totalCartoes + " cartões\n" +
                "• " + totalPix + " chaves Pix\n" +
                "• " + totalCategorias + " categorias\n\n" +
                "⚠️ Isso vai SUBSTITUIR todos os seus dados atuais.\n" +
                "Deseja continuar?";

        new AlertDialog.Builder(this)
                .setTitle("Restaurar Backup")
                .setMessage(mensagem)
                .setPositiveButton("Sim, restaurar", (d, w) -> aplicarBackup(data))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void aplicarBackup(BackupData data) {
        boolean sucesso = BackupHelper.aplicarBackup(this, session.getUserId(), data);
        if (sucesso) {
            new AlertDialog.Builder(this)
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

    // ==========================================
    // BIOMETRIA
    // ==========================================

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

    // ==========================================
    // LIMPAR TUDO (mantido para testes)
    // ==========================================

    private void mostrarDialogoLimparTudo() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar Tudo (Testes)")
                .setMessage("Esta opção apaga TODAS as dívidas, transações e usuários cadastrados. Use apenas para testes. Tem certeza?")
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