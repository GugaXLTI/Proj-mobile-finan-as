package com.example.controle_gastos.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.controle_gastos.database.AppDatabase;
import com.example.controle_gastos.model.ComprovanteProcessado;

/**
 * Helper para o cache de OCR.
 *
 * Chave: (usuarioId, uri). Guarda o texto bruto do OCR.
 * Se o mesmo comprovante for processado de novo, o texto é reaproveitado
 * e o OCR (que é lento) não roda de novo.
 */
public class ReceiptCacheHelper {

    private static final String TAG = "ReceiptCache";

    private ReceiptCacheHelper() { }

    /**
     * Busca o texto OCR cacheado para uma URI.
     * Retorna null se não houver cache.
     */
    public static String buscarTexto(Context context, int usuarioId, Uri uri) {
        if (context == null || uri == null) return null;
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            ComprovanteProcessado c = db.comprovanteProcessadoDao()
                    .buscarPorUri(usuarioId, uri.toString());
            if (c != null && c.textoOcr != null && !c.textoOcr.isEmpty()) {
                Log.d(TAG, "Cache HIT para URI: " + uri);
                return c.textoOcr;
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar cache", e);
        }
        Log.d(TAG, "Cache MISS para URI: " + uri);
        return null;
    }

    /**
     * Salva o texto OCR no cache.
     * Substitui se já existir (chave: usuarioId + uri).
     */
    public static void salvarTexto(Context context, int usuarioId, Uri uri, String texto, String mimeType) {
        if (context == null || uri == null || texto == null || texto.isEmpty()) return;
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            ComprovanteProcessado c = new ComprovanteProcessado(
                    usuarioId, uri.toString(), texto, mimeType);
            db.comprovanteProcessadoDao().inserir(c);
            Log.d(TAG, "Texto salvo no cache. URI: " + uri);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao salvar no cache", e);
        }
    }

    /**
     * Limpa o cache do usuário.
     * Pode ser chamado no "Limpar Tudo" das configurações.
     */
    public static void limpar(Context context, int usuarioId) {
        if (context == null) return;
        try {
            AppDatabase db = AppDatabase.getInstance(context);
            db.comprovanteProcessadoDao().deletarTodosDoUsuario(usuarioId);
            Log.d(TAG, "Cache limpo para usuário: " + usuarioId);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar cache", e);
        }
    }
}