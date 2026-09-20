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

    private TextInputEditText etEmail, etPassword, etConfirmPassword;
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
        String email = etEmail.getText().toString().trim();
        String senha = etPassword.getText().toString().trim();
        String confirmarSenha = etConfirmPassword.getText().toString().trim();

        // Validações
        if (email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, "As senhas não coincidem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica se o e-mail já existe
        UsuarioDao usuarioDao = db.usuarioDao();
        Usuario existente = usuarioDao.buscarPorEmail(email);

        if (existente != null) {
            Toast.makeText(this, "Este e-mail já está cadastrado!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insere o novo usuário
        Usuario novoUsuario = new Usuario("Usuário", email, senha);
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