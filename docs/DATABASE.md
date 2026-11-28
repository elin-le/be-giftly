# Database Schema — Giftly

> Generated from JPA entities — source of truth is the Java entity code under `src/main/java/elin/le/giftly/**/entity/`.

---

## Entity-Relationship Diagram

```
user ──┬── address
       ├── cart ── cart_item ── product_variant ── product ── product_image
       ├── gift_box_config ── gift_box_config_item ── product_variant
       └── order ── order_item ── order_item_component
                           ├── payment
                           └── review ── review_image
```

`cart_item` và `gift_box_config` là hai luồng độc lập: hộp custom được đặt trực tiếp từ cấu hình đã lưu, không được gắn vào cart.

---

## Tables

### 1. User & Role Management

#### `user`

Tài khoản người dùng. Mỗi user có một UUID làm primary key, email là unique credential để đăng nhập.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique user identifier |
| `email` | VARCHAR | **NOT NULL**, **UNIQUE** | Email đăng nhập |
| `password` | VARCHAR | **NOT NULL** | BCrypt-hashed password |
| `full_name` | VARCHAR | **NOT NULL** | Họ và tên |
| `phone` | VARCHAR | NULL | Số điện thoại |
| `status` | VARCHAR (enum) | **NOT NULL** | `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `created_at` | TIMESTAMP | **NOT NULL**, auto-generated (`@CreationTimestamp`), updatable=false | Thời gian tạo tài khoản |
| `updated_at` | TIMESTAMP | auto-updated (`@UpdateTimestamp`) | Thời gian cập nhật cuối |

---

#### `role`

Các vai trò trong hệ thống (Spring Security roles).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique role identifier |
| `name` | VARCHAR | **NOT NULL**, **UNIQUE** | Tên role (e.g. `ROLE_USER`, `ROLE_ADMIN`) |

**Default Data** (seeded by `AdminInitializer`):
- `ROLE_USER` — Khách hàng thường
- `ROLE_ADMIN` — Quản trị viên

---

#### `user_role`

Bảng trung gian cho quan hệ N:N giữa user và role.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique identifier |
| `user_id` | UUID | **NOT NULL**, FK → `user(id)` (`@ManyToOne`, LAZY) | Tham chiếu user |
| `role_id` | UUID | **NOT NULL**, FK → `role(id)` (`@ManyToOne`, LAZY) | Tham chiếu role |

---

#### `address`

Địa chỉ giao hàng của người dùng. Một user có thể có nhiều địa chỉ.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique address identifier |
| `user_id` | UUID | **NOT NULL**, FK → `user(id)` | Chủ sở hữu |
| `recipient_name` | VARCHAR | **NOT NULL** | Tên người nhận |
| `phone` | VARCHAR | **NOT NULL** | Số điện thoại liên hệ |
| `address_line` | VARCHAR | **NOT NULL** | Địa chỉ chi tiết (tên đường, số nhà) |
| `ward` | VARCHAR | NULL | Phường/Xã |
| `district` | VARCHAR | NULL | Quận/Huyện |
| `city` | VARCHAR | NULL | Thành phố/Tỉnh |
| `is_default` | BOOLEAN | NULL | Đánh dấu địa chỉ mặc định |

---

### 2. Product Management

#### `category`

Danh mục sản phẩm.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique category identifier |
| `name` | VARCHAR | **NOT NULL** | Tên danh mục |
| `description` | VARCHAR | NULL | Mô tả danh mục |
| `image_public_id` | VARCHAR(255) | NULL | Cloudinary public ID |
| `image_secure_url` | VARCHAR(500) | NULL | Cloudinary HTTPS URL |

Ảnh do FE tải trực tiếp lên Cloudinary; API chỉ nhận và trả metadata `publicId`/`secureUrl`. Migration V24 thêm hai cột ảnh này.

---

#### `product`

Sản phẩm trong catalog. Có thể là sản phẩm thường, hộp quà, giấy gói, ruy băng, hoặc thiệp.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique product identifier |
| `category_id` | UUID | **NOT NULL**, FK → `category(id)` (`@ManyToOne`, LAZY) | Danh mục sản phẩm |
| `name` | VARCHAR | **NOT NULL** | Tên sản phẩm |
| `description` | VARCHAR | NULL | Mô tả sản phẩm |
| `product_type` | VARCHAR (enum) | **NOT NULL** | Loại catalog (`REGULAR`, `READY_MADE_GIFT_BOX`, `PACKAGING_ACCESSORY`, ...) |
| `custom_component_type` | VARCHAR (enum) | NULL | Nhóm lựa chọn custom (`BOX`, `WRAPPING`, `RIBBON`, `FILLER`, `GREETING_CARD`, `ACCESSORY`) |
| `status` | VARCHAR (enum) | **NOT NULL** | `AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED` |

Giá, tồn kho và ảnh có thể khác nhau theo `product_variant`; ảnh cũng có thể gắn trực tiếp với product. Các vật phẩm custom vẫn là product/variant bình thường và tái sử dụng dữ liệu catalog này.

**Quan hệ:**
- `product` (1) → (N) `product_image` — `CascadeType.ALL`, `orphanRemoval=true`

---

#### `product_image`

Hình ảnh sản phẩm (lưu trên Cloudinary).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique image identifier |
| `product_id` | UUID | **NOT NULL**, FK → `product(id)` (`@ManyToOne`, LAZY) | Sản phẩm cha |
| `image_url` | VARCHAR | **NOT NULL** | URL công khai của ảnh |
| `secure_url` | VARCHAR | **NOT NULL** | URL HTTPS của ảnh |
| `public_id` | VARCHAR | **NOT NULL** | Cloudinary public ID (dùng để xóa/cập nhật) |
| `is_thumbnail` | BOOLEAN | NULL | Đánh dấu ảnh thumbnail |

---

### 3. Gift Box Customization

#### `gift_box_config`

Cấu hình hộp quà tùy chỉnh do người dùng tạo và mua độc lập với cart. Thành phần hộp, giấy gói, ruy-băng, thiệp và các lựa chọn cùng loại được lưu ở `gift_box_config_item` như variant product; nội dung hộp cũng ở bảng này nhưng không có `component_type`.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique config identifier |
| `user_id` | UUID | **NOT NULL**, FK → `user(id)` | Người tạo config |
| `created_at` | TIMESTAMP | **NOT NULL**, auto-generated (`@CreationTimestamp`), updatable=false | Thời gian tạo |

Các trường `updated_at`, `review_status`, `review_note`, `capacity_ratio` được dùng cho kiểm duyệt và đánh giá cấu hình. Admin có thể quản lý yêu cầu qua `/api/admin/gift-box-requests`; xóa bị chặn nếu cấu hình đã được dùng trong order item.
`box_product_id` là cột legacy không còn được ứng dụng sử dụng; V23 cho phép cột này nhận `NULL` vì hộp được lưu bằng variant trong `gift_box_config_item`.

**Quan hệ:**
- `gift_box_config` (1) → (N) `gift_box_config_item` — `CascadeType.ALL`, `orphanRemoval=true`

---

#### `gift_box_config_item`

Các lựa chọn thành phần và sản phẩm bên trong một cấu hình.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique item identifier |
| `config_id` | UUID | **NOT NULL**, FK → `gift_box_config(id)` (`@ManyToOne`, LAZY) | Config cha |
| `product_variant_id` | UUID | **NOT NULL**, FK → `product_variant(id)` | Product variant được chọn |
| `component_type` | VARCHAR (enum) | NULL | Nhóm custom; NULL nghĩa là món bên trong hộp |
| `quantity` | INTEGER | **NOT NULL** | Thành phần custom luôn bằng 1; món bên trong có số lượng tùy chọn |

**Business rule:** `UNIQUE (config_id, component_type)` giới hạn một lựa chọn cho mỗi nhóm trên một cấu hình; nhiều dòng nội dung được phép vì `component_type` là NULL. `BOX` là bắt buộc và được lưu như một dòng thành phần, không còn cột riêng trên `gift_box_config`.

---

### 4. Shopping Cart

#### `cart`

Giỏ hàng của người dùng.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique cart identifier |
| `user_id` | UUID | **NOT NULL**, FK → `user(id)` | Chủ sở hữu |
| `updated_at` | TIMESTAMP | auto-updated (`@UpdateTimestamp`) | Thời gian cập nhật cuối |

**Quan hệ:**
- `cart` (1) → (N) `cart_item` — `CascadeType.ALL`, `orphanRemoval=true`

---

#### `cart_item`

Mục trong giỏ hàng — chỉ đại diện một product variant thường; hộp custom không nằm trong cart.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique item identifier |
| `cart_id` | UUID | **NOT NULL**, FK → `cart(id)` (`@ManyToOne`, LAZY) | Giỏ hàng cha |
| `product_variant_id` | UUID | CHECK `IS NOT NULL` | ID variant của sản phẩm |
| `product_id` | UUID | NULL, legacy | Cột cũ không còn được dùng cho cart item mới |
| `quantity` | INTEGER | **NOT NULL** | Số lượng |

**Business rule:** `cart_item` chỉ chứa một `product_variant_id`. Migration V22 xóa các liên kết hộp custom cũ khỏi cart (không xóa cấu hình hộp) và loại bỏ cột `gift_box_config_id`. Hộp custom được đặt qua `POST /api/gift-boxes/{giftBoxId}/purchase`.

---

### 5. Order Management

#### `order`

Đơn hàng của khách hàng.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique order identifier |
| `user_id` | UUID | **NOT NULL**, FK → `user(id)` | Người đặt hàng |
| `address_id` | UUID | **NOT NULL**, FK → `address(id)` (`@ManyToOne`, LAZY) | Địa chỉ giao hàng |
| `order_number` | VARCHAR | **NOT NULL**, **UNIQUE** | Mã đơn hàng (human-readable) |
| `subtotal` | NUMERIC(19,2) | **NOT NULL** | Tổng giá sản phẩm (chưa bao gồm phí ship) |
| `shipping_fee` | NUMERIC(19,2) | **NOT NULL** | Phí vận chuyển |
| `total_amount` | NUMERIC(19,2) | **NOT NULL** | Tổng thanh toán = subtotal + shipping_fee |
| `status` | VARCHAR (enum) | **NOT NULL** | Xem phần [Order Status](#order-status-orderstatus) |
| `ordered_at` | TIMESTAMP | **NOT NULL** | Thời gian đặt hàng |

**Quan hệ:**
- `order` (1) → (N) `order_item` — `CascadeType.ALL`, `orphanRemoval=true`

---

#### `order_item`

Mục trong đơn hàng — snapshot tại thời điểm đặt hàng (tên, giá được lưu lại).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique item identifier |
| `order_id` | UUID | **NOT NULL**, FK → `order(id)` (`@ManyToOne`, LAZY) | Đơn hàng cha |
| `product_variant_id` | UUID | NULL, FK → `product_variant(id)` | Variant nếu dòng hàng là sản phẩm thường |
| `gift_box_config_id` | UUID | NULL, FK → `gift_box_config(id)` | Cấu hình nếu dòng hàng là hộp quà custom |
| `product_id` | UUID | NULL, legacy | Cột cũ, không dùng khi tạo order item mới |
| `item_name_snapshot` | VARCHAR(255) | **NOT NULL** | Tên sản phẩm/hộp tại thời điểm đặt hàng |
| `sku_snapshot` | VARCHAR(255) | NULL | SKU tại thời điểm đặt hàng |
| `item_name` | VARCHAR | NULL, legacy | Tên cũ; nullable từ migration V16 |
| `unit_price` | NUMERIC(19,2) | **NOT NULL** | Giá đơn vị tại thời điểm đặt |
| `quantity` | INTEGER | **NOT NULL** | Số lượng |
| `subtotal` | NUMERIC(19,2) | **NOT NULL** | Tổng = unit_price × quantity |

**Quan hệ:**
- `order_item` (1) → (N) `order_item_component` — `CascadeType.ALL`, `orphanRemoval=true`

---

#### `order_item_component`

Các thành phần chi tiết của một gift box order item (hộp, giấy gói, ruy băng, thiệp, sản phẩm bên trong).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique component identifier |
| `order_item_id` | UUID | **NOT NULL**, FK → `order_item(id)` (`@ManyToOne`, LAZY) | Order item cha |
| `product_variant_id` | UUID | NULL | Variant thành phần |
| `component_role` | VARCHAR(20) | NULL | Vai trò snapshot: hộp, sản phẩm, phụ kiện hoặc thiệp |
| `product_name_snapshot` | VARCHAR(255) | NULL | Tên thành phần tại lúc đặt hàng |
| `sku_snapshot` | VARCHAR(255) | NULL | SKU thành phần tại lúc đặt hàng |
| `variant_snapshot` | JSON | NULL | Snapshot thuộc tính variant |
| `quantity` | INTEGER | NULL | Số lượng thành phần cho mỗi hộp |
| `unit_price` | NUMERIC(19,2) | NULL | Giá thành phần tại thời điểm đặt |
| `is_complimentary` | BOOLEAN | NULL | Thành phần có được miễn phí không |
| `message_text`, `message_rendering_method` | VARCHAR | NULL | Nội dung và cách in lời nhắn trên thiệp |
| `is_perishable_snapshot`, `shelf_life_hours_snapshot` | BOOLEAN / INTEGER | NULL | Snapshot hạn sử dụng |
| `return_policy_snapshot` | VARCHAR | NULL | Snapshot chính sách đổi trả |
| `advance_payment_percent_snapshot` | NUMERIC(5,2) | NULL | Snapshot tỷ lệ đặt cọc |
| `component_name`, `component_type` | VARCHAR | NULL | Cột legacy, nullable từ migration V21 |

Khi hủy đơn, tồn kho các thành phần được hoàn lại theo số lượng trong hộp nhân số hộp đã đặt.

---

### 6. Payment

#### `payment`

Ghi nhận thanh toán cho đơn hàng.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique payment identifier |
| `order_id` | UUID | **NOT NULL**, FK → `order(id)` (`@ManyToOne`, LAZY) | Đơn hàng liên quan |
| `payment_method` | VARCHAR (enum) | **NOT NULL** | Xem phần [Payment Method](#payment-method-paymentmethod) |
| `payment_status` | VARCHAR (enum) | **NOT NULL** | Xem phần [Payment Status](#payment-status-paymentstatus) |
| `amount` | NUMERIC(19,2) | **NOT NULL** | Số tiền thanh toán |
| `paid_at` | TIMESTAMP | NULL | Thời gian thanh toán thành công |

V24 thêm `CHECK (payment_method IN ('COD', 'ZALOPAY'))`; dữ liệu legacy có phương thức khác được chuẩn hóa về `COD` trước khi áp dụng constraint.

---

### 7. Reviews

#### `review`

Đánh giá sản phẩm của người dùng. Mỗi `order_item` chỉ được đánh giá **một lần** (unique constraint).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique review identifier |
| `user_id` | UUID | **NOT NULL**, FK → `user(id)` | Người đánh giá |
| `product_id` | UUID | **NOT NULL**, FK → `product(id)` (`@ManyToOne`, LAZY) | Sản phẩm được đánh giá |
| `order_item_id` | UUID | **NOT NULL**, FK → `order_item(id)` (`@ManyToOne`, LAZY) | Order item liên quan |
| `rating` | SMALLINT | **NOT NULL** | Điểm đánh giá (1–5) |
| `title` | VARCHAR(255) | NULL | Tiêu đề đánh giá |
| `comment` | VARCHAR | NULL | Nội dung bình luận |
| `status` | VARCHAR (enum) | **NOT NULL** | `PENDING`, `APPROVED`, `REJECTED` |
| `created_at` | TIMESTAMP | **NOT NULL**, auto-generated (`@CreationTimestamp`), updatable=false | Thời gian tạo |
| `updated_at` | TIMESTAMP | auto-updated (`@UpdateTimestamp`) | Thời gian cập nhật cuối |

**Constraints:**
- **UNIQUE** (`order_item_id`) — mỗi order_item chỉ có một review

**Quan hệ:**
- `review` (1) → (N) `review_image` — `CascadeType.ALL`, `orphanRemoval=true`; tối đa 3 ảnh được giới hạn ở service.

---

#### `review_image`

Hình ảnh đính kèm review.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | UUID | **PK**, `GenerationType.UUID` | Unique image identifier |
| `review_id` | UUID | **NOT NULL**, FK → `review(id)` (`@ManyToOne`, LAZY) | Review cha |
| `secure_url` | VARCHAR | **NOT NULL** | URL HTTPS của ảnh trên Cloudinary |
| `public_id` | VARCHAR | NULL | Cloudinary public ID |
| `created_at` | TIMESTAMP | **NOT NULL**, auto-generated (`@CreationTimestamp`), updatable=false | Thời gian tạo |

Migration V20 gộp giá trị URL cũ còn thiếu sang `secure_url`, sau đó loại bỏ cột trùng `image_url`.

---

## Enum Values

### User Status (`UserStatus`)

| Value | Description |
|-------|-------------|
| `ACTIVE` | Tài khoản hoạt động bình thường |
| `INACTIVE` | Tài khoản không hoạt động |
| `SUSPENDED` | Tài khoản bị tạm khóa |

### Product Type (`ProductType`)

| Value | Description |
|-------|-------------|
| `REGULAR` | Sản phẩm thông thường |
| `GIFT_BOX` | Hộp quà (container) |
| `WRAPPING` | Giấy gói quà |
| `RIBBON` | Ruy băng |
| `GREETING_CARD` | Thiệp chúc mừng |

### Product Status (`ProductStatus`)

| Value | Description |
|-------|-------------|
| `AVAILABLE` | Còn hàng, có thể mua |
| `OUT_OF_STOCK` | Hết hàng tạm thời |
| `DISCONTINUED` | Đã ngừng kinh doanh |

### Order Status (`OrderStatus`)

| Value | Description |
|-------|-------------|
| `PENDING_CONFIRMATION` | Chờ xác nhận |
| `CONFIRMED` | Đã xác nhận |
| `PROCESSING` | Đang xử lý |
| `PACKAGING` | Đang đóng gói |
| `READY_TO_SHIP` | Sẵn sàng giao |
| `SHIPPING` | Đang vận chuyển |
| `DELIVERED` | Đã giao đến tay khách |
| `COMPLETED` | Hoàn thành |
| `CANCELLED` | Đã hủy |
| `RETURN_REQUESTED` | Khách yêu cầu trả hàng |
| `RETURNED` | Đã trả hàng |
| `FAILED` | Thất bại |

### Payment Method (`PaymentMethod`)

| Value | Description |
|-------|-------------|
| `COD` | Thanh toán khi nhận hàng |
| `ZALOPAY` | ZaloPay |

### Payment Status (`PaymentStatus`)

| Value | Description |
|-------|-------------|
| `PENDING` | Chờ thanh toán |
| `PAID` | Đã thanh toán |
| `FAILED` | Thanh toán thất bại |
| `REFUNDED` | Đã hoàn tiền |

### Review Status (`ReviewStatus`)

| Value | Description |
|-------|-------------|
| `PENDING` | Chờ admin duyệt |
| `APPROVED` | Admin đã duyệt |
| `REJECTED` | Admin đã từ chối |

---

## Key Relationships

```
user (1) ──── (N) address
user (1) ──── (N) cart
cart (1) ──── (N) cart_item
user (1) ──── (N) order
order (1) ──── (N) order_item
order_item (1) ──── (N) order_item_component
order (1) ──── (N) payment
user (1) ──── (N) review
review (1) ──── (1) review_image
order_item (1) ──── (1) review  (unique constraint)
user (N) ──── (N) role  (via user_role)

category (1) ──── (N) product
product (1) ──── (N) product_image

gift_box_config (1) ──── (N) gift_box_item
gift_box_config (1) ──── (N) order_item (hộp custom được đặt độc lập)
```

---

## Cascade & Orphan Removal

| Parent | Child | Cascade | Orphan Removal |
|--------|-------|---------|----------------|
| `product` | `product_image` | `ALL` | ✓ |
| `cart` | `cart_item` | `ALL` | ✓ |
| `gift_box_config` | `gift_box_item` | `ALL` | ✓ |
| `order` | `order_item` | `ALL` | ✓ |
| `order_item` | `order_item_component` | `ALL` | ✓ |
| `review` | `review_image` | `ALL` | ✓ |

---

## Summary

| # | Table | Entity Class | Primary Key | FKs | Unique Constraints |
|---|-------|-------------|-------------|-----|-------------------|
| 1 | `user` | `User` | `id` (UUID) | — | `email` |
| 2 | `role` | `Role` | `id` (UUID) | — | `name` |
| 3 | `user_role` | `UserRole` | `id` (UUID) | `user_id`, `role_id` | — |
| 4 | `address` | `Address` | `id` (UUID) | `user_id` | — |
| 5 | `category` | `Category` | `id` (UUID) | — | — |
| 6 | `product` | `Product` | `id` (UUID) | `category_id` | — |
| 7 | `product_image` | `ProductImage` | `id` (UUID) | `product_id` | — |
| 8 | `gift_box_config` | `GiftBoxConfig` | `id` (UUID) | `box_product_id`, `wrapping_product_id`, `ribbon_product_id`, `card_product_id` | — |
| 9 | `gift_box_item` | `GiftBoxItem` | `id` (UUID) | `config_id`, `product_id` | — |
| 10 | `cart` | `Cart` | `id` (UUID) | `user_id` | — |
| 11 | `cart_item` | `CartItem` | `id` (UUID) | `cart_id` | — |
| 12 | `order` | `Order` | `id` (UUID) | `user_id`, `address_id` | `order_number` |
| 13 | `order_item` | `OrderItem` | `id` (UUID) | `order_id`, `product_id`, `gift_box_config_id` | — |
| 14 | `order_item_component` | `OrderItemComponent` | `id` (UUID) | `order_item_id`, `product_id` | — |
| 15 | `payment` | `Payment` | `id` (UUID) | `order_id` | — |
| 16 | `review` | `Review` | `id` (UUID) | `user_id`, `product_id`, `order_item_id` | `order_item_id` |
| 17 | `review_image` | `ReviewImage` | `id` (UUID) | `review_id` | — |

**Tổng cộng: 17 bảng, tất cả dùng UUID làm primary key.**
