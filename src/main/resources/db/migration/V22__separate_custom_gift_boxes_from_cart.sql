DELETE FROM cart_item
WHERE gift_box_config_id IS NOT NULL;

ALTER TABLE cart_item DROP CONSTRAINT IF EXISTS chk_cart_item_type;
ALTER TABLE cart_item DROP CONSTRAINT IF EXISTS fk_cart_item_gift_box;
ALTER TABLE cart_item DROP COLUMN IF EXISTS gift_box_config_id;

ALTER TABLE cart_item ADD CONSTRAINT chk_cart_item_type CHECK (
    product_variant_id IS NOT NULL
);