package com.example.controle_gastos.view;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.CartaoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;

public class CartoesFragment extends Fragment {

    private static final String[] INSTITUICOES = {"Nubank", "Inter", "Itaú", "Bradesco", "Santander", "Caixa"};
    private static final String[] TIPOS = {"Crédito", "Débito"};

    private NestedScrollView scrollCartoes;
    private RecyclerView rvCartoes;
    private CartaoAdapter adapter;
    private AppDatabase db;
    private SessionManager session;
    private List<Cartao> listaCartoes = new ArrayList<>();

    private ChipGroup chipGroupInstituicao, chipGroupTipoCartao;
    private EditText etApelido;
    private MaterialButton btnGuardar;
    private TextView btnCancelarEdicao, tvQtdCartoes, btnAutorizarLeitura;
    private View llAvisoPermissao, llVazioCartoes;

    // ⭐ Variável que guarda o cartão em edição (null = modo cadastro)
    private Cartao cartaoEmEdicao = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cartoes, container, false);

        db = AppDatabase.getInstance(requireContext());
        session = new SessionManager(requireContext());

        scrollCartoes = view.findViewById(R.id.scrollCartoes);
        rvCartoes = view.findViewById(R.id.rvCartoes);
        rvCartoes.setLayoutManager(new LinearLayoutManager(getContext()));

        chipGroupInstituicao = view.findViewById(R.id.chipGroupInstituicao);
        chipGroupTipoCartao = view.findViewById(R.id.chipGroupTipoCartao);
        etApelido = view.findViewById(R.id.etApelidoCartao);
        btnGuardar = view.findViewById(R.id.btnGuardarCartao);
        btnCancelarEdicao = view.findViewById(R.id.btnCancelarEdicaoCartao);
        tvQtdCartoes = view.findViewById(R.id.tvQtdCartoes);
        llAvisoPermissao = view.findViewById(R.id.llAvisoPermissao);
        llVazioCartoes = view.findViewById(R.id.llVazioCartoes);
        btnAutorizarLeitura = view.findViewById(R.id.btnAutorizarLeitura);

        configurarChips();

        btnGuardar.setOnClickListener(v -> salvarCartao());
        btnCancelarEdicao.setOnClickListener(v -> cancelarEdicao());
        btnAutorizarLeitura.setOnClickListener(v -> abrirConfiguracaoNotificacoes());

        carregarCartoes();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Ao voltar das configurações do Android, atualiza o aviso e o status dos cards
        atualizarStatusLeitura();
    }

    // ------------------------------------------------------------------
    // CHIPS
    // ------------------------------------------------------------------
    private void configurarChips() {
        for (String inst : INSTITUICOES) {
            chipGroupInstituicao.addView(criarChip(inst, CartaoAdapter.corDoBanco(inst)));
        }
        for (String tipo : TIPOS) {
            chipGroupTipoCartao.addView(criarChip(tipo, 0));
        }
        marcarPrimeiro(chipGroupInstituicao);
        marcarPrimeiro(chipGroupTipoCartao);
    }

    private Chip criarChip(String texto, int corDot) {
        Chip chip = new Chip(requireContext());
        chip.setText(texto);
        chip.setCheckable(true);
        chip.setClickable(true);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}};
        chip.setChipBackgroundColor(new ColorStateList(states,
                new int[]{Color.parseColor("#123D33"), Color.parseColor("#111A2E")}));
        chip.setTextColor(new ColorStateList(states,
                new int[]{Color.parseColor("#34D399"), Color.parseColor("#E2E8F0")}));
        chip.setChipStrokeColor(new ColorStateList(states,
                new int[]{Color.parseColor("#10B981"), Color.parseColor("#1E2A44")}));
        chip.setChipStrokeWidth(2f);
        chip.setChipCornerRadius(20f);

        if (corDot != 0) {
            GradientDrawable bolinha = new GradientDrawable();
            bolinha.setShape(GradientDrawable.OVAL);
            bolinha.setColor(corDot);
            chip.setChipIcon(bolinha);
            chip.setChipIconTint(null);
            chip.setChipIconSize(dp(10));
            chip.setChipIconVisible(true);
            chip.setIconStartPadding(dp(6));
            chip.setTextStartPadding(dp(2));
            chip.setCheckedIconVisible(false);
        }
        return chip;
    }

    private void marcarPrimeiro(ChipGroup grupo) {
        if (grupo.getChildCount() > 0) ((Chip) grupo.getChildAt(0)).setChecked(true);
    }

    private String valorSelecionado(ChipGroup grupo) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            View v = grupo.getChildAt(i);
            if (v instanceof Chip && ((Chip) v).isChecked()) {
                return ((Chip) v).getText().toString();
            }
        }
        return "";
    }

    private boolean selecionarChip(ChipGroup grupo, String texto) {
        for (int i = 0; i < grupo.getChildCount(); i++) {
            View v = grupo.getChildAt(i);
            if (v instanceof Chip && ((Chip) v).getText().toString().equalsIgnoreCase(texto)) {
                ((Chip) v).setChecked(true);
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // SALVAR / EDITAR
    // ------------------------------------------------------------------
    private void salvarCartao() {
        final String tipo = valorSelecionado(chipGroupTipoCartao);
        final String instituicao = valorSelecionado(chipGroupInstituicao);

        if (tipo.isEmpty() || instituicao.isEmpty()) {
            Toast.makeText(getContext(), "Selecione o banco e o tipo do cartão.", Toast.LENGTH_SHORT).show();
            return;
        }

        String apelidoDigitado = etApelido.getText().toString().trim();
        final String apelido = apelidoDigitado.isEmpty() ? instituicao : apelidoDigitado;

        // Evita cadastrar o mesmo banco/tipo duas vezes
        for (Cartao c : listaCartoes) {
            String tipoExistente = c.tipo != null ? c.tipo : "Crédito";
            boolean mesmoBanco = c.instituicao != null
                    && c.instituicao.equalsIgnoreCase(instituicao)
                    && tipoExistente.equalsIgnoreCase(tipo);
            boolean outroRegistro = cartaoEmEdicao == null || c.id != cartaoEmEdicao.id;
            if (mesmoBanco && outroRegistro) {
                Toast.makeText(getContext(), "Você já cadastrou " + instituicao + " (" + tipo + ").", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // ⭐ Se está editando, atualiza; senão, insere novo
        if (cartaoEmEdicao != null) {
            final Cartao editado = cartaoEmEdicao;
            editado.tipo = tipo;
            editado.instituicao = instituicao;
            editado.apelido = apelido;
            // últimos 4 dígitos e vencimento de cartões antigos são preservados

            new Thread(() -> {
                db.cartaoDao().atualizar(editado);
                runUi(() -> {
                    Toast.makeText(requireContext(), "Banco atualizado!", Toast.LENGTH_SHORT).show();
                    cancelarEdicao();
                    carregarCartoes();
                });
            }).start();
        } else {
            final Cartao cartao = new Cartao();
            cartao.usuarioId = session.getUserId();
            cartao.instituicao = instituicao;
            cartao.apelido = apelido;
            cartao.ultimos4Digitos = "";   // não é mais pedido ao usuário
            cartao.diaVencimento = "";     // não é mais pedido ao usuário
            cartao.tipo = tipo;

            new Thread(() -> {
                db.cartaoDao().inserir(cartao);
                runUi(() -> {
                    Toast.makeText(requireContext(), "Banco adicionado!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    carregarCartoes();
                });
            }).start();
        }
    }

    // ⭐ Entra no modo edição
    private void entrarModoEdicao(Cartao cartao) {
        cartaoEmEdicao = cartao;

        String instituicao = cartao.instituicao != null ? cartao.instituicao : INSTITUICOES[0];
        if (!selecionarChip(chipGroupInstituicao, instituicao)) {
            // Banco que não está na lista fixa: cria o chip para não perder o valor
            Chip extra = criarChip(instituicao, CartaoAdapter.corDoBanco(instituicao));
            chipGroupInstituicao.addView(extra);
            extra.setChecked(true);
        }

        String tipo = (cartao.tipo != null) ? cartao.tipo : "Crédito";
        selecionarChip(chipGroupTipoCartao, tipo);

        String apelido = cartao.apelido != null ? cartao.apelido.trim() : "";
        etApelido.setText(apelido.equalsIgnoreCase(instituicao) ? "" : apelido);

        btnGuardar.setText("Atualizar banco");
        btnCancelarEdicao.setVisibility(View.VISIBLE);

        // Sobe para o formulário
        scrollCartoes.smoothScrollTo(0, 0);
    }

    // ⭐ Cancela a edição
    private void cancelarEdicao() {
        cartaoEmEdicao = null;
        limparCampos();
        btnGuardar.setText("Adicionar banco");
        btnCancelarEdicao.setVisibility(View.GONE);
    }

    private void limparCampos() {
        etApelido.setText("");
        marcarPrimeiro(chipGroupInstituicao);
        marcarPrimeiro(chipGroupTipoCartao);
    }

    // ------------------------------------------------------------------
    // LISTA
    // ------------------------------------------------------------------
    private void carregarCartoes() {
        new Thread(() -> {
            final List<Cartao> carregados = db.cartaoDao().listarPorUsuario(session.getUserId());
            runUi(() -> {
                listaCartoes = carregados;
                adapter = new CartaoAdapter(listaCartoes, session.getNome());

                // ⭐ Clique normal = editar
                adapter.setOnItemClickListener(cartao -> entrarModoEdicao(cartao));

                // ⭐ Clique longo (ou lixeira) = excluir
                adapter.setOnItemLongClickListener(cartao -> mostrarDialogoExcluir(cartao));

                rvCartoes.setAdapter(adapter);
                atualizarStatusLeitura();
                atualizarVazioEContador();
            });
        }).start();
    }

    private void atualizarVazioEContador() {
        int n = listaCartoes.size();
        tvQtdCartoes.setText(n + (n == 1 ? " banco" : " bancos"));
        llVazioCartoes.setVisibility(n == 0 ? View.VISIBLE : View.GONE);
        rvCartoes.setVisibility(n == 0 ? View.GONE : View.VISIBLE);
    }

    private void mostrarDialogoExcluir(Cartao cartao) {
        String tipo = cartao.tipo != null ? cartao.tipo : "Crédito";
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir banco")
                .setMessage("Tem certeza que deseja excluir " + cartao.instituicao + " (" + tipo + ")?\n\n" +
                        "O ORG deixa de monitorar as notificações desse cadastro. Suas dívidas não são apagadas.")
                .setPositiveButton("Sim, excluir", (dialog, which) -> excluirCartao(cartao))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirCartao(Cartao cartao) {
        new Thread(() -> {
            db.cartaoDao().deletar(cartao);
            runUi(() -> {
                Toast.makeText(requireContext(), "Banco excluído!", Toast.LENGTH_SHORT).show();
                // Se estava editando este cadastro, cancela a edição
                if (cartaoEmEdicao != null && cartaoEmEdicao.id == cartao.id) {
                    cancelarEdicao();
                }
                carregarCartoes();
            });
        }).start();
    }

    // ------------------------------------------------------------------
    // LEITURA DE NOTIFICAÇÕES
    // ------------------------------------------------------------------
    private boolean notificacoesAutorizadas() {
        Context c = getContext();
        if (c == null) return false;
        return NotificationManagerCompat.getEnabledListenerPackages(c).contains(c.getPackageName());
    }

    private void atualizarStatusLeitura() {
        boolean ok = notificacoesAutorizadas();
        if (llAvisoPermissao != null) llAvisoPermissao.setVisibility(ok ? View.GONE : View.VISIBLE);
        if (adapter != null) adapter.setLeituraAtiva(ok);
    }

    private void abrirConfiguracaoNotificacoes() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ex) {
                Toast.makeText(getContext(), "Não foi possível abrir as configurações.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    /** Só executa se o fragment ainda estiver na tela (evita crash com a Activity fechada). */
    private void runUi(Runnable acao) {
        FragmentActivity activity = getActivity();
        if (activity != null && isAdded()) activity.runOnUiThread(acao);
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}