package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controle_gastos.R;
import com.example.controle_gastos.model.ParsedReceipt;
import com.example.controle_gastos.utils.ReceiptCacheHelper;
import com.example.controle_gastos.utils.ReceiptImageLoader;
import com.example.controle_gastos.utils.ReceiptOcr;
import com.example.controle_gastos.utils.ReceiptParser;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class ReceiptShareActivity extends AppCompatActivity {

    private static final String TAG = "ReceiptShare";

    private ProgressBar progressAnalise;
    private TextView txtStatus;
    private TextView txtDetalhe;
    private Button btnCancelar;
    private ScrollView scrollResultado;
    private TextView txtTextoExtraido;
    private MaterialButton btnConfirmarCadastro;

    private TextView txtParsedTitulo;
    private TextView txtParsedValor;
    private TextView txtParsedData;
    private TextView txtParsedBanco;
    private TextView txtParsedCategoria;
    private TextView txtParsedBeneficiario;
    private TextView txtParsedParcelas;

    private ParsedReceipt parsedReceipt;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_share);

        session = new SessionManager(this);

        progressAnalise = findViewById(R.id.progress_analise);
        txtStatus = findViewById(R.id.txt_status);
        txtDetalhe = findViewById(R.id.txt_detalhe);
        btnCancelar = findViewById(R.id.btn_cancelar);
        scrollResultado = findViewById(R.id.scroll_resultado);
        txtTextoExtraido = findViewById(R.id.txt_texto_extraido);
        btnConfirmarCadastro = findViewById(R.id.btn_confirmar_cadastro);

        txtParsedTitulo = findViewById(R.id.txt_parsed_titulo);
        txtParsedValor = findViewById(R.id.txt_parsed_valor);
        txtParsedData = findViewById(R.id.txt_parsed_data);
        txtParsedBanco = findViewById(R.id.txt_parsed_banco);
        txtParsedCategoria = findViewById(R.id.txt_parsed_categoria);
        txtParsedBeneficiario = findViewById(R.id.txt_parsed_beneficiario);
        txtParsedParcelas = findViewById(R.id.txt_parsed_parcelas);

        btnCancelar.setOnClickListener(v -> finish());
        btnConfirmarCadastro.setOnClickListener(v -> abrirCadastroPreenchido());

        tratarIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        tratarIntent(intent);
    }

    private void tratarIntent(Intent intent) {
        if (intent == null || !Intent.ACTION_SEND.equals(intent.getAction())) {
            Toast.makeText(this, "Nenhum comprovante recebido.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Uri uri = extrairUri(intent);
        if (uri == null) {
            Toast.makeText(this, "Não conseguimos abrir esse arquivo. Tente outro comprovante ou preencha manualmente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String mime = intent.getType();
        if (mime == null) {
            mime = getContentResolver().getType(uri);
        }

        progressAnalise.setVisibility(View.VISIBLE);
        scrollResultado.setVisibility(View.GONE);
        btnConfirmarCadastro.setVisibility(View.GONE);
        resetarBotaoConfirmar();
        parsedReceipt = null;

        txtStatus.setText("Comprovante recebido.");
        txtDetalhe.setText("Processando...");

        final String mimeFinal = mime;
        final int userId = session.getUserId();

        new Thread(() -> {
            String textoCache = ReceiptCacheHelper.buscarTexto(this, userId, uri);
            if (textoCache != null && !textoCache.isEmpty()) {
                Log.d(TAG, "Usando texto do cache.");
                runOnUiThread(() -> processarTexto(textoCache));
                return;
            }

            Bitmap bitmap = carregarBitmap(uri, mimeFinal);
            if (bitmap == null) {
                runOnUiThread(() -> {
                    progressAnalise.setVisibility(View.GONE);
                    txtStatus.setText("Não conseguimos abrir o arquivo.");
                    txtDetalhe.setText("Tente outro comprovante ou preencha manualmente.");
                });
                return;
            }

            runOnUiThread(() -> {
                txtStatus.setText("Comprovante carregado.");
                txtDetalhe.setText("Extraindo texto...");
            });

            ReceiptOcr.extrairTexto(bitmap, new ReceiptOcr.Callback() {
                @Override
                public void onSucesso(String texto) {
                    ReceiptCacheHelper.salvarTexto(ReceiptShareActivity.this, userId, uri, texto, mimeFinal);
                    processarTexto(texto);
                }

                @Override
                public void onErro(Exception e) {
                    if (isFinishing() || isDestroyed()) return;
                    progressAnalise.setVisibility(View.GONE);
                    txtStatus.setText("Não conseguimos analisar o comprovante.");
                    txtDetalhe.setText("Você pode preencher manualmente.");
                }
            });
        }).start();
    }

    private Bitmap carregarBitmap(Uri uri, String mime) {
        if (mime != null && mime.equals("application/pdf")) {
            return ReceiptImageLoader.carregarPdf(this, uri);
        }
        return ReceiptImageLoader.carregarImagem(this, uri);
    }

    private void processarTexto(String texto) {
        if (isFinishing() || isDestroyed()) return;
        progressAnalise.setVisibility(View.GONE);

        if (texto == null || texto.trim().isEmpty()) {
            txtStatus.setText("Nenhum texto encontrado.");
            txtDetalhe.setText("Tente uma foto mais nítida ou preencha manualmente.");
            return;
        }

        parsedReceipt = ReceiptParser.parse(texto, session.getNome());

        // ⭐ CT-24.3 FIX: Se não tem valor, não é um comprovante válido
        if (parsedReceipt.getValor() == null) {
            txtStatus.setText("Nenhum valor encontrado.");
            txtDetalhe.setText("A imagem não parece ser um comprovante válido.");
            return;
        }

        Log.d(TAG, "Parsed: " + parsedReceipt);

        txtStatus.setText("Campos detectados.");
        txtDetalhe.setText(texto.length() + " caracteres analisados.");

        txtParsedTitulo.setText("Título: " + valorOuTraço(parsedReceipt.getTitulo()));
        txtParsedValor.setText("Valor: " + parsedReceipt.getValorFormatado());
        txtParsedData.setText("Data: " + valorOuTraço(parsedReceipt.getData()));
        txtParsedBanco.setText("Banco: " + valorOuTraço(parsedReceipt.getBanco()));
        txtParsedCategoria.setText("Categoria: " + valorOuTraço(parsedReceipt.getCategoria()));
        txtParsedBeneficiario.setText("Beneficiário: " + valorOuTraço(parsedReceipt.getBeneficiario()));
        txtParsedParcelas.setText("Parcelas: " + parsedReceipt.getParcelas() + "x");

        txtTextoExtraido.setText(texto);
        scrollResultado.setVisibility(View.VISIBLE);
        btnConfirmarCadastro.setVisibility(View.VISIBLE);
    }

    private void abrirCadastroPreenchido() {
        if (parsedReceipt == null) {
            Toast.makeText(this, "Nada para cadastrar.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmarCadastro.setEnabled(false);
        btnConfirmarCadastro.setText("Abrindo cadastro...");
        btnConfirmarCadastro.setIconResource(R.drawable.ic_loading);
        btnConfirmarCadastro.setIconTint(null);

        Intent i = new Intent(this, CadastroDividaActivity.class);
        i.putExtra("extra_prefill_receipt", true);
        i.putExtra("extra_prefill_titulo", parsedReceipt.getTitulo());
        i.putExtra("extra_prefill_valor", parsedReceipt.getValor() != null ? parsedReceipt.getValor() : 0.0);
        i.putExtra("extra_prefill_data", parsedReceipt.getData());
        i.putExtra("extra_prefill_banco", parsedReceipt.getBanco());
        i.putExtra("extra_prefill_beneficiario", parsedReceipt.getBeneficiario());
        i.putExtra("extra_prefill_categoria", parsedReceipt.getCategoria());
        i.putExtra("extra_prefill_parcelas", parsedReceipt.getParcelas());

        startActivity(i);
        finish();
    }

    private void resetarBotaoConfirmar() {
        btnConfirmarCadastro.setEnabled(true);
        btnConfirmarCadastro.setText("Confirmar e cadastrar");
        btnConfirmarCadastro.setIcon(null);
    }

    private String valorOuTraço(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }

    @SuppressWarnings("deprecation")
    private Uri extrairUri(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
        } else {
            return intent.getParcelableExtra(Intent.EXTRA_STREAM);
        }
    }
}