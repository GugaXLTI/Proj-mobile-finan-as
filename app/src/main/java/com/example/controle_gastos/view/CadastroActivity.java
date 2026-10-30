package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText etNome, etPassword, etConfirmPassword;
    private MaterialButton btnCadastrar;
    private TextView tvLogin;
    private ImageView btnBack;

    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        etNome = findViewById(R.id.etNome);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        tvLogin = findViewById(R.id.tvLogin);
        btnBack = findViewById(R.id.btnBack);

        btnCadastrar.setOnClickListener(v -> realizarCadastro());

        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void realizarCadastro() {
        String nome = etNome.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();
        String confirmarSenha = etConfirmPassword.getText().toString().trim();

        if (nome.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nome.length() < 2) {
            Toast.makeText(this, "O nome deve ter pelo menos 2 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nome.matches("^[A-Za-zÀ-ÿ0-9_]+$")) {
            Toast.makeText(this, "O nome deve conter apenas letras, números e _ (sem espaços).", Toast.LENGTH_LONG).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        UsuarioDao usuarioDao = db.usuarioDao();
        Usuario existente = usuarioDao.buscarPorNome(nome);

        if (existente != null) {
            Toast.makeText(this, "Este nome de usuário já está em uso!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⭐ Ao criar conta nova, garante que a flag global está limpa
        session.setOnboardingConcluido(false);

        Usuario novoUsuario = new Usuario(nome, "", senha);
        long idGerado = usuarioDao.inserir(novoUsuario);

        if (idGerado > 0) {
            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
}