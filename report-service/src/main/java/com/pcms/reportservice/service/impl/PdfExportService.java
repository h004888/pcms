package com.pcms.reportservice.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

/**
 * PDF export service (B-12).
 * Uses iText 7 to generate PDF files from a list of row maps.
 */
@Service
public class PdfExportService {

    /**
     * Build a simple PDF report from a header row + data rows.
     */
    public byte[] exportToPdf(String title, List<String> headers, List<Map<String, Object>> rows) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf, PageSize.A4.rotate());

            // Title
            PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Paragraph titlePara = new Paragraph(title != null ? title : "Report")
                    .setFont(bold)
                    .setFontSize(16)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titlePara);
            document.add(new Paragraph("\n"));

            // Table
            int colCount = headers.size();
            Table table = new Table(UnitValue.createPercentArray(colCount)).useAllAvailableWidth();

            // Header cells
            for (String header : headers) {
                Cell cell = new Cell().add(new Paragraph(header).setFont(bold));
                cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
                cell.setBorder(new SolidBorder(1));
                cell.setTextAlignment(TextAlignment.CENTER);
                table.addHeaderCell(cell);
            }

            // Data cells
            for (Map<String, Object> row : rows) {
                for (String header : headers) {
                    Object value = row.get(header);
                    String text = value == null ? "" : String.valueOf(value);
                    Cell cell = new Cell().add(new Paragraph(text));
                    cell.setBorder(new SolidBorder(1));
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new com.pcms.common.exception.InvalidOperationException(
                    "Failed to generate PDF: " + e.getMessage(),
                    "Lỗi tạo file PDF: " + e.getMessage());
        }
    }
}
