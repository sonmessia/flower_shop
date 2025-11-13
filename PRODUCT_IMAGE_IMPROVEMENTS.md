# Cải tiến tính năng Upload hình ảnh sản phẩm

## Các vấn đề đã được khắc phục

### 1. ✅ Bỏ giới hạn dung lượng file

**Trước đây:**
- Giới hạn 5MB cho mỗi file
- Thông báo lỗi khi upload file > 5MB

**Sau khi sửa:**
- Không giới hạn dung lượng file ở frontend (ImageUploader.vue)
- Tăng giới hạn backend lên 50MB/file và 100MB/request
- Có thể điều chỉnh lại nếu cần trong `application.properties` và `application-prod.properties`

**Files thay đổi:**
- `frontend/src/components/ImageUploader.vue` - Xóa validation file size
- `flower-shop/src/main/resources/application.properties` - Tăng max-file-size lên 50MB
- `flower-shop/src/main/resources/application-prod.properties` - Tăng max-file-size lên 50MB

---

### 2. ✅ Giữ nguyên hình ảnh cũ khi chỉnh sửa mà không thay đổi

**Vấn đề trước đây:**
- Khi edit sản phẩm, nếu không chọn ảnh mới, ảnh cũ vẫn bị xóa/mất
- Backend xóa tất cả ảnh cũ khi có imageUrls trong request

**Sau khi sửa:**
- Chỉ upload ảnh MỚI khi user chọn ảnh mới
- Nếu không chọn ảnh mới, giữ nguyên ảnh cũ
- Backend không xóa ảnh cũ khi update sản phẩm

**Logic mới:**
```javascript
// Frontend - AdminDashboard.vue
// 1. Chỉ upload main image nếu có new image
if (newMainImages.value.length > 0) {
  // Upload main image...
}

// 2. Chỉ upload additional images nếu có new images
if (newAdditionalImages.value.length > 0) {
  // Upload additional images...
}

// 3. Existing images vẫn được giữ nguyên trong database
```

```java
// Backend - ProductService.java
public Product updateProduct(Long id, ProductUpdateRequest request) {
    // ...
    // Only update imageUrl if provided and not null
    if (request.getImageUrl() != null) {
        product.setImageUrl(request.getImageUrl());
    }
    
    // Note: imageUrls update is handled separately via upload endpoints
    // We don't clear existing images here to preserve them
    
    return productRepository.save(product);
}
```

**Files thay đổi:**
- `frontend/src/components/AdminDashboard.vue` - Sửa logic submitProduct
- `flower-shop/src/main/java/vn/quahoa/flowershop/service/ProductService.java` - Sửa logic updateProduct

---

### 3. ✅ Đảm bảo lưu đầy đủ hình ảnh vào database

**Cải tiến:**
- Reload product list sau khi upload để hiển thị ảnh mới
- Thêm error handling cho từng ảnh upload
- Hiển thị thông báo chi tiết về số ảnh đã upload thành công

**Flow hoạt động:**
```
1. User tạo/sửa sản phẩm
2. Backend lưu thông tin sản phẩm (không có ảnh)
3. Frontend upload từng ảnh qua API riêng:
   - POST /products/{id}/images/main (ảnh đại diện)
   - POST /products/{id}/images (ảnh bổ sung)
4. Backend lưu từng ảnh vào table product_images
5. Frontend reload danh sách sản phẩm
6. Hiển thị sản phẩm với đầy đủ ảnh
```

**Thông báo cải tiến:**
```javascript
if (totalUploads > 0) {
  if (successUploads === totalUploads) {
    showToast("success", `✅ Đã tải lên ${successUploads} hình ảnh thành công!`);
  } else {
    showToast("warning", `⚠️ Đã tải lên ${successUploads}/${totalUploads} hình ảnh`);
  }
}
```

---

## Tóm tắt thay đổi

### Frontend (Vue.js)
1. **ImageUploader.vue**
   - Xóa validation file size 5MB
   - Cập nhật text hiển thị

2. **AdminDashboard.vue**
   - Sửa logic submitProduct để chỉ upload ảnh mới
   - Giữ nguyên imageUrl cũ khi không có thay đổi
   - Cải thiện thông báo upload
   - Reload products sau khi upload xong

### Backend (Spring Boot)
1. **ProductService.java**
   - Xóa logic `product.getImages().clear()` trong updateProduct
   - Chỉ update imageUrl khi có giá trị mới
   - Giữ nguyên images hiện có

2. **application.properties & application-prod.properties**
   - Tăng max-file-size: 5MB → 50MB
   - Tăng max-request-size: 10MB/20MB → 100MB

---

## Testing checklist

- [x] Upload ảnh sản phẩm có dung lượng > 5MB
- [x] Tạo sản phẩm mới với nhiều ảnh
- [x] Chỉnh sửa sản phẩm KHÔNG thay đổi ảnh → Ảnh cũ vẫn giữ nguyên
- [x] Chỉnh sửa sản phẩm và THÊM ảnh mới → Ảnh cũ + ảnh mới đều hiển thị
- [x] Xóa một ảnh bổ sung → Chỉ ảnh đó bị xóa, ảnh khác giữ nguyên
- [x] Xóa ảnh đại diện → Chỉ ảnh đại diện bị xóa

---

## Lưu ý khi deploy

1. **Rebuild Docker images:**
```bash
cd /home/hoangsonsdk/Projects/flower_shop
docker compose up -d --build
```

2. **Kiểm tra logs nếu có lỗi:**
```bash
docker compose logs backend
docker compose logs frontend
```

3. **Test upload ảnh lớn** để đảm bảo không bị timeout

4. **Nếu cần điều chỉnh giới hạn**, sửa trong:
   - `application.properties` (dev)
   - `application-prod.properties` (production)
   - Restart backend sau khi sửa
