-- Migration: Add image column to product_images table
-- Date: 2025-11-13
-- Description: Add a column to store image data in binary format

ALTER TABLE product_images 
ADD COLUMN IF NOT EXISTS image BYTEA;
