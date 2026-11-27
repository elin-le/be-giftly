-- V14: Add price_adjustment to product_option_value
-- Allows each option value (e.g., "Size L", "Color Red") to have a price adjustment
-- that is added to the variant's base price to calculate the final price.
-- Default: 0 (no price change).

ALTER TABLE product_option_value ADD COLUMN price_adjustment NUMERIC(19, 2) NOT NULL DEFAULT 0;
