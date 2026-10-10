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
import com.example.controle_gastos.utils.BiometricHelper;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button btnEntrar, btnGoogle;
    private MaterialButton btnBiometria;
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

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnBiometria = findViewById(R.id.btnBiometria);
        tvEsqueceuSenha = findViewById(R.id.tvEsqueceuSenha);
        tvRodape = findViewById(R.id.tvRodape);

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

        // ⭐ Biometria
        btnBiometria.setOnClickListener(v -> realizarLoginBiometrico());

        verificarBiometria();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Verifica de novo, caso o usuário tenha ativado/desativado a biometria nas Configurações
        verificarBiometria();
    }

    /**
     * Mostra o botão de biometria se:
     * 1. O usuário ativou a biometria nas Configurações
     * 2. O dispositivo tem hardware + digital cadastrada
     */
    private void verificarBiometria() {
        boolean ativo = session.isBiometriaAtiva();
        boolean disponivel = BiometricHelper.podeUsarBiometria(this);

        if (ativo && disponivel) {
            btnBiometria.setVisibility(android.view.View.VISIBLE);
        } else {
            btnBiometria.setVisibility(android.view.View.GONE);
        }
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
            session.salvarSessao(usuario.getId(), usuario.getNome(), usuario.getEmail());

            // ⭐ Se a biometria está ativa, atualiza os dados dela (garante que o usuário salvo é o correto)
            if (session.isBiometriaAtiva()) {
                session.salvarUsuarioBiometrico(usuario.getId(), usuario.getNome(), usuario.getEmail());
            }

            Toast.makeText(this, "Bem-vindo, " + usuario.getNome() + "!", Toast.LENGTH_SHORT).show();
            redirecionarParaInicio();
        } else {
            Toast.makeText(this, "E-mail ou senha inválidos!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ⭐ Login com biometria.
     */
    private void realizarLoginBiometrico() {
        BiometricHelper.autenticar(
                this,
                "Login com biometria",
                "Use sua digital para entrar no ORG",
                new BiometricHelper.Callback() {
                    @Override
                    public void onSucesso() {
                        // Recupera os dados salvos
                        int userId = session.getBiometriaUserId();
                        String nome = session.getBiometriaNome();
                        String email = session.getBiometriaEmail();

                        if (userId == -1) {
                            Toast.makeText(LoginActivity.this,
                                    "Nenhum usuário salvo para biometria.",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Restaura a sessão
                        session.salvarSessao(userId, nome, email);

                        Toast.makeText(LoginActivity.this,
                                "Bem-vindo de volta, " + nome + "!",
                                Toast.LENGTH_SHORT).show();

                        redirecionarParaInicio();
                    }

                    @Override
                    public void onFalha(String motivo) {
                        Toast.makeText(LoginActivity.this,
                                "Biometria não reconhecida. Tente novamente.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void redirecionarParaInicio() {
        Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
        startActivity(intent);
        finish();
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