package com.pcms.customerportal.enums;

/**
 * Voucher discount type per FR19.1.
 * <ul>
 *   <li>PERCENT  — value is a percentage (0-100). Discount = total * value/100 (capped by max_discount)</li>
 *   <li>FIXED    — value is a flat amount (VND). Discount = value</li>
 *   <li>FREE_SHIP — shipping fee becomes 0</li>
 * </ul>
 */
public enum VoucherType {
    PERCENT,
    FIXED,
    FREE_SHIP
}