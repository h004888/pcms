package com.pcms.customerportal.enums;

/**
 * Cart lifecycle.
 * <ul>
 *   <li>ACTIVE — open cart, items can be added/removed</li>
 *   <li>CHECKED_OUT — order has been created from this cart (terminal)</li>
 *   <li>ABANDONED — abandoned cart recovery job will target these (NSF + future)</li>
 * </ul>
 */
public enum CartStatus {
    ACTIVE,
    CHECKED_OUT,
    ABANDONED
}