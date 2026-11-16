-- V4: Major schema redesign - Product Variant, Bundle, Policy, and snapshot support

-- =============================================================================
-- 1. Product Options & Variants
-- =============================================================================

CREATE TABLE product_option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    display_order INTEGER,
    CONSTRAINT fk_product_option_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE product_option_value (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_id UUID NOT NULL,
    value VARCHAR(255) NOT NULL,
    color_hex VARCHAR(7),
    CONSTRAINT fk_product_option_value_option FOREIGN KEY (option_id) REFERENCES product_option(id)
);

CREATE TABLE product_variant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    sku VARCHAR(255) NOT NULL UNIQUE,
    price NUMERIC(19, 2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    length DOUBLE PRECISION,
    width DOUBLE PRECISION,
    height DOUBLE PRECISION,
    dimension_unit VARCHAR(10),
    weight DOUBLE PRECISION,
    weight_unit VARCHAR(10),
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_product_variant_product FOREIGN KEY (product_id) REFERENCES product(id)
);

CREATE TABLE product_variant_option_value (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variant_id UUID NOT NULL,
    option_value_id UUID NOT NULL,
    CONSTRAINT fk_pvo_variant FOREIGN KEY (variant_id) REFERENCES product_variant(id),
    CONSTRAINT fk_pvo_option_value FOREIGN KEY (option_value_id) REFERENCES product_option_value(id)
);

-- =============================================================================
-- 2. Gift Box Variant Detail (inner dimensions, capacity)
-- =============================================================================

CREATE TABLE gift_box_variant_detail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_variant_id UUID NOT NULL UNIQUE,
    inner_length DOUBLE PRECISION,
    inner_width DOUBLE PRECISION,
    inner_height DOUBLE PRECISION,
    max_weight NUMERIC(19, 2),
    dimension_unit VARCHAR(10),
    weight_unit VARCHAR(10),
    CONSTRAINT fk_gbvd_variant FOREIGN KEY (product_variant_id) REFERENCES product_variant(id)
);

-- =============================================================================
-- 3. Product Variant Policy (perishable, return, advance payment)
-- =============================================================================

CREATE TABLE product_variant_policy (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_variant_id UUID NOT NULL UNIQUE,
    is_perishable BOOLEAN DEFAULT FALSE,
    shelf_life_hours INTEGER,
    return_policy VARCHAR(20),
    advance_payment_percent NUMERIC(5, 2),
    CONSTRAINT fk_pvp_variant FOREIGN KEY (product_variant_id) REFERENCES product_variant(id),
    CONSTRAINT chk_advance_payment CHECK (advance_payment_percent IS NULL OR (advance_payment_percent BETWEEN 0 AND 100))
);

-- =============================================================================
-- 4. Bundle (Ready-made Gift Set)
-- =============================================================================

CREATE TABLE bundle_component (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_variant_id UUID NOT NULL,
    component_variant_id UUID NOT NULL,
    component_role VARCHAR(20) NOT NULL,
    quantity INTEGER NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_bc_bundle_variant FOREIGN KEY (bundle_variant_id) REFERENCES product_variant(id),
    CONSTRAINT fk_bc_component_variant FOREIGN KEY (component_variant_id) REFERENCES product_variant(id)
);

CREATE TABLE bundle_option_group (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_variant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    group_type VARCHAR(20) NOT NULL,
    selection_type VARCHAR(20) NOT NULL,
    min_selections INTEGER,
    max_selections INTEGER,
    is_required BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_bog_bundle_variant FOREIGN KEY (bundle_variant_id) REFERENCES product_variant(id)
);

CREATE TABLE bundle_option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    option_group_id UUID NOT NULL,
    product_variant_id UUID NOT NULL,
    pricing_mode VARCHAR(20) NOT NULL,
    fixed_price NUMERIC(19, 2),
    CONSTRAINT fk_bo_group FOREIGN KEY (option_group_id) REFERENCES bundle_option_group(id),
    CONSTRAINT fk_bo_variant FOREIGN KEY (product_variant_id) REFERENCES product_variant(id)
);

-- =============================================================================
-- 5. Cart Item Option Selection
-- =============================================================================

CREATE TABLE cart_item_option_selection (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_item_id UUID NOT NULL,
    bundle_option_id UUID NOT NULL,
    quantity INTEGER,
    message_text TEXT,
    message_rendering_method VARCHAR(20),
    CONSTRAINT fk_cios_cart_item FOREIGN KEY (cart_item_id) REFERENCES cart_item(id)
);

-- =============================================================================
-- 6. Update Product table
-- =============================================================================

ALTER TABLE product ADD COLUMN is_custom_box_eligible BOOLEAN DEFAULT FALSE;

-- Migrate existing products: move price/stock to default variants
-- First, create a default variant for each existing product
INSERT INTO product_variant (id, product_id, sku, price, stock_quantity, status)
SELECT
    gen_random_uuid(),
    p.id,
    'DEFAULT-' || SUBSTRING(MD5(p.id::text), 1, 10),
    p.price,
    COALESCE(p.stock_quantity, 0),
    p.status
FROM product p;

-- =============================================================================
-- 7. Update Product Image table
-- =============================================================================

ALTER TABLE product_image ADD COLUMN product_variant_id UUID REFERENCES product_variant(id);
ALTER TABLE product_image ALTER COLUMN product_id DROP NOT NULL;

-- =============================================================================
-- 8. Update Gift Box Config table
-- =============================================================================

-- Create new gift_box_config_item table
CREATE TABLE gift_box_config_item (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_id UUID NOT NULL,
    product_variant_id UUID NOT NULL,
    quantity INTEGER NOT NULL,
    CONSTRAINT fk_gbci_config FOREIGN KEY (config_id) REFERENCES gift_box_config(id)
);

-- Migrate existing gift_box_item to gift_box_config_item
INSERT INTO gift_box_config_item (id, config_id, product_variant_id, quantity)
SELECT
    gen_random_uuid(),
    gbi.config_id,
    pv.id,
    gbi.quantity
FROM gift_box_item gbi
JOIN product_variant pv ON pv.product_id = gbi.product_id
WHERE pv.sku LIKE 'DEFAULT-%';

-- Add box_variant_id to gift_box_config
ALTER TABLE gift_box_config ADD COLUMN box_variant_id UUID;

-- Migrate box_product_id to box_variant_id
UPDATE gift_box_config gbc
SET box_variant_id = pv.id
FROM product_variant pv
WHERE pv.product_id = gbc.box_product_id
AND pv.sku LIKE 'DEFAULT-%';

ALTER TABLE gift_box_config ALTER COLUMN box_variant_id SET NOT NULL;

-- =============================================================================
-- 9. Update Cart Item table
-- =============================================================================

ALTER TABLE cart_item ADD COLUMN product_variant_id UUID;

-- Migrate product_id to product_variant_id
UPDATE cart_item ci
SET product_variant_id = pv.id
FROM product_variant pv
WHERE pv.product_id = ci.product_id
AND pv.sku LIKE 'DEFAULT-%';

-- =============================================================================
-- 10. Update Order Item table
-- =============================================================================

ALTER TABLE order_item ADD COLUMN product_variant_id UUID;
ALTER TABLE order_item ADD COLUMN item_name_snapshot VARCHAR(255);
ALTER TABLE order_item ADD COLUMN sku_snapshot VARCHAR(255);
ALTER TABLE order_item ADD COLUMN variant_snapshot JSONB;
ALTER TABLE order_item ADD COLUMN return_policy_snapshot VARCHAR(20);
ALTER TABLE order_item ADD COLUMN advance_payment_percent_snapshot NUMERIC(5, 2);
ALTER TABLE order_item ADD COLUMN required_advance_amount NUMERIC(19, 2);

-- Migrate product_id to product_variant_id
UPDATE order_item oi
SET product_variant_id = pv.id,
    item_name_snapshot = oi.item_name
FROM product_variant pv
WHERE pv.product_id = oi.product_id
AND pv.sku LIKE 'DEFAULT-%';

-- =============================================================================
-- 11. Update Order Item Component table
-- =============================================================================

ALTER TABLE order_item_component ADD COLUMN product_variant_id UUID;
ALTER TABLE order_item_component ADD COLUMN component_role VARCHAR(20);
ALTER TABLE order_item_component ADD COLUMN product_name_snapshot VARCHAR(255);
ALTER TABLE order_item_component ADD COLUMN sku_snapshot VARCHAR(255);
ALTER TABLE order_item_component ADD COLUMN variant_snapshot JSONB;
ALTER TABLE order_item_component ADD COLUMN is_complimentary BOOLEAN;
ALTER TABLE order_item_component ADD COLUMN message_text TEXT;
ALTER TABLE order_item_component ADD COLUMN message_rendering_method VARCHAR(20);
ALTER TABLE order_item_component ADD COLUMN return_policy_snapshot VARCHAR(20);
ALTER TABLE order_item_component ADD COLUMN advance_payment_percent_snapshot NUMERIC(5, 2);

-- Migrate product_id to product_variant_id
UPDATE order_item_component oic
SET product_variant_id = pv.id,
    product_name_snapshot = oic.component_name
FROM product_variant pv
WHERE pv.product_id = oic.product_id
AND pv.sku LIKE 'DEFAULT-%';

-- =============================================================================
-- 12. Update Order table
-- =============================================================================

ALTER TABLE order ADD COLUMN required_advance_amount NUMERIC(19, 2);

-- =============================================================================
-- 13. Update Payment table
-- =============================================================================

ALTER TABLE payment ADD COLUMN payment_stage VARCHAR(20) NOT NULL DEFAULT 'FULL_PAYMENT';

-- =============================================================================
-- 14. Update Review table
-- =============================================================================

ALTER TABLE review ADD COLUMN product_variant_id UUID;
ALTER TABLE review ADD COLUMN order_item_component_id UUID REFERENCES order_item_component(id);

-- Migrate product_id reviews to use default variant
UPDATE review r
SET product_variant_id = pv.id
FROM product_variant pv
WHERE pv.product_id = r.product_id
AND pv.sku LIKE 'DEFAULT-%';

-- =============================================================================
-- 15. Update Review Image table
-- =============================================================================

ALTER TABLE review_image ADD COLUMN secure_url VARCHAR(500) NOT NULL DEFAULT '';

-- =============================================================================
-- 16. Create indexes
-- =============================================================================

CREATE INDEX idx_product_option_product ON product_option(product_id);
CREATE INDEX idx_product_option_value_option ON product_option_value(option_id);
CREATE INDEX idx_product_variant_product ON product_variant(product_id);
CREATE INDEX idx_product_variant_sku ON product_variant(sku);
CREATE INDEX idx_product_variant_status ON product_variant(status);
CREATE INDEX idx_product_variant_option_value_variant ON product_variant_option_value(variant_id);
CREATE INDEX idx_bundle_component_bundle ON bundle_component(bundle_variant_id);
CREATE INDEX idx_bundle_option_group_bundle ON bundle_option_group(bundle_variant_id);
CREATE INDEX idx_bundle_option_group ON bundle_option(option_group_id);
CREATE INDEX idx_cart_item_variant ON cart_item(product_variant_id);
CREATE INDEX idx_cart_item_option_selection ON cart_item_option_selection(cart_item_id);
CREATE INDEX idx_gift_box_config_variant ON gift_box_config(box_variant_id);
CREATE INDEX idx_gift_box_config_item ON gift_box_config_item(config_id);
CREATE INDEX idx_order_item_variant ON order_item(product_variant_id);
CREATE INDEX idx_order_item_component_variant ON order_item_component(product_variant_id);
CREATE INDEX idx_review_variant ON review(product_variant_id);
CREATE INDEX idx_product_image_variant ON product_image(product_variant_id);
CREATE INDEX idx_product_custom_eligible ON product(is_custom_box_eligible);
