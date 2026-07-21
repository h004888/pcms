package com.pcms.customerportal.service.impl;

import com.pcms.common.dto.PageResponse;
import com.pcms.customerportal.client.CatalogClient;
import com.pcms.customerportal.client.CategoryClient;
import com.pcms.customerportal.client.OrderClient;
import com.pcms.customerportal.dto.response.HomePageResponse;
import com.pcms.customerportal.dto.response.HomePageResponse.BestSellerResponse;
import com.pcms.customerportal.repository.HomeBannerRepository;
import com.pcms.customerportal.repository.QuickLinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopHomeServiceImplTest {

    @Mock private HomeBannerRepository bannerRepo;
    @Mock private CategoryClient categoryClient;
    @Mock private OrderClient orderClient;
    @Mock private QuickLinkRepository quickLinkRepository;
    @Mock private DataSource dataSource;
    @Mock private CatalogClient catalogClient;

    private ShopHomeServiceImpl service;

    @BeforeEach
    void setUp() {
        when(categoryClient.list(0, 50)).thenReturn(PageResponse.empty(0, 50));
        service = new ShopHomeServiceImpl(
                bannerRepo,
                categoryClient,
                orderClient,
                quickLinkRepository,
                dataSource,
                catalogClient);
    }

    @Test
    void buildHomePage_enrichesBestSellersWithCatalogImageUrl() {
        UUID medicineA = UUID.randomUUID();
        UUID medicineB = UUID.randomUUID();

        when(orderClient.getTopMedicines(30, 10)).thenReturn(List.of(
                Map.of(
                        "medicineId", medicineA.toString(),
                        "medicineName", "Medicine A",
                        "price", 50000,
                        "soldCount", 42),
                Map.of(
                        "medicineId", medicineB.toString(),
                        "medicineName", "Medicine B",
                        "price", 80000,
                        "soldCount", 17)));

        when(catalogClient.getMediaSummaries(List.of(medicineA.toString(), medicineB.toString())))
                .thenReturn(List.of(
                        Map.of(
                                "id", medicineA.toString(),
                                "slug", "medicine-a",
                                "imageUrl", "https://cdn.example.test/medicine-a.jpg",
                                "description", "Description A"),
                        Map.of(
                                "id", medicineB.toString(),
                                "slug", "medicine-b",
                                "imageUrl", "https://cdn.example.test/medicine-b.jpg",
                                "description", "Description B")));

        HomePageResponse response = service.buildHomePage(null);

        List<BestSellerResponse> bestSellers = response.bestSellers();
        assertThat(bestSellers).hasSize(2);

        BestSellerResponse first = bestSellers.get(0);
        assertThat(first.id()).isEqualTo(medicineA.toString());
        assertThat(first.slug()).isEqualTo("medicine-a");
        assertThat(first.imageUrl()).isEqualTo("https://cdn.example.test/medicine-a.jpg");
        assertThat(first.description()).isEqualTo("Description A");
        assertThat(first.name()).isEqualTo("Medicine A");
        assertThat(first.price().intValue()).isEqualTo(50000);
        assertThat(first.soldCount()).isEqualTo(42L);

        BestSellerResponse second = bestSellers.get(1);
        assertThat(second.id()).isEqualTo(medicineB.toString());
        assertThat(second.slug()).isEqualTo("medicine-b");
        assertThat(second.imageUrl()).isEqualTo("https://cdn.example.test/medicine-b.jpg");
        assertThat(second.description()).isEqualTo("Description B");
        assertThat(second.name()).isEqualTo("Medicine B");
        assertThat(second.price().intValue()).isEqualTo(80000);
        assertThat(second.soldCount()).isEqualTo(17L);

        verify(catalogClient, times(1))
                .getMediaSummaries(List.of(medicineA.toString(), medicineB.toString()));
    }

    @Test
    void buildHomePage_keepsFallbackImageUrlWhenCatalogReturnsEmpty() {
        UUID medicineA = UUID.randomUUID();

        when(orderClient.getTopMedicines(30, 10)).thenReturn(List.of(
                Map.of(
                        "medicineId", medicineA.toString(),
                        "medicineName", "Medicine A",
                        "price", 50000,
                        "soldCount", 42)));
        when(catalogClient.getMediaSummaries(anyList())).thenReturn(List.of());

        HomePageResponse response = service.buildHomePage(null);

        List<BestSellerResponse> bestSellers = response.bestSellers();
        assertThat(bestSellers).hasSize(1);
        BestSellerResponse only = bestSellers.get(0);
        assertThat(only.id()).isEqualTo(medicineA.toString());
        assertThat(only.imageUrl()).isEmpty();
        assertThat(only.slug()).isEmpty();
        assertThat(only.description()).isEmpty();

        verify(catalogClient, times(1))
                .getMediaSummaries(List.of(medicineA.toString()));
    }

    @Test
    void buildHomePage_mergesByMedicineIdNotByPosition() {
        UUID medicineA = UUID.randomUUID();
        UUID medicineB = UUID.randomUUID();

        when(orderClient.getTopMedicines(30, 10)).thenReturn(List.of(
                Map.of(
                        "medicineId", medicineA.toString(),
                        "medicineName", "Medicine A",
                        "price", 50000,
                        "soldCount", 42),
                Map.of(
                        "medicineId", medicineB.toString(),
                        "medicineName", "Medicine B",
                        "price", 80000,
                        "soldCount", 17)));

        when(catalogClient.getMediaSummaries(List.of(medicineA.toString(), medicineB.toString())))
                .thenReturn(List.of(
                        Map.of(
                                "id", medicineB.toString(),
                                "slug", "medicine-b",
                                "imageUrl", "https://cdn.example.test/medicine-b.jpg",
                                "description", "Description B"),
                        Map.of(
                                "id", medicineA.toString(),
                                "slug", "medicine-a",
                                "imageUrl", "https://cdn.example.test/medicine-a.jpg",
                                "description", "Description A")));

        HomePageResponse response = service.buildHomePage(null);

        List<BestSellerResponse> bestSellers = response.bestSellers();
        assertThat(bestSellers).hasSize(2);

        BestSellerResponse first = bestSellers.get(0);
        assertThat(first.id()).isEqualTo(medicineA.toString());
        assertThat(first.slug()).isEqualTo("medicine-a");
        assertThat(first.imageUrl()).isEqualTo("https://cdn.example.test/medicine-a.jpg");
        assertThat(first.description()).isEqualTo("Description A");
        assertThat(first.soldCount()).isEqualTo(42L);
        assertThat(first.price().intValue()).isEqualTo(50000);

        BestSellerResponse second = bestSellers.get(1);
        assertThat(second.id()).isEqualTo(medicineB.toString());
        assertThat(second.slug()).isEqualTo("medicine-b");
        assertThat(second.imageUrl()).isEqualTo("https://cdn.example.test/medicine-b.jpg");
        assertThat(second.description()).isEqualTo("Description B");
        assertThat(second.soldCount()).isEqualTo(17L);
        assertThat(second.price().intValue()).isEqualTo(80000);
    }
}