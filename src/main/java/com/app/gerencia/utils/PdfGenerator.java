package com.app.gerencia.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.json.JSONObject;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PdfGenerator {

    public static byte[] generateReferralPdf(
            String selectedFieldsJson,
            String patientName,
            String patientCpf,
            Date sentAt,
            byte[] reportBytes
    ) {
        if (selectedFieldsJson == null || selectedFieldsJson.isBlank()) {
            throw new IllegalArgumentException("JSON dos campos selecionados está vazio ou nulo.");
        }

        try {
            // === Etapa 1: Gera o PDF principal (sem laudo) ===
            ByteArrayOutputStream basePdfStream = new ByteArrayOutputStream();
            createBaseReferralPdf(basePdfStream, selectedFieldsJson, patientName, patientCpf, sentAt, reportBytes != null);

            byte[] basePdf = basePdfStream.toByteArray();

            // === Etapa 2: Se houver laudo, mescla com o relatório ===
            if (reportBytes != null && reportBytes.length > 0) {
                return mergePdfs(basePdf, reportBytes);
            }

            return basePdf;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    // ====================== ETAPA 1 ======================
    private static void createBaseReferralPdf(
            OutputStream outputStream,
            String selectedFieldsJson,
            String patientName,
            String patientCpf,
            Date sentAt,
            boolean hasReports
    ) throws Exception {

        JSONObject data = new JSONObject(selectedFieldsJson);

        Document document = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // === CABEÇALHO COM LOGO ===
        String logoPath = "src/main/resources/static/logo.png";
        File logoFile = new File(logoPath);
        if (logoFile.exists()) {
            Image logo = Image.getInstance(logoPath);
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);
        }

        document.add(Chunk.NEWLINE);

        // === TÍTULO ===
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Relatório de Anamnese", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // === INFORMAÇÕES DO PACIENTE ===
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        Paragraph patientHeader = new Paragraph("Dados do Paciente", labelFont);
        patientHeader.setSpacingBefore(10);
        patientHeader.setSpacingAfter(5);
        document.add(patientHeader);

        PdfPTable patientTable = new PdfPTable(2);
        patientTable.setWidthPercentage(100);
        patientTable.setSpacingAfter(10);

        addStaticField(patientTable, "Nome:", patientName, labelFont, textFont);
        addStaticField(patientTable, "CPF:", patientCpf, labelFont, textFont);

        String formattedDate = "Data não informada";
        if (sentAt != null) {
            formattedDate = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(sentAt);
        }
        addStaticField(patientTable, "Data do Encaminhamento:", formattedDate, labelFont, textFont);

        document.add(patientTable);

        // === SEÇÃO: Informações Gerais ===
        Paragraph infoHeader = new Paragraph("Informações Gerais", labelFont);
        infoHeader.setSpacingBefore(10);
        infoHeader.setSpacingAfter(5);
        document.add(infoHeader);

        PdfPTable infoTable = new PdfPTable(1);
        infoTable.setWidthPercentage(100);

        addField(infoTable, data, "diagnoses", "Diagnóstico do paciente", labelFont, textFont);
        addField(infoTable, data, "medicationAndAllergies", "Medicação e Alergias", labelFont, textFont);
        addField(infoTable, data, "indications", "Indicações", labelFont, textFont);
        addField(infoTable, data, "objectives", "Por qual motivo nos procurou? (Objetivos)", labelFont, textFont);

        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        // === SEÇÃO: Histórico e Conduta ===
        Paragraph historyHeader = new Paragraph("Histórico e Conduta", labelFont);
        historyHeader.setSpacingBefore(10);
        historyHeader.setSpacingAfter(5);
        document.add(historyHeader);

        PdfPTable historyTable = new PdfPTable(1);
        historyTable.setWidthPercentage(100);

        addField(historyTable, data, "developmentHistory", "Gestação - Diagnóstico - Processo de Desenvolvimento - Dias Atuais", labelFont, textFont);
        addField(historyTable, data, "preferences", "Preferências do aluno (a)", labelFont, textFont);
        addField(historyTable, data, "interferingBehaviors", "Comportamentos interferentes e plano de conduta", labelFont, textFont);
        addField(historyTable, data, "qualityOfLife", "Comprometimento da qualidade de vida (aluno e família)", labelFont, textFont);
        addField(historyTable, data, "feeding", "Alimentação (Seletividade - Compulsividade - Acompanhamento Nutricional)", labelFont, textFont);
        addField(historyTable, data, "sleep", "Rotina do sono (agitação, continuidade)", labelFont, textFont);
        addField(historyTable, data, "therapists", "Equipe de terapeutas", labelFont, textFont);

        document.add(historyTable);
        document.add(Chunk.NEWLINE);

        // === SEÇÃO: Laudos (se houver) ===
        if (hasReports) {
            Paragraph reportHeader = new Paragraph("Laudos", labelFont);
            reportHeader.setSpacingBefore(15);
            reportHeader.setSpacingAfter(5);
            document.add(reportHeader);

            Paragraph reportNotice = new Paragraph(
                    "Os laudos referentes ao paciente encontram-se nas páginas seguintes.",
                    textFont
            );
            reportNotice.setSpacingAfter(10);
            document.add(reportNotice);
        }

        document.close();
    }

    // ====================== ETAPA 2 ======================
    private static byte[] mergePdfs(byte[] mainPdf, byte[] reportPdf) throws IOException, DocumentException {
        ByteArrayOutputStream merged = new ByteArrayOutputStream();
        Document document = new Document();
        PdfCopy copy = new PdfCopy(document, merged);

        List<PdfReader> readers = new ArrayList<>();

        try {
            document.open();

            // adiciona primeiro o PDF base
            if (mainPdf != null && mainPdf.length > 0) {
                PdfReader mainReader = new PdfReader(mainPdf);
                readers.add(mainReader);
                copy.addDocument(mainReader);
            }

            // adiciona em seguida o(s) laudo(s)
            if (reportPdf != null && reportPdf.length > 0) {
                PdfReader reportReader = new PdfReader(reportPdf);
                readers.add(reportReader);
                copy.addDocument(reportReader);
            }

            // cria e adiciona a página de rodapé final
            ByteArrayOutputStream footerStream = new ByteArrayOutputStream();
            Document footerDoc = new Document(PageSize.A4);
            PdfWriter.getInstance(footerDoc, footerStream);
            footerDoc.open();

            footerDoc.add(Chunk.NEWLINE);
            Paragraph footer = new Paragraph(
                    "Gerado automaticamente pelo sistema GerenciA ©",
                    FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.GRAY)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            footerDoc.add(footer);

            footerDoc.close();

            PdfReader footerReader = new PdfReader(footerStream.toByteArray());
            readers.add(footerReader);
            copy.addDocument(footerReader);

        } finally {
            if (document.isOpen()) document.close();
            for (PdfReader r : readers) try { r.close(); } catch (Exception ignored) {}
        }

        return merged.toByteArray();
    }

    // ====================== HELPERS ======================
    private static void addField(PdfPTable table, JSONObject data, String key, String label, Font labelFont, Font textFont) {
        if (!data.has(key)) return;

        String value = data.optString(key, "").trim();
        if (value.isEmpty() || value.equalsIgnoreCase("null")) return;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, textFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        table.addCell(valueCell);
    }

    private static void addStaticField(PdfPTable table, String label, String value, Font labelFont, Font textFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "", textFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        table.addCell(valueCell);
    }
}
