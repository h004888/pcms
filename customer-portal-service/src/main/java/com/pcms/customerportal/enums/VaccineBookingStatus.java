package com.pcms.customerportal.enums;

/**
 * UC14 / Sprint 6 - Vaccine booking lifecycle status.
 *
 * <pre>
 *   BOOKED -> CANCELLED  (customer cancel before slot date)
 *   BOOKED -> COMPLETED  (admin confirms injection at branch)
 *   BOOKED -> NO_SHOW    (customer missed appointment)
 * </pre>
 */
public enum VaccineBookingStatus {
    BOOKED,
    CANCELLED,
    COMPLETED,
    NO_SHOW
}