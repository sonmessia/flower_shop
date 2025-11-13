# 🐛 Sửa Lỗi: URL Hình Ảnh Không Được Gửi Đến Backend

## Mô Tả Lỗi

Khi người dùng chọn tải hình ảnh từ URL trong frontend, backend không nhận được dữ liệu URL và không có log nào hiển thị việc tải xuống hình ảnh.

## Nguyên Nhân

**Không khớp tên thuộc tính** giữa `ImageUploader.vue` và `AdminDashboard.vue`:

### ImageUploader.vue (Component con)
Khi user nhập URL, component tạo object với thuộc tính **`url`**:
```javascript
const newImage = {
  url: imageUrl.value,      // ✅ Thuộc tính là "url"
  name: imageUrl.value,
  source: 'url'
};
```

### AdminDashboard.vue (Component cha)
Nhưng code kiểm tra thuộc tính **`imageUrl`**:
```javascript
// ❌ SAI - Tìm "imageUrl" trong khi object có "url"
if (mainImage.source === "url" && mainImage.imageUrl) {
  imageUrlForPayload = mainImage.imageUrl;
}
```

➡️ **Kết quả**: Điều kiện luôn `false` → `imageUrlForPayload` luôn `null` → Backend không nhận được URL

## Giải Pháp

Sửa `AdminDashboard.vue` để sử dụng đúng tên thuộc tính `url`:

### 1. Sửa Logic Tạo Payload (Dòng ~1210, ~1224)
```javascript
// ✅ ĐÚNG - Sử dụng "url"
if (mainImage.source === "url" && mainImage.url) {
  imageUrlForPayload = mainImage.url;
}
```

### 2. Sửa Upload Additional Images (Dòng ~1285, ~1306)
```javascript
// ✅ Log đúng
} else if (mainImage.source === "url" && mainImage.url) {
  console.log("✅ Main image URL saved in payload:", mainImage.url);
}

// ✅ Upload additional image
} else if (image.source === "url" && image.url) {
  await api.post(`/products/${productId}/images/url`, {
    imageUrl: image.url,  // Gửi đúng giá trị
  });
```

## Files Đã Sửa

- ✅ `/frontend/src/components/AdminDashboard.vue` (3 vị trí)

## Kiểm Tra

### Trước khi sửa:
```javascript
console.log(mainImage);
// { url: "https://example.com/image.jpg", source: "url", name: "..." }

console.log(mainImage.imageUrl);  // ❌ undefined
console.log(imageUrlForPayload);  // ❌ null
```

### Sau khi sửa:
```javascript
console.log(mainImage.url);       // ✅ "https://example.com/image.jpg"
console.log(imageUrlForPayload);  // ✅ "https://example.com/image.jpg"
```

## Kiểm Tra Backend

Sau khi sửa, backend logs sẽ hiển thị:
```
=== CREATE PRODUCT REQUEST ===
Product Code: PROD123
ImageUrl: https://example.com/image.jpg  ← ✅ Bây giờ có giá trị
===============================
Attempting to download main image from URL: https://example.com/image.jpg
Downloaded image from URL: https://example.com/image.jpg
Successfully saved file from URL
```

## Ghi Chú

⚠️ **Lưu ý về Naming Convention**:
- `ImageUploader.vue` sử dụng `url` (object property)
- `AdminDashboard.vue` sử dụng `imageUrl` cho payload gửi đến backend (API field name)
- Đây là 2 layer khác nhau, cần mapping đúng giữa chúng

---
**Ngày sửa**: 2025-11-13  
**Trạng thái**: ✅ Đã sửa và rebuild thành công
