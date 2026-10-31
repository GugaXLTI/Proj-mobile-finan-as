package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class CsvExportHelper {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    public static File gerarCsv(Context context, List<Divida> dividas, List<Transacao> transacoes, String mesAno) {
        try {
            String nomeArquivo = "extrato_" + mesAno.toLowerCase()
                    .replace(" ", "_") + ".csv";

            File pasta = new File(context.getCacheDir(), "exports");
            if (!pasta.exists()) pasta.mkdirs();

            File arquivo = new File(pasta, nomeArquivo);
            FileOutputStream fos = new FileOutputStream(arquivo);
            OutputStreamWriter writer = new OutputStreamWriter(fos, StandardCharsets.UTF_8);

            writer.write("\uFEFF"); // BOM UTF-8

            writer.write("Extrato de " + mesAno + "\n");
            writer.write("Gerado pelo ORG Gestão Financeira\n\n");

            // ============ RESUMO ============
            double total = 0, pago = 0;
            for (Divida d : dividas) {
                if (d.isExcluida()) continue;
                total += d.getValorTotal();
                pago += d.getValorPago();
            }
            double faltaPagar = total - pago;

            double totalEntradas = 0, totalSaidas = 0;
            for (Transacao t : transacoes) {
                if ("ENTRADA".equals(t.tipo)) totalEntradas += t.valor;
                else totalSaidas += t.valor;
            }

            writer.write("RESUMO DO MÊS\n");
            writer.write("Dívidas Totais;Dívidas Pagas;Dívidas Pendentes;Entradas Auto;Saídas Auto\n");
            writer.write(String.format(LOCALE_BR, "%.2f;%.2f;%.2f;%.2f;%.2f\n\n",
                    total, pago, faltaPagar, totalEntradas, totalSaidas));

            // ============ DÍVIDAS MANUAIS ============
            writer.write("DÍVIDAS E CONTAS MANUAIS\n");
            writer.write("Título;Categoria;Banco;Parcela;Vencimento;Valor Total;Valor Pago;Falta;Status\n");

            for (Divida d : dividas) {
                String status = d.isExcluida() ? "Excluída" : d.isPago() ? "Liquidado" : d.getValorPago() > 0 ? "Parcial" : "Pendente";
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

            // ⭐ ============ TRANSAÇÕES AUTOMÁTICAS ============
            writer.write("\nTRANSAÇÕES AUTOMÁTICAS (NOTIFICAÇÕES)\n");
            writer.write("Data;Descrição;Tipo;Categoria;Valor\n");

            for (Transacao t : transacoes) {
                String valorFormatado = String.format(LOCALE_BR, "%.2f", t.valor);
                if ("SAIDA".equals(t.tipo)) {
                    valorFormatado = "- " + valorFormatado; // Sinaliza saída visualmente
                }

                writer.write(
                        escapeCsv(t.data) + ";" +
                                escapeCsv(t.descricao) + ";" +
                                escapeCsv(t.tipo) + ";" +
                                escapeCsv(t.categoria) + ";" +
                                valorFormatado + "\n"
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

    public static void compartilharCsv(Context context, File arquivo) {
        if (arquivo == null || !arquivo.exists()) return;

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", arquivo);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Extrato - Controle Gastos");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Compartilhar CSV via..."));
    }

    private static String escapeCsv(String valor) {
        if (valor == null) return "";
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }
}