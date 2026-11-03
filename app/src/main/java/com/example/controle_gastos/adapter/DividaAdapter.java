package com.example.controle_gastos.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.DividaAgrupada;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DividaAdapter extends RecyclerView.Adapter<DividaAdapter.ViewHolder> {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");
    private final List<DividaAgrupada> lista;
    private final OnDividaActionListener listener;

    public interface OnDividaActionListener {
        void onExcluirClick(DividaAgrupada agrupada);
        void onEditarClick(DividaAgrupada agrupada);
        void onPagarClick(DividaAgrupada agrupada);
    }

    public DividaAdapter(List<DividaAgrupada> lista, OnDividaActionListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_divida, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DividaAgrupada agrupada = lista.get(position);
        Divida parcelaExibida = agrupada.getParcelaParaLista();

        if (parcelaExibida == null) return;

        holder.tvTitulo.setText(agrupada.getTitulo());
        holder.tvDetalhe.setText(montarDetalhe(parcelaExibida));

        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_BR);
        holder.tvValor.setText(nf.format(parcelaExibida.getValorTotal()));

        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).parse(parcelaExibida.getVencimento());
            if (data != null) {
                holder.tvMes.setText(new SimpleDateFormat("MMM", LOCALE_BR).format(data).replace(".", "").toUpperCase(LOCALE_BR));
                holder.tvDia.setText(new SimpleDateFormat("dd", LOCALE_BR).format(data));
            }
        } catch (Exception ignored) {}

        holder.tvParcela.setText("Parcela " + parcelaExibida.getParcela());

        // Barra de Progresso e Contagem (Corretos, somam o grupo inteiro)
        int total = agrupada.getTotalParcelas();
        int pagas = agrupada.getParcelasPagas();
        holder.tvProgresso.setText("Pagas: " + pagas + " de " + total);

        if (holder.progressBar != null) {
            holder.progressBar.setMax(total);
            holder.progressBar.setProgress(pagas);
        }

        // ⭐ A CORREÇÃO ESTÁ AQUI: Verifica se a parcela EXIBIDA está paga
        if (parcelaExibida.isPago()) {
            holder.tvStatus.setText("Pago ✓");
            holder.tvStatus.setTextColor(Color.parseColor("#34D399")); // Verde
            holder.btnPagar.setVisibility(View.GONE); // Esconde o botão
            holder.tvValor.setTextColor(Color.parseColor("#94A3B8")); // Deixa o valor cinza
        } else {
            holder.tvValor.setTextColor(Color.WHITE); // Volta o valor para branco
            holder.btnPagar.setVisibility(View.VISIBLE); // Mostra o botão

            if (isAtrasada(parcelaExibida.getVencimento())) {
                holder.tvStatus.setText("Atrasada");
                holder.tvStatus.setTextColor(Color.parseColor("#F87171")); // Vermelho
            } else {
                holder.tvStatus.setText("Vence em " + calcularDias(parcelaExibida.getVencimento()) + " dias");
                holder.tvStatus.setTextColor(Color.parseColor("#F59E0B")); // Laranja
            }
        }

        holder.btnPagar.setOnClickListener(v -> listener.onPagarClick(agrupada));
        holder.btnEditar.setOnClickListener(v -> listener.onEditarClick(agrupada));
        holder.btnExcluir.setOnClickListener(v -> listener.onExcluirClick(agrupada));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    private String montarDetalhe(Divida d) {
        String banco = d.getBanco() == null ? "" : d.getBanco().trim();
        String categoria = d.getCategoria() == null ? "" : d.getCategoria().trim();
        if (!banco.isEmpty() && !categoria.isEmpty()) return banco + " · " + categoria;
        if (!banco.isEmpty()) return banco;
        if (!categoria.isEmpty()) return categoria;
        return "Sem detalhes";
    }

    private boolean isAtrasada(String vencimento) {
        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).parse(vencimento);
            return data != null && data.before(new Date());
        } catch (Exception e) { return false; }
    }

    private long calcularDias(String vencimento) {
        try {
            Date data = new SimpleDateFormat("dd/MM/yyyy", LOCALE_BR).parse(vencimento);
            if (data == null) return 0;
            long diff = data.getTime() - new Date().getTime();
            return Math.max(0, diff / (1000 * 60 * 60 * 24));
        } catch (Exception e) { return 0; }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvValor, tvDetalhe, tvParcela, tvStatus, tvMes, tvDia, tvProgresso;
        ProgressBar progressBar;
        ImageView btnEditar, btnExcluir;
        TextView btnPagar;

        ViewHolder(View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTituloDivida);
            tvDetalhe = itemView.findViewById(R.id.tvDetalheDivida);
            tvValor = itemView.findViewById(R.id.tvValorRestante);
            tvStatus = itemView.findViewById(R.id.tvVencimento);
            tvMes = itemView.findViewById(R.id.tvMesDivida);
            tvDia = itemView.findViewById(R.id.tvDiaDivida);
            tvParcela = itemView.findViewById(R.id.tvParcela);
            tvProgresso = itemView.findViewById(R.id.tvProgressoParcelas);
            progressBar = itemView.findViewById(R.id.progressParcelas);
            btnPagar = itemView.findViewById(R.id.btnPagar);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}