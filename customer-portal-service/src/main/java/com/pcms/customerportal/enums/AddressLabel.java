package com.pcms.customerportal.enums;

/**
 * Address label per FR14.21 - one of {home, office, other}.
 * Customers can save multiple addresses; this label helps them identify
 * which one to pick at checkout.
 */
public enum AddressLabel {
    HOME,
    OFFICE,
    OTHER
}
