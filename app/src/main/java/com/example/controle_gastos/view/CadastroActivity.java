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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class CadastroActivity extends AppCompatActivity {

    private TextInputEditText etNome, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnCadastrar;
    private TextView tvLogin;
    private ImageView btnBack;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicializa o banco
        db = AppDatabase.getInstance(this);

        // Vincular componentes
        etNome = findViewById(R.id.etNome);
        etEmail = findViewById(R.id.etEmail);
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
        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();
        String confirmarSenha = etConfirmPassword.getText().toString().trim();

        // ======== VALIDAÇÃO 1: Campos preenchidos ========
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ======== VALIDAÇÃO 2: Nome (mínimo 2 letras, apenas letras) ========
        if (nome.length() < 2) {
            Toast.makeText(this, "O nome deve ter pelo menos 2 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!nome.matches("^[A-Za-zÀ-ÿ\\s]+$")) {
            Toast.makeText(this, "O nome deve conter apenas letras.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ======== VALIDAÇÃO 3: Formato do e-mail (Regex) ========
        if (!isEmailValido(email)) {
            Toast.makeText(this, "E-mail inválido! Verifique o formato (ex: nome@dominio.com).", Toast.LENGTH_LONG).show();
            return;
        }

        // ======== VALIDAÇÃO 4: Senha ========
        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ======== VALIDAÇÃO 5: E-mail duplicado ========
        UsuarioDao usuarioDao = db.usuarioDao();
        Usuario existente = usuarioDao.buscarPorEmail(email);

        if (existente != null) {
            Toast.makeText(this, "Este e-mail já está cadastrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ======== CADASTRO ========
        Usuario novoUsuario = new Usuario(nome, email, senha);
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

    // ======== VALIDAÇÃO DE E-MAIL COM REGEX ========
    private boolean isEmailValido(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
}