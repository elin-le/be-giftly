-- Repeatable local development data for Giftly. This is intentionally not a Flyway migration.
BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM flyway_schema_history
        WHERE version = '20' AND success = TRUE
    ) THEN
        RAISE EXCEPTION 'Giftly demo seed requires a database migrated through Flyway V20';
    END IF;
END $$;

CREATE TEMP TABLE _demo_product_seed (
    seq INTEGER PRIMARY KEY,
    slug TEXT UNIQUE NOT NULL,
    category_name TEXT NOT NULL,
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    product_type TEXT NOT NULL,
    product_status TEXT NOT NULL DEFAULT 'AVAILABLE',
    custom_eligible BOOLEAN NOT NULL DEFAULT FALSE,
    price NUMERIC(19, 2) NOT NULL,
    alt_price NUMERIC(19, 2),
    option_name TEXT,
    std_label TEXT NOT NULL,
    alt_label TEXT,
    stock_quantity INTEGER NOT NULL,
    alt_stock_quantity INTEGER,
    length DOUBLE PRECISION NOT NULL,
    width DOUBLE PRECISION NOT NULL,
    height DOUBLE PRECISION NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    dimension_unit TEXT NOT NULL DEFAULT 'cm',
    weight_unit TEXT NOT NULL DEFAULT 'kg',
    is_perishable BOOLEAN NOT NULL DEFAULT FALSE,
    shelf_life_hours INTEGER,
    advance_payment_percent NUMERIC(5, 2) NOT NULL DEFAULT 0
) ON COMMIT DROP;

INSERT INTO _demo_product_seed VALUES
(1, 'teddy-bear', 'Thú bông', 'Gấu bông Teddy Mây', 'Gấu bông lông mịn, đường may chắc chắn; phù hợp làm quà sinh nhật và quà kỷ niệm.', 'REGULAR', 'AVAILABLE', TRUE, 289000, 459000, 'Kích cỡ', '25 cm', '40 cm', 38, 21, 25, 18, 30, 0.42, 'cm', 'kg', FALSE, NULL, 0),
(2, 'rose-bouquet', 'Hoa tươi', 'Bó hồng Ecuador đỏ', 'Hoa hồng Ecuador tuyển cành, phối lá bạc và giấy gói màu kem; giao trong ngày tại nội thành.', 'REGULAR', 'AVAILABLE', TRUE, 399000, 629000, 'Số lượng cành', '9 cành', '15 cành', 18, 12, 45, 28, 12, 0.85, 'cm', 'kg', TRUE, 120, 50),
(3, 'sunflower-bouquet', 'Hoa tươi', 'Bó hướng dương Nắng Mai', 'Hướng dương theo mùa, phối lá xanh và giấy kraft; bó hoa được xử lý nước trước khi giao.', 'REGULAR', 'AVAILABLE', TRUE, 245000, 399000, 'Số lượng cành', '5 cành', '9 cành', 16, 10, 42, 28, 12, 0.72, 'cm', 'kg', TRUE, 120, 50),
(4, 'soy-candle', 'Nến & hương thơm', 'Nến thơm Gỗ tuyết tùng', 'Nến sáp đậu nành, hương gỗ ấm dịu, thời gian cháy khoảng 28–45 giờ tùy dung tích.', 'REGULAR', 'AVAILABLE', TRUE, 189000, 279000, 'Khối lượng', '120 g', '200 g', 46, 28, 7, 7, 8, 0.32, 'cm', 'kg', FALSE, NULL, 0),
(5, 'dark-chocolate', 'Bánh kẹo & đồ uống', 'Socola đen 70% cacao', 'Socola đen vị cacao rõ, đóng gói kín để giữ hương vị; sản xuất tại Việt Nam.', 'REGULAR', 'AVAILABLE', TRUE, 125000, 219000, 'Khối lượng', '80 g', '160 g', 55, 33, 12, 8, 2, 0.18, 'cm', 'kg', TRUE, 4320, 50),
(6, 'oolong-tea', 'Bánh kẹo & đồ uống', 'Trà ô long sen vàng', 'Trà ô long rang nhẹ ướp hương sen, đóng túi zip có van giữ hương.', 'REGULAR', 'AVAILABLE', TRUE, 159000, 289000, 'Khối lượng', '50 g', '100 g', 42, 25, 10, 8, 12, 0.16, 'cm', 'kg', FALSE, NULL, 0),
(7, 'ceramic-mug', 'Đồ dùng cá nhân', 'Ly sứ men kem An Nhiên', 'Ly sứ men mờ dung tích 350–500 ml, dùng được với máy rửa chén.', 'REGULAR', 'AVAILABLE', TRUE, 179000, 239000, 'Dung tích', '350 ml', '500 ml', 30, 18, 9, 9, 10, 0.38, 'cm', 'kg', FALSE, NULL, 0),
(8, 'silk-scarf', 'Đồ dùng cá nhân', 'Khăn lụa hoa nhí Mùa Hạ', 'Khăn lụa mềm họa tiết hoa nhí, hoàn thiện cuốn biên thủ công.', 'REGULAR', 'AVAILABLE', TRUE, 349000, 579000, 'Kích cỡ', '45 × 45 cm', '65 × 65 cm', 24, 14, 45, 45, 0.2, 0.09, 'cm', 'kg', FALSE, NULL, 0),
(9, 'leather-wallet', 'Đồ dùng cá nhân', 'Ví da mini thủ công', 'Ví da thật dáng gọn, có ngăn thẻ và ngăn tiền gấp; hoàn thiện thủ công tại Việt Nam.', 'REGULAR', 'AVAILABLE', TRUE, 469000, 629000, 'Màu sắc', 'Nâu hạt dẻ', 'Đen nhám', 19, 15, 11, 9, 2, 0.12, 'cm', 'kg', FALSE, NULL, 0),
(10, 'dried-flower', 'Hoa tươi', 'Bình hoa khô Mộc Lan', 'Hoa khô phối cỏ đuôi thỏ và lavender, cắm sẵn trong bình gốm nhỏ.', 'REGULAR', 'AVAILABLE', TRUE, 299000, 449000, 'Kích cỡ', 'Nhỏ', 'Lớn', 17, 11, 18, 18, 28, 0.48, 'cm', 'kg', FALSE, NULL, 0),
(11, 'acacia-honey', 'Bánh kẹo & đồ uống', 'Mật ong hoa cà phê', 'Mật ong nguyên chất thu hoạch theo mùa, lọ thủy tinh có niêm phong.', 'REGULAR', 'AVAILABLE', TRUE, 199000, 349000, 'Dung tích', '250 ml', '500 ml', 31, 18, 7, 7, 13, 0.42, 'cm', 'kg', FALSE, NULL, 0),
(12, 'drip-coffee', 'Bánh kẹo & đồ uống', 'Cà phê drip Đà Lạt', 'Cà phê Arabica rang vừa, đóng gói thành từng túi lọc tiện pha tại nhà hoặc văn phòng.', 'REGULAR', 'AVAILABLE', TRUE, 139000, 249000, 'Số gói', '5 gói', '10 gói', 44, 27, 16, 11, 8, 0.22, 'cm', 'kg', FALSE, NULL, 0),
(13, 'lavender-sachet', 'Nến & hương thơm', 'Túi thơm lavender', 'Túi thơm vải linen chứa hoa lavender khô, phù hợp đặt trong tủ quần áo hoặc ngăn kéo.', 'REGULAR', 'AVAILABLE', TRUE, 69000, NULL, NULL, 'Một túi', NULL, 52, NULL, 10, 8, 2, 0.04, 'cm', 'kg', FALSE, NULL, 0),
(14, 'wooden-frame', 'Đồ dùng cá nhân', 'Khung ảnh gỗ sồi tối giản', 'Khung gỗ sồi phủ dầu bảo vệ, có chân dựng và móc treo tường.', 'REGULAR', 'AVAILABLE', TRUE, 189000, 269000, 'Kích cỡ ảnh', '13 × 18 cm', '20 × 25 cm', 23, 16, 18, 13, 2, 0.34, 'cm', 'kg', FALSE, NULL, 0),
(15, 'linen-notebook', 'Đồ dùng cá nhân', 'Sổ tay bìa vải Linen', 'Sổ giấy kem định lượng 100 gsm, đóng gáy chỉ và có dây đánh dấu trang.', 'REGULAR', 'AVAILABLE', TRUE, 119000, 169000, 'Số trang', '160 trang', '240 trang', 37, 22, 21, 14, 2, 0.31, 'cm', 'kg', FALSE, NULL, 0),
(16, 'butter-cookies', 'Bánh kẹo & đồ uống', 'Bánh quy bơ hạnh nhân', 'Bánh quy nướng mẻ nhỏ, vị bơ thơm và hạnh nhân lát; đóng hộp thiếc kín.', 'REGULAR', 'AVAILABLE', TRUE, 99000, 179000, 'Khối lượng', '150 g', '300 g', 34, 20, 12, 10, 5, 0.24, 'cm', 'kg', TRUE, 2160, 50),
(17, 'tea-cup-set', 'Đồ dùng cá nhân', 'Bộ tách trà men ngọc', 'Bộ gồm hai tách và hai đĩa lót, men ngọc thủ công với sắc độ tự nhiên.', 'REGULAR', 'AVAILABLE', TRUE, 249000, 399000, 'Số lượng', '2 tách', '4 tách', 18, 12, 22, 18, 10, 0.78, 'cm', 'kg', FALSE, NULL, 0),
(18, 'succulent-pot', 'Hoa tươi', 'Sen đá trong chậu gốm', 'Sen đá khỏe, trồng trong chậu gốm thoát nước; màu cây thay đổi nhẹ theo mùa.', 'REGULAR', 'AVAILABLE', TRUE, 129000, 219000, 'Kích cỡ chậu', '9 cm', '14 cm', 29, 17, 12, 12, 16, 0.62, 'cm', 'kg', TRUE, 720, 50),
(19, 'gift-box-ivory', 'Hộp quà & bao bì', 'Hộp quà nam châm màu kem', 'Hộp giấy cứng nắp nam châm, lót giấy mềm; phù hợp quà sinh nhật và tri ân.', 'READY_MADE_GIFT_BOX', 'AVAILABLE', TRUE, 159000, 249000, 'Kích cỡ', '20 cm', '30 cm', 26, 17, 20, 16, 10, 0.42, 'cm', 'kg', FALSE, NULL, 0),
(20, 'gift-box-forest', 'Hộp quà & bao bì', 'Hộp quà xanh rêu', 'Hộp quà cứng bọc giấy mỹ thuật xanh rêu, kèm khay lót có thể tháo rời.', 'READY_MADE_GIFT_BOX', 'AVAILABLE', TRUE, 179000, 269000, 'Kích cỡ', '22 cm', '32 cm', 22, 14, 22, 17, 11, 0.48, 'cm', 'kg', FALSE, NULL, 0),
(21, 'gift-box-midnight', 'Hộp quà & bao bì', 'Hộp quà xanh navy', 'Hộp nắp nam châm xanh navy, hoàn thiện viền ánh kim kín đáo.', 'READY_MADE_GIFT_BOX', 'AVAILABLE', TRUE, 189000, 279000, 'Kích cỡ', '22 cm', '32 cm', 19, 12, 22, 17, 11, 0.5, 'cm', 'kg', FALSE, NULL, 0),
(22, 'gift-set-gratitude', 'Quà tặng đóng sẵn', 'Set quà Lời cảm ơn', 'Set gồm socola cacao, bó hồng nhỏ và thiệp viết tay; đóng trong hộp giấy màu kem.', 'READY_MADE_GIFT_SET', 'AVAILABLE', FALSE, 799000, 1199000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ', 14, 9, 28, 22, 14, 1.35, 'cm', 'kg', FALSE, NULL, 0),
(23, 'gift-set-birthday', 'Quà tặng đóng sẵn', 'Set quà Sinh nhật dịu dàng', 'Set sinh nhật gồm gấu bông, nến thơm và thiệp; có thể thêm lời chúc khi đặt hàng.', 'READY_MADE_GIFT_SET', 'AVAILABLE', FALSE, 1090000, 1590000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ', 12, 8, 30, 24, 16, 1.6, 'cm', 'kg', FALSE, NULL, 0),
(24, 'gift-set-new-home', 'Quà tặng đóng sẵn', 'Set quà Tân gia ấm áp', 'Set tân gia gồm ly sứ, mật ong và chậu sen đá, sắp xếp trong hộp xanh rêu.', 'READY_MADE_GIFT_SET', 'AVAILABLE', FALSE, 950000, 1450000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ', 11, 7, 31, 25, 16, 1.7, 'cm', 'kg', FALSE, NULL, 0),
(25, 'gift-set-coffee', 'Quà tặng đóng sẵn', 'Set quà Cà phê cuối tuần', 'Set gồm cà phê drip, socola đen và ly sứ men kem; phù hợp tặng đồng nghiệp.', 'READY_MADE_GIFT_SET', 'AVAILABLE', FALSE, 679000, 999000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ', 15, 9, 28, 22, 14, 1.3, 'cm', 'kg', FALSE, NULL, 0),
(26, 'kraft-wrap', 'Hộp quà & bao bì', 'Giấy gói kraft vân mịn', 'Giấy kraft định lượng dày, khổ 50 × 70 cm, dễ gấp và phối ruy băng.', 'PACKAGING_ACCESSORY', 'AVAILABLE', TRUE, 19000, NULL, NULL, 'Một tờ', NULL, 90, NULL, 70, 50, 0.1, 0.08, 'cm', 'kg', FALSE, NULL, 0),
(27, 'satin-ribbon', 'Hộp quà & bao bì', 'Ruy băng satin màu champagne', 'Ruy băng satin bản 2 cm, bề mặt bóng nhẹ, phù hợp gói hộp quà cỡ vừa.', 'PACKAGING_ACCESSORY', 'AVAILABLE', TRUE, 19000, 39000, 'Chiều dài', '2 m', '5 m', 68, 42, 200, 2, 0.1, 0.03, 'cm', 'kg', FALSE, NULL, 0),
(28, 'paper-filler', 'Hộp quà & bao bì', 'Giấy vụn chèn hộp màu kem', 'Giấy chèn cắt sợi nhỏ, giúp cố định sản phẩm và hoàn thiện phần lót hộp.', 'PACKAGING_ACCESSORY', 'AVAILABLE', TRUE, 29000, 49000, 'Khối lượng', '50 g', '100 g', 61, 38, 15, 10, 5, 0.08, 'cm', 'kg', FALSE, NULL, 0),
(29, 'card-thank-you', 'Thiệp chúc mừng', 'Thiệp Cảm ơn chân thành', 'Thiệp giấy mỹ thuật màu ngà, in chữ ép kim nhẹ; kèm phong bì cùng tông.', 'GREETING_CARD', 'AVAILABLE', TRUE, 39000, NULL, NULL, 'Một thiệp', NULL, 72, NULL, 15, 10, 0.1, 0.03, 'cm', 'kg', FALSE, NULL, 0),
(30, 'card-birthday', 'Thiệp chúc mừng', 'Thiệp Chúc mừng sinh nhật', 'Thiệp gập minh họa hoa lá, mặt trong để trống để viết lời chúc riêng.', 'GREETING_CARD', 'AVAILABLE', TRUE, 39000, NULL, NULL, 'Một thiệp', NULL, 65, NULL, 15, 10, 0.1, 0.03, 'cm', 'kg', FALSE, NULL, 0),
(31, 'card-love', 'Thiệp chúc mừng', 'Thiệp Thương gửi bạn', 'Thiệp gập tối giản với họa tiết ép nhũ, kèm phong bì giấy kraft.', 'GREETING_CARD', 'AVAILABLE', TRUE, 39000, NULL, NULL, 'Một thiệp', NULL, 58, NULL, 15, 10, 0.1, 0.03, 'cm', 'kg', FALSE, NULL, 0),
(32, 'card-new-home', 'Thiệp chúc mừng', 'Thiệp Mừng nhà mới', 'Thiệp chúc mừng tân gia minh họa căn nhà nhỏ và nhành olive.', 'GREETING_CARD', 'AVAILABLE', TRUE, 39000, NULL, NULL, 'Một thiệp', NULL, 49, NULL, 15, 10, 0.1, 0.03, 'cm', 'kg', FALSE, NULL, 0);

ALTER TABLE _demo_product_seed ADD COLUMN custom_component_type TEXT;

UPDATE _demo_product_seed
SET custom_component_type = CASE
    WHEN product_type = 'READY_MADE_GIFT_BOX' THEN 'BOX'
    WHEN product_type = 'GREETING_CARD' THEN 'GREETING_CARD'
    WHEN slug = 'kraft-wrap' THEN 'WRAPPING'
    WHEN slug = 'satin-ribbon' THEN 'RIBBON'
    WHEN slug = 'paper-filler' THEN 'FILLER'
    ELSE NULL
END;

INSERT INTO _demo_product_seed (
    seq, slug, category_name, name, description, product_type, product_status,
    custom_eligible, price, alt_price, option_name, std_label, alt_label,
    stock_quantity, alt_stock_quantity, length, width, height, weight,
    dimension_unit, weight_unit, is_perishable, shelf_life_hours,
    advance_payment_percent, custom_component_type
)
SELECT extra.seq, extra.slug, extra.category_name, extra.name, extra.description,
       extra.product_type, 'AVAILABLE', extra.product_type <> 'READY_MADE_GIFT_SET',
       extra.price, extra.alt_price, extra.option_name, extra.std_label, extra.alt_label,
       18 + extra.seq % 41,
       CASE WHEN extra.alt_price IS NULL THEN NULL ELSE 8 + extra.seq % 23 END,
       12 + extra.seq % 20, 8 + extra.seq % 16, 5 + extra.seq % 18,
       0.18 + (extra.seq % 15) * 0.08, 'cm', 'kg',
       extra.category_name IN ('Hoa tươi', 'Bánh kẹo & đồ uống'),
       CASE WHEN extra.category_name = 'Hoa tươi' THEN 120
            WHEN extra.category_name = 'Bánh kẹo & đồ uống' THEN 4320
            ELSE NULL END,
       CASE WHEN extra.category_name IN ('Hoa tươi', 'Bánh kẹo & đồ uống') THEN 50 ELSE 0 END,
       extra.custom_component_type
FROM (VALUES
    (33, 'candle-amber-wood', 'Nến & hương thơm', 'Nến thơm Hổ phách và gỗ', 'Nến sáp đậu nành với hương hổ phách ấm, thoảng gỗ tuyết tùng; thích hợp thư giãn cuối ngày.', 'REGULAR', NULL, 229000, 349000, 'Dung tích', '120 g', '220 g'),
    (34, 'candle-white-tea', 'Nến & hương thơm', 'Nến thơm Trà trắng', 'Hương trà trắng thanh nhẹ kết hợp hoa nhài, đựng trong hũ thủy tinh có nắp gỗ.', 'REGULAR', NULL, 199000, 319000, 'Dung tích', '100 g', '200 g'),
    (35, 'reed-diffuser-fig', 'Nến & hương thơm', 'Tinh dầu khuếch tán Hương sung', 'Bộ khuếch tán que mây mùi quả sung và lá xanh, dùng được khoảng sáu tuần.', 'REGULAR', NULL, 289000, 429000, 'Dung tích', '100 ml', '180 ml'),
    (36, 'essential-oil-lavender', 'Nến & hương thơm', 'Tinh dầu oải hương Đà Lạt', 'Tinh dầu oải hương đóng chai tối màu, dùng với đèn xông hoặc máy khuếch tán.', 'REGULAR', NULL, 159000, 279000, 'Dung tích', '10 ml', '20 ml'),
    (37, 'linen-eye-pillow', 'Nến & hương thơm', 'Gối mắt thảo mộc thư giãn', 'Gối mắt bọc vải linen, ruột hạt lanh và hoa oải hương khô; có vỏ tháo giặt.', 'REGULAR', NULL, 179000, 269000, 'Màu vải', 'Be tự nhiên', 'Xanh olive'),
    (38, 'rose-bouquet-pink', 'Hoa tươi', 'Bó hồng pastel Sớm mai', 'Hoa hồng pastel phối cát tường trắng và lá bạc, gói giấy mỹ thuật tông kem.', 'REGULAR', NULL, 429000, 689000, 'Số lượng cành', '9 cành', '15 cành'),
    (39, 'tulip-bouquet', 'Hoa tươi', 'Bó tulip trắng Dịu dàng', 'Tulip trắng theo mùa được tuyển cành, bó cùng lá eucalyptus; giao trong ngày nội thành.', 'REGULAR', NULL, 549000, 899000, 'Số lượng cành', '7 cành', '12 cành'),
    (40, 'baby-breath-bouquet', 'Hoa tươi', 'Bó baby trắng Mây nhỏ', 'Hoa baby trắng phối cúc tana, bó gọn bằng giấy kraft và dây cotton.', 'REGULAR', NULL, 289000, 459000, 'Kích cỡ', 'Nhỏ', 'Lớn'),
    (41, 'daisy-bouquet', 'Hoa tươi', 'Bó cúc tana Vườn nắng', 'Cúc tana tươi cắt cành, phối lá xanh nhẹ nhàng; phù hợp tặng bạn bè và đồng nghiệp.', 'REGULAR', NULL, 319000, 499000, 'Số lượng cành', '12 cành', '20 cành'),
    (42, 'orchid-pot', 'Hoa tươi', 'Lan hồ điệp mini Trắng ngà', 'Chậu lan hồ điệp mini hai cành, trồng trong chậu gốm men mờ và có hướng dẫn chăm sóc.', 'REGULAR', NULL, 459000, 729000, 'Số cành', '2 cành', '3 cành'),
    (43, 'preserved-rose-dome', 'Hoa tươi', 'Hoa hồng bất tử trong chuông kính', 'Hoa hồng bảo quản đặt trong chuông kính, đế gỗ nhỏ; màu hoa có thể chênh nhẹ theo lô.', 'REGULAR', NULL, 389000, 589000, 'Màu hoa', 'Đỏ rượu', 'Hồng phấn'),
    (44, 'mini-sunflower-pot', 'Hoa tươi', 'Chậu hướng dương mini', 'Cây hướng dương trồng chậu đường kính 12 cm, giao kèm túi đất dinh dưỡng.', 'REGULAR', NULL, 149000, 229000, 'Kích cỡ chậu', '12 cm', '16 cm'),
    (45, 'chrysanthemum-arrangement', 'Hoa tươi', 'Bình cúc mẫu đơn kem', 'Cúc mẫu đơn màu kem cắm trong bình thủy tinh dáng trụ, phối lá bạc hài hòa.', 'REGULAR', NULL, 499000, 799000, 'Số lượng bông', '5 bông', '9 bông'),
    (46, 'mixed-flower-basket', 'Hoa tươi', 'Giỏ hoa đồng nội Tháng tư', 'Cúc tana, đồng tiền và hoa sao phối trong giỏ mây nhỏ, màu sắc thay đổi theo mùa.', 'REGULAR', NULL, 529000, 849000, 'Kích cỡ', 'Tiêu chuẩn', 'Đầy đặn'),
    (47, 'macaron-box', 'Bánh kẹo & đồ uống', 'Hộp macaron vị trái cây', 'Macaron vỏ giòn nhân mềm với các vị chanh, mâm xôi và trà xanh; bảo quản mát.', 'REGULAR', NULL, 189000, 329000, 'Số lượng bánh', '6 chiếc', '12 chiếc'),
    (48, 'chocolate-truffle', 'Bánh kẹo & đồ uống', 'Socola truffle cacao nguyên chất', 'Viên truffle nhân ganache phủ cacao, đóng hộp kín; nên dùng ngon nhất trong 30 ngày.', 'REGULAR', NULL, 239000, 399000, 'Số lượng viên', '9 viên', '16 viên'),
    (49, 'cashew-honey-roasted', 'Bánh kẹo & đồ uống', 'Hạt điều rang mật ong', 'Hạt điều Bình Phước rang mẻ nhỏ với mật ong và chút muối biển, đóng hũ kín.', 'REGULAR', NULL, 149000, 259000, 'Khối lượng', '180 g', '350 g'),
    (50, 'granola-cashew', 'Bánh kẹo & đồ uống', 'Granola hạt điều và nam việt quất', 'Yến mạch nướng cùng hạt điều, hạt bí và nam việt quất sấy; không dùng chất bảo quản.', 'REGULAR', NULL, 169000, 289000, 'Khối lượng', '250 g', '450 g'),
    (51, 'oolong-tea-peach', 'Bánh kẹo & đồ uống', 'Trà ô long đào sấy', 'Trà ô long rang nhẹ phối miếng đào sấy, đóng gói hút ẩm giữ hương tự nhiên.', 'REGULAR', NULL, 179000, 309000, 'Khối lượng', '80 g', '150 g'),
    (52, 'arabica-bean', 'Bánh kẹo & đồ uống', 'Cà phê hạt Arabica Cầu Đất', 'Arabica sơ chế honey từ Cầu Đất, rang vừa; phù hợp pha phin hoặc máy espresso gia đình.', 'REGULAR', NULL, 219000, 389000, 'Khối lượng', '250 g', '500 g'),
    (53, 'cashew-shortbread', 'Bánh kẹo & đồ uống', 'Bánh quy bơ hạt điều', 'Bánh quy bơ nướng giòn, phủ hạt điều rang; đóng hộp thiếc để giữ độ thơm.', 'REGULAR', NULL, 119000, 209000, 'Khối lượng', '160 g', '320 g'),
    (54, 'dried-mango', 'Bánh kẹo & đồ uống', 'Xoài sấy dẻo ít đường', 'Xoài chín sấy dẻo theo mẻ nhỏ, vị chua ngọt tự nhiên và không tẩm màu.', 'REGULAR', NULL, 99000, 179000, 'Khối lượng', '150 g', '300 g'),
    (55, 'hibiscus-tea', 'Bánh kẹo & đồ uống', 'Trà atiso đỏ và cam sấy', 'Hoa atiso đỏ sấy lạnh phối lát cam, dùng nóng hoặc pha lạnh đều có vị chua dịu.', 'REGULAR', NULL, 129000, 229000, 'Số túi lọc', '10 túi', '20 túi'),
    (56, 'salted-caramel-cookie', 'Bánh kẹo & đồ uống', 'Bánh quy caramel muối biển', 'Bánh quy bơ vị caramel, điểm muối biển hạt nhỏ; đóng gói riêng từng phần.', 'REGULAR', NULL, 109000, 189000, 'Khối lượng', '140 g', '280 g'),
    (57, 'ceramic-vase-sand', 'Đồ dùng cá nhân', 'Bình gốm dáng trụ màu cát', 'Bình gốm thủ công men cát, dáng trụ tối giản phù hợp cắm hoa cành ngắn.', 'REGULAR', NULL, 269000, 419000, 'Chiều cao', '18 cm', '25 cm'),
    (58, 'ceramic-mug-sage', 'Đồ dùng cá nhân', 'Cốc gốm men xanh sage', 'Cốc gốm nung nhiệt cao, men xanh sage phủ thủ công; dung tích khoảng 320 ml.', 'REGULAR', NULL, 169000, 249000, 'Dung tích', '320 ml', '420 ml'),
    (59, 'cotton-tote-natural', 'Đồ dùng cá nhân', 'Túi tote canvas màu tự nhiên', 'Túi canvas dày có ngăn trong, quai may gia cố; phù hợp mang sách và đồ dùng hằng ngày.', 'REGULAR', NULL, 199000, 299000, 'Kích cỡ', '32 × 36 cm', '38 × 42 cm'),
    (60, 'leather-card-holder', 'Đồ dùng cá nhân', 'Ví đựng thẻ da bò tối giản', 'Ví da bò thuộc thực vật có bốn ngăn thẻ, đường chỉ may thủ công và cạnh sơn hoàn thiện.', 'REGULAR', NULL, 329000, 489000, 'Màu da', 'Nâu cognac', 'Đen'),
    (61, 'silk-scrunchie-set', 'Đồ dùng cá nhân', 'Bộ dây buộc tóc lụa tơ tằm', 'Hai dây buộc tóc bọc lụa mềm, hạn chế kéo tóc; phối màu trung tính dễ sử dụng.', 'REGULAR', NULL, 159000, 239000, 'Số lượng', '2 chiếc', '4 chiếc'),
    (62, 'wooden-desk-organizer', 'Đồ dùng cá nhân', 'Khay gỗ để bàn đa năng', 'Khay gỗ cao su hoàn thiện dầu lau, chia ngăn đựng bút, đồng hồ và vật dụng nhỏ.', 'REGULAR', NULL, 289000, 429000, 'Kích thước', '20 cm', '28 cm'),
    (63, 'leather-notebook-cover', 'Đồ dùng cá nhân', 'Bìa sổ da tái sử dụng', 'Bìa da thật có khe cài bút và ngăn thẻ, vừa sổ khổ A5; thay ruột khi dùng hết.', 'REGULAR', NULL, 379000, 559000, 'Màu da', 'Nâu', 'Đen'),
    (64, 'cotton-bath-towel', 'Đồ dùng cá nhân', 'Khăn tắm cotton dệt tổ ong', 'Khăn cotton thấm hút tốt, dệt tổ ong nhẹ và nhanh khô; đóng gói bằng dây giấy.', 'REGULAR', NULL, 189000, 329000, 'Kích thước', '50 × 100 cm', '70 × 140 cm'),
    (65, 'glass-tea-infuser', 'Đồ dùng cá nhân', 'Bình trà thủy tinh lọc inox', 'Bình thủy tinh chịu nhiệt có lõi lọc inox tháo rời, dung tích phù hợp dùng tại bàn làm việc.', 'REGULAR', NULL, 249000, 369000, 'Dung tích', '450 ml', '700 ml'),
    (66, 'plant-pot-speckled', 'Đồ dùng cá nhân', 'Chậu cây gốm chấm men', 'Chậu gốm có lỗ thoát nước và đĩa hứng đồng bộ, men chấm thủ công từng chiếc.', 'REGULAR', NULL, 179000, 279000, 'Đường kính', '12 cm', '16 cm'),
    (67, 'photo-album-linen', 'Đồ dùng cá nhân', 'Album ảnh bìa vải linen', 'Album bìa vải linen lưu được 100 ảnh 10 × 15 cm, có trang ghi chú kỷ niệm.', 'REGULAR', NULL, 319000, 469000, 'Số ảnh', '100 ảnh', '200 ảnh'),
    (68, 'desk-clock-wood', 'Đồ dùng cá nhân', 'Đồng hồ để bàn gỗ sồi', 'Đồng hồ để bàn mặt tối giản, khung gỗ sồi và bộ máy kim chạy êm.', 'REGULAR', NULL, 389000, 589000, 'Màu gỗ', 'Sồi sáng', 'Óc chó'),
    (69, 'scarf-cotton-indigo', 'Đồ dùng cá nhân', 'Khăn cotton nhuộm chàm', 'Khăn cotton dệt nhẹ, nhuộm chàm thủ công; sắc độ và vân màu có khác biệt tự nhiên.', 'REGULAR', NULL, 289000, 429000, 'Kích thước', '55 × 55 cm', '90 × 90 cm'),
    (70, 'aroma-stone-set', 'Nến & hương thơm', 'Bộ đá khuếch tán tinh dầu', 'Bộ gồm đĩa đá thạch cao và tinh dầu dịu nhẹ, không cần dùng điện hay ngọn lửa.', 'REGULAR', NULL, 219000, 339000, 'Mùi hương', 'Trà trắng', 'Gỗ thông'),
    (71, 'pressed-flower-frame', 'Đồ dùng cá nhân', 'Tranh hoa ép khung gỗ', 'Hoa ép khô được sắp thủ công trong khung kính và gỗ, mỗi bố cục có nét riêng.', 'REGULAR', NULL, 299000, 459000, 'Kích thước', '13 × 18 cm', '20 × 25 cm'),
    (72, 'handmade-soap-set', 'Nến & hương thơm', 'Bộ xà phòng thủ công dịu nhẹ', 'Xà phòng dầu thực vật nấu lạnh với yến mạch và mật ong, phù hợp làm quà chăm sóc cá nhân.', 'REGULAR', NULL, 189000, 329000, 'Số bánh', '2 bánh', '4 bánh'),
    (73, 'gift-box-rosewood', 'Hộp quà & bao bì', 'Hộp quà vân gỗ hồng', 'Hộp giấy cứng phủ vân gỗ hồng trầm, nắp nam châm và khay lót có thể tháo rời.', 'READY_MADE_GIFT_BOX', 'BOX', 199000, 299000, 'Kích cỡ', '22 cm', '32 cm'),
    (74, 'gift-box-sage', 'Hộp quà & bao bì', 'Hộp quà xanh sage', 'Hộp quà màu xanh sage dịu, lót giấy tissue và có quai ruy băng dệt bản nhỏ.', 'READY_MADE_GIFT_BOX', 'BOX', 169000, 259000, 'Kích cỡ', '20 cm', '28 cm'),
    (75, 'gift-box-terracotta', 'Hộp quà & bao bì', 'Hộp quà đất nung', 'Hộp nắp rời màu đất nung, bề mặt giấy mỹ thuật nhám và khay chia ngăn linh hoạt.', 'READY_MADE_GIFT_BOX', 'BOX', 179000, 279000, 'Kích cỡ', '21 cm', '30 cm'),
    (76, 'gift-box-linen', 'Hộp quà & bao bì', 'Hộp quà bọc vải linen', 'Hộp cứng bọc vải linen màu be, lòng hộp có đệm giấy giúp cố định quà.', 'READY_MADE_GIFT_BOX', 'BOX', 229000, 349000, 'Kích cỡ', '22 cm', '32 cm'),
    (77, 'gift-box-blue-grey', 'Hộp quà & bao bì', 'Hộp quà xanh xám', 'Hộp nắp nam châm xanh xám, viền ép nhũ mờ và khay lót màu trắng ngà.', 'READY_MADE_GIFT_BOX', 'BOX', 189000, 289000, 'Kích cỡ', '20 cm', '30 cm'),
    (78, 'gift-box-burgundy', 'Hộp quà & bao bì', 'Hộp quà đỏ rượu vang', 'Hộp quà cứng bọc giấy đỏ rượu vang, phù hợp quà kỷ niệm và dịp lễ cuối năm.', 'READY_MADE_GIFT_BOX', 'BOX', 209000, 319000, 'Kích cỡ', '22 cm', '32 cm'),
    (79, 'gift-box-natural-kraft', 'Hộp quà & bao bì', 'Hộp kraft nắp trong', 'Hộp kraft định lượng dày có cửa sổ trong, thích hợp bày bánh và sản phẩm thủ công.', 'READY_MADE_GIFT_BOX', 'BOX', 99000, 159000, 'Kích cỡ', '18 cm', '25 cm'),
    (80, 'gift-box-round-ivory', 'Hộp quà & bao bì', 'Hộp tròn màu ngà', 'Hộp quà tròn nắp rời màu ngà, lót vải mềm và phù hợp bó hoa khô cỡ nhỏ.', 'READY_MADE_GIFT_BOX', 'BOX', 149000, 239000, 'Đường kính', '18 cm', '24 cm'),
    (81, 'gift-box-green-tea', 'Hộp quà & bao bì', 'Hộp quà xanh trà', 'Hộp vuông màu xanh trà, kết cấu cứng cáp với dây kéo giấy trang trí tối giản.', 'READY_MADE_GIFT_BOX', 'BOX', 169000, 259000, 'Kích cỡ', '20 cm', '28 cm'),
    (82, 'gift-box-lavender', 'Hộp quà & bao bì', 'Hộp quà tím lavender', 'Hộp nắp nam châm tím lavender phối lớp lót trắng, phù hợp quà sinh nhật nhẹ nhàng.', 'READY_MADE_GIFT_BOX', 'BOX', 179000, 269000, 'Kích cỡ', '20 cm', '30 cm'),
    (83, 'gift-set-tea-evening', 'Quà tặng đóng sẵn', 'Set trà chiều An yên', 'Set gồm trà ô long, hai cốc gốm và bánh quy bơ, sắp trong hộp xanh sage.', 'READY_MADE_GIFT_SET', NULL, 699000, 999000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (84, 'gift-set-newborn-visit', 'Quà tặng đóng sẵn', 'Set thăm em bé dịu nhẹ', 'Set quà thăm em bé gồm khăn cotton mềm, thú bông nhỏ và thiệp lời chúc.', 'READY_MADE_GIFT_SET', NULL, 589000, 889000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (85, 'gift-set-thank-you-teacher', 'Quà tặng đóng sẵn', 'Set tri ân người dẫn đường', 'Set gồm sổ tay linen, trà hoa và nến thơm hũ nhỏ, phù hợp dịp cảm ơn thầy cô.', 'READY_MADE_GIFT_SET', NULL, 649000, 949000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (86, 'gift-set-cozy-home', 'Quà tặng đóng sẵn', 'Set nhà ấm cúng', 'Bộ quà gồm nến thơm, khay gỗ nhỏ và mật ong hoa cà phê trong hộp kraft.', 'READY_MADE_GIFT_SET', NULL, 759000, 1099000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (87, 'gift-set-sweet-moment', 'Quà tặng đóng sẵn', 'Set khoảnh khắc ngọt ngào', 'Set macaron, trà hoa quả và thiệp gập; sản phẩm bánh được giao kèm hướng dẫn bảo quản.', 'READY_MADE_GIFT_SET', NULL, 499000, 749000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (88, 'gift-set-garden-love', 'Quà tặng đóng sẵn', 'Set yêu thương từ khu vườn', 'Set chậu cây mini, bình tưới nhỏ và khăn cotton, đóng gói trong hộp giấy tái chế.', 'READY_MADE_GIFT_SET', NULL, 579000, 849000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (89, 'gift-set-coffee-break', 'Quà tặng đóng sẵn', 'Set nghỉ giữa giờ cà phê', 'Set cà phê Arabica, cốc gốm men sage và bánh quy caramel muối biển.', 'READY_MADE_GIFT_SET', NULL, 629000, 929000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (90, 'gift-set-self-care', 'Quà tặng đóng sẵn', 'Set chăm sóc bản thân', 'Bộ xà phòng thủ công, túi thơm lavender và khăn tắm dệt tổ ong.', 'READY_MADE_GIFT_SET', NULL, 549000, 799000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (91, 'gift-set-memory-book', 'Quà tặng đóng sẵn', 'Set lưu giữ kỷ niệm', 'Album ảnh linen, khung ảnh gỗ và thiệp viết tay cho những dịp đáng nhớ.', 'READY_MADE_GIFT_SET', NULL, 729000, 1099000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (92, 'gift-set-flower-note', 'Quà tặng đóng sẵn', 'Set hoa và lời nhắn', 'Bó baby trắng phối cùng nến trà trắng và thiệp để gửi lời chúc nhẹ nhàng.', 'READY_MADE_GIFT_SET', NULL, 599000, 899000, 'Phiên bản', 'Tiêu chuẩn', 'Đầy đủ'),
    (93, 'wrap-paper-floral', 'Hộp quà & bao bì', 'Giấy gói hoa nhí màu kem', 'Giấy gói chống thấm nhẹ họa tiết hoa nhí, khổ 50 × 70 cm, phối được với dây cotton.', 'PACKAGING_ACCESSORY', 'WRAPPING', 22000, 39000, 'Số tờ', '1 tờ', '2 tờ'),
    (94, 'ribbon-cotton-natural', 'Hộp quà & bao bì', 'Dây cotton xoắn màu tự nhiên', 'Dây cotton xoắn mềm, phù hợp buộc nơ hộp quà và bó hoa phong cách mộc.', 'PACKAGING_ACCESSORY', 'RIBBON', 25000, 45000, 'Chiều dài', '3 m', '6 m'),
    (95, 'filler-paper-sage', 'Hộp quà & bao bì', 'Giấy chèn hộp xanh sage', 'Giấy chèn cắt sợi màu xanh sage, giúp cố định sản phẩm và đồng bộ màu hộp quà.', 'PACKAGING_ACCESSORY', 'FILLER', 32000, 55000, 'Khối lượng', '50 g', '100 g'),
    (96, 'gift-seal-sticker', 'Hộp quà & bao bì', 'Bộ tem niêm phong quà tặng', 'Tem giấy mỹ thuật họa tiết lá, dùng niêm phong giấy gói hoặc dán trang trí hộp.', 'PACKAGING_ACCESSORY', 'ACCESSORY', 29000, 49000, 'Số lượng', '12 tem', '24 tem'),
    (97, 'gift-tag-kraft', 'Hộp quà & bao bì', 'Thẻ treo quà giấy kraft', 'Thẻ kraft có lỗ luồn dây và mặt sau để viết tên người nhận hoặc lời nhắn ngắn.', 'PACKAGING_ACCESSORY', 'ACCESSORY', 19000, 35000, 'Số lượng', '6 thẻ', '12 thẻ'),
    (98, 'card-congratulations', 'Thiệp chúc mừng', 'Thiệp Chúc mừng bạn', 'Thiệp gập giấy mỹ thuật màu kem, minh họa nhành lá ép nhũ và phong bì đồng bộ.', 'GREETING_CARD', 'GREETING_CARD', 42000, NULL, NULL, 'Một thiệp', NULL),
    (99, 'card-graduation', 'Thiệp chúc mừng', 'Thiệp Mừng tốt nghiệp', 'Thiệp chúc mừng tốt nghiệp với thiết kế tối giản, mặt trong để trống viết lời riêng.', 'GREETING_CARD', 'GREETING_CARD', 42000, NULL, NULL, 'Một thiệp', NULL),
    (100, 'card-just-because', 'Thiệp chúc mừng', 'Thiệp Gửi bạn một điều vui', 'Thiệp gập minh họa hoa dại, thích hợp gửi lời hỏi thăm hoặc món quà không cần dịp đặc biệt.', 'GREETING_CARD', 'GREETING_CARD', 42000, NULL, NULL, 'Một thiệp', NULL)
) AS extra(seq, slug, category_name, name, description, product_type, custom_component_type,
           price, alt_price, option_name, std_label, alt_label);

INSERT INTO category (id, name, description)
SELECT md5('giftly-demo-category:' || category_name)::uuid,
       category_name,
       'Danh mục sản phẩm demo Giftly, dữ liệu phục vụ kiểm thử giao diện và API.'
FROM (SELECT DISTINCT category_name FROM _demo_product_seed) categories
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description;

INSERT INTO product (id, category_id, name, description, price, stock_quantity, product_type, status,
                     is_custom_box_eligible, created_at, updated_at, custom_component_type)
SELECT md5('giftly-demo-product:' || p.slug)::uuid,
       md5('giftly-demo-category:' || p.category_name)::uuid,
       p.name, p.description, NULL,
       p.stock_quantity + COALESCE(p.alt_stock_quantity, 0),
       p.product_type, p.product_status, p.custom_eligible,
       CURRENT_TIMESTAMP - INTERVAL '45 days', CURRENT_TIMESTAMP - INTERVAL '2 days',
       p.custom_component_type
FROM _demo_product_seed p
ON CONFLICT (id) DO UPDATE SET
    category_id = EXCLUDED.category_id,
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price = EXCLUDED.price,
    stock_quantity = EXCLUDED.stock_quantity,
    product_type = EXCLUDED.product_type,
    status = EXCLUDED.status,
    is_custom_box_eligible = EXCLUDED.is_custom_box_eligible,
    custom_component_type = EXCLUDED.custom_component_type,
    updated_at = EXCLUDED.updated_at;

CREATE TEMP TABLE _demo_variant_seed ON COMMIT DROP AS
SELECT p.seq, p.slug, p.name, p.product_type, p.product_status,
       p.is_perishable, p.shelf_life_hours, p.advance_payment_percent,
       v.suffix, v.label, v.price, v.stock_quantity, v.length, v.width, v.height, v.weight,
       md5('giftly-demo-variant:' || p.slug || ':' || lower(v.suffix))::uuid AS variant_id,
       'DEMO-' || replace(upper(p.slug), '-', '_') || '-' || v.suffix AS sku
FROM _demo_product_seed p
CROSS JOIN LATERAL (
    VALUES
        ('STD'::text, p.std_label, p.price, p.stock_quantity, p.length, p.width, p.height, p.weight),
        ('ALT'::text, p.alt_label, p.alt_price, p.alt_stock_quantity,
         p.length * 1.35, p.width * 1.25, p.height * 1.25, p.weight * 1.35)
) AS v(suffix, label, price, stock_quantity, length, width, height, weight)
WHERE v.suffix = 'STD' OR p.alt_price IS NOT NULL;

INSERT INTO product_variant (id, product_id, sku, price, stock_quantity, length, width, height,
                             dimension_unit, weight, weight_unit, status)
SELECT v.variant_id,
       md5('giftly-demo-product:' || v.slug)::uuid,
       v.sku, v.price, v.stock_quantity, v.length, v.width, v.height, 'cm', v.weight, 'kg',
       CASE
           WHEN v.product_status = 'DISCONTINUED' THEN 'DISCONTINUED'::variant_status
           WHEN v.stock_quantity = 0 THEN 'OUT_OF_STOCK'::variant_status
           ELSE 'AVAILABLE'::variant_status
       END
FROM _demo_variant_seed v
ON CONFLICT (id) DO UPDATE SET
    product_id = EXCLUDED.product_id,
    sku = EXCLUDED.sku,
    price = EXCLUDED.price,
    stock_quantity = EXCLUDED.stock_quantity,
    length = EXCLUDED.length,
    width = EXCLUDED.width,
    height = EXCLUDED.height,
    dimension_unit = EXCLUDED.dimension_unit,
    weight = EXCLUDED.weight,
    weight_unit = EXCLUDED.weight_unit,
    status = EXCLUDED.status;

INSERT INTO product_variant_policy (id, product_variant_id, is_perishable, shelf_life_hours,
                                    return_policy, advance_payment_percent)
SELECT md5('giftly-demo-policy:' || v.slug || ':' || lower(v.suffix))::uuid,
       v.variant_id,
       v.is_perishable,
       v.shelf_life_hours,
       CASE WHEN v.is_perishable THEN 'NO_RETURN' ELSE 'RETURNABLE' END,
       v.advance_payment_percent
FROM _demo_variant_seed v
ON CONFLICT (product_variant_id) DO UPDATE SET
    is_perishable = EXCLUDED.is_perishable,
    shelf_life_hours = EXCLUDED.shelf_life_hours,
    return_policy = EXCLUDED.return_policy,
    advance_payment_percent = EXCLUDED.advance_payment_percent;

INSERT INTO gift_box_variant_detail (id, product_variant_id, inner_length, inner_width, inner_height,
                                    max_weight, dimension_unit, weight_unit)
SELECT md5('giftly-demo-box-detail:' || v.slug || ':' || lower(v.suffix))::uuid,
       v.variant_id,
       GREATEST(v.length - 3, 1), GREATEST(v.width - 3, 1), GREATEST(v.height - 2, 1),
       CASE WHEN v.suffix = 'ALT' THEN 8.00 ELSE 4.00 END, 'cm', 'kg'
FROM _demo_variant_seed v
WHERE v.product_type = 'READY_MADE_GIFT_BOX'
ON CONFLICT (product_variant_id) DO UPDATE SET
    inner_length = EXCLUDED.inner_length,
    inner_width = EXCLUDED.inner_width,
    inner_height = EXCLUDED.inner_height,
    max_weight = EXCLUDED.max_weight,
    dimension_unit = EXCLUDED.dimension_unit,
    weight_unit = EXCLUDED.weight_unit;

INSERT INTO product_option (id, product_id, name, display_order)
SELECT md5('giftly-demo-option:' || p.slug)::uuid,
       md5('giftly-demo-product:' || p.slug)::uuid,
       p.option_name, 1
FROM _demo_product_seed p
WHERE p.alt_price IS NOT NULL
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, display_order = EXCLUDED.display_order;

INSERT INTO product_option_value (id, option_id, value, color_hex, price_adjustment)
SELECT md5('giftly-demo-option-value:' || p.slug || ':std')::uuid,
       md5('giftly-demo-option:' || p.slug)::uuid,
       p.std_label,
       CASE WHEN p.slug = 'leather-wallet' THEN '#8B5E3C' ELSE NULL END,
       0
FROM _demo_product_seed p
WHERE p.alt_price IS NOT NULL
UNION ALL
SELECT md5('giftly-demo-option-value:' || p.slug || ':alt')::uuid,
       md5('giftly-demo-option:' || p.slug)::uuid,
       p.alt_label,
       CASE WHEN p.slug = 'leather-wallet' THEN '#202124' ELSE NULL END,
       0
FROM _demo_product_seed p
WHERE p.alt_price IS NOT NULL
ON CONFLICT (id) DO UPDATE SET
    value = EXCLUDED.value,
    color_hex = EXCLUDED.color_hex,
    price_adjustment = EXCLUDED.price_adjustment;

INSERT INTO product_variant_option_value (id, variant_id, option_value_id)
SELECT md5('giftly-demo-variant-option:' || v.slug || ':' || lower(v.suffix))::uuid,
       v.variant_id,
       md5('giftly-demo-option-value:' || v.slug || ':' || lower(v.suffix))::uuid
FROM _demo_variant_seed v
JOIN _demo_product_seed p ON p.slug = v.slug AND p.alt_price IS NOT NULL
ON CONFLICT (id) DO UPDATE SET
    variant_id = EXCLUDED.variant_id,
    option_value_id = EXCLUDED.option_value_id;

INSERT INTO product_image (id, image_url, secure_url, public_id, is_primary, display_order,
                           product_variant_id, product_id)
SELECT md5('giftly-demo-image:' || v.slug || ':' || lower(v.suffix))::uuid,
       'https://images.unsplash.com/' ||
       CASE
           WHEN p.category_name = 'Hoa tươi' THEN 'photo-1490750967868-88aa4486c946'
           WHEN p.category_name = 'Nến & hương thơm' THEN 'photo-1603006905003-be475563bc59'
           WHEN p.category_name = 'Bánh kẹo & đồ uống' THEN 'photo-1544787219-7f47ccb76574'
           WHEN p.category_name = 'Thiệp chúc mừng' THEN 'photo-1544816155-12df9643f363'
           WHEN p.category_name = 'Đồ dùng cá nhân' THEN 'photo-1514228742587-6b1558fcca3d'
           ELSE 'photo-1512909006721-3d6018887383'
       END || '?auto=format&fit=crop&w=900&q=85',
       'https://images.unsplash.com/' ||
       CASE
           WHEN p.category_name = 'Hoa tươi' THEN 'photo-1490750967868-88aa4486c946'
           WHEN p.category_name = 'Nến & hương thơm' THEN 'photo-1603006905003-be475563bc59'
           WHEN p.category_name = 'Bánh kẹo & đồ uống' THEN 'photo-1544787219-7f47ccb76574'
           WHEN p.category_name = 'Thiệp chúc mừng' THEN 'photo-1544816155-12df9643f363'
           WHEN p.category_name = 'Đồ dùng cá nhân' THEN 'photo-1514228742587-6b1558fcca3d'
           ELSE 'photo-1512909006721-3d6018887383'
       END || '?auto=format&fit=crop&w=900&q=85',
       'giftly-demo/' || v.slug || '/' || lower(v.suffix),
       v.suffix = 'STD', 1, v.variant_id, NULL
FROM _demo_variant_seed v
JOIN _demo_product_seed p ON p.slug = v.slug
ON CONFLICT (id) DO UPDATE SET
    image_url = EXCLUDED.image_url,
    secure_url = EXCLUDED.secure_url,
    public_id = EXCLUDED.public_id,
    is_primary = EXCLUDED.is_primary,
    display_order = EXCLUDED.display_order,
    product_variant_id = EXCLUDED.product_variant_id;

INSERT INTO bundle_component (id, bundle_variant_id, component_variant_id, component_role,
                             quantity, is_required)
SELECT md5('giftly-demo-bundle-component:' || bundle.slug || ':' || component.slug)::uuid,
       bundle_variant.variant_id, component_variant.variant_id, composition.component_role,
       composition.quantity, TRUE
FROM (VALUES
    ('gift-set-gratitude', 'dark-chocolate', 'PRODUCT', 1),
    ('gift-set-gratitude', 'rose-bouquet', 'PRODUCT', 1),
    ('gift-set-gratitude', 'card-thank-you', 'GREETING_CARD', 1),
    ('gift-set-gratitude', 'gift-box-ivory', 'BOX', 1),
    ('gift-set-birthday', 'teddy-bear', 'PRODUCT', 1),
    ('gift-set-birthday', 'soy-candle', 'PRODUCT', 1),
    ('gift-set-birthday', 'card-birthday', 'GREETING_CARD', 1),
    ('gift-set-birthday', 'gift-box-forest', 'BOX', 1),
    ('gift-set-new-home', 'ceramic-mug', 'PRODUCT', 1),
    ('gift-set-new-home', 'acacia-honey', 'PRODUCT', 1),
    ('gift-set-new-home', 'succulent-pot', 'PRODUCT', 1),
    ('gift-set-new-home', 'card-new-home', 'GREETING_CARD', 1),
    ('gift-set-coffee', 'drip-coffee', 'PRODUCT', 1),
    ('gift-set-coffee', 'dark-chocolate', 'PRODUCT', 1),
    ('gift-set-coffee', 'ceramic-mug', 'PRODUCT', 1),
    ('gift-set-coffee', 'gift-box-midnight', 'BOX', 1)
) AS composition(bundle_slug, component_slug, component_role, quantity)
JOIN _demo_product_seed bundle ON bundle.slug = composition.bundle_slug
JOIN _demo_variant_seed bundle_variant ON bundle_variant.slug = bundle.slug AND bundle_variant.suffix = 'STD'
JOIN _demo_product_seed component ON component.slug = composition.component_slug
JOIN _demo_variant_seed component_variant ON component_variant.slug = component.slug
    AND component_variant.suffix = 'STD'
ON CONFLICT (id) DO UPDATE SET
    bundle_variant_id = EXCLUDED.bundle_variant_id,
    component_variant_id = EXCLUDED.component_variant_id,
    component_role = EXCLUDED.component_role,
    quantity = EXCLUDED.quantity,
    is_required = EXCLUDED.is_required;

INSERT INTO bundle_component (id, bundle_variant_id, component_variant_id, component_role,
                             quantity, is_required)
SELECT md5('giftly-demo-bundle-component:' || bundle.slug || ':' || component.slug || ':'
          || composition.component_role)::uuid,
       bundle_variant.variant_id, component_variant.variant_id,
       composition.component_role, 1, TRUE
FROM _demo_product_seed bundle
CROSS JOIN LATERAL (
    VALUES
        (1 + ((bundle.seq - 83) * 3 % 18), 'PRODUCT'::text),
        (1 + ((bundle.seq - 83) * 3 + 7) % 18, 'PRODUCT'::text),
        (29 + ((bundle.seq - 83) % 4), 'GREETING_CARD'::text),
        (19 + ((bundle.seq - 83) % 3), 'BOX'::text)
) AS composition(component_seq, component_role)
JOIN _demo_product_seed component ON component.seq = composition.component_seq
JOIN _demo_variant_seed bundle_variant ON bundle_variant.slug = bundle.slug AND bundle_variant.suffix = 'STD'
JOIN _demo_variant_seed component_variant ON component_variant.slug = component.slug
    AND component_variant.suffix = 'STD'
WHERE bundle.seq BETWEEN 83 AND 92
ON CONFLICT (id) DO UPDATE SET
    bundle_variant_id = EXCLUDED.bundle_variant_id,
    component_variant_id = EXCLUDED.component_variant_id,
    component_role = EXCLUDED.component_role,
    quantity = EXCLUDED.quantity,
    is_required = EXCLUDED.is_required;

INSERT INTO bundle_option_group (id, bundle_variant_id, name, group_type, selection_type,
                                min_selections, max_selections, is_required, display_order)
SELECT md5('giftly-demo-bundle-group:' || p.slug)::uuid,
       v.variant_id, 'Chọn thiệp viết lời chúc', 'GREETING_CARD', 'SINGLE', 0, 1, FALSE, 1
FROM _demo_product_seed p
JOIN _demo_variant_seed v ON v.slug = p.slug AND v.suffix = 'STD'
WHERE p.product_type = 'READY_MADE_GIFT_SET'
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    group_type = EXCLUDED.group_type,
    selection_type = EXCLUDED.selection_type,
    min_selections = EXCLUDED.min_selections,
    max_selections = EXCLUDED.max_selections,
    is_required = EXCLUDED.is_required,
    display_order = EXCLUDED.display_order;

INSERT INTO bundle_option (id, option_group_id, product_variant_id, pricing_mode, fixed_price)
SELECT md5('giftly-demo-bundle-option:' || bundle.slug || ':' || card.slug)::uuid,
       md5('giftly-demo-bundle-group:' || bundle.slug)::uuid,
       card_variant.variant_id, 'FREE', NULL
FROM (VALUES
    ('gift-set-gratitude', 'card-thank-you'),
    ('gift-set-birthday', 'card-birthday'),
    ('gift-set-new-home', 'card-new-home'),
    ('gift-set-coffee', 'card-thank-you')
) AS selection(bundle_slug, card_slug)
JOIN _demo_product_seed bundle ON bundle.slug = selection.bundle_slug
JOIN _demo_product_seed card ON card.slug = selection.card_slug
JOIN _demo_variant_seed card_variant ON card_variant.slug = card.slug AND card_variant.suffix = 'STD'
ON CONFLICT (id) DO UPDATE SET
    option_group_id = EXCLUDED.option_group_id,
    product_variant_id = EXCLUDED.product_variant_id,
    pricing_mode = EXCLUDED.pricing_mode,
    fixed_price = EXCLUDED.fixed_price;

INSERT INTO bundle_option (id, option_group_id, product_variant_id, pricing_mode, fixed_price)
SELECT md5('giftly-demo-bundle-option:' || bundle.slug || ':' || card.slug)::uuid,
       md5('giftly-demo-bundle-group:' || bundle.slug)::uuid,
       card_variant.variant_id, 'FREE', NULL
FROM _demo_product_seed bundle
JOIN _demo_product_seed card ON card.seq = 29 + ((bundle.seq - 83) % 4)
JOIN _demo_variant_seed card_variant ON card_variant.slug = card.slug AND card_variant.suffix = 'STD'
WHERE bundle.seq BETWEEN 83 AND 92
ON CONFLICT (id) DO UPDATE SET
    option_group_id = EXCLUDED.option_group_id,
    product_variant_id = EXCLUDED.product_variant_id,
    pricing_mode = EXCLUDED.pricing_mode,
    fixed_price = EXCLUDED.fixed_price;

CREATE TEMP TABLE _demo_customer_seed (
    customer_no INTEGER PRIMARY KEY,
    email TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    phone TEXT NOT NULL,
    address_line TEXT NOT NULL,
    ward TEXT NOT NULL,
    district TEXT NOT NULL,
    city TEXT NOT NULL
) ON COMMIT DROP;

INSERT INTO _demo_customer_seed VALUES
(1, 'minhanh.nguyen01@example.test', 'Nguyễn Minh Anh', '0904123781', '18 ngõ 76 Duy Tân', 'Dịch Vọng Hậu', 'Cầu Giấy', 'Hà Nội'),
(2, 'ngochan.tran02@example.test', 'Trần Ngọc Hân', '0916254387', '42 Nguyễn Văn Cừ', 'Bồ Đề', 'Long Biên', 'Hà Nội'),
(3, 'quanghuy.le03@example.test', 'Lê Quang Huy', '0983185246', '15 Trần Phú', 'Mộ Lao', 'Hà Đông', 'Hà Nội'),
(4, 'thuyduong.pham04@example.test', 'Phạm Thùy Dương', '0938472615', '96 Lê Lợi', 'Bến Thành', 'Quận 1', 'TP. Hồ Chí Minh'),
(5, 'baolinh.vo05@example.test', 'Võ Bảo Linh', '0908673142', '27 Nguyễn Thị Minh Khai', 'Đa Kao', 'Quận 1', 'TP. Hồ Chí Minh'),
(6, 'tuananh.do06@example.test', 'Đỗ Tuấn Anh', '0972316485', '81 Phan Đăng Lưu', 'Phường 3', 'Phú Nhuận', 'TP. Hồ Chí Minh'),
(7, 'phuongthao.bui07@example.test', 'Bùi Phương Thảo', '0913948276', '22 Nguyễn Du', 'Thạch Thang', 'Hải Châu', 'Đà Nẵng'),
(8, 'minhkhoi.dang08@example.test', 'Đặng Minh Khôi', '0905762184', '54 Lê Duẩn', 'Chính Gián', 'Thanh Khê', 'Đà Nẵng'),
(9, 'yennhi.ho09@example.test', 'Hồ Yến Nhi', '0935127486', '31 Hai Bà Trưng', 'Vĩnh Ninh', 'Thuận Hóa', 'Huế'),
(10, 'ducminh.ngo10@example.test', 'Ngô Đức Minh', '0986421753', '17 Trần Hưng Đạo', 'An Hải Tây', 'Sơn Trà', 'Đà Nẵng'),
(11, 'thanhha.dinh11@example.test', 'Đinh Thanh Hà', '0901395826', '63 Nguyễn Trãi', 'Nguyễn Cư Trinh', 'Quận 1', 'TP. Hồ Chí Minh'),
(12, 'hoangnam.ly12@example.test', 'Lý Hoàng Nam', '0917823465', '9 Nguyễn Văn Linh', 'Tân Phong', 'Quận 7', 'TP. Hồ Chí Minh'),
(13, 'khanhvy.truong13@example.test', 'Trương Khánh Vy', '0962458137', '108 Phan Chu Trinh', 'Phước Ninh', 'Hải Châu', 'Đà Nẵng'),
(14, 'quocbao.nguyen14@example.test', 'Nguyễn Quốc Bảo', '0907524618', '36 Hoàng Diệu', 'Phường 5', 'Đà Lạt', 'Lâm Đồng'),
(15, 'dieplinh.tran15@example.test', 'Trần Diệp Linh', '0934682157', '12 Nguyễn Công Trứ', 'Phố Huế', 'Hai Bà Trưng', 'Hà Nội'),
(16, 'giaduy.vo16@example.test', 'Võ Gia Duy', '0976381254', '74 Nguyễn Oanh', 'Phường 17', 'Gò Vấp', 'TP. Hồ Chí Minh'),
(17, 'ngocmai.pham17@example.test', 'Phạm Ngọc Mai', '0914582736', '23 Lý Thường Kiệt', 'Trần Phú', 'Hồng Bàng', 'Hải Phòng'),
(18, 'minhquan.le18@example.test', 'Lê Minh Quân', '0981753462', '51 Nguyễn Huệ', 'Bến Nghé', 'Quận 1', 'TP. Hồ Chí Minh'),
(19, 'thutrang.dang19@example.test', 'Đặng Thu Trang', '0906284715', '19 Võ Thị Sáu', 'Thống Nhất', 'Biên Hòa', 'Đồng Nai'),
(20, 'anhkhoa.ho20@example.test', 'Hồ Anh Khoa', '0932716485', '88 Cách Mạng Tháng Tám', 'Hòa Bình', 'Ninh Kiều', 'Cần Thơ'),
(21, 'ngocdiep.do21@example.test', 'Đỗ Ngọc Diệp', '0918352746', '6 Nguyễn Chí Thanh', 'Láng Thượng', 'Đống Đa', 'Hà Nội'),
(22, 'thanhdat.bui22@example.test', 'Bùi Thành Đạt', '0965741832', '39 Phan Xích Long', 'Phường 2', 'Phú Nhuận', 'TP. Hồ Chí Minh'),
(23, 'lananh.ly23@example.test', 'Lý Lan Anh', '0903146872', '28 Nguyễn Văn Thoại', 'Mỹ An', 'Ngũ Hành Sơn', 'Đà Nẵng'),
(24, 'vietanh.ngo24@example.test', 'Ngô Việt Anh', '0985247613', '45 Trần Phú', 'Lộc Thọ', 'Nha Trang', 'Khánh Hòa');

INSERT INTO _demo_customer_seed
SELECT n,
       (ARRAY['minhanh', 'ngochan', 'quanghuy', 'thuyduong', 'baolinh', 'tuananh',
              'phuongthao', 'minhkhoi', 'yennhi', 'ducminh', 'thanhha', 'hoangnam',
              'khanhvy', 'quocbao', 'dieplinh', 'giaduy'])[((n * 11) % 16) + 1]
           || '.' ||
       (ARRAY['nguyen', 'tran', 'le', 'pham', 'vo', 'do', 'bui', 'dang',
              'ho', 'ngo', 'dinh', 'ly'])[((n * 7) % 12) + 1]
           || lpad(n::text, GREATEST(2, length(n::text)), '0') || '@example.test',
       (ARRAY['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Võ', 'Đỗ', 'Bùi', 'Đặng',
              'Hồ', 'Ngô', 'Đinh', 'Lý'])[((n * 7) % 12) + 1]
           || ' ' ||
       (ARRAY['Minh', 'Ngọc', 'Gia', 'Hoàng', 'Thu', 'Khánh', 'Bảo', 'Thanh',
              'Quốc', 'Phương'])[((n * 3) % 10) + 1]
           || ' ' ||
       (ARRAY['Anh', 'Hân', 'Huy', 'Dương', 'Linh', 'Thảo', 'Khôi', 'Nhi',
              'Minh', 'Hà', 'Nam', 'Vy', 'Bảo', 'Trang', 'Khoa', 'Mai'])[((n * 11) % 16) + 1],
       '09' || lpad((10000000 + (n * 7919 % 90000000))::text, 8, '0'),
       (10 + n % 89)::text || ' ' ||
       (ARRAY['Nguyễn Khánh Toàn', 'Lê Văn Sỹ', 'Phan Đăng Lưu', 'Trần Hưng Đạo',
              'Nguyễn Văn Linh'])[n % 5 + 1],
       CASE n % 5
           WHEN 0 THEN 'Dịch Vọng Hậu'
           WHEN 1 THEN 'Bến Nghé'
           WHEN 2 THEN 'Thạch Thang'
           WHEN 3 THEN 'An Cư'
           ELSE 'Phường 1'
       END,
       CASE n % 5
           WHEN 0 THEN 'Cầu Giấy'
           WHEN 1 THEN 'Quận 1'
           WHEN 2 THEN 'Hải Châu'
           WHEN 3 THEN 'Ninh Kiều'
           ELSE 'Đà Lạt'
       END,
       CASE n % 5
           WHEN 0 THEN 'Hà Nội'
           WHEN 1 THEN 'TP. Hồ Chí Minh'
           WHEN 2 THEN 'Đà Nẵng'
           WHEN 3 THEN 'Cần Thơ'
           ELSE 'Lâm Đồng'
       END
FROM generate_series(25, 100) AS generated(n);

ALTER TABLE _demo_customer_seed ADD COLUMN customer_key TEXT;

UPDATE _demo_customer_seed
SET customer_key = lpad(customer_no::text, GREATEST(2, length(customer_no::text)), '0');

INSERT INTO role (id, name)
VALUES (md5('giftly-demo-role:ROLE_USER')::uuid, 'ROLE_USER')
ON CONFLICT (name) DO NOTHING;

INSERT INTO "user" (id, email, password, full_name, phone, status, created_at, updated_at)
SELECT md5('giftly-demo-user:' || c.customer_key)::uuid,
       c.email,
       '$2y$10$M7q/c/AuRzZ0F/rR1WVbjOdYMa/qqSY2xpbu1qWV2z2c.GoP2/6gi',
       c.full_name, c.phone, 'ACTIVE',
       CURRENT_TIMESTAMP - (c.customer_no * INTERVAL '3 days'),
       CURRENT_TIMESTAMP - INTERVAL '1 day'
FROM _demo_customer_seed c
ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    password = EXCLUDED.password,
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

INSERT INTO user_role (id, user_id, role_id)
SELECT md5('giftly-demo-user-role:' || c.customer_key)::uuid,
       md5('giftly-demo-user:' || c.customer_key)::uuid,
       r.id
FROM _demo_customer_seed c
CROSS JOIN role r
WHERE r.name = 'ROLE_USER'
ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id, role_id = EXCLUDED.role_id;

INSERT INTO address (id, user_id, recipient_name, phone, address_line, ward, district, city, is_default)
SELECT md5('giftly-demo-address:' || c.customer_key || ':home')::uuid,
       md5('giftly-demo-user:' || c.customer_key)::uuid,
       c.full_name, c.phone, c.address_line, c.ward, c.district, c.city, TRUE
FROM _demo_customer_seed c
ON CONFLICT (id) DO UPDATE SET
    recipient_name = EXCLUDED.recipient_name,
    phone = EXCLUDED.phone,
    address_line = EXCLUDED.address_line,
    ward = EXCLUDED.ward,
    district = EXCLUDED.district,
    city = EXCLUDED.city,
    is_default = EXCLUDED.is_default;

INSERT INTO address (id, user_id, recipient_name, phone, address_line, ward, district, city, is_default)
SELECT md5('giftly-demo-address:' || c.customer_key || ':work')::uuid,
       md5('giftly-demo-user:' || c.customer_key)::uuid,
       c.full_name,
       c.phone,
       CASE c.customer_no
           WHEN 1 THEN 'Tầng 8, tòa nhà CMC, phố Duy Tân'
           WHEN 2 THEN 'Tầng 5, văn phòng Nguyễn Văn Cừ'
           WHEN 3 THEN 'Tầng 3, khu văn phòng Mộ Lao'
           WHEN 4 THEN 'Tầng 6, tòa nhà Lê Lợi'
           WHEN 5 THEN 'Tầng 4, văn phòng Nguyễn Thị Minh Khai'
           WHEN 6 THEN 'Tầng 2, khu thương mại Phan Đăng Lưu'
           WHEN 7 THEN 'Tầng 7, văn phòng Nguyễn Du'
           ELSE 'Tầng 3, khu văn phòng trung tâm'
       END,
       c.ward, c.district, c.city, FALSE
FROM _demo_customer_seed c
WHERE c.customer_no <= 8
ON CONFLICT (id) DO UPDATE SET
    recipient_name = EXCLUDED.recipient_name,
    phone = EXCLUDED.phone,
    address_line = EXCLUDED.address_line,
    ward = EXCLUDED.ward,
    district = EXCLUDED.district,
    city = EXCLUDED.city,
    is_default = EXCLUDED.is_default;

INSERT INTO cart (id, user_id, updated_at)
SELECT md5('giftly-demo-cart:' || c.customer_key)::uuid,
       md5('giftly-demo-user:' || c.customer_key)::uuid,
       CURRENT_TIMESTAMP - (c.customer_no * INTERVAL '2 hours')
FROM _demo_customer_seed c
ON CONFLICT (id) DO UPDATE SET updated_at = EXCLUDED.updated_at;

INSERT INTO gift_box_config (id, user_id, box_product_id, wrapping_product_id, ribbon_product_id,
                             card_product_id, greeting_message, created_at,
                             updated_at, review_status, review_note, capacity_ratio)
SELECT md5('giftly-demo-gift-config:' || c.customer_key)::uuid,
       md5('giftly-demo-user:' || c.customer_key)::uuid,
       md5('giftly-demo-product:' || box.slug)::uuid,
       md5('giftly-demo-product:kraft-wrap')::uuid,
       md5('giftly-demo-product:satin-ribbon')::uuid,
       md5('giftly-demo-product:' || card.slug)::uuid,
       CASE c.customer_no
           WHEN 1 THEN 'Chúc bạn luôn tìm thấy niềm vui trong những điều giản dị.'
           WHEN 2 THEN 'Mừng sinh nhật, chúc bạn một năm thật nhiều trải nghiệm đẹp.'
           WHEN 3 THEN 'Cảm ơn vì đã luôn đồng hành cùng mình.'
           WHEN 4 THEN 'Chúc ngôi nhà mới luôn ngập tràn tiếng cười.'
           WHEN 5 THEN 'Gửi bạn một chút ấm áp cho ngày đặc biệt.'
           WHEN 6 THEN 'Chúc bạn luôn vững vàng và gặp nhiều may mắn.'
           WHEN 7 THEN 'Một món quà nhỏ thay lời chúc mừng.'
           ELSE 'Thân gửi bạn với tất cả sự trân trọng.'
       END,
       CURRENT_TIMESTAMP - (c.customer_no * INTERVAL '1 day'),
       CURRENT_TIMESTAMP - INTERVAL '1 hour',
       CASE WHEN c.customer_no <= 4 THEN 'APPROVED'::gift_box_review_status
            ELSE 'PENDING'::gift_box_review_status END,
       CASE WHEN c.customer_no <= 4 THEN NULL ELSE 'Chờ kiểm tra cách sắp xếp sản phẩm.' END,
       48 + c.customer_no * 4
FROM _demo_customer_seed c
JOIN _demo_product_seed box ON box.seq = 19 + ((c.customer_no - 1) % 3)
JOIN _demo_variant_seed box_variant ON box_variant.slug = box.slug AND box_variant.suffix = 'STD'
JOIN _demo_product_seed card ON card.seq = CASE WHEN c.customer_no % 2 = 0 THEN 30 ELSE 29 END
WHERE c.customer_no <= 8
ON CONFLICT (id) DO UPDATE SET
    box_product_id = EXCLUDED.box_product_id,
    wrapping_product_id = EXCLUDED.wrapping_product_id,
    ribbon_product_id = EXCLUDED.ribbon_product_id,
    card_product_id = EXCLUDED.card_product_id,
    greeting_message = EXCLUDED.greeting_message,
    review_status = EXCLUDED.review_status,
    review_note = EXCLUDED.review_note,
    capacity_ratio = EXCLUDED.capacity_ratio,
    updated_at = EXCLUDED.updated_at;

INSERT INTO gift_box_config_item (id, config_id, product_variant_id, quantity, component_type,
                                  message_text, message_rendering_method)
SELECT md5('giftly-demo-gift-config-item:' || c.customer_key || ':' || item.line_no)::uuid,
       md5('giftly-demo-gift-config:' || c.customer_key)::uuid,
       variant.variant_id, 1, item.component_type,
       CASE WHEN item.component_type = 'GREETING_CARD' THEN 'Chúc bạn luôn vui và bình an.' ELSE NULL END,
       CASE WHEN item.component_type = 'GREETING_CARD' THEN 'PRINTED' ELSE NULL END
FROM _demo_customer_seed c
CROSS JOIN LATERAL (
    VALUES
        (1, 'gift-box-' || CASE (c.customer_no - 1) % 3 WHEN 0 THEN 'ivory' WHEN 1 THEN 'forest' ELSE 'midnight' END, 'BOX'::text),
        (2, 'kraft-wrap', 'WRAPPING'::text),
        (3, 'satin-ribbon', 'RIBBON'::text),
        (4, CASE WHEN c.customer_no % 2 = 0 THEN 'card-birthday' ELSE 'card-thank-you' END, 'GREETING_CARD'::text),
        (5, CASE WHEN c.customer_no % 3 = 0 THEN 'soy-candle' ELSE 'dark-chocolate' END, NULL::text),
        (6, CASE WHEN c.customer_no % 2 = 0 THEN 'ceramic-mug' ELSE 'teddy-bear' END, NULL::text)
) AS item(line_no, product_slug, component_type)
JOIN _demo_product_seed product ON product.slug = item.product_slug
JOIN _demo_variant_seed variant ON variant.slug = product.slug AND variant.suffix = 'STD'
WHERE c.customer_no <= 8
ON CONFLICT (id) DO UPDATE SET
    config_id = EXCLUDED.config_id,
    product_variant_id = EXCLUDED.product_variant_id,
    quantity = EXCLUDED.quantity,
    message_text = EXCLUDED.message_text,
    message_rendering_method = EXCLUDED.message_rendering_method;

INSERT INTO cart_item (id, cart_id, product_id, gift_box_config_id, quantity, product_variant_id)
SELECT md5('giftly-demo-cart-item:' || c.customer_key || ':1')::uuid,
       md5('giftly-demo-cart:' || c.customer_key)::uuid,
       NULL, NULL, 1 + (c.customer_no % 2), variant.variant_id
FROM _demo_customer_seed c
JOIN _demo_variant_seed variant ON variant.seq = ((c.customer_no - 1) % 18) + 1
    AND variant.suffix = 'STD'
ON CONFLICT (id) DO UPDATE SET
    cart_id = EXCLUDED.cart_id,
    product_id = EXCLUDED.product_id,
    gift_box_config_id = EXCLUDED.gift_box_config_id,
    quantity = EXCLUDED.quantity,
    product_variant_id = EXCLUDED.product_variant_id;

INSERT INTO cart_item (id, cart_id, product_id, gift_box_config_id, quantity, product_variant_id)
SELECT md5('giftly-demo-cart-item:' || c.customer_key || ':2')::uuid,
       md5('giftly-demo-cart:' || c.customer_key)::uuid,
       NULL,
       CASE WHEN c.customer_no <= 8
            THEN md5('giftly-demo-gift-config:' || c.customer_key)::uuid
            ELSE NULL END,
       1,
       CASE
           WHEN c.customer_no <= 8 THEN NULL
           ELSE variant.variant_id
       END
FROM _demo_customer_seed c
LEFT JOIN _demo_variant_seed variant
       ON variant.seq = CASE
           WHEN c.customer_no BETWEEN 9 AND 12 THEN 22 + (c.customer_no - 9)
           ELSE ((c.customer_no + 9) % 100) + 1
       END
       AND variant.suffix = 'STD'
ON CONFLICT (id) DO UPDATE SET
    cart_id = EXCLUDED.cart_id,
    product_id = EXCLUDED.product_id,
    gift_box_config_id = EXCLUDED.gift_box_config_id,
    quantity = EXCLUDED.quantity,
    product_variant_id = EXCLUDED.product_variant_id;

INSERT INTO cart_item_option_selection (id, cart_item_id, bundle_option_id, quantity, message_text,
                                        message_rendering_method)
SELECT md5('giftly-demo-cart-option-selection:' || c.customer_no)::uuid,
       md5('giftly-demo-cart-item:' || c.customer_key || ':2')::uuid,
       md5('giftly-demo-bundle-option:' || bundle.slug || ':' || card.slug)::uuid,
       1,
       CASE WHEN c.customer_no % 2 = 0 THEN 'Chúc mừng bạn!' ELSE 'Gửi bạn lời chúc tốt đẹp nhất.' END,
       'PRINTED'
FROM _demo_customer_seed c
JOIN _demo_product_seed bundle ON bundle.seq = 22 + (c.customer_no - 9)
JOIN _demo_product_seed card ON card.slug = CASE bundle.seq
    WHEN 22 THEN 'card-thank-you'
    WHEN 23 THEN 'card-birthday'
    WHEN 24 THEN 'card-new-home'
    ELSE 'card-thank-you'
END
WHERE c.customer_no BETWEEN 9 AND 12
ON CONFLICT (id) DO UPDATE SET
    cart_item_id = EXCLUDED.cart_item_id,
    bundle_option_id = EXCLUDED.bundle_option_id,
    quantity = EXCLUDED.quantity,
    message_text = EXCLUDED.message_text,
    message_rendering_method = EXCLUDED.message_rendering_method;

CREATE TEMP TABLE _demo_order_seed ON COMMIT DROP AS
SELECT n AS order_no,
       md5('giftly-demo-order:' || lpad(n::text, GREATEST(3, length(n::text)), '0'))::uuid AS order_id,
       customer.customer_no,
       md5('giftly-demo-user:' || customer.customer_key)::uuid AS user_id,
       md5('giftly-demo-address:' || customer.customer_key || ':home')::uuid AS address_id,
       'GLY-DEMO-2026-' || lpad(n::text, 4, '0') AS order_number,
       CASE n % 12
           WHEN 0 THEN 'COMPLETED'
           WHEN 1 THEN 'DELIVERED'
           WHEN 2 THEN 'SHIPPING'
           WHEN 3 THEN 'READY_TO_SHIP'
           WHEN 4 THEN 'PACKAGING'
           WHEN 5 THEN 'PROCESSING'
           WHEN 6 THEN 'CONFIRMED'
           WHEN 7 THEN 'PENDING_CONFIRMATION'
           WHEN 8 THEN 'CANCELLED'
           WHEN 9 THEN 'RETURNED'
           WHEN 10 THEN 'FAILED'
           ELSE 'RETURN_REQUESTED'
       END AS status,
       CURRENT_TIMESTAMP - ((1000 - n) * INTERVAL '8 hours') - INTERVAL '1 day' AS ordered_at
FROM generate_series(1, 1000) AS generated(n)
JOIN _demo_customer_seed customer ON customer.customer_no = ((n - 1) % 100) + 1;

CREATE TEMP TABLE _demo_order_line_seed ON COMMIT DROP AS
SELECT o.order_no,
       line.line_no,
       variant.variant_id,
       variant.sku,
       variant.label,
       variant.price,
       CASE WHEN line.line_no = 1 THEN 1 + (o.order_no % 2) ELSE 1 END AS quantity,
       variant.is_perishable,
       variant.shelf_life_hours,
       variant.advance_payment_percent,
       policy.return_policy,
       product.name AS product_name,
       md5('giftly-demo-product:' || product.slug)::uuid AS product_id
FROM _demo_order_seed o
CROSS JOIN LATERAL (
    VALUES
        (1, ((o.order_no - 1) % 100) + 1),
        (2, ((o.order_no + 28) % 100) + 1),
        (3, ((o.order_no + 62) % 100) + 1)
) AS line(line_no, product_seq)
JOIN _demo_variant_seed variant ON variant.seq = line.product_seq AND variant.suffix = 'STD'
JOIN _demo_product_seed product ON product.seq = variant.seq
JOIN product_variant_policy policy ON policy.product_variant_id = variant.variant_id;

CREATE TEMP TABLE _demo_order_totals ON COMMIT DROP AS
SELECT o.order_no,
       o.order_id,
       o.user_id,
       o.address_id,
       o.order_number,
       o.status,
       o.ordered_at,
       SUM(line.price * line.quantity) AS subtotal,
       CASE WHEN SUM(line.price * line.quantity) >= 500000 THEN 0::numeric ELSE 30000::numeric END AS shipping_fee
FROM _demo_order_seed o
JOIN _demo_order_line_seed line ON line.order_no = o.order_no
GROUP BY o.order_no, o.order_id, o.user_id, o.address_id, o.order_number, o.status, o.ordered_at;

INSERT INTO "order" (id, user_id, address_id, order_number, subtotal, shipping_fee, total_amount,
                     status, ordered_at, required_advance_amount)
SELECT order_id, user_id, address_id, order_number, subtotal, shipping_fee,
       subtotal + shipping_fee, status, ordered_at, 0
FROM _demo_order_totals
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    address_id = EXCLUDED.address_id,
    order_number = EXCLUDED.order_number,
    subtotal = EXCLUDED.subtotal,
    shipping_fee = EXCLUDED.shipping_fee,
    total_amount = EXCLUDED.total_amount,
    status = EXCLUDED.status,
    ordered_at = EXCLUDED.ordered_at,
    required_advance_amount = EXCLUDED.required_advance_amount;

INSERT INTO order_item (id, order_id, product_id, gift_box_config_id, product_variant_id,
                        item_name_snapshot, sku_snapshot, variant_snapshot, unit_price, quantity,
                        subtotal, return_policy_snapshot, advance_payment_percent_snapshot,
                        required_advance_amount, is_perishable_snapshot, shelf_life_hours_snapshot)
SELECT md5('giftly-demo-order-item:' || lpad(line.order_no::text,
                                             GREATEST(3, length(line.order_no::text)), '0')
           || ':' || line.line_no)::uuid,
       totals.order_id,
       NULL, NULL, line.variant_id,
       line.product_name, line.sku,
       jsonb_build_object('label', line.label, 'sku', line.sku),
       line.price, line.quantity, line.price * line.quantity,
       line.return_policy, line.advance_payment_percent,
       line.price * line.quantity * line.advance_payment_percent / 100,
       line.is_perishable, line.shelf_life_hours
FROM _demo_order_line_seed line
JOIN _demo_order_totals totals ON totals.order_no = line.order_no
ON CONFLICT (id) DO UPDATE SET
    order_id = EXCLUDED.order_id,
    product_id = EXCLUDED.product_id,
    gift_box_config_id = EXCLUDED.gift_box_config_id,
    product_variant_id = EXCLUDED.product_variant_id,
    item_name_snapshot = EXCLUDED.item_name_snapshot,
    sku_snapshot = EXCLUDED.sku_snapshot,
    variant_snapshot = EXCLUDED.variant_snapshot,
    unit_price = EXCLUDED.unit_price,
    quantity = EXCLUDED.quantity,
    subtotal = EXCLUDED.subtotal,
    return_policy_snapshot = EXCLUDED.return_policy_snapshot,
    advance_payment_percent_snapshot = EXCLUDED.advance_payment_percent_snapshot,
    required_advance_amount = EXCLUDED.required_advance_amount,
    is_perishable_snapshot = EXCLUDED.is_perishable_snapshot,
    shelf_life_hours_snapshot = EXCLUDED.shelf_life_hours_snapshot;

INSERT INTO payment (id, order_id, payment_method, payment_status, amount, paid_at, payment_stage)
SELECT md5('giftly-demo-payment:' || lpad(o.order_no::text,
                                          GREATEST(3, length(o.order_no::text)), '0'))::uuid,
       o.order_id,
       CASE o.order_no % 2
           WHEN 0 THEN 'COD'
           ELSE 'ZALOPAY'
       END,
       CASE o.status
           WHEN 'PENDING_CONFIRMATION' THEN 'PENDING'
           WHEN 'CONFIRMED' THEN 'PENDING'
           WHEN 'PROCESSING' THEN 'PAID'
           WHEN 'PACKAGING' THEN 'PAID'
           WHEN 'READY_TO_SHIP' THEN 'PAID'
           WHEN 'SHIPPING' THEN 'PAID'
           WHEN 'DELIVERED' THEN 'PAID'
           WHEN 'COMPLETED' THEN 'PAID'
           WHEN 'RETURN_REQUESTED' THEN 'PAID'
           WHEN 'RETURNED' THEN 'REFUNDED'
           WHEN 'CANCELLED' THEN 'REFUNDED'
           ELSE 'FAILED'
       END,
       totals.subtotal + totals.shipping_fee,
       CASE WHEN o.status IN ('PROCESSING', 'PACKAGING', 'READY_TO_SHIP', 'SHIPPING', 'DELIVERED',
                              'COMPLETED', 'RETURN_REQUESTED', 'RETURNED', 'CANCELLED')
            THEN o.ordered_at + INTERVAL '15 minutes' ELSE NULL END,
       'FULL_PAYMENT'
FROM _demo_order_seed o
JOIN _demo_order_totals totals ON totals.order_no = o.order_no
ON CONFLICT (id) DO UPDATE SET
    order_id = EXCLUDED.order_id,
    payment_method = EXCLUDED.payment_method,
    payment_status = EXCLUDED.payment_status,
    amount = EXCLUDED.amount,
    paid_at = EXCLUDED.paid_at,
    payment_stage = EXCLUDED.payment_stage;

INSERT INTO review (id, user_id, product_id, order_item_id, rating, title, comment,
                    created_at, updated_at, product_variant_id, order_item_component_id)
SELECT md5('giftly-demo-review:' || lpad(o.order_no::text,
                                         GREATEST(3, length(o.order_no::text)), '0'))::uuid,
       o.user_id,
       line.product_id,
       md5('giftly-demo-order-item:' || lpad(o.order_no::text,
                                            GREATEST(3, length(o.order_no::text)), '0') || ':1')::uuid,
       CASE WHEN o.order_no % 4 = 0 THEN 4 ELSE 5 END,
       CASE o.order_no % 4
           WHEN 0 THEN 'Đóng gói cẩn thận'
           WHEN 1 THEN 'Món quà rất vừa ý'
           WHEN 2 THEN 'Sản phẩm đúng mô tả'
           ELSE 'Sẽ tiếp tục ủng hộ'
       END,
       CASE o.order_no % 4
           WHEN 0 THEN 'Hộp được chèn giấy chắc chắn, sản phẩm đến nơi nguyên vẹn.'
           WHEN 1 THEN 'Màu sắc đẹp và chất lượng tốt, người nhận rất thích món quà.'
           WHEN 2 THEN 'Đặt hàng nhanh, tư vấn rõ ràng và giao đúng thời gian dự kiến.'
           ELSE 'Sản phẩm chỉn chu, phần thiệp viết tay là điểm mình thích nhất.'
       END,
       o.ordered_at + INTERVAL '2 days',
       NULL,
       line.variant_id,
       NULL
FROM _demo_order_seed o
JOIN _demo_order_line_seed line ON line.order_no = o.order_no AND line.line_no = 1
WHERE o.status IN ('COMPLETED', 'DELIVERED')
ON CONFLICT (id) DO UPDATE SET
    user_id = EXCLUDED.user_id,
    product_id = EXCLUDED.product_id,
    order_item_id = EXCLUDED.order_item_id,
    rating = EXCLUDED.rating,
    title = EXCLUDED.title,
    comment = EXCLUDED.comment,
    product_variant_id = EXCLUDED.product_variant_id;

INSERT INTO review_image (id, review_id, secure_url, public_id, created_at)
SELECT md5('giftly-demo-review-image:' || lpad(o.order_no::text,
                                              GREATEST(3, length(o.order_no::text)), '0'))::uuid,
       md5('giftly-demo-review:' || lpad(o.order_no::text,
                                         GREATEST(3, length(o.order_no::text)), '0'))::uuid,
       'https://images.unsplash.com/photo-1512909006721-3d6018887383?auto=format&fit=crop&w=900&q=85',
       'giftly-demo/review/' || lpad(o.order_no::text,
                                     GREATEST(3, length(o.order_no::text)), '0'),
       o.ordered_at + INTERVAL '2 days'
FROM _demo_order_seed o
WHERE o.status IN ('COMPLETED', 'DELIVERED') AND o.order_no % 2 = 0
ON CONFLICT (id) DO UPDATE SET
    review_id = EXCLUDED.review_id,
    secure_url = EXCLUDED.secure_url,
    public_id = EXCLUDED.public_id;

DO $$
DECLARE
    seeded_products INTEGER;
    seeded_customers INTEGER;
    seeded_orders INTEGER;
BEGIN
    SELECT COUNT(DISTINCT p.id) INTO seeded_products
    FROM product p
    JOIN product_variant v ON v.product_id = p.id
    WHERE v.sku LIKE 'DEMO-%';

    SELECT COUNT(*) INTO seeded_customers
    FROM "user" u
    WHERE u.email LIKE '%@example.test';

    SELECT COUNT(*) INTO seeded_orders
    FROM "order" o
    WHERE o.order_number LIKE 'GLY-DEMO-2026-%';

    IF seeded_products < 100 OR seeded_customers < 100 OR seeded_orders < 1000 THEN
        RAISE EXCEPTION 'Demo seed count check failed: products %, customers %, orders %',
            seeded_products, seeded_customers, seeded_orders;
    END IF;
END $$;

SELECT 'products' AS dataset, COUNT(DISTINCT p.id) AS rows
FROM product p JOIN product_variant v ON v.product_id = p.id WHERE v.sku LIKE 'DEMO-%'
UNION ALL
SELECT 'customers', COUNT(*) FROM "user" WHERE email LIKE '%@example.test'
UNION ALL
SELECT 'orders', COUNT(*) FROM "order" WHERE order_number LIKE 'GLY-DEMO-2026-%';

COMMIT;