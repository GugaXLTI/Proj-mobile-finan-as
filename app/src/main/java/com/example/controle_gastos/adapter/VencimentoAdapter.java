package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;

import java.util.List;
import java.util.Locale;

public class VencimentoAdapter extends RecyclerView.Adapter<VencimentoAdapter.ViewHolder> {

    private List<Divida> dividas;

    public VencimentoAdapter(List<Divida> dividas) {
        this.dividas = dividas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vencimento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Divida d = dividas.get(position);

        // Inicial (primeira letra do banco)
        String inicial = d.getBanco() != null && !d.getBanco().isEmpty()
                ? d.getBanco().substring(0, 1).toUpperCase()
                : "?";
        holder.tvInicial.setText(inicial);

        // Título e detalhe
        holder.tvTitulo.setText(d.getTitulo());
        holder.tvDetalhe.setText(d.getCategoria() + " • Vence em " + d.getVencimento());

        // Valor
        holder.tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", d.getValorRestante()));
    }

    @Override
    public int getItemCount() {
        return dividas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInicial, tvTitulo, tvDetalhe, tvValor;

        ViewHolder(View itemView) {
            super(itemView);
            tvInicial = itemView.findViewById(R.id.tvInicialVencimento);
            tvTitulo = itemView.findViewById(R.id.tvTituloVencimento);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheVencimento);
            tvValor = itemView.findViewById(R.id.tvValorVencimento);
        }
    }
}