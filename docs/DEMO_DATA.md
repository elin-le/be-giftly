# Bộ dữ liệu demo

`scripts/dev_seed.sql` nạp dữ liệu phục vụ phát triển và kiểm thử giao diện/API. Đây không phải Flyway migration; script yêu cầu schema đã chạy tới Flyway V20, chạy trong một transaction và có thể chạy lại mà không nhân bản các bản ghi demo. Script không xóa hoặc sửa các bản ghi không thuộc bộ demo.

## Nội dung

- 100 sản phẩm thuộc nhiều danh mục và loại: sản phẩm lẻ, set quà, hộp quà, phụ kiện đóng gói và thiệp; mỗi sản phẩm có ảnh, SKU và biến thể có giá, tồn kho, kích thước/khối lượng.
- 191 biến thể, kèm option, chính sách bán/đổi trả và thông tin kích thước hộp phù hợp.
- 100 tài khoản khách hàng, 108 địa chỉ, 100 giỏ hàng và 8 cấu hình hộp quà tùy chỉnh với từng component được lưu thành variant.
- 1.000 đơn hàng ở nhiều trạng thái, 3.000 dòng hàng, thanh toán và review tiếng Việt; đơn được phân bổ qua cả 100 khách hàng và catalog.
- Bộ workbook có tab riêng cho mọi bảng trong schema, tab `Schema` liệt kê tất cả cột, và 38 case API/UI tích cực, âm tính, biên.

Tên, địa chỉ và số điện thoại là dữ liệu giả lập. Email dùng domain dành riêng `.test`, có dạng tên người dùng thực tế nhưng không thể nhận thư. Script xác nhận tối thiểu 100 sản phẩm, 100 khách hàng và 1.000 đơn trước khi commit; chạy lại sẽ cập nhật các bản ghi demo theo ID ổn định.

## Nạp dữ liệu vào database local

Từ thư mục gốc dự án:

```bash
docker exec -i postgres-giftly psql -U ledan -d giftly -v ON_ERROR_STOP=1 < scripts/dev_seed.sql
```

Đổi tên container, user hoặc database nếu môi trường local của bạn khác. Ứng dụng/database cần được migrate tới V20 trước khi seed. Không chạy script này trên production.

## Workbook dữ liệu và test cases

Sau khi nạp seed, tạo/cập nhật workbook `docs/giftly_demo_sheets.xlsx`:

```bash
python3 scripts/export_demo_sheets.py
```

Exporter đọc schema và dữ liệu từ database local, không cần cài thư viện Python bổ sung. Có một tab cho mỗi bảng public (kể cả bảng chưa có bản ghi demo), tab `Schema`, `README` và `Test Cases`. File chỉ chứa dữ liệu demo; cột mật khẩu được thay bằng `[REDACTED]`, còn bảng `flyway_schema_history` chỉ chứa metadata migration. Có thể đổi kết nối bằng `--container`, `--db-user`, `--database` hoặc các biến môi trường `GIFTLY_DB_CONTAINER`, `GIFTLY_DB_USER`, `GIFTLY_DB_NAME`.

Các case là checklist manual để QA ghi kết quả; chúng không tự chạy API và mặc định có trạng thái `Chưa chạy`.

## Tài khoản đăng nhập demo

Các khách hàng trong file seed có thể đăng nhập bằng email tương ứng và cùng mật khẩu:

```text
GiftlyDemo!2026
```

Ví dụ email: `minhanh.nguyen01@example.test`, `ngochan.tran02@example.test`. Tài khoản admin hiện có không bị thay đổi.