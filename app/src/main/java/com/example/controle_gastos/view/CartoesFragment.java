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
import androidx.appcompat.app.AlertDialog; // ⭐ NOVO
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
    private Button btnGuardar;

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

        String[] tipos = {"Crédito", "Débito"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tipos);
        adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoCartao.setAdapter(adapterTipo);

        String[] instituicoes = {"Nubank", "Inter", "Itaú", "Bradesco", "Santander", "Caixa"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, instituicoes);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerInstituicao.setAdapter(spinnerAdapter);

        btnGuardar.setOnClickListener(v -> salvarCartao());

        carregarCartoes();
        return view;
    }

    private void salvarCartao() {
        String tipo = spinnerTipoCartao.getSelectedItem().toString();
        String instituicao = spinnerInstituicao.getSelectedItem().toString();
        String apelido = etApelido.getText().toString();
        String ultimos4 = etUltimos4.getText().toString();
        String vencimento = etDiaVencimento.getText().toString();

        if (apelido.isEmpty() || ultimos4.isEmpty() || vencimento.isEmpty()) {
            Toast.makeText(getContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

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
                etApelido.setText("");
                etUltimos4.setText("");
                etDiaVencimento.setText("");
                carregarCartoes();
            });
        }).start();
    }

    private void carregarCartoes() {
        new Thread(() -> {
            listaCartoes = db.cartaoDao().listarPorUsuario(session.getUserId());
            requireActivity().runOnUiThread(() -> {
                adapter = new CartaoAdapter(listaCartoes, session.getNome());

                // ⭐ Configura o listener de clique longo
                adapter.setOnItemLongClickListener(cartao -> {
                    mostrarDialogoExcluir(cartao);
                });

                rvCartoes.setAdapter(adapter);
            });
        }).start();
    }

    // ⭐ Diálogo de confirmação para excluir o cartão
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
                carregarCartoes();
            });
        }).start();
    }
}