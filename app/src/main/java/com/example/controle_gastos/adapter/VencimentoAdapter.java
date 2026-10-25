package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.VencimentoItem;

import java.text.SimpleDateFormat;
import java.util.Date;
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

        // ⭐ NOVO: Quebrando a data para o quadradinho (DD/MM/YYYY)
        if (item.proximoVencimento != null && item.proximoVencimento.length() >= 5) {
            try {
                String dia = item.proximoVencimento.substring(0, 2);
                int mesNum = Integer.parseInt(item.proximoVencimento.substring(3, 5));
                String[] meses = {"JAN", "FEV", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OUT", "NOV", "DEZ"};
                String mesTexto = (mesNum >= 1 && mesNum <= 12) ? meses[mesNum - 1] : "MÊS";

                holder.tvDiaVencimento.setText(dia);
                holder.tvMesVencimento.setText(mesTexto);
            } catch (Exception e) {
                holder.tvDiaVencimento.setText("--");
                holder.tvMesVencimento.setText("---");
            }
        }

        holder.tvTitulo.setText(item.titulo);
        holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", item.valorProxima));

        if (item.parcelado) {
            // ⭐ Parcelado
            holder.tvParcela.setVisibility(View.VISIBLE);
            holder.tvParcela.setText(item.parcelasRestantes + " parcelas restantes");

            holder.tvDetalhe.setText("Próxima: " + item.proximoVencimento);
            holder.tvDetalhe.setTextColor(Color.parseColor("#F59E0B")); // Laranja igual ao print

            holder.tvTotal.setVisibility(View.VISIBLE);
            holder.tvTotal.setText(String.format(LOCALE_BR, "Total: R$ %.2f", item.totalRestante));
        } else {
            // ⭐ À vista
            holder.tvParcela.setVisibility(View.GONE);
            holder.tvDetalhe.setText("Vence em " + item.proximoVencimento);
            holder.tvDetalhe.setTextColor(Color.parseColor("#94A3B8")); // Cinza

            holder.tvTotal.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMesVencimento, tvDiaVencimento, tvTitulo, tvDetalhe, tvValor;
        TextView tvParcela, tvTotal;

        ViewHolder(View itemView) {
            super(itemView);
            // ⭐ IDs do novo layout item_vencimento.xml
            tvMesVencimento = itemView.findViewById(R.id.tvMesVencimento);
            tvDiaVencimento = itemView.findViewById(R.id.tvDiaVencimento);
            tvTitulo = itemView.findViewById(R.id.tvTituloVencimento);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheVencimento);
            tvValor = itemView.findViewById(R.id.tvValorVencimento);
            tvParcela = itemView.findViewById(R.id.tvParcelaVencimento);
            tvTotal = itemView.findViewById(R.id.tvTotalVencimento);
        }
    }
}