package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.controle_gastos.model.Divida;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class CsvExportHelper {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    /**
     * Gera um arquivo CSV com o resumo do mês + lista de lançamentos.
     *
     * @param context Contexto da Activity
     * @param dividas Lista de dívidas do mês (inclui excluídas)
     * @param mesAno  Nome do mês/ano (ex: "Setembro 2026")
     * @return File gerado (ou null em caso de erro)
     */
    public static File gerarCsv(Context context, List<Divida> dividas, String mesAno) {
        try {
            // Nome do arquivo
            String nomeArquivo = "extrato_" + mesAno.toLowerCase()
                    .replace(" ", "_") + ".csv";

            // Pasta de cache (temporária, mas compartilhável via FileProvider)
            File pasta = new File(context.getCacheDir(), "exports");
            if (!pasta.exists()) pasta.mkdirs();

            File arquivo = new File(pasta, nomeArquivo);
            FileOutputStream fos = new FileOutputStream(arquivo);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            // ⭐ BOM UTF-8 (para Excel abrir com acentos corretos)
            writer.write("\uFEFF");

            // ⭐ Cabeçalho do arquivo
            writer.write("Extrato de " + mesAno + "\n");
            writer.write("Gerado pelo ORG Gestão Financeira\n\n");

            // ⭐ Resumo do mês
            double total = 0, pago = 0;
            for (Divida d : dividas) {
                if (d.isExcluida()) continue;
                total += d.getValorTotal();
                pago += d.getValorPago();
            }
            double faltaPagar = total - pago;

            writer.write("RESUMO DO MÊS\n");
            writer.write("Total;Já Pago;Falta Pagar\n");
            writer.write(String.format(LOCALE_BR, "%.2f;%.2f;%.2f\n\n", total, pago, faltaPagar));

            // ⭐ Tabela de lançamentos
            writer.write("LANÇAMENTOS\n");
            writer.write("Título;Categoria;Banco;Parcela;Vencimento;Valor Total;Valor Pago;Falta;Status\n");

            for (Divida d : dividas) {
                String status;
                if (d.isExcluida()) {
                    status = "Excluída";
                } else if (d.isPago()) {
                    status = "Liquidado";
                } else if (d.getValorPago() > 0) {
                    status = "Parcial";
                } else {
                    status = "Pendente";
                }

                writer.write(
                        escapeCsv(d.getTitulo()) + ";" +
                                escapeCsv(d.getCategoria()) + ";" +
                                escapeCsv(d.getBanco()) + ";" +
                                escapeCsv(d.getParcela()) + ";" +
                                escapeCsv(d.getVencimento()) + ";" +
                                String.format(LOCALE_BR, "%.2f;", d.getValorTotal()) +
                                String.format(LOCALE_BR, "%.2f;", d.getValorPago()) +
                                String.format(LOCALE_BR, "%.2f;", d.getValorRestante()) +
                                status + "\n"
                );
            }

            writer.flush();
            writer.close();
            fos.close();

            return arquivo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Compartilha o arquivo CSV via Intent (WhatsApp, Email, Drive, etc).
     */
    public static void compartilharCsv(Context context, File arquivo) {
        if (arquivo == null || !arquivo.exists()) return;

        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                arquivo
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Extrato - Controle Gastos");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Compartilhar CSV via..."));
    }

    /**
     * Escapa campos CSV (envolve em aspas se contiver ; ou aspas).
     */
    private static String escapeCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}