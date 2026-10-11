package com.example.controle_gastos.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.VencimentoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.VencimentoItem;
import com.example.controle_gastos.utils.NotificationHelper;
import com.example.controle_gastos.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InicioActivity extends AppCompatActivity {

    private static final int REQ_NOTIFICACAO = 100;
    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private TextView tvAvatarInicio, tvNomeInicio, tvTotalDividasInicio, tvTotalPagoInicio, tvAlertaMes;
    private RecyclerView rvVencimentos;
    private Button btnVerTodosVencimentos;
    private CardView cardTotalDividas;

    private TextView tabInicio, tabLancar, tabDividas, tabRelatorios, tabConfig;

    private AppDatabase db;
    private SessionManager session;
    private List<Divida> dividas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        db = AppDatabase.getInstance(this);
        session = new SessionManager(this);

        NotificationHelper.criarCanal(this);
        solicitarPermissaoNotificacao();

        tvAvatarInicio = findViewById(R.id.tvAvatarInicio);
        tvNomeInicio = findViewById(R.id.tvNomeInicio);
        tvTotalDividasInicio = findViewById(R.id.tvTotalDividasInicio);
        tvTotalPagoInicio = findViewById(R.id.tvTotalPagoInicio);
        tvAlertaMes = findViewById(R.id.tvAlertaMes);
        rvVencimentos = findViewById(R.id.rvVencimentos);
        btnVerTodosVencimentos = findViewById(R.id.btnVerTodosVencimentos);
        cardTotalDividas = findViewById(R.id.cardTotalDividas);

        tabInicio = findViewById(R.id.tabInicio);
        tabLancar = findViewById(R.id.tabLancar);
        tabDividas = findViewById(R.id.tabDividas);
        tabRelatorios = findViewById(R.id.tabRelatorios);
        tabConfig = findViewById(R.id.tabConfig);

        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }

        rvVencimentos.setLayoutManager(new LinearLayoutManager(this));

        carregarDados();

        cardTotalDividas.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoricoActivity.class));
        });

        btnVerTodosVencimentos.setOnClickListener(v -> {
            startActivity(new Intent(this, DividasActivity.class));
        });

        tabInicio.setOnClickListener(v ->
                Toast.makeText(this, "Você já está no Início", Toast.LENGTH_SHORT).show());

        tabLancar.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroDividaActivity.class)));

        tabDividas.setOnClickListener(v ->
                startActivity(new Intent(this, DividasActivity.class)));

        tabRelatorios.setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));

        tabConfig.setOnClickListener(v ->
                startActivity(new Intent(this, ConfiguracoesActivity.class)));
    }

    private void solicitarPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIFICACAO);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICACAO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Lembretes ativados!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sem permissão, os lembretes não vão funcionar.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String nome = session.getNome();
        if (nome != null && !nome.isEmpty()) {
            tvNomeInicio.setText(nome);
            tvAvatarInicio.setText(String.valueOf(nome.charAt(0)).toUpperCase());
        }
        carregarDados();
    }

    private void carregarDados() {
        dividas = db.dividaDao().listarPorUsuario(session.getUserId());

        // ⭐ Filtra apenas não pagas
        List<Divida> naoPagas = new ArrayList<>();
        for (Divida d : dividas) {
            if (!d.isPago()) naoPagas.add(d);
        }

        // ⭐ Agrupa por grupoId
        List<VencimentoItem> items = agruparVencimentos(naoPagas);

        // Mostra no máximo 3
        if (items.size() > 3) {
            items = items.subList(0, 3);
        }

        rvVencimentos.setAdapter(new VencimentoAdapter(items));

        atualizarTotais();
    }

    /**
     * ⭐ Agrupa dívidas não pagas por grupoId.
     *  - Dívidas com grupoId > 0 viram 1 item (com N parcelas restantes).
     *  - Dívidas sem grupo (grupoId = 0) viram items individuais.
     */
    private List<VencimentoItem> agruparVencimentos(List<Divida> naoPagas) {
        Map<Integer, List<Divida>> grupos = new HashMap<>();
        List<Divida> semGrupo = new ArrayList<>();

        for (Divida d : naoPagas) {
            if (d.getGrupoId() > 0) {
                List<Divida> lista = grupos.get(d.getGrupoId());
                if (lista == null) {
                    lista = new ArrayList<>();
                    grupos.put(d.getGrupoId(), lista);
                }
                lista.add(d);
            } else {
                semGrupo.add(d);
            }
        }

        List<VencimentoItem> items = new ArrayList<>();

        // Processa grupos
        for (Map.Entry<Integer, List<Divida>> entry : grupos.entrySet()) {
            List<Divida> parcelas = entry.getValue();

            // Ordena por vencimento
            parcelas.sort((a, b) -> compararDatas(a.getVencimento(), b.getVencimento()));

            Divida proxima = parcelas.get(0);

            VencimentoItem item = new VencimentoItem();
            item.titulo = proxima.getTitulo();
            item.inicial = inicialDoBanco(proxima.getBanco());
            item.proximoVencimento = proxima.getVencimento();
            item.valorProxima = proxima.getValorTotal();
            item.parcelasRestantes = parcelas.size();
            item.totalParcelas = extrairTotalParcelas(proxima.getParcela());
            item.parcelado = item.totalParcelas > 1;

            double somaRestante = 0;
            for (Divida p : parcelas) somaRestante += p.getValorRestante();
            item.totalRestante = somaRestante;

            items.add(item);
        }

        // Processa individuais
        for (Divida d : semGrupo) {
            VencimentoItem item = new VencimentoItem();
            item.titulo = d.getTitulo();
            item.inicial = inicialDoBanco(d.getBanco());
            item.proximoVencimento = d.getVencimento();
            item.valorProxima = d.getValorTotal();
            item.parcelasRestantes = 1;
            item.totalParcelas = 1;
            item.parcelado = false;
            item.totalRestante = d.getValorRestante();
            items.add(item);
        }

        // Ordena tudo pela próxima data de vencimento
        items.sort((a, b) -> compararDatas(a.proximoVencimento, b.proximoVencimento));

        return items;
    }

    private String inicialDoBanco(String banco) {
        if (banco != null && !banco.isEmpty()) {
            return banco.substring(0, 1).toUpperCase();
        }
        return "?";
    }

    private void atualizarTotais() {
        double totalAPagar = 0;
        double totalPago = 0;
        double totalVencendoMes = 0;
        int contasVencendoMes = 0;

        Calendar c = Calendar.getInstance();
        int mesAtual = c.get(Calendar.MONTH) + 1;
        int anoAtual = c.get(Calendar.YEAR);

        for (Divida d : dividas) {
            if (d.isPago()) {
                totalPago += d.getValorTotal();
            } else {
                totalAPagar += d.getValorRestante();

                // ⭐ Verifica se vence NO MÊS ATUAL
                if (venceNoMes(d.getVencimento(), mesAtual, anoAtual)) {
                    totalVencendoMes += d.getValorRestante();
                    contasVencendoMes++;
                }
            }
        }

        tvTotalDividasInicio.setText(String.format(LOCALE_BR, "R$ %.2f", totalAPagar));
        tvTotalPagoInicio.setText(String.format(LOCALE_BR, "R$ %.2f", totalPago));

        // ⭐ Alerta inteligente
        if (contasVencendoMes > 0) {
            String texto = contasVencendoMes +
                    (contasVencendoMes == 1
                            ? " conta vencendo este mês • R$ "
                            : " contas vencendo este mês • R$ ") +
                    String.format(LOCALE_BR, "%.2f", totalVencendoMes);
            tvAlertaMes.setText(texto);
        } else {
            tvAlertaMes.setText("Nenhuma conta vence este mês ✓");
        }
    }

    private boolean venceNoMes(String vencimento, int mes, int ano) {
        if (vencimento == null || vencimento.isEmpty()) return false;
        try {
            String[] partes = vencimento.split("/");
            if (partes.length != 3) return false;
            int m = Integer.parseInt(partes[1]);
            int a = Integer.parseInt(partes[2]);
            return m == mes && a == ano;
        } catch (Exception e) {
            return false;
        }
    }

    private int compararDatas(String d1, String d2) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date data1 = sdf.parse(d1);
            Date data2 = sdf.parse(d2);
            if (data1 == null || data2 == null) return 0;
            return data1.compareTo(data2);
        } catch (Exception e) {
            return 0;
        }
    }

    private int extrairTotalParcelas(String parcela) {
        if (parcela == null) return 1;
        if (parcela.contains("/")) {
            try {
                String[] partes = parcela.split("/");
                String total = partes[1].replaceAll("[^0-9]", "");
                if (!total.isEmpty()) {
                    int n = Integer.parseInt(total);
                    return n > 0 ? n : 1;
                }
            } catch (Exception e) {
                return 1;
            }
        }
        try {
            String numeros = parcela.replaceAll("[^0-9]", "");
            if (numeros.isEmpty()) return 1;
            int n = Integer.parseInt(numeros);
            return n > 0 ? n : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}