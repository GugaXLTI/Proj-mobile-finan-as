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

    // ⭐ Interface para o clique longo (excluir)
    public interface OnItemLongClickListener {
        void onItemLongClick(Cartao cartao);
    }

    private List<Cartao> listaCartoes;
    private String nomeUsuario;
    private OnItemLongClickListener longClickListener;

    public CartaoAdapter(List<Cartao> listaCartoes, String nomeUsuario) {
        this.listaCartoes = listaCartoes;
        this.nomeUsuario = nomeUsuario;
    }

    // ⭐ Setter para o listener (chamado pelo Fragment)
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
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

        // ⭐ Clique longo para excluir
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(cartao);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return listaCartoes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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