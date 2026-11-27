ALTER TABLE product ADD COLUMN custom_component_type VARCHAR(30);

UPDATE product
SET custom_component_type = CASE product_type
    WHEN 'READY_MADE_GIFT_BOX' THEN 'BOX'
    WHEN 'PACKAGING_ACCESSORY' THEN 'ACCESSORY'
    WHEN 'GREETING_CARD' THEN 'GREETING_CARD'
    ELSE NULL
END;

ALTER TABLE product ADD CONSTRAINT chk_product_custom_component_type CHECK (
    custom_component_type IS NULL
    OR (custom_component_type = 'BOX' AND product_type = 'READY_MADE_GIFT_BOX')
    OR (custom_component_type = 'GREETING_CARD' AND product_type = 'GREETING_CARD')
    OR (custom_component_type IN ('WRAPPING', 'RIBBON', 'FILLER', 'ACCESSORY')
        AND product_type = 'PACKAGING_ACCESSORY')
);

ALTER TABLE gift_box_config_item ADD COLUMN component_type VARCHAR(30);
ALTER TABLE gift_box_config_item ADD CONSTRAINT fk_gbci_product_variant
    FOREIGN KEY (product_variant_id) REFERENCES product_variant(id);

UPDATE gift_box_config_item i
SET component_type = (
    SELECT p.custom_component_type
    FROM product_variant v
    JOIN product p ON p.id = v.product_id
    WHERE v.id = i.product_variant_id
)
WHERE i.quantity = 1
  AND EXISTS (
      SELECT 1
      FROM product_variant v
      JOIN product p ON p.id = v.product_id
      WHERE v.id = i.product_variant_id
        AND p.custom_component_type IS NOT NULL
        AND p.custom_component_type <> 'BOX'
        AND (
            SELECT COUNT(*)
            FROM gift_box_config_item i2
            JOIN product_variant v2 ON v2.id = i2.product_variant_id
            JOIN product p2 ON p2.id = v2.product_id
            WHERE i2.config_id = i.config_id
              AND p2.custom_component_type = p.custom_component_type
        ) = 1
  );

ALTER TABLE gift_box_config_item ADD CONSTRAINT chk_gbci_component_quantity
    CHECK (component_type IS NULL OR quantity = 1);
ALTER TABLE gift_box_config_item ADD CONSTRAINT chk_gbci_component_type CHECK (
    component_type IS NULL OR component_type IN ('BOX', 'WRAPPING', 'RIBBON', 'FILLER', 'GREETING_CARD', 'ACCESSORY')
);
ALTER TABLE gift_box_config_item ADD CONSTRAINT uk_gbci_config_component_type
    UNIQUE (config_id, component_type);

INSERT INTO gift_box_config_item (id, config_id, product_variant_id, quantity, component_type)
SELECT gen_random_uuid(), id, box_variant_id, 1, 'BOX'
FROM gift_box_config;

ALTER TABLE gift_box_config DROP COLUMN box_variant_id;