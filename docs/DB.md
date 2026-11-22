# Giftly Database Schema

## Tổng quan

Database sử dụng **PostgreSQL 15+** với **27 bảng** chính, chia thành các module:

> **Primary keys:** UUID · **Naming:** `snake_case`
> **Companion file:** `Giftly_Database.sql` (tạo schema mới, không chạy trên database đang có dữ liệu)

| Module | Bảng |
|--------|------|
| User & Auth | `user`, `role`, `user_role`, `address` |
| Catalog | `category`, `product`, `product_image` |
| Options & Variants | `product_option`, `product_option_value`, `product_variant`, `product_variant_option_value` |
| Variant Details | `gift_box_variant_detail`, `product_variant_policy` |
| Ready-made Gift Set (Bundle) | `bundle_component`, `bundle_option_group`, `bundle_option` |
| Custom Gift Box | `gift_box_config`, `gift_box_config_item` |
| Cart | `cart`, `cart_item`, `cart_item_option_selection` |
| Order | `order`, `order_item`, `order_item_component` |
| Payment | `payment` |
| Review | `review`, `review_image` |

**Tổng cộng 27 bảng** (tính cả user, role, user_role, address, category, product, product_image, product_option, product_option_value, product_variant, product_variant_option_value, gift_box_variant_detail, product_variant_policy, bundle_component, bundle_option_group, bundle_option, gift_box_config, gift_box_config_item, cart, cart_item, cart_item_option_selection, order, order_item, order_item_component, payment, review, review_image).

---

## 1. User & Auth Module

### `user`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID người dùng |
| `email` | VARCHAR(255) | NOT NULL, UNIQUE | Email đăng nhập |
| `password` | VARCHAR(255) | NOT NULL | Mật khẩu đã hash |
| `full_name` | VARCHAR(255) | NOT NULL | Họ tên |
| `phone` | VARCHAR(50) | NULLABLE | Số điện thoại |
| `status` | VARCHAR(20) | NOT NULL, ENUM(UserStatus) | Trạng thái: `ACTIVE`, `INACTIVE`, `SUSPENDED` |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | NULLABLE | Thời gian cập nhật |

**Indexes:**
- `idx_user_email` ON `user`(email)

---

### `role`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID vai trò |
| `name` | VARCHAR(50) | NOT NULL, UNIQUE | Tên vai trò: `ROLE_USER`, `ROLE_ADMIN` |

---

### `user_role`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID |
| `user_id` | UUID | NOT NULL, **FK** → `user(id)` | Người dùng |
| `role_id` | UUID | NOT NULL, **FK** → `role(id)` | Vai trò |

**Constraints:**
- `uk_user_role` UNIQUE (user_id, role_id) — Một user không thể có cùng role nhiều lần

---

### `address`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID địa chỉ |
| `user_id` | UUID | NOT NULL, **FK** → `user(id)` | Người sở hữu |
| `recipient_name` | VARCHAR(255) | NOT NULL | Tên người nhận |
| `phone` | VARCHAR(50) | NOT NULL | Số điện thoại người nhận |
| `address_line` | VARCHAR(500) | NOT NULL | Địa chỉ chi tiết |
| `ward` | VARCHAR(100) | NULLABLE | Phường/Xã |
| `district` | VARCHAR(100) | NULLABLE | Quận/Huyện |
| `city` | VARCHAR(100) | NULLABLE | Thành phố |
| `is_default` | BOOLEAN | DEFAULT FALSE | Địa chỉ mặc định |

**Indexes:**
- `idx_address_user` ON `address`(user_id)

---

## 2. Catalog Module

### `category`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID danh mục |
| `name` | VARCHAR(255) | NOT NULL | Tên danh mục |
| `description` | TEXT | NULLABLE | Mô tả danh mục |

---

### `product`

Lưu trữ thông tin danh mục chung: `id`, `category_id`, `name`, `description`, `product_type`, `status`, `is_custom_box_eligible`, timestamps. **Price và stock không lưu ở đây**, đã chuyển sang `product_variant`.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID sản phẩm |
| `category_id` | UUID | NOT NULL, **FK** → `category(id)` | Danh mục |
| `name` | VARCHAR(255) | NOT NULL | Tên sản phẩm |
| `description` | TEXT | NULLABLE | Mô tả sản phẩm |
| `product_type` | VARCHAR(20) | NOT NULL, ENUM(ProductType) | Loại sản phẩm |
| `status` | VARCHAR(20) | NOT NULL, ENUM(ProductStatus) | Trạng thái: `AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED` |
| `is_custom_box_eligible` | BOOLEAN | DEFAULT FALSE | Có được chọn trong Custom Box không |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | NULLABLE | Thời gian cập nhật |

**ProductType enum:**

| Type | Ý nghĩa | Bán lẻ | Custom Box |
|------|---------|--------|------------|
| `REGULAR` | Sản phẩm lẻ | Có | Nếu `is_custom_box_eligible = true` |
| `READY_MADE_GIFT_SET` | Set quà hoàn chỉnh | Có | **Không** |
| `READY_MADE_GIFT_BOX` | Hộp quà bán riêng | Có | Có |
| `PACKAGING_ACCESSORY` | Phụ kiện đóng gói | Có | Có, **miễn phí** |
| `GREETING_CARD` | Thiệp chúc mừng | Có | Có, miễn phí hoặc tính phí theo cấu hình |

> `is_custom_box_eligible` do admin kiểm soát, không suy ra từ category hay tên sản phẩm.

**Indexes:**
- `idx_product_category` ON `product`(category_id)
- `idx_product_status` ON `product`(status)
- `idx_product_type` ON `product`(product_type)
- `idx_product_name_trgm` ON `product` USING GIN (name gin_trgm_ops) — Tìm kiếm mờ
- `idx_product_description_trgm` ON `product` USING GIN (description gin_trgm_ops) — Tìm kiếm mờ
- `idx_product_custom_eligible` ON `product`(is_custom_box_eligible)


---

### `product_image`

Mỗi ảnh thuộc về một Variant cụ thể. `product_variant_id` là **NOT NULL**. Không lưu `product_id`; Product cha có thể truy cập qua `product_variant.product_id`.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID ảnh |
| `product_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant của ảnh |
| `image_url` | VARCHAR(500) | NOT NULL | URL ảnh công khai |
| `secure_url` | VARCHAR(500) | NOT NULL | URL ảnh bảo mật |
| `public_id` | VARCHAR(255) | NOT NULL | Public ID (Cloudinary) |
| `is_primary` | BOOLEAN | NULLABLE | Ảnh chính của Variant |
| `display_order` | INTEGER | NULLABLE | Thứ tự hiển thị |

> Mỗi Variant có tối đa một ảnh chính, enforced bằng partial unique index.

**Indexes:**
- `idx_product_image_variant` ON `product_image`(product_variant_id)
- `uk_product_image_primary_variant` ON `product_image`(product_variant_id) WHERE is_primary = TRUE — Partial unique index

---

## 3. Options & Variants Module

### `product_option` / `product_option_value`

Mỗi Product tự định nghĩa các options của mình. Ví dụ: Teddy Bear có Color và Size; một chiếc ly có thể chỉ có Color. Option values là các hàng, không phải các cột hardcode. `color_hex` là metadata hiển thị tùy chọn.

### `product_option`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID tùy chọn |
| `product_id` | UUID | NOT NULL, **FK** → `product(id)` | Sản phẩm cha |
| `name` | VARCHAR(255) | NOT NULL | Tên tùy chọn (ví dụ: "Color", "Size") |
| `display_order` | INTEGER | NULLABLE | Thứ tự hiển thị |

**Indexes:**
- `idx_product_option_product` ON `product_option`(product_id)

---

### `product_option_value`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID giá trị tùy chọn |
| `option_id` | UUID | NOT NULL, **FK** → `product_option(id)` | Tùy chọn cha |
| `value` | VARCHAR(255) | NOT NULL | Giá trị (ví dụ: "Pink", "S", "M") |
| `color_hex` | VARCHAR(7) | NULLABLE | Mã màu HEX (ví dụ: "#FF69B4") |

**Indexes:**
- `idx_product_option_value_option` ON `product_option_value`(option_id)

---

### `product_variant`

Mỗi tổ hợp có thể mua có SKU, price, stock, dimensions, weight và status riêng. Bảng `product_variant_option_value` ghi nhận các option values mà mỗi variant chọn.

> **Backend validation:** Mỗi option value được chọn phải thuộc một option của cùng Product cha. SKU phải duy nhất.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID phiên bản |
| `product_id` | UUID | NOT NULL, **FK** → `product(id)` | Sản phẩm cha |
| `sku` | VARCHAR(255) | NOT NULL, UNIQUE | Mã SKU duy nhất |
| `price` | NUMERIC(19,2) | NOT NULL | Giá bán |
| `stock_quantity` | INTEGER | NOT NULL, DEFAULT 0 | Số lượng tồn kho |
| `length` | DOUBLE PRECISION | NULLABLE | Chiều dài vật lý **bên ngoài** |
| `width` | DOUBLE PRECISION | NULLABLE | Chiều rộng vật lý **bên ngoài** |
| `height` | DOUBLE PRECISION | NULLABLE | Chiều cao vật lý **bên ngoài** |
| `dimension_unit` | VARCHAR(10) | NULLABLE, ENUM(DimensionUnit) | Đơn vị kích thước: `MM`, `CM`, `M` |
| `weight` | DOUBLE PRECISION | NULLABLE | Trọng lượng |
| `weight_unit` | VARCHAR(10) | NULLABLE, ENUM(WeightUnit) | Đơn vị trọng lượng: `G`, `KG` |
| `status` | VARCHAR(20) | NOT NULL, ENUM(VariantStatus) | Trạng thái Variant: `AVAILABLE`, `OUT_OF_STOCK`, `DISCONTINUED` |

**Indexes:**
- `idx_product_variant_product` ON `product_variant`(product_id)
- `idx_product_variant_sku` ON `product_variant`(sku)
- `idx_product_variant_status` ON `product_variant`(status)

**One-to-One relationships:**
- `gift_box_variant_detail` — Chi tiết hộp quà (kích thước lòng, tải trọng)
- `product_variant_policy` — Chính sách (hạn sử dụng, trả hàng, đặt cọc)

### Dimensions

- `product_variant.length/width/height`: Kích thước vật lý **bên ngoài** của mặt hàng bán được.
- `gift_box_variant_detail.inner_length/inner_width/inner_height`: Kích thước lòng **bên trong** của Ready-made Gift Box, plus tải trọng tối đa.

> Kiểm tra kích thước/trọng lượng đơn giản là xấp xỉ ban đầu. Thể tích không chứng minh các vật không đều thực sự vừa. Trong v1, FE có thể từ chối cấu hình khi tổng thể tích hoặc tổng trọng lượng vượt quá giới hạn hộp, nhưng vượt qua kiểm tra đó không phải là bằng chứng rằng nội dung có thể được đóng gói. Backend phải tự thực hiện validation và không coi kiểm tra thể tích/trọng lượng là đảm bảo hoàn toàn. Kiểm tra 3D placement/orientation chính xác là khả năng riêng trừ khi được triển khai rõ ràng.

---

### `product_variant_option_value`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID |
| `variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Phiên bản |
| `option_value_id` | UUID | NOT NULL, **FK** → `product_option_value(id)` | Giá trị tùy chọn |

**Indexes:**
- `idx_product_variant_option_value_variant` ON `product_variant_option_value`(variant_id)

---

## 4. Variant Details Module

### `gift_box_variant_detail`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID |
| `product_variant_id` | UUID | NOT NULL, UNIQUE, **FK** → `product_variant(id)` | Variant hộp quà |
| `inner_length` | DOUBLE PRECISION | NULLABLE | Chiều dài lòng hộp (kích thước **bên trong**) |
| `inner_width` | DOUBLE PRECISION | NULLABLE | Chiều rộng lòng hộp (kích thước **bên trong**) |
| `inner_height` | DOUBLE PRECISION | NULLABLE | Chiều cao lòng hộp (kích thước **bên trong**) |
| `max_weight` | NUMERIC(19,2) | NULLABLE | Tải trọng tối đa |
| `dimension_unit` | VARCHAR(10) | NULLABLE, ENUM(DimensionUnit) | Đơn vị kích thước: `MM`, `CM`, `M` |
| `weight_unit` | VARCHAR(10) | NULLABLE, ENUM(WeightUnit) | Đơn vị trọng lượng: `G`, `KG` |

> Chỉ tồn tại cho Variant thuộc Product loại `READY_MADE_GIFT_BOX`. Kích thước lòng hộp khác với kích thước bên ngoài của Variant.

---

### `product_variant_policy`

Lưu chính sách cho mỗi Variant có thể bán: hoa và trái cây tươi có thể cấu hình là perishable, không trả hàng và yêu cầu đặt cọc 50%. Đây là chính sách do admin cấu hình, không phải quy tắc suy ra từ tên sản phẩm.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID |
| `product_variant_id` | UUID | NOT NULL, UNIQUE, **FK** → `product_variant(id)` | Variant áp dụng |
| `is_perishable` | BOOLEAN | DEFAULT FALSE | Có dễ hỏng không |
| `shelf_life_hours` | INTEGER | NULLABLE | Thời gian sử dụng dự kiến (giờ) |
| `return_policy` | VARCHAR(20) | NULLABLE, ENUM(ReturnPolicy) | Chính sách: `RETURNABLE`, `NO_RETURN` |
| `advance_payment_percent` | NUMERIC(5,2) | NULLABLE | Tỷ lệ đặt cọc (%) |

**Constraints:**
- `chk_advance_payment` CHECK (advance_payment_percent IS NULL OR (advance_payment_percent BETWEEN 0 AND 100))
- `chk_perishable_policy` CHECK (NOT is_perishable OR (return_policy = 'NO_RETURN' AND advance_payment_percent >= 50)) — Variant dễ hỏng phải `NO_RETURN` và đặt cọc ít nhất 50%

**ReturnPolicy enum:**
- `RETURNABLE` — Sản phẩm có thể trả hàng
- `NO_RETURN` — Sản phẩm không thể trả hàng (ví dụ: hàng dễ hỏng)

### Composite Policy (quy tắc tổng hợp v1)

- Nếu bất kỳ thành phần nào của Gift Set hoặc Custom Box là `NO_RETURN`, đánh dấu toàn bộ dòng đơn `NO_RETURN`.
- Nếu bất kỳ thành phần nào yêu cầu đặt cọc, áp dụng tỷ lệ **cao nhất** cho toàn bộ dòng tổng hợp.
- Ví dụ: Custom Box 1.000.000 VND có thành phần yêu cầu đặt cọc 50% → cần thanh toán trước 500.000 VND.

> Các quy tắc tổng hợp này được thực hiện bởi checkout service. Schema lưu các snapshot và số tiền tính toán; không tự động tính toán.

---

## 5. Bundle (Ready-made Gift Set) Module

Một Gift Set là Product với `product_type = READY_MADE_GIFT_SET`. Mỗi Variant của Gift Set có cấu trúc thành phần riêng.

### `bundle_component`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID thành phần |
| `bundle_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant của Gift Set |
| `component_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant thành phần |
| `component_role` | VARCHAR(20) | NOT NULL, ENUM(ComponentRole) | Vai trò thành phần |
| `quantity` | INTEGER | NOT NULL | Số lượng |
| `is_required` | BOOLEAN | NOT NULL, DEFAULT TRUE | Bắt buộc hay không |

**ComponentRole enum:**
- `BOX` — Hộp chứa
- `PRODUCT` — Sản phẩm trong set
- `ACCESSORY` — Phụ kiện đóng gói
- `GREETING_CARD` — Thiệp

**Indexes:**
- `idx_bundle_component_bundle` ON `bundle_component`(bundle_variant_id)

---

### `bundle_option_group`

Định nghĩa các nhóm có thể chọn như Greeting Card hoặc Packaging Accessory. Lưu `selection_type`, min/max selections, required flag và `display_order`.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID nhóm tùy chọn |
| `bundle_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant của Gift Set |
| `name` | VARCHAR(255) | NOT NULL | Tên nhóm (ví dụ: "Greeting Card") |
| `group_type` | VARCHAR(20) | NOT NULL, ENUM(GroupType) | Loại nhóm |
| `selection_type` | VARCHAR(20) | NOT NULL, ENUM(SelectionType) | `SINGLE` hoặc `MULTIPLE` |
| `min_selections` | INTEGER | NULLABLE | Số lựa chọn tối thiểu |
| `max_selections` | INTEGER | NULLABLE | Số lựa chọn tối đa |
| `is_required` | BOOLEAN | DEFAULT FALSE | Bắt buộc chọn không |
| `display_order` | INTEGER | NULLABLE | Thứ tự hiển thị |

**GroupType enum:**
- `GREETING_CARD` — Nhóm chọn thiệp
- `PACKAGING_ACCESSORY` — Nhóm chọn phụ kiện
- `OTHER` — Nhóm tùy chỉnh khác

**SelectionType enum:**
- `SINGLE` — Chọn một
- `MULTIPLE` — Chọn nhiều

**Indexes:**
- `idx_bundle_option_group_bundle` ON `bundle_option_group`(bundle_variant_id)

---

### `bundle_option`

Liên kết một Variant được phép với một option group và định nghĩa cách tính giá.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID tùy chọn |
| `option_group_id` | UUID | NOT NULL, **FK** → `bundle_option_group(id)` | Nhóm cha |
| `product_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant sản phẩm |
| `pricing_mode` | VARCHAR(20) | NOT NULL, ENUM(PricingMode) | Cách tính giá |
| `fixed_price` | NUMERIC(19,2) | NULLABLE | Giá cố định (nếu pricing_mode = FIXED_PRICE) |

**PricingMode enum:**

| `pricing_mode` | Phí khách hàng |
|---|---|
| `FREE` | 0 phí thêm |
| `PRODUCT_PRICE` | Giá bán hiện tại của Variant được chọn |
| `FIXED_PRICE` | `fixed_price` đã cấu hình |

Một Gift Set có thể cung cấp lựa chọn thiệp miễn phí và thiệp cao cấp tính phí. Quy tắc nhóm lựa chọn xác định khách chọn một thiệp hay nhiều thiệp. Service phải thực thi min/max selections.

**Indexes:**
- `idx_bundle_option_group` ON `bundle_option`(option_group_id)

### Bundle Inventory (Tồn kho Bundle)

Đối với set làm sẵn, tồn kho của Set Variant đại diện cho tồn kho set đã lắp ráp. Không trừ tồn kho thành phần cố định thêm lần nữa nếu nó đã được tiêu thụ khi lắp ráp. Các tùy chọn bổ sung (ví dụ: thiệp được chọn) vẫn tiêu thụ tồn kho riêng. Chọn một mô hình sở hữu tồn kho và áp dụng nhất quán.

---

## 6. Custom Gift Box Module

`gift_box_config` thuộc về một user và tham chiếu `box_variant_id`, phải là Variant của Product loại `READY_MADE_GIFT_BOX`. `gift_box_config_item` lưu các Product Variant được chọn, số lượng và dữ liệu thông điệp thiệp tùy chọn cho từng mục cấu hình.

### `gift_box_config`

Thuộc về một user, tham chiếu `box_variant_id` (phải là Variant của `READY_MADE_GIFT_BOX`). Lưu trạng thái phê duyệt của admin và ước lượng dung tích.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID cấu hình |
| `user_id` | UUID | NOT NULL, **FK** → `user(id)` | Người tạo |
| `box_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant hộp được chọn |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian cập nhật |
| `review_status` | VARCHAR(20) | NOT NULL, ENUM(GiftBoxReviewStatus), DEFAULT 'PENDING' | Trạng thái phê duyệt |
| `review_note` | TEXT | NULLABLE | Ghi chú phê duyệt của admin |
| `capacity_ratio` | NUMERIC(5,2) | NULLABLE | Tỷ lệ dung tích ước lượng (%) |

**GiftBoxReviewStatus enum:**
- `PENDING` — Chờ admin xem xét (mặc định)
- `APPROVED` — Admin chấp nhận
- `REJECTED` — Admin từ chối
- `REVISION_REQUIRED` — Khách cần sửa cấu hình

**Constraints:**
- `chk_capacity_ratio` CHECK (capacity_ratio IS NULL OR (capacity_ratio >= 0 AND capacity_ratio <= 100))

> Khi khách thay đổi hộp, mục hoặc số lượng sau khi được phê duyệt, backend phải đặt lại `review_status` về `PENDING` và xóa ghi chú cũ. `capacity_ratio` được tính lại khi nội dung thay đổi.

**Indexes:**
- `idx_gift_box_config_variant` ON `gift_box_config`(box_variant_id)
- `idx_gift_box_config_review_status` ON `gift_box_config`(review_status)

---

### `gift_box_config_item`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID mục |
| `config_id` | UUID | NOT NULL, **FK** → `gift_box_config(id)` | Cấu hình cha |
| `product_variant_id` | UUID | NOT NULL, **FK** → `product_variant(id)` | Variant sản phẩm thêm vào |
| `quantity` | INTEGER | NOT NULL | Số lượng |
| `message_text` | TEXT | NULLABLE | Nội dung thiệp liên quan đến mục này |
| `message_rendering_method` | VARCHAR(20) | NULLABLE, ENUM(MessageRenderingMethod) | `HANDWRITTEN` hoặc `PRINTED` |

> Các trường message là nullable vì không phải mục nào cũng là thiệp và thông điệp có thể bị bỏ qua. Khi mục là thiệp và có thông điệp, backend xác thực và lưu cả hai giá trị.

**Indexes:**
- `idx_gift_box_config_item` ON `gift_box_config_item`(config_id)

---

## 6.5. Packaging Accessories

Ví dụ: giấy lót (crinkle paper), đệm xốp (foam padding), giấy tổ ong chống sốc (honeycomb paper), vật liệu chèn bảo vệ. Tất cả là Product loại `PACKAGING_ACCESSORY` với Variant và giá bán lẻ bình thường.

| Ngữ cảnh | Giá khách hàng |
|----------|----------------|
| Mua riêng | Giá bán Variant |
| Thành phần cố định trong Gift Set | Bao gồm trong giá set |
| Tùy chọn miễn phí trong Gift Set | 0 phí thêm |
| Thêm vào Custom Gift Box | 0 phí thêm |

> **Miễn phí không có nghĩa là không trừ kho.** Phụ kiện complimentary vẫn tiêu thụ tồn kho. Backend tính toán giá; không bao giờ tin tưởng `unitPrice` hoặc `isComplimentary` từ client.

---

## 6.6. Greeting Cards và Message Rendering

Thiệp là Product loại `GREETING_CARD`. Có thể là tùy chọn complimentary trong set, tùy chọn cao cấp tính phí, bán riêng hoặc được chọn trong Custom Box theo cấu hình giá.

`message_rendering_method` là `HANDWRITTEN` hoặc `PRINTED`. Lưu `message_text` và phương thức rendering với cart selection và sao chép vào order component snapshot. Đối với nội dung Custom Gift Box, thông điệp lưu trên `gift_box_config_item` tương ứng; đối với tùy chọn bundle ở cấp cart, thông điệp lưu trên `cart_item_option_selection`. Phương thức rendering không thay đổi giá trừ khi có quy tắc giá rõ ràng.

---

## 7. Cart Module

### `cart`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID giỏ hàng |
| `user_id` | UUID | NOT NULL, **FK** → `user(id)` | Người sở hữu |
| `updated_at` | TIMESTAMP | NULLABLE | Thời gian cập nhật |

**Indexes:**
- `idx_cart_user` ON `cart`(user_id)

---

### `cart_item`

Đúng một nguồn được đặt:
- `product_variant_id`: sản phẩm thường, Ready-made Gift Set, hoặc Ready-made Gift Box;
- `gift_box_config_id`: Custom Gift Box.

`cart_item_option_selection` lưu các tùy chọn bundle, số lượng, nội dung thông điệp và lựa chọn viết tay/in.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID mục giỏ hàng |
| `cart_id` | UUID | NOT NULL, **FK** → `cart(id)` | Giỏ hàng cha |
| `product_variant_id` | UUID | NULLABLE, **FK** → `product_variant(id)` | Variant sản phẩm |
| `gift_box_config_id` | UUID | NULLABLE, **FK** → `gift_box_config(id)` | Cấu hình Custom Box |
| `quantity` | INTEGER | NOT NULL | Số lượng |

**Constraints:**
- `chk_cart_item_type` CHECK (
    (product_variant_id IS NOT NULL AND gift_box_config_id IS NULL) OR
    (product_variant_id IS NULL AND gift_box_config_id IS NOT NULL)
  )
  — Một CartItem chỉ có thể là Product Variant HOẶC GiftBoxConfig, không được cả hai.

**Indexes:**
- `idx_cart_item_cart` ON `cart_item`(cart_id)
- `idx_cart_item_variant` ON `cart_item`(product_variant_id)

---

### `cart_item_option_selection`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID lựa chọn |
| `cart_item_id` | UUID | NOT NULL, **FK** → `cart_item(id)` | Mục giỏ hàng cha |
| `bundle_option_id` | UUID | NOT NULL | Bundle Option được chọn |
| `quantity` | INTEGER | NULLABLE | Số lượng |
| `message_text` | TEXT | NULLABLE | Nội dung thiệp |
| `message_rendering_method` | VARCHAR(20) | NULLABLE, ENUM(MessageRenderingMethod) | `HANDWRITTEN` hoặc `PRINTED` |

**MessageRenderingMethod enum:**
- `HANDWRITTEN` — Viết tay
- `PRINTED` — In

**Indexes:**
- `idx_cart_item_option_selection` ON `cart_item_option_selection`(cart_item_id)

---

## 8. Order Module

### Policy Snapshots

Khi checkout, snapshot `is_perishable`, `shelf_life_hours`, `return_policy` và `advance_payment_percent` vào `order_item` và `order_item_component`. Đơn hàng lịch sử không được thay đổi khi chính sách sản phẩm hiện tại được chỉnh sửa.

### `order`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID đơn hàng |
| `user_id` | UUID | NOT NULL, **FK** → `user(id)` | Người đặt |
| `address_id` | UUID | NOT NULL, **FK** → `address(id)` | Địa chỉ giao hàng |
| `order_number` | VARCHAR(50) | NOT NULL, UNIQUE | Số đơn hàng |
| `subtotal` | NUMERIC(19,2) | NOT NULL | Tổng tiền sản phẩm |
| `shipping_fee` | NUMERIC(19,2) | NOT NULL | Phí vận chuyển |
| `total_amount` | NUMERIC(19,2) | NOT NULL | Tổng cộng |
| `required_advance_amount` | NUMERIC(19,2) | NULLABLE | Tiền đặt cọc cần thanh toán |
| `status` | VARCHAR(20) | NOT NULL, ENUM(OrderStatus) | Trạng thái đơn hàng |
| `ordered_at` | TIMESTAMP | NOT NULL | Thời gian đặt hàng |

**OrderStatus enum:**
- `PENDING_CONFIRMATION` → `CONFIRMED` → `PROCESSING` → `PACKAGING` → `READY_TO_SHIP` → `SHIPPING` → `DELIVERED` → `COMPLETED`
- `CANCELLED`, `RETURN_REQUESTED`, `RETURNED`, `FAILED`

**Indexes:**
- `idx_order_user` ON `order`(user_id)
- `idx_order_number` ON `order`(order_number)
- `idx_order_status` ON `order`(status)

---

### `order_item`

Đúng một nguồn: `product_variant_id` hoặc `gift_box_config_id`. Lưu các snapshot checkout **không thay đổi**: tên mục, SKU, variant JSON, giá đơn, số lượng, thành tiền, tính dễ hỏng, hạn sử dụng, chính sách trả hàng và số tiền đặt cọc.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID dòng đơn |
| `order_id` | UUID | NOT NULL, **FK** → `order(id)` | Đơn hàng cha |
| `product_variant_id` | UUID | NULLABLE, **FK** → `product_variant(id)` | Variant sản phẩm |
| `gift_box_config_id` | UUID | NULLABLE, **FK** → `gift_box_config(id)` | Cấu hình Custom Box |
| `item_name_snapshot` | VARCHAR(255) | NOT NULL | Tên tại thời điểm đặt |
| `sku_snapshot` | VARCHAR(255) | NULLABLE | SKU tại thời điểm đặt |
| `variant_snapshot` | JSONB | NULLABLE | Thông tin Variant tại thời điểm đặt |
| `unit_price` | NUMERIC(19,2) | NOT NULL | Giá đơn tại thời điểm đặt |
| `quantity` | INTEGER | NOT NULL | Số lượng |
| `subtotal` | NUMERIC(19,2) | NOT NULL | Thành tiền |
| `is_perishable_snapshot` | BOOLEAN | NULLABLE | Dễ hỏng tại thời điểm đặt |
| `shelf_life_hours_snapshot` | INTEGER | NULLABLE | Hạn sử dụng (giờ) tại thời điểm đặt |
| `return_policy_snapshot` | VARCHAR(20) | NULLABLE, ENUM(ReturnPolicy) | Chính sách trả hàng tại thời điểm đặt |
| `advance_payment_percent_snapshot` | NUMERIC(5,2) | NULLABLE | Tỷ lệ đặt cọc tại thời điểm đặt |
| `required_advance_amount` | NUMERIC(19,2) | NULLABLE | Tiền đặt cọc của dòng này |

**Constraints:**
- `chk_order_item_type` CHECK (
    (product_variant_id IS NOT NULL AND gift_box_config_id IS NULL) OR
    (product_variant_id IS NULL AND gift_box_config_id IS NOT NULL)
  )
  — Một OrderItem chỉ có thể là Product Variant HOẶC GiftBoxConfig.

> **Quan trọng:** Sau khi checkout, Order chỉ dựa vào snapshot. Không tính lại lịch sử đơn hàng bằng giá Variant hiện tại.

**Indexes:**
- `idx_order_item_order` ON `order_item`(order_id)
- `idx_order_item_variant` ON `order_item`(product_variant_id)

---

### `order_item_component`

Lưu mọi thành phần của Gift Set hoặc Custom Box đã mua: vai trò, tham chiếu variant tùy chọn, snapshot tên/SKU/variant, số lượng, giá đơn, cờ complimentary, thông điệp thiệp/phương thức rendering và snapshot chính sách. Đây cũng là bằng chứng mua hàng để đánh giá một sản phẩm riêng lẻ trong set hoặc custom box.

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID thành phần |
| `order_item_id` | UUID | NOT NULL, **FK** → `order_item(id)` | Dòng đơn cha |
| `product_variant_id` | UUID | NULLABLE, **FK** → `product_variant(id)` | Variant thành phần |
| `component_role` | VARCHAR(20) | NULLABLE, ENUM(ComponentRole) | Vai trò: `BOX`, `PRODUCT`, `ACCESSORY`, `GREETING_CARD` |
| `product_name_snapshot` | VARCHAR(255) | NULLABLE | Tên tại thời điểm đặt |
| `sku_snapshot` | VARCHAR(255) | NULLABLE | SKU tại thời điểm đặt |
| `variant_snapshot` | JSONB | NULLABLE | Thông tin Variant tại thời điểm đặt |
| `quantity` | INTEGER | NULLABLE | Số lượng |
| `unit_price` | NUMERIC(19,2) | NULLABLE | Giá đơn |
| `is_complimentary` | BOOLEAN | NULLABLE | Miễn phí hay không |
| `message_text` | TEXT | NULLABLE | Nội dung thiệp |
| `message_rendering_method` | VARCHAR(20) | NULLABLE, ENUM(MessageRenderingMethod) | `HANDWRITTEN` hoặc `PRINTED` |
| `is_perishable_snapshot` | BOOLEAN | NULLABLE | Dễ hỏng tại thời điểm đặt |
| `shelf_life_hours_snapshot` | INTEGER | NULLABLE | Hạn sử dụng (giờ) tại thời điểm đặt |
| `return_policy_snapshot` | VARCHAR(20) | NULLABLE, ENUM(ReturnPolicy) | Chính sách trả hàng |
| `advance_payment_percent_snapshot` | NUMERIC(5,2) | NULLABLE | Tỷ lệ đặt cọc |

**Indexes:**
- `idx_order_item_component_order_item` ON `order_item_component`(order_item_id)
- `idx_order_item_component_variant` ON `order_item_component`(product_variant_id)

---

## 9. Payment Module

Một Order có thể có nhiều payment rows. `order.required_advance_amount` lưu số tiền tính toán khi checkout. Payment service tạo advance payment và sau đó remaining-balance payment. Payment service phải xác thực giao dịch tổng thanh toán thành công và số dư còn lại.

### `payment`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID thanh toán |
| `order_id` | UUID | NOT NULL, **FK** → `order(id)` | Đơn hàng |
| `payment_method` | VARCHAR(20) | NOT NULL, ENUM(PaymentMethod) | Phương thức thanh toán |
| `payment_status` | VARCHAR(20) | NOT NULL, ENUM(PaymentStatus) | Trạng thái thanh toán |
| `payment_stage` | VARCHAR(20) | NOT NULL, ENUM(PaymentStage) | Giai đoạn thanh toán |
| `amount` | NUMERIC(19,2) | NOT NULL | Số tiền |
| `paid_at` | TIMESTAMP | NULLABLE | Thời gian thanh toán |

**PaymentMethod enum:**
- `CREDIT_CARD`, `DEBIT_CARD`, `BANK_TRANSFER`, `E_WALLET`, `COD`

**PaymentStatus enum:**
- `PENDING`, `PAID`, `FAILED`, `REFUNDED`

**PaymentStage enum:**
- `FULL_PAYMENT` — Thanh toán toàn bộ
- `ADVANCE_PAYMENT` — Đặt cọc trước (ví dụ: 50% cho hàng dễ hỏng)
- `REMAINING_PAYMENT` — Thanh toán số dư sau đặt cọc

**Indexes:**
- `idx_payment_order` ON `payment`(order_id)

> Một đơn hàng có thể có nhiều Payment:
> - Payment 1: 500.000đ — `ADVANCE_PAYMENT`
> - Payment 2: 500.000đ — `REMAINING_PAYMENT`

### Quy tắc thanh toán v1

- **COD không được phép** khi `order.required_advance_amount > 0`. Khách phải dùng phương thức không phải COD để thanh toán đặt cọc.
- Một `ADVANCE_PAYMENT` hoặc `FULL_PAYMENT` không phải COD được coi là `PAID` khi admin **xác nhận** đơn hàng. Trước khi xác nhận, nó vẫn là `PENDING` (trừ khi đã thất bại).
- Một `REMAINING_PAYMENT` không phải COD được coi là `PAID` khi đơn hàng đạt trạng thái `DELIVERED`.
- Một thanh toán COD (COD toàn bộ cho đơn không có đặt cọc, hoặc COD cho số dư còn lại khi luồng đó được hỗ trợ) chỉ được coi là `PAID` khi đơn hàng đạt `DELIVERED`.
- Backend phải thực thi các chuyển đổi này; FE không được đánh dấu payment là paid. Xác nhận của admin và chuyển đổi giao hàng phải cập nhật trạng thái payment giao dịch với chuyển đổi đơn hàng tương ứng.
- Nếu đơn hàng yêu cầu đặt cọc, từ chối COD cho đơn hàng thay vì tạo payment COD bỏ qua yêu cầu đặt cọc.

> Đây là quy tắc cấp service vì chúng phụ thuộc vào yêu cầu đặt cọc tổng hợp của đơn hàng và vòng đời đơn hàng. Không dựa vào CHECK constraint cấp hàng để thực thi quy tắc cross-table hoặc chuyển đổi trạng thái.

---

## 10. Review Module

`review` lưu `user_id`, `product_id`, tùy chọn `product_variant_id`, rating 1–5, tiêu đề/nội dung tùy chọn, timestamps và **đúng một** nguồn mua:
- `order_item_id` cho mục mua riêng lẻ, Gift Set hoặc Ready-made Gift Box;
- `order_item_component_id` cho sản phẩm trong Gift Set hoặc Custom Box.

Không có trạng thái kiểm duyệt: đánh giá hợp lệ được đăng trực tiếp. Backend phải xác minh người dùng xác thực sở hữu đơn hàng, đơn hàng là `DELIVERED` hoặc `COMPLETED`, và Product/Variant được đánh giá khớp với nguồn mua. Một nguồn mua chỉ được review một lần. Tóm tắt sản phẩm tổng hợp rating từ tất cả variant của cùng Product.

### `review`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID đánh giá |
| `user_id` | UUID | NOT NULL, **FK** → `user(id)` | Người đánh giá |
| `product_id` | UUID | NOT NULL, **FK** → `product(id)` | Sản phẩm được đánh giá |
| `product_variant_id` | UUID | NULLABLE, **FK** → `product_variant(id)` | Phiên bản cụ thể đã mua |
| `order_item_id` | UUID | NULLABLE, **FK** → `order_item(id)` | Nguồn mua: dòng đơn |
| `order_item_component_id` | UUID | NULLABLE, **FK** → `order_item_component(id)` | Nguồn mua: thành phần trong Set/Box |
| `rating` | INTEGER | NOT NULL | Điểm đánh giá (1-5) |
| `title` | VARCHAR(255) | NULLABLE | Tiêu đề đánh giá |
| `comment` | TEXT | NULLABLE | Nội dung đánh giá |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |
| `updated_at` | TIMESTAMP | NULLABLE | Thời gian cập nhật |

**Constraints:**
- `chk_review_rating` CHECK (rating >= 1 AND rating <= 5)
- `chk_review_purchase_source` CHECK (
    (order_item_id IS NOT NULL AND order_item_component_id IS NULL) OR
    (order_item_id IS NULL AND order_item_component_id IS NOT NULL)
  ) — Đúng một nguồn mua
- `uk_review_order_item` UNIQUE (order_item_id) — Một OrderItem chỉ được review một lần
- `uk_review_order_item_component` UNIQUE (order_item_component_id) — Một OrderItemComponent chỉ được review một lần

**Indexes:**
- `idx_review_product` ON `review`(product_id)
- `idx_review_user` ON `review`(user_id)
- `idx_review_variant` ON `review`(product_variant_id)

**Quy tắc Review:**
- Review Gift Set hoặc Ready-made Gift Box: tham chiếu `order_item_id`
- Review sản phẩm riêng: tham chiếu `order_item_id`
- Review sản phẩm nằm trong Gift Set hoặc Custom Box: tham chiếu `order_item_component_id`
- Chỉ đơn đã giao (`DELIVERED`/`COMPLETED`) mới được review
- Rating tính trung bình theo `product_id`, gộp tất cả Variant
- Đánh giá trùng lặp và sản phẩm/nguồn không khớp bị từ chối

---

### `review_image`

| Cột | Kiểu | Ràng buộc | Mô tả |
|-----|------|-----------|-------|
| `id` | UUID | **PK**, DEFAULT gen_random_uuid() | ID ảnh |
| `review_id` | UUID | NOT NULL, **FK** → `review(id)` | Đánh giá cha |
| `image_url` | VARCHAR(500) | NOT NULL | URL ảnh công khai |
| `secure_url` | VARCHAR(500) | NOT NULL | URL ảnh bảo mật |
| `public_id` | VARCHAR(255) | NULLABLE | Public ID (Cloudinary) |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Thời gian tạo |

---

## Business Rules

### 1. Tính giá theo ngữ cảnh

| Ngữ cảnh | Giá tính cho khách |
|----------|-------------------|
| Mua PACKAGING_ACCESSORY riêng | Giá Variant |
| PACKAGING_ACCESSORY trong Gift Set | **0 (miễn phí)** |
| PACKAGING_ACCESSORY trong Custom Box | **0 (miễn phí)** |
| GREETING_CARD trong Bundle Option (FREE) | **0 (miễn phí)** |
| GREETING_CARD trong Bundle Option (PRODUCT_PRICE) | Giá Variant |

> Backend phải tính giá theo ngữ cảnh, không tin tưởng giá từ frontend.

### 2. Miễn phí vẫn trừ kho

Mỗi lần sử dụng phụ kiện miễn phí đều phải kiểm tra và trừ tồn kho thực tế.

### 3. Chính sách đặt cọc (Advance Payment)

- Mua trực tiếp sản phẩm dễ hỏng: trả trước theo tỷ lệ của sản phẩm đó
- Gift Set có chứa thành phần yêu cầu trả trước: áp dụng tỷ lệ **cao nhất** cho toàn bộ dòng
- Custom Box có chứa thành phần cần trả trước: áp dụng tỷ lệ **cao nhất** cho toàn bộ dòng
- Nếu một thành phần `NO_RETURN`: toàn bộ dòng Set/Custom Box đánh dấu `NO_RETURN`

### 4. Snapshot khi checkout

Đừng đọc chính sách hiện tại của Product khi khách yêu cầu trả hàng sau vài tuần. Chính sách có thể đã thay đổi. Phải snapshot `is_perishable`, `shelf_life_hours`, `return_policy` và `advance_payment_percent` vào `order_item` và `order_item_component`.

### 5. Custom Box restrictions

- Không lồng `READY_MADE_GIFT_SET` vào Custom Box
- `REGULAR` sản phẩm phải có `is_custom_box_eligible = true`
- `box_variant_id` phải là Variant thuộc `READY_MADE_GIFT_BOX`
- Xác thực loại mục, điều kiện đủ, tồn kho, kích thước/dung tích và giá tính toán bởi server

---

## ER Diagram (Mermaid)

```mermaid
erDiagram
    USER ||--o{ ADDRESS : has
    USER ||--o{ USER_ROLE : "has"
    ROLE ||--o{ USER_ROLE : "has"
    USER ||--o{ CART : "has"
    USER ||--o{ ORDER : "places"
    USER ||--o{ GIFT_BOX_CONFIG : "creates"
    USER ||--o{ REVIEW : "writes"

    ADDRESS ||--o{ ORDER : "ships_to"

    CATEGORY ||--o{ PRODUCT : "contains"
    PRODUCT ||--o{ PRODUCT_IMAGE : "has"
    PRODUCT ||--o{ PRODUCT_OPTION : "defines"
    PRODUCT ||--o{ PRODUCT_VARIANT : "has"
    PRODUCT ||--o{ REVIEW : "reviewed"

    PRODUCT_OPTION ||--o{ PRODUCT_OPTION_VALUE : "has"
    PRODUCT_VARIANT ||--o{ PRODUCT_VARIANT_OPTION_VALUE : "has"
    PRODUCT_OPTION_VALUE ||--o{ PRODUCT_VARIANT_OPTION_VALUE : "selected_by"
    PRODUCT_VARIANT ||--o|| GIFT_BOX_VARIANT_DETAIL : "has"
    PRODUCT_VARIANT ||--o|| PRODUCT_VARIANT_POLICY : "has"
    PRODUCT_VARIANT ||--o{ PRODUCT_IMAGE : "has"

    PRODUCT_VARIANT ||--o{ BUNDLE_COMPONENT : "parent_bundle"
    PRODUCT_VARIANT ||--o{ BUNDLE_COMPONENT : "child_component"
    PRODUCT_VARIANT ||--o{ BUNDLE_OPTION_GROUP : "has"
    PRODUCT_VARIANT ||--o{ BUNDLE_OPTION : "provides"

    BUNDLE_OPTION_GROUP ||--o{ BUNDLE_OPTION : "contains"

    GIFT_BOX_CONFIG ||--o{ GIFT_BOX_CONFIG_ITEM : "contains"
    GIFT_BOX_CONFIG ||--o{ CART_ITEM : "selected"

    CART ||--o{ CART_ITEM : "contains"
    CART_ITEM ||--o{ CART_ITEM_OPTION_SELECTION : "has"

    ORDER ||--o{ ORDER_ITEM : "contains"
    ORDER ||--o{ PAYMENT : "receives"
    ORDER_ITEM ||--o{ ORDER_ITEM_COMPONENT : "has"

    ORDER_ITEM ||--o{ REVIEW : "purchased"
    ORDER_ITEM_COMPONENT ||--o{ REVIEW : "purchased"

    REVIEW ||--o{ REVIEW_IMAGE : "has"
```

---

## Danh sách Indexes

| Index | Bảng | Cột | Loại |
|-------|------|-----|------|
| `idx_user_email` | `user` | email | B-tree |
| `idx_address_user` | `address` | user_id | B-tree |
| `idx_product_category` | `product` | category_id | B-tree |
| `idx_product_status` | `product` | status | B-tree |
| `idx_product_type` | `product` | product_type | B-tree |
| `idx_product_name_trgm` | `product` | name | GIN (trigram) |
| `idx_product_description_trgm` | `product` | description | GIN (trigram) |
| `idx_product_custom_eligible` | `product` | is_custom_box_eligible | B-tree |
| `idx_product_image_product` | `product_image` | product_id | B-tree |
| `idx_product_image_variant` | `product_image` | product_variant_id | B-tree |
| `idx_product_option_product` | `product_option` | product_id | B-tree |
| `idx_product_option_value_option` | `product_option_value` | option_id | B-tree |
| `idx_product_variant_product` | `product_variant` | product_id | B-tree |
| `idx_product_variant_sku` | `product_variant` | sku | B-tree |
| `idx_product_variant_status` | `product_variant` | status | B-tree |
| `idx_product_variant_option_value_variant` | `product_variant_option_value` | variant_id | B-tree |
| `idx_bundle_component_bundle` | `bundle_component` | bundle_variant_id | B-tree |
| `idx_bundle_option_group_bundle` | `bundle_option_group` | bundle_variant_id | B-tree |
| `idx_bundle_option_group` | `bundle_option` | option_group_id | B-tree |
| `idx_cart_user` | `cart` | user_id | B-tree |
| `idx_cart_item_cart` | `cart_item` | cart_id | B-tree |
| `idx_cart_item_variant` | `cart_item` | product_variant_id | B-tree |
| `idx_cart_item_option_selection` | `cart_item_option_selection` | cart_item_id | B-tree |
| `idx_gift_box_config_variant` | `gift_box_config` | box_variant_id | B-tree |
| `idx_gift_box_config_item` | `gift_box_config_item` | config_id | B-tree |
| `idx_order_user` | `order` | user_id | B-tree |
| `idx_order_number` | `order` | order_number | B-tree |
| `idx_order_status` | `order` | status | B-tree |
| `idx_order_item_order` | `order_item` | order_id | B-tree |
| `idx_order_item_variant` | `order_item` | product_variant_id | B-tree |
| `idx_order_item_component_order_item` | `order_item_component` | order_item_id | B-tree |
| `idx_order_item_component_variant` | `order_item_component` | product_variant_id | B-tree |
| `idx_payment_order` | `payment` | order_id | B-tree |
| `idx_review_product` | `review` | product_id | B-tree |
| `idx_review_user` | `review` | user_id | B-tree |
| `idx_review_variant` | `review` | product_variant_id | B-tree |

---

## Extensions

- **pg_trgm**: Hỗ trợ tìm kiếm mờ (fuzzy search) trên `product.name` và `product.description`

---

## Ràng buộc Database vs Quy tắc Service

### Thực thi trong SQL (Database Constraints)

- UUID primary keys và foreign keys.
- Unique email, role name, SKU, order number, user-role pair.
- Giá/số lượng không âm; số lượng dương; kích thước dương.
- Ánh xạ variant-option duy nhất.
- Đúng một nguồn cho CartItem và OrderItem (`chk_cart_item_type`, `chk_order_item_type`).
- Đúng một nguồn mua cho Review; unique review per source; rating 1–5 (`chk_review_purchase_source`, `uk_review_order_item`, `uk_review_order_item_component`).
- Tỷ lệ đặt cọc 0–100; variant dễ hỏng yêu cầu `NO_RETURN` và ít nhất 50% đặt cọc (`chk_advance_payment`, `chk_perishable_policy`).

### Thực thi bởi Backend Services

- Bundle parent là Ready-made Gift Set; gift-box container là Ready-made Gift Box.
- Bundle roles và option Product types hợp lệ.
- Nội dung Custom Box đáp ứng quy tắc loại/điều kiện đủ; không có nested Gift Set.
- Variant option values thuộc cùng Product.
- Số lượng lựa chọn option-group tuân thủ min/max.
- Dung tích hộp vật lý và khả năng đóng gói.
- Dự phòng/trừ tồn kho, bao gồm phụ kiện miễn phí.
- Tất cả giá và cờ complimentary được tính toán bởi server.
- Tổng hợp chính sách trả hàng/đặt cọc và snapshots.
- Sở hữu review, trạng thái delivered/completed và tính nhất quán Product/Variant/source.
- Tổng thanh toán và số dư còn lại.
- Từ chối COD khi yêu cầu đặt cọc và chuyển đổi trạng thái payment tại xác nhận của admin/giao hàng như định nghĩa trong Section 9.
- Khả năng đóng gói Custom Box vượt quá kiểm tra thể tích/trọng lượng sơ bộ của FE.

---

## Cài đặt mới (Fresh Installation)

1. Tạo một database PostgreSQL 15+ trống mới.
2. Thực thi `Giftly_Database.sql`.
3. Script seed `ROLE_USER` và `ROLE_ADMIN`.
4. Cấu hình Spring Boot datasource và đồng bộ JPA table/enum mappings.
5. Triển khai các service validations ở trên và integration tests.

> **Không chạy SQL này trên database đang có dữ liệu.** Schema cũ lưu price/stock trên `product`, dùng product IDs trong cart/order/config và có trạng thái kiểm duyệt review. Thiết kế này chuyển price/stock sang variants, dùng variant references, giới thiệu bundles và bỏ kiểm duyệt. Database có dữ liệu cần kế hoạch migration và backfill riêng.

---

## Danh sách kiểm tra Test (Test Checklist)

- [ ] Teddy Bear variants Pink/Blue × S/M có SKU, price, stock và dimensions riêng biệt.
- [ ] Gift Set chứa hộp/sản phẩm cố định và cung cấp thiệp miễn phí và thiệp cao cấp tính phí.
- [ ] Packaging accessory có giá bán lẻ khi mua riêng, miễn phí khi đi kèm, nhưng kho vẫn giảm.
- [ ] Custom Box chấp nhận Regular Product đủ điều kiện và Ready-made Gift Box; từ chối sản phẩm không đủ điều kiện và Gift Set.
- [ ] Kích thước lòng hộp và tải trọng tối đa được xác thực.
- [ ] Variant hoa/trái cây dễ hỏng là `NO_RETURN` và yêu cầu đặt cọc 50%.
- [ ] Dòng tổng hợp snapshot `NO_RETURN` và số tiền đặt cọc tính toán.
- [ ] Thông điệp thiệp hỗ trợ `HANDWRITTEN` và `PRINTED`.
- [ ] Review yêu cầu đơn `DELIVERED`/`COMPLETED` do người dùng sở hữu; review thành phần xác định sản phẩm đã mua.
- [ ] Đánh giá trùng lặp và sản phẩm/nguồn không khớp bị từ chối.
