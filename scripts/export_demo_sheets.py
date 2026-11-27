#!/usr/bin/env python3
"""Export Giftly demo rows and manual API cases into a dependency-free XLSX workbook."""

import argparse
import json
import os
import subprocess
import zipfile
from decimal import Decimal
from pathlib import Path
from xml.sax.saxutils import escape, quoteattr


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_OUTPUT = ROOT / "docs" / "giftly_demo_sheets.xlsx"

DEMO_USER = "t.email LIKE '%@example.test'"
DEMO_VARIANT = "t.sku LIKE 'DEMO-%'"
DEMO_PRODUCT = "EXISTS (SELECT 1 FROM product_variant dv WHERE dv.product_id = t.id AND dv.sku LIKE 'DEMO-%')"
DEMO_ORDER = "t.order_number LIKE 'GLY-DEMO-%'"

TABLE_SCOPES = {
    "address": f"t.user_id IN (SELECT id FROM \"user\" WHERE {DEMO_USER.replace('t.', '')})",
    "bundle_component": "t.bundle_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "bundle_option": "t.option_group_id IN (SELECT id FROM bundle_option_group WHERE bundle_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')) OR t.product_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "bundle_option_group": "t.bundle_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "cart": f"t.user_id IN (SELECT id FROM \"user\" WHERE {DEMO_USER.replace('t.', '')})",
    "cart_item": "t.cart_id IN (SELECT id FROM cart WHERE user_id IN (SELECT id FROM \"user\" WHERE email LIKE '%@example.test'))",
    "cart_item_option_selection": "t.cart_item_id IN (SELECT ci.id FROM cart_item ci JOIN cart c ON c.id = ci.cart_id JOIN \"user\" u ON u.id = c.user_id WHERE u.email LIKE '%@example.test')",
    "category": "EXISTS (SELECT 1 FROM product dp JOIN product_variant dv ON dv.product_id = dp.id WHERE dp.category_id = t.id AND dv.sku LIKE 'DEMO-%')",
    "flyway_schema_history": "TRUE",
    "gift_box_config": f"t.user_id IN (SELECT id FROM \"user\" WHERE {DEMO_USER.replace('t.', '')})",
    "gift_box_config_item": "t.config_id IN (SELECT id FROM gift_box_config WHERE user_id IN (SELECT id FROM \"user\" WHERE email LIKE '%@example.test'))",
    "gift_box_item": "t.config_id IN (SELECT id FROM gift_box_config WHERE user_id IN (SELECT id FROM \"user\" WHERE email LIKE '%@example.test'))",
    "gift_box_variant_detail": "t.product_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "order": DEMO_ORDER,
    "order_item": "t.order_id IN (SELECT id FROM \"order\" WHERE order_number LIKE 'GLY-DEMO-%')",
    "order_item_component": "t.order_item_id IN (SELECT oi.id FROM order_item oi JOIN \"order\" o ON o.id = oi.order_id WHERE o.order_number LIKE 'GLY-DEMO-%')",
    "payment": "t.order_id IN (SELECT id FROM \"order\" WHERE order_number LIKE 'GLY-DEMO-%')",
    "product": DEMO_PRODUCT,
    "product_image": "t.product_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%') OR t.public_id LIKE 'giftly-demo/%'",
    "product_option": "t.product_id IN (SELECT p.id FROM product p JOIN product_variant pv ON pv.product_id = p.id WHERE pv.sku LIKE 'DEMO-%')",
    "product_option_value": "t.option_id IN (SELECT po.id FROM product_option po JOIN product p ON p.id = po.product_id JOIN product_variant pv ON pv.product_id = p.id WHERE pv.sku LIKE 'DEMO-%')",
    "product_variant": DEMO_VARIANT,
    "product_variant_option_value": "t.variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "product_variant_policy": "t.product_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "review": "t.product_variant_id IN (SELECT id FROM product_variant WHERE sku LIKE 'DEMO-%')",
    "review_image": "t.review_id IN (SELECT r.id FROM review r JOIN product_variant pv ON pv.id = r.product_variant_id WHERE pv.sku LIKE 'DEMO-%')",
    "role": "t.name = 'ROLE_USER'",
    "user": DEMO_USER,
    "user_role": f"t.user_id IN (SELECT id FROM \"user\" WHERE {DEMO_USER.replace('t.', '')})",
}

TEST_CASES = [
    ("TC-001", "Auth", "Đăng nhập khách hàng demo hợp lệ", "Positive", "API và database demo đang chạy.", "POST /api/auth/login với email/password demo.", "user: minhanh.nguyen01@example.test", "HTTP 200; trả access token và success=true.", "", "Chưa chạy", "P1"),
    ("TC-002", "Auth", "Từ chối mật khẩu sai", "Negative", "Dùng tài khoản demo hợp lệ.", "POST /api/auth/login với password sai.", "user: minhanh.nguyen01@example.test", "Xác thực thất bại; không cấp token.", "", "Chưa chạy", "P1"),
    ("TC-003", "Catalog/Search", "Tìm keyword không dấu", "Positive", "Có sản phẩm Gấu bông Teddy Mây.", "GET /api/products?keyword=gaubong", "product: DEMO-TEDDY_BEAR-STD", "Kết quả chứa Gấu bông Teddy Mây.", "", "Chưa chạy", "P1"),
    ("TC-004", "Catalog/Search", "Tìm keyword có dấu", "Positive", "Có sản phẩm Gấu bông Teddy Mây.", "GET /api/products?keyword=gấu+bông", "product: DEMO-TEDDY_BEAR-STD", "Kết quả chứa cùng sản phẩm như tìm không dấu.", "", "Chưa chạy", "P1"),
    ("TC-005", "Catalog/Search", "Tìm gần đúng hoặc một phần từ", "Boundary", "Seed đã nạp.", "GET /api/products?keyword=gau", "product names/descriptions", "Có kết quả phù hợp theo cơ chế tìm kiếm gần đúng.", "", "Chưa chạy", "P2"),
    ("TC-006", "Catalog/Search", "Keyword không tồn tại", "Negative", "Seed đã nạp.", "GET /api/products?keyword=khong-co-san-pham-xyz", "Không cần dữ liệu đặc biệt.", "Danh sách rỗng; API vẫn trả response hợp lệ.", "", "Chưa chạy", "P2"),
    ("TC-007", "Catalog/Filter", "Lọc sản phẩm lẻ bằng alias REGULAR", "Positive", "Seed có sản phẩm REGULAR.", "GET /api/products?type=REGULAR", "products: regular items", "Chỉ trả sản phẩm có variant khớp loại.", "", "Chưa chạy", "P1"),
    ("TC-008", "Catalog/Filter", "Lọc set quà", "Positive", "Seed có set quà đóng sẵn.", "GET /api/products?type=SET", "product: gift-set-gratitude", "Kết quả có set quà và không lẫn sản phẩm lẻ.", "", "Chưa chạy", "P1"),
    ("TC-009", "Catalog/Filter", "Lọc hộp quà", "Positive", "Seed có hộp quà nhiều kích cỡ.", "GET /api/products?type=BOX", "product: gift-box-ivory", "Kết quả chỉ gồm loại hộp quà phù hợp.", "", "Chưa chạy", "P1"),
    ("TC-010", "Catalog/Filter", "Lọc sản phẩm được phép tùy chỉnh", "Positive", "Seed có sản phẩm custom eligible.", "GET /api/products?type=CUSTOM", "product: teddy-bear, rose-bouquet", "Kết quả chỉ gồm sản phẩm đủ điều kiện custom.", "", "Chưa chạy", "P2"),
    ("TC-011", "Catalog/Filter", "Giá biên min/max bao gồm", "Boundary", "Có variant giá 289000.", "GET /api/products?minPrice=289000&maxPrice=289000", "variant: DEMO-TEDDY_BEAR-STD", "Variant giá đúng bằng cả hai biên vẫn xuất hiện.", "", "Chưa chạy", "P1"),
    ("TC-012", "Catalog/Filter", "Kết hợp keyword, loại và khoảng giá", "Positive", "Seed đã nạp.", "GET /api/products?keyword=gaubong&type=REGULAR&minPrice=200000&maxPrice=500000", "product: teddy-bear", "Kết quả thỏa đồng thời tất cả bộ lọc.", "", "Chưa chạy", "P1"),
    ("TC-013", "Catalog/Pagination", "Phân trang cursor", "Boundary", "Có hơn một sản phẩm.", "GET /api/products?pageSize=1; tiếp tục bằng cursor trả về.", "product sheet", "Trang tiếp theo không lặp bản ghi; cursor/hasMore nhất quán.", "", "Chưa chạy", "P2"),
    ("TC-014", "Product Detail", "Thông tin SKU, kích thước và khối lượng", "Positive", "Sản phẩm có hai biến thể.", "GET /api/products/{productId}.", "variant: DEMO-TEDDY_BEAR-STD, DEMO-TEDDY_BEAR-ALT", "Mỗi variant trả SKU, price, length/width/height, units và weight.", "", "Chưa chạy", "P1"),
    ("TC-015", "Cart", "Lấy giỏ hàng hiện có", "Positive", "Đăng nhập tài khoản demo.", "GET /api/cart với Bearer token.", "cart, cart_item", "Trả các item hiện tại với quantity và variant/config đúng loại.", "", "Chưa chạy", "P1"),
    ("TC-016", "Cart", "Thêm variant sản phẩm vào giỏ", "Positive", "Đăng nhập; variant còn tồn kho.", "POST /api/cart/items với productVariantId và quantity=1.", "variant: DEMO-TEDDY_BEAR-STD", "Item được lưu; response trả đúng SKU/giá/quantity.", "", "Chưa chạy", "P1"),
    ("TC-017", "Cart", "Từ chối quantity bằng 0", "Negative", "Đăng nhập.", "POST /api/cart/items với quantity=0.", "cart request", "Validation trả lỗi; không tạo cart_item.", "", "Chưa chạy", "P1"),
    ("TC-018", "Cart", "Từ chối quantity vượt tồn kho", "Boundary", "Đăng nhập; dùng variant có tồn kho thấp.", "Thêm quantity lớn hơn stock_quantity.", "product_variant.stock_quantity", "Báo lỗi phù hợp; dữ liệu tồn kho/giỏ không bị sai.", "", "Chưa chạy", "P1"),
    ("TC-019", "Cart", "Thêm cấu hình hộp quà custom vào giỏ", "Positive", "Đăng nhập; có gift_box_config.", "POST /api/cart/items với giftBoxConfigId và quantity=1.", "gift_box_config: demo config", "Lưu đúng gift_box_config_id, product_variant_id để trống.", "", "Chưa chạy", "P1"),
    ("TC-020", "Cart/Constraint", "Không nhận item thiếu cả hai loại", "Negative", "Có quyền gọi API.", "Gửi item không có productVariantId và giftBoxConfigId.", "cart_item constraint chk_cart_item_type", "Request bị từ chối; không phát sinh lỗi 500 hoặc row lỗi.", "", "Chưa chạy", "P1"),
    ("TC-021", "Gift Box", "Danh sách hộp tùy chỉnh của user", "Positive", "Đăng nhập tài khoản demo có cấu hình.", "GET /api/gift-boxes.", "gift_box_config: 8 configs", "Chỉ trả cấu hình thuộc user hiện tại và trạng thái review đúng.", "", "Chưa chạy", "P1"),
    ("TC-022", "Gift Box", "Tạo hộp với variant hợp lệ", "Positive", "Đăng nhập; chọn hộp có sức chứa phù hợp.", "POST /api/gift-boxes với box variant và các item.", "gift_box_config_item; product_variant", "Tạo hộp và các item; tổng giá/sức chứa hợp lệ.", "", "Chưa chạy", "P1"),
    ("TC-023", "Gift Box", "Từ chối quantity item không hợp lệ", "Negative", "Đăng nhập.", "POST /api/gift-boxes hoặc thêm item với quantity=0.", "gift_box_config_item.quantity", "Validation từ chối; không tạo item không hợp lệ.", "", "Chưa chạy", "P2"),
    ("TC-024", "Gift Box", "Các trạng thái review cấu hình", "Boundary", "Seed có config APPROVED và PENDING.", "Đọc danh sách/chi tiết custom box.", "gift_box_config.review_status", "Hiển thị đúng trạng thái; review_note null/không null đúng dữ liệu.", "", "Chưa chạy", "P2"),
    ("TC-025", "Bundle", "Chi tiết set gồm component và option", "Positive", "Seed có các set đóng sẵn.", "GET sản phẩm set qua /api/products/{productId}.", "bundle_component, bundle_option_group, bundle_option", "Components, vai trò và lựa chọn thiệp hiển thị đúng.", "", "Chưa chạy", "P1"),
    ("TC-026", "Order", "Tạo đơn từ giỏ hợp lệ", "Positive", "Đăng nhập; cart có item; có địa chỉ giao.", "POST /api/orders.", "cart_item, address, order", "Tạo order/item snapshots; subtotal=sum item; total=subtotal+shipping.", "", "Chưa chạy", "P1"),
    ("TC-027", "Order", "Không tạo đơn từ giỏ trống", "Negative", "Đăng nhập; giỏ trống.", "POST /api/orders.", "cart sheet", "Trả lỗi nghiệp vụ; không tạo order rỗng.", "", "Chưa chạy", "P1"),
    ("TC-028", "Order", "Đơn nhiều trạng thái hiển thị đúng", "Positive", "Đăng nhập tài khoản có đơn demo.", "GET /api/orders và GET /api/orders/{orderId}.", "order: GLY-DEMO-2026-*", "Status và snapshot theo đơn khớp dữ liệu lưu.", "", "Chưa chạy", "P1"),
    ("TC-029", "Order", "Hủy đơn theo điều kiện cho phép", "Boundary", "Đăng nhập; chọn đơn còn được hủy.", "PUT /api/orders/{orderId}/cancel.", "order status PENDING_CONFIRMATION/CONFIRMED", "Đơn chuyển CANCELLED; đơn không đủ điều kiện bị từ chối.", "", "Chưa chạy", "P1"),
    ("TC-030", "Order/Policy", "Snapshot hàng dễ hỏng và trả trước", "Boundary", "Giỏ có hoa hoặc thực phẩm.", "Tạo đơn; kiểm tra order_item snapshots.", "variant_policy: rose-bouquet, dark-chocolate", "Lưu is_perishable, shelf_life, return policy và advance amount chính xác.", "", "Chưa chạy", "P1"),
    ("TC-031", "Payment", "Thanh toán COD/ZaloPay", "Positive", "Có order hợp lệ; đăng nhập.", "POST /api/payments với COD; lặp lại với ZALOPAY.", "payment_method COD/ZALOPAY", "Payment gắn đúng order, stage và amount.", "", "Chưa chạy", "P1"),
    ("TC-032", "Payment", "Callback ZaloPay thành công và thất bại", "Boundary", "Có payment ZaloPay.", "POST /api/payments/{paymentId}/zalopay/callback?success=true rồi false trên payment khác.", "payment sheet", "Trạng thái thanh toán đổi đúng từng nhánh callback.", "", "Chưa chạy", "P1"),
    ("TC-033", "Review", "Đọc review và lọc rating", "Positive", "Sản phẩm có review.", "GET /api/reviews/product/{productId}/paginated?rating=5.", "review, review_image", "Trả đúng sản phẩm/rating, paging và ảnh review nếu có.", "", "Chưa chạy", "P2"),
    ("TC-034", "Review", "Từ chối rating ngoài 1..5", "Negative", "Đăng nhập.", "POST /api/reviews multipart với rating=0, sau đó rating=6.", "review.rating constraint/validation", "Cả hai request bị từ chối; không tạo review.", "", "Chưa chạy", "P1"),
    ("TC-035", "Review", "Chỉ đánh giá sản phẩm đã mua", "Negative", "Đăng nhập; chọn sản phẩm chưa mua.", "POST /api/reviews với order item không thuộc user hoặc không tồn tại.", "order_item, review", "Không tạo review trái phép; trả lỗi 4xx phù hợp.", "", "Chưa chạy", "P1"),
    ("TC-036", "Admin", "Quản lý product/variant và SKU trùng", "Boundary", "Đăng nhập admin.", "Tạo/cập nhật product và variant; thử SKU trùng trong cùng product.", "product, product_variant, product_option_value", "Dữ liệu hợp lệ lưu đủ dimensions/weight; SKU trùng bị từ chối.", "", "Chưa chạy", "P1"),
    ("TC-037", "Admin", "Quản lý payment/order/review", "Boundary", "Đăng nhập admin.", "Thử cập nhật trạng thái hợp lệ và trạng thái không hợp lệ.", "order, payment, review", "Chỉ chuyển trạng thái theo rule; response và DB nhất quán.", "", "Chưa chạy", "P2"),
    ("TC-038", "Admin/Stock", "Variant hết hàng và ngừng bán", "Boundary", "Dùng variant demo; hoàn nguyên tồn kho sau test.", "Admin đặt stock=0 hoặc status OUT_OF_STOCK/DISCONTINUED; thử xem và thêm giỏ.", "product_variant.status, stock_quantity", "Không bán variant hết hàng/ngừng bán; catalog phản ánh trạng thái.", "", "Chưa chạy", "P1"),
]


def run_psql(container, db_user, database, sql):
    command = [
        "docker", "exec", container, "psql", "-X", "-A", "-t",
        "-U", db_user, "-d", database, "-v", "ON_ERROR_STOP=1", "-c", sql,
    ]
    result = subprocess.run(command, check=True, capture_output=True, text=True)
    return result.stdout.strip()


def load_json_query(container, db_user, database, sql):
    raw = run_psql(container, db_user, database, sql)
    if not raw:
        return []
    return json.loads(raw, parse_float=Decimal)


def quote_identifier(value):
    return '"' + value.replace('"', '""') + '"'


def table_rows(container, db_user, database, table_name, first_column):
    condition = TABLE_SCOPES[table_name]
    table = quote_identifier(table_name)
    first_column = quote_identifier(first_column)
    if table_name == "user":
        row_json = "to_jsonb(t) || jsonb_build_object('password', '[REDACTED]')"
    else:
        row_json = "to_jsonb(t)"
    sql = (
        "SELECT COALESCE(json_agg(exported.row_data ORDER BY "
        "COALESCE(exported.row_data->>'id', exported.row_data->>'installed_rank')), '[]'::json)::text "
        f"FROM (SELECT {row_json} AS row_data FROM {table} t "
        f"WHERE {condition} ORDER BY t.{first_column}) exported"
    )
    return load_json_query(container, db_user, database, sql)


def cell_text(value):
    if value is None:
        return ""
    if isinstance(value, bool):
        return "TRUE" if value else "FALSE"
    if isinstance(value, Decimal):
        return format(value, "f")
    if isinstance(value, (dict, list)):
        return json.dumps(value, ensure_ascii=False, separators=(",", ":"))
    return str(value)


def column_name(index):
    result = ""
    while index:
        index, remainder = divmod(index - 1, 26)
        result = chr(65 + remainder) + result
    return result


def xml_text(value):
    safe = "".join(
        char for char in value
        if char in "\t\n\r" or ord(char) >= 0x20
    )
    return escape(safe)


def worksheet_xml(rows):
    max_columns = max((len(row) for row in rows), default=1)
    max_rows = max(len(rows), 1)
    last_cell = f"{column_name(max_columns)}{max_rows}"
    xml_rows = []
    for row_index, row in enumerate(rows, start=1):
        cells = []
        for column_index, value in enumerate(row, start=1):
            cell_ref = f"{column_name(column_index)}{row_index}"
            text = xml_text(cell_text(value))
            cells.append(
                f'<c r={quoteattr(cell_ref)} t="inlineStr"><is><t xml:space="preserve">{text}</t></is></c>'
            )
        xml_rows.append(f'<row r="{row_index}">{"".join(cells)}</row>')
    auto_filter = f'<autoFilter ref="A1:{last_cell}"/>' if len(rows) > 1 else ""
    return (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
        f'<dimension ref="A1:{last_cell}"/>'
        '<sheetViews><sheetView workbookViewId="0"><pane ySplit="1" topLeftCell="A2" '
        'activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>'
        '<sheetFormatPr defaultRowHeight="15"/><sheetData>'
        f'{"".join(xml_rows)}</sheetData>{auto_filter}</worksheet>'
    )


def workbook_files(sheets):
    content_overrides = [
        '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>'
    ]
    workbook_sheets = []
    relationships = []
    sheet_files = {}
    for index, (name, rows) in enumerate(sheets, start=1):
        sheet_name = name[:31]
        workbook_sheets.append(
            f'<sheet name={quoteattr(sheet_name)} sheetId="{index}" r:id="rId{index}"/>'
        )
        relationships.append(
            f'<Relationship Id="rId{index}" '
            'Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" '
            f'Target="worksheets/sheet{index}.xml"/>'
        )
        content_overrides.append(
            f'<Override PartName="/xl/worksheets/sheet{index}.xml" '
            'ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
        )
        sheet_files[f"xl/worksheets/sheet{index}.xml"] = worksheet_xml(rows)

    content_types = (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
        '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
        '<Default Extension="xml" ContentType="application/xml"/>'
        f'{"".join(content_overrides)}</Types>'
    )
    root_relationships = (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        '<Relationship Id="rId1" '
        'Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" '
        'Target="xl/workbook.xml"/></Relationships>'
    )
    workbook = (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" '
        'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">'
        f'<sheets>{"".join(workbook_sheets)}</sheets></workbook>'
    )
    workbook_relationships = (
        '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
        '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
        f'{"".join(relationships)}</Relationships>'
    )
    return {
        "[Content_Types].xml": content_types,
        "_rels/.rels": root_relationships,
        "xl/workbook.xml": workbook,
        "xl/_rels/workbook.xml.rels": workbook_relationships,
        **sheet_files,
    }


def test_case_rows():
    headers = ["Case ID", "Module", "Scenario", "Type", "Preconditions", "Steps/API", "Data / Sheet refs", "Expected result", "Actual result", "Status", "Priority"]
    return [headers, *TEST_CASES]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--container", default=os.getenv("GIFTLY_DB_CONTAINER", "postgres-giftly"))
    parser.add_argument("--db-user", default=os.getenv("GIFTLY_DB_USER", "ledan"))
    parser.add_argument("--database", default=os.getenv("GIFTLY_DB_NAME", "giftly"))
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()

    metadata_sql = (
        "SELECT COALESCE(json_agg(metadata ORDER BY table_name, ordinal_position), '[]'::json)::text "
        "FROM (SELECT table_name, column_name, data_type, is_nullable, ordinal_position "
        "FROM information_schema.columns WHERE table_schema = 'public') metadata"
    )
    metadata = load_json_query(args.container, args.db_user, args.database, metadata_sql)
    columns_by_table = {}
    for column in metadata:
        columns_by_table.setdefault(column["table_name"], []).append(column)

    table_names = sorted(columns_by_table)
    missing_scopes = sorted(set(table_names) - set(TABLE_SCOPES))
    if missing_scopes:
        raise SystemExit(f"Add demo-only export scopes before exporting new tables: {', '.join(missing_scopes)}")

    data_sheets = []
    row_counts = []
    schema_rows = [["Table", "Column", "Data type", "Nullable", "Ordinal position"]]
    for table_name in table_names:
        table_columns = columns_by_table[table_name]
        names = [column["column_name"] for column in table_columns]
        first_column = names[0]
        data = table_rows(args.container, args.db_user, args.database, table_name, first_column)
        rows = [names]
        rows.extend([[row.get(column_name) for column_name in names] for row in data])
        data_sheets.append((table_name, rows))
        row_counts.append((table_name, len(data)))
        schema_rows.extend([
            [table_name, column["column_name"], column["data_type"], column["is_nullable"], column["ordinal_position"]]
            for column in table_columns
        ])

    if "user" in columns_by_table and "password" in [c["column_name"] for c in columns_by_table["user"]]:
        password_note = "Cột password được giữ trong sheet user nhưng giá trị đã thay bằng [REDACTED]."
    else:
        password_note = ""
    readme_rows = [
        ["Giftly demo data & test cases", ""],
        ["Workbook generated", "Tạo từ database local; chạy lại scripts/export_demo_sheets.py để cập nhật."],
        ["Data scope", "Chỉ xuất bản ghi demo Giftly (email @example.test, SKU DEMO-*, order GLY-DEMO-*); không xuất dữ liệu người dùng khác."],
        ["Password handling", password_note],
        ["Demo login", "minhanh.nguyen01@example.test / GiftlyDemo!2026"],
        ["Schema", "Tab Schema liệt kê mọi cột public table và kiểu dữ liệu."],
        ["Table tabs", "Mỗi public table có một tab riêng; bảng không có bản ghi demo vẫn có header đầy đủ."],
        ["Internal table", "flyway_schema_history được giữ để tiện kiểm tra phiên bản migration."],
        ["Test Cases", "38 case API/UI tích cực, âm tính và biên; cột Actual result/Status để cập nhật khi chạy."],
        ["Generated table count", len(table_names)],
        ["Table", "Demo row count"],
        *[[table_name, count] for table_name, count in row_counts],
    ]
    sheets = [("README", readme_rows), ("Schema", schema_rows), *data_sheets, ("Test Cases", test_case_rows())]
    names = [name[:31].casefold() for name, _ in sheets]
    if len(names) != len(set(names)):
        raise SystemExit("Workbook sheet names must be unique (case-insensitive).")

    output = args.output if args.output.is_absolute() else ROOT / args.output
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as workbook:
        for filename, contents in workbook_files(sheets).items():
            workbook.writestr(filename, contents.encode("utf-8"))
    print(f"Created {output} with {len(table_names)} database table tabs and {len(TEST_CASES)} test cases.")
    print(f"Included {sum(count for _, count in row_counts)} demo-scoped records across {len(row_counts)} tables.")


if __name__ == "__main__":
    main()