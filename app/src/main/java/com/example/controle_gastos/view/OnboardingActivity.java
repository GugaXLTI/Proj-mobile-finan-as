package com.example.controle_gastos.view;

import android.content.Context;
import android.content.Intent;
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
import com.example.controle_gastos.database.AppDatabase; // ⭐ IMPORT ADICIONADO
import com.example.controle_gastos.model.Conta; // ⭐ IMPORT ADICIONADO
import com.example.controle_gastos.utils.OnboardingStore;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    public static final String EXTRA_EDITANDO = "editando_renda";

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private static final int PG_WELCOME = 0;
    private static final int PG_SALDO = 1;
    private static final int PG_PERMISSAO = 2;

    private ViewFlipper viewFlipper;
    private MaterialButton btnAvancar;
    private ImageView btnVoltar;
    private LinearLayout llProgress;
    private TextView tvPasso;

    private EditText editSaldoInicial;
    private TextView tvStatusPermissao;
    private ImageView ivStatusPermissao;
    private MaterialButton btnAutorizar;

    private double saldoInicial = 0;
    private boolean editando = false;

    private SessionManager session;
    private AppDatabase db; // ⭐ Variável do banco de dados

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        session = new SessionManager(this);
        db = AppDatabase.getInstance(this); // ⭐ Inicializa o banco
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
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_permissoes, viewFlipper, false));    // 2

        setupSaldo();
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

        // ⭐ CORREÇÃO: Tenta pegar do Banco (Room) primeiro, depois do OnboardingStore
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

            // Salva no SharedPreferences (lógica antiga)
            OnboardingStore.salvarSaldoInicial(this, session.getUserId(), saldoInicial);

            // ⭐ CORREÇÃO: Salva no Banco de Dados (Room) para a InicioActivity ler
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
                ir(PG_PERMISSAO);
            }
        } else {
            concluirOnboarding();
        }
    }

    private boolean voltarFluxo() {
        int atual = viewFlipper.getDisplayedChild();
        if (atual == PG_PERMISSAO) {
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
            int atual = (pagina == PG_SALDO) ? 1 : 2;
            montarProgresso(2, atual);
            tvPasso.setText("Passo " + atual + " de 2");
        }

        if (pagina == PG_WELCOME) {
            btnAvancar.setText("Começar");
        } else if (pagina == PG_SALDO) {
            btnAvancar.setText(editando ? "Salvar" : "Continuar");
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