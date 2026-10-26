package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.example.controle_gastos.model.ParsedReceipt;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReceiptAiParser {

    private static final String TAG = "ReceiptAiParser";
    private static final int MAX_LEN = 100;

    private final Context context;
    private Interpreter interpreter;
    private Map<String, Integer> vocab;
    private final String[] indexToTag = {"O", "B-VALOR", "I-VALOR", "B-DATA", "B-BANCO"};

    public ReceiptAiParser(Context context) {
        this.context = context;
        carregarModelo();
        carregarVocabulario();
    }

    private void carregarModelo() {
        try {
            AssetFileDescriptor fileDescriptor = context.getAssets().openFd("modelo_comprovante.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
            interpreter = new Interpreter(buffer);
            Log.d(TAG, "Modelo TFLite carregado com sucesso.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar modelo TFLite", e);
        }
    }

    private void carregarVocabulario() {
        vocab = new HashMap<>();
        try {
            InputStream is = context.getAssets().open("vocab.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject jsonObject = new JSONObject(sb.toString());
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                vocab.put(key, jsonObject.getInt(key));
            }
            Log.d(TAG, "Vocabulário carregado: " + vocab.size() + " palavras.");
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar vocab.json", e);
        }
    }

    public ParsedReceipt extrair(String textoOcr) {
        ParsedReceipt result = new ParsedReceipt();
        if (interpreter == null || vocab == null) return result;

        String[] palavras = textoOcr.split("\\s+");
        float[][] input = new float[1][MAX_LEN];
        String[] palavrasUsadas = new String[MAX_LEN];

        int count = 0;
        for (String p : palavras) {
            if (count >= MAX_LEN) break;
            palavrasUsadas[count] = p;
            int index = vocab.getOrDefault(p, 0);
            input[0][count] = index;
            count++;
        }

        float[][][] output = new float[1][MAX_LEN][indexToTag.length];
        interpreter.run(input, output);

        StringBuilder valorAchado = new StringBuilder();
        StringBuilder dataAchada = new StringBuilder();
        StringBuilder bancoAchado = new StringBuilder();

        for (int i = 0; i < count; i++) {
            int melhorTagIndex = 0;
            float melhorProbabilidade = 0;

            for (int j = 0; j < indexToTag.length; j++) {
                if (output[0][i][j] > melhorProbabilidade) {
                    melhorProbabilidade = output[0][i][j];
                    melhorTagIndex = j;
                }
            }

            String tag = indexToTag[melhorTagIndex];
            String palavra = palavrasUsadas[i];

            if (tag.contains("VALOR")) valorAchado.append(palavra).append(" ");
            else if (tag.contains("DATA")) dataAchada.append(palavra).append(" ");
            else if (tag.contains("BANCO")) bancoAchado.append(palavra).append(" ");
        }

        // Limpa o valor (remove R$ e letras)
        String valorBruto = valorAchado.toString().replace("R$", "").trim();
        valorBruto = valorBruto.replaceAll("[^0-9,.]", "");
        if (!valorBruto.isEmpty()) {
            try {
                valorBruto = valorBruto.replace(".", "").replace(",", ".");
                result.setValor(Double.parseDouble(valorBruto));
            } catch (Exception e) {
                Log.e(TAG, "Erro ao converter valor: " + valorBruto);
            }
        }

        String dataBruta = dataAchada.toString().trim();
        if (!dataBruta.isEmpty()) result.setData(dataBruta);

        String bancoBruto = bancoAchado.toString().trim();
        if (!bancoBruto.isEmpty()) result.setBanco(bancoBruto);

        Log.d(TAG, "IA extraiu -> Valor: " + result.getValor() + " | Data: " + result.getData() + " | Banco: " + result.getBanco());
        return result;
    }
}