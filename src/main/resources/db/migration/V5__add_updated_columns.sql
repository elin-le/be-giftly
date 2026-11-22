-- V5: Add updated columns per Giftly_Database_Design_Updated.md

-- 1. gift_box_config_item: add message fields for greeting cards in Custom Box
ALTER TABLE gift_box_config_item
    ADD COLUMN message_text TEXT NULL;

ALTER TABLE gift_box_config_item
    ADD COLUMN message_rendering_method VARCHAR(20) NULL;

-- 2. bundle_option_group: add display_order
ALTER TABLE bundle_option_group
    ADD COLUMN display_order INTEGER NULL;

-- 3. order_item: add perishable policy snapshots
ALTER TABLE order_item
    ADD COLUMN is_perishable_snapshot BOOLEAN NULL;

ALTER TABLE order_item
    ADD COLUMN shelf_life_hours_snapshot INTEGER NULL;

-- 4. order_item_component: add perishable policy snapshots
ALTER TABLE order_item_component
    ADD COLUMN is_perishable_snapshot BOOLEAN NULL;

ALTER TABLE order_item_component
    ADD COLUMN shelf_life_hours_snapshot INTEGER NULL;

-- 5. product: add timestamps
ALTER TABLE product
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE product
    ADD COLUMN updated_at TIMESTAMP NULL;

-- 6. product_variant_policy: add perishable policy check constraint
ALTER TABLE product_variant_policy
    ADD CONSTRAINT chk_perishable_policy
    CHECK (NOT is_perishable OR (return_policy = 'NO_RETURN' AND advance_payment_percent >= 50));

-- 7. review: add purchase source check constraint and unique constraint on component
ALTER TABLE review
    ADD CONSTRAINT chk_review_purchase_source
    CHECK (
        (order_item_id IS NOT NULL AND order_item_component_id IS NULL) OR
        (order_item_id IS NULL AND order_item_component_id IS NOT NULL)
    );

ALTER TABLE review
    ADD CONSTRAINT uk_review_order_item_component UNIQUE (order_item_component_id);
