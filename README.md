# 🌸 Flower Shop - Flower Shop Management System

## 📋 Description

A flower shop management application with a pastel pink theme, including:

- **Backend**: Spring Boot + PostgreSQL
- **Frontend**: Vue.js 3
- **Database**: PostgreSQL (Pre-initialized with data via `init.sql`)

## 🚀 Installation and Setup

### Requirements

- Docker Desktop
- Docker Compose

### Run the entire system

The system provides a ready-to-use `manage.sh` script to easily manage Docker services:

```bash
# Provide execution permission for the script (if needed)
chmod +x manage.sh

# Start and run all services in the background
./manage.sh start

# View system logs
./manage.sh logs
```

Alternatively, you can use Docker Compose directly:

```bash
# Clone the repository and navigate into the directory
cd /home/hoangsonsdk/flower_shop

# Build and run all services
docker compose up --build

# Or run in the background
docker compose up -d --build
```

### Accessing the application

- **Frontend (Home)**: <http://localhost>
- **Backend API**: <http://localhost:8080>
- **Admin Dashboard**: <http://localhost/admin/login>

### Stopping the system

Using the management script:

```bash
# Stop all services
./manage.sh stop

# Stop and remove all data (including the database)
./manage.sh clean
```

Or using Docker Compose:

```bash
# Stop all containers
docker compose down

# Stop and remove volumes (deletes the database)
docker compose down -v
```

## 🛠️ Local Development

### Backend (Spring Boot)

```bash
cd flower-shop

# Run PostgreSQL separately
docker run -d \
  --name postgres-dev \
  -e POSTGRES_DB=flowershop \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# Run backend
mvn spring-boot:run
```

### Frontend (Vue.js)

```bash
cd frontend

# Install dependencies
npm install

# Run dev server
npm run serve
```

Backend will run on: <http://localhost:8080>
Frontend will run on: <http://localhost:84>

## 📦 Project Structure

```text
flower_shop/
├── .env.example                # Example environment variables file
├── docker-compose.yml          # Docker Compose configuration (Dev environment)
├── docker-compose.prod.yml     # Docker Compose configuration (Production environment)
├── init.sql                    # Default database initialization script
├── manage.sh                   # Docker helper management script
├── flower-shop/                # Spring Boot Backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── frontend/                   # Vue.js Frontend
    ├── Dockerfile
    ├── nginx.conf
    ├── package.json
    └── src/
```

## 🔧 Environment Variables

Before running the system, create the `.env` file from the example file:

```bash
cp .env.example .env
```

The environment variables are defined in the `.env` file:

### Backend Environment Variables

| Variable    | Default    | Description         |
| ----------- | ---------- | ------------------- |
| DB_HOST     | localhost  | PostgreSQL host     |
| DB_PORT     | 5432       | PostgreSQL port     |
| DB_NAME     | flowershop | Database name       |
| DB_USER     | postgres   | Database username   |
| DB_PASSWORD | postgres   | Database password   |
| SERVER_PORT | 8080       | Backend server port |

### Frontend Build Args

The frontend is built with `VUE_APP_API_BASE=http://localhost:8080/api`

## 📝 API Endpoints

### Products

- **`GET /api/products`** - Get all products
- **`GET /api/products/{id}`** - Get product details
- **`POST /api/products`** - Create new product
- **`PUT /api/products/{id}`** - Update product
- **`DELETE /api/products/{id}`** - Delete product

### Categories

- **`GET /api/categories`** - Get all categories
- **`GET /api/categories/{id}/products`** - Get products by category
- **`POST /api/categories`** - Create new category
- **`PUT /api/categories/{id}`** - Update category
- **`DELETE /api/categories/{id}`** - Delete category

### Admin

- **`POST /api/admins/login`** - Admin login

## 🗄️ Database Schema

### Products Table

- `id` (BIGINT, PK)
- `product_code` (VARCHAR, UNIQUE)
- `name` (VARCHAR)
- `description` (TEXT)
- `price` (DOUBLE)
- `image_url` (VARCHAR)
- `category_id` (BIGINT, FK)

### Categories Table

- `id` (BIGINT, PK)
- `name` (VARCHAR)

### Product Collections Table

- `id` (BIGINT, PK)
- `name` (VARCHAR)

## 🎨 Theme

The application uses a pastel pink color palette:

- Primary: `#F36DA1`
- Light: `#FFE1F0`
- Dark: `#D63675`

## 🔐 Default Admin Credentials

- Username: `admin`
- Password: `admin123`

## 📸 Screenshots

[Add screenshots later]

## 🐛 Troubleshooting

### Port already in use

```bash
# Change the port in docker-compose.yml
# For example: "8081:8080" instead of "8080:8080"
```

### Database connection error

```bash
# Check if PostgreSQL is running
./manage.sh status

# View logs
./manage.sh logs
```

### Frontend cannot connect to backend

```bash
# Check backend logs
./manage.sh logs

# Restart services
./manage.sh restart
```

## 📄 License

MIT License

## 👥 Contributors

- [Your Name]
