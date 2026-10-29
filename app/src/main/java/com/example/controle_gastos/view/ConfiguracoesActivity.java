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
import android.widget.ImageView;
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
import java.text.NumberFormat;
import java.util.Locale;

public class ConfiguracoesActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private LinearLayout itemCartoes, itemCategorias, itemBackup, itemDesconectar, itemSalario;
    private LinearLayout itemLembretes, itemBiometria;
    private SwitchMaterial switchLembretes, switchBiometria;
    private TextView tvEditar, tvNomeUsuario, tvAvatar, tvSalarioAtual, tvBiometriaSub;

    // Navegação (aba ativa = LinearLayout, demais = ImageView)
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
        atualizarSalarioSubtitulo();

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

        // ======== AÇÕES DOS ITENS ========

        tvEditar.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, EditarPerfilActivity.class)));

        itemCartoes.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, CartoesActivity.class)));

        itemCategorias.setOnClickListener(v ->
                startActivity(new Intent(ConfiguracoesActivity.this, CategoriasActivity.class)));

        itemSalario.setOnClickListener(v -> mostrarDialogoEditarSalario());

        itemBackup.setOnClickListener(v -> mostrarDialogoBackup());

        // Tocar na linha inteira também alterna a chave
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
        // Atualiza nome/avatar/salário ao voltar da tela de editar perfil
        atualizarPerfil();
        atualizarSalarioSubtitulo();
    }

    private void atualizarPerfil() {
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeUsuario.setText(nome);
            tvAvatar.setText(String.valueOf(nome.charAt(0)).toUpperCase(LOCALE_BR));
        }
    }

    private void atualizarSalarioSubtitulo() {
        Usuario user = db.usuarioDao().buscarPorId(session.getUserId());
        if (user != null && user.getSalarioMensal() > 0) {
            tvSalarioAtual.setText("Atual: "
                    + NumberFormat.getCurrencyInstance(LOCALE_BR).format(user.getSalarioMensal()));
        } else {
            tvSalarioAtual.setText("Defina seu orçamento base");
        }
    }

    // Diálogo para editar o salário com máscara
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
                .setTitle("Editar salário mensal")
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
                        atualizarSalarioSubtitulo();
                        Toast.makeText(ConfiguracoesActivity.this, "Salário atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ConfiguracoesActivity.this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
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
                "Exportar backup (JSON)",
                "Restaurar backup",
                "Limpar tudo (testes)"
        };

        new AlertDialog.Builder(this)
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
                "Contém:\n" +
                "• " + totalDividas + " dívidas\n" +
                "• " + totalCartoes + " cartões\n" +
                "• " + totalPix + " chaves Pix\n" +
                "• " + totalCategorias + " categorias\n\n" +
                "Atenção: isso vai SUBSTITUIR todos os seus dados atuais.\n" +
                "Deseja continuar?";

        new AlertDialog.Builder(this)
                .setTitle("Restaurar backup")
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
                .setTitle("Limpar tudo (testes)")
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