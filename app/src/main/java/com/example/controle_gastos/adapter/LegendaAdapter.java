package com.example.controle_gastos.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.CategoriaResumo;
import java.util.List;
import java.util.Locale;

public class LegendaAdapter extends RecyclerView.Adapter<LegendaAdapter.ViewHolder> {

    private List<CategoriaResumo> categorias;

    public LegendaAdapter(List<CategoriaResumo> categorias) {
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_legenda, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoriaResumo cat = categorias.get(position);
        holder.tvNome.setText(cat.getNome() + " (" + cat.getQuantidade() + ")");
        holder.tvTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", cat.getTotal()));
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvTotal;
        ViewHolder(View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNomeLegenda);
            tvTotal = itemView.findViewById(R.id.tvTotalLegenda);
        }
    }
}