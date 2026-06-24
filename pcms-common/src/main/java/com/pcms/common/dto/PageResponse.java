package com.pcms.common.dto;

import java.util.List;

/**
 * Standard paginated response wrapper — PCMS-wide contract.
 * <p>5 fields, promoted to {@code pcms-common} (CR-04).
 * <p>Replaces per-service local PageResponse variants to ensure consistent
 * client experience across all 12 business services.
 *
 * @param data        list of items in the current page
 * @param page        zero-based page index
 * @param size        page size (capped at 100 by service layer)
 * @param total       total number of items
 * @param totalPages  total number of pages = ceil(total / size)
 * @param <T>         item type
 */

public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long total,
        int totalPages
) {
    /**
     * Create a PageResponse from a Spring Data Page.
     */
    public static <T> PageResponse<T> from(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Create a PageResponse from a Spring Data Page with explicit mapper.
     */
    public static <S, T> PageResponse<T> of(
            org.springframework.data.domain.Page<S> page,
            java.util.function.Function<S, T> mapper
    ) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /**
     * Empty page (no results).
     */
    public static <T> PageResponse<T> of(List<T> items, java.util.function.Function<T, T> mapper) {
        return new PageResponse<>(items, 0, items.size(), items.size(), items.size() > 0 ? 1 : 0);
    }

    public static <T> PageResponse<T> empty(int page, int size) {
        return new PageResponse<>(List.of(), page, size, 0L, 0);
    }
}
