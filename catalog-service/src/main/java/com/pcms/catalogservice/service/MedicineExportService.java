package com.pcms.catalogservice.service;

import java.util.UUID;

public interface MedicineExportService {
    byte[] exportExcel(UUID categoryId);
}