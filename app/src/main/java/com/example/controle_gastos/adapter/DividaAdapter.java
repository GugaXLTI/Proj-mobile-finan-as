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

        // ⭐ Cada registro JÁ É uma parcela. Não dividir!
        int totalParcelas = extrairTotalParcelas(d.getParcela());

        if (totalParcelas > 1) {
            holder.tvParcela.setText("Parcela " + d.getParcela()); // "Parcela 2/4"
        } else {
            holder.tvParcela.setText("Parcela " + d.getParcela());
        }

        holder.tvVencimento.setText("Vencimento: " + d.getVencimento());

        if (d.isPago()) {
            holder.tvValorRestante.setText("Pago ✓");
            holder.tvValorRestante.setTextColor(Color.parseColor("#10B981"));

            holder.btnPagar.setText("Pago");
            holder.btnPagar.setEnabled(false);
            holder.btnPagar.setAlpha(0.5f);
        } else {
            holder.tvValorRestante.setText(
                    "R$ " + String.format(LOCALE_BR, "%.2f", d.getValorTotal())
            );
            holder.tvValorRestante.setTextColor(Color.parseColor("#EF4444"));

            holder.btnPagar.setText("Pagar");
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

    /**
     * ⭐ Extrai o TOTAL de parcelas do campo "X/Y".
     * "2/4"      → 4
     * "1x"       → 1
     * "1x (À vista)" → 1
     */
    private int extrairTotalParcelas(String parcela) {
        if (parcela == null) return 1;

        // Formato "X/Y" → pega o Y
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

        // Formato "1x" ou "1x (À vista)"
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