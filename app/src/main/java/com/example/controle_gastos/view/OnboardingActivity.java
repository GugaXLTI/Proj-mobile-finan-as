package com.example.controle_gastos.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.controle_gastos.R;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Usuario;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnAvancar;
    private View dot1, dot2;

    private SessionManager session;
    private AppDatabase db;
    private TextInputEditText editSalarioOnboarding;

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        session = new SessionManager(this);
        db = AppDatabase.getInstance(this);

        viewPager = findViewById(R.id.viewPagerOnboarding);
        btnAvancar = findViewById(R.id.btnAvancar);
        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);

        OnboardingAdapter adapter = new OnboardingAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    btnAvancar.setText("Começar");
                    dot1.getLayoutParams().width = (int) (24 * getResources().getDisplayMetrics().density);
                    dot1.setBackgroundResource(R.drawable.bg_dot_active);
                    dot2.getLayoutParams().width = (int) (8 * getResources().getDisplayMetrics().density);
                    dot2.setBackgroundResource(R.drawable.bg_dot_inactive);
                } else {
                    btnAvancar.setText("Concluir");
                    dot1.getLayoutParams().width = (int) (8 * getResources().getDisplayMetrics().density);
                    dot1.setBackgroundResource(R.drawable.bg_dot_inactive);
                    dot2.getLayoutParams().width = (int) (24 * getResources().getDisplayMetrics().density);
                    dot2.setBackgroundResource(R.drawable.bg_dot_active);
                }
                dot1.requestLayout();
                dot2.requestLayout();
            }
        });

        btnAvancar.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == 0) {
                viewPager.setCurrentItem(1, true);
            } else {
                finalizarOnboarding();
            }
        });
    }

    private void finalizarOnboarding() {
        if (editSalarioOnboarding == null) return;

        String salarioStr = editSalarioOnboarding.getText().toString()
                .replace("R$", "").replace(".", "").replace(",", ".").trim();

        if (salarioStr.isEmpty() || Double.parseDouble(salarioStr) <= 0) {
            Toast.makeText(this, "Por favor, informe um salário válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        double salario = Double.parseDouble(salarioStr);

        Usuario user = db.usuarioDao().buscarPorId(session.getUserId());
        if (user != null) {
            user.setSalarioMensal(salario);
            db.usuarioDao().atualizar(user);
            session.setOnboardingConcluido(true);

            Toast.makeText(this, "Tudo pronto! Vamos começar.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(OnboardingActivity.this, InicioActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Erro ao buscar usuário.", Toast.LENGTH_SHORT).show();
        }
    }

    private class OnboardingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == 0) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_welcome, parent, false);
                return new WelcomeViewHolder(view);
            } else {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_salary, parent, false);
                return new SalaryViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof SalaryViewHolder && editSalarioOnboarding == null) {
                editSalarioOnboarding = ((SalaryViewHolder) holder).editSalario;
                configurarMascaraValor(editSalarioOnboarding);
            }
        }

        @Override
        public int getItemCount() { return 2; }

        @Override
        public int getItemViewType(int position) { return position; }

        class WelcomeViewHolder extends RecyclerView.ViewHolder {
            WelcomeViewHolder(@NonNull View itemView) { super(itemView); }
        }

        class SalaryViewHolder extends RecyclerView.ViewHolder {
            TextInputEditText editSalario;
            SalaryViewHolder(@NonNull View itemView) {
                super(itemView);
                editSalario = itemView.findViewById(R.id.editSalarioOnboarding);
            }
        }
    }

    private void configurarMascaraValor(TextInputEditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdating) return;
                isUpdating = true;

                String text = s.toString().replaceAll("[^0-9]", "");

                if (text.isEmpty()) {
                    editText.setText("");
                    isUpdating = false;
                    return;
                }

                if (text.length() > 11) text = text.substring(0, 11);

                double valor = Double.parseDouble(text) / 100.0;
                String formatted = String.format(LOCALE_BR, "R$ %.2f", valor);
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
            }
        });
    }
}