-- Migration: Update product_images table to make image_url nullable
-- Date: 2025-11-13
-- Description: Allow image_url to be null since we're storing binary data in the image column

ALTER TABLE product_images 
ALTER COLUMN image_url DROP NOT NULL;

-- Make image column NOT NULL since we're storing binary data
ALTER TABLE product_images 
ALTER COLUMN image DROP NOT NULL;

COMMENT ON COLUMN product_images.image IS 'Binary image data stored in database';
COMMENT ON COLUMN product_images.image_url IS 'Legacy field - can be null when using binary storage';
