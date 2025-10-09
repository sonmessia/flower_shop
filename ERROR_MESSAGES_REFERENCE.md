# 🔍 Quick Reference - Error Messages

## 📋 Sản phẩm (Products)

### Validation Errors (Client-side)
```
❌ Vui lòng nhập tên sản phẩm.
❌ Vui lòng nhập mã sản phẩm.
❌ Vui lòng chọn danh mục cho sản phẩm.
❌ Giá sản phẩm phải lớn hơn 0.
❌ Mô tả sản phẩm không được vượt quá 2000 ký tự.
```

### Success Messages
```
✅ Đã thêm sản phẩm "Hoa hồng pastel" thành công!
✅ Đã cập nhật sản phẩm "Hoa hồng pastel" thành công!
✅ Đã xoá sản phẩm "Hoa hồng pastel" thành công!
```

### Server Errors
```
❌ Mã sản phẩm "FL001" đã tồn tại. Vui lòng chọn mã khác.
❌ Mã sản phẩm không hợp lệ hoặc đã tồn tại. Vui lòng nhập mã khác.
❌ Tên sản phẩm không được để trống hoặc quá dài.
❌ Giá sản phẩm phải là số dương và hợp lệ.
❌ Danh mục không tồn tại. Vui lòng chọn danh mục khác.
❌ Không tìm thấy sản phẩm. Có thể đã bị xóa trước đó.
❌ Không thể xóa sản phẩm "Hoa hồng" vì đang được sử dụng trong đơn hàng.
```

---

## 📂 Danh mục (Categories)

### Validation Errors (Client-side)
```
❌ Vui lòng nhập tên danh mục.
❌ Tên danh mục không được vượt quá 100 ký tự.
```

### Success Messages
```
✅ Đã thêm danh mục "Hoa cưới" thành công!
✅ Đã cập nhật danh mục "Hoa cưới" thành công!
✅ Đã xoá danh mục "Hoa cưới" thành công!
```

### Dependency Check
```
❌ Không thể xóa danh mục "Hoa cưới" vì còn 5 sản phẩm. 
   Vui lòng xóa hoặc chuyển sản phẩm sang danh mục khác trước.
```

### Server Errors
```
❌ Tên danh mục không được để trống hoặc quá dài.
❌ Danh mục "Hoa cưới" đã tồn tại. Vui lòng đổi tên khác.
❌ Không tìm thấy danh mục. Có thể đã bị xóa trước đó.
❌ Không thể xóa danh mục "Hoa cưới" vì còn sản phẩm phụ thuộc.
```

---

## 🖼️ Hình ảnh (Images)

### Validation Errors
```
❌ Vui lòng nhập URL hình ảnh.
❌ URL không hợp lệ. Vui lòng nhập URL đúng định dạng (https://...).
❌ URL hình ảnh này đã được thêm trước đó.
```

### Success Messages
```
✅ Đã thêm hình ảnh thành công! (3 ảnh)
✅ Đã xóa hình ảnh. Còn lại 2 ảnh.
```

---

## 🌐 Network & System

### Network Errors
```
❌ Không thể kết nối với server. Vui lòng kiểm tra kết nối mạng.
```

### HTTP Status Codes
```
400 Bad Request:
  ❌ Dữ liệu nhập vào không đúng định dạng. Vui lòng kiểm tra lại.
  ❌ Dữ liệu không hợp lệ: [chi tiết lỗi]

404 Not Found:
  ❌ Không tìm thấy [context]. Có thể đã bị xóa trước đó.

409 Conflict:
  ❌ [context] đã tồn tại. Vui lòng kiểm tra lại.

500 Server Error:
  ❌ Lỗi server. Vui lòng thử lại sau hoặc liên hệ quản trị viên.

403 Forbidden:
  ❌ Bạn không có quyền thực hiện thao tác này.
```

---

## 🔄 Refresh

### Success
```
✅ Đã làm mới dữ liệu thành công!
```

### Error
```
❌ Không thể làm mới dữ liệu. Vui lòng kiểm tra kết nối.
```

---

## ⚠️ Confirmations

### Delete Product
```
⚠️ Bạn có chắc muốn xoá sản phẩm "Hoa hồng pastel"?

Thao tác này không thể hoàn tác!
```

### Delete Category
```
⚠️ Bạn có chắc muốn xoá danh mục "Hoa cưới"?

Thao tác này không thể hoàn tác!
```

---

## 🎨 Message Format

### Success Pattern
```
✅ Đã [action] [type] "[name]" thành công!

Examples:
✅ Đã thêm sản phẩm "Hoa hồng pastel" thành công!
✅ Đã cập nhật danh mục "Hoa cưới" thành công!
✅ Đã xoá sản phẩm "Hoa lan" thành công!
```

### Error Pattern
```
❌ [Mô tả lỗi cụ thể]. [Hướng dẫn khắc phục (nếu có)].

Examples:
❌ Mã sản phẩm "FL001" đã tồn tại. Vui lòng chọn mã khác.
❌ Giá sản phẩm phải lớn hơn 0.
❌ URL không hợp lệ. Vui lòng nhập URL đúng định dạng (https://...).
```

---

## 📊 Error Priority

### Critical (Ngăn chặn action)
1. ❌ Validation errors (empty fields, invalid format)
2. ❌ Conflict errors (duplicate)
3. ❌ Dependency errors (cannot delete)

### Warning (Cảnh báo)
1. ⚠️ Delete confirmations
2. ⚠️ Data loss warnings

### Info (Thông tin)
1. ✅ Success messages
2. ✅ Operation completed

---

## 🔑 Key Principles

1. **Specific**: Nói rõ lỗi gì, ở đâu
2. **Actionable**: Hướng dẫn cách fix
3. **User-friendly**: Tiếng Việt, dễ hiểu
4. **Consistent**: Format giống nhau
5. **Visual**: Icon ❌ ✅ ⚠️ dễ nhận biết

---

## 💡 Tips for Users

### Khi thêm sản phẩm:
- ✓ Đảm bảo mã sản phẩm chưa tồn tại
- ✓ Nhập giá > 0
- ✓ Mô tả ≤ 2000 ký tự
- ✓ Chọn danh mục hợp lệ

### Khi xóa danh mục:
- ✓ Xóa hoặc chuyển sản phẩm trước
- ✓ Kiểm tra số sản phẩm trong danh mục

### Khi thêm hình ảnh:
- ✓ Dùng URL đầy đủ (https://...)
- ✓ Kiểm tra URL chưa được thêm
- ✓ Đảm bảo URL trỏ đến hình ảnh hợp lệ

---

**Last Updated**: January 9, 2025
