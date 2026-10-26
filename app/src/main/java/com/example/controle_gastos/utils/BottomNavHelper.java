package com.example.controle_gastos.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.controle_gastos.R;
import com.example.controle_gastos.view.CadastroDividaActivity;
import com.example.controle_gastos.view.ConfiguracoesActivity;
import com.example.controle_gastos.view.DashboardActivity;
import com.example.controle_gastos.view.DividasActivity;
import com.example.controle_gastos.view.InicioActivity;

public final class BottomNavHelper {

    public static final int INICIO = 0;
    public static final int LANCAR = 1;
    public static final int DIVIDAS = 2;
    public static final int RELATORIOS = 3;
    public static final int CONFIG = 4;

    private static final int COR_ATIVA = 0xFF34D399;
    private static final int COR_INATIVA = 0xFF94A3B8;

    private static final int[] TABS = {
            R.id.tabInicio, R.id.tabLancar, R.id.tabDividas, R.id.tabRelatorios, R.id.tabConfig
    };
    private static final int[] ICONES = {
            R.id.ivTabInicio, R.id.ivTabLancar, R.id.ivTabDividas, R.id.ivTabRelatorios, R.id.ivTabConfig
    };
    private static final int[] TEXTOS = {
            R.id.tvTabInicio, R.id.tvTabLancar, R.id.tvTabDividas, R.id.tvTabRelatorios, R.id.tvTabConfig
    };

    private BottomNavHelper() { }

    public static void configurar(final Activity activity, final int abaAtiva) {
        final Class<?>[] destinos = {
                InicioActivity.class,
                CadastroDividaActivity.class,
                DividasActivity.class,
                DashboardActivity.class,
                ConfiguracoesActivity.class
        };

        float d = activity.getResources().getDisplayMetrics().density;
        int altura = Math.round(46 * d);
        int padAtiva = Math.round(18 * d);

        TypedValue tv = new TypedValue();
        activity.getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, tv, true);

        for (int i = 0; i < TABS.length; i++) {
            LinearLayout tab = activity.findViewById(TABS[i]);
            ImageView icone = activity.findViewById(ICONES[i]);
            TextView texto = activity.findViewById(TEXTOS[i]);
            if (tab == null || icone == null || texto == null) continue;

            boolean ativa = (i == abaAtiva);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ativa ? ViewGroup.LayoutParams.WRAP_CONTENT : 0,
                    altura,
                    ativa ? 0f : 1f);
            tab.setLayoutParams(lp);

            if (ativa) {
                tab.setBackgroundResource(R.drawable.bg_tab_ativo);
                tab.setPadding(padAtiva, 0, padAtiva, 0);
                texto.setVisibility(View.VISIBLE);
                icone.setImageTintList(ColorStateList.valueOf(COR_ATIVA));
            } else {
                if (tv.resourceId != 0) {
                    tab.setBackgroundResource(tv.resourceId);
                } else {
                    tab.setBackground(null);
                }
                tab.setPadding(0, 0, 0, 0);
                texto.setVisibility(View.GONE);
                icone.setImageTintList(ColorStateList.valueOf(COR_INATIVA));
            }

            final int destino = i;
            tab.setOnClickListener(v -> {
                if (destino == abaAtiva) return;
                activity.startActivity(new Intent(activity, destinos[destino]));
                activity.finish();

                // ⭐ CORREÇÃO: Removido o "android." antes do R.anim
                if (destino > abaAtiva) {
                    // Clicou em uma aba à direita (ex: Início -> Lançar)
                    activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else {
                    // Clicou em uma aba à esquerda (ex: Lançar -> Início)
                    activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
        }
    }
}