package com.pcms.reportservice;

import com.pcms.reportservice.client.InventoryClient;
import com.pcms.reportservice.client.OrderClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "pcms.eureka.resilient-shutdown=false",
        "spring.task.scheduling.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:report_service;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReportServiceApplicationContextTest {

    @MockitoBean
    private OrderClient orderClient;

    @MockitoBean
    private InventoryClient inventoryClient;

    @Test
    void contextLoads() {
    }
}
