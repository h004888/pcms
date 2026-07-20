# Khôi phục khả năng khởi động `report-service` — Kế hoạch triển khai

**Mục tiêu:** Làm cho `report-service` biên dịch, vượt qua toàn bộ test tự động và khởi tạo được Spring ApplicationContext bằng cách chuẩn hóa code về hợp đồng lịch báo cáo và realtime hiện hành.

**Cách tiếp cận:** Giữ luồng hiện hành đang được `ReportController` sử dụng: `ReportScheduleService`/`ReportScheduleResponse` cho lịch báo cáo và `realtimeStats(UUID branchId)` cho thống kê realtime. Xóa các DTO, method và dependency injection cũ còn sót sau merge; đồng thời chuyển toàn bộ test sang chữ ký `OrderClient.getOrders(...)` bảy tham số. Một context-startup test dùng H2 ở test scope khóa lại yêu cầu “service có thể khởi tạo” mà không phụ thuộc MySQL, Config Server hoặc Eureka bên ngoài.

**Đã tra cứu codebase (codebase-memory-mcp):**
- `index_status`: project `C-Users-ADMIN-Downloads-temp_v12` đã index và ở trạng thái `ready` với 10.688 nodes, 29.924 edges.
- `get_architecture`: workspace chứa backend Java/Maven nhiều module; `report-service` là Spring Boot module, có controller, service, JPA repository, Feign client và test Java.
- `search_graph`: xác nhận hai hợp đồng lịch cùng tồn tại: hợp đồng hiện hành `CreateReportScheduleRequest`/`ReportScheduleResponse` và hợp đồng cũ `CreateScheduleRequest`/`ScheduleResponse`.
- `get_code_snippet`: xác nhận `ReportController` gọi `ReportScheduleService` cho `/reports/schedule`, `/reports/schedules`, `/reports/schedules/{id}` và gọi `ReportService.realtimeStats(UUID)` cho `/reports/realtime/stats`.
- `get_code_snippet`: xác nhận `ReportServiceImpl` đang chứa đồng thời `realtimeStats(UUID)` hiện hành và `realtimeStats()` cũ; overload cũ gọi `OrderClient.getOrders(null, 0, 1000)` nên không còn khớp chữ ký bảy tham số.
- `get_code_snippet`: xác nhận `ReportScheduleServiceImpl` đã ánh xạ mô hình hiện hành `type`, `format`, `branchId`, `cronExpression`, `recipientEmail`, `createdBy`, `nextRunAt`, `lastStatus` sang `ReportScheduleResponse`.
- `trace_path`: `ReportServiceImpl.createSchedule` và `ReportServiceImpl.listSchedules` cũ không có caller inbound; `ReportController.realtimeStats` đi tới overload có `UUID branchId`.
- `trace_path`: `OrderClient.getOrders` có các caller production `revenue`, `staff`, `recentOrders`, `realtimeStats` và các caller test trong `ReportServiceImplTest`; vì vậy mọi mock ba tham số trong test phải được đổi sang hợp đồng bảy tham số.
- `get_code_snippet`: `ReportScheduleResponse.from` có caller hiện hành từ `ReportScheduleServiceImpl.create` và `ReportScheduleServiceImpl.cancel`; luồng này phải được giữ nguyên.

**Framework test & quy ước (đã xác nhận qua codebase-memory-mcp):**
- Framework: JUnit 5 qua `spring-boot-starter-test`, Mockito qua `@ExtendWith(MockitoExtension.class)`, AssertJ qua `assertThat`/`assertThatThrownBy`.
- Vị trí đặt file test: `report-service/src/test/java/` mirror package của `report-service/src/main/java/`; test class kết thúc bằng `Test`.
- Lệnh test mục tiêu: `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am '-Dtest=ReportServiceContractTest,ReportServiceImplTest,ReportScheduleServiceImplTest,ReportServiceApplicationContextTest' -Dsurefire.failIfNoSpecifiedTests=false test` chạy từ thư mục `pcms`.
- Lệnh regression đầy đủ: `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am test` chạy từ thư mục `pcms`.

## Ràng buộc chung (Global Constraints)

- Mọi task viết/sửa hành vi code phải theo chu trình Red → Green → Refactor.
- Không có task nào được đánh dấu hoàn tất nếu thiếu test tự động tương ứng và log xác nhận PASS.
- Không thay đổi route HTTP, schema bảng `report_schedules`, cấu hình MySQL, Config Server, Eureka hoặc code của module khác.
- Không thay đổi hành vi của `ReportScheduleServiceImpl`, `ReportScheduleResponse`, `realtimeStats(UUID)`, `revenue`, `staff` và `recentOrders` ngoài việc cập nhật test về đúng hợp đồng hiện hành.
- Không giữ adapter tạm cho `CreateScheduleRequest`, `ScheduleResponse`, `createSchedule`, `listSchedules` hoặc `realtimeStats()` không tham số; mục tiêu là loại bỏ nguyên nhân trôi hợp đồng, không che lỗi compile.
- Không xóa hoặc sửa các thay đổi local `.factorypath` hiện có.
- Mọi lệnh Maven dùng Maven đi kèm workspace tại `..\tools\apache-maven-3.9.16\bin\mvn.cmd`; không yêu cầu cài Maven toàn hệ thống.

## Phạm vi file đã xác nhận

**Tạo mới:**
- `report-service/src/test/java/com/pcms/reportservice/service/ReportServiceContractTest.java`
- `report-service/src/test/java/com/pcms/reportservice/service/impl/ReportScheduleServiceImplTest.java`
- `report-service/src/test/java/com/pcms/reportservice/ReportServiceApplicationContextTest.java`

**Chỉnh sửa:**
- `report-service/pom.xml`
- `report-service/src/main/java/com/pcms/reportservice/service/ReportService.java`
- `report-service/src/main/java/com/pcms/reportservice/service/impl/ReportServiceImpl.java`
- `report-service/src/test/java/com/pcms/reportservice/service/impl/ReportServiceImplTest.java`

**Xóa:**
- `report-service/src/main/java/com/pcms/reportservice/dto/CreateScheduleRequest.java`
- `report-service/src/main/java/com/pcms/reportservice/dto/ScheduleResponse.java`

### Task 1: Chuẩn hóa hợp đồng `report-service` và khóa bằng test tự động

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Tạo mới: `report-service/src/test/java/com/pcms/reportservice/service/ReportServiceContractTest.java`.
- Tạo mới: `report-service/src/test/java/com/pcms/reportservice/service/impl/ReportScheduleServiceImplTest.java`.
- Tạo mới: `report-service/src/test/java/com/pcms/reportservice/ReportServiceApplicationContextTest.java`.
- Chỉnh sửa: `report-service/pom.xml`, node `C-Users-ADMIN-Downloads-temp_v12.pcms.report-service.pom.dependencies`.
- Chỉnh sửa: `report-service/src/main/java/com/pcms/reportservice/service/ReportService.java`, qualified name `C-Users-ADMIN-Downloads-temp_v12.pcms.report-service.src.main.java.com.pcms.reportservice.service.ReportService.ReportService`.
- Chỉnh sửa: `report-service/src/main/java/com/pcms/reportservice/service/impl/ReportServiceImpl.java`, qualified name `C-Users-ADMIN-Downloads-temp_v12.pcms.report-service.src.main.java.com.pcms.reportservice.service.impl.ReportServiceImpl.ReportServiceImpl`.
- Chỉnh sửa test: `report-service/src/test/java/com/pcms/reportservice/service/impl/ReportServiceImplTest.java`, qualified name `C-Users-ADMIN-Downloads-temp_v12.pcms.report-service.src.test.java.com.pcms.reportservice.service.impl.ReportServiceImplTest.ReportServiceImplTest`.
- Xóa: `report-service/src/main/java/com/pcms/reportservice/dto/CreateScheduleRequest.java` và `report-service/src/main/java/com/pcms/reportservice/dto/ScheduleResponse.java`.
- Kiểm chứng bằng: `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am '-Dtest=ReportServiceContractTest,ReportServiceImplTest,ReportScheduleServiceImplTest,ReportServiceApplicationContextTest' -Dsurefire.failIfNoSpecifiedTests=false test`.

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: `ReportServiceImpl.createSchedule` và `ReportServiceImpl.listSchedules` cũ không có caller inbound; `ScheduleResponse.from` cũng không có caller inbound.
- Luồng phải giữ: `ReportController.realtimeStats` gọi `ReportService.realtimeStats(UUID)`; `ReportScheduleServiceImpl.create`/`cancel` gọi `ReportScheduleResponse.from`.
- Gọi tới: `realtimeStats(UUID)` gọi `revenue`, `inventory`, `recentOrders`; các hàm này gọi `OrderClient.getOrders` và `InventoryClient`.
- Regression test cần chạy thêm do có inbound calls: toàn bộ `ReportServiceImplTest`, test mới cho `ReportScheduleServiceImpl`, test hợp đồng interface và context-startup test.
- `detect_changes` trên index workspace không đọc được nested Git root `pcms`; blast radius được xác minh bằng `trace_path` và `get_code_snippet`, đồng thời bước cuối chạy toàn bộ test của module để bù cho giới hạn này.

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: kết quả chẩn đoán compile và graph đã ghi ở phần đầu kế hoạch.
- Cung cấp đầu ra cho: Task 2 nhận một module đã compile và có test chứng minh hợp đồng chuẩn duy nhất.

- [ ] **Bước 1 [RED]: Viết test khóa hợp đồng chuẩn và chuyển test cũ sang chữ ký hiện hành**
  Vị trí file test: `report-service/src/test/java/com/pcms/reportservice/service/ReportServiceContractTest.java`.
  Test case `reportServiceExposesOnlyBranchAwareRealtimeStats`: dùng reflection lấy các method tên `realtimeStats` trên `ReportService`, xác nhận chỉ có một method và parameter duy nhất là `UUID.class`.
  Test case `reportServiceDoesNotExposeLegacySchedulingOperations`: xác nhận danh sách method của `ReportService` không chứa `createSchedule` và `listSchedules`.
  Test case `legacyScheduleDtosAreAbsent`: gọi `Class.forName` bằng chuỗi qualified class của `CreateScheduleRequest` và `ScheduleResponse`, xác nhận cả hai ném `ClassNotFoundException`.
  Vị trí file test: `report-service/src/test/java/com/pcms/reportservice/service/impl/ReportScheduleServiceImplTest.java`.
  Test case `createMapsCanonicalRequestToCurrentEntityAndResponse`: tạo request `("revenue", "pdf", branchId, "0 0 9 * * *", "ops@pcms.vn", createdBy)`, mock `reportScheduleRepository.save` trả lại entity đầu vào, rồi xác nhận entity/response chứa đúng `type=revenue`, `format=pdf`, `branchId`, cron, email, `createdBy`, `active=true`, `lastStatus=PENDING` và `nextRunAt` khác null.
  Test case `listMapsCurrentEntitiesToReportScheduleResponse`: mock repository trả một entity có các field hiện hành, gọi `list()`, xác nhận kết quả là `ReportScheduleResponse` chứa đúng các field đó và không dùng DTO cũ.
  Vị trí file test sửa: `report-service/src/test/java/com/pcms/reportservice/service/impl/ReportServiceImplTest.java`.
  Thay mọi mock `getOrders` ba tham số trong các test revenue/realtime bằng bảy tham số tương ứng; chuyển `realtimeStats_returnsTodaySummary` và `realtimeStats_withLowStockItems_reflectsCount` sang `realtimeStats(null)` và assert payload chuẩn gồm `todayRevenue`, `todayOrders`, `lowStockCount`, `totalBatches`, `recentOrders`.
  Xóa mock `ReportScheduleRepository` khỏi test và khởi tạo `ReportServiceImpl` bằng bốn dependency còn lại để thể hiện constructor chuẩn mong muốn.
  Vị trí file test: `report-service/src/test/java/com/pcms/reportservice/ReportServiceApplicationContextTest.java`.
  Thêm `@SpringBootTest` với `contextLoads()`, tắt Config Client/Eureka/Scheduling bằng test properties và cấu hình H2 MySQL mode; thêm dependency `com.h2database:h2` scope `test` trong `report-service/pom.xml` để JPA context khởi tạo độc lập.
  Lifecycle kiểm tra: `mvn test`; trên máy Windows này dùng lệnh Maven workspace đầy đủ ở dòng kỳ vọng bên dưới.
  Kỳ vọng: chạy `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am '-Dtest=ReportServiceContractTest,ReportServiceImplTest,ReportScheduleServiceImplTest,ReportServiceApplicationContextTest' -Dsurefire.failIfNoSpecifiedTests=false test` → **FAIL** tại compile vì production code vẫn chứa bốn getter cũ trong `ScheduleResponse`, lời gọi `getOrders` ba tham số, constructor năm dependency và các method legacy; không chấp nhận lỗi cú pháp hoặc import trong test mới.

- [ ] **Bước 2 [GREEN]: Loại bỏ hợp đồng legacy và giữ duy nhất luồng hiện hành để test ở Bước 1 PASS**
  Tại `ReportService.java`, xóa import `CreateScheduleRequest`/`ScheduleResponse` và xóa ba khai báo `realtimeStats()`, `createSchedule(CreateScheduleRequest)`, `listSchedules()`; giữ nguyên `realtimeStats(UUID)` và `recentOrders(UUID, int)`.
  Tại `ReportServiceImpl.java`, xóa overload `realtimeStats()` không tham số; xóa `createSchedule`/`listSchedules`; xóa import, field và constructor parameter `ReportScheduleRepository`; xóa import `CreateScheduleRequest`, `ScheduleResponse`, `ReportSchedule` nếu không còn được sử dụng.
  Xóa hai file `CreateScheduleRequest.java` và `ScheduleResponse.java`; không sửa `CreateReportScheduleRequest.java`, `ReportScheduleResponse.java`, `ReportScheduleService.java` hoặc `ReportScheduleServiceImpl.java`.
  Giữ nguyên implementation `realtimeStats(UUID)`, bao gồm chuỗi gọi `revenue(today, today, branchId, "day")`, `inventory(branchId)` và `recentOrders(branchId, 5)`.
  Lifecycle kiểm tra: `mvn test`; trên máy Windows này dùng lệnh Maven workspace đầy đủ ở dòng kỳ vọng bên dưới.
  Kỳ vọng: chạy `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am '-Dtest=ReportServiceContractTest,ReportServiceImplTest,ReportScheduleServiceImplTest,ReportServiceApplicationContextTest' -Dsurefire.failIfNoSpecifiedTests=false test` → **PASS**; compiler không còn năm lỗi đã tái hiện, contract tests và context-startup test đều PASS.

- [ ] **Bước 3 [REFACTOR]: Làm sạch import, tên test và JavaDoc sau khi hợp đồng đã thống nhất**
  Xóa import không dùng trong `ReportService.java`, `ReportServiceImpl.java`, `ReportServiceImplTest.java`; đổi comment test từ `realtimeStats()` sang `realtimeStats(UUID)`; giữ nguyên payload và route hiện hành.
  Không chuyển logic từ `ReportScheduleServiceImpl` sang `ReportServiceImpl` và không tạo abstraction mới.
  Lifecycle kiểm tra: `mvn test`; trên máy Windows này dùng lệnh Maven workspace đầy đủ ở dòng kỳ vọng bên dưới.
  Kỳ vọng: chạy `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am test` → vẫn **PASS**, hành vi không đổi sau refactor.

- [ ] **Bước 4: Xác nhận hoàn tất task**
  Cách kiểm tra: chạy toàn bộ test liên quan bằng `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am test`.
  Kết quả mong đợi: reactor `pharmacy-chain-management`, `pcms-common`, `report-service` đều `SUCCESS`; toàn bộ JUnit test PASS; `ReportServiceApplicationContextTest.contextLoads` PASS; không có compilation error từ `ScheduleResponse` hoặc `OrderClient.getOrders`.

### Task 2: Kiểm chứng artifact đóng gói của `report-service`

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Tạo mới: không có.
- Chỉnh sửa: không có; đây là task kiểm chứng sau triển khai.
- Test tương ứng: toàn bộ test dưới `report-service/src/test/java/`, bao gồm context-startup test từ Task 1.
- Kiểm chứng bằng: `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am verify`.

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: không áp dụng; task không thay đổi symbol.
- Gọi tới: Maven reactor build parent, `pcms-common`, rồi `report-service`.
- Regression test cần chạy thêm do có inbound calls: toàn bộ suite `report-service` đã được Maven `verify` thực thi.

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: Task 1 cung cấp source đã chuẩn hóa và test PASS.
- Cung cấp đầu ra cho: artifact Spring Boot `report-service/target/report-service-1.0.0-SNAPSHOT.jar` đã build thành công và được bảo vệ bởi context-startup test.

- [ ] **Bước 1: Chạy Maven verify cho reactor cần thiết**
  Cách kiểm tra: chạy `& '..\tools\apache-maven-3.9.16\bin\mvn.cmd' -pl report-service -am verify` từ thư mục `pcms`.
  Kết quả mong đợi: lệnh trả exit code `0`; các module parent, `pcms-common`, `report-service` báo `SUCCESS`; tất cả test PASS.

- [ ] **Bước 2: Xác nhận artifact được tạo**
  Cách kiểm tra: chạy `Test-Path -LiteralPath 'report-service\target\report-service-1.0.0-SNAPSHOT.jar'` từ thư mục `pcms`.
  Kết quả mong đợi: PowerShell trả `True`; artifact được tạo từ đúng source đã vượt qua `verify`.

- [ ] **Bước 3: Xác nhận phạm vi Git**
  Cách kiểm tra: chạy `git status --short -- report-service docs/plans` và `git diff --check -- report-service docs/plans`.
  Kết quả mong đợi: chỉ xuất hiện các file đã liệt kê trong kế hoạch và file kế hoạch này; `git diff --check` trả exit code `0`; các thay đổi `.factorypath` có sẵn không bị sửa thêm.

## Tiêu chí hoàn tất toàn kế hoạch

- Hợp đồng public `ReportService` chỉ còn `realtimeStats(UUID branchId)` cho realtime và không còn method lịch báo cáo legacy.
- Luồng lịch báo cáo duy nhất là `ReportController` → `ReportScheduleService` → `ReportScheduleServiceImpl` → `ReportScheduleResponse`.
- Không còn class `CreateScheduleRequest` và `ScheduleResponse` trong production classpath.
- Mọi lời gọi/mock `OrderClient.getOrders` trong `report-service` dùng đủ bảy tham số.
- `mvn -pl report-service -am test` và `mvn -pl report-service -am verify` đều trả exit code `0`.
- Context-startup test chứng minh Spring ApplicationContext khởi tạo được trong môi trường test độc lập.
- Artifact `report-service-1.0.0-SNAPSHOT.jar` tồn tại sau `verify`.
