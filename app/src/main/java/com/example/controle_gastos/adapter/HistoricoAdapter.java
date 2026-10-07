package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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

public class HistoricoAdapter extends RecyclerView.Adapter<HistoricoAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private List<Divida> lista;

    public HistoricoAdapter(List<Divida> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Divida d = lista.get(position);

        holder.tvTitulo.setText(d.getTitulo());
        holder.tvDetalhe.setText(d.getBanco() + " • " + d.getCategoria() + " • " + d.getVencimento());
        holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()));

        if (d.isExcluida()) {
            holder.tvIcone.setText("✗");
            aplicarCorIcone(holder, "#EF4444", "#3A1515");
            holder.tvStatus.setText("Excluída ✗");
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
            holder.tvStatus.setBackground(criarBadge("#3A1515"));
            holder.tvValor.setTextColor(Color.parseColor("#EF4444"));
            holder.tvValor.setPaintFlags(holder.tvValor.getPaintFlags() |
                    android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        } else if (d.isPago()) {
            holder.tvIcone.setText("✓");
            aplicarCorIcone(holder, "#10B981", "#1A3D35");
            holder.tvStatus.setText("Liquidado ✓");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            holder.tvStatus.setBackground(criarBadge("#1A3D35"));
            holder.tvValor.setTextColor(Color.parseColor("#10B981"));
            holder.tvValor.setPaintFlags(holder.tvValor.getPaintFlags() &
                    (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));

        } else {
            // ⏳ PENDENTE
            holder.tvIcone.setText("→");
            aplicarCorIcone(holder, "#F59E0B", "#2A1F0A");

            int totalParcelas = extrairTotalParcelas(d.getParcela());
            if (totalParcelas > 1) {
                holder.tvStatus.setText("Parcela " + d.getParcela());
            } else {
                holder.tvStatus.setText("Pendente");
            }

            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvStatus.setBackground(criarBadge("#2A1F0A"));
            holder.tvValor.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvValor.setPaintFlags(holder.tvValor.getPaintFlags() &
                    (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void aplicarCorIcone(ViewHolder holder, String corIcone, String corFundo) {
        holder.tvIcone.setTextColor(Color.parseColor(corIcone));
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(corFundo));
        holder.tvIcone.setBackground(circle);
    }

    private GradientDrawable criarBadge(String corFundo) {
        GradientDrawable badge = new GradientDrawable();
        badge.setShape(GradientDrawable.RECTANGLE);
        badge.setCornerRadius(20f);
        badge.setColor(Color.parseColor(corFundo));
        return badge;
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

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcone, tvTitulo, tvDetalhe, tvValor, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvIcone = itemView.findViewById(R.id.tvIconeEstado);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistorico);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheHistorico);
            tvValor = itemView.findViewById(R.id.tvValorHistorico);
            tvStatus = itemView.findViewById(R.id.tvStatusHistorico);
        }
    }
}