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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DividaAdapter extends RecyclerView.Adapter<DividaAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private final List<Divida> dividas;
    private final OnDividaActionListener listener;

    public interface OnDividaActionListener {
        void onExcluirClick(Divida divida);
        void onEditarClick(Divida divida);
        void onPagarClick(Divida divida);
    }

    public DividaAdapter(List<Divida> dividas, OnDividaActionListener listener) {
        this.dividas = dividas;
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
        Divida d = dividas.get(position);

        holder.tvTitulo.setText(d.getTitulo());
        holder.tvDetalhe.setText(montarDetalhe(d));
        holder.tvParcela.setText("Parcela " + d.getParcela());

        // Data (bloco mês/dia) + texto de vencimento
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR);
        Date data = null;
        try {
            data = sdfData.parse(d.getVencimento());
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

        holder.tvValor.setText(moeda(d.getValorTotal()));

        if (d.isPago()) {
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

        // Progresso das parcelas (só quando é parcelado)
        int totalParcelas = extrairTotalParcelas(d.getParcela());
        if (totalParcelas > 1) {
            int pagas = contarPagas(d, totalParcelas);
            holder.llParcelas.setVisibility(View.VISIBLE);
            holder.tvProgresso.setText("Pagas: " + pagas + " de " + totalParcelas);
            holder.progressParcelas.setProgress(Math.min(100, (pagas * 100) / totalParcelas));
        } else {
            holder.llParcelas.setVisibility(View.GONE);
        }

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) listener.onExcluirClick(d);
        });
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(d);
        });
        holder.btnPagar.setOnClickListener(v -> {
            if (listener != null && !d.isPago()) listener.onPagarClick(d);
        });
    }

    @Override
    public int getItemCount() {
        return dividas.size();
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private String montarDetalhe(Divida d) {
        String banco = d.getBanco() == null ? "" : d.getBanco().trim();
        String categoria = d.getCategoria() == null ? "" : d.getCategoria().trim();
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

    /** Conta quantas parcelas do mesmo grupo estão pagas (usa a lista atual). */
    private int contarPagas(Divida atual, int totalParcelas) {
        if (atual.getGrupoId() <= 0) {
            return atual.isPago() ? 1 : 0;
        }
        int pagas = 0;
        for (Divida outra : dividas) {
            if (outra.getGrupoId() == atual.getGrupoId() && outra.isPago()) pagas++;
        }
        return Math.min(pagas, totalParcelas);
    }

    /**
     * Extrai o TOTAL de parcelas do campo "X/Y".
     * "2/4" → 4 | "1x" → 1 | "1x (À vista)" → 1
     */
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