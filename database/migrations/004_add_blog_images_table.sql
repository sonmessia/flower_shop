-- Migration: Add blog_images table for supporting multiple images per blog
-- Created: 2025-01-13

CREATE TABLE IF NOT EXISTS blog_images (
    id BIGSERIAL PRIMARY KEY,
    blog_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255),
    file_path VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    CONSTRAINT fk_blog_images_blog FOREIGN KEY (blog_id) REFERENCES blogs(id) ON DELETE CASCADE
);

-- Create index for faster lookups
CREATE INDEX idx_blog_images_blog_id ON blog_images(blog_id);
CREATE INDEX idx_blog_images_display_order ON blog_images(display_order);

-- Add comment to table
COMMENT ON TABLE blog_images IS 'Stores additional images for blog posts';
COMMENT ON COLUMN blog_images.blog_id IS 'Reference to the blog post';
COMMENT ON COLUMN blog_images.image_url IS 'URL of the image';
COMMENT ON COLUMN blog_images.file_name IS 'Original filename of the uploaded image';
COMMENT ON COLUMN blog_images.file_path IS 'Path where the image is stored';
COMMENT ON COLUMN blog_images.display_order IS 'Order in which images should be displayed';
