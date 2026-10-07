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

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

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

        // Inicial do banco
        String inicial = d.getBanco() != null && !d.getBanco().isEmpty()
                ? d.getBanco().substring(0, 1).toUpperCase()
                : "?";
        holder.tvInicial.setText(inicial);

        holder.tvTitulo.setText(d.getTitulo());
        holder.tvDetalhe.setText("Vence em " + d.getVencimento());

        int totalParcelas = extrairTotalParcelas(d.getParcela());

        if (totalParcelas > 1) {
            // ⭐ Parcelado: mostra "Parcela 2/4" + valor da parcela + total do grupo
            holder.tvParcela.setText("Parcela " + d.getParcela());
            holder.tvParcela.setVisibility(View.VISIBLE);

            holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()));

            // Total do grupo = valor da parcela × total de parcelas
            double totalGrupo = d.getValorTotal() * totalParcelas;
            holder.tvTotal.setText(String.format(LOCALE_BR, "Total: R$ %.2f", totalGrupo));
            holder.tvTotal.setVisibility(View.VISIBLE);
        } else {
            // ⭐ À vista: só valor
            holder.tvParcela.setVisibility(View.GONE);
            holder.tvTotal.setVisibility(View.GONE);
            holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()));
        }
    }

    @Override
    public int getItemCount() {
        return dividas.size();
    }

    private int extrairTotalParcelas(String parcela) {
        if (parcela == null) return 1;
        if (parcela.contains("/")) {
            try {
                String[] partes = parcela.split("/");
                String total = partes[1].replaceAll("[^0-9]", "");
                if (!total.isEmpty()) {
                    int n = Integer.parseInt(total);
                    return n > 0 ? n : 1;
                }
            } catch (Exception e) {
                return 1;
            }
        }
        try {
            String numeros = parcela.replaceAll("[^0-9]", "");
            if (numeros.isEmpty()) return 1;
            int n = Integer.parseInt(numeros);
            return n > 0 ? n : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
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