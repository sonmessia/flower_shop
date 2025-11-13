-- Migration: Update product and product_images tables to use file paths instead of binary data
-- Date: 2025-11-13
-- Description: Replace BYTEA columns with VARCHAR for storing file paths

-- 1. Update products table
ALTER TABLE products DROP COLUMN IF EXISTS image CASCADE;
ALTER TABLE products ADD COLUMN IF NOT EXISTS main_image_url VARCHAR(500);

COMMENT ON COLUMN products.main_image_url IS 'Relative path to main product image file';

-- 2. Update product_images table
ALTER TABLE product_images DROP COLUMN IF EXISTS image CASCADE;
ALTER TABLE product_images ALTER COLUMN image_url DROP NOT NULL;
ALTER TABLE product_images ALTER COLUMN image_url TYPE VARCHAR(500);
ALTER TABLE product_images ALTER COLUMN image_url SET NOT NULL;

COMMENT ON COLUMN product_images.image_url IS 'Relative path to product image file';

-- 3. Clean up legacy columns (if any)
ALTER TABLE product_images DROP COLUMN IF EXISTS file_path CASCADE;

COMMENT ON TABLE products IS 'Products table with file-based image storage';
COMMENT ON TABLE product_images IS 'Additional product images with file-based storage';
