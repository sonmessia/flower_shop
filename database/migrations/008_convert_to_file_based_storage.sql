-- Migration 008: Update products and product_images tables to use file-based storage
-- This migration converts from BYTEA binary storage to VARCHAR file path storage

-- Step 1: Add new columns for file-based storage (if not exists)
ALTER TABLE products ADD COLUMN IF NOT EXISTS main_image_url VARCHAR(500);

-- Step 2: Drop old binary column from products (if exists)
ALTER TABLE products DROP COLUMN IF EXISTS image CASCADE;

-- Step 3: Update product_images table
-- Rename old image column to image_backup temporarily
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'product_images' 
        AND column_name = 'image'
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE product_images RENAME COLUMN image TO image_backup;
    END IF;
END $$;

-- Add new image_url column
ALTER TABLE product_images ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Step 4: Drop the backup column (since we can't migrate binary data to URLs anyway)
ALTER TABLE product_images DROP COLUMN IF EXISTS image_backup CASCADE;

-- Step 5: Make image_url NOT NULL (after all data is migrated)
-- Note: This will fail if there are existing rows without image_url
-- Run this only after populating image_url values
-- ALTER TABLE product_images ALTER COLUMN image_url SET NOT NULL;

-- Step 6: Add comments
COMMENT ON COLUMN products.main_image_url IS 'Public URL to access the main product image stored in file system';
COMMENT ON COLUMN product_images.image_url IS 'Public URL to access the product image stored in file system';

-- Step 7: Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_products_main_image_url ON products(main_image_url);
CREATE INDEX IF NOT EXISTS idx_product_images_image_url ON product_images(image_url);
