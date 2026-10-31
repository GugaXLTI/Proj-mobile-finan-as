package com.example.controle_gastos.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import com.example.controle_gastos.model.Divida;
import com.example.controle_gastos.model.Transacao;
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

    private static final BaseColor COR_CARD = new BaseColor(0x13, 0x1C, 0x2E);
    private static final BaseColor COR_WHITE = new BaseColor(0xFF, 0xFF, 0xFF);
    private static final BaseColor COR_GRAY = new BaseColor(0x64, 0x74, 0x8B);
    private static final BaseColor COR_GREEN = new BaseColor(0x10, 0xB9, 0x81);
    private static final BaseColor COR_RED = new BaseColor(0xEF, 0x44, 0x44);
    private static final BaseColor COR_PURPLE = new BaseColor(0xA8, 0x55, 0xF7);
    private static final BaseColor COR_BLUE = new BaseColor(0x3B, 0x82, 0xF6);

    public static File gerarPdf(Context context, List<Divida> dividas, List<Transacao> transacoes, String mesAno, String categoria) {
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
            double totalDividas = 0, pago = 0;
            for (Divida d : dividas) {
                if (d.isExcluida()) continue;
                totalDividas += d.getValorTotal();
                pago += d.getValorPago();
            }
            double faltaPagar = totalDividas - pago;

            double totalEntradas = 0;
            for (Transacao t : transacoes) {
                if ("ENTRADA".equals(t.tipo)) totalEntradas += t.valor;
            }

            PdfPTable resumo = new PdfPTable(3);
            resumo.setWidthPercentage(100);
            resumo.setSpacingAfter(20);

            resumo.addCell(criarCelulaResumo("CONTAS A PAGAR",
                    String.format(LOCALE_BR, "R$ %.2f", totalDividas),
                    "Soma das dívidas do mês", valorVermelhoFont, labelFont, detalheFont));

            resumo.addCell(criarCelulaResumo("ENTRADAS AUTO",
                    String.format(LOCALE_BR, "R$ %.2f", totalEntradas),
                    "Pix e créditos via notificação", valorVerdeFont, labelFont, detalheFont));

            resumo.addCell(criarCelulaResumo("JÁ PAGO (MANUAL)",
                    String.format(LOCALE_BR, "R$ %.2f", pago),
                    "Dívidas liquidadas", valorGrandeFont, labelFont, detalheFont));

            document.add(resumo);

            // ============ TABELA DE DÍVIDAS ============
            Paragraph tituloLista = new Paragraph(
                    "DÍVIDAS E CONTAS MANUAIS",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COR_WHITE));
            tituloLista.setSpacingAfter(12);
            document.add(tituloLista);

            for (Divida d : dividas) {
                PdfPTable item = new PdfPTable(2);
                item.setWidthPercentage(100);
                item.setWidths(new float[]{3f, 1.5f});
                item.setSpacingAfter(6);

                PdfPCell cellLeft = new PdfPCell();
                cellLeft.setBackgroundColor(COR_CARD);
                cellLeft.setBorder(0);
                cellLeft.setPadding(10);

                Paragraph titulo = new Paragraph(d.getTitulo(), itemFont);
                cellLeft.addElement(titulo);

                String detalhe = d.isExcluida() ? "Cancelamento • " + d.getVencimento() : "Vence em " + d.getVencimento() + " • " + d.getBanco();
                Paragraph det = new Paragraph(detalhe, detalheFont);
                det.setSpacingBefore(2);
                cellLeft.addElement(det);

                item.addCell(cellLeft);

                PdfPCell cellRight = new PdfPCell();
                cellRight.setBackgroundColor(COR_CARD);
                cellRight.setBorder(0);
                cellRight.setPadding(10);
                cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);

                BaseColor corValor = definirCorValor(d);
                Font valorFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, corValor);
                Paragraph valor = new Paragraph(String.format(LOCALE_BR, "R$ %.2f", d.getValorTotal()), valorFont);
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

            // ⭐ ============ TABELA DE TRANSAÇÕES AUTOMÁTICAS ============
            if (!transacoes.isEmpty()) {
                document.add(new Paragraph("\n")); // Espaçamento

                Paragraph tituloTransacoes = new Paragraph(
                        "TRANSAÇÕES AUTOMÁTICAS (NOTIFICAÇÕES)",
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COR_WHITE));
                tituloTransacoes.setSpacingAfter(12);
                document.add(tituloTransacoes);

                for (Transacao t : transacoes) {
                    PdfPTable itemT = new PdfPTable(2);
                    itemT.setWidthPercentage(100);
                    itemT.setWidths(new float[]{3f, 1.5f});
                    itemT.setSpacingAfter(6);

                    PdfPCell cellLeftT = new PdfPCell();
                    cellLeftT.setBackgroundColor(COR_CARD);
                    cellLeftT.setBorder(0);
                    cellLeftT.setPadding(10);

                    Paragraph tituloT = new Paragraph(t.descricao, itemFont);
                    cellLeftT.addElement(tituloT);

                    Paragraph detT = new Paragraph(t.data + " • " + t.categoria, detalheFont);
                    detT.setSpacingBefore(2);
                    cellLeftT.addElement(detT);

                    itemT.addCell(cellLeftT);

                    PdfPCell cellRightT = new PdfPCell();
                    cellRightT.setBackgroundColor(COR_CARD);
                    cellRightT.setBorder(0);
                    cellRightT.setPadding(10);
                    cellRightT.setHorizontalAlignment(Element.ALIGN_RIGHT);

                    boolean isEntrada = "ENTRADA".equals(t.tipo);
                    BaseColor corT = isEntrada ? COR_GREEN : COR_RED;
                    Font valorFontT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, corT);

                    String prefixo = isEntrada ? "+ " : "- ";
                    Paragraph valorT = new Paragraph(prefixo + String.format(LOCALE_BR, "R$ %.2f", t.valor), valorFontT);
                    valorT.setAlignment(Element.ALIGN_RIGHT);
                    cellRightT.addElement(valorT);

                    Font statusFontT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, corT);
                    Paragraph statusT = new Paragraph(t.tipo, statusFontT);
                    statusT.setSpacingBefore(2);
                    statusT.setAlignment(Element.ALIGN_RIGHT);
                    cellRightT.addElement(statusT);

                    itemT.addCell(cellRightT);
                    document.add(itemT);
                }
            }

            document.close();
            return arquivo;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static PdfPCell criarCelulaResumo(String label, String valor, String detalhe, Font valorFont, Font labelFont, Font detalheFont) {
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
        if (d.getValorPago() > 0) return "Parcial";
        return "Pendente";
    }

    public static void compartilharPdf(Context context, File arquivo) {
        if (arquivo == null || !arquivo.exists()) return;

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", arquivo);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Extrato - Controle Gastos");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, "Compartilhar PDF via..."));
    }
}