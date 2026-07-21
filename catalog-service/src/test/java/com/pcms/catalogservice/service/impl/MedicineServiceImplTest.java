package com.pcms.catalogservice.service.impl;

import com.pcms.catalogservice.client.CategoryClient;
import com.pcms.catalogservice.client.SupplierClient;
import com.pcms.catalogservice.dto.response.MedicineMediaSummaryResponse;
import com.pcms.catalogservice.entity.Medicine;
import com.pcms.catalogservice.repository.MedicineRepository;
import com.pcms.catalogservice.service.ImageStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MedicineServiceImplTest {

    private MedicineRepository medicineRepository;
    private CategoryClient categoryClient;
    private SupplierClient supplierClient;
    private ImageStorageService imageStorageService;
    private MedicineServiceImpl service;

    @BeforeEach
    void setUp() {
        medicineRepository = mock(MedicineRepository.class);
        categoryClient = mock(CategoryClient.class);
        supplierClient = mock(SupplierClient.class);
        imageStorageService = mock(ImageStorageService.class);
        service = new MedicineServiceImpl(medicineRepository, categoryClient, supplierClient, imageStorageService);
    }

    @Test
    void shouldReturnMediaSummariesForAllExistingMedicines() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Medicine medicine1 = buildMedicine(id1, "medicine-a", "medicine-a", "https://cdn.example.test/a.jpg", "Desc A");
        Medicine medicine2 = buildMedicine(id2, "medicine-b", "medicine-b", "https://cdn.example.test/b.jpg", "Desc B");

        when(medicineRepository.findAllById(List.of(id1, id2))).thenReturn(List.of(medicine1, medicine2));

        List<MedicineMediaSummaryResponse> result = service.getMediaSummaries(List.of(id1, id2));

        assertThat(result).hasSize(2);
        MedicineMediaSummaryResponse first = result.get(0);
        assertThat(first.id()).isEqualTo(id1);
        assertThat(first.slug()).isEqualTo("medicine-a");
        assertThat(first.imageUrl()).isEqualTo("https://cdn.example.test/a.jpg");
        assertThat(first.description()).isEqualTo("Desc A");

        MedicineMediaSummaryResponse second = result.get(1);
        assertThat(second.id()).isEqualTo(id2);
        assertThat(second.slug()).isEqualTo("medicine-b");
        assertThat(second.imageUrl()).isEqualTo("https://cdn.example.test/b.jpg");
        assertThat(second.description()).isEqualTo("Desc B");
    }

    @Test
    void shouldReturnEmptyListWhenIdsAreEmptyAndNotCallRepository() {
        List<MedicineMediaSummaryResponse> result = service.getMediaSummaries(List.of());

        assertThat(result).isEmpty();
        verify(medicineRepository, never()).findAllById(any());
    }

    @Test
    void shouldIgnoreIdsThatDoNotExist() {
        UUID existingId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        Medicine medicine = buildMedicine(existingId, "medicine-x", "medicine-x",
                "https://cdn.example.test/x.jpg", "Desc X");

        when(medicineRepository.findAllById(List.of(existingId, missingId)))
                .thenReturn(List.of(medicine));

        List<MedicineMediaSummaryResponse> result = service.getMediaSummaries(List.of(existingId, missingId));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(existingId);
    }

    @Test
    void shouldPassRequestedIdsToRepositoryExactlyOnce() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(medicineRepository.findAllById(any())).thenReturn(List.of());

        service.getMediaSummaries(List.of(id1, id2));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicineRepository).findAllById(captor.capture());
        assertThat(captor.getValue()).containsExactly(id1, id2);
    }

    @Test
    void shouldPreserveNullImageUrlWithoutGeneratingFakeUrl() {
        UUID id = UUID.randomUUID();
        Medicine medicine = buildMedicine(id, "no-image", "no-image", null, "No image desc");

        when(medicineRepository.findAllById(List.of(id))).thenReturn(List.of(medicine));

        List<MedicineMediaSummaryResponse> result = service.getMediaSummaries(List.of(id));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).imageUrl()).isNull();
    }

    @Test
    void shouldPreserveEmptyImageUrlWithoutGeneratingFakeUrl() {
        UUID id = UUID.randomUUID();
        Medicine medicine = buildMedicine(id, "blank-image", "blank-image", "", "Blank image desc");

        when(medicineRepository.findAllById(List.of(id))).thenReturn(List.of(medicine));

        List<MedicineMediaSummaryResponse> result = service.getMediaSummaries(List.of(id));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).imageUrl()).isEqualTo("");
    }

    private Medicine buildMedicine(UUID id, String name, String slug, String imageUrl, String description) {
        Medicine medicine = new Medicine();
        medicine.setId(id);
        medicine.setSku("SKU-" + id.toString().substring(0, 8));
        medicine.setSlug(slug);
        medicine.setName(name);
        medicine.setImageUrl(imageUrl);
        medicine.setDescription(description);
        return medicine;
    }
}