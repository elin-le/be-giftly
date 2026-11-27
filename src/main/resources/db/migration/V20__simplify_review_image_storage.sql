-- Preserve the usable legacy URL when secure_url was left at its default, then remove the duplicate column.
UPDATE review_image
SET secure_url = image_url
WHERE secure_url IS NULL OR secure_url = '';

ALTER TABLE review_image ALTER COLUMN secure_url SET NOT NULL;
ALTER TABLE review_image DROP COLUMN image_url;