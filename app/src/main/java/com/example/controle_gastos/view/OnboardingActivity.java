package com.example.controle_gastos.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Renda;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    /** Quando true, a tela é aberta pelas Configurações para refazer a renda. */
    public static final String EXTRA_EDITANDO = "editando_renda";

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private static final int PG_WELCOME = 0;
    private static final int PG_PERFIL = 1;
    private static final int PG_ADIANT = 2;
    private static final int PG_VALORES = 3;

    private static final String[] PERIODOS_CHIP = {"Começo", "Meio", "Fim", "Varia muito"};
    private static final String[] PERIODOS_DESC = {"começo do mês", "meio do mês", "fim do mês", "sem data fixa"};
    private static final String[] PERIODOS_DETALHE = {
            "costuma entrar no começo do mês", "costuma entrar no meio do mês",
            "costuma entrar no fim do mês", "sem data fixa"};

    private ViewFlipper viewFlipper;
    private MaterialButton btnAvancar;
    private ImageView btnVoltar;
    private LinearLayout llProgress;
    private TextView tvPasso;

    private View cardClt, cardAuto, cardOutros, cardTemAdiant, cardNaoAdiant;

    private View groupModo, groupAdiantamento, groupDiaSalario, groupPeriodo;
    private TextView segFixo, segVaria, labelSalario, tvInfoValor;
    private TextView tvResumoTitulo, tvResumoValor, tvResumoDetalhe;
    private EditText editValorAdiantamento, editValorSalario;
    private ViewGroup chipsPeriodo;
    private final List<TextView> chipsPeriodoViews = new ArrayList<>();
    private SeletorDia seletorAdiant, seletorSalario;

    private String perfilSelecionado = "";
    private String perfilConfigurado = "";
    private boolean temAdiantamento = false;
    private boolean perfilEscolhido = false;
    private boolean adiantEscolhido = false;
    private boolean rendaFixa = true;
    private int periodoIdx = -1;
    private boolean editando = false;

    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);
        editando = getIntent().getBooleanExtra(EXTRA_EDITANDO, false);

        viewFlipper = findViewById(R.id.viewFlipperOnboarding);
        btnAvancar = findViewById(R.id.btnAvancarOnb);
        btnVoltar = findViewById(R.id.btnVoltarOnb);
        llProgress = findViewById(R.id.llProgressOnb);
        tvPasso = findViewById(R.id.tvPassoOnb);

        viewFlipper.setInAnimation(this, android.R.anim.fade_in);
        viewFlipper.setOutAnimation(this, android.R.anim.fade_out);

        LayoutInflater inflater = getLayoutInflater();
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_welcome, viewFlipper, false)); // 0
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_profile, viewFlipper, false)); // 1
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_advance, viewFlipper, false)); // 2
        viewFlipper.addView(inflater.inflate(R.layout.item_onb_values, viewFlipper, false));  // 3

        setupPerfil();
        setupAdiantamento();
        setupValores();

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

        ir(editando ? PG_PERFIL : PG_WELCOME);
    }

    // ------------------------------------------------------------------
    // SETUP DAS PÁGINAS
    // ------------------------------------------------------------------
    private void setupPerfil() {
        View page = viewFlipper.getChildAt(PG_PERFIL);
        cardClt = page.findViewById(R.id.cardPerfilClt);
        cardAuto = page.findViewById(R.id.cardPerfilAuto);
        cardOutros = page.findViewById(R.id.cardPerfilOutros);

        View.OnClickListener l = v -> {
            cardClt.setSelected(false);
            cardAuto.setSelected(false);
            cardOutros.setSelected(false);
            v.setSelected(true);

            if (v == cardClt) perfilSelecionado = "CLT";
            else if (v == cardAuto) perfilSelecionado = "AUTONOMO";
            else perfilSelecionado = "OUTROS";

            perfilEscolhido = true;
            atualizarHeader();
            atualizarBotao();
        };
        cardClt.setOnClickListener(l);
        cardAuto.setOnClickListener(l);
        cardOutros.setOnClickListener(l);
    }

    private void setupAdiantamento() {
        View page = viewFlipper.getChildAt(PG_ADIANT);
        cardTemAdiant = page.findViewById(R.id.cardTemAdiantamento);
        cardNaoAdiant = page.findViewById(R.id.cardNaoTemAdiantamento);

        View.OnClickListener l = v -> {
            cardTemAdiant.setSelected(false);
            cardNaoAdiant.setSelected(false);
            v.setSelected(true);
            temAdiantamento = (v == cardTemAdiant);
            adiantEscolhido = true;
            atualizarBotao();
        };
        cardTemAdiant.setOnClickListener(l);
        cardNaoAdiant.setOnClickListener(l);
    }

    private void setupValores() {
        View page = viewFlipper.getChildAt(PG_VALORES);

        groupModo = page.findViewById(R.id.groupModo);
        groupAdiantamento = page.findViewById(R.id.groupAdiantamento);
        groupDiaSalario = page.findViewById(R.id.groupDiaSalario);
        groupPeriodo = page.findViewById(R.id.groupPeriodo);
        segFixo = page.findViewById(R.id.segFixo);
        segVaria = page.findViewById(R.id.segVaria);
        labelSalario = page.findViewById(R.id.labelSalario);
        tvInfoValor = page.findViewById(R.id.tvInfoValor);
        tvResumoTitulo = page.findViewById(R.id.tvResumoTitulo);
        tvResumoValor = page.findViewById(R.id.tvResumoValor);
        tvResumoDetalhe = page.findViewById(R.id.tvResumoDetalhe);
        editValorAdiantamento = page.findViewById(R.id.editValorAdiantamento);
        editValorSalario = page.findViewById(R.id.editValorSalario);
        chipsPeriodo = page.findViewById(R.id.chipsDiaAdiantamento).getParent() == null
                ? null : (ViewGroup) page.findViewById(R.id.chipsPeriodo);

        ColorStateList coresSeg = coresSelecionavel("#34D399", "#94A3B8");
        segFixo.setTextColor(coresSeg);
        segVaria.setTextColor(coresSeg);
        segFixo.setOnClickListener(v -> aplicarModo(true));
        segVaria.setOnClickListener(v -> aplicarModo(false));

        seletorAdiant = new SeletorDia(
                (ViewGroup) page.findViewById(R.id.chipsDiaAdiantamento),
                (EditText) page.findViewById(R.id.editDiaAdiantamento),
                new int[]{5, 10, 15, 20});

        seletorSalario = new SeletorDia(
                (ViewGroup) page.findViewById(R.id.chipsDiaSalario),
                (EditText) page.findViewById(R.id.editDiaSalario),
                new int[]{5, 10, 15, 20, 25, 30});

        for (int i = 0; i < PERIODOS_CHIP.length; i++) {
            final int idx = i;
            TextView chip = criarChip(PERIODOS_CHIP[i]);
            chip.setOnClickListener(v -> {
                periodoIdx = idx;
                for (int k = 0; k < chipsPeriodoViews.size(); k++) {
                    chipsPeriodoViews.get(k).setSelected(k == idx);
                }
                atualizarResumo();
                atualizarBotao();
            });
            chipsPeriodoViews.add(chip);
            chipsPeriodo.addView(chip);
        }

        configurarMascaraValor(editValorAdiantamento);
        configurarMascaraValor(editValorSalario);
    }

    // ------------------------------------------------------------------
    // NAVEGAÇÃO
    // ------------------------------------------------------------------
    private void avancarFluxo() {
        int atual = viewFlipper.getDisplayedChild();

        if (atual == PG_WELCOME) {
            ir(PG_PERFIL);
        } else if (atual == PG_PERFIL) {
            if (!perfilEscolhido) {
                Toast.makeText(this, "Selecione uma opção para continuar.", Toast.LENGTH_SHORT).show();
                return;
            }
            if ("CLT".equals(perfilSelecionado)) {
                ir(PG_ADIANT);
            } else {
                configurarValores();
                ir(PG_VALORES);
            }
        } else if (atual == PG_ADIANT) {
            if (!adiantEscolhido) {
                Toast.makeText(this, "Informe se recebe adiantamento.", Toast.LENGTH_SHORT).show();
                return;
            }
            configurarValores();
            ir(PG_VALORES);
        } else {
            salvarOnboarding();
        }
    }

    private boolean voltarFluxo() {
        int atual = viewFlipper.getDisplayedChild();
        if (atual == PG_VALORES) {
            ir("CLT".equals(perfilSelecionado) ? PG_ADIANT : PG_PERFIL);
            return true;
        }
        if (atual == PG_ADIANT) {
            ir(PG_PERFIL);
            return true;
        }
        if (atual == PG_PERFIL && !editando) {
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
        llProgress.setVisibility(welcome ? View.INVISIBLE : View.VISIBLE);
        tvPasso.setVisibility(welcome ? View.INVISIBLE : View.VISIBLE);

        if (!welcome) {
            boolean clt = perfilSelecionado.isEmpty() || "CLT".equals(perfilSelecionado);
            int total = clt ? 3 : 2;
            int atual;
            if (pagina == PG_PERFIL) atual = 1;
            else if (pagina == PG_ADIANT) atual = 2;
            else atual = clt ? 3 : 2;

            montarProgresso(total, atual);
            tvPasso.setText("Passo " + atual + " de " + total);
        }

        if (pagina == PG_WELCOME) btnAvancar.setText("Começar");
        else if (pagina == PG_VALORES) btnAvancar.setText(editando ? "Salvar" : "Concluir");
        else btnAvancar.setText("Continuar");
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
        boolean ok;
        switch (pagina) {
            case PG_PERFIL:
                ok = perfilEscolhido;
                break;
            case PG_ADIANT:
                ok = adiantEscolhido;
                break;
            case PG_VALORES:
                ok = valoresValidos();
                break;
            default:
                ok = true;
        }
        btnAvancar.setEnabled(ok);
        btnAvancar.setAlpha(ok ? 1f : 0.4f);
    }

    // ------------------------------------------------------------------
    // TELA DE VALORES
    // ------------------------------------------------------------------
    private void configurarValores() {
        boolean clt = "CLT".equals(perfilSelecionado);

        if (!perfilSelecionado.equals(perfilConfigurado)) {
            perfilConfigurado = perfilSelecionado;
            periodoIdx = -1;
            for (TextView c : chipsPeriodoViews) c.setSelected(false);

            if (clt) {
                seletorSalario.selecionarPorValor(30);
                seletorAdiant.selecionarPorValor(15);
            } else {
                seletorSalario.limpar();
                // Autônomo costuma variar; os demais (aposentado, pró-labore) costumam ter dia fixo
                rendaFixa = !"AUTONOMO".equals(perfilSelecionado);
            }
        }

        groupModo.setVisibility(clt ? View.GONE : View.VISIBLE);

        if (clt) {
            rendaFixa = true;
            labelSalario.setText(temAdiantamento ? "Salário líquido (fim do mês)" : "Salário líquido");
            tvInfoValor.setText("Informe o valor líquido, já descontados INSS, VT e demais descontos.");
            groupAdiantamento.setVisibility(temAdiantamento ? View.VISIBLE : View.GONE);
            groupDiaSalario.setVisibility(View.VISIBLE);
            groupPeriodo.setVisibility(View.GONE);
        } else {
            tvInfoValor.setText("Informe o valor líquido, o que realmente cai na sua conta.");
            groupAdiantamento.setVisibility(View.GONE);
            aplicarModo(rendaFixa);
        }

        atualizarResumo();
    }

    private void aplicarModo(boolean fixa) {
        rendaFixa = fixa;
        segFixo.setSelected(fixa);
        segVaria.setSelected(!fixa);
        labelSalario.setText(fixa ? "Valor mensal líquido" : "Renda média mensal");
        groupDiaSalario.setVisibility(fixa ? View.VISIBLE : View.GONE);
        groupPeriodo.setVisibility(fixa ? View.GONE : View.VISIBLE);
        atualizarResumo();
        atualizarBotao();
    }

    private boolean valoresValidos() {
        if (lerValor(editValorSalario) <= 0) return false;

        if ("CLT".equals(perfilSelecionado)) {
            if (!seletorSalario.valido()) return false;
            if (temAdiantamento) {
                return lerValor(editValorAdiantamento) > 0 && seletorAdiant.valido();
            }
            return true;
        }
        if (rendaFixa) return seletorSalario.valido();
        return periodoIdx >= 0;
    }

    private void atualizarResumo() {
        if (seletorSalario == null || tvResumoValor == null) return;

        boolean clt = "CLT".equals(perfilSelecionado);
        double salario = lerValor(editValorSalario);
        double adiant = (clt && temAdiantamento) ? lerValor(editValorAdiantamento) : 0;
        double total = salario + adiant;

        tvResumoValor.setText(moeda(total));
        tvResumoValor.setTextColor(Color.parseColor(total > 0 ? "#34D399" : "#64748B"));

        if (clt) {
            tvResumoTitulo.setText("VOCÊ RECEBE");
            if (temAdiantamento) {
                tvResumoDetalhe.setText("por mês · dia " + diaTxt(seletorAdiant.getDia())
                        + " e dia " + diaTxt(seletorSalario.getDia()));
            } else {
                tvResumoDetalhe.setText("por mês · dia " + diaTxt(seletorSalario.getDia()));
            }
        } else if (rendaFixa) {
            tvResumoTitulo.setText("VOCÊ RECEBE");
            tvResumoDetalhe.setText("por mês · todo dia " + diaTxt(seletorSalario.getDia()));
        } else {
            tvResumoTitulo.setText("RENDA MÉDIA ESTIMADA");
            tvResumoDetalhe.setText(periodoIdx >= 0
                    ? "por mês · " + PERIODOS_DETALHE[periodoIdx]
                    : "por mês · escolha quando costuma entrar");
        }
    }

    private String diaTxt(int dia) {
        return dia > 0 ? String.valueOf(dia) : "—";
    }

    // ------------------------------------------------------------------
    // SALVAR
    // ------------------------------------------------------------------
    private void salvarOnboarding() {
        if (!valoresValidos()) {
            Toast.makeText(this, "Preencha os valores para continuar.", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario user = db.usuarioDao().buscarPorId(session.getUserId());
        if (user == null) {
            Toast.makeText(this, "Usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        boolean clt = "CLT".equals(perfilSelecionado);
        double salario = lerValor(editValorSalario);
        double totalMensal = salario;
        List<Renda> novas = new ArrayList<>();

        if (clt) {
            if (temAdiantamento) {
                double adiant = lerValor(editValorAdiantamento);
                totalMensal += adiant;
                novas.add(new Renda(user.getId(), "Adiantamento", adiant, seletorAdiant.getDia(), "ADIANTAMENTO"));
            }
            novas.add(new Renda(user.getId(), "Salário", salario, seletorSalario.getDia(), "SALARIO"));
        } else if (rendaFixa) {
            novas.add(new Renda(user.getId(), "Renda mensal", salario, seletorSalario.getDia(), "SALARIO"));
        } else {
            // Renda variável: sem dia fixo. O dia 1 só mantém a coluna preenchida.
            novas.add(new Renda(user.getId(), "Renda variável · " + PERIODOS_DESC[periodoIdx],
                    salario, 1, "VARIAVEL"));
        }

        user.setPerfilFinanceiro(perfilSelecionado);
        user.setSalarioMensal(totalMensal);
        db.usuarioDao().atualizar(user);

        // Só apaga as rendas antigas depois de validar tudo
        if (editando) db.rendaDao().deletarTodosDoUsuario(user.getId());
        for (Renda r : novas) db.rendaDao().inserir(r);

        session.setOnboardingConcluido(true);

        if (editando) {
            Toast.makeText(this, "Renda atualizada!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Tudo pronto! Vamos começar.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, InicioActivity.class));
            finish();
        }
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
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
                atualizarResumo();
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

    private ColorStateList coresSelecionavel(String selecionada, String normal) {
        return new ColorStateList(
                new int[][]{{android.R.attr.state_selected}, {}},
                new int[]{Color.parseColor(selecionada), Color.parseColor(normal)});
    }

    private TextView criarChip(String texto) {
        TextView t = new TextView(this);
        t.setText(texto);
        t.setTextSize(14f);
        t.setGravity(Gravity.CENTER);
        t.setTextColor(coresSelecionavel("#34D399", "#C7CEDB"));
        t.setBackgroundResource(R.drawable.bg_onb_chip);
        t.setMinWidth(dp(56));
        t.setPadding(dp(18), 0, dp(18), 0);
        t.setClickable(true);
        t.setFocusable(true);
        t.setLayoutParams(new ChipGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(44)));
        return t;
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

    /** Linha de chips com os dias mais comuns + "Outro" (campo livre de 1 a 31). */
    private class SeletorDia {
        private final EditText editOutro;
        private final List<TextView> chips = new ArrayList<>();
        private int dia = 0;
        private boolean outro = false;

        SeletorDia(ViewGroup grupo, EditText editOutro, int[] opcoes) {
            this.editOutro = editOutro;

            for (final int d : opcoes) {
                final TextView chip = criarChip(String.valueOf(d));
                chip.setOnClickListener(v -> selecionar(chip, d, false));
                chips.add(chip);
                grupo.addView(chip);
            }

            final TextView chipOutro = criarChip("Outro");
            chipOutro.setOnClickListener(v -> selecionar(chipOutro, 0, true));
            chips.add(chipOutro);
            grupo.addView(chipOutro);

            editOutro.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
                @Override
                public void afterTextChanged(Editable s) {
                    atualizarResumo();
                    atualizarBotao();
                }
            });
        }

        void selecionar(TextView escolhido, int d, boolean ehOutro) {
            for (TextView c : chips) c.setSelected(c == escolhido);
            dia = d;
            outro = ehOutro;
            editOutro.setVisibility(ehOutro ? View.VISIBLE : View.GONE);
            if (ehOutro) editOutro.requestFocus();
            atualizarResumo();
            atualizarBotao();
        }

        void selecionarPorValor(int d) {
            for (TextView c : chips) {
                if (c.getText().toString().equals(String.valueOf(d))) {
                    selecionar(c, d, false);
                    return;
                }
            }
        }

        void limpar() {
            for (TextView c : chips) c.setSelected(false);
            dia = 0;
            outro = false;
            editOutro.setText("");
            editOutro.setVisibility(View.GONE);
        }

        int getDia() {
            if (outro) {
                String s = editOutro.getText().toString().trim();
                if (s.isEmpty()) return 0;
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return dia;
        }

        boolean valido() {
            int d = getDia();
            return d >= 1 && d <= 31;
        }
    }
}