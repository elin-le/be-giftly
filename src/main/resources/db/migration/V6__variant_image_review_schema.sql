-- V6: Update product_image to variant-only, add gift_box_config review columns, add variant_status enum

-- 1. Create variant_status enum
DO $$ BEGIN
    CREATE TYPE variant_status AS ENUM ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 2. Create gift_box_review_status enum
DO $$ BEGIN
    CREATE TYPE gift_box_review_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED', 'REVISION_REQUIRED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. Update product_image: remove product_id, make product_variant_id NOT NULL, add is_primary and display_order
-- First, migrate existing images: if product_id is set but product_variant_id is null,
-- assign to the first variant of that product
UPDATE product_image
SET product_variant_id = (
    SELECT pv.id FROM product_variant pv
    WHERE pv.product_id = product_image.product_id
    LIMIT 1
)
WHERE product_variant_id IS NULL AND product_id IS NOT NULL;

-- Drop old FK and column
ALTER TABLE product_image DROP CONSTRAINT IF EXISTS fk_product_image_product;
ALTER TABLE product_image DROP COLUMN IF EXISTS product_id;

-- Make product_variant_id NOT NULL
ALTER TABLE product_image ALTER COLUMN product_variant_id SET NOT NULL;

-- Rename is_thumbnail to is_primary and add display_order
ALTER TABLE product_image RENAME COLUMN is_thumbnail TO is_primary;
ALTER TABLE product_image ADD COLUMN IF NOT EXISTS display_order INTEGER;

-- 4. Update product_variant status column to use variant_status enum
-- Migrate existing ProductStatus values to VariantStatus
ALTER TABLE product_variant
    ADD COLUMN status_new variant_status;

UPDATE product_variant
SET status_new = status::variant_status;

ALTER TABLE product_variant DROP COLUMN status;
ALTER TABLE product_variant RENAME COLUMN status_new TO status;

-- 5. Add gift_box_config review columns
ALTER TABLE gift_box_config ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE gift_box_config ADD COLUMN IF NOT EXISTS review_status gift_box_review_status NOT NULL DEFAULT 'PENDING';
ALTER TABLE gift_box_config ADD COLUMN IF NOT EXISTS review_note TEXT;
ALTER TABLE gift_box_config ADD COLUMN IF NOT EXISTS capacity_ratio NUMERIC(5,2);

-- Add constraint: capacity_ratio between 0 and 100
ALTER TABLE gift_box_config ADD CONSTRAINT chk_capacity_ratio
    CHECK (capacity_ratio IS NULL OR (capacity_ratio >= 0 AND capacity_ratio <= 100));

-- 6. Add partial unique index: at most one primary image per variant
CREATE UNIQUE INDEX IF NOT EXISTS uk_product_image_primary_variant
    ON product_image (product_variant_id) WHERE is_primary = TRUE;

-- 7. Add indexes
CREATE INDEX IF NOT EXISTS idx_gift_box_config_review_status ON gift_box_config (review_status);
