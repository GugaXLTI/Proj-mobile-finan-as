package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.utils.BancoHelper;
import java.util.List;

public class BancoBottomSheetAdapter extends RecyclerView.Adapter<BancoBottomSheetAdapter.ViewHolder> {

    private final List<String[]> itens;
    private final OnBancoClickListener listener;

    public interface OnBancoClickListener {
        void onBancoClick(String[] banco, int position);
    }

    public BancoBottomSheetAdapter(List<String[]> itens, OnBancoClickListener listener) {
        this.itens = itens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_banco_bottom_sheet, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = itens.get(position);
        holder.tvNome.setText(item[2]);
        holder.tvDetalhe.setText(item[3]);

        int logoResId = BancoHelper.getLogoBanco(item[2]);
        if (logoResId != 0) {
            holder.ivLogo.setImageResource(logoResId);
            holder.ivLogo.setVisibility(View.VISIBLE);
            holder.tvIniciais.setVisibility(View.GONE);
        } else {
            holder.ivLogo.setVisibility(View.GONE);
            holder.tvIniciais.setVisibility(View.VISIBLE);

            holder.tvIniciais.setText(item[0]); // As iniciais já vêm prontas na posição 0 do array

            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(BancoHelper.getCorBanco(item[2]));
            holder.tvIniciais.setBackground(shape);
        }

        holder.itemView.setOnClickListener(v -> listener.onBancoClick(item, position));
    }

    @Override
    public int getItemCount() { return itens.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivLogo;
        TextView tvIniciais, tvNome, tvDetalhe;
        ViewHolder(View itemView) {
            super(itemView);
            ivLogo = itemView.findViewById(R.id.ivLogoItem);
            tvIniciais = itemView.findViewById(R.id.tvIniciaisItem);
            tvNome = itemView.findViewById(R.id.tvNomeItem);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheItem);
        }
    }
}