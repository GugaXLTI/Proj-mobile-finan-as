package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.CategoriaAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Categoria;
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity implements CategoriaAdapter.OnCategoriaActionListener {

    private RecyclerView rvCategorias;
    private EditText editNomeCategoria;
    private TextView tvPreviaTag;
    private MaterialButton btnSalvarCategoria;
    private ImageView btnVoltar;
    private LinearLayout containerCores;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;
    private CategoriaAdapter adapter;
    private List<Categoria> categorias;
    private List<Integer> contagens;

    // Cor selecionada atualmente
    private String corSelecionada = "#A855F7";

    // Paleta de cores
    private final String[] CORES = {
            "#A855F7", "#10B981", "#F59E0B", "#EC4899",
            "#EF4444", "#3B82F6", "#06B6D4", "#64748B"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        // Popula categorias padrão na primeira vez
        CategoriaSeeder.popularSeVazio(this, session.getUserId());

        // Vincula componentes
        rvCategorias = findViewById(R.id.rvCategorias);
        editNomeCategoria = findViewById(R.id.editNomeCategoria);
        tvPreviaTag = findViewById(R.id.tvPreviaTag);
        btnSalvarCategoria = findViewById(R.id.btnSalvarCategoria);
        btnVoltar = findViewById(R.id.btnVoltar);
        containerCores = findViewById(R.id.containerCores);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        rvCategorias.setLayoutManager(new LinearLayoutManager(this));

        // Cria os círculos de cores
        criarPaletaCores();

        // Atualiza a prévia quando o usuário digita
        editNomeCategoria.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String texto = s.toString().trim();
                tvPreviaTag.setText(texto.isEmpty() ? "Prévia da tag" : texto);
            }
        });

        // Botão Salvar
        btnSalvarCategoria.setOnClickListener(v -> salvarCategoria());

        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Navegação
        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(CategoriasActivity.this, InicioActivity.class));
            finish();
        });
        tabLancar.setOnClickListener(v -> {
            startActivity(new Intent(CategoriasActivity.this, CadastroDividaActivity.class));
        });
        tabDividas.setOnClickListener(v -> {
            startActivity(new Intent(CategoriasActivity.this, DividasActivity.class));
            finish();
        });
        tabRelatorios.setOnClickListener(v -> {
            startActivity(new Intent(CategoriasActivity.this, DashboardActivity.class));
            finish();
        });
        tabConfig.setOnClickListener(v -> {
            startActivity(new Intent(CategoriasActivity.this, ConfiguracoesActivity.class));
            finish();
        });

        // Carrega as categorias
        carregarCategorias();
    }

    private void criarPaletaCores() {
        for (String cor : CORES) {
            View circulo = new View(this);
            int tamanho = (int) (32 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tamanho, tamanho);
            params.setMargins(8, 0, 8, 0);
            circulo.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(cor));
            circulo.setBackground(drawable);

            circulo.setOnClickListener(v -> {
                corSelecionada = cor;
                atualizarCorPrevia();
                destacarCorSelecionada();
            });

            circulo.setTag(cor);
            containerCores.addView(circulo);
        }
        // Marca a primeira cor como selecionada
        destacarCorSelecionada();
        atualizarCorPrevia();
    }

    private void destacarCorSelecionada() {
        for (int i = 0; i < containerCores.getChildCount(); i++) {
            View v = containerCores.getChildAt(i);
            String corDoView = (String) v.getTag();
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(corDoView));

            if (corDoView.equals(corSelecionada)) {
                // Selecionada: borda branca grossa
                drawable.setStroke(4, Color.WHITE);
            } else {
                drawable.setStroke(0, Color.TRANSPARENT);
            }
            v.setBackground(drawable);
        }
    }

    private void atualizarCorPrevia() {
        try {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(24f);
            drawable.setColor(Color.parseColor(corSelecionada));
            drawable.setStroke(1, Color.parseColor("#334155"));
            tvPreviaTag.setBackground(drawable);

            // Ajusta cor do texto conforme o fundo (simples)
            tvPreviaTag.setTextColor(Color.WHITE);
        } catch (Exception e) {
            // fallback
        }
    }

    private void salvarCategoria() {
        String nome = editNomeCategoria.getText().toString().trim();

        if (nome.isEmpty()) {
            Toast.makeText(this, "Digite um nome para a categoria!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica se já existe
        Categoria existente = db.categoriaDao().buscarPorNome(session.getUserId(), nome);
        if (existente != null) {
            Toast.makeText(this, "Já existe uma categoria com esse nome!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria a nova categoria
        Categoria nova = new Categoria(session.getUserId(), nome, corSelecionada);
        long id = db.categoriaDao().inserir(nova);

        if (id > 0) {
            Toast.makeText(this, "Categoria criada com sucesso!", Toast.LENGTH_SHORT).show();
            editNomeCategoria.setText("");
            tvPreviaTag.setText("Prévia da tag");
            carregarCategorias();
        } else {
            Toast.makeText(this, "Erro ao criar categoria.", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarCategorias() {
        categorias = db.categoriaDao().listarPorUsuario(session.getUserId());
        contagens = new ArrayList<>();

        for (Categoria c : categorias) {
            int count = db.categoriaDao().contarDividasPorCategoria(session.getUserId(), c.getNome());
            contagens.add(count);
        }

        adapter = new CategoriaAdapter(categorias, contagens, this);
        rvCategorias.setAdapter(adapter);
    }

    @Override
    public void onExcluirClick(Categoria categoria) {
        int count = db.categoriaDao().contarDividasPorCategoria(session.getUserId(), categoria.getNome());

        String mensagem = "Tem certeza que deseja excluir \"" + categoria.getNome() + "\"?";
        if (count > 0) {
            mensagem += "\n\nAtenção: existem " + count + " dívida(s) usando essa categoria.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir categoria")
                .setMessage(mensagem)
                .setPositiveButton("Excluir", (dialog, which) -> {
                    db.categoriaDao().deletar(categoria);
                    carregarCategorias();
                    Toast.makeText(this, "Categoria excluída!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}