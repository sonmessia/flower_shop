# -----------------------------
# 1. Base Stage (Common dependencies setup)
# -----------------------------
FROM node:18-alpine AS base
WORKDIR /app
# Only copy package files first to leverage Docker layer caching
COPY package*.json ./

# -----------------------------
# 2. Development Stage
# -----------------------------
FROM base AS development
# Install all dependencies (including devDependencies)
RUN npm install

# Default Vue CLI dev server port (update if overridden in vue.config.js)
EXPOSE 5173

# Start the Vue CLI dev server and allow external access (--host 0.0.0.0)
CMD ["npm", "run", "serve", "--", "--port", "5173", "--host", "0.0.0.0"]

# -----------------------------
# 3. Build Stage
# -----------------------------
FROM base AS build
# Use 'npm ci' instead of 'npm install' for deterministic builds (relies on package-lock.json)
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build the application (default output folder for Vite/Vue CLI is /app/dist)
RUN npm run build

# -----------------------------
# 4. Production Stage
# -----------------------------
FROM nginx:alpine AS production
WORKDIR /usr/share/nginx/html

# Remove default Nginx static assets
RUN rm -rf ./*

# Copy built assets from the build stage
COPY --from=build /app/dist .

# Copy custom Nginx configuration (MUST exist in the same directory as the Dockerfile)
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Run Nginx in the foreground (otherwise the container will exit immediately)
CMD ["nginx", "-g", "daemon off;"]
