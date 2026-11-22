package com.example.controle_gastos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Cartao;
import com.example.controle_gastos.utils.BancoHelper;
import java.util.List;
import java.util.Locale;

public class CartaoAdapter extends RecyclerView.Adapter<CartaoAdapter.ViewHolder> {

    public interface OnItemLongClickListener { void onItemLongClick(Cartao cartao); }
    public interface OnItemClickListener { void onItemClick(Cartao cartao); }

    private final List<Cartao> listaCartoes;
    private final String nomeUsuario;
    private OnItemLongClickListener longClickListener;
    private OnItemClickListener clickListener;
    private boolean leituraAtiva = false;

    public CartaoAdapter(List<Cartao> listaCartoes, String nomeUsuario) {
        this.listaCartoes = listaCartoes;
        this.nomeUsuario = nomeUsuario;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) { this.longClickListener = listener; }
    public void setOnItemClickListener(OnItemClickListener listener) { this.clickListener = listener; }

    public void setLeituraAtiva(boolean ativa) {
        if (this.leituraAtiva != ativa) {
            this.leituraAtiva = ativa;
            notifyDataSetChanged();
        }
    }

    public static int corDoBanco(String instituicao) {
        String s = instituicao == null ? "" : instituicao.toLowerCase(Locale.ROOT);
        if (s.contains("nubank") || s.equals("nu")) return Color.parseColor("#8A05BE");
        if (s.contains("inter")) return Color.parseColor("#FF7A00");
        if (s.contains("itaú") || s.contains("itau")) return Color.parseColor("#EC7000");
        if (s.contains("bradesco")) return Color.parseColor("#CC092F");
        if (s.contains("santander")) return Color.parseColor("#EC0000");
        if (s.contains("caixa")) return Color.parseColor("#005CA9");
        return Color.parseColor("#64748B");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cartao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cartao cartao = listaCartoes.get(position);
        Context ctx = holder.itemView.getContext();
        float d = ctx.getResources().getDisplayMetrics().density;

        String instituicao = cartao.instituicao != null ? cartao.instituicao : "";
        String tipo = (cartao.tipo != null) ? cartao.tipo : "Crédito";
        int cor = corDoBanco(instituicao);

        GradientDrawable fundo = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{ColorUtils.setAlphaComponent(cor, 80), Color.parseColor("#111A2E")});
        fundo.setCornerRadius(24 * d);
        fundo.setStroke(Math.max(1, Math.round(d)), ColorUtils.setAlphaComponent(cor, 110));
        holder.root.setBackground(fundo);

        // LÓGICA SEGURA: Imagem ou Inicial
        int logoResId = BancoHelper.getLogoBanco(instituicao);
        if (logoResId != 0) {
            holder.ivLogoBanco.setImageResource(logoResId);
            holder.ivLogoBanco.setVisibility(View.VISIBLE);
            holder.tvSigla.setVisibility(View.GONE);
        } else {
            holder.ivLogoBanco.setVisibility(View.GONE);
            holder.tvSigla.setVisibility(View.VISIBLE);

            String sigla = (cartao.bandeira != null && !cartao.bandeira.trim().isEmpty()) ? cartao.bandeira.trim() : instituicao;
            sigla = sigla.length() >= 2 ? sigla.substring(0, 2) : sigla;
            holder.tvSigla.setText(sigla.toUpperCase(Locale.ROOT));

            GradientDrawable badge = new GradientDrawable();
            badge.setCornerRadius(14 * d);
            badge.setColor(cor);
            holder.tvSigla.setBackground(badge);
        }

        holder.tvNomeCartao.setText(instituicao);
        holder.tvTipoCartao.setText(tipo);

        StringBuilder sub = new StringBuilder();
        String apelido = cartao.apelido != null ? cartao.apelido.trim() : "";
        if (!apelido.isEmpty() && !apelido.equalsIgnoreCase(instituicao)) { sub.append(apelido); }
        String final4 = cartao.ultimos4Digitos != null ? cartao.ultimos4Digitos.trim() : "";
        if (!final4.isEmpty()) {
            if (sub.length() > 0) sub.append(" · ");
            sub.append("final ").append(final4);
        }
        holder.tvApelido.setText(sub.toString());
        holder.tvApelido.setVisibility(sub.length() == 0 ? View.GONE : View.VISIBLE);

        int corStatus = Color.parseColor(leituraAtiva ? "#34D399" : "#FBBF24");
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(corStatus);
        holder.vDotStatus.setBackground(dot);
        holder.tvStatusLeitura.setText(leituraAtiva ? "Leitura automática ativa" : "Aguardando permissão de leitura");
        holder.tvStatusLeitura.setTextColor(corStatus);

        holder.tvTitular.setText(nomeUsuario != null ? nomeUsuario.toUpperCase(Locale.ROOT) : "");

        holder.itemView.setOnClickListener(v -> { if (clickListener != null) clickListener.onItemClick(cartao); });
        holder.btnEditar.setOnClickListener(v -> { if (clickListener != null) clickListener.onItemClick(cartao); });
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) { longClickListener.onItemLongClick(cartao); return true; }
            return false;
        });
        holder.btnExcluir.setOnClickListener(v -> { if (longClickListener != null) longClickListener.onItemLongClick(cartao); });
    }

    @Override
    public int getItemCount() { return listaCartoes.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View root, vDotStatus;
        TextView tvSigla, tvNomeCartao, tvApelido, tvTipoCartao, tvStatusLeitura, tvTitular;
        ImageView ivLogoBanco, btnEditar, btnExcluir;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            root = itemView.findViewById(R.id.rootCartao);
            vDotStatus = itemView.findViewById(R.id.vDotStatus);
            tvSigla = itemView.findViewById(R.id.tvBandeira);
            tvNomeCartao = itemView.findViewById(R.id.tvNomeCartao);
            tvApelido = itemView.findViewById(R.id.tvApelidoCartao);
            tvTipoCartao = itemView.findViewById(R.id.tvTipoCartao);
            tvStatusLeitura = itemView.findViewById(R.id.tvStatusLeitura);
            tvTitular = itemView.findViewById(R.id.tvTitular);
            ivLogoBanco = itemView.findViewById(R.id.ivLogoBanco);
            btnEditar = itemView.findViewById(R.id.btnEditarCartao);
            btnExcluir = itemView.findViewById(R.id.btnExcluirCartao);
        }
    }
}