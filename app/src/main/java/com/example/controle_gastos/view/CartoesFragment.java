package com.example.controle_gastos.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.adapter.CartaoAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class CartoesFragment extends Fragment {

    private RecyclerView rvCartoes;
    private CartaoAdapter adapter;
    private AppDatabase db;
    private SessionManager session;
    private List<Cartao> listaCartoes = new ArrayList<>();

    private Spinner spinnerTipoCartao, spinnerInstituicao;
    private EditText etApelido, etUltimos4, etDiaVencimento;
    private Button btnGuardar, btnCancelarEdicao;

    // ⭐ Variável que guarda o cartão em edição (null = modo cadastro)
    private Cartao cartaoEmEdicao = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cartoes, container, false);

        db = AppDatabase.getInstance(requireContext());
        session = new SessionManager(requireContext());

        rvCartoes = view.findViewById(R.id.rvCartoes);
        rvCartoes.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerTipoCartao = view.findViewById(R.id.spinnerTipoCartao);
        spinnerInstituicao = view.findViewById(R.id.spinnerInstituicao);
        etApelido = view.findViewById(R.id.etApelidoCartao);
        etUltimos4 = view.findViewById(R.id.etUltimos4);
        etDiaVencimento = view.findViewById(R.id.etDiaVencimento);
        btnGuardar = view.findViewById(R.id.btnGuardarCartao);
        btnCancelarEdicao = view.findViewById(R.id.btnCancelarEdicaoCartao);

        String[] tipos = {"Crédito", "Débito"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoCartao.setAdapter(adapterTipo);

        String[] instituicoes = {"Nubank", "Inter", "Itaú", "Bradesco", "Santander", "Caixa"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, instituicoes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstituicao.setAdapter(spinnerAdapter);

        btnGuardar.setOnClickListener(v -> salvarCartao());
        btnCancelarEdicao.setOnClickListener(v -> cancelarEdicao());

        carregarCartoes();
        return view;
    }

    private void salvarCartao() {
        String tipo = spinnerTipoCartao.getSelectedItem().toString();
        String instituicao = spinnerInstituicao.getSelectedItem().toString();
        String apelido = etApelido.getText().toString().trim();
        String ultimos4 = etUltimos4.getText().toString().trim();
        String vencimento = etDiaVencimento.getText().toString().trim();

        if (apelido.isEmpty() || ultimos4.isEmpty() || vencimento.isEmpty()) {
            Toast.makeText(getContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⭐ Se está editando, atualiza; senão, insere novo
        if (cartaoEmEdicao != null) {
            cartaoEmEdicao.tipo = tipo;
            cartaoEmEdicao.instituicao = instituicao;
            cartaoEmEdicao.apelido = apelido;
            cartaoEmEdicao.ultimos4Digitos = ultimos4;
            cartaoEmEdicao.diaVencimento = vencimento;

            new Thread(() -> {
                db.cartaoDao().atualizar(cartaoEmEdicao);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Cartão atualizado!", Toast.LENGTH_SHORT).show();
                    cancelarEdicao();
                    carregarCartoes();
                });
            }).start();
        } else {
            Cartao cartao = new Cartao();
            cartao.usuarioId = session.getUserId();
            cartao.instituicao = instituicao;
            cartao.apelido = apelido;
            cartao.ultimos4Digitos = ultimos4;
            cartao.diaVencimento = vencimento;
            cartao.tipo = tipo;

            new Thread(() -> {
                db.cartaoDao().inserir(cartao);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Cartão salvo!", Toast.LENGTH_SHORT).show();
                    limparCampos();
                    carregarCartoes();
                });
            }).start();
        }
    }

    // ⭐ Entra no modo edição
    private void entrarModoEdicao(Cartao cartao) {
        cartaoEmEdicao = cartao;

        // Preenche os campos
        String tipo = (cartao.tipo != null) ? cartao.tipo : "Crédito";
        int posTipo = ((ArrayAdapter<String>) spinnerTipoCartao.getAdapter()).getPosition(tipo);
        spinnerTipoCartao.setSelection(Math.max(posTipo, 0));

        int posInstituicao = ((ArrayAdapter<String>) spinnerInstituicao.getAdapter()).getPosition(cartao.instituicao);
        spinnerInstituicao.setSelection(Math.max(posInstituicao, 0));

        etApelido.setText(cartao.apelido);
        etUltimos4.setText(cartao.ultimos4Digitos);
        etDiaVencimento.setText(cartao.diaVencimento);

        // Muda o botão principal e mostra o botão de cancelar
        btnGuardar.setText("✓ Atualizar Cartão");
        btnCancelarEdicao.setVisibility(View.VISIBLE);

        // Rola para o topo do formulário
        rvCartoes.smoothScrollToPosition(0);
    }

    // ⭐ Cancela a edição
    private void cancelarEdicao() {
        cartaoEmEdicao = null;
        limparCampos();
        btnGuardar.setText("+ Guardar Cartão");
        btnCancelarEdicao.setVisibility(View.GONE);
    }

    private void limparCampos() {
        etApelido.setText("");
        etUltimos4.setText("");
        etDiaVencimento.setText("");
        spinnerTipoCartao.setSelection(0);
        spinnerInstituicao.setSelection(0);
    }

    private void carregarCartoes() {
        new Thread(() -> {
            listaCartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            requireActivity().runOnUiThread(() -> {
                adapter = new CartaoAdapter(listaCartoes, session.getNome());

                // ⭐ Clique normal = editar
                adapter.setOnItemClickListener(cartao -> entrarModoEdicao(cartao));

                // ⭐ Clique longo = excluir
                adapter.setOnItemLongClickListener(cartao -> mostrarDialogoExcluir(cartao));

                rvCartoes.setAdapter(adapter);
            });
        }).start();
    }

    private void mostrarDialogoExcluir(Cartao cartao) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir cartão")
                .setMessage("Tem certeza que deseja excluir o cartão " +
                        cartao.instituicao + " (final " + cartao.ultimos4Digitos + ")?")
                .setPositiveButton("Sim, excluir", (dialog, which) -> excluirCartao(cartao))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirCartao(Cartao cartao) {
        new Thread(() -> {
            db.cartaoDao().deletar(cartao);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Cartão excluído!", Toast.LENGTH_SHORT).show();
                // Se estava editando este cartão, cancela a edição
                if (cartaoEmEdicao != null && cartaoEmEdicao.id == cartao.id) {
                    cancelarEdicao();
                }
                carregarCartoes();
            });
        }).start();
    }
}