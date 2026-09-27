package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CadastroDividaActivity extends AppCompatActivity {

    private Spinner spinnerTipoDivida, spinnerBanco, spinnerCategoria, spinnerParcelas;
    private EditText editDevedor, editDescricao, editValor, editDataCompra, editVencimento;
    private MaterialButton btnSalvar;

    private AppDatabase db;
    private SessionManager session;

    // Modo edição
    private int dividaId = -1;
    private Divida dividaEmEdicao = null;

    // Lista de categorias carregada do banco
    private List<Categoria> categoriasDoBanco = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_divida);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        // Garante que as categorias padrão existem
        CategoriaSeeder.popularSeVazio(this, session.getUserId());

        spinnerTipoDivida = findViewById(R.id.spinnerTipoDivida);
        spinnerBanco = findViewById(R.id.spinnerBanco);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        spinnerParcelas = findViewById(R.id.spinnerParcelas);
        editDevedor = findViewById(R.id.editDevedor);
        editDescricao = findViewById(R.id.editDescricao);
        editValor = findViewById(R.id.editValor);
        editDataCompra = findViewById(R.id.editDataCompra);
        editVencimento = findViewById(R.id.editVencimento);
        btnSalvar = findViewById(R.id.btnSalvarDivida);

        configurarSpinners();
        configurarDatePicker(editDataCompra);
        configurarDatePicker(editVencimento);
        configurarMascaraValor(editValor);

        // Verifica se está em modo edição
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
    }

    private void preencherCampos(Divida d) {
        editDevedor.setText(d.getBanco());
        editDescricao.setText(d.getTitulo());

        String valorFormatado = String.format(Locale.getDefault(), "R$ %.2f", d.getValorTotal());
        editValor.setText(valorFormatado);
        editValor.setSelection(valorFormatado.length());

        editVencimento.setText(d.getVencimento());

        for (int i = 0; i < spinnerBanco.getAdapter().getCount(); i++) {
            if (spinnerBanco.getAdapter().getItem(i).toString().equals(d.getBanco())) {
                spinnerBanco.setSelection(i);
                break;
            }
        }

        // ⭐ Seleciona a categoria no spinner dinâmico
        for (int i = 0; i < categoriasDoBanco.size(); i++) {
            if (categoriasDoBanco.get(i).getNome().equals(d.getCategoria())) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < spinnerParcelas.getAdapter().getCount(); i++) {
            if (spinnerParcelas.getAdapter().getItem(i).toString().equals(d.getParcela())) {
                spinnerParcelas.setSelection(i);
                break;
            }
        }
    }

    private void configurarMascaraValor(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

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

                double valor = Double.parseDouble(text) / 100.0;
                String formatted = String.format(Locale.getDefault(), "R$ %.2f", valor);
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
            }
        });
    }

    private void configurarSpinners() {
        // Tipo de Dívida (fixo - não mudou)
        String[] tipos = {"Cartão de Crédito", "Empréstimo", "Fatura", "Boleto", "Outros"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDivida.setAdapter(adapterTipo);

        // Bancos (fixo - não mudou)
        String[] bancos = {"Nubank", "Itaú", "Banco Inter", "Bradesco", "Santander"};
        ArrayAdapter<String> adapterBanco = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bancos);
        adapterBanco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanco.setAdapter(adapterBanco);

        // ⭐ CATEGORIAS DINÂMICAS (do banco de dados)
        carregarCategoriasDoBanco();

        // Parcelas (fixo - não mudou)
        String[] parcelas = {"1x (À vista)", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "10x", "11x", "12x"};
        ArrayAdapter<String> adapterParcelas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parcelas);
        adapterParcelas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParcelas.setAdapter(adapterParcelas);
    }

    private void carregarCategoriasDoBanco() {
        // Busca as categorias do usuário logado
        categoriasDoBanco = db.categoriaDao().listarPorUsuario(session.getUserId());

        // Extrai apenas os nomes para o spinner
        List<String> nomesCategorias = new ArrayList<>();
        for (Categoria c : categoriasDoBanco) {
            nomesCategorias.add(c.getNome());
        }

        // Se o usuário não tiver nenhuma categoria, mostra mensagem
        if (nomesCategorias.isEmpty()) {
            nomesCategorias.add("Nenhuma categoria cadastrada");
            Toast.makeText(this, "Cadastre uma categoria primeiro em Configurações!", Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                nomesCategorias
        );
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);
    }

    private void configurarDatePicker(EditText editText) {
        editText.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year1, month1, dayOfMonth) -> {
                        String data = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                        editText.setText(data);
                    }, year, month, day);
            datePicker.show();
        });
    }

    private void salvarDivida() {
        // Verifica se tem categorias
        if (categoriasDoBanco.isEmpty()) {
            Toast.makeText(this, "Cadastre uma categoria antes de lançar uma dívida!", Toast.LENGTH_LONG).show();
            return;
        }

        String banco = spinnerBanco.getSelectedItem().toString();
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String parcelas = spinnerParcelas.getSelectedItem().toString();
        String devedor = editDevedor.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim()
                .replace("R$ ", "")
                .replace(".", "")
                .replace(",", ".");
        String dataCompra = editDataCompra.getText().toString().trim();
        String vencimento = editVencimento.getText().toString().trim();

        if (devedor.isEmpty() || descricao.isEmpty() || valorStr.isEmpty() || vencimento.isEmpty()) {
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
            // Modo edição
            dividaEmEdicao.titulo = descricao;
            dividaEmEdicao.valorTotal = valor;
            dividaEmEdicao.banco = banco;
            dividaEmEdicao.categoria = categoria;
            dividaEmEdicao.parcela = parcelas;
            dividaEmEdicao.vencimento = vencimento;

            db.dividaDao().atualizar(dividaEmEdicao);
            Toast.makeText(this, "Dívida atualizada com sucesso!", Toast.LENGTH_SHORT).show();
        } else {
            // Modo cadastro
            Divida novaDivida = new Divida(
                    session.getUserId(),
                    descricao,
                    valor,
                    0.0,
                    banco,
                    categoria,
                    parcelas,
                    vencimento,
                    false
            );

            long idGerado = db.dividaDao().inserir(novaDivida);

            if (idGerado > 0) {
                Toast.makeText(this, "Dívida cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        finish();
    }
}