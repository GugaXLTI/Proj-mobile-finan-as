package com.example.controle_gastos.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.ChavePix;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ChavePixAdapter extends RecyclerView.Adapter<ChavePixAdapter.ViewHolder> {

    public interface OnItemLongClickListener {
        void onItemLongClick(ChavePix chave);
    }

    public interface OnItemClickListener {
        void onItemClick(ChavePix chave);
    }

    private List<ChavePix> listaChaves;
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;

    public ChavePixAdapter(List<ChavePix> listaChaves) {
        this.listaChaves = listaChaves;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chave_pix, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChavePix chave = listaChaves.get(position);

        holder.tvChavePix.setText(chave.chave);
        holder.tvNomeFavorecido.setText(chave.nomeFavorecido);

        holder.btnCopiarPix.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Chave Pix", chave.chave);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(v.getContext(), "Chave copiada!", Toast.LENGTH_SHORT).show();
            }
        });

        // ⭐ Clique normal = editar
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(chave);
            }
        });

        // ⭐ Clique longo = excluir
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(chave);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return listaChaves.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeFavorecido, tvChavePix;
        MaterialButton btnCopiarPix;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeFavorecido = itemView.findViewById(R.id.tvNomeFavorecido);
            tvChavePix = itemView.findViewById(R.id.tvChavePix);
            btnCopiarPix = itemView.findViewById(R.id.btnCopiarPix);
        }
    }
}