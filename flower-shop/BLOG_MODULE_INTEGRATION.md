# Blog Module - Hướng dẫn tích hợp

## Các file đã tạo

### 1. Model Layer
- `model/Blog.java` - Entity chính với các trường:
  - id, title, content, imageUrl, summary
  - status (DRAFT/PUBLISHED)
  - author (reference đến Admin)
  - createdAt, updatedAt (tự động)

### 2. Repository Layer
- `repository/BlogRepository.java` - Interface với các method:
  - findByStatus() - Lọc theo trạng thái
  - findByAuthor_Id() - Lọc theo tác giả
  - searchPublishedBlogs() - Tìm kiếm full-text

### 3. DTO Layer
- `dto/blog/BlogCreateRequest.java` - Request tạo blog mới
- `dto/blog/BlogUpdateRequest.java` - Request cập nhật blog
- `dto/blog/BlogResponse.java` - Response trả về client

### 4. Service Layer
- `service/BlogService.java` - Business logic với các method:
  - createBlog() - Tạo blog
  - getAllBlogs() - Lấy tất cả (admin)
  - getPublishedBlogs() - Lấy blog đã publish (public)
  - getBlogById() - Chi tiết blog
  - updateBlog() - Cập nhật
  - deleteBlog() - Xóa
  - publishBlog() / unpublishBlog() - Thay đổi trạng thái

### 5. Controller Layer
- `controller/BlogController.java` - RESTful API endpoints

## Cấu trúc Database

Module sẽ tự động tạo bảng `blogs` khi chạy application (nhờ JPA):

```sql
CREATE TABLE blogs (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(255),
    summary VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    author_id BIGINT REFERENCES admins(id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

## API Endpoints

### Public (Người dùng)
```
GET    /api/blogs              - Danh sách blog published
GET    /api/blogs?search=...   - Tìm kiếm blog
GET    /api/blogs/{id}         - Chi tiết blog
```

### Admin
```
POST   /api/admin/blogs                 - Tạo blog mới
PUT    /api/admin/blogs/{id}            - Cập nhật blog
DELETE /api/admin/blogs/{id}            - Xóa blog
GET    /api/admin/blogs                 - Danh sách tất cả blog
GET    /api/admin/blogs/author/{id}     - Blog theo tác giả
PATCH  /api/admin/blogs/{id}/publish    - Publish blog
PATCH  /api/admin/blogs/{id}/unpublish  - Unpublish blog
```

## Cách sử dụng

### 1. Chạy application
```bash
mvn spring-boot:run
```

### 2. Test API với curl

#### Tạo blog mới (Admin)
```bash
curl -X POST http://localhost:8080/api/admin/blogs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Hướng dẫn chọn hoa tươi",
    "content": "Nội dung chi tiết...",
    "summary": "Mẹo chọn hoa tươi lâu",
    "imageUrl": "https://example.com/flower.jpg",
    "status": "PUBLISHED",
    "authorId": 1
  }'
```

#### Xem danh sách blog (Public)
```bash
curl http://localhost:8080/api/blogs
```

#### Tìm kiếm blog
```bash
curl "http://localhost:8080/api/blogs?search=hoa"
```

#### Publish blog
```bash
curl -X PATCH http://localhost:8080/api/admin/blogs/1/publish
```

## Tích hợp với Frontend

### React/Angular/Vue Example
```javascript
// Get all published blogs
const blogs = await fetch('/api/blogs').then(r => r.json());

// Search blogs
const results = await fetch('/api/blogs?search=hoa').then(r => r.json());

// Get blog detail
const blog = await fetch('/api/blogs/1').then(r => r.json());

// Admin: Create blog
const newBlog = await fetch('/api/admin/blogs', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    title: 'Blog title',
    content: 'Blog content...',
    status: 'PUBLISHED',
    authorId: 1
  })
}).then(r => r.json());
```

## Security Notes

**QUAN TRỌNG**: Hiện tại module chưa có authentication/authorization. Cần bổ sung:

1. **Spring Security** để bảo vệ admin endpoints
2. **JWT Token** hoặc Session cho authentication
3. **Role-based access control** (ROLE_ADMIN, ROLE_USER)

### Ví dụ bổ sung Security:
```java
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/admin/blogs")
public BlogResponse createBlog(@Valid @RequestBody BlogCreateRequest request) {
    // ...
}
```

## Validation

Module đã tích hợp validation:
- Title: required, max 255 chars
- Content: required
- Summary: max 500 chars
- AuthorId: phải tồn tại trong bảng admins

## Error Handling

Sử dụng exception handler có sẵn trong project:
- `ResourceNotFoundException` - Khi không tìm thấy blog/admin
- `ValidationException` - Khi dữ liệu không hợp lệ

## Testing

### Unit Test Example
```java
@Test
void testCreateBlog() {
    BlogCreateRequest request = new BlogCreateRequest();
    request.setTitle("Test Blog");
    request.setContent("Test content");
    
    Blog blog = blogService.createBlog(request);
    
    assertNotNull(blog.getId());
    assertEquals("Test Blog", blog.getTitle());
}
```

## Next Steps

1. ✅ Module đã hoàn thiện và build thành công
2. 🔒 Bổ sung Spring Security cho admin endpoints
3. 📝 Thêm pagination cho danh sách blog (nếu cần)
4. 🖼️ Upload image service (nếu cần)
5. 💬 Module comment cho blog (tùy chọn)
6. 📊 Blog analytics (view count, likes, etc.)

## Compatibility

- ✅ Spring Boot 3.5.6
- ✅ Java 17
- ✅ PostgreSQL / H2
- ✅ Lombok
- ✅ JPA/Hibernate
- ✅ Bean Validation

Module đã sẵn sàng sử dụng và dễ dàng mở rộng!
