package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.ChipSelecaoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.ChavePix;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.ParsedReceipt;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.BottomNavHelper;
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.ReceiptCacheHelper;
import com.example.controle_gastos.utils.ReceiptImageLoader;
import com.example.controle_gastos.utils.ReceiptOcr;
import com.example.controle_gastos.utils.ReceiptParser;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CadastroDividaActivity extends AppCompatActivity {

    private static final String TAG = "CadastroDivida";
    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    // Tipos de dívida (mesmos 7 do Spinner antigo, mesma ordem)
    private static final String[] TIPOS = {
            "Cartão de Crédito", "Cartão de Débito", "Pix", "Empréstimo", "Fatura", "Boleto", "Outros"
    };
    private static final int[] PARCELAS_PRESET = {1, 2, 3, 6, 12};
    private static final int PARCELAS_MAX = 60;

    // Cores (mesma paleta da tela Início)
    private static final int COR_VERDE = 0xFF10B981;
    private static final int COR_VERDE_CLARO = 0xFF34D399;
    private static final int COR_CHIP_BG = 0xFF111A2E;
    private static final int COR_CHIP_BG_SEL = 0xFF123D33;
    private static final int COR_BORDA = 0xFF1E2A44;
    private static final int COR_TEXTO_CHIP = 0xFFE2E8F0;

    // Views
    private ChipGroup chipGroupTipo, chipGroupParcelas, chipGroupCategoria;
    private Chip chipOutroParcelas;
    private EditText editDescricao, editValor, editDataCompra, editVencimento;
    private MaterialButton btnSalvar;
    private LinearLayout btnImportarComprovante, containerVazio, containerParcelas, containerVencimento;
    private ImageView ivComprovante, btnVoltar;
    private TextView tvComprovante, tvTituloTela, tvSubtituloTela, tvResumoParcelas;
    private TextView tvTituloSelecao, tvMensagemVazio, btnNovoItem, btnNovaCategoria, tvSemCategoria;
    private View containerListaSelecao, btnAdicionarItem, boxDataCompra, boxVencimento;
    private RecyclerView rvSelecao;
    private Button btnCadastrarAgora;

    private AppDatabase db;
    private SessionManager session;
    private ChipSelecaoAdapter chipAdapter;

    // Estado
    private String tipoSelecionado = TIPOS[0];
    private int parcelasSel = 1;
    private Categoria categoriaSelecionada = null;
    private boolean primeiraVez = true;

    private int dividaId = -1;
    private Divida dividaEmEdicao = null;
    // Na edição: true quando o usuário escolheu cartão/tipo de novo, ou quando o banco original foi reconhecido na lista
    private boolean bancoEdicaoReconhecido = false;

    private List<Categoria> categoriasDoBanco = new ArrayList<>();
    private final List<String[]> itensSelecao = new ArrayList<>();

    private final ActivityResultLauncher<String[]> pickComprovanteLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) {
                    Log.d(TAG, "Seleção de comprovante cancelada.");
                    return;
                }
                Log.d(TAG, "Comprovante selecionado: " + uri);
                processarComprovanteEscolhido(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_divida);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

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
        ivComprovante = findViewById(R.id.ivComprovante);
        tvComprovante = findViewById(R.id.tvComprovante);
        btnVoltar = findViewById(R.id.btnVoltar);
        tvTituloTela = findViewById(R.id.tvTituloTela);
        tvSubtituloTela = findViewById(R.id.tvSubtituloTela);
        tvResumoParcelas = findViewById(R.id.tvResumoParcelas);
        rvSelecao = findViewById(R.id.rvSelecao);
        containerListaSelecao = findViewById(R.id.containerListaSelecao);
        btnAdicionarItem = findViewById(R.id.btnAdicionarItem);
        containerVazio = findViewById(R.id.containerVazio);
        containerParcelas = findViewById(R.id.containerParcelas);
        containerVencimento = findViewById(R.id.containerVencimento);
        boxDataCompra = findViewById(R.id.boxDataCompra);
        boxVencimento = findViewById(R.id.boxVencimento);
        btnNovoItem = findViewById(R.id.btnNovoItem);
        btnNovaCategoria = findViewById(R.id.btnNovaCategoria);
        tvSemCategoria = findViewById(R.id.tvSemCategoria);
        tvTituloSelecao = findViewById(R.id.tvTituloSelecao);
        tvMensagemVazio = findViewById(R.id.tvMensagemVazio);
        btnCadastrarAgora = findViewById(R.id.btnCadastrarAgora);

        BottomNavHelper.configurar(this, BottomNavHelper.LANCAR);

        rvSelecao.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        montarChipsTipo();
        montarChipsParcelas();
        carregarCategoriasDoBanco(null);
        configurarDatePicker(editDataCompra, boxDataCompra);
        configurarDatePicker(editVencimento, boxVencimento);
        configurarMascaraValor(editValor);

        btnVoltar.setOnClickListener(v -> finish());

        btnNovaCategoria.setOnClickListener(v ->
                startActivity(new Intent(this, CategoriasActivity.class)));

        View.OnClickListener abrirCartoes = v ->
                startActivity(new Intent(this, CartoesActivity.class));
        btnNovoItem.setOnClickListener(abrirCartoes);
        btnAdicionarItem.setOnClickListener(abrirCartoes);
        btnCadastrarAgora.setOnClickListener(abrirCartoes);

        btnImportarComprovante.setOnClickListener(v ->
                pickComprovanteLauncher.launch(new String[]{"image/*", "application/pdf"}));

        selecionarTipo(TIPOS[0]);
        selecionarParcelas(1);

        if (getIntent().hasExtra("divida_id")) {
            dividaId = getIntent().getIntExtra("divida_id", -1);
            if (dividaId != -1) {
                dividaEmEdicao = db.dividaDao().buscarPorId(dividaId);
                if (dividaEmEdicao != null) {
                    preencherCampos(dividaEmEdicao);
                    btnSalvar.setText("Atualizar dívida");
                    tvTituloTela.setText("Editar dívida");
                    tvSubtituloTela.setText("Altere os dados e salve");
                    btnImportarComprovante.setVisibility(View.GONE);
                    atualizarSelecao(null);
                    reconhecerBancoNaEdicao(dividaEmEdicao);
                    atualizarResumo();
                }
            }
        }

        if (getIntent().getBooleanExtra("extra_prefill_receipt", false)) {
            ParsedReceipt parsed = lerParsedDoIntent(getIntent());
            aplicarParsedReceipt(parsed);
        }

        btnSalvar.setOnClickListener(v -> salvarDivida());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (primeiraVez) {
            primeiraVez = false;
            return;
        }
        // Voltou de Cartões / Categorias: recarrega mantendo o que estava selecionado
        String nomeCat = categoriaSelecionada != null ? categoriaSelecionada.getNome() : null;
        carregarCategoriasDoBanco(nomeCat);

        String valorBanco = null;
        if (chipAdapter != null && chipAdapter.getItemSelecionado() != null) {
            valorBanco = chipAdapter.getItemSelecionado()[2];
        }
        atualizarSelecao(valorBanco);
    }

    // ============================================================
    // Chips (tipo, parcelas, categoria)
    // ============================================================

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private ColorStateList csl(int checked, int normal) {
        return new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, {}},
                new int[]{checked, normal});
    }

    private Chip criarChip(String texto, Object tag) {
        Chip chip = new Chip(this);
        chip.setId(View.generateViewId());
        chip.setText(texto);
        chip.setTag(tag);
        chip.setCheckable(true);
        chip.setCheckedIconVisible(false);
        chip.setChipIconVisible(false);
        chip.setChipBackgroundColor(csl(COR_CHIP_BG_SEL, COR_CHIP_BG));
        chip.setChipStrokeColor(csl(COR_VERDE, COR_BORDA));
        chip.setChipStrokeWidth(dp(1));
        chip.setTextColor(csl(COR_VERDE_CLARO, COR_TEXTO_CHIP));
        chip.setTextSize(14);
        chip.setChipCornerRadius(dp(24));
        chip.setChipMinHeight(dp(44));
        chip.setChipStartPadding(dp(6));
        chip.setChipEndPadding(dp(6));
        chip.setEnsureMinTouchTargetSize(false);
        chip.setRippleColor(ColorStateList.valueOf(0x2210B981));
        return chip;
    }

    /** Marca como selecionado só o chip cuja tag é igual à informada. */
    private void marcarChips(ChipGroup grupo, Object tag) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            View c = grupo.getChildAt(i);
            if (c instanceof Chip) {
                ((Chip) c).setChecked(tag != null && tag.equals(c.getTag()));
            }
        }
    }

    private void montarChipsTipo() {
        chipGroupTipo.removeAllViews();
        for (final String tipo : TIPOS) {
            Chip chip = criarChip(tipo, tipo);
            chip.setOnClickListener(v -> {
                bancoEdicaoReconhecido = true;
                selecionarTipo(tipo);
            });
            chipGroupTipo.addView(chip);
        }
    }

    private void montarChipsParcelas() {
        chipGroupParcelas.removeAllViews();
        for (final int n : PARCELAS_PRESET) {
            Chip chip = criarChip(n + "x", Integer.valueOf(n));
            chip.setOnClickListener(v -> selecionarParcelas(n));
            chipGroupParcelas.addView(chip);
        }
        chipOutroParcelas = criarChip("Outro", "outro");
        chipOutroParcelas.setOnClickListener(v -> mostrarDialogParcelas());
        chipGroupParcelas.addView(chipOutroParcelas);
    }

    private void selecionarTipo(String tipo) {
        tipoSelecionado = tipo;
        marcarChips(chipGroupTipo, tipo);
        atualizarSelecao(null);
        atualizarResumo();
    }

    private void selecionarParcelas(int n) {
        parcelasSel = n;
        boolean preset = false;
        for (int p : PARCELAS_PRESET) {
            if (p == n) {
                preset = true;
                break;
            }
        }
        if (preset) {
            chipOutroParcelas.setText("Outro");
            chipOutroParcelas.setTag("outro");
        } else {
            chipOutroParcelas.setText(n + "x");
            chipOutroParcelas.setTag(Integer.valueOf(n));
        }
        marcarChips(chipGroupParcelas, Integer.valueOf(n));
        atualizarResumo();
    }

    private void mostrarDialogParcelas() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Ex: 18");
        if (parcelasSel > 1) {
            input.setText(String.valueOf(parcelasSel));
            input.setSelection(input.getText().length());
        }

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(20), dp(8), dp(20), 0);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Número de parcelas")
                .setMessage("Digite um valor de 1 a " + PARCELAS_MAX + ".")
                .setView(container)
                .setPositiveButton("OK", (dialog, which) -> {
                    int n = -1;
                    try {
                        n = Integer.parseInt(input.getText().toString().trim());
                    } catch (NumberFormatException ignored) { }
                    if (n < 1 || n > PARCELAS_MAX) {
                        Toast.makeText(this, "Digite um número entre 1 e " + PARCELAS_MAX + ".",
                                Toast.LENGTH_SHORT).show();
                        marcarChips(chipGroupParcelas, Integer.valueOf(parcelasSel));
                        return;
                    }
                    selecionarParcelas(n);
                })
                .setNegativeButton("Cancelar", (dialog, which) ->
                        marcarChips(chipGroupParcelas, Integer.valueOf(parcelasSel)))
                .setOnCancelListener(dialog ->
                        marcarChips(chipGroupParcelas, Integer.valueOf(parcelasSel)))
                .show();
    }

    private GradientDrawable bolinha(String corHex) {
        int cor;
        try {
            cor = Color.parseColor(corHex);
        } catch (Exception e) {
            cor = 0xFF64748B;
        }
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(cor);
        g.setSize(dp(10), dp(10));
        return g;
    }

    private void carregarCategoriasDoBanco(String manterNome) {
        categoriasDoBanco = db.categoriaDao().listarPorUsuario(session.getUserId());
        chipGroupCategoria.removeAllViews();
        categoriaSelecionada = null;

        if (categoriasDoBanco.isEmpty()) {
            tvSemCategoria.setVisibility(View.VISIBLE);
            return;
        }
        tvSemCategoria.setVisibility(View.GONE);

        Categoria alvo = categoriasDoBanco.get(0);
        for (final Categoria c : categoriasDoBanco) {
            Chip chip = criarChip(c.getNome(), c.getNome());
            chip.setChipIcon(bolinha(c.getCor()));
            chip.setChipIconSize(dp(10));
            chip.setChipIconTint(null);
            chip.setChipIconVisible(true);
            chip.setIconStartPadding(dp(4));
            chip.setIconEndPadding(dp(2));
            chip.setOnClickListener(v -> selecionarCategoria(c));
            chipGroupCategoria.addView(chip);

            if (manterNome != null && manterNome.equals(c.getNome())) {
                alvo = c;
            }
        }
        selecionarCategoria(alvo);
    }

    private void selecionarCategoria(Categoria c) {
        categoriaSelecionada = c;
        marcarChips(chipGroupCategoria, c != null ? c.getNome() : null);
    }

    // ============================================================
    // Resumo do valor / parcelas
    // ============================================================

    private double lerValorDigitado() {
        String digitos = editValor.getText().toString().replaceAll("[^0-9]", "");
        if (digitos.isEmpty()) return 0.0;
        if (digitos.length() > 11) digitos = digitos.substring(0, 11);
        try {
            return Double.parseDouble(digitos) / 100.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void atualizarResumo() {
        if (tvResumoParcelas == null) return;

        if (dividaEmEdicao != null) {
            String p = dividaEmEdicao.getParcela();
            tvResumoParcelas.setText("Parcela: " + (p != null && !p.isEmpty() ? p : "1x (À vista)"));
            return;
        }
        if (isTipoAVista(tipoSelecionado)) {
            tvResumoParcelas.setText("Pagamento à vista · registrado como pago");
            return;
        }
        double porParcela = lerValorDigitado() / Math.max(parcelasSel, 1);
        String valorFmt = String.format(LOCALE_BR, "R$ %.2f", porParcela);
        if (parcelasSel == 1) {
            tvResumoParcelas.setText("1x de " + valorFmt + " · à vista");
        } else {
            tvResumoParcelas.setText(parcelasSel + "x de " + valorFmt + " · parcelado");
        }
    }

    // ============================================================
    // Processar comprovante escolhido no seletor
    // ============================================================

    private void setImportando(boolean importando) {
        btnImportarComprovante.setEnabled(!importando);
        tvComprovante.setText(importando ? "Analisando..." : "Preencher com comprovante");
        if (importando) {
            ivComprovante.setImageResource(R.drawable.ic_loading);
            ivComprovante.setImageTintList(null);
        } else {
            ivComprovante.setImageResource(R.drawable.ic_lancar_camera);
            ivComprovante.setImageTintList(ColorStateList.valueOf(COR_VERDE_CLARO));
        }
    }

    private void resetarBotaoImportar() {
        setImportando(false);
    }

    private void processarComprovanteEscolhido(Uri uri) {
        setImportando(true);

        String mime = getContentResolver().getType(uri);
        Log.d(TAG, "MIME do comprovante: " + mime);

        final int userId = session.getUserId();

        new Thread(() -> {
            // 1) Verifica cache
            String textoCache = ReceiptCacheHelper.buscarTexto(this, userId, uri);
            if (textoCache != null && !textoCache.isEmpty()) {
                Log.d(TAG, "Usando texto do cache (OCR não será executado).");
                runOnUiThread(() -> {
                    ParsedReceipt parsed = ReceiptParser.parse(textoCache, session.getNome());

                    // CT-24.3 FIX: se não tem valor, não é um comprovante válido
                    if (parsed.getValor() == null) {
                        resetarBotaoImportar();
                        Toast.makeText(this,
                                "Nenhum valor encontrado. A imagem não parece ser um comprovante válido.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    aplicarParsedReceipt(parsed);
                    resetarBotaoImportar();
                    Toast.makeText(this,
                            "Comprovante importado (cache). Confira e salve.",
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            // 2) Cache miss → bitmap
            Bitmap bitmap;
            if (mime != null && mime.equals("application/pdf")) {
                bitmap = ReceiptImageLoader.carregarPdf(this, uri);
            } else {
                bitmap = ReceiptImageLoader.carregarImagem(this, uri);
            }

            if (bitmap == null) {
                runOnUiThread(() -> {
                    resetarBotaoImportar();
                    Toast.makeText(this,
                            "Não conseguimos abrir esse arquivo. Tente outro comprovante ou preencha manualmente.",
                            Toast.LENGTH_LONG).show();
                });
                return;
            }

            // 3) OCR
            ReceiptOcr.extrairTexto(bitmap, new ReceiptOcr.Callback() {
                @Override
                public void onSucesso(String texto) {
                    if (texto == null || texto.trim().isEmpty()) {
                        resetarBotaoImportar();
                        Toast.makeText(CadastroDividaActivity.this,
                                "O comprovante está ilegível. Tente uma foto mais nítida ou preencha manualmente.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    ParsedReceipt parsed = ReceiptParser.parse(texto, session.getNome());

                    // CT-24.3 FIX: se não tem valor, não é um comprovante válido
                    if (parsed.getValor() == null) {
                        resetarBotaoImportar();
                        Toast.makeText(CadastroDividaActivity.this,
                                "Nenhum valor encontrado. A imagem não parece ser um comprovante válido.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    ReceiptCacheHelper.salvarTexto(CadastroDividaActivity.this, userId, uri, texto, mime);
                    Log.d(TAG, "Parsed do botão: " + parsed);

                    aplicarParsedReceipt(parsed);
                    resetarBotaoImportar();

                    Toast.makeText(CadastroDividaActivity.this,
                            "Comprovante importado. Confira e salve.", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onErro(Exception e) {
                    Log.e(TAG, "Erro no OCR", e);
                    resetarBotaoImportar();
                    Toast.makeText(CadastroDividaActivity.this,
                            "Não conseguimos analisar o comprovante. Você pode preencher manualmente.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
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

    private void aplicarParsedReceipt(ParsedReceipt parsed) {
        if (parsed == null) return;

        selecionarTipo("Pix");

        if (parsed.getTitulo() != null && !parsed.getTitulo().trim().isEmpty()) {
            editDescricao.setText(parsed.getTitulo().trim());
        }

        if (parsed.getValor() != null && parsed.getValor() > 0) {
            String valorFormatado = String.format(LOCALE_BR, "R$ %.2f", parsed.getValor());
            editValor.setText(valorFormatado);
            editValor.setSelection(editValor.getText().length());
        }

        if (parsed.getData() != null && !parsed.getData().trim().isEmpty()) {
            editDataCompra.setText(parsed.getData().trim());
            editVencimento.setText(parsed.getData().trim());
        }

        if (parsed.getCategoria() != null && !parsed.getCategoria().trim().isEmpty()) {
            for (Categoria c : categoriasDoBanco) {
                if (c.getNome().equalsIgnoreCase(parsed.getCategoria().trim())) {
                    selecionarCategoria(c);
                    break;
                }
            }
        }

        selecionarParcelas(1);

        String msg = "Comprovante pré-preenchido. Confira e salve.";
        if (parsed.getBanco() != null && !parsed.getBanco().trim().isEmpty()) {
            msg = "Comprovante do " + parsed.getBanco() + " pré-preenchido. Confira e salve.";
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // ============================================================
    // Seleção de cartão / chave Pix / banco
    // ============================================================

    private boolean isTipoAVista(String tipo) {
        return tipo.equals("Pix") || tipo.equals("Cartão de Débito");
    }

    private String sigla(String texto, String padrao) {
        if (texto == null || texto.length() < 2) return padrao;
        return texto.substring(0, 2).toUpperCase();
    }

    private void atualizarSelecao(String manterValor) {
        String tipo = tipoSelecionado;
        itensSelecao.clear();

        boolean isAVista = isTipoAVista(tipo);
        boolean emEdicao = (dividaEmEdicao != null);
        // Na edição a quantidade de parcelas não é alterada
        containerParcelas.setVisibility((isAVista || emEdicao) ? View.GONE : View.VISIBLE);
        containerVencimento.setVisibility(isAVista ? View.GONE : View.VISIBLE);

        if (tipo.equals("Cartão de Crédito") || tipo.equals("Cartão de Débito")) {
            String tipoCartaoFiltro = tipo.equals("Cartão de Crédito") ? "Crédito" : "Débito";
            tvTituloSelecao.setText("Selecione o Cartão");
            List<Cartao> cartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            for (Cartao c : cartoes) {
                if (c.tipo != null && c.tipo.equals(tipoCartaoFiltro)) {
                    String final4 = c.ultimos4Digitos != null ? c.ultimos4Digitos : "";
                    itensSelecao.add(new String[]{
                            sigla(c.instituicao, "CA"),
                            c.instituicao,
                            c.instituicao,
                            "•••• " + final4});
                }
            }
        } else if (tipo.equals("Pix")) {
            tvTituloSelecao.setText("Selecione a Chave Pix");
            List<ChavePix> chaves = db.chavePixDao().listarPorUsuario(session.getUserId());
            for (ChavePix p : chaves) {
                itensSelecao.add(new String[]{
                        sigla(p.nomeFavorecido, "PX"),
                        p.nomeFavorecido,
                        p.chave,
                        p.chave});
            }
        } else {
            tvTituloSelecao.setText("Selecione o Banco / Origem");
            itensSelecao.add(new String[]{"GE", "Genérico", "Genérico", "Sem vínculo"});
        }

        if (itensSelecao.isEmpty()) {
            containerListaSelecao.setVisibility(View.GONE);
            containerVazio.setVisibility(View.VISIBLE);

            // Limpa o adapter anterior para não manter seleção de outro tipo
            chipAdapter = null;

            String msg;
            if (tipo.equals("Pix")) {
                msg = "Nenhuma chave Pix cadastrada.\nO vínculo com uma chave é opcional para registrar a despesa.";
            } else if (tipo.equals("Cartão de Crédito")) {
                msg = "Nenhum cartão de crédito cadastrado.\nCadastre um cartão para continuar.";
            } else if (tipo.equals("Cartão de Débito")) {
                msg = "Nenhum cartão de débito cadastrado.\nCadastre um cartão para continuar.";
            } else {
                msg = "Nenhum banco cadastrado.";
            }
            tvMensagemVazio.setText(msg);
            btnCadastrarAgora.setVisibility(View.VISIBLE);
        } else {
            containerListaSelecao.setVisibility(View.VISIBLE);
            containerVazio.setVisibility(View.GONE);
            chipAdapter = new ChipSelecaoAdapter(itensSelecao, position -> bancoEdicaoReconhecido = true);
            rvSelecao.setAdapter(chipAdapter);
            if (manterValor != null) {
                chipAdapter.selecionarPorValor(manterValor);
            }
        }
    }

    /**
     * Na edição, tenta descobrir qual tipo/cartão a dívida usava (olhando o campo banco).
     * Se não achar, o banco original é preservado ao salvar.
     */
    private void reconhecerBancoNaEdicao(Divida d) {
        bancoEdicaoReconhecido = false;
        String[] tentativas = {"Cartão de Crédito", "Cartão de Débito", "Pix"};
        for (String t : tentativas) {
            selecionarTipo(t);
            if (chipAdapter != null && chipAdapter.selecionarPorValor(d.getBanco())) {
                bancoEdicaoReconhecido = true;
                return;
            }
        }
        selecionarTipo("Outros");
        if (chipAdapter != null && chipAdapter.selecionarPorValor(d.getBanco())) {
            bancoEdicaoReconhecido = true;
        }
    }

    // ============================================================
    // Máscara do valor e DatePicker
    // ============================================================

    private void configurarMascaraValor(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;
                aplicarMascara(editText, s.toString());
                isUpdating = false;
                atualizarResumo();
            }
        });
    }

    private void aplicarMascara(EditText editText, String bruto) {
        String text = bruto.replaceAll("[^0-9]", "");

        if (text.isEmpty()) {
            editText.setText("");
            return;
        }

        if (text.length() > 11) {
            text = text.substring(0, 11);
        }

        double valor = Double.parseDouble(text) / 100.0;
        String formatted = String.format(LOCALE_BR, "R$ %.2f", valor);
        editText.setText(formatted);
        editText.setSelection(formatted.length());
    }

    private void configurarDatePicker(final EditText editText, View caixa) {
        View.OnClickListener abrir = v -> {
            Calendar calendar = converterParaCalendar(editText.getText().toString().trim());
            if (calendar == null) calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String data = String.format(LOCALE_BR, "%02d/%02d/%04d", day, month + 1, year);
                        editText.setText(data);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        };
        editText.setOnClickListener(abrir);
        caixa.setOnClickListener(abrir);
    }

    // ============================================================
    // Edição
    // ============================================================

    private void preencherCampos(Divida d) {
        editDescricao.setText(d.getTitulo());

        String valorFormatado = String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal());
        editValor.setText(valorFormatado);
        editValor.setSelection(editValor.getText().length());

        editVencimento.setText(d.getVencimento());

        String nomeCategoria = d.getCategoria();
        boolean categoriaExiste = false;
        for (Categoria c : categoriasDoBanco) {
            if (c.getNome().equals(nomeCategoria)) {
                categoriaExiste = true;
                selecionarCategoria(c);
                break;
            }
        }

        if (!categoriaExiste && nomeCategoria != null && !nomeCategoria.isEmpty()) {
            mostrarDialogCategoriaOrfa(nomeCategoria);
        }
    }

    private void mostrarDialogCategoriaOrfa(String nomeAntigo) {
        final EditText input = new EditText(this);
        input.setText(nomeAntigo);
        input.setSelection(nomeAntigo.length());
        input.setHint("Nome da categoria");
        input.setTextColor(ContextCompat.getColor(this, R.color.text_white));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        container.setPadding(padding, 0, padding, 0);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Categoria não existe mais")
                .setMessage("A categoria \"" + nomeAntigo + "\" foi excluída.\n\n" +
                        "Digite um nome para criar uma nova categoria para esta dívida:")
                .setView(container)
                .setCancelable(false)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String novoNome = input.getText().toString().trim();
                    if (novoNome.isEmpty()) {
                        Toast.makeText(this, "Digite um nome válido!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    criarCategoriaOrfa(novoNome);
                })
                .setNegativeButton("Cancelar", (dialog, which) ->
                        Toast.makeText(this,
                                "Escolha uma categoria antes de salvar!",
                                Toast.LENGTH_LONG).show())
                .show();
    }

    private void criarCategoriaOrfa(String nome) {
        Categoria existente = db.categoriaDao().buscarPorNome(session.getUserId(), nome);

        if (existente == null) {
            Categoria nova = new Categoria(session.getUserId(), nome, "#64748B");
            long id = db.categoriaDao().inserir(nova);

            if (id <= 0) {
                Toast.makeText(this, "Erro ao criar categoria.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        carregarCategoriasDoBanco(nome);
        Toast.makeText(this, "Categoria \"" + nome + "\" criada!", Toast.LENGTH_SHORT).show();
    }

    // ============================================================
    // Salvar
    // ============================================================

    private int extrairNumeroParcelas(String parcela) {
        try {
            String numeros = parcela.replaceAll("[^0-9]", "");
            if (numeros.isEmpty()) return 1;
            int n = Integer.parseInt(numeros);
            return n > 0 ? n : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void salvarDivida() {
        if (categoriasDoBanco.isEmpty() || categoriaSelecionada == null) {
            Toast.makeText(this, "Cadastre uma categoria antes de lançar uma dívida!", Toast.LENGTH_LONG).show();
            return;
        }

        String tipoDivida = tipoSelecionado;
        boolean isAVista = isTipoAVista(tipoDivida);

        String categoria = categoriaSelecionada.getNome();

        String descricao = editDescricao.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim()
                .replace("R$", "").replace(".", "").replace(",", ".").trim();

        String dataCompra = editDataCompra.getText().toString().trim();
        String vencimento = editVencimento.getText().toString().trim();

        String parcelas;
        if (isAVista) {
            parcelas = "1x (À vista)";
            if (vencimento.isEmpty()) {
                if (!dataCompra.isEmpty()) {
                    vencimento = dataCompra;
                } else {
                    Calendar c = Calendar.getInstance();
                    vencimento = String.format(LOCALE_BR, "%02d/%02d/%04d",
                            c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
                }
            }
        } else {
            parcelas = (parcelasSel == 1) ? "1x (À vista)" : (parcelasSel + "x");
        }

        String bancoSelecionado;
        if (chipAdapter != null && chipAdapter.getItemSelecionado() != null) {
            bancoSelecionado = chipAdapter.getItemSelecionado()[2];
        } else {
            // Regra de negócio: sem chave Pix/cartão selecionado, usa o nome do tipo como fallback
            bancoSelecionado = tipoDivida;
        }

        // Na edição, se não deu pra reconhecer o banco original e o usuário não escolheu outro, preserva o original
        if (dividaEmEdicao != null && !bancoEdicaoReconhecido && dividaEmEdicao.banco != null) {
            bancoSelecionado = dividaEmEdicao.banco;
        }

        if (descricao.isEmpty() || valorStr.isEmpty() || (!isAVista && vencimento.isEmpty())) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios!", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (valor <= 0) {
            Toast.makeText(this, "Valor deve ser maior que zero!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dividaEmEdicao != null) {
            AlarmeHelper.cancelar(this, dividaEmEdicao);
            dividaEmEdicao.titulo = descricao;
            dividaEmEdicao.valorTotal = valor;
            dividaEmEdicao.valorParcela = valor;
            if (dividaEmEdicao.isPago()) {
                dividaEmEdicao.valorPago = valor;
            }
            dividaEmEdicao.banco = bancoSelecionado;
            dividaEmEdicao.categoria = categoria;
            // dividaEmEdicao.parcela NÃO é alterada (ex.: "2/4" continua "2/4")
            dividaEmEdicao.vencimento = vencimento;
            db.dividaDao().atualizar(dividaEmEdicao);

            if (!dividaEmEdicao.isPago()) {
                AlarmeHelper.agendar(this, dividaEmEdicao);
            }
            Toast.makeText(this, "Dívida atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isAVista) {
            Divida nova = new Divida(
                    session.getUserId(), descricao, valor, valor, valor,
                    bancoSelecionado, categoria, "1x (À vista)", vencimento, true
            );
            db.dividaDao().inserir(nova);

            String msg = tipoDivida.equals("Pix") ? "Compra via Pix registrada!" : "Compra no Débito registrada!";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int numParcelas = extrairNumeroParcelas(parcelas);
        if (numParcelas == 1) {
            Divida nova = new Divida(
                    session.getUserId(), descricao, valor, valor, 0.0,
                    bancoSelecionado, categoria, "1x (À vista)", vencimento, false
            );
            long idGerado = db.dividaDao().inserir(nova);
            if (idGerado > 0) {
                nova.id = (int) idGerado;
                AlarmeHelper.agendar(this, nova);
                Toast.makeText(this, "Dívida cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
            }
            finish();
            return;
        }

        int grupoId = db.dividaDao().maxGrupoId() + 1;
        double valorParcela = valor / numParcelas;

        Calendar cBase = converterParaCalendar(vencimento);
        if (cBase == null) {
            Toast.makeText(this, "Data de vencimento inválida!", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < numParcelas; i++) {
            Calendar cParcela = (Calendar) cBase.clone();
            cParcela.add(Calendar.MONTH, i);

            String vencParcela = String.format(LOCALE_BR, "%02d/%02d/%04d",
                    cParcela.get(Calendar.DAY_OF_MONTH),
                    cParcela.get(Calendar.MONTH) + 1,
                    cParcela.get(Calendar.YEAR));

            String numeroParcela = (i + 1) + "/" + numParcelas;

            Divida parcela = new Divida(
                    session.getUserId(),
                    grupoId,
                    descricao,
                    valorParcela,
                    valorParcela,
                    0.0,
                    bancoSelecionado,
                    categoria,
                    numeroParcela,
                    vencParcela,
                    false,
                    false
            );

            long idGerado = db.dividaDao().inserir(parcela);
            if (idGerado > 0) {
                parcela.id = (int) idGerado;
                AlarmeHelper.agendar(this, parcela);
            }
        }

        Toast.makeText(this,
                "Dívida cadastrada em " + numParcelas + "x de R$ " +
                        String.format(LOCALE_BR, "%.2f", valorParcela),
                Toast.LENGTH_LONG).show();
        finish();
    }

    private Calendar converterParaCalendar(String data) {
        try {
            String[] partes = data.split("/");
            if (partes.length != 3) return null;
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]) - 1;
            int ano = Integer.parseInt(partes[2]);

            Calendar c = Calendar.getInstance();
            c.set(ano, mes, dia, 9, 0, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c;
        } catch (Exception e) {
            return null;
        }
    }
}