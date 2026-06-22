# PCMS Backend — Phase 1 Changes

**Last updated:** 2026-06-22

## Tóm tắt

Phase 1 chỉ thay đổi **3 files** trong backend (chưa cần Maven/compile):

1. `user-service/.../dto/response/LoginResponse.java` — nested `user` object
2. `user-service/.../service/impl/UserServiceImpl.java` — 2 chỗ gọi constructor
3. `scripts/seed-admin-user.sql` — regenerate BCrypt hash

## Chi tiết thay đổi

### 1. LoginResponse shape (P1.2)

**Trước:**
```java
public record LoginResponse(
    String accessToken, String refreshToken, String tokenType, long expiresIn,
    UUID userId, String email, String fullName, Role role, UUID branchId
) {}
```

**Sau:**
```java
public record LoginResponse(
    String accessToken, String refreshToken, String tokenType, long expiresIn,
    UserInfo user
) {
    public record UserInfo(UUID id, String email, String fullName, Role role, UUID branchId, UserStatus status) {
        public static UserInfo from(User u) { ... }
    }
}
```

**Lý do:** Frontend `LoginResponse` type expect nested `user: AuthUser`. Flat response làm auth context vỡ.

### 2. UserServiceImpl constructor calls (P1.2)

2 chỗ (login + refresh) chuyển từ flat 9-arg sang 5-arg với `UserInfo.from(user)`.

Lưu ý: Plan nói sửa `UserService.java` (interface) nhưng thực tế chỉ có `UserServiceImpl.java` chứa constructor calls. Subagent đã sửa đúng file.

### 3. BCrypt seed hash (P1.1)

**Hash cũ** (placeholder/fake): `$2a$10$YxNztL.9cWZ2JniaTdY8TOYTZVhaY96FfQj3S4YWSmymuDv/PiCMS`

**Hash mới** (verified round-trip với bcryptjs):
- admin@pcms.vn / admin123 → `$2a$10$6yDocMLWwXDCp9sfBjmsiey.JMDeR.k.mVzA4EU.70V/Ow11vPML2`
- pharmacist01@pcms.vn / pharma123 → `$2a$10$bSTzFttm6asD/O/O/UqwFuofbDtnuRjO0s7Ku4E4MKA0W0PhP.j5m`

Hash mới tương thích với Spring `BCryptPasswordEncoder` (cost factor 10).

## Còn lại (chưa làm — chờ Phase tiếp theo)

- **Maven chưa cài** → chưa compile được các services
- **MySQL chưa setup** → chưa apply seed
- **Config server / Eureka** → chưa start được
- **18 services Java** → chưa build

## Tích hợp Frontend ↔ Backend (khi có môi trường)

Frontend hiện chạy với **mock BFF layer** (Next.js route handlers). Khi backend sẵn sàng:

1. Set `NEXT_PUBLIC_USE_MOCK_API=false` trong `.env.local`
2. Set `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1`
3. Login `admin@pcms.vn` / `admin123` sẽ gọi thẳng `user-service` qua API Gateway
4. Mock layer sẽ tự bỏ qua

## Verification khi backend chạy

```bash
# 1. Verify BCrypt hash
mysql -u pcms_user -ppcms_pass pcms_user -e \
  "SELECT email, password_hash FROM users WHERE email IN ('admin@pcms.vn','pharmacist01@pcms.vn');"

# 2. Test login qua gateway
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@pcms.vn","password":"admin123"}' | jq .

# Expected:
# {
#   "accessToken": "eyJ...",
#   "refreshToken": "eyJ...",
#   "tokenType": "Bearer",
#   "expiresIn": 3600,
#   "user": {
#     "id": "uuid...",
#     "email": "admin@pcms.vn",
#     "fullName": "System Administrator",
#     "role": "ADMIN",
#     "branchId": null,
#     "status": "ACTIVE"
#   }
# }
```

## Commits

```
d3f803e fix(auth): LoginResponse nested user object to match FE contract (P1.2)
265257d fix(auth): regenerate BCrypt hash for seed users (P1.1)
```

## Plan reference

`C:\Users\ADMIN\Downloads\temp_v12\.hermes\plans\2026-06-22_113000-pcms-full-integration-5phase.md`
