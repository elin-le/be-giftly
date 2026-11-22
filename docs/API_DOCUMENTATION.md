# Giftly API Documentation

## Tổng quan

Tài liệu API này mô tả đầy đủ tất cả endpoint của hệ thống Giftly — nền tảng bán quà tặng online.

| Thành phần | Chi tiết |
|---|---|
| **Base URL** | `http://localhost:8080` |
| **API Prefix** | `/api` |
| **Content-Type** | `application/json` (trừ upload ảnh dùng `multipart/form-data`) |
| **CORS** | Cho phép `http://localhost:5173` với credentials |
| **Xác thực** | JWT Bearer token + HttpOnly cookie (refresh token) |
| **Database** | PostgreSQL |
| **Swagger UI** | `http://localhost:8080/swagger-ui.html` |

---

## 1. Cấu trúc Response Chung

Tất cả response thành công đều được bọc trong `ApiResponse`:

```json
{
  "success": true,
  "message": "Success",
  "data": { ... },
  "timestamp": "2026-11-22T17:00:00"
}
```

Tất cả response lỗi đều có cấu trúc:

```json
{
  "timestamp": "2026-11-22T17:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Lỗi cụ thể",
  "path": "/api/..."
}
```

Validation lỗi trả về thêm `fieldErrors`:

```json
{
  "timestamp": "2026-11-22T17:00:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "One or more fields failed validation",
  "path": "/api/...",
  "fieldErrors": {
    "email": "Email is required",
    "password": "Password must be at least 6 characters"
  }
}
```

---

## 2. Xác thực (Authentication)

### Cơ chế JWT

- **Access Token**: Trả về trong response body, hết hạn sau **1 giờ** (3600000ms).
- **Refresh Token**: Lưu trong **HttpOnly cookie** (không có trong response body), hết hạn sau **7 ngày** (604800000ms).

### Gửi token

Mọi request cần xác thực phải gửi:

```
Authorization: Bearer <access_token>
```

Refresh token được trình duyệt tự động gửi qua cookie (HttpOnly, Secure=false cho dev, Path=/).

### CORS & Credentials

Frontend phải cấu hình `withCredentials: true` (Axios) hoặc `credentials: 'include'` (Fetch) để gửi cookie cross-origin.

```javascript
// Axios
axios.defaults.withCredentials = true;

// Fetch
fetch(url, { credentials: 'include' });
```

---

## 3. Enum Quan Trọng

### 3.1. OrderStatus

| Giá trị | Mô tả |
|---|---|
| `PENDING_CONFIRMATION` | Chờ xác nhận |
| `CONFIRMED` | Đã xác nhận |
| `PROCESSING` | Đang xử lý |
| `PACKAGING` | Đang đóng gói |
| `READY_TO_SHIP` | Sẵn sàng giao |
| `SHIPPING` | Đang giao |
| `DELIVERED` | Đã giao |
| `COMPLETED` | Hoàn thành |
| `CANCELLED` | Đã hủy |
| `RETURN_REQUESTED` | Yêu cầu trả hàng |
| `RETURNED` | Đã trả hàng |
| `FAILED` | Thất bại |

### 3.2. UserStatus

| Giá trị | Mô tả |
|---|---|
| `ACTIVE` | Hoạt động |
| `INACTIVE` | Không hoạt động |
| `SUSPENDED` | Tạm khóa |

### 3.3. ProductStatus

| Giá trị | Mô tả |
|---|---|
| `AVAILABLE` | Có sẵn |
| `OUT_OF_STOCK` | Hết hàng |
| `DISCONTINUED` | Ngừng kinh doanh |

### 3.4. ProductType

| Giá trị | Mô tả |
|---|---|
| `REGULAR` | Sản phẩm thông thường (gấu bông, hoa, socola...) |
| `READY_MADE_GIFT_SET` | Bộ quà sẵn có cấu hình |
| `READY_MADE_GIFT_BOX` | Hộp quà sẵn bán riêng |
| `PACKAGING_ACCESSORY` | Phụ kiện đóng gói (giấy, xốp...) |
| `GREETING_CARD` | Thiệp chúc mừng |

### 3.5. PaymentMethod

| Giá trị | Mô tả |
|---|---|
| `CREDIT_CARD` | Thẻ tín dụng |
| `DEBIT_CARD` | Thẻ ghi nợ |
| `BANK_TRANSFER` | Chuyển khoản |
| `E_WALLET` | Ví điện tử |
| `COD` | Thanh toán khi nhận hàng |
| `ZALOPAY` | ZaloPay |

### 3.6. PaymentStatus

| Giá trị | Mô tả |
|---|---|
| `PENDING` | Chờ thanh toán |
| `PAID` | Đã thanh toán |
| `FAILED` | Thất bại |
| `REFUNDED` | Đã hoàn tiền |

### 3.7. PaymentStage

| Giá trị | Mô tả |
|---|---|
| `FULL_PAYMENT` | Thanh toán toàn bộ |
| `ADVANCE_PAYMENT` | Đặt cọc trước |
| `REMAINING_PAYMENT` | Thanh toán số dư |

### 3.8. VariantStatus

| Giá trị | Mô tả |
|---|---|
| `AVAILABLE` | Có sẵn |
| `OUT_OF_STOCK` | Hết hàng |
| `DISCONTINUED` | Ngừng kinh doanh |

### 3.9. ReviewStatus

| Giá trị | Mô tả |
|---|---|
| `PENDING` | Chờ duyệt |
| `APPROVED` | Đã duyệt |
| `REJECTED` | Từ chối |

### 3.10. ReturnPolicy

| Giá trị | Mô tả |
|---|---|
| `RETURNABLE` | Có thể trả hàng |
| `NO_RETURN` | Không trả hàng |

### 3.11. GroupType

| Giá trị | Mô tả |
|---|---|
| `GREETING_CARD` | Nhóm chọn thiệp |
| `PACKAGING_ACCESSORY` | Nhóm chọn phụ kiện |
| `OTHER` | Nhóm tùy chỉnh khác |

### 3.12. SelectionType

| Giá trị | Mô tả |
|---|---|
| `SINGLE` | Chọn 1 |
| `MULTIPLE` | Chọn nhiều |

### 3.13. PricingMode

| Giá trị | Mô tả |
|---|---|
| `FREE` | Miễn phí |
| `PRODUCT_PRICE` | Giá theo sản phẩm |
| `FIXED_PRICE` | Giá cố định |

### 3.14. ComponentRole

| Giá trị | Mô tả |
|---|---|
| `BOX` | Hộp chứa |
| `PRODUCT` | Sản phẩm trong bundle |
| `ACCESSORY` | Phụ kiện |
| `GREETING_CARD` | Thiệp |

### 3.15. MessageRenderingMethod

| Giá trị | Mô tả |
|---|---|
| `HANDWRITTEN` | Viết tay |
| `PRINTED` | In |

### 3.16. DimensionUnit

| Giá trị | Mô tả |
|---|---|
| `MM` | Milimet |
| `CM` | Xentimét |
| `M` | Mét |

### 3.17. WeightUnit

| Giá trị | Mô tả |
|---|---|
| `G` | Gam |
| `KG` | Kilogam |

### 3.18. GiftBoxReviewStatus

| Giá trị | Mô tả |
|---|---|
| `PENDING` | Chờ duyệt |
| `APPROVED` | Đã duyệt |
| `REJECTED` | Từ chối |
| `REVISION_REQUIRED` | Cần sửa đổi |

---

## 4. API Endpoint

### 4.1. Authentication (Không cần auth)

#### 4.1.1. Đăng nhập

```
POST /api/auth/login
```

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "fullName": "John Doe"
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

**Cookie:** `refreshToken` (HttpOnly, Secure=false, Path=/, MaxAge=604800s)

**Lỗi:**
- `401` — Email hoặc mật khẩu sai

---

#### 4.1.2. Đăng ký

```
POST /api/auth/register
```

**Request Body:**

```json
{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "phone": "0839003848"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "fullName": "John Doe"
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

**Cookie:** `refreshToken` (HttpOnly, Secure=false, Path=/, MaxAge=604800s)

**Lỗi:**
- `400` — Validation lỗi
- `409` — Email đã tồn tại

---

#### 4.1.3. Làm mới token

```
POST /api/auth/refresh
```

Không cần Authorization header. Endpoint đọc refresh token từ HttpOnly cookie.

**Response (200):**

```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "user@example.com",
    "fullName": "John Doe"
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

**Cookie:** `refreshToken` cũ được giữ nguyên (không dùng token rotation)

**Lỗi:**
- `401` — Refresh token không tìm thấy hoặc đã hết hạn

**Ví dụ Axios interceptor tự động refresh:**

```javascript
axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        await axios.post('/api/auth/refresh');
        return axios(originalRequest);
      } catch (refreshError) {
        // Redirect to login
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
```

---

#### 4.1.4. Đăng xuất

```
POST /api/auth/logout
```

**Response (200):**

```json
{
  "success": true,
  "message": "Logout successful.",
  "data": "Logged out",
  "timestamp": "2026-11-22T17:00:00"
}
```

**Cookie:** `refreshToken` bị xóa (MaxAge=0)

---

### 4.2. Products (Không cần auth)

#### 4.2.1. Lấy danh sách sản phẩm

```
GET /api/products
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `categoryId` | UUID | Không | Lọc theo danh mục |
| `keyword` | String | Không | Tìm kiếm tên/mô tả |
| `cursor` | String | Không | Con trỏ phân trang |
| `pageSize` | int | Không | Số lượng (mặc định 20) |

**Response (200):**

```json
{
  "success": true,
  "message": "Products retrieved successfully",
  "data": {
    "products": [
      {
        "id": "uuid",
        "name": "Gấu bông Teddy 30cm",
        "description": "Gấu bông mềm mại...",
        "productType": "REGULAR",
        "status": "AVAILABLE",
        "isCustomBoxEligible": true,
        "category": {
          "id": "uuid",
          "name": "Gấu bông"
        },
        "images": [
          {
            "id": "uuid",
            "imageUrl": "http://...",
            "publicId": "...",
            "secureUrl": "https://...",
            "isPrimary": true,
            "displayOrder": 0
          }
        ],
        "variants": [
          {
            "id": "uuid",
            "sku": "SM-RED",
            "price": 50000,
            "stockQuantity": 100,
            "status": "AVAILABLE",
            "images": [ ... ],
            "optionValues": [
              {
                "optionValueId": "uuid",
                "optionName": "Màu sắc",
                "value": "Đỏ",
                "colorHex": "#FF0000"
              },
              {
                "optionValueId": "uuid",
                "optionName": "Kích cỡ",
                "value": "S",
                "colorHex": null
              }
            ]
          }
        ],
        "averageRating": 4.5,
        "reviewCount": 10
      }
    ],
    "nextCursor": "uuid",
    "hasMore": true,
    "totalSize": 20
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

### 4.3. Product Options (Không cần auth)

#### 4.3.1. Lấy tùy chọn sản phẩm

```
GET /api/products/{productId}/options
```

Lấy danh sách tùy chọn (màu sắc, kích thước...) của sản phẩm.

**Response (200):**

```json
{
  "success": true,
  "message": "Product options retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Màu sắc",
      "displayOrder": 0,
      "values": [
        {
          "id": "uuid",
          "value": "Đỏ",
          "colorHex": "#FF0000"
        },
        {
          "id": "uuid",
          "value": "Xanh",
          "colorHex": "#0000FF"
        }
      ]
    }
  ],
  "timestamp": "2026-11-22T17:00:00"
}
```

#### 4.3.2. Tìm biến thể theo tùy chọn đã chọn (Mới)

```
GET /api/products/{productId}/resolve-variant?optionValueIds=id1,id2,id3
```

Dùng để tìm biến thể (variant) tương ứng với các tùy chọn (màu, kích cỡ...) mà người dùng đã chọn. FE gọi endpoint này khi người dùng chọn màu/kích cỡ trên trang chi tiết sản phẩm.

**Query Parameters:**
- `optionValueIds` (bắt buộc): Danh sách các option value ID, phân cách bằng dấu phẩy

**Response (200):**

```json
{
  "success": true,
  "message": "Variant resolved successfully",
  "data": {
    "id": "uuid",
    "sku": "RED-M-001",
    "price": 99000,
    "stockQuantity": 50,
    "status": "ACTIVE",
    "images": [
      {
        "id": "uuid",
        "imageUrl": "https://...",
        "isPrimary": true,
        "displayOrder": 0
      }
    ],
    "optionValues": [
      {
        "optionValueId": "uuid",
        "optionName": "Màu sắc",
        "value": "Đỏ",
        "colorHex": "#FF0000"
      },
      {
        "optionValueId": "uuid",
        "optionName": "Kích cỡ",
        "value": "M",
        "colorHex": null
      }
    ]
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

**Lỗi:**
- `404` — Không tìm thấy variant khớp với các option value IDs đã cho

---

### 4.4. Categories (Không cần auth)

#### 4.4.1. Lấy danh sách danh mục

```
GET /api/categories
```

**Response (200):**

```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Gấu bông",
      "description": "Các loại gấu bông"
    }
  ],
  "timestamp": "2026-11-22T17:00:00"
}
```

---

### 4.5. Bundles (Không cần auth)

Các API bundle dùng để lấy cấu hình gói quà, tùy chọn, và chính sách biến thể.

#### 4.5.1. Lấy nhóm tùy chọn bundle

```
GET /api/bundles/variants/{variantId}/option-groups
```

**Response (200):**

```json
{
  "success": true,
  "message": "Bundle option groups retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Thiệp chúc mừng",
      "groupType": "GREETING_CARD",
      "selectionType": "SINGLE",
      "minSelections": 1,
      "maxSelections": 1,
      "isRequired": true,
      "displayOrder": 0,
      "options": [
        {
          "id": "uuid",
          "productVariantId": "uuid",
          "pricingMode": "FIXED_PRICE",
          "fixedPrice": 10000
        }
      ]
    }
  ],
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.5.2. Lấy thành phần bundle

```
GET /api/bundles/variants/{variantId}/components
```

**Response (200):**

```json
{
  "success": true,
  "message": "Bundle components retrieved successfully",
  "data": [
    {
      "id": "uuid",
      "componentVariantId": "uuid",
      "componentRole": "ACCESSORY",
      "quantity": 1,
      "isRequired": true
    }
  ],
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.5.3. Lấy chính sách biến thể

```
GET /api/bundles/variants/{variantId}/policy
```

**Response (200):**

```json
{
  "success": true,
  "message": "Variant policy retrieved successfully",
  "data": {
    "id": "uuid",
    "variantId": "uuid",
    "isPerishable": true,
    "shelfLifeHours": 24,
    "returnPolicy": "NO_RETURN",
    "advancePaymentPercent": 50.00
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.5.4. Lấy thông tin hộp quà

```
GET /api/bundles/variants/{variantId}/gift-box-detail
```

**Response (200):**

```json
{
  "success": true,
  "message": "Gift box detail retrieved successfully",
  "data": {
    "id": "uuid",
    "variantId": "uuid",
    "innerLength": 15.0,
    "innerWidth": 10.0,
    "innerHeight": 8.0,
    "maxWeight": 2.0,
    "dimensionUnit": "cm",
    "weightUnit": "kg"
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

### 4.6. Shopping Cart (Cần auth)

#### 4.6.1. Lấy giỏ hàng

```
GET /api/cart
```

**Response (200):**

```json
{
  "success": true,
  "message": "Cart retrieved successfully",
  "data": {
    "id": "uuid",
    "items": [
      {
        "id": "uuid",
        "productVariantId": "uuid",
        "productName": "Gấu bông Teddy",
        "price": 50000,
        "thumbnailUrl": "https://...",
        "quantity": 2
      }
    ],
    "subtotal": 100000,
    "totalItems": 2
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.6.2. Thêm vào giỏ hàng

```
POST /api/cart/items
```

**Request Body:**

```json
{
  "productVariantId": "uuid",
  "quantity": 1
}
```

**Response (200):** Trả về `CartResponse` đã cập nhật.

**Lỗi:**
- `400` — Validation lỗi (quantity < 1)

---

#### 4.6.3. Cập nhật số lượng

```
PUT /api/cart/items/{itemId}
```

**Request Body:**

```json
{
  "quantity": 3
}
```

**Response (200):** Trả về `CartResponse` đã cập nhật.

---

#### 4.6.4. Xóa sản phẩm khỏi giỏ

```
DELETE /api/cart/items/{itemId}
```

**Response (200):** Trả về `CartResponse` đã cập nhật.

---

#### 4.6.5. Xóa toàn bộ giỏ hàng

```
DELETE /api/cart
```

**Response (200):**

```json
{
  "success": true,
  "message": "Cart cleared",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

### 4.7. Orders (Cần auth)

#### 4.7.1. Tạo đơn hàng

```
POST /api/orders
```

**Request Body:**

```json
{
  "addressId": "uuid"
}
```

**Response (201):**

```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "id": "uuid",
    "orderNumber": "ORD-20261122-001",
    "subtotal": 100000,
    "shippingFee": 30000,
    "totalAmount": 130000,
    "status": "PENDING_CONFIRMATION",
    "orderedAt": "2026-11-22T17:00:00",
    "address": {
      "id": "uuid",
      "recipientName": "John Doe",
      "phone": "0839003848",
      "addressLine": "123 Đường ABC",
      "ward": "Phường 1",
      "district": "Quận 1",
      "city": "TP. Hồ Chí Minh"
    },
    "items": [
      {
        "id": "uuid",
        "productVariantId": "uuid",
        "itemName": "Gấu bông Teddy",
        "unitPrice": 50000,
        "quantity": 2,
        "subtotal": 100000
      }
    ]
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.7.2. Lấy danh sách đơn hàng của tôi

```
GET /api/orders
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `cursor` | String | Không | Con trỏ phân trang |
| `pageSize` | int | Không | Số lượng (mặc định 20) |

**Response (200):**

```json
{
  "success": true,
  "message": "Orders retrieved successfully",
  "data": {
    "orders": [ ... ],
    "nextCursor": "uuid",
    "hasMore": true,
    "totalSize": 20
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.7.3. Lấy chi tiết đơn hàng

```
GET /api/orders/{orderId}
```

**Response (200):** Trả về `OrderResponse`.

**Lỗi:**
- `404` — Đơn hàng không tồn tại
- `403` — Không phải đơn hàng của bạn

---

#### 4.7.4. Hủy đơn hàng

```
PUT /api/orders/{orderId}/cancel
```

**Response (200):** Trả về `OrderResponse` đã cập nhật status.

---

### 4.8. Reviews (Cần auth)

#### 4.8.1. Tạo đánh giá

```
POST /api/reviews
Content-Type: multipart/form-data
```

**Form Data:**

| Field | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `request` | JSON | Có | Dữ liệu đánh giá |
| `image` | File | Không | Ảnh đánh giá (JPEG, PNG) |

**`request` JSON:**

```json
{
  "orderItemId": "uuid",
  "rating": 5,
  "title": "Sản phẩm tuyệt vời!",
  "comment": "Chất lượng tốt, giao hàng nhanh.",
  "imageUrl": "https://example.com/review-image.jpg"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Review created successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "userEmail": "user@example.com",
    "product": {
      "id": "uuid",
      "name": "Gấu bông Teddy",
      "thumbnailUrl": "https://..."
    },
    "productVariantId": "uuid",
    "rating": 5,
    "title": "Sản phẩm tuyệt vời!",
    "comment": "Chất lượng tốt, giao hàng nhanh.",
    "imageUrls": ["https://..."],
    "createdAt": "2026-11-22T17:00:00",
    "updatedAt": "2026-11-22T17:00:00"
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.8.2. Lấy đánh giá theo sản phẩm

```
GET /api/reviews/product/{productId}
```

**Response (200):** Trả về `List<ReviewResponse>`.

---

#### 4.8.3. Lấy đánh giá theo sản phẩm (phân trang)

```
GET /api/reviews/product/{productId}/paginated
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `rating` | Short | Không | Lọc theo sao (1-5) |
| `page` | int | Không | Trang (mặc định 0) |
| `size` | int | Không | Số lượng (mặc định 20) |
| `sort` | String | Không | Sắp xếp (mặc định createdAt) |

**Response (200):** Trả về `Page<ReviewResponse>`.

---

#### 4.8.4. Lấy đánh giá của tôi

```
GET /api/reviews/me
```

**Query Parameters:** `page`, `size`, `sort` (mặc định createdAt).

**Response (200):** Trả về `Page<ReviewResponse>`.

---

#### 4.8.5. Lấy đánh giá theo ID

```
GET /api/reviews/{reviewId}
```

**Response (200):** Trả về `ReviewResponse`.

---

#### 4.8.6. Xóa đánh giá của tôi

```
DELETE /api/reviews/{reviewId}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Review deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

### 4.9. User Profile (Cần auth)

#### 4.9.1. Lấy thông tin cá nhân

```
GET /api/users/me
```

**Response (200):**

```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": "uuid",
    "email": "user@example.com",
    "fullName": "John Doe",
    "phone": "0839003848",
    "status": "ACTIVE",
    "roles": ["USER"],
    "createdAt": "2026-11-22T17:00:00"
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

**Lưu ý:** Field `roles` trả về danh sách vai trò của user (ví dụ: `["USER"]`, `["ADMIN", "USER"]`). FE có thể dùng field này để xác định quyền admin mà không cần gọi thử `/api/admin/dashboard/stats`.

---

#### 4.9.2. Cập nhật thông tin cá nhân

```
PUT /api/users/me
```

**Request Body:**

```json
{
  "fullName": "John Doe Updated",
  "phone": "0901234567"
}
```

**Response (200):** Trả về `UserResponse` đã cập nhật.

---

#### 4.9.3. Cập nhật profile admin

```
PATCH /api/users/me
```

**Request Body:**

```json
{
  "secretCode": "123456",
  "fullName": "John Doe",
  "phone": "0901234567",
  "password": "NewSecurePass123!"
}
```

**Response (200):** Trả về `UserResponse` đã cập nhật.

---

### 4.10. Addresses (Cần auth)

#### 4.10.1. Tạo địa chỉ

```
POST /api/addresses
```

**Request Body:**

```json
{
  "recipientName": "John Doe",
  "phone": "0839003848",
  "addressLine": "123 Đường ABC",
  "ward": "Phường 1",
  "district": "Quận 1",
  "city": "TP. Hồ Chí Minh",
  "isDefault": true
}
```

**Response (201):**

```json
{
  "success": true,
  "message": "Address created successfully",
  "data": {
    "id": "uuid",
    "recipientName": "John Doe",
    "phone": "0839003848",
    "addressLine": "123 Đường ABC",
    "ward": "Phường 1",
    "district": "Quận 1",
    "city": "TP. Hồ Chí Minh",
    "isDefault": true
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.10.2. Cập nhật địa chỉ

```
PUT /api/addresses/{addressId}
```

**Request Body:**

```json
{
  "recipientName": "John Doe",
  "phone": "0839003848",
  "addressLine": "456 Đường XYZ",
  "ward": "Phường 2",
  "district": "Quận 1",
  "city": "TP. Hồ Chí Minh"
}
```

**Response (200):** Trả về `AddressResponse` đã cập nhật.

---

#### 4.10.3. Đặt địa chỉ mặc định

```
PUT /api/addresses/{addressId}/set-default
```

**Response (200):** Trả về `AddressResponse` đã cập nhật.

---

#### 4.10.4. Xóa địa chỉ

```
DELETE /api/addresses/{addressId}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Address deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.10.5. Lấy danh sách địa chỉ của tôi

```
GET /api/addresses
```

**Response (200):** Trả về `List<AddressResponse>`.

---

#### 4.10.6. Lấy địa chỉ theo ID

```
GET /api/addresses/{addressId}
```

**Response (200):** Trả về `AddressResponse`.

---

### 4.11. Payments (Cần auth)

#### 4.11.1. Tạo thanh toán

```
POST /api/payments
```

**Request Body:**

```json
{
  "orderId": "uuid",
  "paymentMethod": "COD"
}
```

`paymentMethod` có thể là: `COD`, `ZALOPAY`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `E_WALLET`.

**Response (201):**

```json
{
  "success": true,
  "message": "Payment created successfully",
  "data": {
    "id": "uuid",
    "orderId": "uuid",
    "paymentMethod": "COD",
    "paymentStatus": "PENDING",
    "paymentStage": "FULL_PAYMENT",
    "amount": 130000,
    "paidAt": null,
    "checkoutUrl": null
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

Với `ZALOPAY`, `checkoutUrl` sẽ chứa URL thanh toán.

---

#### 4.11.2. Lấy thanh toán theo đơn hàng

```
GET /api/payments/order/{orderId}
```

**Response (200):** Trả về `List<PaymentResponse>`.

---

#### 4.11.3. Lấy thanh toán theo ID

```
GET /api/payments/{paymentId}
```

**Response (200):** Trả về `PaymentResponse`.

---

#### 4.11.4. ZaloPay callback

```
POST /api/payments/{paymentId}/zalopay/callback?success=true
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `success` | boolean | Có | `true` = thành công, `false` = thất bại |

**Response (200):** Trả về `PaymentResponse` đã cập nhật.

---

### 4.12. Gift Boxes (Cần auth)

#### 4.12.1. Tạo hộp quà

```
POST /api/gift-boxes
```

**Request Body:**

```json
{
  "boxVariantId": "uuid",
  "items": [
    {
      "productVariantId": "uuid",
      "quantity": 1
    },
    {
      "productVariantId": "uuid",
      "quantity": 2
    }
  ]
}
```

**Response (201):**

```json
{
  "success": true,
  "message": "Gift box created successfully",
  "data": {
    "id": "uuid",
    "boxProduct": {
      "variantId": "uuid",
      "name": "Hộp quà cao cấp",
      "price": 20000
    },
    "items": [
      {
        "id": "uuid",
        "variantId": "uuid",
        "productName": "Gấu bông Teddy",
        "sku": "SM-RED",
        "price": 50000,
        "quantity": 1,
        "subtotal": 50000
      }
    ],
    "totalPrice": 70000
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.12.2. Lấy danh sách hộp quà của tôi

```
GET /api/gift-boxes
```

**Response (200):** Trả về `List<GiftBoxResponse>`.

---

#### 4.12.3. Lấy chi tiết hộp quà

```
GET /api/gift-boxes/{id}
```

**Response (200):** Trả về `GiftBoxResponse`.

---

#### 4.12.4. Xóa hộp quà

```
DELETE /api/gift-boxes/{id}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Gift box deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

### 4.13. Admin APIs (Cần ADMIN role)

Tất cả API admin yêu cầu role `ADMIN` và JWT Bearer token.

#### 4.13.1. Dashboard

```
GET /api/admin/dashboard/stats
```

**Response (200):**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalProducts": 100,
    "totalCategories": 10,
    "totalOrders": 500,
    "totalUsers": 200,
    "totalReviews": 150,
    "totalRevenue": 50000000,
    "pendingOrders": 25,
    "lowStockProducts": 5
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.13.2. Admin Products

**Lấy danh sách:**

```
GET /api/admin/products
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `categoryId` | UUID | Không | Lọc theo danh mục |
| `status` | ProductStatus | Không | Lọc theo trạng thái |
| `type` | ProductType | Không | Lọc theo loại |
| `page` | int | Không | Trang (mặc định 0) |
| `size` | int | Không | Số lượng (mặc định 20) |

**Response (200):** Trả về `Page<Product>` entity.

---

**Lấy theo ID:**

```
GET /api/admin/products/{productId}
```

**Response (200):** Trả về `Product` entity.

---

**Tạo sản phẩm:**

```
POST /api/admin/products
```

**Request Body:**

```json
{
  "categoryId": "uuid",
  "name": "Gấu bông Teddy 30cm",
  "description": "Gấu bông mềm mại, kích thước 30cm",
  "productType": "REGULAR",
  "status": "AVAILABLE",
  "isCustomBoxEligible": true,
  "variants": [
    {
      "sku": "SM-RED",
      "price": 50000,
      "stockQuantity": 100,
      "length": 30.0,
      "width": 20.0,
      "height": 15.0,
      "dimensionUnit": "cm",
      "weight": 0.5,
      "weightUnit": "kg",
      "status": "AVAILABLE",
      "optionValueIds": ["uuid", "uuid"],
      "giftBoxDetail": {
        "innerLength": 35.0,
        "innerWidth": 25.0,
        "innerHeight": 20.0,
        "maxWeight": 1.0,
        "dimensionUnit": "cm",
        "weightUnit": "kg"
      },
      "variantPolicy": {
        "isPerishable": false,
        "shelfLifeHours": null,
        "returnPolicy": "RETURNABLE",
        "advancePaymentPercent": null
      },
      "images": [
        {
          "imageUrl": "http://res.cloudinary.com/...",
          "secureUrl": "https://res.cloudinary.com/...",
          "publicId": "giftly/products/...",
          "isPrimary": true,
          "displayOrder": 0
        }
      ]
    }
  ]
}
```

**Bắt buộc trong variant:** `sku`, `price`, `stockQuantity`, `status`.

**Response (201):** Trả về `Product` entity đã lưu.

---

**Cập nhật sản phẩm:**

```
PUT /api/admin/products/{productId}
```

**Request Body:**

```json
{
  "categoryId": "uuid",
  "name": "Gấu bông Teddy 30cm - Updated",
  "description": "Mô tả mới",
  "productType": "REGULAR",
  "status": "AVAILABLE",
  "isCustomBoxEligible": true,
  "variants": [
    {
      "id": "variant-uuid",
      "sku": "NEW-SKU",
      "price": 99000,
      "stockQuantity": 50,
      "status": "ACTIVE",
      "images": [
        {
          "imageUrl": "https://...",
          "secureUrl": "https://...",
          "publicId": "abc123",
          "isPrimary": true,
          "displayOrder": 0
        }
      ]
    }
  ]
}
```

Tất cả field đều tùy chọn, chỉ cập nhật field được cung cấp.

**Trường `variants` (Mới):** Cho phép cập nhật thông tin biến thể (SKU, giá, tồn kho, ảnh, kích thước, chính sách) của sản phẩm đã tồn tại. Mỗi variant trong danh sách phải có `id` để xác định variant cần cập nhật.

**Response (200):** Trả về `Product` entity đã cập nhật.

---

**Xóa sản phẩm:**

```
DELETE /api/admin/products/{productId}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Product deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.13.3. Admin Categories

**Lấy danh sách:**

```
GET /api/admin/categories
```

**Query Parameters:** `page`, `size` (mặc định 20).

**Response (200):** Trả về `Page<Category>`.

---

**Lấy theo ID:**

```
GET /api/admin/categories/{categoryId}
```

**Response (200):** Trả về `Category` entity.

---

**Tạo danh mục:**

```
POST /api/admin/categories
```

**Request Body:**

```json
{
  "name": "Gấu bông",
  "description": "Các loại gấu bông"
}
```

**Response (201):** Trả về `Category` entity.

---

**Cập nhật danh mục:**

```
PUT /api/admin/categories/{categoryId}
```

**Request Body:**

```json
{
  "name": "Gấu bông cao cấp",
  "description": "Gấu bông cao cấp nhập khẩu"
}
```

**Response (200):** Trả về `Category` entity đã cập nhật.

---

**Xóa danh mục:**

```
DELETE /api/admin/categories/{categoryId}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Category deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

#### 4.13.4. Admin Orders

**Lấy danh sách:**

```
GET /api/admin/orders
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `status` | OrderStatus | Không | Lọc theo trạng thái |
| `userId` | UUID | Không | Lọc theo user |
| `page` | int | Không | Trang (mặc định 0) |
| `size` | int | Không | Số lượng (mặc định 20) |

**Response (200):**

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "uuid",
        "orderNumber": "ORD-20261122-001",
        "userId": "uuid",
        "userEmail": "user@example.com",
        "userFullName": "John Doe",
        "subtotal": 100000,
        "shippingFee": 30000,
        "totalAmount": 130000,
        "status": "PENDING_CONFIRMATION",
        "orderedAt": "2026-11-22T17:00:00",
        "address": {
          "id": "uuid",
          "recipientName": "John Doe",
          "phone": "0839003848",
          "addressLine": "123 Đường ABC",
          "ward": "Phường 1",
          "district": "Quận 1",
          "city": "TP. Hồ Chí Minh"
        },
        "items": [
          {
            "id": "uuid",
            "productId": "uuid",
            "itemName": "Gấu bông Teddy",
            "unitPrice": 50000,
            "quantity": 2,
            "subtotal": 100000
          }
        ]
      }
    ],
    "totalElements": 500,
    "totalPages": 25,
    "size": 20,
    "number": 0
  },
  "timestamp": "2026-11-22T17:00:00"
}
```

---

**Lấy theo ID:**

```
GET /api/admin/orders/{orderId}
```

**Response (200):** Trả về `AdminOrderResponse`.

---

**Lấy theo số đơn hàng:**

```
GET /api/admin/orders/number/{orderNumber}
```

**Response (200):** Trả về `AdminOrderResponse`.

---

**Cập nhật trạng thái đơn hàng:**

```
PATCH /api/admin/orders/{orderId}/status
```

**Request Body:**

```json
{
  "status": "CONFIRMED"
}
```

**Response (200):** Trả về `AdminOrderResponse` đã cập nhật.

---

#### 4.13.5. Admin Reviews

**Lấy danh sách:**

```
GET /api/admin/reviews
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `productId` | UUID | Không | Lọc theo sản phẩm |
| `userId` | UUID | Không | Lọc theo user |
| `page` | int | Không | Trang (mặc định 0) |
| `size` | int | Không | Số lượng (mặc định 20) |
| `sort` | String | Không | Sắp xếp (mặc định createdAt) |

**Response (200):** Trả về `Page<ReviewResponse>`.

---

**Lấy theo ID:**

```
GET /api/admin/reviews/{reviewId}
```

**Response (200):** Trả về `ReviewResponse`.

---

**Xóa đánh giá:**

```
DELETE /api/admin/reviews/{reviewId}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Review deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

Ảnh liên quan sẽ bị xóa khỏi Cloudinary.

---

#### 4.13.6. Admin Users

**Lấy danh sách:**

```
GET /api/admin/users
```

**Query Parameters:** `page`, `size` (mặc định 20).

**Response (200):** Trả về `Page<User>`.

---

**Lấy theo ID:**

```
GET /api/admin/users/{userId}
```

**Response (200):** Trả về `User` entity.

---

**Lấy vai trò của user:**

```
GET /api/admin/users/{userId}/roles
```

**Response (200):** Trả về `List<UserRole>`.

---

**Cập nhật trạng thái user:**

```
PATCH /api/admin/users/{userId}/status
```

**Request Body:**

```json
{
  "status": "SUSPENDED"
}
```

**Response (200):** Trả về `User` entity đã cập nhật.

---

**Gán vai trò:**

```
POST /api/admin/users/roles/assign
```

**Request Body:**

```json
{
  "userId": "uuid",
  "roleName": "ADMIN"
}
```

**Response (200):** Trả về `UserRole` entity.

---

**Xóa vai trò:**

```
DELETE /api/admin/users/roles/remove?userId=uuid&roleName=ADMIN
```

**Query Parameters:**

| Tham số | Loại | Bắt buộc | Mô tả |
|---|---|---|---|
| `userId` | UUID | Có | ID user |
| `roleName` | String | Có | Tên vai trò |

**Response (200):**

```json
{
  "success": true,
  "message": "Role removed successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

**Xóa user:**

```
DELETE /api/admin/users/{userId}
```

**Response (200):**

```json
{
  "success": true,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2026-11-22T17:00:00"
}
```

---

## 5. Mã Lỗi Thường Gặp

| HTTP Status | Mô tả | Nguyên nhân |
|---|---|---|
| `400` | Bad Request | Validation lỗi, tham số sai |
| `401` | Unauthorized | Chưa đăng nhập hoặc token hết hạn |
| `403` | Forbidden | Không có quyền truy cập |
| `404` | Not Found | Tài nguyên không tồn tại |
| `405` | Method Not Allowed | HTTP method không hỗ trợ |
| `409` | Conflict | Dữ liệu trùng lặp (email đã tồn tại) |
| `500` | Internal Server Error | Lỗi server |

---

## 6. Ví dụ gọi API

### Axios

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  withCredentials: true, // Quan trọng: gửi cookie cross-origin
  headers: {
    'Content-Type': 'application/json'
  }
});

// Tự động attach token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Tự động refresh token khi 401
api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        await api.post('/api/auth/refresh');
        return api(originalRequest);
      } catch (refreshError) {
        localStorage.removeItem('accessToken');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

// Đăng nhập
const login = async (email, password) => {
  const response = await api.post('/api/auth/login', { email, password });
  localStorage.setItem('accessToken', response.data.data.accessToken);
  return response.data;
};

// Lấy danh sách sản phẩm
const getProducts = async (categoryId, keyword, cursor, pageSize = 20) => {
  const params = {};
  if (categoryId) params.categoryId = categoryId;
  if (keyword) params.keyword = keyword;
  if (cursor) params.cursor = cursor;
  params.pageSize = pageSize;
  return api.get('/api/products', { params });
};

// Tạo đơn hàng
const createOrder = async (addressId) => {
  return api.post('/api/orders', { addressId });
};

// Tạo thanh toán COD
const createPayment = async (orderId) => {
  return api.post('/api/payments', { orderId, paymentMethod: 'COD' });
};

// Tạo đánh giá với ảnh
const createReview = async (orderItemId, rating, title, comment, imageFile) => {
  const formData = new FormData();
  formData.append('request', new Blob([JSON.stringify({
    orderItemId, rating, title, comment
  })], { type: 'application/json' }));
  if (imageFile) {
    formData.append('image', imageFile);
  }
  return api.post('/api/reviews', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};
```

### Fetch API

```javascript
// Đăng nhập
const login = async (email, password) => {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    credentials: 'include', // Quan trọng: gửi cookie cross-origin
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  const data = await response.json();
  localStorage.setItem('accessToken', data.data.accessToken);
  return data;
};

// Lấy sản phẩm
const getProducts = async () => {
  const token = localStorage.getItem('accessToken');
  const response = await fetch('http://localhost:8080/api/products', {
    credentials: 'include',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};
```

---

## 7. Ghi chú quan trọng

1. **Refresh token** được lưu trong HttpOnly cookie, không có trong response body.
2. Frontend **bắt buộc** phải cấu hình `withCredentials: true` (Axios) hoặc `credentials: 'include'` (Fetch).
3. CORS được cấu hình cho `http://localhost:5173` với credentials.
4. Access token hết hạn sau **1 giờ**, refresh token hết hạn sau **7 ngày**.
5. Khi access token hết hạn, gọi `POST /api/auth/refresh` để lấy token mới.
6. Product **không có** field `price` ở level product — price thuộc về ProductVariant.
7. Khi tạo sản phẩm, field `variants` là **bắt buộc** phải có trong request body.
8. `status` trong variant phải là `AVAILABLE`, `OUT_OF_STOCK`, hoặc `DISCONTINUED` (enum VariantStatus).
9. Payment methods hỗ trợ: `COD`, `ZALOPAY`, `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `E_WALLET`.
10. Bundle APIs (`/api/bundles/**`) **không cần auth** — public access.
11. Review được tạo với status `PENDING` và cần admin duyệt.
12. Swagger UI có sẵn tại `http://localhost:8080/swagger-ui.html`.
