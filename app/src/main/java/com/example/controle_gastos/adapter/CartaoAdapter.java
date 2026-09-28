package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Cartao;
import java.util.List;

public class CartaoAdapter extends RecyclerView.Adapter<CartaoAdapter.ViewHolder> {

    private List<Cartao> listaCartoes;

    public CartaoAdapter(List<Cartao> listaCartoes) {
        this.listaCartoes = listaCartoes;
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

        holder.tvNomeCartao.setText(cartao.instituicao + " • " + cartao.apelido);
        holder.tvDetalhesCartao.setText("•••• •••• " + cartao.ultimos4Digitos + " | Vence dia " + cartao.diaVencimento);
        // Aqui você pode adicionar lógica para mudar a cor do status ou o ícone do banco
        holder.tvStatusCartao.setText("Ativo");
    }

    @Override
    public int getItemCount() {
        return listaCartoes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeCartao, tvDetalhesCartao, tvStatusCartao;
        ImageView ivBandeira;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeCartao = itemView.findViewById(R.id.tvNomeCartao);
            tvDetalhesCartao = itemView.findViewById(R.id.tvDetalhesCartao);
            tvStatusCartao = itemView.findViewById(R.id.tvStatusCartao);
            ivBandeira = itemView.findViewById(R.id.ivBandeira);
        }
    }
}