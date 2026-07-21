package com.pcms.branchservice.service.impl;

import com.pcms.branchservice.dto.request.CreateBranchRequest;
import com.pcms.branchservice.dto.request.UpdateBranchRequest;
import com.pcms.branchservice.dto.response.BranchResponse;
import com.pcms.branchservice.entity.Branch;
import com.pcms.branchservice.enums.BranchStatus;
import com.pcms.branchservice.repository.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchServiceImplTest {

    @Mock
    private BranchRepository repository;

    @Mock
    private com.pcms.branchservice.client.UserClient userClient;

    @InjectMocks
    private BranchServiceImpl branchService;

    private Branch createBranch(String code, String name, String address, String phone,
                                String province, String district, Double lat, Double lng) {
        Branch b = new Branch(code, name, address, phone);
        b.setId(UUID.randomUUID());
        b.setStatus(BranchStatus.ACTIVE);
        b.setProvince(province);
        b.setDistrict(district);
        b.setLat(lat);
        b.setLng(lng);
        b.setOpenHours("06:00 - 23:00");
        return b;
    }

    @Test
    void list_shouldFilterByProvince() {
        Branch hcm1 = createBranch("HCM-Q1", "CN Quận 1", "123 Nguyễn Huệ", "0281234567",
                "Hồ Chí Minh", "Quận 1", 10.7757, 106.7004);
        Branch hn = createBranch("HN-HK", "CN Hoàn Kiếm", "789 Tràng Tiền", "0243456789",
                "Hà Nội", "Hoàn Kiếm", 21.0285, 105.8542);
        Page<Branch> page = new PageImpl<>(List.of(hcm1, hn));

        when(repository.searchBranches(isNull(), eq("Hồ Chí Minh"), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hcm1)));

        Page<BranchResponse> result = branchService.list(null, "Hồ Chí Minh", null, Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).province()).isEqualTo("Hồ Chí Minh");
    }

    @Test
    void list_shouldFilterByDistrict() {
        Branch hcm1 = createBranch("HCM-Q1", "CN Quận 1", "123 Nguyễn Huệ", "0281234567",
                "Hồ Chí Minh", "Quận 1", 10.7757, 106.7004);
        when(repository.searchBranches(isNull(), isNull(), eq("Quận 1"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hcm1)));

        Page<BranchResponse> result = branchService.list(null, null, "Quận 1", Pageable.unpaged());

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).district()).isEqualTo("Quận 1");
    }

    @Test
    void toResponse_shouldIncludeNewFields() {
        Branch branch = createBranch("DN-HC", "CN Hải Châu", "321 Lê Duẩn", "0236456789",
                "Đà Nẵng", "Hải Châu", 16.0471, 108.2068);

        BranchResponse response = branchService.toResponse(branch);

        assertThat(response.province()).isEqualTo("Đà Nẵng");
        assertThat(response.district()).isEqualTo("Hải Châu");
        assertThat(response.lat()).isEqualTo(16.0471);
        assertThat(response.lng()).isEqualTo(108.2068);
        assertThat(response.openHours()).isEqualTo("06:00 - 23:00");
    }

    @Test
    void create_shouldAcceptNewFields() {
        CreateBranchRequest request = new CreateBranchRequest("NEW", "CN Mới", "Địa chỉ", "0123456789",
                "Hà Nội", "Ba Đình", 21.03, 105.82, "08:00 - 22:00");
        when(repository.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        BranchResponse response = branchService.create(request);

        assertThat(response.province()).isEqualTo("Hà Nội");
        assertThat(response.district()).isEqualTo("Ba Đình");
        assertThat(response.lat()).isEqualTo(21.03);
        assertThat(response.lng()).isEqualTo(105.82);
        assertThat(response.openHours()).isEqualTo("08:00 - 22:00");
    }
}
