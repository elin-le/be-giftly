# Changelog — Giftly Backend

## v1.17 — 2026-11-27

### Thêm
- **Mua hộp quà custom độc lập với cart** — `POST /api/gift-boxes/{giftBoxId}/purchase` tạo đơn trực tiếp từ cấu hình đã lưu, không thêm hộp vào cart và không xóa/thay đổi cart.
- **Validate cấu hình khi mua** — Bắt buộc đúng một hộp và tối thiểu 3 món không phải phụ kiện/thiệp; xác minh eligibility, trạng thái, tồn kho, trọng lượng và kích thước lòng hộp.
- **Snapshot thành phần đơn hàng** — Lưu variant, SKU, tên, giá và lời nhắn từng thành phần; trừ tồn kho khi đặt và hoàn tồn khi hủy.

### Cơ sở dữ liệu và xác minh
- **V21** — Cho phép cột tên/loại legacy của `order_item_component` nhận `NULL` khi ghi snapshot mới.
- **V22** — Gỡ `gift_box_config_id` khỏi `cart_item`, xóa liên kết custom-box cũ trong cart nhưng giữ nguyên cấu hình; ràng buộc cart chỉ chứa product variant.
- Thêm regression tests cho migration và các giới hạn mua hộp custom; cập nhật API/database docs.

---

## v1.16 — 2026-11-27

### Thêm
- **Lọc sản phẩm** — `GET /api/products` hỗ trợ tìm kiếm gần đúng, theo từ và không phân biệt dấu tiếng Việt; có thể kết hợp từ khóa với loại sản phẩm, category và khoảng giá. Thêm alias cho các loại custom, mua lẻ, set và box.
- **Custom gift box dùng sản phẩm có ảnh** — Hộp, ruy-băng, giấy gói và thiệp được quản lý như product/variant; API trả về ảnh và thông tin variant để FE chọn component.
- **Ảnh review qua Cloudinary** — `POST /api/reviews` nhận JSON với tối đa 3 cặp `publicId`/`secureUrl`; FE upload trực tiếp lên Cloudinary, backend chỉ xác thực và lưu metadata.
- **Bộ dữ liệu demo lớn** — Seed idempotent gồm 100 sản phẩm, 191 variants, 100 tài khoản khách hàng, 1.000 đơn hàng và 3.000 dòng hàng, cùng dữ liệu thanh toán, review và gift box.
- **Workbook demo và test cases** — Tạo workbook cho 29 bảng dữ liệu với 38 kịch bản kiểm thử; bổ sung hướng dẫn tái tạo và sử dụng dữ liệu demo.

### Sửa
- **Thông tin variant trong response** — Bổ sung kích thước (`length`, `width`, `height`, `dimensionUnit`) và khối lượng (`weight`, `weightUnit`) để FE nhận được đầy đủ sau khi lưu sản phẩm hoặc resolve variant.
- **Lọc theo loại và giá** — Cho phép kết hợp điều kiện sản phẩm, loại, category và khoảng giá; giới hạn giá áp dụng khi variant có giá phù hợp.
- **Giới hạn component mỗi loại** — Mỗi cấu hình custom gift box chỉ có thể chọn tối đa một component cho mỗi loại; không cho phép xóa hộp/component đang được cấu hình.
- **Review theo sản phẩm phân trang trên API hiện hữu** — `GET /api/reviews/product/{productId}` hỗ trợ phân trang và lọc rating trực tiếp; không cần route `/paginated`, response trả về `ApiResponse<PageResponse<ReviewResponse>>`.
- **Dọn dữ liệu ảnh review trùng lặp** — Giữ `secure_url` làm URL ảnh chính và loại bỏ cột URL legacy sau khi bảo toàn dữ liệu cũ.

### Cơ sở dữ liệu
- **V15–V17** — Sửa check constraint của `cart_item` và `order_item` để kiểm tra `product_variant_id` hoặc `gift_box_config_id`; cho phép cột tên legacy của `order_item` nhận `NULL`.
- **V18** — Bật PostgreSQL `unaccent` và thêm index giá variant phục vụ tìm kiếm không dấu và lọc giá.
- **V19** — Chuẩn hóa component gift box trong `gift_box_config_item`, backfill dữ liệu cũ và ràng buộc duy nhất theo cấu hình/loại component.
- **V20** — Đơn giản hóa lưu trữ ảnh review, giữ metadata Cloudinary và loại bỏ cột URL bị trùng.

### Tài liệu và xác minh
- Cập nhật API, database, kiến trúc và hướng dẫn demo; thêm regression tests cho filter, migration, custom gift box và review ảnh.
- `./gradlew test` hoàn tất thành công; `git diff --check` không phát hiện lỗi whitespace.

---

## v1.15 — 2026-11-24

### Sửa
- **Xóa sản phẩm không xóa ảnh trên Cloudinary** — Khi admin xóa sản phẩm (`DELETE /api/admin/products/{id}`) hoặc xóa variant (`DELETE /api/admin/products/{productId}/variants/{variantId}`), ảnh vẫn còn trên Cloudinary. Giờ tự động xóa tất cả ảnh common và variant images từ Cloudinary trước khi xóa trong DB.
- **Cập nhật ảnh không xóa ảnh cũ trên Cloudinary** — Khi admin cập nhật ảnh của product common images hoặc variant images, ảnh cũ bị thay thế trong DB nhưng vẫn còn trên Cloudinary. Giờ so sánh `publicId` của ảnh cũ và ảnh mới, chỉ xóa những ảnh có `publicId` không còn trong danh sách mới.
- **`AdminProductService`** — Inject `CloudinaryService`. Thêm `deleteRemovedImages()` helper method để so sánh danh sách ảnh cũ và mới theo `publicId`, xóa những ảnh đã bị loại bỏ khỏi Cloudinary.
- **`AdminProductService.deleteProduct()`** — Xóa common images và tất cả variant images từ Cloudinary trước khi xóa product trong DB.
- **`AdminProductService.deleteVariant()`** — Xóa variant images từ Cloudinary trước khi xóa variant trong DB.
- **`AdminProductService.updateProduct()`** — So sánh và xóa common images cũ khi cập nhật. So sánh và xóa variant images cũ khi cập nhật variant trong product.
- **`AdminProductService.updateVariant()`** — So sánh và xóa variant images cũ khi cập nhật variant độc lập.
- **`ProductImageRepository`** — Thêm `findByProductId(UUID)` method để lấy common images của product.

### Ghi chú
- **Cơ chế so sánh** — Dùng `publicId` làm unique identifier để nhận diện ảnh. Nếu `publicId` của ảnh cũ không có trong danh sách ảnh mới gửi lên, ảnh đó bị xóa khỏi Cloudinary. Nếu `publicId` vẫn còn giữ nguyên, ảnh không bị xóa.
- **An toàn** — `CloudinaryService.deleteImage()` đã có try-catch và log warning nếu xóa thất bại, nên nếu Cloudinary API lỗi thì không ảnh hưởng đến việc xóa/sửa product trong DB.
- **ReviewService và AdminReviewService** — Đã có xóa ảnh Cloudinary khi xóa review từ trước, không cần sửa thêm.

---

## v1.14 — 2026-11-24

### Sửa
- **Lỗi `null value in column "option_value_id"` vẫn xuất hiện sau v1.13** — `entityManager.clear()` detach toàn bộ persistence context, nhưng code sau đó vẫn cố sửa các collection của entity đã detached (ví dụ: `option.getValues().remove(oldValue)`), khiến Hibernate re-attach và flush với FK=null. Cách tiếp cận mix DB-level delete với JPA operations không thể hoạt động ổn định.
- **`ProductOptionValue` entity** — Thêm `@OneToMany(mappedBy = "optionValue", cascade = CascadeType.REMOVE)` — `variantLinks` collection. Khi xóa option value, JPA tự động cascade xóa các variant-option links liên quan, không cần xóa thủ công ở DB level.
- **`AdminProductOptionService`** — Bỏ `EntityManager`, bỏ `@Modifying` delete queries, bỏ `entityManager.clear()`. Rely hoàn toàn vào JPA cascade: `ProductOption` → `ProductOptionValue` (orphanRemoval=true) → `ProductVariantOptionValue` (CascadeType.REMOVE).
- **`AdminProductService`** — Bỏ `EntityManager`, bỏ `@Modifying` delete queries, bỏ `entityManager.clear()`. Rely vào `ProductVariant.optionValues` (orphanRemoval=true) để tự động xóa variant-option links khi xóa variant hoặc clear collection.
- **`ProductVariantOptionValueRepository`** — Xóa 2 methods `@Modifying` (`deleteByOptionValueId`, `deleteByVariantId`) — không còn cần thiết.

### Ghi chú
- **Giải pháp cuối cùng** — Thay vì cố gắng mix DB-level delete queries với JPA persistence context management, fix này dùng JPA cascade relationships để Hibernate tự động xử lý thứ tự xóa đúng: option value → variant-option links. Không cần `entityManager.clear()`, không cần `@Modifying` delete queries.
- **Cascade chain**: `ProductOption` (orphanRemoval=true) → `ProductOptionValue` (CascadeType.REMOVE) → `ProductVariantOptionValue`. Khi xóa option value, Hibernate tự động xóa các variant-option links trước, sau đó mới xóa option value.
- **Ảnh hưởng** — Xóa option value, xóa option group, xóa variant, và cập nhật option values của variant giờ hoạt động đúng hoàn toàn bằng JPA cascade. Lỗi `null value in column "option_value_id"` không còn xuất hiện.

---

## v1.12 — 2026-11-24

### Sửa
- **Lỗi `null value in column "option_value_id"` vẫn xuất hiện sau v1.11** — Fix ở v1.11 thêm `@Modifying` delete query (`deleteByOptionValueId`, `deleteByVariantId`) để xóa ở DB level, nhưng Hibernate persistence context vẫn giữ các managed `ProductVariantOptionValue` entity cũ. Sau khi DB đã xóa, Hibernate vẫn cố flush các entity này với `option_value_id=null`, vi phạm `NOT NULL` constraint.
- **`ProductVariantOptionValueRepository`** — Thêm `clearAutomatically = true` vào 2 phương thức `@Modifying`:
  - `deleteByOptionValueId()` — Giờ tự động làm sạch persistence context sau khi xóa.
  - `deleteByVariantId()` — Giờ tự động làm sạch persistence context sau khi xóa.

### Ghi chú
- **`clearAutomatically = true`** — Thuộc tính của `@Modifying` annotation, yêu cầu Hibernate evict (loại bỏ) tất cả các managed entities bị ảnh hưởng bởi query khỏi persistence context ngay sau khi thực thi. Điều này ngăn Hibernate flush các entity cũ với FK bằng null.
- **Khác biệt với v1.11** — v1.11 chỉ thay `deleteAll(links)` bằng `@Modifying` delete query, nhưng không clear persistence context. v1.12 bổ sung `clearAutomatically = true` để hoàn tất fix.
- **Ảnh hưởng** — Xóa option value, xóa option group, xóa variant, và cập nhật option values của variant giờ hoạt động đúng hoàn toàn.

---

## v1.11 — 2026-11-24

### Sửa
- **Xóa option value gây lỗi `null value in column "option_value_id"`** — Khi xóa option value hoặc variant, `productVariantOptionValueRepository.deleteAll(links)` dùng JPA managed delete. Hibernate flush trước, thấy `optionValue` đang bị xóa, cố UPDATE các `ProductVariantOptionValue` links với `option_value_id=null` thay vì DELETE chúng, vi phạm `NOT NULL` constraint.
- **`ProductVariantOptionValueRepository`** — Thêm 2 methods `@Modifying`:
  - `deleteByOptionValueId(UUID)` — Xóa trực tiếp các links của option value ở DB level.
  - `deleteByVariantId(UUID)` — Xóa trực tiếp các links của variant ở DB level.
- **`AdminProductOptionService`** — 3 chỗ sửa từ `deleteAll(links)` sang `deleteByOptionValueId()`: `updateOption()`, `deleteOption()`, `deleteOptionValue()`.
- **`AdminProductService`** — 2 chỗ sửa từ `deleteAll(links)` sang `deleteByVariantId()`: `deleteVariant()`, `updateVariant()`.

### Ghi chú
- **Nguyên nhân gốc rễ** — JPA `deleteAll()` trên managed entities khiến Hibernate cố gắng duy trì referential integrity bằng cách set null foreign key thay vì xóa hàng. `@Modifying` JPQL `DELETE` query bỏ qua JPA persistence context và xóa trực tiếp ở DB, tránh hoàn toàn vấn đề này.
- **Ảnh hưởng** — Xóa option value, xóa option group, xóa variant, và cập nhật option values của variant giờ hoạt động đúng mà không gây lỗi `NOT NULL constraint`.

---

## v1.10 — 2026-11-24

### Thêm
- **`AdminVariantController`** — Controller mới với `@RequestMapping("/api/admin/variants")` để cung cấp endpoint độc lập cho việc cập nhật variant.
  - `PUT /api/admin/variants/{variantId}` — Cập nhật thông tin của một variant cụ thể (SKU, giá, tồn kho, kích thước, trọng lượng, trạng thái, option values, ảnh, gift box detail, policy).

### Sửa
- **FE gọi sai URL** — FE gọi `PUT /api/admin/variants/{id}` nhưng endpoint trước đó nằm trong `AdminProductController` dưới path `/api/admin/products`, nên URL thực tế là `PUT /api/admin/products/variants/{id}`. Spring không tìm thấy mapping nên trả về `NoHandlerFoundException`.
- **`AdminVariantController`** — Reuse `AdminProductService.updateVariant()` method đã có sẵn, chỉ cần mapping đúng URL.

### Ghi chú
- **2 endpoint update variant tồn tại song song:** `PUT /api/admin/products/variants/{variantId}` (trong AdminProductController) và `PUT /api/admin/variants/{variantId}` (trong AdminVariantController). Cả hai gọi cùng một service method, FE dùng cái nào cũng được.
- `AdminProductService.updateVariant()` đã implement đầy đủ: cập nhật SKU, giá, tồn kho, kích thước, trọng lượng, trạng thái, option values, ảnh, gift box detail, và policy.

---

## v1.9 — 2026-11-24

### Thêm
- **`PageResponse<T>`** — DTO wrapper mới cho các response có pagination, thay thế việc trả về trực tiếp `Page<T>` của Spring Data. Cung cấp cấu trúc JSON ổn định, không phụ thuộc vào nội bộ của Spring Data.
  - Fields: `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`
  - Static method `PageResponse.from(Page<T>)` để chuyển đổi từ Spring Data Page sang wrapper

### Sửa
- **8 controller endpoints** — Đổi từ `ApiResponse<Page<T>>` sang `ApiResponse<PageResponse<T>>`:
  - `AdminCategoryController.getAllCategories()`
  - `AdminOrderController.getAllOrders()`
  - `AdminPaymentController.getAllPayments()`
  - `AdminProductController.getAllProducts()`
  - `AdminReviewController.getAllReviews()`
  - `AdminUserController.getAllUsers()`
  - `ReviewController.getReviewsByProductPaginated()`
  - `ReviewController.getMyReviews()`

### Ghi chú
- **Cảnh báo serialization** — Spring Data cảnh báo `Serializing PageImpl instances as-is is not supported` khi trả về `Page<T>` trực tiếp trong JSON response. Cấu trúc JSON không ổn định và có thể thay đổi giữa các phiên bản. `PageResponse<T>` wrapper giải quyết vấn đề này hoàn toàn.
- **Cấu trúc JSON mới** — Thay vì trả về Spring Data's PageImpl structure (có thể thay đổi), FE giờ nhận được cấu trúc ổn định:
  ```json
  { "content": [...], "page": 0, "size": 20, "totalElements": 100, "totalPages": 5, "first": true, "last": false }
  ```
- **Service layer không đổi** — Các service vẫn trả về `Page<T>` như cũ; chỉ controller layer mới wrap sang `PageResponse<T>`.

---

## v1.8 — 2026-11-23

### Thêm
- **Admin Payment module** — 4 endpoints admin mới để quản lý thanh toán:
  - `GET /api/admin/payments` — Liệt kê tất cả payments với filter theo order ID và status, có pagination
  - `GET /api/admin/payments/{paymentId}` — Xem chi tiết payment theo ID
  - `GET /api/admin/payments/order/{orderId}` — Xem tất cả payments của một đơn hàng
  - `PATCH /api/admin/payments/{paymentId}/status` — Cập nhật trạng thái payment (PAID, FAILED, REFUNDED)
- **`AdminPaymentService`** — Service xử lý admin payment logic với validation cho terminal states (PAID, REFUNDED không thể đổi trạng thái)
- **`AdminPaymentController`** — Controller với 4 endpoints admin cho payment
- **`AdminPaymentResponse`** — DTO trả về thông tin payment kèm order number, user email
- **`UpdatePaymentStatusRequest`** — DTO cho việc cập nhật trạng thái payment
- **GiftBox update** — 2 endpoints mới để cập nhật gift box đã tạo:
  - `POST /api/gift-boxes/{giftBoxId}/items` — Thêm sản phẩm vào gift box
  - `DELETE /api/gift-boxes/{giftBoxId}/items/{itemId}` — Xóa sản phẩm khỏi gift box
- **`GiftBoxService.addGiftBoxItem()`** — Thêm item vào gift box với validation tương tự create
- **`GiftBoxService.removeGiftBoxItem()`** — Xóa item khỏi gift box

### Sửa
- **`OrderResponse`** — Thêm `requiredAdvanceAmount` field để FE biết số tiền cần đặt cọc trước
- **`AdminOrderResponse`** — Thêm `requiredAdvanceAmount` field tương tự
- **`OrderService.toOrderResponse()`** — Trả về `requiredAdvanceAmount` từ order entity
- **`AdminOrderService.toAdminResponse()`** — Trả về `requiredAdvanceAmount` từ order entity

### Ghi chú
- **Admin Payment trước đó hoàn toàn trống** — Không có API nào để admin xem hay quản lý payment. Giờ admin có thể xem tất cả payments, filter theo đơn hàng/trạng thái, và cập nhật trạng thái payment thủ công.
- **GiftBox trước đó chỉ có create, read, delete** — User không thể chỉnh sửa gift box đã tạo. Giờ có thể thêm/xóa items.
- **OrderResponse thiếu requiredAdvanceAmount** — FE không biết số tiền đặt cọc trước của đơn hàng. Giờ field này được trả về đầy đủ.

---

## v1.7 — 2026-11-23

### Thêm
- **`GET /api/products/{productId}`** — API public mới để FE xem chi tiết sản phẩm, bao gồm variants với `finalPrice`, ảnh chung, rating và số lượng đánh giá. Trước đó FE chỉ có API list products mà không có API xem chi tiết.
- **`PUT /api/admin/bundle/components/{componentId}`** — Cập nhật bundle component (role, quantity). Trước đó chỉ có thể tạo/xóa bundle component.
- **`GET /api/admin/bundle/option-groups`** — Liệt kê tất cả bundle option groups.
- **`GET /api/admin/bundle/variants/{variantId}/option-groups`** — Liệt kê bundle option groups theo variant.
- **`GET /api/admin/bundle/components`** — Liệt kê tất cả bundle components.
- **`GET /api/admin/bundle/variants/{variantId}/components`** — Liệt kê bundle components theo variant.
- **`UpdateBundleComponentRequest`** — DTO mới cho việc cập nhật bundle component.

### Sửa
- **`ProductService`** — Thêm method `getProductById()` để trả về `ProductResponse` đầy đủ với variants, rating, và review count.
- **`ProductController`** — Thêm endpoint `GET /{productId}` cho FE xem chi tiết sản phẩm.
- **`AdminBundleOptionService`** — Thêm `updateBundleComponent()`, `getAllBundleOptionGroups()`, `getBundleOptionGroupsByVariantId()`, `getAllBundleComponents()`, `getBundleComponentsByVariantId()`.
- **`AdminBundleOptionController`** — Thêm 5 endpoints mới: PUT bundle component và 4 GET list endpoints.
- **`API_DOCUMENTATION.md`** — Document đầy đủ các endpoints mới: GET product detail (4.2.2), DELETE variant, PUT variant, PUT bundle component, và 4 GET list bundle endpoints.

### Ghi chú
- **CRUD Product đã đầy đủ:** Product (GET all, GET by ID, POST, PUT, DELETE), Variant (GET by product, PUT, DELETE), Option (GET, POST, PUT, DELETE), Option Value (GET, POST, PUT, DELETE), Bundle Option Group (GET all, GET by variant, POST, PUT, DELETE), Bundle Component (GET all, GET by variant, POST, PUT, DELETE).
- **FE giờ có đầy đủ API:** Xem chi tiết sản phẩm (`GET /api/products/{id}`), chọn variant theo option (`GET /api/products/{id}/resolve-variant`), xem options (`GET /api/products/{id}/options`).

---

## v1.6 — 2026-11-23

### Thêm
- **`priceAdjustment` field trong `ProductOptionValue`** — Mỗi giá trị tùy chọn (ví dụ: "Size L", "Màu đỏ") giờ có thể có mức tăng/giảm giá (mặc định 0). Admin đặt giá tăng/giảm cho từng option value, không cần đặt lại toàn bộ giá của variant.
- **`finalPrice` field trong `ProductResponse.VariantResponse`** — Giá cuối cùng của variant = base price + tổng priceAdjustment của các option values gắn với variant. FE dùng `finalPrice` để hiển thị giá thực tế người dùng trả.
- **Migration V14** — Thêm cột `price_adjustment NUMERIC(19, 2) NOT NULL DEFAULT 0` vào bảng `product_option_value`.

### Sửa
- **`CreateProductOptionValueRequest`** — Thêm field `priceAdjustment` (BigDecimal, optional). Admin gửi giá tăng/giảm khi tạo/sửa option value.
- **`ProductOptionValueResponse`** — Thêm field `priceAdjustment` để trả về giá tăng/giảm của option value.
- **`AdminProductOptionService`** — Tất cả 4 method (createOption, updateOption, addOptionValue, updateOptionValue) giờ xử lý `priceAdjustment`.
- **`ProductOptionService`** — `toProductOptionValueResponse()` trả về `priceAdjustment`; `toVariantResponse()` tính `finalPrice`.
- **`ProductMapper`** — Thêm method `calculateFinalPrice()` để tính giá cuối cùng của variant; `toResponseWithDetails()` trả về `finalPrice`.

### Ghi chú
- **Cơ chế tính giá:** Variant có `price` (base price) — admin đặt khi tạo variant. Mỗi option value có `priceAdjustment` (mặc định 0). Giá cuối cùng = base price + tổng priceAdjustment của các option values gắn với variant.
- **Lợi ích:** Xóa option value không ảnh hưởng đến variant — variant vẫn giữ base price. Admin chỉ cần set priceAdjustment cho option value để điều chỉnh giá, không cần sửa lại giá của variant.
- **Ví dụ:** Variant base price = 50000. Option value "Size L" có priceAdjustment = +5000. Option value "Màu đỏ" có priceAdjustment = +0. Final price = 50000 + 5000 + 0 = 55000.

---

## v1.5 — 2026-11-23

### Thêm
- **6 API CRUD Product Options** — Admin giờ có thể tạo/sửa/xóa các nhóm tùy chọn (màu sắc, kích cỡ) và giá trị của chúng:
  - `POST /api/admin/products/{productId}/options` — Tạo nhóm option mới (ví dụ: "Màu sắc" với các giá trị ["Đỏ", "Xanh"])
  - `PUT /api/admin/products/{productId}/options/{optionId}` — Cập nhật tên, thứ tự hiển thị, và/hoặc thay thế toàn bộ giá trị của nhóm option
  - `DELETE /api/admin/products/{productId}/options/{optionId}` — Xóa nhóm option và tất cả giá trị, đồng thời xóa các link variant-option
  - `POST /api/admin/products/{productId}/options/{optionId}/values` — Thêm giá trị mới vào nhóm option (ví dụ: thêm "Xanh lá" vào "Màu sắc")
  - `PUT /api/admin/products/{productId}/options/{optionId}/values/{valueId}` — Cập nhật tên và/hoặc mã màu HEX của một giá trị option
  - `DELETE /api/admin/products/{productId}/options/{optionId}/values/{valueId}` — Xóa một giá trị option, đồng thời xóa các link variant-option
- **3 DTOs mới:** `CreateProductOptionRequest`, `CreateProductOptionValueRequest`, `UpdateProductOptionRequest`
- **`AdminProductOptionService`** — Service xử lý CRUD logic cho product options, tự động xóa variant-option links khi xóa option value
- **`AdminProductOptionController`** — Controller với 6 endpoints admin cho product options
- **`ProductVariantOptionValueRepository.findByOptionValueId()`** — Tìm các link variant-option theo option value ID, dùng để xóa cascade khi xóa option value

### Ghi chú
- **Giá của biến thể đã độc lập** — Mỗi `ProductVariant` có field `price` riêng, nên các tổ hợp option khác nhau có thể có giá khác nhau (ví dụ: màu đen + size L = 100000, màu đen + size M = 95000). Khi admin tạo variant, giá được đặt trực tiếp trên variant, không phụ thuộc vào option values.
- Trước đó chỉ có API GET để đọc options (`GET /api/products/{productId}/options`), không có cách nào để admin tạo/sửa/xóa options.

---

## v1.4 — 2026-11-23

### Thêm
- **`DELETE /api/admin/products/{productId}/variants/{variantId}`** — Xóa variant riêng của sản phẩm. Xóa cùng lúc các entity liên quan: option value links, policy, gift box detail, và ảnh của variant.
- **`PUT /api/admin/variants/{variantId}`** — Cập nhật trực tiếp một variant riêng: SKU, giá, tồn kho, kích thước, trọng lượng, trạng thái, option values, ảnh, gift box detail, và policy.
- **`AdminProductService.deleteVariant()`** — Logic xóa variant an toàn, kiểm tra variant thuộc về product đúng, xóa cascade các entity con.
- **`AdminProductService.updateVariant()`** — Logic cập nhật variant độc lập, không cần gọi PUT product.
- **`ProductVariantRepository.findByProductIdAndSku()`** — Tìm variant theo product ID và SKU, dùng để kiểm tra trùng lặp.

### Sửa
- **`AdminProductService.updateProduct()`** — Khi thêm variant mới (id=null), kiểm tra nếu SKU đã tồn tại trên product thì bỏ qua, không cố insert gây lỗi `duplicate key`. Trước đó code luôn tạo variant mới dù SKU đã có, dẫn đến lỗi ràng buộc `product_variant_product_sku_key`.
- **`AdminProductService`** — Thêm các repository injections: `ProductVariantPolicyRepository`, `GiftBoxVariantDetailRepository`, `ProductVariantOptionValueRepository`, `ProductImageRepository` để hỗ trợ delete/update variant.

### Xóa
- **Lỗi "duplicate key value violates unique constraint product_variant_product_sku_key"** — Không còn xảy ra khi update product với variant có SKU trùng: variant trùng bị skip thay vì gây lỗi.

---

## v1.3 — 2026-11-23

### Sửa
- **`BundleOptionService.getProductVariantPolicy()`** — Khi variant chưa có policy, trả về default values (isPerishable=false, các trường còn lại null) thay vì ném `ResourceNotFoundException`. FE không còn lỗi khi gọi endpoint policy của variant mới tạo.
- **`AdminProductService.updateProduct()`** — Hỗ trợ thêm variant mới vào product đã tồn tại: nếu `UpdateVariantRequest.id` là null, sẽ tạo variant mới thay vì tìm variant cũ. FE có thể dùng `PUT /api/admin/products/{id}` để thêm biến thể mới mà không cần gọi riêng endpoint tạo variant.
- **`UpdateVariantRequest`** — Trường `id` giờ là nullable (trước đó bắt buộc có giá trị).

### Xóa
- **Lỗi "Product Variant Policy not found"** — Không còn xảy ra khi FE gọi policy endpoint của variant chưa cấu hình policy.
- **Lỗi "The given id must not be null"** — Không còn xảy ra khi FE thêm variant mới qua PUT product.

---

## v1.2 — 2026-11-22

### Thêm
- **Bundle options CRUD** — 6 admin endpoints mới để quản lý bundle option groups và components:
  - `POST /api/admin/bundle/option-groups` — Tạo nhóm tùy chọn bundle
  - `PUT /api/admin/bundle/option-groups/{groupId}` — Cập nhật nhóm tùy chọn
  - `DELETE /api/admin/bundle/option-groups/{groupId}` — Xóa nhóm tùy chọn
  - `DELETE /api/admin/bundle/options/{optionId}` — Xóa tùy chọn
  - `POST /api/admin/bundle/components` — Tạo component (hộp, sản phẩm, phụ kiện, thiệp)
  - `DELETE /api/admin/bundle/components/{componentId}` — Xóa component
- **4 DTOs mới:** `CreateBundleOptionGroupRequest`, `CreateBundleOptionRequest`, `UpdateBundleOptionGroupRequest`, `CreateBundleComponentRequest`
- **`AdminBundleOptionService`** — Service xử lý CRUD logic cho bundle options và components
- **`AdminBundleOptionController`** — Controller với 6 endpoints admin cho bundle
- **Ảnh chung sản phẩm** — Relationship giữa Product và ProductImage:
  - `ProductImage` thêm field `product` (nullable, bên cạnh `productVariant`)
  - `Product` thêm `commonImages: List<ProductImage>`
  - `CreateProductRequest` và `UpdateProductRequest` thêm `commonImages` field
  - `ProductMapper` ưu tiên common images, fallback vào ảnh variant đầu tiên

### Sửa
- **`AdminProductService`** — Xử lý common images khi tạo/sửa product
- **`ProductMapper.toResponseWithDetails()`** — Trả về common images thay vì ảnh variant đầu tiên
- **`docs/api/backend-integration.md`** — Section 4 (Bundle CRUD) và Section 5 (Ảnh chung) đánh dấu ✓ Đã hoàn thành
- **`docs/api/backend-integration.md`** — Bảng tóm tắt: 6/6 mục đã hoàn thành, không còn gap pending
- **`docs/api/backend-integration.md`** — Ghi chú FE: tất cả workaround đã loại bỏ

### Xóa
- **FE workaround** — Không còn workaround nào: bundle options và ảnh chung sản phẩm đã có API chính thức

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
| 7 | Bundle options CRUD (6 endpoints) | ✓ Đã thêm |
| 8 | Ảnh chung sản phẩm (commonImages) | ✓ Đã thêm |
| 9 | Product options CRUD (6 endpoints) | ✓ Đã thêm |
| 10 | Option value priceAdjustment | ✓ Đã thêm |
| 11 | Variant finalPrice (base + adjustments) | ✓ Đã thêm |
| 12 | GET /api/products/{id} (public product detail) | ✓ Đã thêm |
| 13 | PUT /api/admin/bundle/components/{id} | ✓ Đã thêm |
| 14 | GET list bundle option groups & components | ✓ Đã thêm |
| 15 | Tài liệu: document DELETE variant, PUT variant | ✓ Đã thêm |
| 16 | Admin Payment module (4 endpoints) | ✓ Đã thêm |
| 17 | GiftBox add/remove items | ✓ Đã thêm |
| 18 | OrderResponse thêm requiredAdvanceAmount | ✓ Đã thêm |

---

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
