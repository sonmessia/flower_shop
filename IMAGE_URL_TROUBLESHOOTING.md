# Troubleshooting Guide - Image URL Download Issues

## Vấn Đề: Mất Hình Ảnh Khi Lưu Từ URL

### Nguyên Nhân Có Thể

1. **URL không hợp lệ hoặc không truy cập được**
2. **Timeout khi download**
3. **Server từ chối request (403 Forbidden)**
4. **Lỗi network/firewall**
5. **URL yêu cầu authentication**

## Cách Kiểm Tra

### 1. Kiểm Tra Logs Backend

```bash
# Xem logs real-time
docker compose logs -f backend

# Xem 100 dòng logs gần nhất
docker compose logs backend --tail 100

# Tìm kiếm lỗi liên quan đến URL
docker compose logs backend --tail 200 | grep -i "url\|download\|error"
```

**Các thông báo cần chú ý:**
- `Downloading image from URL: ...` - Bắt đầu download
- `Successfully downloaded and saved image from URL` - Thành công
- `Failed to download image from URL` - Thất bại
- `HTTP code: 403/404/500` - Lỗi HTTP

### 2. Test URL Trực Tiếp

```bash
# Test URL có download được không
curl -I "https://example.com/image.jpg"

# Download thử
curl -o test.jpg "https://example.com/image.jpg"

# Test với User-Agent (giống backend)
curl -H "User-Agent: Mozilla/5.0" -I "https://example.com/image.jpg"
```

### 3. Kiểm Tra Database

```bash
# Connect vào database
docker exec -it flowershop-db-prod psql -U postgres -d flowershop

# Kiểm tra products có main_image_url
SELECT id, name, main_image_url FROM products WHERE id = 1;

# Kiểm tra product_images có image_url
SELECT id, image_url, file_name FROM product_images WHERE product_id = 1;

# Tìm sản phẩm không có ảnh
SELECT id, name FROM products WHERE main_image_url IS NULL;
```

### 4. Kiểm Tra File Storage

```bash
# Kiểm tra thư mục images
docker exec -it flowershop-backend-prod ls -la /app/images/products/

# Đếm số file
docker exec -it flowershop-backend-prod find /app/images/products/ -type f | wc -l

# Xem dung lượng
docker exec -it flowershop-backend-prod du -sh /app/images/
```

### 5. Test API Endpoint

```bash
# Test tạo sản phẩm với URL
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "TEST001",
    "name": "Test Product",
    "description": "Testing",
    "price": 100000,
    "imageUrl": "https://picsum.photos/400/400",
    "categoryId": 1
  }'

# Kiểm tra response có imageUrl không
# Response should contain: "imageUrl": "http://localhost:8080/images/products/xxx.jpg"
```

## Các Lỗi Thường Gặp

### Lỗi 1: `Failed to download image from URL: ..., HTTP code: 403`

**Nguyên nhân:** Server từ chối request (thường do thiếu User-Agent header)

**Giải pháp:** Backend đã được cấu hình User-Agent, nhưng một số website vẫn block. Thử:
1. Sử dụng URL từ nguồn khác
2. Download manual và upload file thay vì URL
3. Host ảnh trên CDN của riêng bạn

### Lỗi 2: `java.net.SocketTimeoutException: Read timed out`

**Nguyên nhân:** Timeout khi download (file quá lớn hoặc mạng chậm)

**Giải pháp:** Tăng timeout trong `FileStorageService.java`:
```java
connection.setConnectTimeout(30000); // 30 seconds
connection.setReadTimeout(30000); // 30 seconds
```

### Lỗi 3: `Image URL does not match base URL`

**Nguyên nhân:** Cấu hình `STORAGE_BASE_URL` không đúng

**Kiểm tra:**
```bash
# Xem environment variables
docker exec -it flowershop-backend-prod env | grep STORAGE
```

**Sửa trong docker-compose.yml:**
```yaml
environment:
  STORAGE_BASE_URL: http://localhost:8080/images
```

### Lỗi 4: File không tồn tại khi truy cập

**Nguyên nhân:** 
- File chưa được lưu thành công
- Sai đường dẫn
- Volume chưa được mount đúng

**Kiểm tra:**
```bash
# Xem volume
docker volume inspect flower_shop_images_data

# Kiểm tra mount point
docker exec -it flowershop-backend-prod mount | grep images
```

### Lỗi 5: `IllegalArgumentException: Image URL cannot be null or empty`

**Nguyên nhân:** Frontend gửi imageUrl = null hoặc ""

**Kiểm tra Frontend:**
```javascript
// Đảm bảo không gửi imageUrl nếu rỗng
const payload = {
  productCode: "SP001",
  name: "Product",
  price: 100000,
  categoryId: 1
}

// Chỉ thêm imageUrl nếu có giá trị
if (imageUrl && imageUrl.trim() !== '') {
  payload.imageUrl = imageUrl
}
```

## Debugging Steps

### Step 1: Enable Debug Logging

Thêm vào `application.properties`:
```properties
logging.level.vn.quahoa.flowershop.service.FileStorageService=DEBUG
logging.level.vn.quahoa.flowershop.service.ProductService=DEBUG
```

### Step 2: Monitor Real-time

```bash
# Terminal 1: Follow logs
docker compose logs -f backend

# Terminal 2: Test API
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{ ... }'
```

### Step 3: Verify Image Access

```bash
# Get product
RESPONSE=$(curl -s http://localhost:8080/api/products/1)
echo $RESPONSE | jq '.'

# Extract image URL
IMAGE_URL=$(echo $RESPONSE | jq -r '.imageUrl')
echo "Image URL: $IMAGE_URL"

# Test image access
curl -I $IMAGE_URL
```

### Step 4: Check File System

```bash
# List all images
docker exec -it flowershop-backend-prod find /app/images -type f -name "*.jpg" -o -name "*.png"

# Check permissions
docker exec -it flowershop-backend-prod ls -la /app/images/products/
```

## Solutions

### Giải Pháp 1: Sử dụng File Upload Thay Vì URL

Nếu URL không hoạt động, hãy dùng file upload:

```javascript
// Frontend
const formData = new FormData()
formData.append('file', fileObject)

const response = await fetch(`/api/products/${productId}/images/main`, {
  method: 'POST',
  body: formData
})
```

### Giải Pháp 2: Proxy URL Qua Backend

Nếu frontend không thể truy cập URL, proxy qua backend:

```java
@PostMapping("/products/{id}/images/main-proxy")
public String uploadMainImageFromProxy(@PathVariable Long id, @RequestBody ProxyRequest request) {
    // Backend tải ảnh từ URL và lưu
    return productService.uploadMainImageFromUrl(id, request.getUrl());
}
```

### Giải Pháp 3: Sử dụng CDN

Upload ảnh lên CDN trước, sau đó dùng CDN URL:

```javascript
// 1. Upload to CDN
const cdnUrl = await uploadToCDN(file)

// 2. Save CDN URL to backend
await createProduct({
  ...productData,
  imageUrl: cdnUrl
})
```

## Testing Script

Chạy script test:
```bash
chmod +x scripts/test_image_url.sh
./scripts/test_image_url.sh
```

## Additional Resources

- [FILE_STORAGE_GUIDE.md](./FILE_STORAGE_GUIDE.md) - Hướng dẫn chi tiết về hệ thống lưu trữ
- Backend logs: `docker compose logs backend`
- Database: `docker exec -it flowershop-db-prod psql -U postgres -d flowershop`

## Contact Support

Nếu vẫn gặp vấn đề, cung cấp thông tin sau:
1. Logs backend (50 dòng gần nhất)
2. Request payload (JSON)
3. Response từ API
4. URL ảnh đang sử dụng
5. Output của `docker exec -it flowershop-backend-prod ls -la /app/images/products/`
