-- V10: Add product_id column back to product_image for common product images

-- Add nullable product_id column with FK to products table
ALTER TABLE product_image ADD COLUMN IF NOT EXISTS product_id UUID;
ALTER TABLE product_image ADD CONSTRAINT fk_product_image_product_common
    FOREIGN KEY (product_id) REFERENCES product(id);

-- Make product_variant_id nullable since images can belong to product directly (common images)
ALTER TABLE product_image ALTER COLUMN product_variant_id DROP NOT NULL;

-- Add index for faster lookups
CREATE INDEX IF NOT EXISTS idx_product_image_product ON product_image (product_id);
