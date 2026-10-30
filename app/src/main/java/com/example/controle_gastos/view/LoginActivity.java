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

    private EditText editNome, editSenha;
    private Button btnEntrar;
    private MaterialButton btnBiometria;
    private TextView tvRodape;

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

        editNome = findViewById(R.id.editNome);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnBiometria = findViewById(R.id.btnBiometria);
        tvRodape = findViewById(R.id.tvRodape);

        destacarTextoCadastro();

        btnEntrar.setOnClickListener(v -> realizarLogin());

        tvRodape.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        btnBiometria.setOnClickListener(v -> realizarLoginBiometrico());

        verificarBiometria();
    }

    @Override
    protected void onResume() {
        super.onResume();
        verificarBiometria();
    }

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
        String nome = editNome.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (nome.isEmpty() || senha.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        UsuarioDao usuarioDao = db.usuarioDao();
        Usuario usuario = usuarioDao.login(nome, senha);

        if (usuario != null) {
            session.salvarSessao(usuario.getId(), usuario.getNome(), usuario.getEmail() != null ? usuario.getEmail() : "");

            if (session.isBiometriaAtiva()) {
                session.salvarUsuarioBiometrico(usuario.getId(), usuario.getNome(), usuario.getEmail() != null ? usuario.getEmail() : "");
            }

            Toast.makeText(this, "Bem-vindo, " + usuario.getNome() + "!", Toast.LENGTH_SHORT).show();
            redirecionarParaInicio();
        } else {
            Toast.makeText(this, "Nome ou senha inválidos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void realizarLoginBiometrico() {
        BiometricHelper.autenticar(
                this,
                "Login com biometria",
                "Use sua digital para entrar no ORG",
                new BiometricHelper.Callback() {
                    @Override
                    public void onSucesso() {
                        int userId = session.getBiometriaUserId();
                        String nome = session.getBiometriaNome();
                        String email = session.getBiometriaEmail();

                        if (userId == -1) {
                            Toast.makeText(LoginActivity.this, "Nenhum usuário salvo para biometria.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        session.salvarSessao(userId, nome, email);
                        Toast.makeText(LoginActivity.this, "Bem-vindo de volta, " + nome + "!", Toast.LENGTH_SHORT).show();
                        redirecionarParaInicio();
                    }

                    @Override
                    public void onFalha(String motivo) {
                        Toast.makeText(LoginActivity.this, "Biometria não reconhecida. Tente novamente.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // ⭐ CORREÇÃO ARQUITETURAL: A decisão agora é baseada no Banco de Dados
    private void redirecionarParaInicio() {
        int userId = session.getUserId();

        // Verifica se o usuário tem rendas cadastradas (se tiver, já fez o onboarding)
        boolean temRendaCadastrada = !db.rendaDao().listarPorUsuario(userId).isEmpty();

        // Fallback de segurança: se for um usuário antigo (antes da v12), ele tem salarioMensal > 0
        Usuario user = db.usuarioDao().buscarPorId(userId);
        boolean temSalarioAntigo = user != null && user.getSalarioMensal() > 0;

        if (temRendaCadastrada || temSalarioAntigo) {
            // Usuário já configurado
            startActivity(new Intent(LoginActivity.this, InicioActivity.class));
        } else {
            // Conta nova ou usuário que nunca configurou a renda
            startActivity(new Intent(LoginActivity.this, OnboardingActivity.class));
        }
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