package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Transacao;
import java.util.List;
import java.util.Locale;

public class LancamentoAdapter extends RecyclerView.Adapter<LancamentoAdapter.ViewHolder> {

    private List<Transacao> transacoes;

    public LancamentoAdapter(List<Transacao> transacoes) {
        this.transacoes = transacoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lancamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transacao t = transacoes.get(position);
        holder.tvDescricao.setText(t.getDescricao());
        holder.tvDetalhe.setText("Nubank • " + t.getCategoria());
        holder.tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", t.getValor()));
    }

    @Override
    public int getItemCount() {
        return transacoes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescricao, tvDetalhe, tvValor;
        ViewHolder(View itemView) {
            super(itemView);
            tvDescricao = itemView.findViewById(R.id.tvDescricaoLancamento);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheLancamento);
            tvValor = itemView.findViewById(R.id.tvValorLancamento);
        }
    }
}