FROM node:20-alpine AS build

WORKDIR /app

# Accept VAPID_PUBLIC_KEY as build argument
ARG VITE_VAPID_PUBLIC_KEY
ARG VITE_API_BASE=/api

# Set environment variables for build
ENV VITE_VAPID_PUBLIC_KEY=${VITE_VAPID_PUBLIC_KEY}
ENV VITE_API_BASE=${VITE_API_BASE}

# Copy package.json and package-lock.json first to leverage Docker cache
COPY package*.json ./
RUN npm install --legacy-peer-deps

# Copy the rest of the application
COPY . .

# Build the application with environment variables
RUN npm run build

# Production environment
FROM nginx:alpine

# Copy built assets from builder
COPY --from=build /app/dist /usr/share/nginx/html

# nginx.conf for Docker Compose (API proxy); nginx.k8s.conf for Kubernetes (static only)
ARG NGINX_CONF=nginx.conf
COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
