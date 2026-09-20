package com.example.controle_gastos.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.dao.UsuarioDao;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button btnEntrar, btnGoogle;
    private TextView tvEsqueceuSenha, tvRodape;

    private AppDatabase db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Inicializa o banco e a sessão
        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvEsqueceuSenha = findViewById(R.id.tvEsqueceuSenha);
        tvRodape = findViewById(R.id.tvRodape);
        btnGoogle = findViewById(R.id.btnGoogle);

        destacarTextoCadastro();

        btnEntrar.setOnClickListener(v -> realizarLogin());

        tvEsqueceuSenha.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Funcionalidade em breve!", Toast.LENGTH_SHORT).show()
        );

        tvRodape.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(LoginActivity.this, "Login com Google em breve!", Toast.LENGTH_SHORT).show()
        );
    }

    private void realizarLogin() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        UsuarioDao usuarioDao = db.usuarioDao();
        Usuario usuario = usuarioDao.login(email, senha);

        if (usuario != null) {
            // Salva a sessão
            session.salvarSessao(usuario.getId(), usuario.getNome());

            Toast.makeText(this, "Bem-vindo, " + usuario.getNome() + "!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "E-mail ou senha inválidos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void destacarTextoCadastro() {
        String textoCompleto = "Não tem uma conta? Cadastre-se";
        SpannableString spannableString = new SpannableString(textoCompleto);

        int inicio = textoCompleto.indexOf("Cadastre-se");
        int fim = inicio + "Cadastre-se".length();

        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#10B981")),
                inicio,
                fim,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvRodape.setText(spannableString);
    }
}