-- V3: Add unique constraint on review.order_item_id to enforce one review per order item

ALTER TABLE "review" ADD CONSTRAINT uk_review_order_item UNIQUE (order_item_id);
