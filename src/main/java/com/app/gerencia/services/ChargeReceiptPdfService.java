package com.app.gerencia.services;

import com.app.gerencia.entities.Charge;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ChargeReceiptPdfService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(Charge charge) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Recibo de Pagamento", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            String guardianName = charge.getPatient().getGuardian() != null
                    ? charge.getPatient().getGuardian().getName()
                    : "-";

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addField(table, "Paciente:", charge.getPatient().getName(), labelFont, textFont);
            addField(table, "Responsável:", guardianName, labelFont, textFont);
            addField(table, "Valor:", "R$ " + charge.getValor().toPlainString(), labelFont, textFont);
            addField(table, "Forma de pagamento:", charge.getFormaPagamento().name(), labelFont, textFont);
            addField(table, "Data do pagamento:", charge.getDataPagamento().format(DATE_FORMAT), labelFont, textFont);

            document.add(table);
            document.close();

            return out.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar recibo: " + e.getMessage(), e);
        }
    }

    private void addField(PdfPTable table, String label, String value, Font labelFont, Font textFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, textFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(8);
        table.addCell(valueCell);
    }
}
