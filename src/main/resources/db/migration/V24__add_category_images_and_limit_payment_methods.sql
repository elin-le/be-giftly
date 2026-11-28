ALTER TABLE category ADD COLUMN image_public_id VARCHAR(255);
ALTER TABLE category ADD COLUMN image_secure_url VARCHAR(500);

UPDATE payment
SET payment_method = 'COD'
WHERE payment_method NOT IN ('COD', 'ZALOPAY');

ALTER TABLE payment
    ADD CONSTRAINT chk_payment_method_supported
    CHECK (payment_method IN ('COD', 'ZALOPAY'));