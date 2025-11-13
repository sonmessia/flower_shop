# Blog Image Upload - Hướng dẫn sử dụng

## Tổng quan
Tính năng upload hình ảnh cho Blog hỗ trợ:
- ✅ Tải nhiều hình ảnh cùng lúc
- ✅ Hỗ trợ cả upload file từ máy tính
- ✅ Hỗ trợ upload từ URL
- ✅ Quản lý hình ảnh đại diện (main image)
- ✅ Quản lý hình ảnh bổ sung (additional images)

## Cấu trúc Database

### Bảng `blogs`
- `imageUrl`: Hình ảnh đại diện/featured image (chỉ 1 ảnh)

### Bảng `blog_images` (mới)
- `id`: Primary key
- `blog_id`: Foreign key tới bảng blogs
- `image_url`: URL của hình ảnh
- `file_name`: Tên file gốc
- `file_path`: Đường dẫn lưu trữ
- `display_order`: Thứ tự hiển thị

## API Endpoints

### Upload Hình ảnh đại diện

**1. Upload từ file**
```
POST /api/admin/blogs/{blogId}/images/main
Content-Type: multipart/form-data

Body: file=<image-file>
```

**2. Upload từ URL**
```
POST /api/admin/blogs/{blogId}/images/main-url
Content-Type: application/json

{
  "imageUrl": "https://example.com/image.jpg"
}
```

### Upload Hình ảnh bổ sung

**3. Upload từ file**
```
POST /api/admin/blogs/{blogId}/images
Content-Type: multipart/form-data

Body: file=<image-file>
```

**4. Upload từ URL**
```
POST /api/admin/blogs/{blogId}/images/url
Content-Type: application/json

{
  "imageUrl": "https://example.com/image.jpg"
}
```

### Xóa Hình ảnh

**5. Xóa hình ảnh đại diện**
```
DELETE /api/admin/blogs/{blogId}/images/main
```

**6. Xóa một hình ảnh bổ sung**
```
DELETE /api/admin/blogs/{blogId}/images/{imageId}
```

**7. Xóa tất cả hình ảnh bổ sung**
```
DELETE /api/admin/blogs/{blogId}/images
```

## Frontend Implementation

Component `AdminBlogManagement.vue` đã được tích hợp với `ImageUploader` component để:

1. **Upload hình ảnh đại diện**: Chỉ cho phép 1 ảnh
2. **Upload hình ảnh bổ sung**: Cho phép nhiều ảnh
3. **Hỗ trợ cả File và URL**: Người dùng có thể chọn upload từ file hoặc nhập URL

### Cách sử dụng trong giao diện

1. **Tạo/Sửa bài viết**: Mở modal tạo hoặc sửa blog
2. **Thêm hình ảnh đại diện**: 
   - Click vào "🖼️ Hình ảnh đại diện"
   - Chọn "File" hoặc "URL"
   - Upload ảnh
3. **Thêm hình ảnh bổ sung**:
   - Click vào "📸 Hình ảnh bổ sung"
   - Chọn "File" hoặc "URL"
   - Upload nhiều ảnh
4. **Xóa hình ảnh**: Click nút ❌ trên mỗi ảnh
5. **Lưu bài viết**: Tất cả hình ảnh sẽ được upload sau khi bài viết được tạo/cập nhật

## Migration

Chạy migration để tạo bảng `blog_images`:

```bash
psql -U your_username -d your_database -f database/migrations/004_add_blog_images_table.sql
```

Hoặc nếu sử dụng Docker:

```bash
docker-compose exec database psql -U postgres -d flowershop -f /docker-entrypoint-initdb.d/migrations/004_add_blog_images_table.sql
```

## Response Format

Khi lấy thông tin blog, response sẽ bao gồm:

```json
{
  "id": 1,
  "title": "Blog Title",
  "content": "Blog content...",
  "imageUrl": "https://example.com/main-image.jpg",
  "summary": "Blog summary",
  "status": "PUBLISHED",
  "authorId": 1,
  "authorUsername": "admin",
  "createdAt": "2025-01-13T10:00:00",
  "updatedAt": "2025-01-13T11:00:00",
  "images": [
    {
      "id": 1,
      "imageUrl": "https://example.com/image1.jpg",
      "fileName": "image1.jpg",
      "displayOrder": 0
    },
    {
      "id": 2,
      "imageUrl": "https://example.com/image2.jpg",
      "fileName": "image2.jpg",
      "displayOrder": 1
    }
  ]
}
```

## Lưu ý

1. **Thứ tự thực hiện**: Hình ảnh chỉ được upload SAU KHI blog được tạo/cập nhật
2. **Xử lý lỗi**: Nếu upload hình ảnh thất bại, blog vẫn được lưu nhưng không có ảnh
3. **Giới hạn file**: Kiểm tra cấu hình Spring Boot cho kích thước file tối đa
4. **Storage**: Hình ảnh được lưu thông qua `ImageStorageService` (local hoặc cloud)

## Testing

Để test tính năng:

1. Tạo một blog mới
2. Upload hình ảnh đại diện (file hoặc URL)
3. Upload 2-3 hình ảnh bổ sung
4. Kiểm tra blog details API để xem danh sách ảnh
5. Xóa một vài ảnh
6. Cập nhật blog và thêm ảnh mới

## Troubleshooting

**Lỗi: "Failed to upload image"**
- Kiểm tra kích thước file
- Kiểm tra định dạng file (jpg, png, gif)
- Kiểm tra quyền ghi vào thư mục storage

**Lỗi: "Image does not belong to this blog"**
- Đảm bảo imageId đúng với blogId

**Lỗi: Cannot find ImageUploader component**
- Kiểm tra import ImageUploader trong AdminBlogManagement.vue
