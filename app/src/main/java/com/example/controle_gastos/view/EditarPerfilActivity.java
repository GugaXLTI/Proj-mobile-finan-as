package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class EditarPerfilActivity extends AppCompatActivity {

    private ImageView btnVoltarPerfil;
    private TextView tvAvatarPerfil, btnEliminarConta;
    private EditText editNomePerfil, editEmailPerfil, editSenhaAtual,
            editNovaSenha, editConfirmarNovaSenha;
    private MaterialButton btnSalvarPerfil;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;
    private Usuario usuarioLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        btnVoltarPerfil = findViewById(R.id.btnVoltarPerfil);
        tvAvatarPerfil = findViewById(R.id.tvAvatarPerfil);
        editNomePerfil = findViewById(R.id.editNomePerfil);
        editEmailPerfil = findViewById(R.id.editEmailPerfil);
        editSenhaAtual = findViewById(R.id.editSenhaAtual);
        editNovaSenha = findViewById(R.id.editNovaSenha);
        editConfirmarNovaSenha = findViewById(R.id.editConfirmarNovaSenha);
        btnSalvarPerfil = findViewById(R.id.btnSalvarPerfil);
        btnEliminarConta = findViewById(R.id.btnEliminarConta);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        carregarUsuario();

        btnVoltarPerfil.setOnClickListener(v -> {
            startActivity(new Intent(this, ConfiguracoesActivity.class));
            finish();
        });

        btnSalvarPerfil.setOnClickListener(v -> salvarAlteracoes());
        btnEliminarConta.setOnClickListener(v -> confirmarExclusao());

        tabInicio.setOnClickListener(v -> {
            startActivity(new Intent(this, InicioActivity.class));
            finish();
        });
        tabLancar.setOnClickListener(v -> {
            startActivity(new Intent(this, CadastroDividaActivity.class));
        });
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

    /**
     * ⭐ Carrega o usuário logado com 3 fallbacks para evitar o erro "Erro ao carregar dados".
     *
     * 1. Busca por ID (sessão)
     * 2. Busca por nome (sessão)
     * 3. Se só tem 1 usuário no banco, pega ele
     * 4. Se nada funcionar, força logout
     */
    private void carregarUsuario() {
        int userId = session.getUserId();

        // Tentativa 1: por ID
        usuarioLogado = db.usuarioDao().buscarPorId(userId);

        // Tentativa 2: por nome da sessão
        if (usuarioLogado == null) {
            String nomeSessao = session.getNome();
            if (nomeSessao != null && !nomeSessao.isEmpty()) {
                usuarioLogado = db.usuarioDao().buscarPorNome(nomeSessao);
            }
        }

        // Tentativa 3: se só existe 1 usuário no banco
        if (usuarioLogado == null) {
            int total = db.usuarioDao().contarUsuarios();
            if (total == 1) {
                usuarioLogado = db.usuarioDao().buscarPrimeiroUsuario();
            }
        }

        // Nada funcionou: sessão expirada
        if (usuarioLogado == null) {
            new AlertDialog.Builder(this)
                    .setTitle("Sessão expirada")
                    .setMessage("Sua sessão expirou ou os dados foram redefinidos. Faça login novamente.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (d, w) -> {
                        session.logout();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .show();
            return;
        }

        // ⭐ Se o userId da sessão não bate com o do banco, atualiza
        if (usuarioLogado.getId() != userId) {
            session.salvarSessao(usuarioLogado.getId(), usuarioLogado.getNome(), usuarioLogado.getEmail());
        }

        // Preenche os campos
        editNomePerfil.setText(usuarioLogado.getNome());
        editEmailPerfil.setText(usuarioLogado.getEmail());

        String nome = usuarioLogado.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvAvatarPerfil.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }
    }

    private void salvarAlteracoes() {
        String novoNome = editNomePerfil.getText().toString().trim();
        String novoEmail = editEmailPerfil.getText().toString().trim();
        String senhaAtual = editSenhaAtual.getText().toString().trim();
        String novaSenha = editNovaSenha.getText().toString().trim();
        String confirmarNovaSenha = editConfirmarNovaSenha.getText().toString().trim();

        if (usuarioLogado == null) {
            Toast.makeText(this, "Usuário não carregado. Tente novamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novoNome.isEmpty() || novoEmail.isEmpty() || senhaAtual.isEmpty()) {
            Toast.makeText(this, "Preencha nome, e-mail e senha atual!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novoNome.length() < 2) {
            Toast.makeText(this, "O nome deve ter pelo menos 2 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!novoNome.matches("^[A-Za-zÀ-ÿ\\s]+$")) {
            Toast.makeText(this, "O nome deve conter apenas letras.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValido(novoEmail)) {
            Toast.makeText(this, "E-mail inválido! Verifique o formato.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senhaAtual.equals(usuarioLogado.getSenha())) {
            Toast.makeText(this, "Senha atual incorreta!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!novoEmail.equals(usuarioLogado.getEmail())) {
            Usuario existente = db.usuarioDao().buscarPorEmail(novoEmail);
            if (existente != null) {
                Toast.makeText(this, "Este e-mail já está cadastrado por outro usuário!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (!novaSenha.isEmpty() || !confirmarNovaSenha.isEmpty()) {
            if (novaSenha.length() < 6) {
                Toast.makeText(this, "A nova senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!novaSenha.equals(confirmarNovaSenha)) {
                Toast.makeText(this, "As novas senhas não coincidem!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        usuarioLogado.setNome(novoNome);
        usuarioLogado.setEmail(novoEmail);

        if (!novaSenha.isEmpty()) {
            usuarioLogado.setSenha(novaSenha);
        }

        db.usuarioDao().atualizar(usuarioLogado);

        // ⭐ Atualiza nome E e-mail na sessão
        session.atualizarNome(novoNome);
        session.atualizarEmail(novoEmail);

        Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmarExclusao() {
        if (usuarioLogado == null) {
            Toast.makeText(this, "Usuário não carregado.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Eliminar conta")
                .setMessage("Tem certeza que deseja eliminar sua conta?\n\nTodos os seus dados (dívidas, transações e conta) serão apagados permanentemente. Essa ação não pode ser desfeita.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    db.usuarioDao().deletar(usuarioLogado);
                    session.logout();

                    Toast.makeText(this, "Conta eliminada. Todos os dados foram apagados.", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(EditarPerfilActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private boolean isEmailValido(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
}