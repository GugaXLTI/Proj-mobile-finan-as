package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Cartao;
import java.util.List;

public class CartaoAdapter extends RecyclerView.Adapter<CartaoAdapter.ViewHolder> {

    private List<Cartao> listaCartoes;
    private String nomeUsuario; // ⭐ NOVO: Nome do titular para exibir no cartão

    // ⭐ Construtor atualizado para receber o nome do usuário logado
    public CartaoAdapter(List<Cartao> listaCartoes, String nomeUsuario) {
        this.listaCartoes = listaCartoes;
        this.nomeUsuario = nomeUsuario;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cartao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cartao cartao = listaCartoes.get(position);

        // Nome do Banco + Tipo (Ex: Nubank • Crédito)
        String tipo = (cartao.tipo != null) ? cartao.tipo : "Crédito";
        holder.tvNomeCartao.setText(cartao.instituicao + " • " + tipo);

        holder.tvNumeroCartao.setText("•••• •••• " + cartao.ultimos4Digitos);
        holder.tvTitular.setText(nomeUsuario.toUpperCase());
        holder.tvVencimento.setText("Dia " + cartao.diaVencimento);

        if (cartao.bandeira != null && !cartao.bandeira.isEmpty()) {
            holder.tvBandeira.setText(cartao.bandeira.toUpperCase());
        } else {
            String sigla = cartao.instituicao.length() >= 2 ? cartao.instituicao.substring(0, 2) : cartao.instituicao;
            holder.tvBandeira.setText(sigla.toUpperCase());
        }
    }

    @Override
    public int getItemCount() {
        return listaCartoes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // ⭐ IDs atualizados conforme o novo layout item_cartao.xml
        TextView tvNomeCartao, tvBandeira, tvNumeroCartao, tvTitular, tvVencimento;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeCartao = itemView.findViewById(R.id.tvNomeCartao);
            tvBandeira = itemView.findViewById(R.id.tvBandeira);
            tvNumeroCartao = itemView.findViewById(R.id.tvNumeroCartao);
            tvTitular = itemView.findViewById(R.id.tvTitular);
            tvVencimento = itemView.findViewById(R.id.tvVencimento);
        }
    }
}