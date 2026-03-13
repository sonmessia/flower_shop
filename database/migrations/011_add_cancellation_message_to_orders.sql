-- Add cancellation message so admins can notify users when an order is cancelled
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS cancellation_message TEXT;
