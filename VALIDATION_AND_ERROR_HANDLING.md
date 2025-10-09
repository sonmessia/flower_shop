# Validation và Error Handling - Hệ Thống Flower Shop

## 📋 Tổng quan
Tài liệu này mô tả các validation rules và error handling được implement trong hệ thống Flower Shop.

---

## ✅ Các Tính Năng Đã Hoàn Thành

### 1. 🔐 **Tài Khoản Admin Mặc Định**

**File:** `/flower-shop/src/main/java/vn/quahoa/flowershop/config/DataInitializer.java`

**Chức năng:**
- Tự động tạo tài khoản admin mặc định khi khởi động ứng dụng lần đầu
- Chỉ tạo nếu chưa có tài khoản admin nào trong database

**Thông tin đăng nhập mặc định:**
```
Username: admin
Password: admin123
```

**⚠️ Khuyến nghị:** Đổi mật khẩu ngay sau khi đăng nhập lần đầu!

---

### 2. 🛡️ **Validation Rules - Backend**

#### **A. Product (Sản phẩm)**

**File:** `/flower-shop/src/main/java/vn/quahoa/flowershop/service/ProductService.java`

**Validation Rules:**

1. **Mã sản phẩm (Product Code)**
   ```java
   - Không được trùng (case-insensitive)
   - Exception: ValidationException("productCode", "Product code already exists")
   - Message: "❌ Mã sản phẩm '{code}' đã tồn tại. Vui lòng chọn mã khác."
   ```

2. **Tên sản phẩm (Product Name)**
   ```java
   - Không được trùng (case-insensitive)
   - Exception: ValidationException("name", "Product name already exists")
   - Message: "❌ Tên sản phẩm '{name}' đã tồn tại. Vui lòng đổi tên khác."
   ```

3. **Danh mục (Category)**
   ```java
   - Phải tồn tại trong database
   - Exception: ResourceNotFoundException("Category", categoryId)
   - Message: "❌ Danh mục không tồn tại. Vui lòng chọn danh mục khác."
   ```

4. **Giá (Price)**
   ```java
   - Phải > 0
   - Message: "❌ Giá sản phẩm phải lớn hơn 0."
   ```

5. **Mô tả (Description)**
   ```java
   - Tối đa 2000 ký tự
   - Message: "❌ Mô tả sản phẩm quá dài (tối đa 2000 ký tự)."
   ```

#### **B. Category (Danh mục)**

**File:** `/flower-shop/src/main/java/vn/quahoa/flowershop/service/CategoryService.java`

**Validation Rules:**

1. **Tên danh mục (Category Name)**
   ```java
   - Không được trùng (case-insensitive)
   - Exception: ValidationException("name", "Category name already exists")
   - Message: "❌ Tên danh mục '{name}' đã tồn tại. Vui lòng đổi tên khác."
   ```

2. **Xóa danh mục**
   ```java
   - Không được xóa nếu còn sản phẩm
   - Message: "❌ Không thể xóa danh mục '{name}' vì còn X sản phẩm."
   ```

#### **C. Admin Account**

**Validation Rules:**

1. **Username**
   ```java
   - Không được trùng
   - Tối thiểu 3 ký tự
   - Message: "❌ Tên đăng nhập đã tồn tại."
   ```

2. **Password**
   ```java
   - Tối thiểu 6 ký tự
   - Message: "❌ Mật khẩu phải có ít nhất 6 ký tự."
   ```

3. **Xóa tài khoản**
   ```java
   - Không được xóa tài khoản đang đăng nhập
   - Không được xóa tài khoản admin cuối cùng
   - Message: "❌ Không thể xóa tài khoản đang đăng nhập."
   ```

---

### 3. 🎯 **Error Handling - Frontend**

**File:** `/frontend/src/components/AdminDashboard.vue`

**Function:** `getErrorMessage(error, context)`

#### **HTTP Status Codes Handling:**

| Status | Loại lỗi | Xử lý |
|--------|-----------|-------|
| **400** | Bad Request | Parse validation errors chi tiết |
| **401** | Unauthorized | Phiên đăng nhập hết hạn |
| **403** | Forbidden | Không có quyền |
| **404** | Not Found | Không tìm thấy resource |
| **409** | Conflict | Dữ liệu trùng lặp |
| **500** | Server Error | Lỗi server |
| **No Response** | Network Error | Không kết nối được |

#### **Detailed Error Messages:**

**400 - Bad Request:**
```javascript
// Product Code
"❌ Mã sản phẩm '{code}' đã tồn tại. Vui lòng chọn mã khác."
"❌ Mã sản phẩm không hợp lệ. Vui lòng kiểm tra lại."

// Name
"❌ Tên {context} không được để trống."
"❌ Tên {context} quá dài. Vui lòng rút ngắn lại."
"❌ Tên {context} '{name}' đã tồn tại. Vui lòng đổi tên khác."

// Price
"❌ Giá sản phẩm phải lớn hơn 0."
"❌ Giá sản phẩm không hợp lệ. Vui lòng nhập số dương."

// Description
"❌ Mô tả sản phẩm quá dài (tối đa 2000 ký tự)."

// Category
"❌ Danh mục không tồn tại. Vui lòng chọn danh mục khác."
"❌ Không thể xóa danh mục vì còn sản phẩm."

// Image URL
"❌ URL hình ảnh không hợp lệ. Vui lòng nhập URL đúng định dạng."
```

**404 - Not Found:**
```javascript
"❌ Không tìm thấy {context}. Có thể đã bị xóa trước đó."
"❌ Không tìm thấy {context}: {message}"
```

**409 - Conflict:**
```javascript
"❌ Mã sản phẩm '{code}' đã tồn tại. Vui lòng chọn mã khác."
"❌ Tên {context} '{name}' đã tồn tại. Vui lòng đổi tên khác."
"❌ Không thể xóa vì {context} đang được sử dụng."
```

**Network Error:**
```javascript
"❌ Không thể kết nối với server. Vui lòng kiểm tra kết nối mạng hoặc server có đang chạy không."
```

---

## 🎨 Toast Notifications

**Hiển thị:**
- ✅ Success (màu xanh) - Thao tác thành công
- ❌ Error (màu đỏ) - Có lỗi xảy ra
- ⚠️ Warning (màu cam) - Cảnh báo

**Tính năng:**
- Auto hide sau 5 giây
- Có thể đóng thủ công
- Hiển thị nhiều toast cùng lúc
- Positioned: Top-right corner

---

## 📊 Validation Flow

### **Tạo Sản Phẩm Mới:**

```
1. Frontend Validation
   ├─ Kiểm tra tên không trống
   ├─ Kiểm tra mã sản phẩm không trống
   ├─ Kiểm tra giá > 0
   ├─ Kiểm tra mô tả <= 2000 ký tự
   └─ Kiểm tra đã chọn danh mục

2. Send Request to Backend

3. Backend Validation
   ├─ Validate Product Code unique
   ├─ Validate Product Name unique
   ├─ Validate Category exists
   └─ Validate all required fields

4. Response
   ├─ Success: Save to DB → Return 201
   └─ Error: Return appropriate error code

5. Frontend Handling
   ├─ Success: Show toast + Reload list
   └─ Error: Parse error + Show detailed toast
```

### **Cập Nhật Sản Phẩm:**

```
1. Frontend Validation (tương tự Create)

2. Backend Validation
   ├─ Check product exists
   ├─ Validate Product Code unique (exclude current)
   ├─ Validate Product Name unique (exclude current)
   └─ Validate Category exists

3. Response & Handling (tương tự Create)
```

### **Xóa Sản Phẩm:**

```
1. Confirmation Dialog
   ⚠️ Bạn có chắc muốn xoá sản phẩm "{name}"?
   Thao tác này không thể hoàn tác!

2. Send Delete Request

3. Backend Processing
   └─ Check if product exists

4. Response
   ├─ Success: Delete + Return 200
   ├─ 404: Product not found
   └─ 409: Product is being used

5. Frontend Handling
   ├─ Success: Show toast + Reload list
   └─ Error: Show appropriate error message
```

---

## 🔧 Repository Methods

### **ProductRepository:**

```java
// Tìm theo product code (case-insensitive)
Optional<Product> findByProductCodeIgnoreCase(String productCode);

// Tìm theo tên (case-insensitive)
Optional<Product> findByNameIgnoreCase(String name);

// Tìm theo category
List<Product> findByCategory_Id(Long categoryId);

// Tìm kiếm
List<Product> searchProducts(String keyword);
```

### **CategoryRepository:**

```java
// Tìm theo tên (case-insensitive)
Optional<Category> findByNameIgnoreCase(String name);
```

---

## 🧪 Test Cases

### **Product Validation:**

1. ✅ Tạo sản phẩm mới với mã hợp lệ → Success
2. ✅ Tạo sản phẩm với mã đã tồn tại → Error 400
3. ✅ Tạo sản phẩm với tên đã tồn tại → Error 400
4. ✅ Tạo sản phẩm với giá = 0 → Error 400
5. ✅ Cập nhật sản phẩm giữ nguyên mã → Success
6. ✅ Cập nhật sản phẩm đổi mã trùng khác → Error 400
7. ✅ Xóa sản phẩm tồn tại → Success
8. ✅ Xóa sản phẩm không tồn tại → Error 404

### **Category Validation:**

1. ✅ Tạo danh mục với tên hợp lệ → Success
2. ✅ Tạo danh mục với tên đã tồn tại → Error 400
3. ✅ Xóa danh mục không có sản phẩm → Success
4. ✅ Xóa danh mục còn sản phẩm → Error (frontend block)
5. ✅ Cập nhật tên danh mục trùng khác → Error 400

---

## 📝 Best Practices

### **Frontend:**
1. ✅ Validate trước khi gửi request
2. ✅ Parse error message từ backend
3. ✅ Hiển thị thông báo rõ ràng, dễ hiểu
4. ✅ Sử dụng toast notifications
5. ✅ Disable buttons khi đang xử lý

### **Backend:**
1. ✅ Throw appropriate exceptions
2. ✅ Return proper HTTP status codes
3. ✅ Include detailed error messages
4. ✅ Validate before save
5. ✅ Case-insensitive validation cho unique fields

---

## 🚀 Future Improvements

### **Có thể thêm:**

1. **Validation:**
   - ⭐ Email validation cho admin
   - ⭐ Phone number validation
   - ⭐ URL validation cho image URLs
   - ⭐ Price range validation
   - ⭐ Stock quantity validation

2. **Error Handling:**
   - ⭐ Error logging to file/database
   - ⭐ Error tracking service integration
   - ⭐ Custom error codes
   - ⭐ Multi-language error messages

3. **Security:**
   - ⭐ Password strength validation
   - ⭐ Rate limiting
   - ⭐ CSRF protection
   - ⭐ SQL injection prevention

4. **UX:**
   - ⭐ Inline validation
   - ⭐ Field-level error display
   - ⭐ Validation on blur
   - ⭐ Undo functionality

---

## 📚 Related Files

### **Backend:**
- `/flower-shop/src/main/java/vn/quahoa/flowershop/service/ProductService.java`
- `/flower-shop/src/main/java/vn/quahoa/flowershop/service/CategoryService.java`
- `/flower-shop/src/main/java/vn/quahoa/flowershop/repository/ProductRepository.java`
- `/flower-shop/src/main/java/vn/quahoa/flowershop/repository/CategoryRepository.java`
- `/flower-shop/src/main/java/vn/quahoa/flowershop/config/DataInitializer.java`
- `/flower-shop/src/main/java/vn/quahoa/flowershop/exception/ValidationException.java`

### **Frontend:**
- `/frontend/src/components/AdminDashboard.vue`

---

## 🔗 API Error Response Format

```json
{
  "timestamp": "2025-10-09T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Product code already exists",
  "path": "/api/products"
}
```

---

**Ngày cập nhật:** 09/10/2025  
**Version:** 3.0  
**Tác giả:** Development Team
