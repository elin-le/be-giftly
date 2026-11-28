# Changelog — Giftly Backend

## 2026-11-28

Các endpoint admin bên dưới cần `Authorization: Bearer <token>` của tài khoản có role `ADMIN`. Các ví dụ response lược bớt field không liên quan; response thành công dùng wrapper `{ "success": true, "message": "...", "data": ..., "timestamp": "..." }`.

### Category image và API danh mục

FE upload ảnh trực tiếp lên Cloudinary, sau đó gửi metadata `image: { "publicId": "...", "secureUrl": "https://..." }` cho backend; không gửi file/multipart.

- Public đọc danh mục: `GET /api/categories` (không cần auth, `200 OK`). `data` là mảng category `{ id, name, description, image }`; `image` là `null` hoặc `{ publicId, secureUrl }`.
- Admin đọc danh sách: `GET /api/admin/categories?page=0&size=20` trả `200 OK` và `ApiResponse<PageResponse<CategoryResponse>>`; đọc chi tiết: `GET /api/admin/categories/{categoryId}` trả `200 OK` và `ApiResponse<CategoryResponse>`.
- Tạo: `POST /api/admin/categories` — trả `201 Created`.

```json
{
  "name": "Gấu bông",
  "description": "Các loại gấu bông",
  "image": {
    "publicId": "giftly/categories/teddy",
    "secureUrl": "https://res.cloudinary.com/demo/teddy.jpg"
  }
}
```

- Sửa: `PUT /api/admin/categories/{categoryId}` — trả `200 OK`. Bỏ `image` để giữ ảnh hiện tại; gửi metadata mới để thay ảnh; gửi `removeImage: true` (không gửi đồng thời `image`) để xóa ảnh. Ví dụ body: `{"name":"Gấu bông cao cấp","description":"Gấu bông nhập khẩu","image":{"publicId":"giftly/categories/premium","secureUrl":"https://res.cloudinary.com/demo/premium.jpg"},"removeImage":false}`.
- Xóa: `DELETE /api/admin/categories/{categoryId}` — trả `200 OK`, `data: null`.

Response category có dạng:

```json
{
  "success": true,
  "message": "Category created successfully",
  "data": {
    "id": "<category-uuid>",
    "name": "Gấu bông",
    "description": "Các loại gấu bông",
    "image": {
      "publicId": "giftly/categories/teddy",
      "secureUrl": "https://res.cloudinary.com/demo/teddy.jpg"
    }
  },
  "timestamp": "2026-11-28T10:00:00"
}
```

Product response cũng trả category summary có `image` cùng cấu trúc này.

### Admin CRUD yêu cầu custom gift box

Các route dưới `/api/admin/gift-box-requests` quản lý cấu hình hộp quà gắn với khách hàng; cấu trúc variant/component/item được trả trong `configuration` (`id`, `boxProduct`, `components`, `items`, `totalPrice`).

- `POST /api/admin/gift-box-requests` — tạo thay mặt khách, trả `201 Created` và `ApiResponse<AdminGiftBoxRequestResponse>`.
- `GET /api/admin/gift-box-requests?status=PENDING&page=0&size=20` — `200 OK`; lọc trạng thái tùy chọn, phân trang mặc định `page=0`, `size=20`, mới nhất trước; `status` nhận `PENDING`, `APPROVED`, `REJECTED`, `REVISION_REQUIRED`.
- `GET /api/admin/gift-box-requests/{requestId}` — lấy chi tiết, `200 OK`.
- `PUT /api/admin/gift-box-requests/{requestId}` — thay toàn bộ cấu hình, `200 OK`; cần gửi đủ `boxVariantId`, `components`, `items`. Thành công đặt lại trạng thái review thành `PENDING` và xóa ghi chú cũ.
- `PATCH /api/admin/gift-box-requests/{requestId}/review` — cập nhật trạng thái/ghi chú, `200 OK`.
- `DELETE /api/admin/gift-box-requests/{requestId}` — xóa request chưa được dùng trong order; trả `200 OK` và `data: null`. Request đã được order tham chiếu không thể xóa.

Body tạo/cập nhật cấu hình:

```json
{
  "customerId": "<customer-uuid>",
  "boxVariantId": "<box-variant-uuid>",
  "components": [
    { "productVariantId": "<ribbon-variant-uuid>" },
    { "productVariantId": "<card-variant-uuid>" }
  ],
  "items": [
    { "productVariantId": "<product-variant-uuid>", "quantity": 2 }
  ]
}
```

`customerId` chỉ dùng ở `POST`; với `PUT`, gửi body gồm `boxVariantId`, `components`, `items` (không gửi `customerId`). Gửi mảng rỗng nếu muốn bỏ hết component/item tương ứng.

Body duyệt request:

```json
{
  "status": "REVISION_REQUIRED",
  "note": "Vui lòng chọn lại ruy-băng"
}
```

Ví dụ response của `POST` (các field trong `data` giống với response chi tiết từ `GET`, `PUT`, `PATCH`):

```json
{
  "success": true,
  "message": "Gift box request created successfully",
  "data": {
    "id": "<request-uuid>",
    "customerId": "<customer-uuid>",
    "customerEmail": "khach@example.test",
    "reviewStatus": "PENDING",
    "reviewNote": null,
    "createdAt": "2026-11-28T10:00:00",
    "updatedAt": "2026-11-28T10:00:00",
    "configuration": {
      "id": "<gift-box-uuid>",
      "boxProduct": { "variantId": "<box-variant-uuid>", "name": "Hộp quà", "sku": "BOX-01", "price": 50000, "images": [] },
      "components": [],
      "items": [],
      "totalPrice": 50000
    }
  },
  "timestamp": "2026-11-28T10:00:00"
}
```

Danh sách dùng cùng envelope, nhưng `data` là `PageResponse` với `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`.

### Admin analytics

Hai endpoint cần ADMIN; `from`/`to` tùy chọn theo `YYYY-MM-DD`, tính inclusive. Mặc định là 30 ngày tính đến hôm nay; nếu `from > to` trả lỗi request. Doanh thu chỉ tính payment `PAID` theo `paidAt`; AOV là trung bình `totalAmount` của các đơn có payment thành công.

- `GET /api/admin/analytics/overview?from=2026-11-01&to=2026-11-28` trả `data` gồm `from`, `to`, `totalRevenue`, `averageOrderValue`, `paidOrderCount`, `totalOrderCount`, `customerCount`.
- `GET /api/admin/analytics/revenue/daily?from=2026-11-01&to=2026-11-28` trả `data` là mảng `{ date, revenue, paymentCount, paidOrderCount }`; ngày không có giao dịch vẫn xuất hiện với các số bằng `0`.

```json
{
  "success": true,
  "message": "Success",
  "data": [
    { "date": "2026-11-01", "revenue": 1250000, "paymentCount": 3, "paidOrderCount": 3 },
    { "date": "2026-11-02", "revenue": 0, "paymentCount": 0, "paidOrderCount": 0 }
  ],
  "timestamp": "2026-11-28T10:00:00"
}
```

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "from": "2026-11-01",
    "to": "2026-11-28",
    "totalRevenue": 1250000,
    "averageOrderValue": 375000,
    "paidOrderCount": 3,
    "totalOrderCount": 8,
    "customerCount": 6
  },
  "timestamp": "2026-11-28T10:00:00"
}
```

### Catalog sorting và payment methods

- `GET /api/products` không cần auth; ví dụ lọc kết hợp: `/api/products?keyword=gau%20bong&type=REGULAR&minPrice=50000&maxPrice=300000&categoryId=<uuid>&cursor=<uuid>&pageSize=20`. Hỗ trợ thêm `customComponentType`; keyword tìm gần đúng/theo từ và không phân biệt dấu. `type` hỗ trợ `CUSTOM`, `REGULAR`/`RETAIL`, `SET`, `BOX` hoặc enum `ProductType`. Giá biên inclusive và sản phẩm phải có ít nhất một variant phù hợp. Mặc định/luôn sắp mới nhất trước (`createdAt DESC`, `id DESC`); response `data` có `{ products, nextCursor, hasMore, totalSize }`, cursor là UUID sản phẩm cuối trang.
- Tạo payment cần user auth: `POST /api/payments` nhận `{"orderId":"<order-uuid>","paymentMethod":"COD"}` hoặc `{"orderId":"<order-uuid>","paymentMethod":"ZALOPAY"}`; code hiện trả `200 OK`. `data` có `{ id, orderId, paymentMethod, paymentStatus, paymentStage, amount, paidAt, checkoutUrl }`; `checkoutUrl` chứa link thanh toán cho ZaloPay. Mọi phương thức khác `COD`/`ZALOPAY` không còn hợp lệ.

Ví dụ response payment:

```json
{
  "success": true,
  "message": "Payment created successfully",
  "data": {
    "id": "<payment-uuid>",
    "orderId": "<order-uuid>",
    "paymentMethod": "ZALOPAY",
    "paymentStatus": "PENDING",
    "paymentStage": "FULL_PAYMENT",
    "amount": 130000,
    "paidAt": null,
    "checkoutUrl": "https://zalopay.example/checkout/<token>"
  },
  "timestamp": "2026-11-28T10:00:00"
}
```

### Migration và kiểm thử

- **V24** — Thêm Cloudinary metadata cho category và giới hạn `payment_method` trong `COD`/`ZALOPAY`.
- Cập nhật demo seed, API/database/architecture docs và regression tests cho các thay đổi trên.
- `./gradlew test` thành công; `git diff --check` sạch.
