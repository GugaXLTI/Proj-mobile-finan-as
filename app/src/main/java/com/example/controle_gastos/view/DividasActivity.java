package com.example.controle_gastos.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.DividaAdapter;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.utils.DadosMock;

import java.util.List;
import java.util.Locale;

public class DividasActivity extends AppCompatActivity implements DividaAdapter.OnDividaActionListener {

    private RecyclerView rvDividas;
    private TextView tvTotalAPagar, tvTotalPago, tvTotalGeral;
    private Button btnCadastrarDivida;
    private DividaAdapter adapter;
    private List<Divida> dividas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dividas);

        // Vincular componentes
        rvDividas = findViewById(R.id.rvDividas);
        tvTotalAPagar = findViewById(R.id.tvTotalAPagar);
        tvTotalPago = findViewById(R.id.tvTotalPago);
        tvTotalGeral = findViewById(R.id.tvTotalGeral);
        btnCadastrarDivida = findViewById(R.id.btnCadastrarDivida);

        // Carregar dados mock
        dividas = DadosMock.getDividasIniciais();

        // Configurar RecyclerView
        rvDividas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DividaAdapter(dividas, this);
        rvDividas.setAdapter(adapter);

        // Atualizar totais
        atualizarTotais();

        // Botão Cadastrar
        btnCadastrarDivida.setOnClickListener(v -> {
            Toast.makeText(this, "Abrir tela de cadastro de dívida", Toast.LENGTH_SHORT).show();
            // Aqui futuramente você navega para AdicionarDividaActivity
        });
    }

    private void atualizarTotais() {
        double totalAPagar = 0;
        double totalPago = 0;
        double totalGeral = 0;

        for (Divida d : dividas) {
            totalGeral += d.getValorTotal();
            if (d.isPago()) {
                totalPago += d.getValorTotal();
            } else {
                totalAPagar += d.getValorRestante();
            }
        }

        tvTotalAPagar.setText(String.format(Locale.getDefault(), "R$ %.2f", totalAPagar));
        tvTotalPago.setText(String.format(Locale.getDefault(), "R$ %.2f", totalPago));
        tvTotalGeral.setText(String.format(Locale.getDefault(), "R$ %.2f", totalGeral));
    }

    // ========== Ações dos botões ==========
    @Override
    public void onExcluirClick(int position) {
        Divida d = dividas.get(position);
        Toast.makeText(this, "Excluir: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
        // Aqui futuramente remove do banco e atualiza a lista
    }

    @Override
    public void onEditarClick(int position) {
        Divida d = dividas.get(position);
        Toast.makeText(this, "Editar: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
        // Aqui futuramente abre tela de edição
    }

    @Override
    public void onPagarClick(int position) {
        Divida d = dividas.get(position);
        d.setPago(true);
        adapter.notifyItemChanged(position);
        atualizarTotais();
        Toast.makeText(this, "Pagamento registrado: " + d.getTitulo(), Toast.LENGTH_SHORT).show();
        // Aqui futuramente atualiza no banco
    }
}