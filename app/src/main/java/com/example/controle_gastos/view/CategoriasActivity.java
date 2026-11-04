package com.example.controle_gastos.view;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
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
import com.example.controle_gastos.utils.BottomNavHelper;
import com.example.controle_gastos.utils.CategoriaSeeder;
import com.example.controle_gastos.utils.NavScrollHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity implements CategoriaAdapter.OnCategoriaActionListener {

    private RecyclerView rvCategorias;
    private EditText editNomeCategoria;
    private TextView tvPreviaTag, tvQtdCategorias;
    private MaterialButton btnSalvarCategoria;
    private ImageView btnVoltar;
    private GridLayout containerCores;

    private AppDatabase db;
    private SessionManager session;
    private CategoriaAdapter adapter;
    private List<Categoria> categorias;
    private List<Integer> contagens;

    private String corSelecionada = "#A855F7";

    // ⭐ 12 Cores para ficar igual à tela de Dívidas
    private final String[] CORES = {
            "#10B981", "#34D399", "#3B82F6", "#06B6D4",
            "#F59E0B", "#FCD34D", "#EF4444", "#EC4899",
            "#8B5CF6", "#A855F7", "#64748B", "#94A3B8"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        NavScrollHelper.attach(this);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        // Barra inferior reutilizável (esta tela é aberta pelas Configurações)
        BottomNavHelper.configurar(this, BottomNavHelper.CONFIG);

        CategoriaSeeder.popularSeVazio(this, session.getUserId());

        rvCategorias = findViewById(R.id.rvCategorias);
        editNomeCategoria = findViewById(R.id.editNomeCategoria);
        tvPreviaTag = findViewById(R.id.tvPreviaTag);
        tvQtdCategorias = findViewById(R.id.tvQtdCategorias);
        btnSalvarCategoria = findViewById(R.id.btnSalvarCategoria);
        btnVoltar = findViewById(R.id.btnVoltar);
        containerCores = findViewById(R.id.containerCores);

        rvCategorias.setLayoutManager(new LinearLayoutManager(this));

        criarPaletaCores();

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

        btnSalvarCategoria.setOnClickListener(v -> salvarCategoria());

        btnVoltar.setOnClickListener(v -> finish());

        carregarCategorias();
    }

    private void criarPaletaCores() {
        containerCores.removeAllViews();

        for (String cor : CORES) {
            View circulo = new View(this);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dp(36);
            params.height = dp(36);
            params.setMargins(dp(4), dp(4), dp(4), dp(4));
            circulo.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(Color.parseColor(cor));
            circulo.setBackground(drawable);
            circulo.setContentDescription("Cor " + cor);

            circulo.setOnClickListener(v -> {
                corSelecionada = cor;
                atualizarCorPrevia();
                destacarCorSelecionada();
            });

            circulo.setTag(cor);
            containerCores.addView(circulo);
        }
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
                drawable.setStroke(dp(3), Color.WHITE);
                v.setScaleX(1.1f);
                v.setScaleY(1.1f);
            } else {
                drawable.setStroke(0, Color.TRANSPARENT);
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
            v.setBackground(drawable);
        }
    }

    private void atualizarCorPrevia() {
        try {
            int cor = Color.parseColor(corSelecionada);

            // Fundo translúcido com a cor + borda na cor
            GradientDrawable fundo = new GradientDrawable();
            fundo.setShape(GradientDrawable.RECTANGLE);
            fundo.setCornerRadius(dp(18));
            fundo.setColor(Color.argb(40, Color.red(cor), Color.green(cor), Color.blue(cor)));
            fundo.setStroke(dp(1), cor);
            tvPreviaTag.setBackground(fundo);
            tvPreviaTag.setTextColor(Color.WHITE);

            // Bolinha da cor antes do texto
            GradientDrawable bolinha = new GradientDrawable();
            bolinha.setShape(GradientDrawable.OVAL);
            bolinha.setColor(cor);
            bolinha.setSize(dp(10), dp(10));
            tvPreviaTag.setCompoundDrawablesRelativeWithIntrinsicBounds(bolinha, null, null, null);
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

        Categoria existente = db.categoriaDao().buscarPorNome(session.getUserId(), nome);
        if (existente != null) {
            Toast.makeText(this, "Já existe uma categoria com esse nome!", Toast.LENGTH_SHORT).show();
            return;
        }

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

        int total = categorias.size();
        tvQtdCategorias.setText(total + (total == 1 ? " categoria" : " categorias"));
    }

    @Override
    public void onExcluirClick(Categoria categoria) {
        int count = db.categoriaDao().contarDividasPorCategoria(session.getUserId(), categoria.getNome());

        if (count > 0) {
            String textoDividas = count == 1 ? "1 dívida" : count + " dívidas";

            new AlertDialog.Builder(this)
                    .setTitle("Não é possível excluir")
                    .setMessage("A categoria \"" + categoria.getNome() + "\" está sendo usada por " +
                            textoDividas + ".\n\n" +
                            "Para excluí-la, primeiro altere a categoria dessas dívidas ou exclua-as.")
                    .setPositiveButton("Entendi", null)
                    .show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Excluir categoria")
                .setMessage("Tem certeza que deseja excluir \"" + categoria.getNome() + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    db.categoriaDao().deletar(categoria);
                    carregarCategorias();
                    Toast.makeText(this, "Categoria excluída!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}