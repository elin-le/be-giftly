-- V9: Make product.price nullable
-- Price belongs to ProductVariant, not Product.
-- The product.price column is kept for backward compatibility but made nullable.

ALTER TABLE product ALTER COLUMN price DROP NOT NULL;

-- Clear existing prices from product table (they are now in product_variant)
UPDATE product SET price = NULL WHERE price IS NOT NULL;
