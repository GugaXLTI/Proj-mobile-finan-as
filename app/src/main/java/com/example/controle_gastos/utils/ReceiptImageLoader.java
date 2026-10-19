package com.example.controle_gastos.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.InputStream;

/**
 * Responsável por transformar uma Uri (imagem ou PDF) em Bitmap.
 *
 * - Imagens: usa BitmapFactory com inSampleSize para não estourar memória.
 * - PDFs: usa PdfRenderer e renderiza APENAS a primeira página.
 *
 * Não faz OCR nem parsing. Só entrega o Bitmap pronto para o ML Kit.
 */
public class ReceiptImageLoader {

    private static final String TAG = "ReceiptImageLoader";

    // Limite de dimensão do maior lado. Evita OutOfMemory em fotos gigantes.
    private static final int MAX_DIMENSAO = 2048;

    /**
     * Carrega uma imagem (jpg, png, webp...) a partir da Uri.
     * Retorna null em caso de erro.
     */
    public static Bitmap carregarImagem(Context context, Uri uri) {
        if (context == null || uri == null) return null;

        InputStream is = null;
        try {
            // 1ª passada: só para descobrir as dimensões reais
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            fecharSilencioso(is);

            // Calcula quanto reduzir para caber em MAX_DIMENSAO
            options.inSampleSize = calcularInSampleSize(
                    options.outWidth, options.outHeight, MAX_DIMENSAO);
            options.inJustDecodeBounds = false;

            // 2ª passada: carrega de verdade
            is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            fecharSilencioso(is);

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar imagem da Uri: " + uri, e);
            fecharSilencioso(is);
            return null;
        }
    }

    /**
     * Carrega a PRIMEIRA página de um PDF como Bitmap.
     * Retorna null em caso de erro (PDF corrompido, protegido, sem páginas, etc).
     */
    public static Bitmap carregarPdf(Context context, Uri uri) {
        if (context == null || uri == null) return null;

        ParcelFileDescriptor pfd = null;
        PdfRenderer renderer = null;
        try {
            pfd = context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) {
                Log.e(TAG, "openFileDescriptor retornou null para: " + uri);
                return null;
            }

            renderer = new PdfRenderer(pfd);
            if (renderer.getPageCount() == 0) {
                Log.e(TAG, "PDF sem páginas: " + uri);
                return null;
            }

            PdfRenderer.Page page = renderer.openPage(0);

            // Calcula escala para caber em MAX_DIMENSAO (mínimo 1x para não pixelizar)
            float escala = Math.min(
                    (float) MAX_DIMENSAO / page.getWidth(),
                    (float) MAX_DIMENSAO / page.getHeight()
            );
            if (escala < 1f) escala = 1f;

            int largura = (int) (page.getWidth() * escala);
            int altura = (int) (page.getHeight() * escala);

            Bitmap bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888);
            // Fundo branco: PDFs podem ter transparência, e OCR gosta de fundo claro
            bitmap.eraseColor(Color.WHITE);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();

            return bitmap;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar PDF da Uri: " + uri, e);
            return null;

        } finally {
            try {
                if (renderer != null) renderer.close();
            } catch (Exception ignored) { }
            try {
                if (pfd != null) pfd.close();
            } catch (Exception ignored) { }
        }
    }

    /**
     * Calcula o inSampleSize (potência de 2) para reduzir a imagem.
     */
    private static int calcularInSampleSize(int largura, int altura, int maxDimensao) {
        int inSampleSize = 1;
        if (largura <= 0 || altura <= 0) return inSampleSize;

        while ((largura / (inSampleSize * 2)) >= maxDimensao
                || (altura / (inSampleSize * 2)) >= maxDimensao) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private static void fecharSilencioso(InputStream is) {
        if (is != null) {
            try { is.close(); } catch (Exception ignored) { }
        }
    }
}