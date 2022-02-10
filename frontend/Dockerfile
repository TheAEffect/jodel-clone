# ==========================================
# Stage 1: Build Svelte Frontend
# ==========================================
FROM node:18-alpine AS builder

WORKDIR /app

# Arguments for the backend URL (baked into JS bundle at build time)
ARG SERVER_PATH=http://localhost
ARG SERVER_PORT=8080

ENV SERVER_PATH=${SERVER_PATH}
ENV SERVER_PORT=${SERVER_PORT}

# Copy package manifests
COPY package*.json ./

# Install npm dependencies
RUN npm install

# Copy source code and assets
COPY . .

# Build production bundle
RUN npm run build

# ==========================================
# Stage 2: Nginx Static Web Server
# ==========================================
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy static assets and compiled bundle
COPY --from=builder /app/public /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

