package com.example.controle_gastos.view;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.DadosMock;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CadastroDividaActivity extends AppCompatActivity {

    private Spinner spinnerTipoDivida, spinnerBanco, spinnerCategoria, spinnerParcelas;
    private EditText editDevedor, editDescricao, editValor, editDataCompra, editVencimento;
    private MaterialButton btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_divida);

        // Vincular componentes
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

        // Configurar Spinners
        configurarSpinners();

        // Configurar seletores de data
        configurarDatePicker(editDataCompra);
        configurarDatePicker(editVencimento);

        // Ação do botão Salvar
        btnSalvar.setOnClickListener(v -> salvarDivida());
    }

    private void configurarSpinners() {
        // Tipo de Dívida
        String[] tipos = {"Cartão de Crédito", "Empréstimo", "Fatura", "Boleto", "Outros"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDivida.setAdapter(adapterTipo);

        // Bancos
        String[] bancos = {"Nubank", "Itaú", "Banco Inter", "Bradesco", "Santander"};
        ArrayAdapter<String> adapterBanco = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bancos);
        adapterBanco.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBanco.setAdapter(adapterBanco);

        // Categorias
        String[] categorias = {"Alimentação", "Transporte", "Saúde", "Educação", "Lazer", "Moradia", "Outros"};
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categorias);
        adapterCategoria.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoria);

        // Parcelas
        String[] parcelas = {"1x (À vista)", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "10x", "11x", "12x"};
        ArrayAdapter<String> adapterParcelas = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, parcelas);
        adapterParcelas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParcelas.setAdapter(adapterParcelas);
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
        // Pegar valores dos campos
        String tipo = spinnerTipoDivida.getSelectedItem().toString();
        String banco = spinnerBanco.getSelectedItem().toString();
        String categoria = spinnerCategoria.getSelectedItem().toString();
        String parcelas = spinnerParcelas.getSelectedItem().toString();
        String devedor = editDevedor.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String valorStr = editValor.getText().toString().trim().replace(",", ".");
        String dataCompra = editDataCompra.getText().toString().trim();
        String vencimento = editVencimento.getText().toString().trim();

        // Validações
        if (devedor.isEmpty() || descricao.isEmpty() || valorStr.isEmpty() || dataCompra.isEmpty() || vencimento.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
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

        // Criar nova dívida
        int novoId = DadosMock.getDividasIniciais().size() + 1;
        Divida novaDivida = new Divida(
                novoId,
                descricao,
                valor,
                0.0,
                banco,
                categoria,
                parcelas,
                vencimento,
                false
        );

        // Adicionar à lista compartilhada (USANDO O MÉTODO DO DADOSMOCK)
        DadosMock.adicionarDivida(novaDivida);

        Toast.makeText(this, "Dívida cadastrada com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }
}