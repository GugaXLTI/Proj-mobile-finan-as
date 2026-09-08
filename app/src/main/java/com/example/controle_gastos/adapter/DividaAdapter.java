package com.example.controle_gastos.adapter;

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

    private List<Divida> dividas;
    private OnDividaActionListener listener;

    public interface OnDividaActionListener {
        void onExcluirClick(int position);
        void onEditarClick(int position);
        void onPagarClick(int position);
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
        holder.tvValorRestante.setText("Falta: R$ " + String.format(Locale.getDefault(), "%.2f", d.getValorRestante()));
        holder.tvDetalhe.setText(d.getBanco() + " • " + d.getCategoria());
        holder.tvParcela.setText("Parcela " + d.getParcela());
        holder.tvVencimento.setText("Vencimento: " + d.getVencimento());

        // Ações
        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) listener.onExcluirClick(position);
        });
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(position);
        });
        holder.btnPagar.setOnClickListener(v -> {
            if (listener != null) listener.onPagarClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return dividas.size();
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