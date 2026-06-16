package com.pcms.reportservice.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Excel export service (B-12).
 * Uses Apache POI to generate .xlsx files from a list of row maps.
 */
@Service
public class ExcelExportService {

    /**
     * Build a simple Excel file from a header row + data rows.
     *
     * @param sheetName name of the worksheet
     * @param headers   column headers (e.g. "ID", "Name", "Total")
     * @param rows      data rows; each map's keys must match headers
     * @return byte[] ready to be written to a file or HTTP response
     */
    public byte[] exportToExcel(String sheetName, List<String> headers, List<Map<String, Object>> rows) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int r = 0; r < rows.size(); r++) {
                Row dataRow = sheet.createRow(r + 1);
                Map<String, Object> row = rows.get(r);
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = dataRow.createCell(c);
                    Object value = row.get(headers.get(c));
                    if (value == null) {
                        cell.setCellValue("");
                    } else if (value instanceof Number) {
                        cell.setCellValue(((Number) value).doubleValue());
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new com.pcms.common.exception.InvalidOperationException(
                    "Failed to generate Excel: " + e.getMessage(),
                    "Lỗi tạo file Excel: " + e.getMessage());
        }
    }
}
