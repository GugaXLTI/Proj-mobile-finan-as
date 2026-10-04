package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;

import java.util.List;
import java.util.Locale;

public class DividaAdapter extends RecyclerView.Adapter<DividaAdapter.ViewHolder> {

    // ⭐ Locale brasileiro para forçar vírgula decimal
    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private List<Divida> dividas;
    private OnDividaActionListener listener;

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
        holder.tvDetalhe.setText(d.getBanco() + " • " + d.getCategoria());

        // ⭐ Mostra o número de parcelas E o valor de cada uma
        int totalParcelas = extrairNumeroParcelas(d.getParcela());
        double valorParcelaReal = d.getValorTotal() / totalParcelas;

        if (totalParcelas > 1) {
            holder.tvParcela.setText(
                    "Parcela " + d.getParcela() + " de R$ " +
                            String.format(LOCALE_BR, "%.2f", valorParcelaReal)
            );
        } else {
            holder.tvParcela.setText("Parcela " + d.getParcela());
        }

        holder.tvVencimento.setText("Vencimento: " + d.getVencimento());

        int parcelasPagas = (int) Math.round(d.getValorPago() / valorParcelaReal);

        if (d.isPago()) {
            // ⭐ Dívida totalmente paga
            holder.tvValorRestante.setText("Pago ✓");
            holder.tvValorRestante.setTextColor(Color.parseColor("#10B981"));

            holder.btnPagar.setText("Pago");
            holder.btnPagar.setEnabled(false);
            holder.btnPagar.setAlpha(0.5f);
        } else {
            // ⭐ Ainda falta pagar (parcial ou total)
            holder.tvValorRestante.setText(
                    "Falta: R$ " + String.format(LOCALE_BR, "%.2f", d.getValorRestante())
            );
            holder.tvValorRestante.setTextColor(Color.parseColor("#EF4444"));

            if (totalParcelas > 1) {
                holder.btnPagar.setText("Pagar (" + parcelasPagas + "/" + totalParcelas + ")");
            } else {
                holder.btnPagar.setText("Pagar");
            }
            holder.btnPagar.setEnabled(true);
            holder.btnPagar.setAlpha(1.0f);
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

    private int extrairNumeroParcelas(String parcela) {
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
        TextView tvTitulo, tvValorRestante, tvDetalhe, tvParcela, tvVencimento;
        Button btnExcluir, btnEditar, btnPagar;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloDivida);
            tvValorRestante = itemView.findViewById(R.id.tvValorRestante);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheDivida);
            tvParcela = itemView.findViewById(R.id.tvParcela);
            tvVencimento = itemView.findViewById(R.id.tvVencimento);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnPagar = itemView.findViewById(R.id.btnPagar);
        }
    }
}