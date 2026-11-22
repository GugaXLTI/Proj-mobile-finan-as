package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.BancoBottomSheetAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.ParsedReceipt;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.BancoHelper;
import com.example.controle_gastos.utils.BottomNavHelper;
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.ReceiptAiParser;
import com.example.controle_gastos.utils.ReceiptCacheHelper;
import com.example.controle_gastos.utils.ReceiptImageLoader;
import com.example.controle_gastos.utils.ReceiptOcr;
import com.example.controle_gastos.utils.ReceiptParser;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CadastroDividaActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final int MAX_PARCELAS = 24;

    private ChipGroup chipGroupTipo, chipGroupParcelas, chipGroupCategoria;
    private Chip chipMaisParcelas;
    private int parcelasPersonalizadas = 7;

    private EditText editDescricao, editValor, editDataCompra, editVencimento;
    private MaterialButton btnSalvar;
    private LinearLayout btnImportarComprovante;
    private TextView tvComprovante, tvTituloTela;
    private ImageView ivComprovante;

    private LinearLayout containerToggle, containerVencimento;
    private TextView btnAVista, btnParcelado, tvDicaVencimento;
    private boolean isParceladoAtivo = false;

    // Data da compra: Hoje / Ontem / Outra
    private TextView boxDataCompraHoje, boxDataCompraOntem, tvChipOutra, tvBadgeDataCompra;
    private LinearLayout boxDataCompraOutra;

    private LinearLayout boxSelecionarBanco, containerVazio, containerSelecao;
    private TextView btnNovoItem, btnNovaCategoria, tvTituloSelecao, tvResumoParcelas, tvSemCategoria, tvBancoSelecionado;
    private ImageView ivBancoSelecionadoLogo, ivBancoPlaceholder;
    private TextView tvBancoSelecionadoIniciais;
    private Button btnCadastrarAgora;
    private LinearLayout boxVencimento, containerParcelas;

    private View overlaySucesso;
    private MaterialButton btnConcluirSucesso;
    private LinearLayout llResumoSucesso;
    private TextView tvTituloSucesso;
    private final List<String[]> linhasResumo = new ArrayList<>();

    private AppDatabase db;
    private SessionManager session;

    private int dividaId = -1;
    private Divida dividaEmEdicao = null;
    private List<Categoria> categoriasDoBanco = new ArrayList<>();
    private List<String[]> itensSelecao = new ArrayList<>();
    private String[] bancoSelecionadoAtual = null;

    private final ActivityResultLauncher<String[]> pickComprovanteLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                processarComprovanteEscolhido(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_divida);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        BottomNavHelper.configurar(this, BottomNavHelper.LANCAR);

        CategoriaSeeder.popularSeVazio(this, session.getUserId());

        chipGroupTipo = findViewById(R.id.chipGroupTipo);
        chipGroupParcelas = findViewById(R.id.chipGroupParcelas);
        chipGroupCategoria = findViewById(R.id.chipGroupCategoria);
        editDescricao = findViewById(R.id.editDescricao);
        editValor = findViewById(R.id.editValor);
        editDataCompra = findViewById(R.id.editDataCompra);
        editVencimento = findViewById(R.id.editVencimento);
        btnSalvar = findViewById(R.id.btnSalvarDivida);
        btnImportarComprovante = findViewById(R.id.btnImportarComprovante);
        tvComprovante = findViewById(R.id.tvComprovante);
        ivComprovante = findViewById(R.id.ivComprovante);
        tvTituloTela = findViewById(R.id.tvTituloTela);

        containerToggle = findViewById(R.id.containerToggle);
        btnAVista = findViewById(R.id.btnAVista);
        btnParcelado = findViewById(R.id.btnParcelado);

        boxDataCompraHoje = findViewById(R.id.boxDataCompraHoje);
        boxDataCompraOntem = findViewById(R.id.boxDataCompraOntem);
        boxDataCompraOutra = findViewById(R.id.boxDataCompraOutra);
        tvChipOutra = findViewById(R.id.tvChipOutra);
        tvBadgeDataCompra = findViewById(R.id.tvBadgeDataCompra);

        containerVazio = findViewById(R.id.containerVazio);
        containerSelecao = findViewById(R.id.containerSelecao);
        btnNovoItem = findViewById(R.id.btnNovoItem);
        btnNovaCategoria = findViewById(R.id.btnNovaCategoria);
        tvTituloSelecao = findViewById(R.id.tvTituloSelecao);
        btnCadastrarAgora = findViewById(R.id.btnCadastrarAgora);
        tvResumoParcelas = findViewById(R.id.tvResumoParcelas);
        tvSemCategoria = findViewById(R.id.tvSemCategoria);

        containerVencimento = findViewById(R.id.containerVencimento);
        boxVencimento = findViewById(R.id.boxVencimento);
        containerParcelas = findViewById(R.id.containerParcelas);
        tvDicaVencimento = findViewById(R.id.tvDicaVencimento);

        boxSelecionarBanco = findViewById(R.id.boxSelecionarBanco);
        ivBancoSelecionadoLogo = findViewById(R.id.ivBancoSelecionadoLogo);
        ivBancoPlaceholder = findViewById(R.id.ivBancoPlaceholder);
        tvBancoSelecionadoIniciais = findViewById(R.id.tvBancoSelecionadoIniciais);
        tvBancoSelecionado = findViewById(R.id.tvBancoSelecionado);

        overlaySucesso = findViewById(R.id.overlaySucesso);
        llResumoSucesso = findViewById(R.id.llResumoSucesso);
        tvTituloSucesso = findViewById(R.id.tvTituloSucesso);
        btnConcluirSucesso = findViewById(R.id.btnConcluirSucesso);
        btnConcluirSucesso.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroDividaActivity.this, InicioActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        configurarChipGroups();
        configurarDatePicker(boxDataCompraOutra, editDataCompra);
        configurarDatePicker(boxVencimento, editVencimento);
        configurarMascaraValor(editValor);

        btnNovaCategoria.setOnClickListener(v -> startActivity(new Intent(this, CategoriasActivity.class)));
        btnNovoItem.setOnClickListener(v -> startActivity(new Intent(this, CartoesActivity.class)));
        btnCadastrarAgora.setOnClickListener(v -> startActivity(new Intent(this, CartoesActivity.class)));
        btnImportarComprovante.setOnClickListener(v -> pickComprovanteLauncher.launch(new String[]{"image/*", "application/pdf"}));
        boxSelecionarBanco.setOnClickListener(v -> abrirBottomSheetBancos());

        chipGroupTipo.setOnCheckedStateChangeListener((group, checkedIds) -> atualizarSelecao());
        chipGroupParcelas.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (chipMaisParcelas != null
                    && (checkedIds.isEmpty() || checkedIds.get(0) != chipMaisParcelas.getId())
                    && chipMaisParcelas.isCheckable()) {
                chipMaisParcelas.setCheckable(false);
                chipMaisParcelas.setText("Mais");
            }
            atualizarResumoParcelas();
        });

        btnAVista.setOnClickListener(v -> {
            isParceladoAtivo = false;
            atualizarEstiloToggle();
            atualizarCamposPorTipo();
        });
        btnParcelado.setOnClickListener(v -> {
            isParceladoAtivo = true;
            atualizarEstiloToggle();
            atualizarCamposPorTipo();
            atualizarResumoParcelas();
        });

        editDataCompra.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { sincronizarChipsData(); }
        });
        boxDataCompraHoje.setOnClickListener(v -> editDataCompra.setText(getHoje()));
        boxDataCompraOntem.setOnClickListener(v -> editDataCompra.setText(getOntem()));

        atualizarEstiloToggle();
        editDataCompra.setText(getHoje());

        atualizarSelecao();

        editDescricao.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().toLowerCase();
                if (texto.length() < 3 || itensSelecao == null || itensSelecao.isEmpty()) return;

                for (String[] item : itensSelecao) {
                    String nomeBanco = item[2].toLowerCase();
                    if (texto.contains(nomeBanco) || (nomeBanco.contains("nubank") && texto.contains("nu"))) {
                        selecionarBancoNaTela(item);
                        break;
                    }
                }
            }
        });

        if (getIntent().hasExtra("divida_id")) {
            dividaId = getIntent().getIntExtra("divida_id", -1);
            if (dividaId != -1) {
                dividaEmEdicao = db.dividaDao().buscarPorId(dividaId);
                if (dividaEmEdicao != null) {
                    preencherCampos(dividaEmEdicao);
                    btnSalvar.setText("Atualizar dívida");
                    if (tvTituloTela != null) tvTituloTela.setText("Editar lançamento");
                    btnImportarComprovante.setVisibility(View.GONE);
                }
            }
        }

        if (getIntent().getBooleanExtra("extra_prefill_receipt", false)) {
            ParsedReceipt parsed = lerParsedDoIntent(getIntent());
            aplicarParsedReceipt(parsed);
        }

        btnSalvar.setOnClickListener(v -> salvarDivida());
    }

    private void atualizarEstiloToggle() {
        estilizarToggle(btnAVista, !isParceladoAtivo);
        estilizarToggle(btnParcelado, isParceladoAtivo);
    }

    private void estilizarToggle(TextView t, boolean ativo) {
        if (ativo) {
            t.setBackgroundResource(R.drawable.bg_toggle_ativo);
            t.setTextColor(Color.parseColor("#042F2E"));
            t.setTypeface(null, Typeface.BOLD);
        } else {
            t.setBackground(null);
            t.setTextColor(Color.parseColor("#94A3B8"));
            t.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void sincronizarChipsData() {
        String data = editDataCompra.getText().toString().trim();
        boolean ehHoje = data.equals(getHoje());
        boolean ehOntem = data.equals(getOntem());
        boolean ehOutra = !data.isEmpty() && !ehHoje && !ehOntem;

        estilizarChipData(boxDataCompraHoje, boxDataCompraHoje, ehHoje);
        estilizarChipData(boxDataCompraOntem, boxDataCompraOntem, ehOntem);
        estilizarChipData(boxDataCompraOutra, tvChipOutra, ehOutra);

        if (data.isEmpty()) tvBadgeDataCompra.setText("Selecione a data");
        else if (ehHoje) tvBadgeDataCompra.setText("Hoje • " + data);
        else if (ehOntem) tvBadgeDataCompra.setText("Ontem • " + data);
        else tvBadgeDataCompra.setText(data);
    }

    private void estilizarChipData(View caixa, TextView rotulo, boolean selecionado) {
        caixa.setSelected(selecionado);
        rotulo.setTextColor(Color.parseColor(selecionado ? "#34D399" : "#CBD5E1"));
        rotulo.setTypeface(null, selecionado ? Typeface.BOLD : Typeface.NORMAL);
    }

    private void abrirBottomSheetBancos() {
        if (itensSelecao == null || itensSelecao.isEmpty()) return;

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_selecao_banco, null);
        bottomSheetDialog.setContentView(view);
        deixarFundoDaGavetaTransparente(view);

        TextView tvTituloBS = view.findViewById(R.id.tvTituloBottomSheet);
        tvTituloBS.setText(tvTituloSelecao.getText().toString());

        RecyclerView rv = view.findViewById(R.id.rvBancosBottomSheet);
        rv.setLayoutManager(new LinearLayoutManager(this));

        BancoBottomSheetAdapter adapter = new BancoBottomSheetAdapter(itensSelecao, (banco, position) -> {
            selecionarBancoNaTela(banco);
            bottomSheetDialog.dismiss();
        });
        rv.setAdapter(adapter);

        bottomSheetDialog.show();
    }

    private void deixarFundoDaGavetaTransparente(View conteudo) {
        if (conteudo.getParent() instanceof View) {
            ((View) conteudo.getParent()).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void selecionarBancoNaTela(String[] banco) {
        bancoSelecionadoAtual = banco;
        ivBancoPlaceholder.setVisibility(View.GONE);

        String nome = banco[2];
        String detalhe = banco[3];
        SpannableStringBuilder sb = new SpannableStringBuilder(nome);
        sb.setSpan(new StyleSpan(Typeface.BOLD), 0, nome.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (detalhe != null && !detalhe.isEmpty()) {
            int inicio = sb.length() + 1;
            sb.append("\n").append(detalhe);
            sb.setSpan(new ForegroundColorSpan(Color.parseColor("#94A3B8")), inicio, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new RelativeSizeSpan(0.82f), inicio, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tvBancoSelecionado.setText(sb);
        tvBancoSelecionado.setTextColor(Color.WHITE);

        int logoResId = BancoHelper.getLogoBanco(banco[2]);
        if (logoResId != 0) {
            ivBancoSelecionadoLogo.setImageResource(logoResId);
            ivBancoSelecionadoLogo.setVisibility(View.VISIBLE);
            tvBancoSelecionadoIniciais.setVisibility(View.GONE);
        } else {
            ivBancoSelecionadoLogo.setVisibility(View.GONE);
            tvBancoSelecionadoIniciais.setVisibility(View.VISIBLE);

            tvBancoSelecionadoIniciais.setText(banco[0]);

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(BancoHelper.getCorBanco(banco[2]));
            tvBancoSelecionadoIniciais.setBackground(shape);
        }
    }

    private void processarComprovanteEscolhido(Uri uri) {
        btnImportarComprovante.setEnabled(false);
        tvComprovante.setText("Analisando...");
        ivComprovante.setVisibility(View.GONE);
        String mime = getContentResolver().getType(uri);
        final int userId = session.getUserId();

        new Thread(() -> {
            String textoCache = ReceiptCacheHelper.buscarTexto(this, userId, uri);
            if (textoCache != null && !textoCache.isEmpty()) {
                runOnUiThread(() -> processarComIA(textoCache, userId, uri, mime));
                return;
            }
            Bitmap bitmap = (mime != null && mime.equals("application/pdf")) ?
                    ReceiptImageLoader.carregarPdf(this, uri) : ReceiptImageLoader.carregarImagem(this, uri);
            if (bitmap == null) {
                runOnUiThread(() -> {
                    resetarBotaoImportar();
                    Toast.makeText(this, "Não conseguimos abrir esse arquivo.", Toast.LENGTH_LONG).show();
                });
                return;
            }
            ReceiptOcr.extrairTexto(bitmap, new ReceiptOcr.Callback() {
                @Override
                public void onSucesso(String texto) {
                    ReceiptCacheHelper.salvarTexto(CadastroDividaActivity.this, userId, uri, texto, mime);
                    runOnUiThread(() -> processarComIA(texto, userId, uri, mime));
                }
                @Override
                public void onErro(Exception e) {
                    runOnUiThread(() -> {
                        resetarBotaoImportar();
                        Toast.makeText(CadastroDividaActivity.this, "Erro ao analisar.", Toast.LENGTH_LONG).show();
                    });
                }
            });
        }).start();
    }

    private void processarComIA(String texto, int userId, Uri uri, String mime) {
        ReceiptAiParser aiParser = new ReceiptAiParser(this);
        ParsedReceipt parsed = aiParser.extrair(texto);
        if (parsed.getBanco() == null) {
            ParsedReceipt regexFallback = ReceiptParser.parse(texto, session.getNome());
            parsed.setBanco(regexFallback.getBanco());
            parsed.setCategoria(regexFallback.getCategoria());
            parsed.setTitulo(regexFallback.getTitulo());
            parsed.setBeneficiario(regexFallback.getBeneficiario());
        }
        if (parsed.getValor() == null) {
            resetarBotaoImportar();
            Toast.makeText(this, "Nenhum valor encontrado. Verifique a imagem.", Toast.LENGTH_LONG).show();
            return;
        }
        aplicarParsedReceipt(parsed);
        resetarBotaoImportar();
        Toast.makeText(this, "Comprovante importado. Confira e salve.", Toast.LENGTH_LONG).show();
    }

    private void resetarBotaoImportar() {
        btnImportarComprovante.setEnabled(true);
        tvComprovante.setText("Comprovante");
        ivComprovante.setVisibility(View.VISIBLE);
    }

    private void configurarChipGroups() {
        chipGroupTipo.addView(criarChipTipo("Cartão de Crédito", R.drawable.ic_nav_card, "#34D399"));
        chipGroupTipo.addView(criarChipTipo("Cartão de Débito", R.drawable.ic_nav_card, "#38BDF8"));
        chipGroupTipo.addView(criarChipTipo("Pix", R.drawable.ic_pay_pix, "#2DD4BF"));
        chipGroupTipo.addView(criarChipTipo("Dinheiro", R.drawable.ic_conf_money, "#FBBF24"));
        chipGroupTipo.addView(criarChipTipo("Empréstimo", R.drawable.ic_banco, "#A78BFA"));
        chipGroupTipo.addView(criarChipTipo("Fatura", R.drawable.ic_pay_doc, "#F472B6"));
        chipGroupTipo.addView(criarChipTipo("Boleto", R.drawable.ic_pay_barcode, "#94A3B8"));
        if (chipGroupTipo.getChildCount() > 0) { ((Chip) chipGroupTipo.getChildAt(0)).setChecked(true); }

        String[] parcelas = {"1x", "2x", "3x", "4x", "5x", "6x"};
        for (String p : parcelas) { chipGroupParcelas.addView(criarChip(p)); }

        chipMaisParcelas = criarChip("Mais");
        chipMaisParcelas.setCheckable(false);
        chipMaisParcelas.setOnClickListener(v -> abrirBottomSheetParcelas());
        chipGroupParcelas.addView(chipMaisParcelas);

        if (chipGroupParcelas.getChildCount() > 0) { ((Chip) chipGroupParcelas.getChildAt(0)).setChecked(true); }

        carregarCategoriasDoBanco();
    }

    private Chip criarChip(String texto) {
        Chip chip = new Chip(this);
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setClickable(true);
        int[][] states = new int[][] { new int[] {android.R.attr.state_checked}, new int[] {-android.R.attr.state_checked} };
        int[] colors = new int[] { Color.parseColor("#123D33"), Color.parseColor("#111A2E") };
        ColorStateList bgColors = new ColorStateList(states, colors);
        int[] textColors = new int[] { Color.parseColor("#34D399"), Color.parseColor("#E2E8F0") };
        ColorStateList txtColors = new ColorStateList(states, textColors);
        int[] strokeColors = new int[] { Color.parseColor("#10B981"), Color.parseColor("#1E2A44") };
        ColorStateList strkColors = new ColorStateList(states, strokeColors);
        chip.setChipBackgroundColor(bgColors);
        chip.setTextColor(txtColors);
        chip.setChipStrokeColor(strkColors);
        chip.setChipStrokeWidth(dp(1));
        chip.setChipCornerRadius(dp(20));
        chip.setChipMinHeight(dp(40));
        return chip;
    }

    private Chip criarChipTipo(String texto, int iconeRes, String corIcone) {
        Chip chip = criarChip(texto);
        chip.setChipIconResource(iconeRes);
        chip.setChipIconTint(ColorStateList.valueOf(Color.parseColor(corIcone)));
        chip.setChipIconSize(dp(16));
        chip.setChipIconVisible(true);
        chip.setIconStartPadding(dp(8));
        chip.setTextStartPadding(dp(2));
        chip.setCheckedIconVisible(false);
        return chip;
    }

    private Chip criarChipCategoria(String nome, String corHex) {
        Chip chip = criarChip(nome);
        int cor;
        try {
            cor = Color.parseColor(corHex);
        } catch (Exception e) {
            cor = Color.parseColor("#64748B");
        }
        GradientDrawable bolinha = new GradientDrawable();
        bolinha.setShape(GradientDrawable.OVAL);
        bolinha.setColor(cor);

        chip.setChipIcon(bolinha);
        chip.setChipIconTint(null);
        chip.setChipIconSize(dp(10));
        chip.setChipIconVisible(true);
        chip.setIconStartPadding(dp(8));
        chip.setTextStartPadding(dp(2));
        chip.setCheckedIconVisible(false);
        return chip;
    }

    private void carregarCategoriasDoBanco() {
        categoriasDoBanco = db.categoriaDao().listarPorUsuario(session.getUserId());
        chipGroupCategoria.removeAllViews();
        if (categoriasDoBanco.isEmpty()) {
            tvSemCategoria.setVisibility(View.VISIBLE);
        } else {
            tvSemCategoria.setVisibility(View.GONE);
            for (Categoria c : categoriasDoBanco) { chipGroupCategoria.addView(criarChipCategoria(c.getNome(), c.getCor())); }
            if (chipGroupCategoria.getChildCount() > 0) { ((Chip) chipGroupCategoria.getChildAt(0)).setChecked(true); }
        }
    }

    private void abrirBottomSheetParcelas() {
        final BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_parcelas, null);
        sheet.setContentView(view);
        deixarFundoDaGavetaTransparente(view);

        final TextView tvValor = view.findViewById(R.id.tvParcelasValor);
        final TextView tvPreview = view.findViewById(R.id.tvParcelasPreview);
        final int[] atual = {Math.max(2, Math.min(MAX_PARCELAS, parcelasPersonalizadas))};
        final double total = lerValorDigitado();

        final Runnable atualizarTela = () -> {
            tvValor.setText(atual[0] + "x");
            tvPreview.setText("de " + String.format(LOCALE_BR, "R$ %.2f", total / atual[0]) + " · sem juros");
        };
        atualizarTela.run();

        view.findViewById(R.id.btnParcelasMenos).setOnClickListener(v -> {
            if (atual[0] > 2) { atual[0]--; atualizarTela.run(); }
        });
        view.findViewById(R.id.btnParcelasMais).setOnClickListener(v -> {
            if (atual[0] < MAX_PARCELAS) { atual[0]++; atualizarTela.run(); }
        });
        view.findViewById(R.id.btnAplicarParcelas).setOnClickListener(v -> {
            aplicarParcelasPersonalizadas(atual[0]);
            sheet.dismiss();
        });

        sheet.show();
    }

    private void aplicarParcelasPersonalizadas(int n) {
        parcelasPersonalizadas = n;

        if (n <= 6) {
            for (int i = 0; i < chipGroupParcelas.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupParcelas.getChildAt(i);
                if (chip != chipMaisParcelas && chip.getText().toString().equals(n + "x")) {
                    chip.setChecked(true);
                    return;
                }
            }
        }

        chipMaisParcelas.setCheckable(true);
        chipMaisParcelas.setText(n + "x");
        chipMaisParcelas.setChecked(true);
        atualizarResumoParcelas();
    }

    private String tipoSelecionado() {
        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            if (chip.isChecked()) return chip.getText().toString();
        }
        return "";
    }

    // ⭐ AQUI ESTÁ A LÓGICA INTELIGENTE DE EXIBIÇÃO
    private void atualizarCamposPorTipo() {
        String tipo = tipoSelecionado();

        boolean aceitaParcelamento = tipo.equals("Cartão de Crédito") ||
                tipo.equals("Empréstimo") ||
                tipo.equals("Fatura") ||
                tipo.equals("Boleto");

        boolean exigeVencimento = aceitaParcelamento;

        containerToggle.setVisibility(aceitaParcelamento ? View.VISIBLE : View.GONE);

        if (!aceitaParcelamento) {
            isParceladoAtivo = false;
            atualizarEstiloToggle();
        }

        if (isParceladoAtivo) {
            containerParcelas.setVisibility(View.VISIBLE);
            tvResumoParcelas.setVisibility(View.VISIBLE);
        } else {
            containerParcelas.setVisibility(View.GONE);
            tvResumoParcelas.setVisibility(View.GONE);
        }

        if (exigeVencimento) {
            containerVencimento.setVisibility(View.VISIBLE);
            if (tipo.equals("Cartão de Crédito")) {
                tvDicaVencimento.setText(isParceladoAtivo ? "Vencimento da 1ª parcela" : "Fatura do cartão selecionado");
            } else {
                tvDicaVencimento.setText("Data de vencimento");
            }
        } else {
            containerVencimento.setVisibility(View.GONE);
        }
    }

    private void atualizarSelecao() {
        String tipo = tipoSelecionado();
        if (tipo.isEmpty()) return;

        atualizarCamposPorTipo();

        itensSelecao.clear();
        bancoSelecionadoAtual = null;
        containerSelecao.setVisibility(View.VISIBLE);

        tvBancoSelecionado.setText("Toque para selecionar...");
        tvBancoSelecionado.setTextColor(Color.parseColor("#64748B"));
        ivBancoSelecionadoLogo.setVisibility(View.GONE);
        tvBancoSelecionadoIniciais.setVisibility(View.GONE);
        ivBancoPlaceholder.setVisibility(View.VISIBLE);

        if (tipo.equals("Cartão de Crédito") || tipo.equals("Cartão de Débito")) {
            String tipoCartaoFiltro = tipo.equals("Cartão de Crédito") ? "Crédito" : "Débito";
            tvTituloSelecao.setText("Selecione o Cartão");
            List<Cartao> cartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            for (Cartao c : cartoes) {
                if (c.tipo != null && c.tipo.equals(tipoCartaoFiltro)) {
                    String sigla = c.instituicao.length() >= 2 ? c.instituicao.substring(0, 2).toUpperCase() : c.instituicao;
                    String detalhe = (c.ultimos4Digitos != null && !c.ultimos4Digitos.trim().isEmpty())
                            ? "Final •••• " + c.ultimos4Digitos + (c.diaVencimento != null ? " • Vence dia " + c.diaVencimento : "")
                            : tipoCartaoFiltro;
                    itensSelecao.add(new String[]{sigla, c.instituicao, c.instituicao, detalhe});
                }
            }
        } else if (tipo.equals("Pix") || tipo.equals("Dinheiro")) {
            tvTituloSelecao.setText("Selecione o Banco / Origem");
            List<Cartao> cartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            Set<String> instituicoesUnicas = new HashSet<>();
            for (Cartao c : cartoes) {
                if (instituicoesUnicas.add(c.instituicao)) {
                    String sigla = c.instituicao.length() >= 2 ? c.instituicao.substring(0, 2).toUpperCase() : c.instituicao;
                    itensSelecao.add(new String[]{sigla, c.instituicao, c.instituicao, tipo});
                }
            }
        } else {
            tvTituloSelecao.setText("Selecione o Banco / Origem");
            String[] bancosComuns = {"Nubank", "Itaú", "Bradesco", "Santander", "Caixa", "Inter"};
            for (String b : bancosComuns) {
                String sigla = b.length() >= 2 ? b.substring(0, 2).toUpperCase() : b.toUpperCase();
                itensSelecao.add(new String[]{sigla, b, b, ""});
            }
        }

        if (itensSelecao.isEmpty()) {
            boxSelecionarBanco.setVisibility(View.GONE);
            containerVazio.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.tvMensagemVazio)).setText("Nenhum cartão cadastrado.");
            btnCadastrarAgora.setVisibility(View.VISIBLE);
        } else {
            boxSelecionarBanco.setVisibility(View.VISIBLE);
            containerVazio.setVisibility(View.GONE);

            if (itensSelecao.size() == 1) {
                selecionarBancoNaTela(itensSelecao.get(0));
            }
        }
    }

    private void atualizarResumoParcelas() {
        String valorStr = editValor.getText().toString().replace("R$", "").replace(".", "").replace(",", ".").trim();
        if (valorStr.isEmpty()) return;
        double valorTotal;
        try { valorTotal = Double.parseDouble(valorStr); } catch (Exception e) { return; }

        int numParcelas = 1;
        for (int i = 0; i < chipGroupParcelas.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupParcelas.getChildAt(i);
            if (chip.isChecked()) {
                String txt = chip.getText().toString().replace("x", "");
                try { numParcelas = Integer.parseInt(txt); } catch (Exception e) {}
                break;
            }
        }
        double valorParcela = valorTotal / numParcelas;
        String texto = numParcelas + "x de " + String.format(LOCALE_BR, "R$ %.2f", valorParcela);
        texto += numParcelas == 1 ? " · à vista" : " · sem juros";
        tvResumoParcelas.setText(texto);
    }

    private void aplicarParsedReceipt(ParsedReceipt parsed) {
        if (parsed == null) return;

        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            if (chip.getText().toString().equalsIgnoreCase("Pix")) { chip.setChecked(true); break; }
        }

        if (parsed.getTitulo() != null && !parsed.getTitulo().trim().isEmpty()) { editDescricao.setText(parsed.getTitulo().trim()); }
        if (parsed.getValor() != null && parsed.getValor() > 0) {
            String valorFormatado = String.format(LOCALE_BR, "R$ %.2f", parsed.getValor());
            editValor.setText(valorFormatado);
            editValor.setSelection(valorFormatado.length());
        }
        if (parsed.getData() != null && !parsed.getData().trim().isEmpty()) {
            editDataCompra.setText(parsed.getData().trim());
            editVencimento.setText(parsed.getData().trim());
        }
        if (parsed.getCategoria() != null && !parsed.getCategoria().trim().isEmpty()) {
            for (int i = 0; i < chipGroupCategoria.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategoria.getChildAt(i);
                if (chip.getText().toString().equalsIgnoreCase(parsed.getCategoria().trim())) { chip.setChecked(true); break; }
            }
        }
        atualizarResumoParcelas();
    }

    // ⭐ AQUI ESTÁ A LÓGICA INTELIGENTE DE SALVAMENTO
    private void salvarDivida() {
        String tipoDivida = tipoSelecionado();
        if (tipoDivida.isEmpty()) { Toast.makeText(this, "Selecione o tipo.", Toast.LENGTH_SHORT).show(); return; }

        String categoria = "";
        for (int i = 0; i < chipGroupCategoria.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategoria.getChildAt(i);
            if (chip.isChecked()) { categoria = chip.getText().toString(); break; }
        }
        if (categoria.isEmpty()) { Toast.makeText(this, "Selecione uma categoria.", Toast.LENGTH_SHORT).show(); return; }

        String descricao = editDescricao.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim().replace("R$", "").replace(".", "").replace(",", ".").trim();
        String dataCompra = editDataCompra.getText().toString().trim();
        String vencimento = editVencimento.getText().toString().trim();

        if (descricao.isEmpty() || valorStr.isEmpty()) { Toast.makeText(this, "Preencha descrição e valor!", Toast.LENGTH_SHORT).show(); return; }

        double valor;
        try { valor = Double.parseDouble(valorStr); } catch (Exception e) { Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show(); return; }
        if (valor <= 0) { Toast.makeText(this, "O valor deve ser > 0.", Toast.LENGTH_SHORT).show(); return; }

        if (bancoSelecionadoAtual == null) {
            Toast.makeText(this, "Selecione o banco/origem.", Toast.LENGTH_SHORT).show();
            return;
        }

        String bancoSelecionado = bancoSelecionadoAtual[2];
        if (tipoDivida.equals("Pix")) {
            bancoSelecionado = "Pix (" + bancoSelecionadoAtual[2] + ")";
        }

        boolean aceitaParcelamento = tipoDivida.equals("Cartão de Crédito") ||
                tipoDivida.equals("Empréstimo") ||
                tipoDivida.equals("Fatura") ||
                tipoDivida.equals("Boleto");

        String parcelas = "1x (À vista)";

        if (aceitaParcelamento) {
            if (vencimento.isEmpty()) { Toast.makeText(this, "Informe o vencimento.", Toast.LENGTH_SHORT).show(); return; }
            if (isParceladoAtivo) {
                for (int i = 0; i < chipGroupParcelas.getChildCount(); i++) {
                    Chip chip = (Chip) chipGroupParcelas.getChildAt(i);
                    if (chip.isChecked()) { parcelas = chip.getText().toString(); break; }
                }
            }
        } else {
            vencimento = dataCompra.isEmpty() ? getHoje() : dataCompra;
        }

        String condicao = "À vista";
        if (aceitaParcelamento && isParceladoAtivo) {
            int n = 1;
            try { n = Integer.parseInt(parcelas.replace("x", "").trim()); } catch (Exception e) { }
            condicao = n > 1 ? n + "x de " + String.format(LOCALE_BR, "R$ %.2f", valor / n) : "1x";
        }
        prepararResumo(tipoDivida, valor, descricao, categoria, bancoSelecionadoAtual[2],
                condicao, dataCompra, aceitaParcelamento ? vencimento : "");

        if (dividaEmEdicao != null) {
            AlarmeHelper.cancelar(this, dividaEmEdicao);
            dividaEmEdicao.titulo = descricao;
            dividaEmEdicao.valorTotal = valor;
            dividaEmEdicao.valorParcela = valor;
            dividaEmEdicao.banco = bancoSelecionado;
            dividaEmEdicao.categoria = categoria;
            dividaEmEdicao.parcela = (aceitaParcelamento && isParceladoAtivo) ? parcelas : "1x (À vista)";
            dividaEmEdicao.vencimento = vencimento;
            db.dividaDao().atualizar(dividaEmEdicao);
            if (!dividaEmEdicao.isPago()) { agendarAlarmeSeguro(dividaEmEdicao); }
            mostrarTelaSucesso();
            return;
        }

        if (!aceitaParcelamento || !isParceladoAtivo) {
            Divida nova = new Divida(session.getUserId(), descricao, valor, valor, valor, bancoSelecionado, categoria, "1x (À vista)", vencimento, true);
            nova.setDataPagamento(vencimento);
            db.dividaDao().inserir(nova);
            mostrarTelaSucesso();
            return;
        }

        int numParcelas = 1;
        try { numParcelas = Integer.parseInt(parcelas.replace("x", "")); } catch (Exception e) {}

        if (numParcelas == 1) {
            Divida nova = new Divida(session.getUserId(), descricao, valor, valor, 0.0, bancoSelecionado, categoria, "1x", vencimento, false);
            long id = db.dividaDao().inserir(nova);
            if (id > 0) {
                nova.id = (int) id;
                agendarAlarmeSeguro(nova);
            }
            mostrarTelaSucesso();
            return;
        }

        int grupoId = db.dividaDao().maxGrupoId() + 1;
        double valorParcela = valor / numParcelas;
        Calendar cBase = converterParaCalendar(vencimento);
        if (cBase == null) { Toast.makeText(this, "Data inválida!", Toast.LENGTH_SHORT).show(); return; }

        for (int i = 0; i < numParcelas; i++) {
            Calendar cParcela = (Calendar) cBase.clone();
            cParcela.add(Calendar.MONTH, i);
            String vencParcela = String.format(LOCALE_BR, "%02d/%02d/%04d", cParcela.get(Calendar.DAY_OF_MONTH), cParcela.get(Calendar.MONTH) + 1, cParcela.get(Calendar.YEAR));
            String numeroParcela = (i + 1) + "/" + numParcelas;
            Divida parcela = new Divida(session.getUserId(), grupoId, descricao, valorParcela, valorParcela, 0.0, bancoSelecionado, categoria, numeroParcela, vencParcela, false, false);
            long idGerado = db.dividaDao().inserir(parcela);
            if (idGerado > 0) {
                parcela.id = (int) idGerado;
                agendarAlarmeSeguro(parcela);
            }
        }
        mostrarTelaSucesso();
    }

    private void agendarAlarmeSeguro(Divida divida) {
        try {
            AlarmeHelper.agendar(this, divida);
        } catch (Exception e) {
            android.util.Log.e("CadastroDivida", "Erro ao agendar alarme: " + e.getMessage());
        }
    }

    private void prepararResumo(String tipo, double valor, String descricao, String categoria,
                                String banco, String condicao, String dataCompra, String vencimento) {
        linhasResumo.clear();
        linhasResumo.add(new String[]{"Valor total", String.format(LOCALE_BR, "R$ %.2f", valor), "1"});
        linhasResumo.add(new String[]{"Condição", condicao, "0"});
        linhasResumo.add(new String[]{"Descrição", descricao, "0"});
        linhasResumo.add(new String[]{"Pagamento", tipo, "0"});
        linhasResumo.add(new String[]{"Banco", banco, "0"});
        linhasResumo.add(new String[]{"Categoria", categoria, "0"});
        if (dataCompra != null && !dataCompra.isEmpty()) linhasResumo.add(new String[]{"Data da compra", dataCompra, "0"});
        if (vencimento != null && !vencimento.isEmpty()) linhasResumo.add(new String[]{"1º vencimento", vencimento, "0"});
    }

    private void mostrarTelaSucesso() {
        if (overlaySucesso == null) {
            finish();
            return;
        }

        tvTituloSucesso.setText(dividaEmEdicao != null ? "Dívida atualizada!" : "Dívida registrada!");

        llResumoSucesso.removeAllViews();
        for (int i = 0; i < linhasResumo.size(); i++) {
            String[] l = linhasResumo.get(i);
            adicionarLinhaResumo(l[0], l[1], "1".equals(l[2]), i == 0);
        }

        overlaySucesso.setVisibility(View.VISIBLE);
        overlaySucesso.setAlpha(0f);
        overlaySucesso.animate().alpha(1f).setDuration(300).start();
    }

    private void adicionarLinhaResumo(String rotulo, String valor, boolean destaque, boolean primeira) {
        LinearLayout linha = new LinearLayout(this);
        linha.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpLinha = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (!primeira) lpLinha.topMargin = dp(10);
        linha.setLayoutParams(lpLinha);

        TextView tvRotulo = new TextView(this);
        tvRotulo.setText(rotulo);
        tvRotulo.setTextColor(Color.parseColor("#94A3B8"));
        tvRotulo.setTextSize(13f);
        tvRotulo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvValor = new TextView(this);
        tvValor.setText(valor);
        tvValor.setTextColor(Color.parseColor(destaque ? "#34D399" : "#FFFFFF"));
        tvValor.setTextSize(destaque ? 15f : 13f);
        tvValor.setTypeface(null, Typeface.BOLD);
        tvValor.setGravity(Gravity.END);
        tvValor.setMaxLines(2);
        tvValor.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams lpValor = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lpValor.setMarginStart(dp(12));
        tvValor.setLayoutParams(lpValor);

        linha.addView(tvRotulo);
        linha.addView(tvValor);
        llResumoSucesso.addView(linha);
    }

    private void preencherCampos(Divida d) {
        editDescricao.setText(d.getTitulo());
        editValor.setText(String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()));
        editVencimento.setText(d.getVencimento());

        String bancoDivida = d.getBanco() != null ? d.getBanco() : "";
        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            String tipo = chip.getText().toString();

            if (tipo.equals("Pix") && bancoDivida.toLowerCase().contains("pix")) {
                chip.setChecked(true);
                break;
            } else if (!tipo.equals("Pix") && bancoDivida.equalsIgnoreCase(tipo)) {
                chip.setChecked(true);
                break;
            }
        }
        for (int i = 0; i < chipGroupCategoria.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategoria.getChildAt(i);
            if (chip.getText().toString().equals(d.getCategoria())) { chip.setChecked(true); break; }
        }

        boxSelecionarBanco.post(() -> {
            if (bancoDivida.isEmpty()) return;
            for (String[] item : itensSelecao) {
                if (bancoDivida.toLowerCase().contains(item[2].toLowerCase())) {
                    selecionarBancoNaTela(item);
                    break;
                }
            }
        });

        atualizarResumoParcelas();
    }

    private double lerValorDigitado() {
        String s = editValor.getText().toString().replace("R$", "").replace(".", "").replace(",", ".").trim();
        try { return s.isEmpty() ? 0 : Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private String getHoje() {
        Calendar c = Calendar.getInstance();
        return String.format(LOCALE_BR, "%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    private String getOntem() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -1);
        return String.format(LOCALE_BR, "%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
    }

    private Calendar converterParaCalendar(String data) {
        try {
            String[] partes = data.split("/");
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(partes[2]), Integer.parseInt(partes[1]) - 1, Integer.parseInt(partes[0]), 9, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c;
        } catch (Exception e) { return null; }
    }

    private void configurarMascaraValor(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                String text = s.toString().replaceAll("[^0-9]", "");
                if (text.isEmpty()) { editText.setText(""); isUpdating = false; return; }
                if (text.length() > 11) text = text.substring(0, 11);
                double valor = Double.parseDouble(text) / 100.0;
                String formatted = String.format(LOCALE_BR, "R$ %.2f", valor);
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
                atualizarResumoParcelas();
            }
        });
    }

    private void configurarDatePicker(View view, EditText editText) {
        view.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            try {
                String[] partes = editText.getText().toString().split("/");
                calendar.set(Integer.parseInt(partes[2]), Integer.parseInt(partes[1]) - 1, Integer.parseInt(partes[0]));
            } catch (Exception e) { }
            new DatePickerDialog(this, (pickerView, year, month, day) -> {
                String data = String.format(LOCALE_BR, "%02d/%02d/%04d", day, month + 1, year);
                editText.setText(data);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private ParsedReceipt lerParsedDoIntent(Intent i) {
        ParsedReceipt p = new ParsedReceipt();
        p.setTitulo(i.getStringExtra("extra_prefill_titulo"));
        p.setValor(i.getDoubleExtra("extra_prefill_valor", 0.0));
        p.setData(i.getStringExtra("extra_prefill_data"));
        p.setBanco(i.getStringExtra("extra_prefill_banco"));
        p.setBeneficiario(i.getStringExtra("extra_prefill_beneficiario"));
        p.setCategoria(i.getStringExtra("extra_prefill_categoria"));
        p.setParcelas(i.getIntExtra("extra_prefill_parcelas", 1));
        return p;
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}