package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;

import java.util.List;
import java.util.Locale;

public class HistoricoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private static final int TYPE_DIVIDA = 1;
    private static final int TYPE_TRANSACAO = 2;

    private List<Object> listaUnificada;

    public HistoricoAdapter(List<Object> listaUnificada) {
        this.listaUnificada = listaUnificada;
    }

    @Override
    public int getItemViewType(int position) {
        if (listaUnificada.get(position) instanceof Divida) return TYPE_DIVIDA;
        return TYPE_TRANSACAO;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DIVIDA) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico, parent, false);
            return new DividaViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historico_transacao, parent, false);
            return new TransacaoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = listaUnificada.get(position);

        if (item instanceof Divida) {
            bindDivida((DividaViewHolder) holder, (Divida) item);
        } else if (item instanceof Transacao) {
            bindTransacao((TransacaoViewHolder) holder, (Transacao) item);
        }
    }

    private void bindDivida(DividaViewHolder holder, Divida d) {
        holder.tvTitulo.setText(d.getTitulo());
        holder.tvDetalhe.setText(d.getBanco() + " • " + d.getCategoria() + " • " + d.getVencimento());
        holder.tvValor.setText(String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()));

        if (d.isExcluida()) {
            holder.tvIcone.setText("✗");
            aplicarCorIcone(holder.tvIcone, "#EF4444", "#3A1515");
            holder.tvStatus.setText("Excluída ✗");
            holder.tvStatus.setTextColor(Color.parseColor("#EF4444"));
            holder.tvStatus.setBackground(criarBadge("#3A1515"));
            holder.tvValor.setTextColor(Color.parseColor("#EF4444"));
            holder.tvValor.setPaintFlags(holder.tvValor.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

        } else if (d.isPago()) {
            holder.tvIcone.setText("✓");
            aplicarCorIcone(holder.tvIcone, "#10B981", "#1A3D35");
            holder.tvStatus.setText("Liquidado ✓");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            holder.tvStatus.setBackground(criarBadge("#1A3D35"));
            holder.tvValor.setTextColor(Color.parseColor("#10B981"));
            holder.tvValor.setPaintFlags(holder.tvValor.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));

        } else {
            holder.tvIcone.setText("→");
            aplicarCorIcone(holder.tvIcone, "#F59E0B", "#2A1F0A");

            int totalParcelas = extrairTotalParcelas(d.getParcela());
            holder.tvStatus.setText(totalParcelas > 1 ? "Parcela " + d.getParcela() : "Pendente");
            holder.tvStatus.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvStatus.setBackground(criarBadge("#2A1F0A"));
            holder.tvValor.setTextColor(Color.parseColor("#F59E0B"));
            holder.tvValor.setPaintFlags(holder.tvValor.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void bindTransacao(TransacaoViewHolder holder, Transacao t) {
        holder.tvTitulo.setText(t.descricao);
        holder.tvDetalhe.setText("Automático • " + (t.categoria != null ? t.categoria : "Sem categoria") + " • " + t.data);

        boolean isEntrada = "ENTRADA".equals(t.tipo);
        String prefixo = isEntrada ? "+ " : "- ";

        holder.tvValor.setText(prefixo + String.format(LOCALE_BR, "R$ %.2f", t.valor));

        if (isEntrada) {
            holder.tvIcone.setText("↓"); // Ícone de entrada
            aplicarCorIcone(holder.tvIcone, "#10B981", "#1A3D35");
            holder.tvStatus.setText("Entrada");
            holder.tvStatus.setTextColor(Color.parseColor("#10B981"));
            holder.tvStatus.setBackground(criarBadge("#1A3D35"));
            holder.tvValor.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.tvIcone.setText("↑"); // Ícone de saída
            aplicarCorIcone(holder.tvIcone, "#F87171", "#3A1515");
            holder.tvStatus.setText("Saída");
            holder.tvStatus.setTextColor(Color.parseColor("#F87171"));
            holder.tvStatus.setBackground(criarBadge("#3A1515"));
            holder.tvValor.setTextColor(Color.parseColor("#F87171"));
        }
    }

    private void aplicarCorIcone(TextView tvIcone, String corIcone, String corFundo) {
        tvIcone.setTextColor(Color.parseColor(corIcone));
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(corFundo));
        tvIcone.setBackground(circle);
    }

    private GradientDrawable criarBadge(String corFundo) {
        GradientDrawable badge = new GradientDrawable();
        badge.setShape(GradientDrawable.RECTANGLE);
        badge.setCornerRadius(20f);
        badge.setColor(Color.parseColor(corFundo));
        return badge;
    }

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
            } catch (Exception e) { return 1; }
        }
        return 1;
    }

    @Override
    public int getItemCount() {
        return listaUnificada.size();
    }

    // ViewHolder para Dívidas
    static class DividaViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcone, tvTitulo, tvDetalhe, tvValor, tvStatus;

        DividaViewHolder(View itemView) {
            super(itemView);
            tvIcone = itemView.findViewById(R.id.tvIconeEstado);
            tvTitulo = itemView.findViewById(R.id.tvTituloHistorico);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheHistorico);
            tvValor = itemView.findViewById(R.id.tvValorHistorico);
            tvStatus = itemView.findViewById(R.id.tvStatusHistorico);
        }
    }

    // ViewHolder para Transações
    static class TransacaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcone, tvTitulo, tvDetalhe, tvValor, tvStatus;

        TransacaoViewHolder(View itemView) {
            super(itemView);
            tvIcone = itemView.findViewById(R.id.tvIconeTransacao);
            tvTitulo = itemView.findViewById(R.id.tvTituloTransacao);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheTransacao);
            tvValor = itemView.findViewById(R.id.tvValorTransacao);
            tvStatus = itemView.findViewById(R.id.tvStatusTransacao);
        }
    }
}