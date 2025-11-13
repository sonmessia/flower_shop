#!/bin/bash

# Test script for image URL download functionality

API_BASE="http://localhost:8080/api"
ADMIN_TOKEN=""

echo "======================================"
echo "Testing Image URL Download"
echo "======================================"

# Test 1: Create product with image URL
echo -e "\n1. Creating product with image URL..."
PRODUCT_RESPONSE=$(curl -s -X POST "$API_BASE/products" \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "TEST_URL_001",
    "name": "Test Product with URL Image",
    "description": "Testing image download from URL",
    "price": 100000,
    "imageUrl": "https://picsum.photos/400/400",
    "categoryId": 1
  }')

echo "Response: $PRODUCT_RESPONSE"

# Extract product ID
PRODUCT_ID=$(echo $PRODUCT_RESPONSE | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
echo "Created product ID: $PRODUCT_ID"

# Test 2: Get product to verify image URL
if [ ! -z "$PRODUCT_ID" ]; then
  echo -e "\n2. Getting product to verify image URL..."
  curl -s "$API_BASE/products/$PRODUCT_ID" | jq '.'
  
  # Test 3: Access image directly
  echo -e "\n3. Testing image access..."
  IMAGE_URL=$(curl -s "$API_BASE/products/$PRODUCT_ID" | jq -r '.imageUrl')
  echo "Image URL: $IMAGE_URL"
  
  if [ ! -z "$IMAGE_URL" ] && [ "$IMAGE_URL" != "null" ]; then
    echo "Testing image download..."
    curl -I "$IMAGE_URL"
  else
    echo "ERROR: No image URL found!"
  fi
  
  # Test 4: Update product with new image URL
  echo -e "\n4. Updating product with new image URL..."
  curl -s -X PUT "$API_BASE/products/$PRODUCT_ID" \
    -H "Content-Type: application/json" \
    -d '{
      "productCode": "TEST_URL_001",
      "name": "Test Product with URL Image (Updated)",
      "description": "Testing image download from URL - updated",
      "price": 150000,
      "imageUrl": "https://picsum.photos/500/500",
      "categoryId": 1
    }' | jq '.'
  
  # Test 5: Create product with multiple image URLs
  echo -e "\n5. Creating product with multiple image URLs..."
  curl -s -X POST "$API_BASE/products" \
    -H "Content-Type: application/json" \
    -d '{
      "productCode": "TEST_URL_002",
      "name": "Test Product with Multiple URLs",
      "description": "Testing multiple image downloads",
      "price": 200000,
      "imageUrl": "https://picsum.photos/400/400",
      "imageUrls": [
        "https://picsum.photos/401/401",
        "https://picsum.photos/402/402",
        "https://picsum.photos/403/403"
      ],
      "categoryId": 1
    }' | jq '.'
  
  # Cleanup
  echo -e "\n6. Cleaning up test products..."
  curl -s -X DELETE "$API_BASE/products/$PRODUCT_ID"
  echo "Deleted product $PRODUCT_ID"
else
  echo "ERROR: Failed to create product!"
fi

echo -e "\n======================================"
echo "Test completed!"
echo "Check backend logs for detailed download info:"
echo "  docker compose logs backend --tail 50"
echo "======================================"
