package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;

public class SplashActivity extends AppCompatActivity {

    // Tempo de exibição da tela em milissegundos (2000 ms = 2 segundos)
    private static final int TEMPO_SPLASH = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Oculta a Action Bar superior para visual em tela cheia
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Timer para navegar para a LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Destrói a Splash para o botão 'Voltar' não retornar a ela
        }, TEMPO_SPLASH);
    }
}