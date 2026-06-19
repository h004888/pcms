package com.pcms.customerportal.dto.response;

import java.util.List;

/**
 * STORE-LOCATOR response. Wraps a list of branches for the B2C store finder.
 */
public record BranchListResponse(
        List<BranchSummary> branches,
        int total
) {

    public record BranchSummary(
            String id,
            String code,
            String name,
            String address,
            String phone,
            String province,
            String district,
            Double lat,
            Double lng,
            String openHours,
            List<String> services
    ) {}
}
