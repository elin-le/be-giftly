-- V2: Enable pg_trgm extension for fuzzy/typo-tolerant search

CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create GIN indexes for trigram-based search on product name and description
CREATE INDEX IF NOT EXISTS idx_product_name_trgm ON product USING GIN (name gin_trgm_ops);
CREATE INDEX IF NOT EXISTS idx_product_description_trgm ON product USING GIN (description gin_trgm_ops);
