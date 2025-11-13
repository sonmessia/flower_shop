# Hướng Dẫn Sử Dụng Hệ Thống Lưu Trữ Ảnh File-Based

## Tổng Quan

Hệ thống đã được chuyển từ lưu trữ ảnh dưới dạng binary (BYTEA) trong database sang lưu trữ file trên file system với các ưu điểm:

- ✅ Hiệu suất tốt hơn (không tải binary qua database)
- ✅ Dễ dàng backup và restore
- ✅ Có thể phục vụ ảnh trực tiếp qua web server
- ✅ Tiết kiệm dung lượng database

## Cấu Trúc Lưu Trữ

### Thư Mục
```
/app/images/
├── products/          # Ảnh sản phẩm
│   ├── uuid_abc.jpg
│   └── uuid_xyz.png
└── blogs/             # Ảnh blog
    ├── uuid_def.jpg
    └── uuid_ghi.png
```

### Database Schema

**Table: products**
- `main_image_url` VARCHAR(500) - URL công khai để truy cập ảnh chính

**Table: product_images**
- `image_url` VARCHAR(500) NOT NULL - URL công khai để truy cập ảnh
- `file_name` VARCHAR(255) - Tên file gốc
- `display_order` INTEGER - Thứ tự hiển thị

## Cấu Hình

### application.properties
```properties
# File Storage Configuration
app.storage.local-path=${STORAGE_LOCAL_PATH:/app/images}
app.storage.base-url=${STORAGE_BASE_URL:http://localhost:8080/images}
```

### Docker Compose
```yaml
backend:
  environment:
    STORAGE_LOCAL_PATH: /app/images
    STORAGE_BASE_URL: http://localhost:8080/images
  volumes:
    - images_data:/app/images

volumes:
  images_data:
    driver: local
```

## API Endpoints

### Upload Ảnh

#### 1. Upload Ảnh Chính Sản Phẩm (Main Image)
```bash
POST /api/products/{id}/images/main
Content-Type: multipart/form-data

FormData:
  - file: <image_file>

Response: "http://localhost:8080/images/products/uuid_filename.jpg"
```

#### 2. Upload Ảnh Bổ Sung Sản Phẩm
```bash
POST /api/products/{id}/images
Content-Type: multipart/form-data

FormData:
  - file: <image_file>

Response: "http://localhost:8080/images/products/uuid_filename.jpg"
```

### Xóa Ảnh

#### 1. Xóa Ảnh Chính
```bash
DELETE /api/products/{id}/images/main
```

#### 2. Xóa Ảnh Bổ Sung
```bash
DELETE /api/products/{productId}/images/{imageId}
```

#### 3. Xóa Tất Cả Ảnh Bổ Sung
```bash
DELETE /api/products/{id}/images
```

### Tạo/Cập Nhật Sản Phẩm với URL

Bạn có thể gửi URL ảnh trong payload, backend sẽ tự động download và lưu:

```json
POST /api/products
{
  "productCode": "SP001",
  "name": "Hoa hồng đỏ",
  "description": "Hoa hồng tươi",
  "price": 100000,
  "imageUrl": "https://example.com/image.jpg",
  "imageUrls": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "categoryId": 1
}
```

## Cách Sử Dụng Từ Frontend

### 1. Upload File từ Form

```javascript
import { uploadMainProductImage, uploadProductImage } from '@/config/apiUtils'

// Upload ảnh chính
async function uploadMain(productId, file) {
  try {
    const imageUrl = await uploadMainProductImage(productId, file)
    console.log('Uploaded:', imageUrl)
    // imageUrl = "http://localhost:8080/images/products/uuid_abc.jpg"
  } catch (error) {
    console.error('Upload failed:', error)
  }
}

// Upload ảnh bổ sung
async function uploadAdditional(productId, file) {
  try {
    const imageUrl = await uploadProductImage(productId, file)
    console.log('Uploaded:', imageUrl)
  } catch (error) {
    console.error('Upload failed:', error)
  }
}
```

### 2. Hiển Thị Ảnh

```vue
<template>
  <div>
    <!-- Ảnh chính -->
    <img 
      v-if="product.imageUrl" 
      :src="product.imageUrl" 
      alt="Product image"
    />
    
    <!-- Ảnh bổ sung -->
    <div v-for="image in product.images" :key="image.id">
      <img :src="image.imageUrl" :alt="image.fileName" />
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      product: {
        id: 1,
        name: "Hoa hồng",
        imageUrl: "http://localhost:8080/images/products/uuid_abc.jpg",
        images: [
          {
            id: 1,
            imageUrl: "http://localhost:8080/images/products/uuid_def.jpg",
            fileName: "rose1.jpg"
          }
        ]
      }
    }
  }
}
</script>
```

### 3. Xóa Ảnh

```javascript
import { deleteMainProductImage, deleteProductImage } from '@/config/apiUtils'

// Xóa ảnh chính
async function deleteMain(productId) {
  try {
    await deleteMainProductImage(productId)
    console.log('Deleted main image')
  } catch (error) {
    console.error('Delete failed:', error)
  }
}

// Xóa ảnh bổ sung
async function deleteImage(productId, imageId) {
  try {
    await deleteProductImage(productId, imageId)
    console.log('Deleted image')
  } catch (error) {
    console.error('Delete failed:', error)
  }
}
```

## Response Format

### ProductResponse
```json
{
  "id": 1,
  "productCode": "SP001",
  "name": "Hoa hồng đỏ",
  "description": "Hoa hồng tươi",
  "price": 100000,
  "imageUrl": "http://localhost:8080/images/products/uuid_abc.jpg",
  "imageUrls": [
    "http://localhost:8080/images/products/uuid_def.jpg",
    "http://localhost:8080/images/products/uuid_ghi.jpg"
  ],
  "images": [
    {
      "id": 1,
      "imageUrl": "http://localhost:8080/images/products/uuid_def.jpg",
      "fileName": "rose1.jpg",
      "displayOrder": 0
    },
    {
      "id": 2,
      "imageUrl": "http://localhost:8080/images/products/uuid_ghi.jpg",
      "fileName": "rose2.jpg",
      "displayOrder": 1
    }
  ],
  "categoryId": 1,
  "categoryName": "Hoa tươi"
}
```

## Xử Lý Lỗi

### Lỗi Thường Gặp

1. **File không tồn tại khi xóa**
   - Log: "Failed to delete old image file: ..."
   - Không làm fail operation, chỉ log warning

2. **URL không hợp lệ**
   - Log: "Image URL does not match base URL: ..."
   - Kiểm tra lại cấu hình `STORAGE_BASE_URL`

3. **Không đủ quyền tạo thư mục**
   - Đảm bảo Docker container có quyền write vào `/app/images`
   - Check: `chmod -R 755 /app/images`

## Migration

Migration đã được apply tự động:
- `008_convert_to_file_based_storage.sql`

Nếu cần chạy lại:
```bash
docker exec -it flowershop-db-prod psql -U postgres -d flowershop \
  -f /docker-entrypoint-initdb.d/008_convert_to_file_based_storage.sql
```

## Backup & Restore

### Backup Ảnh
```bash
# Backup volume
docker run --rm \
  -v flower_shop_images_data:/data \
  -v $(pwd)/backup:/backup \
  alpine tar czf /backup/images-$(date +%Y%m%d).tar.gz /data

# Hoặc copy trực tiếp
docker cp flowershop-backend-prod:/app/images ./backup/images
```

### Restore Ảnh
```bash
# Restore từ tar
docker run --rm \
  -v flower_shop_images_data:/data \
  -v $(pwd)/backup:/backup \
  alpine tar xzf /backup/images-20231113.tar.gz -C /

# Hoặc copy trực tiếp
docker cp ./backup/images flowershop-backend-prod:/app/
```

## Production Considerations

1. **CDN**: Nên sử dụng CDN để phục vụ ảnh
2. **Nginx**: Cấu hình Nginx để serve static files
3. **S3/Cloud Storage**: Có thể mở rộng để lưu trên AWS S3 hoặc Azure Blob
4. **Image Optimization**: Nên resize/compress ảnh trước khi lưu
5. **Security**: Validate file type và size trước khi upload

## Troubleshooting

### Check Storage Directory
```bash
docker exec -it flowershop-backend-prod ls -la /app/images/products
```

### Check Logs
```bash
docker compose logs backend --tail 50
```

### Test Upload
```bash
curl -X POST http://localhost:8080/api/products/1/images/main \
  -H "Content-Type: multipart/form-data" \
  -F "file=@/path/to/image.jpg"
```

### Verify Image Access
```bash
curl -I http://localhost:8080/images/products/uuid_abc.jpg
```
