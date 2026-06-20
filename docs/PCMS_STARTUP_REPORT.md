# PCMS Startup Report - 2026-06-20

## Status: ✅ ALL 18 SERVICES RUNNING

### Services (18/18 UP in Eureka)

| # | Service | Port | Status |
|---|---------|------|--------|
| 1 | API-GATEWAY | 8080 | ✅ UP |
| 2 | USER-SERVICE | 8081 | ✅ UP |
| 3 | BRANCH-SERVICE | 8082 | ✅ UP |
| 4 | CATALOG-SERVICE | 8083 | ✅ UP |
| 5 | CATEGORY-SERVICE | 8084 | ✅ UP |
| 6 | SUPPLIER-SERVICE | 8085 | ✅ UP |
| 7 | INVENTORY-SERVICE | 8086 | ✅ UP |
| 8 | CUSTOMER-SERVICE | 8087 | ✅ UP |
| 9 | ORDER-SERVICE | 8088 | ✅ UP |
| 10 | PAYMENT-SERVICE | 8089 | ✅ UP |
| 11 | PRESCRIPTION-SERVICE | 8090 | ✅ UP |
| 12 | NOTIFICATION-SERVICE | 8091 | ✅ UP |
| 13 | REPORT-SERVICE | 8092 | ✅ UP |
| 14 | CUSTOMER-PORTAL-SERVICE | 8093 | ✅ UP |
| 15 | PHARMACIST-WORKBENCH-SERVICE | 8094 | ✅ UP |
| 16 | MOBILE-BFF | 8096 | ✅ UP |
| 17 | HEALTH-TOOLS-SERVICE | 8097 | ✅ UP |
| 18 | ECOM-OPS-SERVICE | 8098 | ✅ UP |

### Infrastructure
- **Eureka Dashboard**: http://localhost:8761
- **Config Server**: http://localhost:8888
- **API Gateway**: http://localhost:8080

### Test Credentials
- **Email**: admin@pcms.vn
- **Password**: admin123

### API Test Results
| Endpoint | Status | Response |
|----------|--------|----------|
| POST /api/v1/auth/login | ✅ | Returns JWT token |
| GET /api/v1/users | ✅ | 2 users (admin, pharmacist) |
| GET /api/v1/branches | ✅ | Returns branch list |
| GET /api/v1/medicines | ✅ | Returns 22 medicines |
| GET /api/v1/categories | ✅ | Returns 10 categories |
| GET /api/v1/customers | ✅ | Empty list (0 customers) |

### Database Seeding
- **pcms_user**: 2 users (admin@pcms.vn ADMIN, pharmacist01@pcms.vn PHARMACIST)
- **pcms_category**: 10 categories
- **pcms_supplier**: 5 suppliers
- **pcms_catalog**: 22 medicines
- All other services: Empty (DDL auto-created by JPA)

### Fixes Applied

1. **catalog-service/data.sql**: Removed cross-database SELECT references
2. **supplier-service/data.sql**: Added created_at/updated_at columns to INSERT
3. **order-service/application.yml**: Added `spring.main.allow-bean-definition-overriding=true`
4. **pharmacist-workbench-service/application.yml**: Added `spring.main.allow-bean-definition-overriding=true`
5. **pharmacist-workbench-service/application.yml**: Changed MySQL port from 3307 to use env var
6. **config-server/user-service.yml**: Changed `ddl-auto` from `create` to `update` to preserve seeded users
7. **scripts/seed-admin-user.sql**: Generated proper BCrypt hash for "admin123" and update on duplicate
