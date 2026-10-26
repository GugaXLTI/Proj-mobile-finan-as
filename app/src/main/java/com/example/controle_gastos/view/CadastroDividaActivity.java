package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.controle_gastos.adapter.ChipSelecaoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.ChavePix;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.ParsedReceipt;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.BottomNavHelper; // ⭐ IMPORT ADICIONADO
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.ReceiptAiParser;
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

    private ChipGroup chipGroupTipo, chipGroupParcelas, chipGroupCategoria;
    private EditText editDescricao, editValor, editDataCompra, editVencimento;
    private MaterialButton btnSalvar;
    private LinearLayout btnImportarComprovante;
    private TextView tvComprovante;
    private ImageView ivComprovante;

    private RecyclerView rvSelecao;
    private LinearLayout containerVazio;
    private TextView btnNovoItem, btnNovaCategoria, tvTituloSelecao, tvResumoParcelas, tvSemCategoria;
    private Button btnCadastrarAgora;
    private LinearLayout boxDataCompra, boxVencimento, containerParcelas;

    private AppDatabase db;
    private SessionManager session;
    private ChipSelecaoAdapter chipAdapter;

    private int dividaId = -1;
    private Divida dividaEmEdicao = null;
    private List<Categoria> categoriasDoBanco = new ArrayList<>();
    private List<String[]> itensSelecao = new ArrayList<>();

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

        // ⭐ CORREÇÃO: Ativa o botão voltar e a barra inferior
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
        rvSelecao = findViewById(R.id.rvSelecao);
        containerVazio = findViewById(R.id.containerVazio);
        btnNovoItem = findViewById(R.id.btnNovoItem);
        btnNovaCategoria = findViewById(R.id.btnNovaCategoria);
        tvTituloSelecao = findViewById(R.id.tvTituloSelecao);
        btnCadastrarAgora = findViewById(R.id.btnCadastrarAgora);
        tvResumoParcelas = findViewById(R.id.tvResumoParcelas);
        tvSemCategoria = findViewById(R.id.tvSemCategoria);
        boxDataCompra = findViewById(R.id.boxDataCompra);
        boxVencimento = findViewById(R.id.boxVencimento);
        containerParcelas = findViewById(R.id.containerParcelas);

        rvSelecao.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        configurarChipGroups();
        configurarDatePicker(boxDataCompra, editDataCompra);
        configurarDatePicker(boxVencimento, editVencimento);
        configurarMascaraValor(editValor);

        btnNovaCategoria.setOnClickListener(v -> startActivity(new Intent(this, CategoriasActivity.class)));
        btnNovoItem.setOnClickListener(v -> startActivity(new Intent(this, CartoesActivity.class)));
        btnCadastrarAgora.setOnClickListener(v -> startActivity(new Intent(this, CartoesActivity.class)));
        btnImportarComprovante.setOnClickListener(v -> pickComprovanteLauncher.launch(new String[]{"image/*", "application/pdf"}));

        chipGroupTipo.setOnCheckedStateChangeListener((group, checkedIds) -> atualizarSelecao());
        chipGroupParcelas.setOnCheckedStateChangeListener((group, checkedIds) -> atualizarResumoParcelas());

        if (getIntent().hasExtra("divida_id")) {
            dividaId = getIntent().getIntExtra("divida_id", -1);
            if (dividaId != -1) {
                dividaEmEdicao = db.dividaDao().buscarPorId(dividaId);
                if (dividaEmEdicao != null) {
                    preencherCampos(dividaEmEdicao);
                    btnSalvar.setText("Atualizar Dívida");
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

    // ... (O restante dos métodos processarComprovanteEscolhido, salvarDivida, etc., permanecem exatamente iguais aos da mensagem anterior)
    // Vou colocar os métodos essenciais aqui para você não precisar copiar duas vezes se não quiser, mas o ideal é substituir o arquivo inteiro.

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
                        Toast.makeText(CadastroDividaActivity.this, "Erro ao analisar o comprovante.", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Nenhum valor encontrado. A imagem não parece ser um comprovante válido.", Toast.LENGTH_LONG).show();
            return;
        }
        aplicarParsedReceipt(parsed);
        resetarBotaoImportar();
        Toast.makeText(this, "Comprovante importado. Confira e salve.", Toast.LENGTH_LONG).show();
    }

    private void resetarBotaoImportar() {
        btnImportarComprovante.setEnabled(true);
        tvComprovante.setText("Preencher com comprovante");
        ivComprovante.setVisibility(View.VISIBLE);
    }

    private void configurarChipGroups() {
        String[] tipos = {"Cartão de Crédito", "Cartão de Débito", "Pix", "Empréstimo", "Fatura", "Boleto", "Outros"};
        for (String tipo : tipos) { chipGroupTipo.addView(criarChip(tipo)); }
        if (chipGroupTipo.getChildCount() > 0) { ((Chip) chipGroupTipo.getChildAt(0)).setChecked(true); }

        String[] parcelas = {"1x", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "10x", "11x", "12x"};
        for (String p : parcelas) { chipGroupParcelas.addView(criarChip(p)); }
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
        chip.setChipStrokeWidth(2f);
        chip.setChipCornerRadius(20f);
        return chip;
    }

    private void carregarCategoriasDoBanco() {
        categoriasDoBanco = db.categoriaDao().listarPorUsuario(session.getUserId());
        chipGroupCategoria.removeAllViews();
        if (categoriasDoBanco.isEmpty()) {
            tvSemCategoria.setVisibility(View.VISIBLE);
        } else {
            tvSemCategoria.setVisibility(View.GONE);
            for (Categoria c : categoriasDoBanco) { chipGroupCategoria.addView(criarChip(c.getNome())); }
            if (chipGroupCategoria.getChildCount() > 0) { ((Chip) chipGroupCategoria.getChildAt(0)).setChecked(true); }
        }
    }

    private void atualizarSelecao() {
        String tipo = "";
        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            if (chip.isChecked()) { tipo = chip.getText().toString(); break; }
        }
        if (tipo.isEmpty()) return;

        boolean isAVista = tipo.equals("Pix") || tipo.equals("Cartão de Débito");
        containerParcelas.setVisibility(isAVista ? View.GONE : View.VISIBLE);
        boxVencimento.setVisibility(isAVista ? View.GONE : View.VISIBLE);
        tvResumoParcelas.setVisibility(isAVista ? View.GONE : View.VISIBLE);

        itensSelecao.clear();
        if (tipo.equals("Cartão de Crédito") || tipo.equals("Cartão de Débito")) {
            String tipoCartaoFiltro = tipo.equals("Cartão de Crédito") ? "Crédito" : "Débito";
            tvTituloSelecao.setText("Selecione o Cartão");
            List<Cartao> cartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            for (Cartao c : cartoes) {
                if (c.tipo != null && c.tipo.equals(tipoCartaoFiltro)) {
                    String sigla = c.instituicao.length() >= 2 ? c.instituicao.substring(0, 2).toUpperCase() : c.instituicao;
                    String detalhe = "•••• " + (c.ultimos4Digitos != null ? c.ultimos4Digitos : "0000");
                    itensSelecao.add(new String[]{sigla, c.instituicao, c.instituicao, detalhe});
                }
            }
        } else if (tipo.equals("Pix")) {
            tvTituloSelecao.setText("Selecione a Chave Pix");
            List<ChavePix> chaves = db.chavePixDao().listarPorUsuario(session.getUserId());
            for (ChavePix p : chaves) {
                String sigla = p.nomeFavorecido.length() >= 2 ? p.nomeFavorecido.substring(0, 2).toUpperCase() : "PX";
                itensSelecao.add(new String[]{sigla, p.nomeFavorecido, p.chave, p.chave});
            }
        } else {
            tvTituloSelecao.setText("Selecione o Banco / Origem");
            itensSelecao.add(new String[]{"GE", "Genérico", "Genérico", ""});
        }

        if (itensSelecao.isEmpty()) {
            rvSelecao.setVisibility(View.GONE);
            containerVazio.setVisibility(View.VISIBLE);
            String msg = tipo.equals("Pix") ? "Nenhuma chave Pix cadastrada." : "Nenhum cartão cadastrado.";
            ((TextView) findViewById(R.id.tvMensagemVazio)).setText(msg);
            btnCadastrarAgora.setVisibility(View.VISIBLE);
            chipAdapter = null;
        } else {
            rvSelecao.setVisibility(View.VISIBLE);
            containerVazio.setVisibility(View.GONE);
            chipAdapter = new ChipSelecaoAdapter(itensSelecao, position -> { });
            rvSelecao.setAdapter(chipAdapter);
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
        String texto = numParcelas + "x de " + String.format(new Locale("pt", "BR"), "R$ %.2f", valorParcela);
        if (numParcelas == 1) texto += " · à vista";
        tvResumoParcelas.setText(texto);
    }

    private void aplicarParsedReceipt(ParsedReceipt parsed) {
        if (parsed == null) return;
        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            if (chip.getText().toString().equals("Pix")) { chip.setChecked(true); break; }
        }
        if (parsed.getTitulo() != null && !parsed.getTitulo().trim().isEmpty()) { editDescricao.setText(parsed.getTitulo().trim()); }
        if (parsed.getValor() != null && parsed.getValor() > 0) {
            String valorFormatado = String.format(new Locale("pt", "BR"), "R$ %.2f", parsed.getValor());
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
        for (int i = 0; i < chipGroupParcelas.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupParcelas.getChildAt(i);
            if (chip.getText().toString().equals("1x")) { chip.setChecked(true); break; }
        }
        atualizarResumoParcelas();
    }

    private void salvarDivida() {
        String tipoDivida = "";
        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            if (chip.isChecked()) { tipoDivida = chip.getText().toString(); break; }
        }
        if (tipoDivida.isEmpty()) { Toast.makeText(this, "Selecione o tipo de dívida.", Toast.LENGTH_SHORT).show(); return; }

        boolean isAVista = tipoDivida.equals("Pix") || tipoDivida.equals("Cartão de Débito");

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

        if (descricao.isEmpty() || valorStr.isEmpty()) { Toast.makeText(this, "Preencha a descrição e o valor!", Toast.LENGTH_SHORT).show(); return; }

        double valor;
        try { valor = Double.parseDouble(valorStr); } catch (Exception e) { Toast.makeText(this, "Valor inválido!", Toast.LENGTH_SHORT).show(); return; }
        if (valor <= 0) { Toast.makeText(this, "O valor deve ser maior que zero.", Toast.LENGTH_SHORT).show(); return; }

        String bancoSelecionado = tipoDivida;
        if (chipAdapter != null && chipAdapter.getItemSelecionado() != null) { bancoSelecionado = chipAdapter.getItemSelecionado()[2]; }

        String parcelas = "1x (À vista)";
        if (isAVista) {
            if (vencimento.isEmpty()) { vencimento = dataCompra.isEmpty() ? getHoje() : dataCompra; }
        } else {
            if (vencimento.isEmpty()) { Toast.makeText(this, "Informe o 1º vencimento.", Toast.LENGTH_SHORT).show(); return; }
            for (int i = 0; i < chipGroupParcelas.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupParcelas.getChildAt(i);
                if (chip.isChecked()) { parcelas = chip.getText().toString(); break; }
            }
        }

        if (dividaEmEdicao != null) {
            AlarmeHelper.cancelar(this, dividaEmEdicao);
            dividaEmEdicao.titulo = descricao;
            dividaEmEdicao.valorTotal = valor;
            dividaEmEdicao.valorParcela = valor;
            dividaEmEdicao.banco = bancoSelecionado;
            dividaEmEdicao.categoria = categoria;
            dividaEmEdicao.parcela = isAVista ? "1x (À vista)" : parcelas;
            dividaEmEdicao.vencimento = vencimento;
            db.dividaDao().atualizar(dividaEmEdicao);
            if (!dividaEmEdicao.isPago()) { AlarmeHelper.agendar(this, dividaEmEdicao); }
            Toast.makeText(this, "Dívida atualizada!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (isAVista) {
            Divida nova = new Divida(session.getUserId(), descricao, valor, valor, valor, bancoSelecionado, categoria, "1x (À vista)", vencimento, true);
            db.dividaDao().inserir(nova);
            Toast.makeText(this, "Lançamento à vista registrado!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int numParcelas = 1;
        try { numParcelas = Integer.parseInt(parcelas.replace("x", "")); } catch (Exception e) {}

        if (numParcelas == 1) {
            Divida nova = new Divida(session.getUserId(), descricao, valor, valor, 0.0, bancoSelecionado, categoria, "1x", vencimento, false);
            long id = db.dividaDao().inserir(nova);
            if (id > 0) { nova.id = (int) id; AlarmeHelper.agendar(this, nova); }
            Toast.makeText(this, "Dívida cadastrada!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int grupoId = db.dividaDao().maxGrupoId() + 1;
        double valorParcela = valor / numParcelas;
        Calendar cBase = converterParaCalendar(vencimento);
        if (cBase == null) { Toast.makeText(this, "Data inválida!", Toast.LENGTH_SHORT).show(); return; }

        for (int i = 0; i < numParcelas; i++) {
            Calendar cParcela = (Calendar) cBase.clone();
            cParcela.add(Calendar.MONTH, i);
            String vencParcela = String.format(new Locale("pt", "BR"), "%02d/%02d/%04d", cParcela.get(Calendar.DAY_OF_MONTH), cParcela.get(Calendar.MONTH) + 1, cParcela.get(Calendar.YEAR));
            String numeroParcela = (i + 1) + "/" + numParcelas;
            Divida parcela = new Divida(session.getUserId(), grupoId, descricao, valorParcela, valorParcela, 0.0, bancoSelecionado, categoria, numeroParcela, vencParcela, false, false);
            long idGerado = db.dividaDao().inserir(parcela);
            if (idGerado > 0) { parcela.id = (int) idGerado; AlarmeHelper.agendar(this, parcela); }
        }
        Toast.makeText(this, "Dívida parcelada em " + numParcelas + "x cadastrada!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void preencherCampos(Divida d) {
        editDescricao.setText(d.getTitulo());
        editValor.setText(String.format(new Locale("pt", "BR"), "R$ %.2f", d.getValorTotal()));
        editVencimento.setText(d.getVencimento());
        for (int i = 0; i < chipGroupTipo.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupTipo.getChildAt(i);
            String tipo = chip.getText().toString();
            if ((tipo.equals("Pix") && d.getBanco() != null && d.getBanco().length() > 20) || (d.getBanco() != null && d.getBanco().equals(tipo))) {
                chip.setChecked(true);
                break;
            }
        }
        for (int i = 0; i < chipGroupCategoria.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupCategoria.getChildAt(i);
            if (chip.getText().toString().equals(d.getCategoria())) { chip.setChecked(true); break; }
        }
        atualizarResumoParcelas();
    }

    private String getHoje() {
        Calendar c = Calendar.getInstance();
        return String.format(new Locale("pt", "BR"), "%02d/%02d/%04d", c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR));
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
                String formatted = String.format(new Locale("pt", "BR"), "R$ %.2f", valor);
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
                String data = String.format(new Locale("pt", "BR"), "%02d/%02d/%04d", day, month + 1, year);
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
}