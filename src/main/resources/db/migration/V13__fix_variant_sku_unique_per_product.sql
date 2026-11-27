-- V13: Fix product_variant SKU unique constraint to be per-product instead of global
-- The current UNIQUE constraint on sku prevents different products from having variants
-- with the same SKU (e.g., "Màu tím" on product A blocks "Màu tím" on product B).
-- SKU should only be unique within the same product.

-- Drop the global unique constraint
ALTER TABLE product_variant DROP CONSTRAINT product_variant_sku_key;

-- Add a unique constraint per product (product_id, sku)
ALTER TABLE product_variant ADD CONSTRAINT product_variant_product_sku_key UNIQUE (product_id, sku);
