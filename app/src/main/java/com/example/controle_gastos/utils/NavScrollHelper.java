package com.example.controle_gastos.utils;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.example.controle_gastos.R;

/**
 * Esconde a barra inferior flutuante ao rolar para baixo e mostra ao rolar para cima.
 * Também desenha um degradê atrás da barra para o conteúdo não aparecer cortado.
 *
 * Uso: NavScrollHelper.attach(this); logo depois do setContentView(...).
 * Requisitos: a tela precisa ter um ScrollView (ou NestedScrollView) e a barra com id bottomNavigation.
 */
public final class NavScrollHelper {

    private static final int LIMIAR_PX = 6;
    private static final int DURACAO_MS = 220;

    private NavScrollHelper() {
    }

    public static void attach(Activity activity) {
        final View nav = activity.findViewById(R.id.bottomNavigation);
        View raiz = activity.findViewById(android.R.id.content);
        if (nav == null || raiz == null) return;

        View scroll = encontrarScroll(raiz);
        if (scroll == null) return;

        final View scrim = criarScrim(activity, nav);
        final boolean[] visivel = {true};

        scroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            int dy = scrollY - oldScrollY;
            boolean noTopo = !v.canScrollVertically(-1);
            boolean noFim = !v.canScrollVertically(1);

            boolean mostrar;
            if (noTopo || noFim || dy < -LIMIAR_PX) {
                mostrar = true;
            } else if (dy > LIMIAR_PX) {
                mostrar = false;
            } else {
                return;
            }

            if (mostrar == visivel[0]) return;
            visivel[0] = mostrar;

            animar(nav, mostrar, distanciaDaBarra(activity, nav));
            if (scrim != null) {
                animar(scrim, mostrar, scrim.getHeight());
            }
        });
    }

    private static View encontrarScroll(View v) {
        if (v instanceof android.widget.ScrollView || v instanceof NestedScrollView) {
            return v;
        }
        if (v instanceof ViewGroup) {
            ViewGroup grupo = (ViewGroup) v;
            for (int i = 0; i < grupo.getChildCount(); i++) {
                View achado = encontrarScroll(grupo.getChildAt(i));
                if (achado != null) return achado;
            }
        }
        return null;
    }

    private static View criarScrim(Activity activity, View nav) {
        ViewParent pai = nav.getParent();
        if (!(pai instanceof ConstraintLayout)) return null;

        ConstraintLayout raiz = (ConstraintLayout) pai;

        View scrim = new View(activity);
        scrim.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x000A101C, 0xE60A101C, 0xFF0A101C}));

        ConstraintLayout.LayoutParams lp =
                new ConstraintLayout.LayoutParams(0, dp(activity, 120));
        lp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        lp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

        // Entra logo abaixo da barra na ordem de desenho: fica atrás dela e na frente do scroll
        raiz.addView(scrim, raiz.indexOfChild(nav), lp);
        return scrim;
    }

    private static float distanciaDaBarra(Activity activity, View nav) {
        int margem = 0;
        ViewGroup.LayoutParams lp = nav.getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            margem = ((ViewGroup.MarginLayoutParams) lp).bottomMargin;
        }
        return nav.getHeight() + margem + dp(activity, 24);
    }

    private static void animar(View alvo, boolean mostrar, float distancia) {
        alvo.animate()
                .translationY(mostrar ? 0f : distancia)
                .alpha(mostrar ? 1f : 0f)
                .setDuration(DURACAO_MS)
                .start();
    }

    private static int dp(Activity activity, int valor) {
        return Math.round(valor * activity.getResources().getDisplayMetrics().density);
    }
}