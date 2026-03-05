# Development stage
FROM node:18-alpine AS development

WORKDIR /app

# Copy package files and install dependencies
# Source code is bind-mounted from host for hot-reload
COPY package*.json ./
RUN npm install

# Expose Vue CLI dev server port (matches docker-compose port mapping 5173:5173)
EXPOSE 5173

# Start development server with hot-reload accessible from outside the container
CMD ["npm", "run", "serve", "--", "--port", "5173", "--host", "0.0.0.0"]

# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Production stage
FROM nginx:alpine AS production
WORKDIR /usr/share/nginx/html

# Remove default nginx static assets
RUN rm -rf ./*

# Copy built assets from build stage
COPY --from=build /app/dist .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
