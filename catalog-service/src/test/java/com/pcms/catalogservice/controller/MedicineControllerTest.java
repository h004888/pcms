package com.pcms.catalogservice.controller;

import com.pcms.catalogservice.dto.response.MedicineMediaSummaryResponse;
import com.pcms.catalogservice.service.ImageStorageService;
import com.pcms.catalogservice.service.MedicineExportService;
import com.pcms.catalogservice.service.MedicineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MedicineControllerTest {

    private MockMvc mockMvc;
    private MedicineService medicineService;
    private ImageStorageService imageStorageService;
    private MedicineExportService medicineExportService;

    @BeforeEach
    void setUp() {
        medicineService = mock(MedicineService.class);
        imageStorageService = mock(ImageStorageService.class);
        medicineExportService = mock(MedicineExportService.class);
        MedicineController controller = new MedicineController(
                medicineService, imageStorageService, medicineExportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnMediaSummariesForRepeatedIdsParam() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<MedicineMediaSummaryResponse> summaries = List.of(
                new MedicineMediaSummaryResponse(id1, "medicine-a",
                        "https://cdn.example.test/a.jpg", "Description A"),
                new MedicineMediaSummaryResponse(id2, "medicine-b",
                        "https://cdn.example.test/b.jpg", "Description B"));
        when(medicineService.getMediaSummaries(anyList())).thenReturn(summaries);

        mockMvc.perform(get("/medicines/media-summary")
                        .param("ids", id1.toString())
                        .param("ids", id2.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[0].slug").value("medicine-a"))
                .andExpect(jsonPath("$[0].imageUrl").value("https://cdn.example.test/a.jpg"))
                .andExpect(jsonPath("$[0].description").value("Description A"))
                .andExpect(jsonPath("$[1].id").value(id2.toString()))
                .andExpect(jsonPath("$[1].slug").value("medicine-b"))
                .andExpect(jsonPath("$[1].imageUrl").value("https://cdn.example.test/b.jpg"))
                .andExpect(jsonPath("$[1].description").value("Description B"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicineService).getMediaSummaries(captor.capture());
        assertThat(captor.getValue()).containsExactly(id1, id2);
    }

    @Test
    void shouldReturnEmptyArrayWhenNoMedicinesMatch() throws Exception {
        UUID id = UUID.randomUUID();
        when(medicineService.getMediaSummaries(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/medicines/media-summary").param("ids", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnEmptyArrayWhenIdsParamMissing() throws Exception {
        when(medicineService.getMediaSummaries(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/medicines/media-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
        verify(medicineService).getMediaSummaries(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    void shouldNotTouchImageStorageOrExportServices() throws Exception {
        UUID id = UUID.randomUUID();
        when(medicineService.getMediaSummaries(anyList()))
                .thenReturn(List.of(new MedicineMediaSummaryResponse(id, "slug", null, "desc")));

        mockMvc.perform(get("/medicines/media-summary").param("ids", id.toString()))
                .andExpect(status().isOk());

        verifyNoInteractions(imageStorageService);
        verifyNoInteractions(medicineExportService);
    }
}