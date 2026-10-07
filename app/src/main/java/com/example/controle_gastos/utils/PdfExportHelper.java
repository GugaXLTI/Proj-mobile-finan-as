package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.controle_gastos.model.Divida;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

public class PdfExportHelper {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    // Cores do Figma
    private static final BaseColor COR_CARD = new BaseColor(0x13, 0x1C, 0x2E);
    private static final BaseColor COR_WHITE = new BaseColor(0xFF, 0xFF, 0xFF);
    private static final BaseColor COR_GRAY = new BaseColor(0x64, 0x74, 0x8B);
    private static final BaseColor COR_GREEN = new BaseColor(0x10, 0xB9, 0x81);
    private static final BaseColor COR_RED = new BaseColor(0xEF, 0x44, 0x44);
    private static final BaseColor COR_PURPLE = new BaseColor(0xA8, 0x55, 0xF7);
    private static final BaseColor COR_BLUE = new BaseColor(0x3B, 0x82, 0xF6);

    public static File gerarPdf(Context context, List<Divida> dividas, String mesAno, String categoria) {
        try {
            String sufixo = categoria.equals("Todos") ? "geral" : categoria.toLowerCase().replace(" ", "_");
            String nomeArquivo = "extrato_" + sufixo + "_" + mesAno.toLowerCase()
                    .replace(" ", "_") + ".pdf";

            File pasta = new File(context.getCacheDir(), "exports");
            if (!pasta.exists()) pasta.mkdirs();

            File arquivo = new File(pasta, nomeArquivo);

            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, new FileOutputStream(arquivo));
            document.open();

            // ============ FONTS ============
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COR_WHITE);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA, 11, COR_GRAY);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COR_GRAY);
            Font valorGrandeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COR_WHITE);
            Font valorVerdeFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COR_GREEN);
            Font valorVermelhoFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COR_RED);
            Font itemFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COR_WHITE);
            Font detalheFont = FontFactory.getFont(FontFactory.HELVETICA, 9, COR_GRAY);

            // ============ HEADER ============
            Paragraph header = new Paragraph("ORG", tituloFont);
            header.setAlignment(Element.ALIGN_LEFT);
            document.add(header);

            Paragraph subHeader = new Paragraph("Gestão Financeira • Extrato Consolidado", subtituloFont);
            subHeader.setSpacingAfter(4);
            document.add(subHeader);

            Paragraph mesP = new Paragraph(mesAno + (categoria.equals("Todos") ? "" : " • " + categoria),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COR_PURPLE));
            mesP.setSpacingAfter(20);
            document.add(mesP);

            // ============ RESUMO DO MÊS ============
            double total = 0, pago = 0;
            for (Divida d : dividas) {
                if (d.isExcluida()) continue;
                total += d.getValorTotal();
                pago += d.getValorPago();
            }
            double faltaPagar = total - pago;
            int percPago = total > 0 ? (int) Math.round((pago / total) * 100) : 0;
            int percFalta = 100 - percPago;

            PdfPTable resumo = new PdfPTable(3);
            resumo.setWidthPercentage(100);
            resumo.setSpacingAfter(20);

            resumo.addCell(criarCelulaResumo("TOTAL DO MÊS",
                    String.format(LOCALE_BR, "R$ %.2f", total),
                    "100% faturas", valorGrandeFont, labelFont, detalheFont));

            resumo.addCell(criarCelulaResumo("JÁ PAGO",
                    String.format(LOCALE_BR, "R$ %.2f", pago),
                    percPago + "% liquidado", valorVerdeFont, labelFont, detalheFont));

            resumo.addCell(criarCelulaResumo("FALTA PAGAR",
                    String.format(LOCALE_BR, "R$ %.2f", faltaPagar),
                    percFalta + "% em aberto", valorVermelhoFont, labelFont, detalheFont));

            document.add(resumo);

            // ============ TABELA DE LANÇAMENTOS ============
            Paragraph tituloLista = new Paragraph(
                    "DEMONSTRATIVO " + (categoria.equals("Todos") ? "GERAL" : categoria.toUpperCase()),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COR_WHITE));
            tituloLista.setSpacingAfter(12);
            document.add(tituloLista);

            for (Divida d : dividas) {
                PdfPTable item = new PdfPTable(2);
                item.setWidthPercentage(100);
                item.setWidths(new float[]{3f, 1.5f});
                item.setSpacingAfter(6);

                // Coluna esquerda
                PdfPCell cellLeft = new PdfPCell();
                cellLeft.setBackgroundColor(COR_CARD);
                cellLeft.setBorder(0);
                cellLeft.setPadding(10);

                Paragraph titulo = new Paragraph(d.getTitulo(), itemFont);
                cellLeft.addElement(titulo);

                String detalhe;
                if (d.isExcluida()) {
                    detalhe = "Cancelamento • " + d.getVencimento();
                } else {
                    detalhe = "Vence em " + d.getVencimento() + " • " + d.getBanco();
                }
                Paragraph det = new Paragraph(detalhe, detalheFont);
                det.setSpacingBefore(2);
                cellLeft.addElement(det);

                item.addCell(cellLeft);

                // Coluna direita
                PdfPCell cellRight = new PdfPCell();
                cellRight.setBackgroundColor(COR_CARD);
                cellRight.setBorder(0);
                cellRight.setPadding(10);
                cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

                BaseColor corValor = definirCorValor(d);
                Font valorFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, corValor);
                Paragraph valor = new Paragraph(
                        String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()), valorFont);
                valor.setAlignment(Element.ALIGN_RIGHT);
                cellRight.addElement(valor);

                String status = definirStatus(d);
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, corValor);
                Paragraph statusP = new Paragraph(status, statusFont);
                statusP.setSpacingBefore(2);
                statusP.setAlignment(Element.ALIGN_RIGHT);
                cellRight.addElement(statusP);

                item.addCell(cellRight);

                document.add(item);
            }

            document.close();
            return arquivo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPCell criarCelulaResumo(String label, String valor, String detalhe,
                                              Font valorFont, Font labelFont, Font detalheFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COR_CARD);
        cell.setBorder(0);
        cell.setPadding(12);

        Paragraph labelP = new Paragraph(label, labelFont);
        cell.addElement(labelP);

        Paragraph valorP = new Paragraph(valor, valorFont);
        valorP.setSpacingBefore(4);
        cell.addElement(valorP);

        Paragraph detP = new Paragraph(detalhe, detalheFont);
        detP.setSpacingBefore(2);
        cell.addElement(detP);

        return cell;
    }

    private static BaseColor definirCorValor(Divida d) {
        if (d.isExcluida()) return COR_RED;
        if (d.isPago()) return COR_GREEN;
        if (d.getValorPago() > 0) return COR_BLUE;
        return COR_WHITE;
    }

    private static String definirStatus(Divida d) {
        if (d.isExcluida()) return "Excluída ✗";
        if (d.isPago()) return "Liquidado ✓";
        if (d.getValorPago() > 0) {
            int totalParcelas = extrairNumeroParcelas(d.getParcela());
            double valorParcelaReal = d.getValorTotal() / totalParcelas;
            int parcelasPagas = (int) Math.round(d.getValorPago() / valorParcelaReal);
            return "Parcela " + parcelasPagas + "/" + totalParcelas;
        }
        return "Pendente";
    }

    private static int extrairNumeroParcelas(String parcela) {
        try {
            String numeros = parcela.replaceAll("[^0-9]", "");
            if (numeros.isEmpty()) return 1;
            int n = Integer.parseInt(numeros);
            return n > 0 ? n : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    public static void compartilharPdf(Context context, File arquivo) {
        if (arquivo == null || !arquivo.exists()) return;

        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".fileprovider",
                arquivo
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Extrato - Controle Gastos");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Compartilhar PDF via..."));
    }
}