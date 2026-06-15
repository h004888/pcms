package com.pcms.inventoryservice.enums;

public enum TransactionType {
    IMPORT,         // UC05 - import from supplier
    EXPORT,         // UC05 - export/destroy
    TRANSFER_OUT,   // UC05 AT2
    TRANSFER_IN,    // UC05 AT2
    SALE,           // auto-deducted when order paid
    ADJUSTMENT      // manual correction
}
