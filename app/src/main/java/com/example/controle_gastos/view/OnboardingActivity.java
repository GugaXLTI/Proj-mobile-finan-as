package com.example.controle_gastos.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Conta;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.OnboardingStore;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    public static final String EXTRA_EDITANDO = "editando_renda";

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private static final int PG_WELCOME = 0;
    private static final int PG_SALDO = 1;
    private static final int PG_CONTAS = 2;
    private static final int PG_PERMISSAO = 3;

    private ViewFlipper viewFlipper;
    private MaterialButton btnAvancar;
    private ImageView btnVoltar;
    private LinearLayout llProgress;
    private TextView tvPasso;

    private EditText editSaldoInicial;
    private TextView tvStatusPermissao;
    private ImageView ivStatusPermissao;
    private MaterialButton btnAutorizar;

    // ⭐ Variáveis da nova tela de Contas Fixas
    private ChipGroup chipGroupContasOnb;
    private TextView btnOutraContaOnb;
    private final String[] contasComuns = {
            "Água", "Energia", "Internet", "Aluguel",
            "Gás", "Celular", "Streaming", "Condomínio"
    };

    private double saldoInicial = 0;
    private boolean editando = false;

    private SessionManager session;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        session = new SessionManager(this);
        db = AppDatabase.getInstance(this);
        editando = getIntent().getBooleanExtra(EXTRA_EDITANDO, false);

        viewFlipper = findViewById(R.id.viewFlipperOnboarding);
        btnAvancar = findViewById(R.id.btnAvancarOnb);
        btnVoltar = findViewById(R.id.btnVoltarOnb);
        llProgress = findViewById(R.id.llProgressOnb);
        tvPasso = findViewById(R.id.tvPassoOnb);

        viewFlipper.setInAnimation(this, android.R.anim.fade_in);
        viewFlipper.setOutAnimation(this, android.R.anim.fade_out);

        LayoutInflater inflater = getLayoutInflater();
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_welcome, viewFlipper, false));       // 0
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_saldo_inicial, viewFlipper, false)); // 1
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_contas, viewFlipper, false));        // 2 (NOVA)
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_permissoes, viewFlipper, false));    // 3

        setupSaldo();
        setupContas();
        setupPermissao();

        btnAvancar.setOnClickListener(v -> avancarFluxo());
        btnVoltar.setOnClickListener(v -> {
            if (!voltarFluxo()) finish();
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!voltarFluxo()) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        ir(editando ? PG_SALDO : PG_WELCOME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarStatusPermissao();
        atualizarBotao();
    }

    // ------------------------------------------------------------------
    // SETUP DAS PÁGINAS
    // ------------------------------------------------------------------
    private void setupSaldo() {
        View page = viewFlipper.getChildAt(PG_SALDO);
        editSaldoInicial = page.findViewById(R.id.editSaldoInicial);
        configurarMascaraValor(editSaldoInicial);

        int userId = session.getUserId();
        Conta conta = db.contaDao().buscarPorUsuario(userId);
        if (conta != null) {
            saldoInicial = conta.saldoInicial;
        } else if (OnboardingStore.temSaldoInicial(this, userId)) {
            saldoInicial = OnboardingStore.getSaldoInicial(this, userId);
        }

        if (saldoInicial > 0) {
            String formatado = moeda(saldoInicial);
            editSaldoInicial.setText(formatado);
            editSaldoInicial.setSelection(formatado.length());
        }
    }

    private void setupContas() {
        View page = viewFlipper.getChildAt(PG_CONTAS);
        chipGroupContasOnb = page.findViewById(R.id.chipGroupContasOnb);
        btnOutraContaOnb = page.findViewById(R.id.btnOutraContaOnb);

        for (String conta : contasComuns) {
            Chip chip = criarChipEstilizado(conta);
            chip.setOnClickListener(v -> abrirDialogConta(conta, chip));
            chipGroupContasOnb.addView(chip);
        }

        btnOutraContaOnb.setOnClickListener(v -> abrirDialogConta(null, null));
    }

    private void setupPermissao() {
        View page = viewFlipper.getChildAt(PG_PERMISSAO);
        tvStatusPermissao = page.findViewById(R.id.tvStatusPermissao);
        ivStatusPermissao = page.findViewById(R.id.ivStatusPermissao);
        btnAutorizar = page.findViewById(R.id.btnAutorizarNotificacoes);

        btnAutorizar.setOnClickListener(v -> abrirConfiguracaoNotificacoes());
    }

    // ------------------------------------------------------------------
    // NAVEGAÇÃO
    // ------------------------------------------------------------------
    private void avancarFluxo() {
        int atual = viewFlipper.getDisplayedChild();

        if (atual == PG_WELCOME) {
            ir(PG_SALDO);
        } else if (atual == PG_SALDO) {
            if (!saldoPreenchido()) {
                Toast.makeText(this, "Informe quanto você tem na conta hoje.", Toast.LENGTH_SHORT).show();
                return;
            }
            saldoInicial = lerValor(editSaldoInicial);

            OnboardingStore.salvarSaldoInicial(this, session.getUserId(), saldoInicial);

            Conta contaExistente = db.contaDao().buscarPorUsuario(session.getUserId());
            if (contaExistente != null) {
                contaExistente.saldoInicial = saldoInicial;
                db.contaDao().atualizar(contaExistente);
            } else {
                Conta novaConta = new Conta(session.getUserId(), "Conta Principal", saldoInicial);
                db.contaDao().inserir(novaConta);
            }

            if (editando) {
                Toast.makeText(this, "Saldo inicial atualizado!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                ir(PG_CONTAS); // Vai para a tela de contas fixas
            }
        } else if (atual == PG_CONTAS) {
            ir(PG_PERMISSAO); // Avança para permissão
        } else {
            concluirOnboarding();
        }
    }

    private boolean voltarFluxo() {
        int atual = viewFlipper.getDisplayedChild();
        if (atual == PG_PERMISSAO) {
            ir(PG_CONTAS);
            return true;
        }
        if (atual == PG_CONTAS) {
            ir(PG_SALDO);
            return true;
        }
        if (atual == PG_SALDO && !editando) {
            ir(PG_WELCOME);
            return true;
        }
        return false;
    }

    private void ir(int pagina) {
        esconderTeclado();
        viewFlipper.setDisplayedChild(pagina);
        atualizarHeader();
        atualizarBotao();
    }

    private void atualizarHeader() {
        int pagina = viewFlipper.getDisplayedChild();
        boolean welcome = pagina == PG_WELCOME;

        btnVoltar.setVisibility(welcome ? View.INVISIBLE : View.VISIBLE);

        boolean mostrarProgresso = !welcome && !editando;
        llProgress.setVisibility(mostrarProgresso ? View.VISIBLE : View.INVISIBLE);
        tvPasso.setVisibility(mostrarProgresso ? View.VISIBLE : View.INVISIBLE);

        if (mostrarProgresso) {
            int atual = (pagina == PG_SALDO) ? 1 : (pagina == PG_CONTAS ? 2 : 3);
            montarProgresso(3, atual);
            tvPasso.setText("Passo " + atual + " de 3");
        }

        if (pagina == PG_WELCOME) {
            btnAvancar.setText("Começar");
        } else if (pagina == PG_SALDO) {
            btnAvancar.setText(editando ? "Salvar" : "Continuar");
        } else if (pagina == PG_CONTAS) {
            btnAvancar.setText("Continuar");
        } else {
            btnAvancar.setText(notificacoesAutorizadas() ? "Concluir" : "Pular por enquanto");
        }
    }

    private void montarProgresso(int total, int atual) {
        llProgress.removeAllViews();
        for (int i = 1; i <= total; i++) {
            View seg = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(4), 1f);
            if (i < total) lp.setMarginEnd(dp(6));
            seg.setLayoutParams(lp);
            seg.setBackgroundResource(i <= atual ? R.drawable.bg_onb_progress_on : R.drawable.bg_onb_progress_off);
            llProgress.addView(seg);
        }
    }

    private void atualizarBotao() {
        int pagina = viewFlipper.getDisplayedChild();
        boolean ok = (pagina != PG_SALDO) || saldoPreenchido();
        btnAvancar.setEnabled(ok);
        btnAvancar.setAlpha(ok ? 1f : 0.4f);

        if (pagina == PG_PERMISSAO) {
            btnAvancar.setText(notificacoesAutorizadas() ? "Concluir" : "Pular por enquanto");
        }
    }

    // ------------------------------------------------------------------
    // LÓGICA DE CONTAS FIXAS (DIALOG E SALVAMENTO)
    // ------------------------------------------------------------------
    private Chip criarChipEstilizado(String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setClickable(true);

        int[][] states = new int[][] { new int[] {android.R.attr.state_checked}, new int[] {} };
        int[] colors = new int[] { Color.parseColor("#123D33"), Color.parseColor("#111A2E") };
        chip.setChipBackgroundColor(new ColorStateList(states, colors));

        int[] textColors = new int[] { Color.parseColor("#34D399"), Color.parseColor("#E2E8F0") };
        chip.setTextColor(new ColorStateList(states, textColors));

        chip.setChipStrokeColor(ColorStateList.valueOf(Color.parseColor("#1E2A44")));
        chip.setChipStrokeWidth(2f);
        chip.setChipCornerRadius(24f);
        return chip;
    }

    private void abrirDialogConta(String nomeContaPadrao, Chip chipVisual) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_quick_conta, null);

        TextView tvTitulo = dialogView.findViewById(R.id.tvTituloDialogConta);
        TextInputLayout tilNome = dialogView.findViewById(R.id.tilNomeConta);
        com.google.android.material.textfield.TextInputEditText editNome = dialogView.findViewById(R.id.editNomeConta);
        com.google.android.material.textfield.TextInputEditText editValor = dialogView.findViewById(R.id.editValorConta);
        com.google.android.material.textfield.TextInputEditText editDia = dialogView.findViewById(R.id.editDiaVencimento);

        if (nomeContaPadrao != null) {
            tvTitulo.setText("Adicionar " + nomeContaPadrao);
            tilNome.setVisibility(View.GONE);
        } else {
            tvTitulo.setText("Nova Conta");
            tilNome.setVisibility(View.VISIBLE);
        }

        new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String nome = (nomeContaPadrao != null) ? nomeContaPadrao : editNome.getText().toString().trim();
                    String valorStr = editValor.getText().toString().trim().replace(",", ".");
                    String diaStr = editDia.getText().toString().trim();

                    if (nome.isEmpty() || valorStr.isEmpty() || diaStr.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double valor = Double.parseDouble(valorStr);
                        int diaVencimento = Integer.parseInt(diaStr);

                        if (diaVencimento < 1 || diaVencimento > 31) {
                            Toast.makeText(this, "Dia inválido. Use entre 1 e 31.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        salvarContaFixaNoBanco(nome, valor, diaVencimento);
                        if (chipVisual != null) chipVisual.setChecked(true);

                        // ⭐ Mensagem atualizada para tranquilizar o usuário
                        Toast.makeText(this, nome + " adicionada! Você pode ajustar o valor a qualquer momento.", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Valor ou dia inválido.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void salvarContaFixaNoBanco(String nome, double valor, int diaVencimento) {
        int grupoId = db.dividaDao().maxGrupoId() + 1;

        Calendar cBase = Calendar.getInstance();
        if (cBase.get(Calendar.DAY_OF_MONTH) > diaVencimento) {
            cBase.add(Calendar.MONTH, 1);
        }
        cBase.set(Calendar.DAY_OF_MONTH, diaVencimento);

        // Cria 12 meses de uma vez
        for (int i = 0; i < 12; i++) {
            Calendar cParcela = (Calendar) cBase.clone();
            cParcela.add(Calendar.MONTH, i);
            String vencParcela = String.format(LOCALE_BR, "%02d/%02d/%04d",
                    cParcela.get(Calendar.DAY_OF_MONTH), cParcela.get(Calendar.MONTH) + 1, cParcela.get(Calendar.YEAR));

            String numeroParcela = (i + 1) + "/12";

            Divida parcela = new Divida(session.getUserId(), grupoId, nome, valor, valor, 0.0, "Conta Fixa", "Contas Fixas", numeroParcela, vencParcela, false, false);
            db.dividaDao().inserir(parcela);
        }
    }

    // ------------------------------------------------------------------
    // PERMISSÃO DE NOTIFICAÇÕES
    // ------------------------------------------------------------------
    private boolean notificacoesAutorizadas() {
        return NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
    }

    private void abrirConfiguracaoNotificacoes() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(this, "Não foi possível abrir as configurações.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void atualizarStatusPermissao() {
        if (tvStatusPermissao == null) return;

        boolean ok = notificacoesAutorizadas();
        if (ok) {
            tvStatusPermissao.setText("Notificações autorizadas");
            tvStatusPermissao.setTextColor(Color.parseColor("#34D399"));
            ivStatusPermissao.setColorFilter(Color.parseColor("#34D399"));
            btnAutorizar.setText("Gerenciar permissão");
        } else {
            tvStatusPermissao.setText("Ainda não autorizado");
            tvStatusPermissao.setTextColor(Color.parseColor("#FCD34D"));
            ivStatusPermissao.setColorFilter(Color.parseColor("#FBBF24"));
            btnAutorizar.setText("Autorizar notificações");
        }
    }

    // ------------------------------------------------------------------
    // CONCLUIR
    // ------------------------------------------------------------------
    private void concluirOnboarding() {
        int userId = session.getUserId();

        OnboardingStore.setConcluido(this, userId, true);
        session.setOnboardingConcluido(true);

        Toast.makeText(this, "Tudo pronto! Vamos começar.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, InicioActivity.class));
        finish();
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private boolean saldoPreenchido() {
        return !editSaldoInicial.getText().toString().replaceAll("[^0-9]", "").isEmpty();
    }

    private void configurarMascaraValor(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean atualizando = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (atualizando) return;
                atualizando = true;

                String digitos = s.toString().replaceAll("[^0-9]", "");
                if (digitos.isEmpty()) {
                    editText.setText("");
                } else {
                    if (digitos.length() > 11) digitos = digitos.substring(0, 11);
                    String formatado = moeda(Double.parseDouble(digitos) / 100.0);
                    editText.setText(formatado);
                    editText.setSelection(formatado.length());
                }

                atualizando = false;
                atualizarBotao();
            }
        });
    }

    private double lerValor(EditText e) {
        String digitos = e.getText().toString().replaceAll("[^0-9]", "");
        if (digitos.isEmpty()) return 0;
        if (digitos.length() > 11) digitos = digitos.substring(0, 11);
        return Double.parseDouble(digitos) / 100.0;
    }

    private String moeda(double valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
    }

    private void esconderTeclado() {
        View foco = getCurrentFocus();
        if (foco != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
            foco.clearFocus();
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}