package com.example.controle_gastos.view;

import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.controle_gastos.R;

public class CartoesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cartoes);

        // Carrega o fragment de cartões diretamente
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_fragment, new CartoesFragment())
                    .commit();
        }

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }
}