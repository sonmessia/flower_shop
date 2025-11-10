# Blog Module API Documentation

## Endpoints

### Public Endpoints (User)

#### 1. Get All Published Blogs
```
GET /api/blogs
```
Lấy danh sách tất cả các blog đã publish.

**Query Parameters:**
- `search` (optional): Tìm kiếm blog theo title hoặc content

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "title": "Blog Title",
    "content": "Blog content...",
    "imageUrl": "https://...",
    "summary": "Short summary",
    "status": "PUBLISHED",
    "authorId": 1,
    "authorUsername": "admin",
    "createdAt": "2025-11-10T10:00:00",
    "updatedAt": "2025-11-10T10:00:00"
  }
]
```

#### 2. Get Blog Detail
```
GET /api/blogs/{id}
```
Lấy chi tiết một blog theo ID.

**Response:** `200 OK`
```json
{
  "id": 1,
  "title": "Blog Title",
  "content": "Full blog content...",
  "imageUrl": "https://...",
  "summary": "Short summary",
  "status": "PUBLISHED",
  "authorId": 1,
  "authorUsername": "admin",
  "createdAt": "2025-11-10T10:00:00",
  "updatedAt": "2025-11-10T10:00:00"
}
```

---

### Admin Endpoints

#### 3. Create New Blog
```
POST /api/admin/blogs
```
Tạo blog mới (Admin only).

**Request Body:**
```json
{
  "title": "New Blog Title",
  "content": "Blog content here...",
  "imageUrl": "https://example.com/image.jpg",
  "summary": "Short summary",
  "status": "DRAFT",
  "authorId": 1
}
```

**Validation:**
- `title`: Required, max 255 characters
- `content`: Required
- `summary`: Optional, max 500 characters
- `status`: DRAFT or PUBLISHED (default: DRAFT)
- `authorId`: Optional

**Response:** `201 CREATED`

#### 4. Update Blog
```
PUT /api/admin/blogs/{id}
```
Cập nhật blog (Admin only).

**Request Body:**
```json
{
  "title": "Updated Title",
  "content": "Updated content...",
  "imageUrl": "https://example.com/new-image.jpg",
  "summary": "Updated summary",
  "status": "PUBLISHED"
}
```

**Response:** `200 OK`

#### 5. Delete Blog
```
DELETE /api/admin/blogs/{id}
```
Xóa blog (Admin only).

**Response:** `204 NO CONTENT`

#### 6. Get All Blogs (Including Drafts)
```
GET /api/admin/blogs
```
Lấy tất cả blog bao gồm cả draft (Admin only).

**Response:** `200 OK`

#### 7. Get Blogs By Author
```
GET /api/admin/blogs/author/{authorId}
```
Lấy tất cả blog của một author (Admin only).

**Response:** `200 OK`

#### 8. Publish Blog
```
PATCH /api/admin/blogs/{id}/publish
```
Chuyển blog từ DRAFT sang PUBLISHED (Admin only).

**Response:** `200 OK`

#### 9. Unpublish Blog
```
PATCH /api/admin/blogs/{id}/unpublish
```
Chuyển blog từ PUBLISHED về DRAFT (Admin only).

**Response:** `200 OK`

---

## Database Schema

### Table: `blogs`
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

---

## Error Responses

### 404 Not Found
```json
{
  "message": "Blog not found with id: 1"
}
```

### 400 Bad Request (Validation Error)
```json
{
  "message": "Validation failed",
  "errors": {
    "title": "Title is required",
    "content": "Content is required"
  }
}
```

---

## Usage Examples

### Example 1: User xem danh sách blog
```bash
curl -X GET http://localhost:8080/api/blogs
```

### Example 2: User tìm kiếm blog
```bash
curl -X GET "http://localhost:8080/api/blogs?search=flower"
```

### Example 3: Admin tạo blog mới
```bash
curl -X POST http://localhost:8080/api/admin/blogs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Hướng dẫn chăm sóc hoa hồng",
    "content": "Nội dung chi tiết về cách chăm sóc hoa hồng...",
    "imageUrl": "https://example.com/rose.jpg",
    "summary": "Hướng dẫn cơ bản chăm sóc hoa hồng",
    "status": "PUBLISHED",
    "authorId": 1
  }'
```

### Example 4: Admin publish blog
```bash
curl -X PATCH http://localhost:8080/api/admin/blogs/1/publish
```

### Example 5: Admin xóa blog
```bash
curl -X DELETE http://localhost:8080/api/admin/blogs/1
```
