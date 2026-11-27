-- V11: Add implicit cast for variant_status enum to fix Hibernate string insertion

-- Allow PostgreSQL to automatically cast varchar to variant_status enum
-- This fixes the issue where Hibernate's @Enumerated(EnumType.STRING) sends 'AVAILABLE' as varchar
-- but the column expects variant_status enum type
CREATE CAST (varchar AS variant_status) WITH INOUT;
