package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.DividaAgrupada;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DividaAdapter extends RecyclerView.Adapter<DividaAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private final List<DividaAgrupada> listaAgrupada;
    private final OnDividaActionListener listener;

    public interface OnDividaActionListener {
        void onExcluirClick(DividaAgrupada agrupada);
        void onEditarClick(DividaAgrupada agrupada);
        void onPagarClick(DividaAgrupada agrupada);
    }

    public DividaAdapter(List<DividaAgrupada> listaAgrupada, OnDividaActionListener listener) {
        this.listaAgrupada = listaAgrupada;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_divida, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DividaAgrupada agrupada = listaAgrupada.get(position);
        Divida proxima = agrupada.getProximaParcela();

        holder.tvTitulo.setText(agrupada.getTitulo());
        holder.tvDetalhe.setText(montarDetalhe(agrupada));
        holder.tvParcela.setText("Parcela " + proxima.getParcela());

        // Data (bloco mês/dia) + texto de vencimento
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR);
        Date data = null;
        try {
            data = sdfData.parse(proxima.getVencimento());
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

        // Valor restante total do grupo
        holder.tvValor.setText(moeda(agrupada.getValorRestanteGrupo()));

        if (agrupada.isTotalmentePago()) {
            holder.itemView.setAlpha(0.75f);
            holder.tvValor.setTextColor(Color.parseColor("#94A3B8"));
            holder.tvVencimento.setText("Paga ✓");
            holder.tvVencimento.setTextColor(Color.parseColor("#34D399"));

            holder.btnPagar.setText("Pago ✓");
            holder.btnPagar.setTextColor(Color.parseColor("#34D399"));
            holder.btnPagar.setBackgroundResource(R.drawable.bg_botao_pago);
            holder.btnPagar.setEnabled(false);
        } else {
            holder.itemView.setAlpha(1f);
            holder.tvValor.setTextColor(Color.WHITE);
            definirTextoVencimento(holder.tvVencimento, data);

            holder.btnPagar.setText("Pagar");
            holder.btnPagar.setTextColor(Color.parseColor("#052E22"));
            holder.btnPagar.setBackgroundResource(R.drawable.bg_botao_pagar);
            holder.btnPagar.setEnabled(true);
        }

        // Progresso das parcelas
        int totalParcelas = agrupada.getTotalParcelas();
        if (totalParcelas > 1) {
            int pagas = agrupada.getParcelasPagas();
            holder.llParcelas.setVisibility(View.VISIBLE);
            holder.tvProgresso.setText("Pagas: " + pagas + " de " + totalParcelas);
            holder.progressParcelas.setProgress(Math.min(100, (pagas * 100) / totalParcelas));
        } else {
            holder.llParcelas.setVisibility(View.GONE);
        }

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) listener.onExcluirClick(agrupada);
        });
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(agrupada);
        });
        holder.btnPagar.setOnClickListener(v -> {
            if (listener != null && !agrupada.isTotalmentePago()) listener.onPagarClick(agrupada);
        });
    }

    @Override
    public int getItemCount() {
        return listaAgrupada.size();
    }

    private String montarDetalhe(DividaAgrupada a) {
        String banco = a.getBanco() == null ? "" : a.getBanco().trim();
        String categoria = a.getCategoria() == null ? "" : a.getCategoria().trim();
        if (!banco.isEmpty() && !categoria.isEmpty()) return banco + " · " + categoria;
        if (!banco.isEmpty()) return banco;
        return categoria;
    }

    private String moeda(double valor) {
        return NumberFormat.getCurrencyInstance(LOCALE_BR).format(valor);
    }

    private void definirTextoVencimento(TextView tv, Date data) {
        if (data == null) {
            tv.setText("");
            return;
        }
        long dias = diasAte(data);
        if (dias < 0) {
            tv.setText(dias == -1 ? "Venceu ontem" : "Venceu há " + (-dias) + " dias");
            tv.setTextColor(Color.parseColor("#F87171"));
        } else if (dias == 0) {
            tv.setText("Vence hoje");
            tv.setTextColor(Color.parseColor("#F59E0B"));
        } else if (dias == 1) {
            tv.setText("Vence amanhã");
            tv.setTextColor(Color.parseColor("#F59E0B"));
        } else {
            tv.setText("Vence em " + dias + " dias");
            tv.setTextColor(Color.parseColor(dias <= 5 ? "#F59E0B" : "#94A3B8"));
        }
    }

    private long diasAte(Date data) {
        Calendar hoje = Calendar.getInstance();
        zerarHora(hoje);
        Calendar alvo = Calendar.getInstance();
        alvo.setTime(data);
        zerarHora(alvo);
        return Math.round((alvo.getTimeInMillis() - hoje.getTimeInMillis()) / 86400000.0);
    }

    private void zerarHora(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvValor, tvDetalhe, tvParcela, tvVencimento;
        TextView tvMes, tvDia, tvProgresso, btnPagar;
        ImageView btnExcluir, btnEditar;
        View llParcelas;
        ProgressBar progressParcelas;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloDivida);
            tvValor = itemView.findViewById(R.id.tvValorRestante);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheDivida);
            tvParcela = itemView.findViewById(R.id.tvParcela);
            tvVencimento = itemView.findViewById(R.id.tvVencimento);
            tvMes = itemView.findViewById(R.id.tvMesDivida);
            tvDia = itemView.findViewById(R.id.tvDiaDivida);
            tvProgresso = itemView.findViewById(R.id.tvProgressoParcelas);
            llParcelas = itemView.findViewById(R.id.llParcelas);
            progressParcelas = itemView.findViewById(R.id.progressParcelas);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnPagar = itemView.findViewById(R.id.btnPagar);
        }
    }
}