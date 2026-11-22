# Backend Integration Gaps — Danh sách API cần bổ sung

Tài liệu này liệt kê các chức năng BE chưa hoàn thiện, gây ra gaps giữa BE và FE.
FE đã xử lý tạm thời (workaround) nhưng cần BE bổ sung để hoạt động đầy đủ.

---

## 1. Vai trò (Roles) trong `/api/users/me` — ✓ Đã hoàn thành

### Vấn đề (Đã giải quyết)
`GET /api/users/me` trả về `UserResponse` **không có field `roles`**. FE không biết user có vai trò ADMIN hay không, phải đoán bằng cách gọi thử `GET /api/admin/dashboard/stats` và kiểm tra xem có trả về 403 hay không.

### Đã sửa
Field `roles` đã được thêm vào `UserResponse`:

```java
public record UserResponse(
    UUID id,
    String email,
    String fullName,
    String phone,
    UserStatus status,
    List<String> roles,          // ← Đã thêm: ["ADMIN", "USER"]
    LocalDateTime createdAt
) {}
```

Method `UserService.getCurrentUser()` giờ truy vấn bảng `user_role` để lấy danh sách roles của user.

### Kết luận
- **FE không cần đoán admin nữa** — dùng trực tiếp field `roles` từ `/api/users/me`.
- **Workaround đã loại bỏ** — không cần gọi thử `/api/admin/dashboard/stats`.

---

## 2. Cập nhật sản phẩm — ✓ Đã thêm trường variants

### Vấn đề (Đã giải quyết)
`PUT /api/admin/products/{productId}` dùng `UpdateProductRequest` chỉ chứa thông tin chung (name, description, categoryId, status, productType, isCustomBoxEligible).

**Không thể** đổi SKU, giá, tồn kho, hay ảnh của sản phẩm đã tạo qua endpoint này.

### Đã sửa
Trường `variants` đã được thêm vào `UpdateProductRequest` (Lựa chọn A):

```java
public record UpdateProductRequest(
    UUID categoryId,
    String name,
    String description,
    ProductType productType,
    ProductStatus status,
    Boolean isCustomBoxEligible,
    List<CreateVariantRequest> variants  // ← Đã thêm: cập nhật variants
) {}
```

`PUT /api/admin/products/{id}` giờ nhận được cập nhật variant (SKU, giá, tồn kho, ảnh, kích thước, chính sách).

### Kết luận
- **FE có thể cập nhật variant** qua PUT endpoint — không cần endpoint riêng.
- **Workaround đã loại bỏ** — không còn giới hạn chỉ cập nhật thông tin chung.

---

## 3. Biến thể gắn với giá trị tuỳ chọn — ✓ Đã hoàn thành

### Vấn đề (Đã giải quyết)
BE có bảng `product_variant_option_value` để link variant với option value (màu, kích cỡ), nhưng **không có API** để FE:
1. Biết variant nào tương ứng với tổ hợp option value nào (ví dụ: màu Đỏ + kích cỡ M = variant ID xyz)
2. Chọn biến thể theo màu/kích cỡ trên trang chi tiết sản phẩm

### Đã sửa

**A.** Trường `optionValues` đã được thêm vào `VariantResponse`:
```java
public record VariantResponse(
    UUID id,
    String sku,
    BigDecimal price,
    Integer stockQuantity,
    String status,
    List<ImageResponse> images,
    List<ProductVariantOptionValueResponse> optionValues  // ← Đã thêm
) {}
```

**B.** Endpoint resolve variant đã được thêm:
```
GET /api/products/{productId}/resolve-variant?optionValueIds=id1,id2,id3
```

Response trả về variant khớp với các option value IDs đã chọn, bao gồm thông tin màu/kích cỡ.

### Kết luận
- **FE có thể chọn variant theo màu/kích cỡ** — gọi resolve-variant endpoint.
- **Mỗi variant trả về optionValues** — FE biết variant nào là màu gì, kích cỡ gì.
- **Workaround đã loại bỏ** — trang chi tiết sản phẩm giờ chọn được biến thể theo tùy chọn.

---

## 4. Bundle options — BE chưa có API tạo/quản lý

### Vấn đề
`BundleOptionController` chỉ có các GET endpoints để đọc bundle options, components, policy, gift-box-detail. **Không có POST/PUT** để tạo hoặc cập nhật bundle option groups và options cho một variant.

### Hiện tại
File: `src/main/java/elin/le/giftly/product/controller/BundleOptionController.java`

Chỉ có:
- `GET /api/bundles/variants/{variantId}/option-groups`
- `GET /api/bundles/variants/{variantId}/components`
- `GET /api/bundles/variants/{variantId}/policy`
- `GET /api/bundles/variants/{variantId}/gift-box-detail`

### Yêu cầu BE
Thêm các admin endpoints để quản lý bundle:

```
POST   /api/admin/bundles/variants/{variantId}/option-groups
POST   /api/admin/bundles/option-groups/{groupId}/options
PUT    /api/admin/bundles/option-groups/{groupId}
PUT    /api/admin/bundles/options/{optionId}
DELETE /api/admin/bundles/options/{optionId}
DELETE /api/admin/bundles/option-groups/{groupId}

POST   /api/admin/bundles/variants/{variantId}/components
PUT    /api/admin/bundles/components/{componentId}
DELETE /api/admin/bundles/components/{componentId}
```

---

## 5. Ảnh chung của sản phẩm không có chỗ riêng ở BE

### Vấn đề
BE không có field `images` ở level Product — ảnh chỉ thuộc về ProductVariant. FE phải gắn ảnh chung vào variant đầu tiên như một workaround.

### Hiện tại
- `Product` entity không có relationship đến `ProductImage`.
- `ProductVariant` có `List<ProductImage> images`.
- `CreateProductRequest` không có field `images` ở level product.

### Yêu cầu BE
Thêm relationship giữa Product và ProductImage:

```java
// Trong Product entity
@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
private List<ProductImage> images = new ArrayList<>();
```

Hoặc thêm field `images` vào `CreateProductRequest` và `UpdateProductRequest` để FE có thể upload ảnh chung của sản phẩm.

---

## 6. Refresh Token — Đã loại bỏ token rotation

### Vấn đề
Issue nói: "FE nói nè chời, không có dùng rotation cho refreshToken nhé, tạo tài liệu sai hay làm sai rồi?"

### Đã sửa
**BE đã loại bỏ token rotation.** File `AuthenticationService.java` — method `refreshToken()` giờ không còn tạo refresh token mới:
```java
// Không còn: String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);
// Thay vào đó, trả về token cũ:
result.put("refreshToken", refreshTokenCookie);
```

`AuthController.java` — comment và description đã cập nhật:
```java
// Refresh token is reused (no token rotation) - just refresh the cookie expiry
```

Mỗi lần gọi `POST /api/auth/refresh`, BE chỉ tạo access token mới, refresh token cũ được giữ nguyên và set lại vào cookie để gia hạn thời gian.

### Kết luận
- **BE không dùng token rotation** — refresh token được tái sử dụng.
- **API_DOCUMENTATION.md đã cập nhật** — ghi rõ "không dùng token rotation".
- **FE không cần làm gì** — trình duyệt tự động giữ cookie, FE chỉ cần gọi `/api/auth/refresh` khi access token hết hạn.

---

## Tóm tắt — Thứ tự ưu tiên

| # | Vấn đề | Mức độ | Trạng thái |
|---|--------|--------|------------|
| 1 | Roles trong `/users/me` | Cao | ✓ Đã thêm field roles |
| 2 | PUT product cập nhật variant | Cao | ✓ Đã thêm trường variants |
| 3 | Variant ↔ Option value mapping | Cao | ✓ Đã thêm optionValues + resolve-variant |
| 4 | Bundle options CRUD | Trung | ⏳ Chưa implement — cần admin endpoints |
| 5 | Ảnh chung sản phẩm | Thấp | ⏳ Chưa implement — gắn vào variant đầu tiên |
| 6 | Refresh token rotation | Thấp | ✓ Đã loại bỏ rotation |

---

## Ghi chú FE

**Workaround đã loại bỏ:**
1. ~~**Roles:**~~ ✓ FE giờ dùng field `roles` từ `/api/users/me` — không cần đoán admin.
2. ~~**PUT product:**~~ ✓ FE giờ gửi trường `variants` trong PUT request để cập nhật SKU, giá, tồn kho, ảnh.
3. ~~**Variant mapping:**~~ ✓ FE giờ gọi `resolve-variant` endpoint để chọn biến thể theo màu/kích cỡ.

**Workaround vẫn còn:**
1. **Ảnh chung:** Gắn ảnh chung vào variant đầu tiên khi tạo sản phẩm (đợi BE thêm relationship Product → Image).
2. **Bundle options:** Form tạo sản phẩm có cảnh báo vàng nói rõ lý do khi BE chưa có API (đợi BE thêm admin endpoints).

Khi BE bổ sung các API còn lại (bundle CRUD, ảnh chung sản phẩm), FE sẽ loại bỏ các workaround còn lại và sử dụng API chính thức.
