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
import com.example.controle_gastos.model.Categoria;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private List<Categoria> categorias;
    private List<Integer> contagens; // contagem de dívidas de cada categoria
    private OnCategoriaActionListener listener;

    public interface OnCategoriaActionListener {
        void onExcluirClick(Categoria categoria);
    }

    public CategoriaAdapter(List<Categoria> categorias, List<Integer> contagens, OnCategoriaActionListener listener) {
        this.categorias = categorias;
        this.contagens = contagens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Categoria c = categorias.get(position);

        holder.tvNome.setText(c.getNome());

        // Contagem de dívidas
        int count = contagens.get(position);
        String textoContagem = count + (count == 1 ? " dívida" : " dívidas");
        holder.tvContagem.setText(textoContagem);

        // Aplica a cor da categoria na bolinha
        try {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor(c.getCor()));
            holder.viewCor.setBackground(circle);
        } catch (Exception e) {
            // Se a cor for inválida, usa cinza como padrão
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor("#64748B"));
            holder.viewCor.setBackground(circle);
        }

        // Botão Excluir
        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) listener.onExcluirClick(c);
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewCor;
        TextView tvNome, tvContagem;
        ImageView btnExcluir;

        ViewHolder(View itemView) {
            super(itemView);
            viewCor = itemView.findViewById(R.id.viewCorCategoria);
            tvNome = itemView.findViewById(R.id.tvNomeCategoria);
            tvContagem = itemView.findViewById(R.id.tvContagemCategoria);
            btnExcluir = itemView.findViewById(R.id.btnExcluirCategoria);
        }
    }
}