package com.example.controle_gastos.view;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;
import com.example.controle_gastos.utils.BottomNavHelper;
import com.example.controle_gastos.utils.NavScrollHelper;

public class CartoesActivity extends AppCompatActivity {

    private ImageView btnVoltar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartoes);

        // Barra de navegação inferior (aba Config ativa, pois esta tela abre pelas Configurações)
        BottomNavHelper.configurar(this, BottomNavHelper.CONFIG);

        // Botão de voltar redondo
        btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(v -> finish());
        }

        if (savedInstanceState == null) {
            // Primeira abertura: carrega o fragment e já liga o esconde-barra
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_fragment, new CartoesFragment())
                    .commitNow(); // commitNow() deixa o fragment pronto antes do NavScrollHelper
            NavScrollHelper.attach(this);
        } else {
            // Tela recriada: o sistema restaura o fragment depois, então espera a tela montar
            View container = findViewById(R.id.container_fragment);
            container.post(() -> NavScrollHelper.attach(CartoesActivity.this));
        }
    }
}