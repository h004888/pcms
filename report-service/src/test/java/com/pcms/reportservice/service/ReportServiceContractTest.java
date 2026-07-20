package com.pcms.reportservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ReportServiceContractTest {

    @Test
    void reportServiceExposesOnlyBranchAwareRealtimeStats() {
        List<Method> realtimeStatsMethods = Arrays.stream(ReportService.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("realtimeStats"))
                .toList();

        assertThat(realtimeStatsMethods)
                .singleElement()
                .satisfies(method -> assertThat(method.getParameterTypes()).containsExactly(UUID.class));
    }

    @Test
    void reportServiceDoesNotExposeLegacySchedulingOperations() {
        List<String> methodNames = Arrays.stream(ReportService.class.getDeclaredMethods())
                .map(Method::getName)
                .toList();

        assertThat(methodNames).doesNotContain("createSchedule", "listSchedules");
    }

    @Test
    void legacyScheduleDtosAreAbsent() {
        assertThatExceptionOfType(ClassNotFoundException.class)
                .isThrownBy(() -> Class.forName("com.pcms.reportservice.dto.CreateScheduleRequest"));
        assertThatExceptionOfType(ClassNotFoundException.class)
                .isThrownBy(() -> Class.forName("com.pcms.reportservice.dto.ScheduleResponse"));
    }
}
