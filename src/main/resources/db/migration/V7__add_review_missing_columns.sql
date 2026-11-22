-- V7: Add missing columns required by entities

-- 1. Review entity: title and updated_at
ALTER TABLE "review" ADD COLUMN IF NOT EXISTS title VARCHAR(255);
ALTER TABLE "review" ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;

-- 2. ReviewImage entity: image_url, public_id, created_at
-- Note: V4 created review_image with 'url' but entity uses 'image_url'
DO $$ BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='review_image' AND column_name='url') THEN
        ALTER TABLE review_image RENAME COLUMN url TO image_url;
    END IF;
END $$;
ALTER TABLE review_image ADD COLUMN IF NOT EXISTS public_id VARCHAR(255);
ALTER TABLE review_image ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
