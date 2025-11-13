-- Migration: Remove legacy image columns from product_images table
-- Date: 2025-11-13
-- Description: Clean up legacy columns as we're now using only binary storage

-- Remove legacy columns from product_images table
ALTER TABLE product_images 
DROP COLUMN IF EXISTS image_url,
DROP COLUMN IF EXISTS file_path;

-- Make image column NOT NULL since it's the primary storage
ALTER TABLE product_images 
ALTER COLUMN image SET NOT NULL;

-- Remove image_url from products table if exists (we only use binary storage now)
ALTER TABLE products 
DROP COLUMN IF EXISTS image_url;

-- Add comments
COMMENT ON COLUMN products.image IS 'Main product image stored as binary data (BYTEA)';
COMMENT ON COLUMN product_images.image IS 'Additional product image stored as binary data (BYTEA)';
COMMENT ON COLUMN product_images.file_name IS 'Original filename for reference';
COMMENT ON COLUMN product_images.display_order IS 'Order in which images should be displayed';
