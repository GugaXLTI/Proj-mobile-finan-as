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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter SOMENTE LEITURA para o Dashboard.
 * Sem botões de ação, apenas exibe os dados.
 */
public class DividaDashboardAdapter extends RecyclerView.Adapter<DividaDashboardAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private final List<Divida> dividas;

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
        holder.tvDetalhe.setText(montarDetalhe(d));
        holder.tvParcela.setText("Parcela " + d.getParcela());
        holder.tvValor.setText(NumberFormat.getCurrencyInstance(LOCALE_BR).format(d.getValorTotal()));

        Date data = null;
        try {
            data = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).parse(d.getVencimento());
        } catch (Exception ignored) {
        }

        if (data != null) {
            holder.tvMes.setText(new SimpleDateFormat("MMM", LOCALE_BR)
                    .format(data).replace(".", "").toUpperCase(LOCALE_BR));
            holder.tvDia.setText(new SimpleDateFormat("dd", LOCALE_BR).format(data));
        } else {
            holder.tvMes.setText("--");
            holder.tvDia.setText("--");
        }

        if (d.isPago()) {
            holder.tvStatus.setText("Pago ✓");
            holder.tvStatus.setTextColor(Color.parseColor("#34D399"));
            holder.tvValor.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            holder.tvValor.setTextColor(Color.WHITE);
            if (data != null && estaAtrasada(data)) {
                holder.tvStatus.setText("Atrasada");
                holder.tvStatus.setTextColor(Color.parseColor("#F87171"));
            } else {
                holder.tvStatus.setText("Pendente");
                holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
            }
        }
    }

    @Override
    public int getItemCount() {
        return dividas.size();
    }

    private String montarDetalhe(Divida d) {
        String banco = d.getBanco() == null ? "" : d.getBanco().trim();
        String categoria = d.getCategoria() == null ? "" : d.getCategoria().trim();
        if (!banco.isEmpty() && !categoria.isEmpty()) return banco + " · " + categoria;
        if (!banco.isEmpty()) return banco;
        return categoria;
    }

    private boolean estaAtrasada(Date data) {
        Calendar hoje = Calendar.getInstance();
        zerarHora(hoje);
        Calendar alvo = Calendar.getInstance();
        alvo.setTime(data);
        zerarHora(alvo);
        return alvo.before(hoje);
    }

    private void zerarHora(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvValor, tvDetalhe, tvParcela, tvStatus, tvMes, tvDia;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloDivida);
            tvValor = itemView.findViewById(R.id.tvValorRestante);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheDivida);
            tvParcela = itemView.findViewById(R.id.tvParcela);
            tvStatus = itemView.findViewById(R.id.tvVencimento);
            tvMes = itemView.findViewById(R.id.tvMesDash);
            tvDia = itemView.findViewById(R.id.tvDiaDash);
        }
    }
}