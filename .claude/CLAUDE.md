# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Flower Shop is an e-commerce platform for a flower store with admin management capabilities. The system uses:
- **Backend**: Spring Boot 3.5.6 + PostgreSQL (Java 25)
- **Frontend**: Vue.js 3 with Vue Router
- **Infrastructure**: Docker Compose with separate development/production profiles

## Development Commands

### Docker-based Development (Recommended)
```bash
# Start development environment (backend on :8081, frontend on :5173)
docker compose --profile development up

# Start production environment (backend on :8080, frontend on :80)
docker compose --profile production up

# Management shortcuts
./manage.sh start     # Start all services
./manage.sh stop      # Stop all services
./manage.sh logs      # View logs (follow mode)
./manage.sh db        # Access PostgreSQL CLI
./manage.sh rebuild   # Rebuild all services
./manage.sh clean     # Stop and remove all data including database
```

### Local Backend Development
```bash
cd flower-shop
./mvnw spring-boot:run                    # Run backend
./mvnw test                                # Run tests
./mvnw clean package                       # Build JAR
./mvnw clean package -DskipTests          # Build without tests
```

### Local Frontend Development
```bash
cd frontend
npm install           # Install dependencies
npm run serve         # Dev server (hot-reload)
npm run build         # Production build
npm run lint          # Run ESLint
```

## Architecture

### Backend Structure (`flower-shop/src/main/java/vn/quahoa/flowershop/`)
```
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # JPA data access
├── model/          # JPA entities (Product, Category, Blog, Admin, etc.)
├── dto/            # Request/response data transfer objects
├── config/         # Security, CORS, storage, and initialization configs
├── exception/      # Custom exception handlers
```

The backend follows standard Spring Boot layered architecture with Lombok for boilerplate reduction.

### Frontend Structure (`frontend/src/`)
```
├── components/     # Vue SFC components (HomePage, AdminDashboard, etc.)
├── router/         # Vue Router configuration with auth guards
├── config/         # API base URL configuration
├── assets/         # Static assets and styles
```

### Database
- PostgreSQL with migrations in `database/migrations/`
- Tables: `products`, `product_images`, `categories`, `blogs`, `blog_images`, `admins`, `orders`, `order_items`
- Images stored as file paths (not binary) with URLs accessible via backend

## Key Configuration

### Environment Variables
Copy `.env.example` to `.env` and configure:
- `POSTGRES_*`: Database credentials
- `DB_*`: Backend database connection
- `VUE_APP_API_BASE`: Frontend API endpoint (e.g., `http://localhost:8081/api` for dev)

### Docker Profiles
- `development`: Backend on :8081, Frontend on :5173 with hot-reload and source mounting
- `production`: Backend on :8080, Frontend on :80 with Nginx

## API Endpoints

Base path: `/api`
- `/products` - Product CRUD
- `/categories` - Category management
- `/blogs` - Blog posts
- `/admins` - Admin authentication (`POST /admins/login`)

## Theme

Pink pastel color scheme:
- Primary: `#F36DA1`
- Light: `#FFE1F0`
- Dark: `#D63675`
