package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editSenha;
    private Button btnEntrar;
    private TextView tvEsqueceuSenha, tvRodape, tvDivisorTexto; // tvRodape = "Não tem uma conta? Cadastre-se"
    private Button btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Vincular os componentes do layout
        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvEsqueceuSenha = findViewById(R.id.tvEsqueceuSenha);
        tvRodape = findViewById(R.id.tvRodape); // TextView do rodapé
        btnGoogle = findViewById(R.id.btnGoogle);

        // Ação do botão Entrar
        btnEntrar.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            } else {
                // Navegar para a Dashboard (criar depois)
                Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish(); // Fecha a Login para não voltar com o botão "voltar"
            }
        });

        // Ação do link "Esqueceu a senha?"
        tvEsqueceuSenha.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Funcionalidade em breve!", Toast.LENGTH_SHORT).show();
        });

        // Ação do link "Não tem uma conta? Cadastre-se" (rodapé)
        tvRodape.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        // Ação do botão "Continuar com Google"
        btnGoogle.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Login com Google em breve!", Toast.LENGTH_SHORT).show();
        });
    }
}