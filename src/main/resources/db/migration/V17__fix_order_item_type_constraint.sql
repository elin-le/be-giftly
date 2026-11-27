ALTER TABLE order_item DROP CONSTRAINT chk_order_item_type;

ALTER TABLE order_item ADD CONSTRAINT chk_order_item_type CHECK (
    (product_variant_id IS NOT NULL AND gift_box_config_id IS NULL) OR
    (product_variant_id IS NULL AND gift_box_config_id IS NOT NULL)
);