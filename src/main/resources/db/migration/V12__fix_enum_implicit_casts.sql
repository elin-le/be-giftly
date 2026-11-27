-- V12: Fix implicit casts for PostgreSQL enum types used with @Enumerated(EnumType.STRING)
-- V11 used incorrect syntax (WITH INOUT without AS IMPLICIT).
-- This migration drops the old cast and recreates it with the correct syntax.

-- Fix variant_status cast (used in ProductVariant.status)
DROP CAST IF EXISTS (varchar AS variant_status);
CREATE CAST (varchar AS variant_status) WITH INOUT AS IMPLICIT;

-- Fix gift_box_review_status cast (used in GiftBoxConfig.reviewStatus)
DROP CAST IF EXISTS (varchar AS gift_box_review_status);
CREATE CAST (varchar AS gift_box_review_status) WITH INOUT AS IMPLICIT;
