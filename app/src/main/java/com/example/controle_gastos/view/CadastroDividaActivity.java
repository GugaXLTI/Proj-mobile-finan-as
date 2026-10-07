package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.CategoriaSpinnerAdapter;
import com.example.controle_gastos.adapter.ChipSelecaoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.ChavePix;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.AlarmeHelper;
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CadastroDividaActivity extends AppCompatActivity {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private Spinner spinnerTipoDivida, spinnerCategoria, spinnerParcelas;
    private EditText editDescricao, editValor, editDataCompra, editVencimento;
    private MaterialButton btnSalvar;
    private RecyclerView rvSelecao;
    private LinearLayout containerVazio, containerParcelas, containerVencimento;
    private TextView btnNovoItem, btnNovaCategoria, tvTituloSelecao;
    private Button btnCadastrarAgora;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;
    private ChipSelecaoAdapter chipAdapter;

    private int dividaId = -1;
    private Divida dividaEmEdicao = null;
    private List<Categoria> categoriasDoBanco = new ArrayList<>();

    private List<String[]> itensSelecao = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_divida);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        CategoriaSeeder.popularSeVazio(this, session.getUserId());

        spinnerTipoDivida = findViewById(R.id.spinnerTipoDivida);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerParcelas = findViewById(R.id.spinnerParcelas);
        editDescricao = findViewById(R.id.editDescricao);
        editValor = findViewById(R.id.editValor);
        editDataCompra = findViewById(R.id.editDataCompra);
        editVencimento = findViewById(R.id.editVencimento);
        btnSalvar = findViewById(R.id.btnSalvarDivida);
        rvSelecao = findViewById(R.id.rvSelecao);
        containerVazio = findViewById(R.id.containerVazio);
        containerParcelas = findViewById(R.id.containerParcelas);
        containerVencimento = findViewById(R.id.containerVencimento);
        btnNovoItem = findViewById(R.id.btnNovoItem);
        btnNovaCategoria = findViewById(R.id.btnNovaCategoria);
        tvTituloSelecao = findViewById(R.id.tvTituloSelecao);
        btnCadastrarAgora = findViewById(R.id.btnCadastrarAgora);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvSelecao.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        configurarSpinners();
        configurarDatePicker(editDataCompra);
        configurarDatePicker(editVencimento);
        configurarMascaraValor(editValor);

        btnNovaCategoria.setOnClickListener(v -> {
            startActivity(new Intent(this, CategoriasActivity.class));
        });

        btnNovoItem.setOnClickListener(v -> {
            startActivity(new Intent(this, CartoesActivity.class));
        });

        btnCadastrarAgora.setOnClickListener(v -> {
            startActivity(new Intent(this, CartoesActivity.class));
        });

        spinnerTipoDivida.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                atualizarSelecao();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        if (getIntent().hasExtra("divida_id")) {
            dividaId = getIntent().getIntExtra("divida_id", -1);
            if (dividaId != -1) {
                dividaEmEdicao = db.dividaDao().buscarPorId(dividaId);
                if (dividaEmEdicao != null) {
                    preencherCampos(dividaEmEdicao);
                    btnSalvar.setText("Atualizar Dívida");
                }
            }
        }

        btnSalvar.setOnClickListener(v -> salvarDivida());

        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(this, InicioActivity.class));
            finish();
        });

        tabLancar.setOnClickListener(v ->
                Toast.makeText(this, "Você já está em Lançar", Toast.LENGTH_SHORT).show());

        tabDividas.setOnClickListener(v -> {
            startActivity(new Intent(this, DividasActivity.class));
            finish();
        });

        tabRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });

        tabConfig.setOnClickListener(v -> {
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            finish();
        });
    }

    private boolean isTipoAVista(String tipo) {
        return tipo.equals("Pix") || tipo.equals("Cartão de Débito");
    }

    private void atualizarSelecao() {
        String tipo = spinnerTipoDivida.getSelectedItem().toString();
        itensSelecao.clear();

        boolean isAVista = isTipoAVista(tipo);
        containerParcelas.setVisibility(isAVista ? View.GONE : View.VISIBLE);
        containerVencimento.setVisibility(isAVista ? View.GONE : View.VISIBLE);

        if (tipo.equals("Cartão de Crédito") || tipo.equals("Cartão de Débito")) {
            String tipoCartaoFiltro = tipo.equals("Cartão de Crédito") ? "Crédito" : "Débito";
            tvTituloSelecao.setText("Selecione o Cartão");
            List<Cartao> cartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            for (Cartao c : cartoes) {
                if (c.tipo != null && c.tipo.equals(tipoCartaoFiltro)) {
                    String sigla = c.instituicao.length() >= 2 ? c.instituicao.substring(0, 2).toUpperCase() : c.instituicao;
                    itensSelecao.add(new String[]{sigla, c.instituicao + " " + c.ultimos4Digitos, c.instituicao});
                }
            }
        } else if (tipo.equals("Pix")) {
            tvTituloSelecao.setText("Selecione a Chave Pix");
            List<ChavePix> chaves = db.chavePixDao().listarPorUsuario(session.getUserId());
            for (ChavePix p : chaves) {
                String sigla = p.nomeFavorecido.length() >= 2 ? p.nomeFavorecido.substring(0, 2).toUpperCase() : "PX";
                itensSelecao.add(new String[]{sigla, p.nomeFavorecido, p.chave});
            }
        } else {
            tvTituloSelecao.setText("Selecione o Banco / Origem");
            itensSelecao.add(new String[]{"GE", "Genérico", "Genérico"});
        }

        if (itensSelecao.isEmpty()) {
            rvSelecao.setVisibility(View.GONE);
            containerVazio.setVisibility(View.VISIBLE);

            String msg;
            if (tipo.equals("Pix")) {
                msg = "Nenhuma chave Pix cadastrada.\nCadastre uma chave para continuar.";
            } else if (tipo.equals("Cartão de Crédito")) {
                msg = "Nenhum cartão de crédito cadastrado.\nCadastre um cartão para continuar.";
            } else if (tipo.equals("Cartão de Débito")) {
                msg = "Nenhum cartão de débito cadastrado.\nCadastre um cartão para continuar.";
            } else {
                msg = "Nenhum banco cadastrado.";
            }
            ((TextView) findViewById(R.id.tvMensagemVazio)).setText(msg);
            btnCadastrarAgora.setVisibility(View.VISIBLE);
        } else {
            rvSelecao.setVisibility(View.VISIBLE);
            containerVazio.setVisibility(View.GONE);
            chipAdapter = new ChipSelecaoAdapter(itensSelecao, position -> { });
            rvSelecao.setAdapter(chipAdapter);
        }
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

                if (text.isEmpty()) {
                    editText.setText("");
                    isUpdating = false;
                    return;
                }

                if (text.length() > 11) {
                    text = text.substring(0, 11);
                }

                double valor = Double.parseDouble(text) / 100.0;
                String formatted = String.format(LOCALE_BR, "R$ %.2f", valor);
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
            }
        });
    }

    private void configurarSpinners() {
        String[] tipos = {"Cartão de Crédito", "Cartão de Débito", "Pix", "Empréstimo", "Fatura", "Boleto", "Outros"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDivida.setAdapter(adapterTipo);

        carregarCategoriasDoBanco();

        String[] parcelas = {"1x (À vista)", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "10x", "11x", "12x"};
        ArrayAdapter<String> adapterParcelas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parcelas);
        adapterParcelas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParcelas.setAdapter(adapterParcelas);
    }

    private void carregarCategoriasDoBanco() {
        categoriasDoBanco = db.categoriaDao().listarPorUsuario(session.getUserId());

        if (categoriasDoBanco.isEmpty()) {
            Categoria vazia = new Categoria(0, session.getUserId(), "Nenhuma categoria", "#64748B");
            List<Categoria> listaVazia = new ArrayList<>();
            listaVazia.add(vazia);
            CategoriaSpinnerAdapter adapterVazio = new CategoriaSpinnerAdapter(this, listaVazia);
            spinnerCategoria.setAdapter(adapterVazio);
            return;
        }

        CategoriaSpinnerAdapter adapter = new CategoriaSpinnerAdapter(this, categoriasDoBanco);
        spinnerCategoria.setAdapter(adapter);
    }

    private void configurarDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        String data = String.format(LOCALE_BR, "%02d/%02d/%04d", day, month + 1, year);
                        editText.setText(data);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void preencherCampos(Divida d) {
        editDescricao.setText(d.getTitulo());

        String valorFormatado = String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal());
        editValor.setText(valorFormatado);
        editValor.setSelection(valorFormatado.length());

        editVencimento.setText(d.getVencimento());

        boolean categoriaExiste = false;
        for (Categoria c : categoriasDoBanco) {
            if (c.getNome().equals(d.getCategoria())) {
                categoriaExiste = true;
                break;
            }
        }

        if (!categoriaExiste && !d.getCategoria().isEmpty()) {
            mostrarDialogCategoriaOrfa(d.getCategoria());
        } else {
            for (int i = 0; i < categoriasDoBanco.size(); i++) {
                if (categoriasDoBanco.get(i).getNome().equals(d.getCategoria())) {
                    spinnerCategoria.setSelection(i);
                    break;
                }
            }
        }

        for (int i = 0; i < spinnerParcelas.getAdapter().getCount(); i++) {
            if (spinnerParcelas.getAdapter().getItem(i).toString().equals(d.getParcela())) {
                spinnerParcelas.setSelection(i);
                break;
            }
        }
    }

    private void mostrarDialogCategoriaOrfa(String nomeAntigo) {
        final EditText input = new EditText(this);
        input.setText(nomeAntigo);
        input.setSelection(nomeAntigo.length());
        input.setHint("Nome da categoria");
        input.setTextColor(getResources().getColor(R.color.text_white));

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
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
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Toast.makeText(this,
                            "Escolha uma categoria antes de salvar!",
                            Toast.LENGTH_LONG).show();
                })
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

        categoriasDoBanco = db.categoriaDao().listarPorUsuario(session.getUserId());
        CategoriaSpinnerAdapter adapter = new CategoriaSpinnerAdapter(this, categoriasDoBanco);
        spinnerCategoria.setAdapter(adapter);

        for (int i = 0; i < categoriasDoBanco.size(); i++) {
            if (categoriasDoBanco.get(i).getNome().equals(nome)) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }

        Toast.makeText(this, "Categoria \"" + nome + "\" criada!", Toast.LENGTH_SHORT).show();
    }

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
        if (categoriasDoBanco.isEmpty()) {
            Toast.makeText(this, "Cadastre uma categoria antes de lançar uma dívida!", Toast.LENGTH_LONG).show();
            return;
        }

        String tipoDivida = spinnerTipoDivida.getSelectedItem().toString();
        boolean isAVista = isTipoAVista(tipoDivida);

        Object itemCategoria = spinnerCategoria.getSelectedItem();
        String categoria;
        if (itemCategoria instanceof Categoria) {
            categoria = ((Categoria) itemCategoria).getNome();
        } else {
            categoria = itemCategoria.toString();
        }

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
            parcelas = spinnerParcelas.getSelectedItem().toString();
        }

        String bancoSelecionado;
        if (chipAdapter != null && chipAdapter.getItemSelecionado() != null) {
            bancoSelecionado = chipAdapter.getItemSelecionado()[2];
        } else {
            bancoSelecionado = tipoDivida;
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

        int numParcelas = extrairNumeroParcelas(parcelas);
        double valorParcela = valor / numParcelas;

        boolean jaPago = isAVista;
        double valorJaPago = isAVista ? valor : 0.0;

        if (dividaEmEdicao != null) {
            AlarmeHelper.cancelar(this, dividaEmEdicao);
            dividaEmEdicao.titulo = descricao;
            dividaEmEdicao.valorTotal = valor;
            dividaEmEdicao.valorParcela = valorParcela;
            dividaEmEdicao.valorPago = valorJaPago;
            dividaEmEdicao.banco = bancoSelecionado;
            dividaEmEdicao.categoria = categoria;
            dividaEmEdicao.parcela = parcelas;
            dividaEmEdicao.vencimento = vencimento;
            dividaEmEdicao.pago = jaPago;
            db.dividaDao().atualizar(dividaEmEdicao);

            if (!jaPago) {
                AlarmeHelper.agendar(this, dividaEmEdicao);
            }
            Toast.makeText(this, "Dívida atualizada com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            // ⭐ Agora usa o construtor de conveniência (assume excluida = false)
            Divida novaDivida = new Divida(
                    session.getUserId(), descricao, valor, valorParcela, valorJaPago,
                    bancoSelecionado, categoria, parcelas, vencimento, jaPago
            );
            long idGerado = db.dividaDao().inserir(novaDivida);
            if (idGerado > 0) {
                novaDivida.id = (int) idGerado;

                if (!jaPago) {
                    AlarmeHelper.agendar(this, novaDivida);
                }

                String msg;
                if (tipoDivida.equals("Pix")) {
                    msg = "Compra via Pix registrada!";
                } else if (tipoDivida.equals("Cartão de Débito")) {
                    msg = "Compra no Débito registrada!";
                } else {
                    msg = "Dívida cadastrada com sucesso!";
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        finish();
    }
}