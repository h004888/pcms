package com.pcms.catalogservice.service.impl;

import com.pcms.catalogservice.dto.response.MedicineResponse;
import com.pcms.catalogservice.service.MedicineExportService;
import com.pcms.catalogservice.service.MedicineService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class MedicineExportServiceImpl implements MedicineExportService {

    private final MedicineService medicineService;

    public MedicineExportServiceImpl(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @Override
    public byte[] exportExcel(UUID categoryId) {
        List<MedicineResponse> medicines = medicineService.search(null, categoryId, null, null,
                com.pcms.catalogservice.enums.MedicineStatus.ACTIVE, 0, 1000).data();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Medicines");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("SKU");
            header.createCell(1).setCellValue("Name");
            header.createCell(2).setCellValue("Category ID");
            header.createCell(3).setCellValue("Supplier ID");
            header.createCell(4).setCellValue("Price");
            header.createCell(5).setCellValue("Unit");
            header.createCell(6).setCellValue("Status");

            int rowIndex = 1;
            for (MedicineResponse medicine : medicines) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(medicine.sku());
                row.createCell(1).setCellValue(medicine.name());
                row.createCell(2).setCellValue(String.valueOf(medicine.categoryId()));
                row.createCell(3).setCellValue(String.valueOf(medicine.supplierId()));
                row.createCell(4).setCellValue(medicine.price().doubleValue());
                row.createCell(5).setCellValue(medicine.unit());
                row.createCell(6).setCellValue(medicine.status().name());
            }

            workbook.write(output);
            return output.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to export medicine Excel", ex);
        }
    }
}