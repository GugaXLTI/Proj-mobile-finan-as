package com.example.controle_gastos.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

/**
 * Wrapper do ML Kit Text Recognition.
 *
 * Recebe um Bitmap e devolve o texto bruto extraído.
 * Roda 100% on-device (offline, gratuito).
 *
 * Não faz parsing. Só entrega o texto para o ReceiptParser.
 */
public class ReceiptOcr {

    private static final String TAG = "ReceiptOcr";

    public interface Callback {
        void onSucesso(String texto);
        void onErro(Exception e);
    }

    /**
     * Extrai o texto do bitmap de forma assíncrona.
     * O callback é chamado na thread principal.
     */
    public static void extrairTexto(Bitmap bitmap, Callback callback) {
        if (bitmap == null) {
            callback.onErro(new IllegalArgumentException("Bitmap nulo"));
            return;
        }

        TextRecognizer recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS);

        InputImage image = InputImage.fromBitmap(bitmap, 0);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String texto = visionText.getText();
                    Log.d(TAG, "Texto extraído (" + texto.length() + " chars):");
                    Log.d(TAG, texto);
                    recognizer.close();
                    callback.onSucesso(texto);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro no OCR", e);
                    recognizer.close();
                    callback.onErro(e);
                });
    }
}