package com.pcms.customerportal.controller;

import com.pcms.customerportal.client.BranchClient;
import com.pcms.customerportal.dto.response.BranchListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreControllerTest {

    @Mock
    private BranchClient branchClient;

    @InjectMocks
    private StoreController storeController;

    private Map<String, Object> buildBranch(String code, String name, String province) {
        return Map.of(
                "id", "id-" + code,
                "code", code,
                "name", name,
                "address", "123 Test St",
                "phone", "0123456789",
                "province", province,
                "district", "Test District",
                "lat", 10.0,
                "lng", 106.0,
                "openHours", "06:00 - 23:00"
        );
    }

    @Test
    void provinces_shouldReturnDistinctSortedProvinces() {
        Map<String, Object> pageResponse = Map.of("data", List.of(
                buildBranch("HCM-Q1", "CN Q1", "Hồ Chí Minh"),
                buildBranch("HN-HK", "CN HK", "Hà Nội"),
                buildBranch("DN-HC", "CN HC", "Đà Nẵng"),
                buildBranch("HCM-Q3", "CN Q3", "Hồ Chí Minh")
        ));
        when(branchClient.list(anyInt(), anyInt(), isNull(), isNull())).thenReturn(pageResponse);

        ResponseEntity<List<String>> result = storeController.provinces();

        assertThat(result.getBody()).containsExactlyInAnyOrder("Đà Nẵng", "Hà Nội", "Hồ Chí Minh");
    }

    @Test
    void locator_shouldFilterByProvince() {
        Map<String, Object> pageResponse = Map.of("data", List.of(
                buildBranch("HCM-Q1", "CN Q1", "Hồ Chí Minh")
        ));
        when(branchClient.list(eq(0), eq(50), eq("Hồ Chí Minh"), isNull())).thenReturn(pageResponse);

        ResponseEntity<BranchListResponse> result = storeController.locator("Hồ Chí Minh", null, 0, 50);

        assertThat(result.getBody().total()).isEqualTo(1);
        assertThat(result.getBody().branches().get(0).code()).isEqualTo("HCM-Q1");
    }
}
