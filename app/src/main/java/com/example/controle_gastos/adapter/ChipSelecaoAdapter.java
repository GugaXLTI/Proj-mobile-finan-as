package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.google.android.material.card.MaterialCardView;
import java.util.List;

public class ChipSelecaoAdapter extends RecyclerView.Adapter<ChipSelecaoAdapter.ViewHolder> {

    public interface OnItemSelecionadoListener {
        void onItemSelecionado(int position);
    }

    private List<String[]> listaItens; // Cada item é [sigla, nome, valorReal]
    private int posicaoSelecionada = 0;
    private OnItemSelecionadoListener listener;

    public ChipSelecaoAdapter(List<String[]> listaItens, OnItemSelecionadoListener listener) {
        this.listaItens = listaItens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chip_selecao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = listaItens.get(position);
        holder.tvSiglaChip.setText(item[0]);
        holder.tvNomeChip.setText(item[1]);

        // Estado de seleção
        if (position == posicaoSelecionada) {
            holder.cardChip.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_green));
            holder.cardChip.setStrokeWidth(4);
            holder.cardChip.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg));
            holder.tvSiglaChip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.brand_green));
        } else {
            holder.cardChip.setStrokeColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_gray));
            holder.cardChip.setStrokeWidth(2);
            holder.cardChip.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_bg));
            holder.tvSiglaChip.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_white));
        }

        holder.itemView.setOnClickListener(v -> {
            int posAnterior = posicaoSelecionada;
            posicaoSelecionada = holder.getAdapterPosition();
            notifyItemChanged(posAnterior);
            notifyItemChanged(posicaoSelecionada);
            if (listener != null) {
                listener.onItemSelecionado(posicaoSelecionada);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaItens.size();
    }

    public int getPosicaoSelecionada() {
        return posicaoSelecionada;
    }

    public String[] getItemSelecionado() {
        if (listaItens.isEmpty()) return null;
        return listaItens.get(posicaoSelecionada);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardChip;
        TextView tvSiglaChip, tvNomeChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardChip = itemView.findViewById(R.id.cardChip);
            tvSiglaChip = itemView.findViewById(R.id.tvSiglaChip);
            tvNomeChip = itemView.findViewById(R.id.tvNomeChip);
        }
    }
}