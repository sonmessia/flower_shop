# 🚀 Quick Start Guide

## Chạy ứng dụng với Docker

```bash
# Di chuyển vào thư mục project
cd /home/hoangsonsdk/flower_shop

# Khởi động tất cả services
./manage.sh start

# Hoặc dùng docker compose trực tiếp
docker compose up -d --build
```

## Truy cập ứng dụng

- **Trang chủ**: http://localhost
- **API**: http://localhost:8080
- **Admin**: http://localhost/admin/login

## Các lệnh hữu ích

```bash
# Xem logs
./manage.sh logs

# Xem trạng thái
./manage.sh status

# Dừng services
./manage.sh stop

# Restart services
./manage.sh restart

# Xóa tất cả dữ liệu và bắt đầu lại
./manage.sh clean
./manage.sh start

# Truy cập PostgreSQL
./manage.sh db
```

## Phát triển local (không dùng Docker)

### 1. Khởi động PostgreSQL

```bash
docker run -d \
  --name postgres-dev \
  -e POSTGRES_DB=flowershop \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### 2. Khởi động Backend

```bash
cd flower-shop
mvn clean install
mvn spring-boot:run
```

### 3. Khởi động Frontend

```bash
cd frontend
npm install
npm run serve
```

## Cấu trúc Database

Database sẽ được tạo tự động bởi Hibernate khi backend khởi động lần đầu.

Các bảng:
- `products` - Sản phẩm
- `categories` - Danh mục  
- `product_collections` - Bộ sưu tập
- `admins` - Quản trị viên

## Troubleshooting

### Port bị chiếm
```bash
# Kiểm tra port đang sử dụng
lsof -i :80    # Frontend
lsof -i :8080  # Backend
lsof -i :5432  # PostgreSQL

# Dừng tiến trình đang dùng port hoặc đổi port trong docker-compose.yml
```

### Không kết nối được database
```bash
# Kiểm tra logs PostgreSQL
docker compose logs postgres

# Restart lại database
docker compose restart postgres
```

### Frontend không load được
```bash
# Kiểm tra logs
docker compose logs frontend

# Rebuild frontend
docker compose up -d --build frontend
```

### Backend không khởi động
```bash
# Xem logs backend
docker compose logs backend

# Kiểm tra kết nối database
docker compose exec postgres pg_isready -U postgres
```

## Làm sạch và bắt đầu lại

```bash
# Dừng tất cả
docker compose down -v

# Xóa images (optional)
docker compose down --rmi all

# Build và start lại
docker compose up -d --build
```
