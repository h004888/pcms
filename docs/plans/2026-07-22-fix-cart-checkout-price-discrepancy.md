# Fix Cart-Checkout Price Discrepancy — Kế hoạch triển khai

**Mục tiêu:** Loại bỏ chênh lệch tổng tiền giữa giỏ hàng và màn xác nhận thanh toán bằng cách truyền `unitPrice` từ cart sang order-service, đảm bảo giá chốt tại thời điểm addItem được dùng xuyên suốt.

**Cách tiếp cận:** Strategy "Cart là nguồn truth" — giá chỉ lookup từ catalog một lần duy nhất tại thời điểm thêm vào giỏ, sau đó `unitPrice` được lưu trong `CartItem` và truyền sang order-service khi checkout. Order-service dùng `unitPrice` được gửi lên thay vì tự lookup lại catalog. Checkout response trả về order total từ order-service thay vì `cart.getTotal()`.

**Đã tra cứu codebase (codebase-memory-mcp):**
- `get_architecture`: dự án Java Spring Boot microservices, package gồm customer-portal-service, order-service, catalog-service, payment-service.
- `search_graph` + `get_code_snippet`: xác nhận `OrderItemRequest` (`order-service/.../dto/OrderItemRequest.java:11-14`) chỉ có `(UUID medicineId, Integer quantity)`, thiếu `unitPrice`.
- `get_code_snippet`: `CheckoutServiceImpl.confirm` (`customer-portal-service/.../CheckoutServiceImpl.java:116-189`) tại dòng 130-135 chỉ gửi `medicineId` + `quantity` qua `orderClient.createOrder()`, không gửi `unitPrice`. Dòng 153 dùng `cart.getTotal()` thay vì parse `total` từ `orderResponse`.
- `get_code_snippet`: `OrderServiceImpl.create` (`order-service/.../OrderServiceImpl.java:269-275`) gọi `catalogClient.getMedicineById()` để lookup giá tại thời điểm checkout, không dùng giá từ cart.
- `trace_path` (outbound, depth=3): `confirm` gọi `createOrder` (Feign HTTP → order-service), `create` gọi `getMedicineById` (Feign HTTP → catalog-service).
- `get_code_snippet`: xác nhận test conventions — JUnit 5 `@ExtendWith(MockitoExtension.class)`, Mockito `@Mock`/`@InjectMocks`, AssertJ `assertThat()`, test files tại `src/test/java/` mirror `src/main/java/`.

**Framework test & quy ước (đã xác nhận qua codebase-memory-mcp):**
- Framework: JUnit 5 + Mockito + AssertJ (`@ExtendWith(MockitoExtension.class)`)
- Vị trí đặt file test: `src/test/java/` mirror theo `src/main/java/`, quy tắc đặt tên `<ClassName>Test.java`
- Lệnh chạy test (customer-portal-service): `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false`
- Lệnh chạy test (order-service): `mvn test -pl order-service -Dtest="OrderServiceImplTest" -DfailIfNoTests=false`

## Ràng buộc chung (Global Constraints)

- Mọi task viết/sửa hành vi code phải theo chu trình Red → Green → Refactor.
- Không có task nào được đánh dấu hoàn tất nếu thiếu test tự động tương ứng và log xác nhận PASS.
- `unitPrice` trong `OrderItemRequest` để nullable (không `@NotNull`) để backward compatible với các client cũ không gửi field này.
- `OrderServiceImpl.create` phải fallback về lookup catalog khi `unitPrice` là null.
- Giữ nguyên `@NotNull` trên `medicineId` và `quantity` hiện có.

---

### Task 1: Thêm `unitPrice` vào `OrderItemRequest`

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Chỉnh sửa: `pcms/order-service/src/main/java/com/pcms/orderservice/dto/OrderItemRequest.java` (`OrderItemRequest`)
- Test tương ứng: `pcms/order-service/src/test/java/com/pcms/orderservice/dto/OrderItemRequestTest.java` (tạo mới)
- Kiểm chứng bằng: `mvn test -pl order-service -Dtest="OrderItemRequestTest" -DfailIfNoTests=false`

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: `OrderServiceImpl.create` (order-service), `CheckoutServiceImpl.confirm` (customer-portal-service — không trực tiếp, qua Map)
- Gọi tới: không có (là record DTO)
- Regression test cần chạy thêm: `mvn test -pl order-service -Dtest="OrderServiceImplTest" -DfailIfNoTests=false` (tạo instance OrderItemRequest trong setUp)

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: không có
- Cung cấp đầu ra cho: Task 2 (OrderServiceImpl.create dùng `unitPrice`), Task 3 (CheckoutServiceImpl.confirm gửi `unitPrice`)

- [ ] **Bước 1 [RED]: Viết test xác nhận OrderItemRequest hỗ trợ `unitPrice`**
  Vị trí file test: `pcms/order-service/src/test/java/com/pcms/orderservice/dto/OrderItemRequestTest.java`
  Test case 1 `constructor_withUnitPrice_setsField`: tạo `new OrderItemRequest(medicineId, quantity, unitPrice)` → assert `getUnitPrice()` trả về đúng giá trị.
  Test case 2 `constructor_withoutUnitPrice_defaultsToNull`: tạo `new OrderItemRequest(medicineId, quantity)` (constructor 2-arg) → assert `unitPrice()` trả về null.
  Kỳ vọng: chạy `mvn test -pl order-service -Dtest="OrderItemRequestTest" -DfailIfNoTests=false` → FAIL (compile error: `OrderItemRequest(medicineId, quantity, unitPrice)` chưa tồn tại, `unitPrice()` chưa tồn tại).

- [ ] **Bước 2 [GREEN]: Thêm `BigDecimal unitPrice` vào record `OrderItemRequest`**
  Vị trí: `pcms/order-service/src/main/java/com/pcms/orderservice/dto/OrderItemRequest.java`
  Thay `public record OrderItemRequest(@NotNull UUID medicineId, @NotNull @Min(1) Integer quantity)` thành `public record OrderItemRequest(@NotNull UUID medicineId, @NotNull @Min(1) Integer quantity, BigDecimal unitPrice)`.
  Kỳ vọng: chạy `mvn test -pl order-service -Dtest="OrderItemRequestTest" -DfailIfNoTests=false` → PASS.

- [ ] **Bước 3 [REFACTOR]: Không cần refactor — record field tự sinh getter, equals, hashCode.**

- [ ] **Bước 4: Xác nhận hoàn tất task**
  Cách kiểm tra: chạy `mvn test -pl order-service -Dtest="OrderItemRequestTest,OrderServiceImplTest" -DfailIfNoTests=false`
  Kết quả mong đợi: tất cả PASS. Lưu ý: `OrderServiceImplTest.setUp()` tạo `new OrderItemRequest(medicineId, 2)` — constructor 2-arg này tự động gọi constructor canonical với `unitPrice=null`, hợp lệ.

---

### Task 2: `OrderServiceImpl.create` dùng `unitPrice` từ request thay vì lookup catalog

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Chỉnh sửa: `pcms/order-service/src/main/java/com/pcms/orderservice/service/impl/OrderServiceImpl.java` (method `create`, qualified name: `OrderServiceImpl.create`, start_line: 165, end_line: 286)
- Test tương ứng: `pcms/order-service/src/test/java/com/pcms/orderservice/service/impl/OrderServiceImplTest.java` (chỉnh sửa)
- Kiểm chứng bằng: `mvn test -pl order-service -Dtest="OrderServiceImplTest" -DfailIfNoTests=false`

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: `OrderController.create` (HTTP POST /orders)
- Gọi tới: `catalogClient.getMedicineById()`, `isPrescriptionRequired()`, `validatePrescription()`, `generateOrderNumber()`, `CouponService.*`, `orderRepository.save()`
- Regression test cần chạy thêm: `mvn test -pl order-service -Dtest="OrderServiceImplTest" -DfailIfNoTests=false` (toàn bộ test class, đặc biệt `create_withValidRequest_returnsOrderResponse`, `create_withEmptyItems_throwsInvalidOperationException`, `create_withNonExistentCustomer_throwsResourceNotFoundException`)

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: Task 1 (`OrderItemRequest` có field `unitPrice`)
- Cung cấp đầu ra cho: Task 3+4 (CheckoutServiceImpl nhận order response có total chính xác)

- [ ] **Bước 1 [RED]: Viết test `create_withProvidedUnitPrice_usesProvidedPriceNotCatalog`**
  Vị trí: thêm method vào `pcms/order-service/src/test/java/com/pcms/orderservice/service/impl/OrderServiceImplTest.java`
  Test case: tạo `CreateOrderRequest` với `new OrderItemRequest(medicineId, 2, BigDecimal.valueOf(99999))` — unitPrice khác biệt rõ rệt so với catalog mock (45000). Cấu hình mock `catalogClient.getMedicineById()` thành fail nếu được gọi (để phát hiện nếu code vẫn lookup catalog).
  Assert: `verify(orderRepository).save(argThat(order -> order.getItems().get(0).getUnitPrice().compareTo(BigDecimal.valueOf(99999)) == 0))` — tức OrderItem lưu với đúng unitPrice=99999 từ request, không phải 45000 từ catalog.
  Kỳ vọng: chạy `mvn test -pl order-service -Dtest="OrderServiceImplTest#create_withProvidedUnitPrice_usesProvidedPriceNotCatalog" -DfailIfNoTests=false` → FAIL (code hiện tại luôn gọi `catalogClient.getMedicineById()` và dùng giá từ catalog).

- [ ] **Bước 2 [GREEN]: Sửa `OrderServiceImpl.create` để dùng `unitPrice` từ request khi có**
  Vị trí: `pcms/order-service/src/main/java/com/pcms/orderservice/service/impl/OrderServiceImpl.java`, method `create`, trong vòng lặp `for (OrderItemRequest itemReq : request.items())` (bắt đầu khoảng dòng 264)
  Thay đổi: wrap `catalogClient.getMedicineById()` trong điều kiện `if (itemReq.unitPrice() == null)`, nếu `unitPrice` != null thì dùng luôn `itemReq.unitPrice()` làm giá, không gọi catalog. Vẫn cần lookup catalog nếu `unitPrice` null để lấy `name` và `prescriptionRequired`. Cụ thể:
  ```
  BigDecimal price;
  String name;
  boolean prescriptionReq = false;
  if (itemReq.unitPrice() != null) {
      price = itemReq.unitPrice();
      Map<String, Object> medicine = catalogClient.getMedicineById(itemReq.medicineId());
      name = (String) medicine.getOrDefault("name", "Unknown");
      prescriptionReq = isPrescriptionRequired(medicine);
  } else {
      Map<String, Object> medicine = catalogClient.getMedicineById(itemReq.medicineId());
      name = (String) medicine.getOrDefault("name", "Unknown");
      if ("Unknown".equals(name) || medicine.get("price") == null ...) throw ...;
      price = new BigDecimal(medicine.get("price").toString());
      prescriptionReq = isPrescriptionRequired(medicine);
  }
  ```
  Kỳ vọng: chạy `mvn test -pl order-service -Dtest="OrderServiceImplTest#create_withProvidedUnitPrice_usesProvidedPriceNotCatalog" -DfailIfNoTests=false` → PASS.

- [ ] **Bước 3 [GREEN-2]: Viết test `create_withNullUnitPrice_fallsBackToCatalog` để đảm bảo backward compatibility**
  Test case: tạo `CreateOrderRequest` với `new OrderItemRequest(medicineId, 2)` (unitPrice mặc định null). Cấu hình `catalogClient.getMedicineById()` trả về giá 45000 như mock mặc định.
  Assert: `verify(catalogClient).getMedicineById(medicineId)` được gọi, và `order.getItems().get(0).getUnitPrice()` = 45000 (từ catalog).
  Kỳ vọng: test này sẽ PASS ngay vì code đã xử lý fallback catalog khi `unitPrice == null`. Chạy để xác nhận: `mvn test ...#create_withNullUnitPrice_fallsBackToCatalog` → PASS.

- [ ] **Bước 4 [REFACTOR]: Trích xuất logic phân giải giá thành private method**
  Vị trí: `OrderServiceImpl.java`
  Tạo private method `resolveItemPrice(OrderItemRequest itemReq)` trả về `PriceResolution` record (chứa `BigDecimal price`, `String name`, `boolean prescriptionRequired`). Move logic if/else ở Bước 2 vào method này. Gọi `resolveItemPrice(itemReq)` trong vòng lặp `for` thay vì inline.
  Kỳ vọng: chạy `mvn test -pl order-service -Dtest="OrderServiceImplTest" -DfailIfNoTests=false` → toàn bộ test vẫn PASS.

- [ ] **Bước 5: Xác nhận hoàn tất task**
  Cách kiểm tra: chạy `mvn test -pl order-service -Dtest="OrderServiceImplTest" -DfailIfNoTests=false`
  Kết quả mong đợi: tất cả test PASS, bao gồm 3 test hiện có (`create_withValidRequest_returnsOrderResponse`, `create_withEmptyItems_throwsInvalidOperationException`, `create_withNonExistentCustomer_throwsResourceNotFoundException`) và 2 test mới.

---

### Task 3: `CheckoutServiceImpl.confirm` gửi `unitPrice` từ cart sang order-service

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Chỉnh sửa: `pcms/customer-portal-service/src/main/java/com/pcms/customerportal/service/impl/CheckoutServiceImpl.java` (method `confirm`, start_line: 116, end_line: 189)
- Test tương ứng: `pcms/customer-portal-service/src/test/java/com/pcms/customerportal/service/impl/CheckoutServiceImplTest.java` (chỉnh sửa)
- Kiểm chứng bằng: `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false`

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: `CartController.confirm` (HTTP POST /checkout/confirm)
- Gọi tới: `cartFactory.getOrCreateCart()`, `cartItemRepository.findByCartId()`, `orderClient.createOrder()`, `paymentServiceClient.createPayment()`, `cartRepository.save()`
- Regression test cần chạy thêm: `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false` (toàn bộ test class — `confirm_whenNoActiveCart_autoCreatesCart`, `confirm_withCodPayment_setsCartCheckedOut`, `confirm_withEmptyCart_throwsInvalidOperationException`, `preview_delegatesToCartFactory`)

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: Task 1+2 (OrderItemRequest hỗ trợ unitPrice, OrderServiceImpl dùng unitPrice)
- Cung cấp đầu ra cho: Task 4 (CheckoutServiceImpl trả về order total từ response)

- [ ] **Bước 1 [RED]: Viết test `confirm_sendsUnitPriceInOrderItems`**
  Vị trí: thêm method vào `pcms/customer-portal-service/src/test/java/com/pcms/customerportal/service/impl/CheckoutServiceImplTest.java`
  Test case: setup CartItem với `unitPrice = BigDecimal.valueOf(25000)`. Mock `orderClient.createOrder()` dùng `ArgumentCaptor<Map<String, Object>>` để capture request gửi đi.
  Assert: items trong captured request có chứa `unitPrice = 25000`, khớp với `CartItem.getUnitPrice()`.
  Kỳ vọng: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest#confirm_sendsUnitPriceInOrderItems" -DfailIfNoTests=false` → FAIL (code hiện tại không gửi unitPrice).

- [ ] **Bước 2 [GREEN]: Sửa `confirm` để gửi `unitPrice` trong order items**
  Vị trí: `pcms/customer-portal-service/src/main/java/com/pcms/customerportal/service/impl/CheckoutServiceImpl.java`, method `confirm`, dòng 130-135
  Sửa `items.stream().map(...)` để thêm `"unitPrice", item.getUnitPrice()`:
  ```
  List<Map<String, Object>> orderItems = items.stream()
      .map(item -> {
          Map<String, Object> m = new java.util.HashMap<>();
          m.put("medicineId", item.getMedicineId().toString());
          m.put("quantity", item.getQty());
          m.put("unitPrice", item.getUnitPrice());
          return m;
      })
      .toList();
  ```
  Kỳ vọng: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest#confirm_sendsUnitPriceInOrderItems" -DfailIfNoTests=false` → PASS.

- [ ] **Bước 3 [REFACTOR]: Trích xuất `buildOrderItems(List<CartItem>)` thành private method**
  Vị trí: `CheckoutServiceImpl.java`
  Tạo `private List<Map<String, Object>> buildOrderItems(List<CartItem> items)` chứa logic map ở Bước 2. Gọi từ `confirm`.
  Kỳ vọng: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false` → toàn bộ test PASS.

- [ ] **Bước 4: Xác nhận hoàn tất task**
  Cách kiểm tra: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false`
  Kết quả mong đợi: tất cả 4 test hiện có + 1 test mới đều PASS.

---

### Task 4: `CheckoutServiceImpl.confirm` trả về order total từ order-service response

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Chỉnh sửa: `pcms/customer-portal-service/src/main/java/com/pcms/customerportal/service/impl/CheckoutServiceImpl.java` (method `confirm`, dòng 153: `BigDecimal total = cart.getTotal()`)
- Test tương ứng: `pcms/customer-portal-service/src/test/java/com/pcms/customerportal/service/impl/CheckoutServiceImplTest.java` (chỉnh sửa)
- Kiểm chứng bằng: `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false`

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: `CartController.confirm` (HTTP POST /checkout/confirm)
- Gọi tới: `cart.getTotal()` (sẽ không còn dùng cho response), `orderClient.createOrder()`
- Regression test cần chạy thêm: `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false` (toàn bộ test class, đặc biệt `confirm_whenNoActiveCart_autoCreatesCart` có assert `result.total()`)

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: Task 3 (confirm đã gửi unitPrice sang order-service, order-service trả về total chính xác)
- Cung cấp đầu ra cho: frontend hiển thị tổng tiền chính xác

- [ ] **Bước 1 [RED]: Viết test `confirm_returnsOrderTotalFromResponse`**
  Vị trí: thêm method vào `pcms/customer-portal-service/src/test/java/com/pcms/customerportal/service/impl/CheckoutServiceImplTest.java`
  Test case: set `cart.setTotal(BigDecimal.valueOf(50000))` (total trong cart cũ). Mock `orderClient.createOrder()` trả về `Map.of("id", orderId.toString(), "orderNumber", "ORD-001", "status", "PENDING", "total", BigDecimal.valueOf(52000))` — order total khác cart total (52000 vs 50000).
  Assert: `result.total()` = `BigDecimal.valueOf(52000)` (lấy từ orderResponse), KHÔNG phải 50000 (cart total cũ).
  Kỳ vọng: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest#confirm_returnsOrderTotalFromResponse" -DfailIfNoTests=false` → FAIL (code hiện tại dùng `cart.getTotal()` = 50000, không parse từ orderResponse).

- [ ] **Bước 2 [GREEN]: Parse `total` từ `orderResponse` thay vì `cart.getTotal()`**
  Vị trí: `pcms/customer-portal-service/src/main/java/com/pcms/customerportal/service/impl/CheckoutServiceImpl.java`, method `confirm`, dòng 153
  Thay `BigDecimal total = cart.getTotal()` bằng:
  ```
  Object totalObj = orderResponse.get("total");
  BigDecimal total;
  if (totalObj instanceof Number n) {
      total = BigDecimal.valueOf(n.doubleValue());
  } else {
      total = cart.getTotal(); // fallback cho backward compatibility
  }
  ```
  Kỳ vọng: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest#confirm_returnsOrderTotalFromResponse" -DfailIfNoTests=false` → PASS.

- [ ] **Bước 3 [GREEN-2]: Cập nhật test hiện có `confirm_whenNoActiveCart_autoCreatesCart`**
  Test hiện tại không có `"total"` trong orderResponse mock. Sau khi sửa, nó sẽ fallback về `cart.getTotal()`. Thêm `"total", cart.getTotal()` vào orderResponse mock để test dùng total từ response thay vì fallback.
  Kỳ vọng: `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest#confirm_whenNoActiveCart_autoCreatesCart" -DfailIfNoTests=false` → PASS.

- [ ] **Bước 4 [REFACTOR]: Trích xuất `parseTotal(Map, BigDecimal fallback)` thành private method**
  Vị trí: `CheckoutServiceImpl.java`
  Tạo `private BigDecimal parseTotal(Map<String, Object> orderResponse, BigDecimal fallback)` chứa logic parse ở Bước 2.
  Kỳ vọng: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false` → toàn bộ test PASS.

- [ ] **Bước 5: Xác nhận hoàn tất task**
  Cách kiểm tra: chạy `mvn test -pl customer-portal-service -Dtest="CheckoutServiceImplTest" -DfailIfNoTests=false`
  Kết quả mong đợi: tất cả 4 test hiện có + 2 test mới đều PASS.

---

### Task 5: Integration verification — chạy toàn bộ test suite

**Đối tượng liên quan (đã xác nhận qua codebase-memory-mcp):**
- Tạo mới: không có
- Test tương ứng: toàn bộ test suite của cả 2 service
- Kiểm chứng bằng: `mvn test -pl customer-portal-service -DfailIfNoTests=false` và `mvn test -pl order-service -DfailIfNoTests=false`

**Ảnh hưởng liên quan (từ trace_path / detect_changes):**
- Được gọi bởi: không áp dụng (task verification)
- Gọi tới: không áp dụng
- Regression test cần chạy thêm: toàn bộ

**Giao diện/Kết nối với các task khác:**
- Nhận đầu vào từ: Task 1+2+3+4
- Cung cấp đầu ra cho: không có

- [ ] **Bước 1: Chạy toàn bộ test order-service**
  Kỳ vọng: `mvn test -pl order-service -DfailIfNoTests=false` → BUILD SUCCESS, tất cả test PASS.

- [ ] **Bước 2: Chạy toàn bộ test customer-portal-service**
  Kỳ vọng: `mvn test -pl customer-portal-service -DfailIfNoTests=false` → BUILD SUCCESS, tất cả test PASS.

- [ ] **Bước 3: Chạy full build pcms (nếu Maven reactor hỗ trợ)**
  Kỳ vọng: `mvn test -f pcms/pom.xml -DfailIfNoTests=false` → BUILD SUCCESS (có thể skip nếu pcms không có parent pom).

- [ ] **Bước 4: Xác nhận hoàn tất**
  Toàn bộ test suite của cả 2 service đều PASS. Không regression.
