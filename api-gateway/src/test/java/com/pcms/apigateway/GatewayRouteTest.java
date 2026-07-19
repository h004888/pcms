package com.pcms.apigateway;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GatewayRouteTest {

    private String loadRoutes() throws IOException {
        return Files.readString(Path.of(
                "src/main/resources/application.yml"));
    }

    @Test
    void customerPortalRouteHasB2cOrderPaths() throws IOException {
        String yml = loadRoutes();

        assertTrue(yml.contains("/api/v1/orders/history"),
                "customer-portal-service route must include /api/v1/orders/history");
        assertTrue(yml.contains("/api/v1/orders/*/track"),
                "customer-portal-service route must include /api/v1/orders/*/track");
        assertTrue(yml.contains("/api/v1/orders/*/detail"),
                "customer-portal-service route must include /api/v1/orders/*/detail");
    }

    @Test
    void orderServiceRouteStillHasOrdersCatchAll() throws IOException {
        String yml = loadRoutes();

        assertTrue(yml.contains("/api/v1/orders/**"),
                "order-service route must still match /api/v1/orders/**");
    }
}
