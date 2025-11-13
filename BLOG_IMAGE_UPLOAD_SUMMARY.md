# Tổng kết: Thêm chức năng upload nhiều hình ảnh cho Blog

## Các file đã tạo mới

### Backend
1. **`BlogImage.java`** - Model cho hình ảnh blog
   - Đường dẫn: `/flower-shop/src/main/java/vn/quahoa/flowershop/model/BlogImage.java`
   
2. **`BlogImageRepository.java`** - Repository cho BlogImage
   - Đường dẫn: `/flower-shop/src/main/java/vn/quahoa/flowershop/repository/BlogImageRepository.java`

3. **Migration SQL** - Tạo bảng blog_images
   - Đường dẫn: `/database/migrations/004_add_blog_images_table.sql`

### Documentation
4. **`BLOG_IMAGE_UPLOAD_GUIDE.md`** - Hướng dẫn sử dụng tính năng
   - Đường dẫn: `/BLOG_IMAGE_UPLOAD_GUIDE.md`

## Các file đã cập nhật

### Backend

1. **`Blog.java`**
   - Thêm quan hệ OneToMany với BlogImage
   - Thêm field `images` để lưu danh sách hình ảnh

2. **`BlogResponse.java`**
   - Thêm nested class `BlogImageResponse`
   - Thêm field `images` trong response

3. **`BlogService.java`**
   - Thêm dependency: `BlogImageRepository`, `ImageStorageService`
   - Thêm 8 phương thức mới:
     - `uploadMainBlogImage()` - Upload ảnh đại diện từ file
     - `uploadMainBlogImageFromUrl()` - Upload ảnh đại diện từ URL
     - `uploadBlogImage()` - Upload ảnh bổ sung từ file
     - `uploadBlogImageFromUrl()` - Upload ảnh bổ sung từ URL
     - `deleteMainBlogImage()` - Xóa ảnh đại diện
     - `deleteBlogImage()` - Xóa một ảnh bổ sung
     - `deleteAllBlogImages()` - Xóa tất cả ảnh bổ sung

4. **`BlogController.java`**
   - Thêm imports: `ResponseEntity`, `MultipartFile`, `ImageUrlRequest`
   - Thêm 8 endpoints mới cho upload/delete hình ảnh

### Frontend

5. **`AdminBlogManagement.vue`**
   - Import component `ImageUploader`
   - Thêm refs: `mainImageUploader`, `additionalImagesUploader`
   - Thêm reactive states:
     - `existingMainImage`
     - `newMainImages`
     - `existingAdditionalImages`
     - `newAdditionalImages`
   - Thêm 4 handler functions:
     - `handleMainImageUpdate()`
     - `handleAdditionalImagesUpdate()`
     - `handleDeleteMainImage()`
     - `handleDeleteAdditionalImage()`
   - Cập nhật `openCreateModal()` - Reset image states
   - Cập nhật `openEditModal()` - Load existing images
   - Cập nhật `closeModal()` - Clear image states
   - Cập nhật `saveBlog()` - Upload images sau khi tạo/cập nhật blog
   - Thay thế form input cũ bằng 2 `ImageUploader` components

6. **`api.js`**
   - Thêm 6 endpoints mới trong object `blogs`:
     - `uploadMainImage()`
     - `uploadMainImageUrl()`
     - `uploadImage()`
     - `uploadImageUrl()`
     - `deleteMainImage()`
     - `deleteImage()`

## Tính năng mới

✅ **Upload từ file** - Người dùng có thể chọn file từ máy tính
✅ **Upload từ URL** - Người dùng có thể nhập URL hình ảnh
✅ **Hình ảnh đại diện** - Một ảnh chính cho blog (imageUrl)
✅ **Hình ảnh bổ sung** - Nhiều ảnh phụ (images array)
✅ **Preview ảnh** - Xem trước ảnh trước khi upload
✅ **Xóa ảnh** - Xóa từng ảnh hoặc tất cả
✅ **Drag & Drop** - Kéo thả file vào để upload (từ ImageUploader)

## Cách sử dụng

### Bước 1: Chạy Migration
```bash
# Nếu dùng Docker
docker-compose exec database psql -U postgres -d flowershop -f /docker-entrypoint-initdb.d/migrations/004_add_blog_images_table.sql

# Hoặc chạy trực tiếp
psql -U postgres -d flowershop -f database/migrations/004_add_blog_images_table.sql
```

### Bước 2: Restart Backend
```bash
cd flower-shop
./mvnw spring-boot:run
```

### Bước 3: Sử dụng trong giao diện
1. Vào trang Admin Dashboard
2. Click vào tab "Blog"
3. Tạo hoặc sửa bài viết
4. Thêm hình ảnh đại diện và hình ảnh bổ sung
5. Lưu bài viết

## Flow hoạt động

```
1. User tạo/sửa blog → Modal mở
2. User chọn ảnh đại diện (1 ảnh)
3. User chọn ảnh bổ sung (nhiều ảnh)
4. User click "Lưu"
5. → Backend tạo/cập nhật blog
6. → Frontend upload ảnh đại diện (file hoặc URL)
7. → Frontend upload từng ảnh bổ sung (file hoặc URL)
8. → Backend lưu vào database
9. → Frontend hiển thị thông báo thành công
10. → Modal đóng, danh sách blog refresh
```

## API Endpoints tóm tắt

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/admin/blogs/{id}/images/main` | Upload ảnh đại diện (file) |
| POST | `/api/admin/blogs/{id}/images/main-url` | Upload ảnh đại diện (URL) |
| POST | `/api/admin/blogs/{id}/images` | Upload ảnh bổ sung (file) |
| POST | `/api/admin/blogs/{id}/images/url` | Upload ảnh bổ sung (URL) |
| DELETE | `/api/admin/blogs/{id}/images/main` | Xóa ảnh đại diện |
| DELETE | `/api/admin/blogs/{id}/images/{imageId}` | Xóa ảnh bổ sung |
| DELETE | `/api/admin/blogs/{id}/images` | Xóa tất cả ảnh bổ sung |

## Thống kê thay đổi

- **Tệp mới**: 4 files
- **Tệp cập nhật**: 6 files
- **Dòng code thêm mới**: ~600+ lines
- **API endpoints mới**: 8 endpoints
- **Database tables mới**: 1 table (blog_images)

## Testing checklist

- [ ] Tạo blog mới với ảnh đại diện (file)
- [ ] Tạo blog mới với ảnh đại diện (URL)
- [ ] Thêm nhiều ảnh bổ sung (file)
- [ ] Thêm nhiều ảnh bổ sung (URL)
- [ ] Kết hợp cả file và URL
- [ ] Xóa ảnh đại diện
- [ ] Xóa từng ảnh bổ sung
- [ ] Cập nhật blog và thêm ảnh mới
- [ ] Cập nhật blog và xóa ảnh cũ
- [ ] Kiểm tra response API có chứa danh sách images

## Lưu ý quan trọng

⚠️ **Chạy migration trước khi test**
⚠️ **Đảm bảo ImageStorageService hoạt động đúng**
⚠️ **Kiểm tra cấu hình max file size trong application.properties**

```properties
# application.properties
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```
