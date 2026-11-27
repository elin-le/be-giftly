CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE INDEX IF NOT EXISTS idx_product_variant_product_price
    ON product_variant(product_id, price);