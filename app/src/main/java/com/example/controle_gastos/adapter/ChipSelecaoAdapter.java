package com.example.controle_gastos.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.controle_gastos.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

/**
 * Cada item é um String[]:
 *   [0] sigla, [1] nome, [2] valorReal (vira Divida.banco), [3] detalhe (opcional, ex.: "•••• 1234")
 * Itens antigos com só 3 posições continuam funcionando.
 */
public class ChipSelecaoAdapter extends RecyclerView.Adapter<ChipSelecaoAdapter.ViewHolder> {

    public interface OnItemSelecionadoListener {
        void onItemSelecionado(int position);
    }

    private static final int COR_CARD = 0xFF111A2E;
    private static final int COR_CARD_SEL = 0xFF123D33;
    private static final int COR_BORDA = 0xFF1E2A44;
    private static final int COR_VERDE = 0xFF10B981;
    private static final int COR_VERDE_CLARO = 0xFF34D399;
    private static final int COR_BRANCO = 0xFFFFFFFF;

    private final List<String[]> listaItens;
    private int posicaoSelecionada = 0;
    private final OnItemSelecionadoListener listener;

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
        Context ctx = holder.itemView.getContext();
        String[] item = listaItens.get(position);

        holder.tvSiglaChip.setText(item[0]);
        holder.tvNomeChip.setText(item[1]);

        if (item.length > 3 && item[3] != null && !item[3].isEmpty()) {
            holder.tvDetalheChip.setText(item[3]);
            holder.tvDetalheChip.setVisibility(View.VISIBLE);
        } else {
            holder.tvDetalheChip.setVisibility(View.GONE);
        }

        boolean selecionado = (position == posicaoSelecionada);
        float d = ctx.getResources().getDisplayMetrics().density;

        holder.cardChip.setCardBackgroundColor(selecionado ? COR_CARD_SEL : COR_CARD);
        holder.cardChip.setStrokeColor(selecionado ? COR_VERDE : COR_BORDA);
        holder.cardChip.setStrokeWidth(Math.round((selecionado ? 1.5f : 1f) * d));
        holder.tvSiglaChip.setTextColor(selecionado ? COR_VERDE_CLARO : COR_BRANCO);

        holder.itemView.setOnClickListener(v -> {
            int nova = holder.getAdapterPosition();
            if (nova == RecyclerView.NO_POSITION) return;
            int anterior = posicaoSelecionada;
            posicaoSelecionada = nova;
            notifyItemChanged(anterior);
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
        if (posicaoSelecionada < 0 || posicaoSelecionada >= listaItens.size()) {
            posicaoSelecionada = 0;
        }
        return listaItens.get(posicaoSelecionada);
    }

    /** Seleciona o item cujo valorReal (índice 2) é igual ao informado. Retorna true se achou. */
    public boolean selecionarPorValor(String valorReal) {
        if (valorReal == null) return false;
        for (int i = 0; i < listaItens.size(); i++) {
            String[] item = listaItens.get(i);
            if (item.length > 2 && valorReal.equals(item[2])) {
                posicaoSelecionada = i;
                notifyDataSetChanged();
                return true;
            }
        }
        return false;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardChip;
        TextView tvSiglaChip, tvNomeChip, tvDetalheChip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardChip = itemView.findViewById(R.id.cardChip);
            tvSiglaChip = itemView.findViewById(R.id.tvSiglaChip);
            tvNomeChip = itemView.findViewById(R.id.tvNomeChip);
            tvDetalheChip = itemView.findViewById(R.id.tvDetalheChip);
        }
    }
}