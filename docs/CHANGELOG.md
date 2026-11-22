# Changelog — Giftly Backend

## v1.1 — 2026-11-22

### Thêm
- **`UserResponse`** — trường `roles: List<String>` (ví dụ: `["USER"]`, `["ADMIN", "USER"]`). FE không cần đoán vai trò admin nữa.
- **`UpdateProductRequest`** — trường `variants: List<CreateVariantRequest>`. `PUT /api/admin/products/{id}` giờ nhận được cập nhật variant (SKU, giá, tồn kho, ảnh, kích thước, chính sách).
- **`VariantResponse`** — trường `optionValues: List<ProductVariantOptionValueResponse>`. Mỗi variant trả về thông tin màu/kích cỡ gắn với nó.
- **Endpoint `GET /api/products/{id}/resolve-variant?optionValueIds=id1,id2`** — tìm variant khớp với các tùy chọn người dùng đã chọn, giúp FE chọn đúng biến thể theo màu/kích cỡ.
- **File `docs/api/backend-integration.md`** — tài liệu chi tiết các gap BE/FE, FE workaround, và yêu cầu BE bổ sung (bundle CRUD, ảnh chung sản phẩm).

### Sửa
- **Refresh token rotation — đã loại bỏ.** `POST /api/auth/refresh` giờ giữ nguyên refresh token cũ và chỉ tạo access token mới. Trước đó BE tạo refresh token mới mỗi lần (token rotation).
- **`AuthenticationService.java`** — method `refreshToken()` không còn gọi `jwtUtil.generateRefreshToken()`, trả về token cũ thay vì token mới.
- **`AuthController.java`** — cập nhật comment và OpenAPI description ghi rõ "no token rotation".
- **`docs/API_DOCUMENTATION.md`** — bổ sung đầy đủ mô tả 4 thay đổi mới: roles field, variants field, optionValues field, resolve-variant endpoint. Sửa ghi chú refresh token: "refreshToken cũ được giữ nguyên (không dùng token rotation)".
- **`docs/api/backend-integration.md`** — cập nhật section 1, 2, 3 thành ✓ Đã hoàn thành; cập nhật bảng tóm tắt và ghi chú FE.

### Xóa
- **Token rotation logic** — bỏ `generateRefreshToken()` trong flow refresh token. Refresh token được tái sử dụng, chỉ set lại cookie để gia hạn thời gian.

---

## Tóm tắt
| STT | Thay đổi | Trạng thái |
|-----|----------|------------|
| 1 | UserResponse thêm roles | ✓ Đã thêm |
| 2 | UpdateProductRequest thêm variants | ✓ Đã thêm |
| 3 | VariantResponse thêm optionValues | ✓ Đã thêm |
| 4 | Endpoint resolve-variant | ✓ Đã thêm |
| 5 | Loại bỏ refresh token rotation | ✓ Đã xóa |
| 6 | Tài liệu backend-integration.md | ✓ Đã tạo |
