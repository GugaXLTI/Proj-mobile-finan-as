package com.example.controle_gastos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.controle_gastos.R;
import com.example.controle_gastos.model.Categoria;
import java.util.List;

public class CategoriaSpinnerAdapter extends ArrayAdapter<Categoria> {

    private final Context context;
    private final List<Categoria> categorias;

    public CategoriaSpinnerAdapter(@NonNull Context context, @NonNull List<Categoria> categorias) {
        super(context, R.layout.item_spinner_categoria, categorias);
        this.context = context;
        this.categorias = categorias;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return criarView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return criarView(position, convertView, parent);
    }

    private View criarView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_spinner_categoria, parent, false);
        }

        View bolinha = convertView.findViewById(R.id.viewCorSpinner);
        TextView nome = convertView.findViewById(R.id.tvNomeSpinner);

        Categoria c = categorias.get(position);
        nome.setText(c.getNome());

        try {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor(c.getCor()));
            bolinha.setBackground(circle);
        } catch (Exception e) {
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(Color.parseColor("#64748B"));
            bolinha.setBackground(circle);
        }

        return convertView;
    }
}