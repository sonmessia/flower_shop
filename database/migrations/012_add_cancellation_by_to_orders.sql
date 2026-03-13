-- Track who cancelled the order (admin/customer)
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS cancellation_by VARCHAR(255);
