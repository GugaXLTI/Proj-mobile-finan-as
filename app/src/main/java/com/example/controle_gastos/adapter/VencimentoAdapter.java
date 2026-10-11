package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.VencimentoItem;

import java.util.List;
import java.util.Locale;

public class VencimentoAdapter extends RecyclerView.Adapter<VencimentoAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private List<VencimentoItem> items;

    public VencimentoAdapter(List<VencimentoItem> items) {
        this.items = items;
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
        VencimentoItem item = items.get(position);

        holder.tvInicial.setText(item.inicial);
        holder.tvTitulo.setText(item.titulo);

        if (item.parcelado) {
            // ⭐ Parcelado: mostra "3 parcelas restantes" + próxima + total restante
            holder.tvParcela.setVisibility(View.VISIBLE);
            holder.tvParcela.setText(item.parcelasRestantes +
                    (item.parcelasRestantes == 1 ? " parcela restante" : " parcelas restantes"));

            holder.tvDetalhe.setText("Próxima: " + item.proximoVencimento);

            holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", item.valorProxima));
            holder.tvValor.setTextColor(0xFFEF4444);

            holder.tvTotal.setVisibility(View.VISIBLE);
            holder.tvTotal.setText(String.format(LOCALE_BR, "Total restante: R$ %.2f", item.totalRestante));
        } else {
            // ⭐ À vista: só o valor
            holder.tvParcela.setVisibility(View.GONE);
            holder.tvDetalhe.setText("Vence em " + item.proximoVencimento);
            holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", item.valorProxima));
            holder.tvValor.setTextColor(0xFFEF4444);
            holder.tvTotal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvInicial, tvTitulo, tvDetalhe, tvValor;
        TextView tvParcela, tvTotal;

        ViewHolder(View itemView) {
            super(itemView);
            tvInicial = itemView.findViewById(R.id.tvInicialVencimento);
            tvTitulo = itemView.findViewById(R.id.tvTituloVencimento);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheVencimento);
            tvValor = itemView.findViewById(R.id.tvValorVencimento);
            tvParcela = itemView.findViewById(R.id.tvParcelaVencimento);
            tvTotal = itemView.findViewById(R.id.tvTotalVencimento);
        }
    }
}