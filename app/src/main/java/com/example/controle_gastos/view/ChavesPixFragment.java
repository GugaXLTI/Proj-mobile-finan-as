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
import com.example.controle_gastos.adapter.ChavePixAdapter;
import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.ChavePix;
import com.example.controle_gastos.utils.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class ChavesPixFragment extends Fragment {

    private RecyclerView rvChaves;
    private ChavePixAdapter adapter;
    private AppDatabase db;
    private SessionManager session;
    private List<ChavePix> listaChaves = new ArrayList<>();

    private Spinner spinnerTipo;
    private EditText etChave, etNome;
    private Button btnCadastrar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chaves_pix, container, false);

        db = AppDatabase.getInstance(requireContext());
        session = new SessionManager(requireContext());

        rvChaves = view.findViewById(R.id.rvChavesPix);
        rvChaves.setLayoutManager(new LinearLayoutManager(getContext()));

        spinnerTipo = view.findViewById(R.id.spinnerTipoChave);
        etChave = view.findViewById(R.id.etChavePix);
        etNome = view.findViewById(R.id.etNomeFavorecido);
        btnCadastrar = view.findViewById(R.id.btnCadastrarPix);

        String[] tipos = {"CNPJ/CPF", "Celular", "E-mail", "Chave Aleatória"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tipos);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(spinnerAdapter);

        btnCadastrar.setOnClickListener(v -> salvarChave());

        carregarChaves();
        return view;
    }

    private void salvarChave() {
        String tipo = spinnerTipo.getSelectedItem().toString();
        String chave = etChave.getText().toString();
        String nome = etNome.getText().toString();

        if (chave.isEmpty() || nome.isEmpty()) {
            Toast.makeText(getContext(), "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        ChavePix novaChave = new ChavePix();
        novaChave.usuarioId = session.getUserId();
        novaChave.tipoChave = tipo;
        novaChave.chave = chave;
        novaChave.nomeFavorecido = nome;
        novaChave.banco = "Inter";
        novaChave.apelidoDivida = "Dívida";

        new Thread(() -> {
            db.chavePixDao().inserir(novaChave);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Chave Pix salva!", Toast.LENGTH_SHORT).show();
                etChave.setText("");
                etNome.setText("");
                carregarChaves();
            });
        }).start();
    }

    private void carregarChaves() {
        new Thread(() -> {
            listaChaves = db.chavePixDao().listarPorUsuario(session.getUserId());
            requireActivity().runOnUiThread(() -> {
                adapter = new ChavePixAdapter(listaChaves);

                // ⭐ Configura o listener de clique longo
                adapter.setOnItemLongClickListener(chave -> {
                    mostrarDialogoExcluir(chave);
                });

                rvChaves.setAdapter(adapter);
            });
        }).start();
    }

    // ⭐ Diálogo de confirmação para excluir a chave Pix
    private void mostrarDialogoExcluir(ChavePix chave) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir chave Pix")
                .setMessage("Tem certeza que deseja excluir a chave de " + chave.nomeFavorecido + "?")
                .setPositiveButton("Sim, excluir", (dialog, which) -> excluirChave(chave))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirChave(ChavePix chave) {
        new Thread(() -> {
            db.chavePixDao().deletar(chave);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Chave Pix excluída!", Toast.LENGTH_SHORT).show();
                carregarChaves();
            });
        }).start();
    }
}