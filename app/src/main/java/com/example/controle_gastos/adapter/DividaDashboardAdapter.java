package com.example.controle_gastos.adapter;

import android.graphics.Color;
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

/**
 * Adapter SOMENTE LEITURA para o Dashboard.
 * Sem botões de ação — apenas exibe os dados.
 */
public class DividaDashboardAdapter extends RecyclerView.Adapter<DividaDashboardAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private List<Divida> dividas;

    public DividaDashboardAdapter(List<Divida> dividas) {
        this.dividas = dividas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_divida_dashboard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Divida d = dividas.get(position);

        holder.tvTitulo.setText(d.getTitulo());
        holder.tvDetalhe.setText(d.getBanco() + " • " + d.getCategoria());
        holder.tvParcela.setText("Parcela " + d.getParcela());
        holder.tvVencimento.setText("Vencimento: " + d.getVencimento());

        if (d.isPago()) {
            holder.tvValorRestante.setText("Pago ✓");
            holder.tvValorRestante.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.tvValorRestante.setText(
                    String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal())
            );
            holder.tvValorRestante.setTextColor(Color.parseColor("#EF4444"));
        }
    }

    @Override
    public int getItemCount() {
        return dividas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvValorRestante, tvDetalhe, tvParcela, tvVencimento;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloDivida);
            tvValorRestante = itemView.findViewById(R.id.tvValorRestante);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheDivida);
            tvParcela = itemView.findViewById(R.id.tvParcela);
            tvVencimento = itemView.findViewById(R.id.tvVencimento);
        }
    }
}