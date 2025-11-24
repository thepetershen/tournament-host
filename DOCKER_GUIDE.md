# 🐳 Docker Guide for Tournament Host

## What is Docker?

**Docker** is a platform that packages your application and all its dependencies into a standardized unit called a **container**. Think of it as a lightweight, portable box that contains everything needed to run your application.

### Why Use Docker?

✅ **"It works on my machine" problem solved** - If it runs in Docker, it runs everywhere
✅ **No manual setup** - No need to install Java, Node.js, PostgreSQL separately
✅ **Isolation** - Each service runs in its own container
✅ **Easy deployment** - One command to start everything
✅ **Consistency** - Development and production environments are identical

---

## Tournament Host Docker Architecture

Your application uses **3 containers**:

```
┌─────────────────────────────────────────────────────┐
│                 Docker Network                      │
│                                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────┐ │
│  │   Frontend   │  │   Backend    │  │ Database │ │
│  │   (React)    │  │ (Spring Boot)│  │(Postgres)│ │
│  │   Port 80    │  │  Port 8080   │  │Port 5432 │ │
│  └──────────────┘  └──────────────┘  └──────────┘ │
│         ▲                 ▲                ▲        │
└─────────┼─────────────────┼────────────────┼────────┘
          │                 │                │
      Your Browser    API Requests      Data Storage
```

---

## Understanding the Files

### 1. `Dockerfile` (Frontend & Backend)

A `Dockerfile` is like a recipe for building a container image. It tells Docker:
- What base image to use (like Ubuntu, Java, Node.js)
- What files to copy
- What commands to run
- How to start the application

**Backend Dockerfile** (2-stage build):
```dockerfile
# Stage 1: Build the JAR file
FROM maven:3.9-eclipse-temurin-21-alpine AS build
# ... build steps ...

# Stage 2: Run the JAR file
FROM eclipse-temurin:21-jre-alpine
# ... runtime setup ...
```

**Why 2 stages?**
- Stage 1: Heavy (includes Maven, build tools) - used only for building
- Stage 2: Lightweight (only JRE) - used for running in production
- Result: Smaller final image (better performance, faster deployment)

### 2. `docker-compose.yml`

Docker Compose is a tool for defining and running **multi-container** applications. Instead of running 3 separate `docker run` commands, you define everything in one file.

**Our docker-compose.yml defines**:
- **postgres**: Database service
- **backend**: Spring Boot API
- **frontend**: React application

**Key Concepts**:

```yaml
services:
  postgres:
    image: postgres:17-alpine        # Use official Postgres image
    environment:                     # Set environment variables
      POSTGRES_DB: tournament_host
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:                         # Persist data (survives container restart)
      - postgres_data:/var/lib/postgresql/data
    networks:                        # Connect to network
      - tournament-network

  backend:
    build:                           # Build from Dockerfile
      context: ./connectFrontendWithBackend
    depends_on:                      # Wait for postgres to be healthy
      postgres:
        condition: service_healthy
    ports:                           # Map container port to host
      - "8080:8080"                  # host:container
```

### 3. `.env` File

Environment variables file. Docker Compose reads this and injects values into containers.

**Example**:
```bash
DB_PASSWORD=mySecurePassword123
JWT_SECRET_KEY=abc...xyz
```

When you use `${DB_PASSWORD}` in `docker-compose.yml`, Docker replaces it with the value from `.env`.

---

## Docker Commands Explained

### Basic Commands

```bash
# Start all services (detached mode = runs in background)
docker-compose up -d

# What it does:
# 1. Reads docker-compose.yml
# 2. Builds images if needed (backend, frontend)
# 3. Pulls images from Docker Hub (postgres)
# 4. Creates network for containers to communicate
# 5. Starts containers in the right order (postgres → backend → frontend)
```

```bash
# Stop all services
docker-compose down

# What it does:
# 1. Stops all running containers
# 2. Removes containers
# 3. Keeps volumes (data) by default
```

```bash
# View logs (follow mode = real-time updates)
docker-compose logs -f

# Options:
docker-compose logs -f backend   # Only backend logs
docker-compose logs -f frontend  # Only frontend logs
docker-compose logs --tail=100 backend  # Last 100 lines
```

```bash
# Check status of containers
docker-compose ps

# Output example:
# NAME                  STATUS         PORTS
# tournament-backend    Up 10 minutes  0.0.0.0:8080->8080/tcp
# tournament-frontend   Up 10 minutes  0.0.0.0:80->80/tcp
# tournament-postgres   Up 10 minutes  0.0.0.0:5432->5432/tcp
```

```bash
# Rebuild images (after code changes)
docker-compose build

# Or rebuild and restart:
docker-compose up -d --build
```

```bash
# Execute commands inside a running container
docker-compose exec backend bash      # Open shell in backend
docker-compose exec postgres psql -U tournament_manager tournament_host
```

### Advanced Commands

```bash
# View resource usage (CPU, Memory)
docker stats

# Remove everything including volumes (⚠️ DELETES DATABASE DATA)
docker-compose down -v

# Pull latest images
docker-compose pull

# Restart a single service
docker-compose restart backend

# Scale a service (run multiple instances)
docker-compose up -d --scale backend=3
```

---

## Volumes Explained

**Volumes** are Docker's way of persisting data. Without volumes, all data is lost when a container stops.

```yaml
volumes:
  postgres_data:
    driver: local
```

This creates a named volume `postgres_data` that stores the PostgreSQL database files on your host machine (outside the container).

**Where is the data stored?**
- Linux: `/var/lib/docker/volumes/`
- Mac: `~/Library/Containers/com.docker.docker/Data/`
- Windows: `C:\ProgramData\Docker\volumes\`

---

## Networks Explained

Docker creates a **bridge network** so containers can talk to each other using container names as hostnames.

```yaml
networks:
  tournament-network:
    driver: bridge
```

**How backend connects to database**:
```java
// Backend uses container name "postgres" as hostname
DB_URL=jdbc:postgresql://postgres:5432/tournament_host
//                       ^^^^^^^^ container name, not "localhost"
```

---

## Health Checks

Health checks tell Docker if a container is working properly.

```yaml
healthcheck:
  test: ["CMD", "wget", "--spider", "http://localhost:8080/api/tournaments"]
  interval: 30s          # Check every 30 seconds
  timeout: 10s           # Wait max 10 seconds for response
  retries: 5             # Try 5 times before marking unhealthy
  start_period: 60s      # Give 60 seconds to start before checking
```

**Why use health checks?**
- Prevents traffic to unhealthy containers
- Ensures `depends_on` waits for service to be truly ready (not just started)

---

## Step-by-Step: What Happens When You Run `docker-compose up`

1. **Reads Configuration**
   - Loads `docker-compose.yml`
   - Reads `.env` file
   - Validates configuration

2. **Creates Network**
   - Creates `tournament-network` bridge network

3. **Pulls/Builds Images**
   - Pulls `postgres:17-alpine` from Docker Hub
   - Builds backend image from `connectFrontendWithBackend/Dockerfile`
   - Builds frontend image from `tournamentFrontend/Dockerfile`

4. **Creates Volumes**
   - Creates `postgres_data` volume for database persistence

5. **Starts Containers** (in order)
   - Starts `postgres` container
   - Waits for postgres health check to pass
   - Starts `backend` container
   - Starts `frontend` container

6. **Exposes Ports**
   - Maps port 5432 (postgres) → localhost:5432
   - Maps port 8080 (backend) → localhost:8080
   - Maps port 80 (frontend) → localhost:80

7. **Injects Environment Variables**
   - Passes variables from `.env` to each container

8. **Application Ready!**
   - Frontend: http://localhost:80
   - Backend: http://localhost:8080
   - Database: localhost:5432

---

## Troubleshooting

### Container won't start

```bash
# Check logs for errors
docker-compose logs backend

# Common issues:
# - Port already in use → Change port in .env
# - Missing environment variable → Check .env file
# - Database connection failed → Check postgres is healthy
```

### Database connection errors

```bash
# Check postgres is running
docker-compose ps postgres

# Check health status
docker-compose ps
# Look for "healthy" status

# Connect to database manually
docker-compose exec postgres psql -U tournament_manager tournament_host
```

### "Port is already allocated"

```bash
# Find what's using the port
lsof -i :8080    # Mac/Linux
netstat -ano | findstr :8080    # Windows

# Change port in .env
BACKEND_PORT=8081
```

### Out of memory / disk space

```bash
# Remove unused Docker data
docker system prune -a

# Remove all stopped containers
docker container prune

# Remove unused images
docker image prune -a

# Remove unused volumes (⚠️ deletes data)
docker volume prune
```

---

## Production Deployment

### Build for Production

```bash
# Build optimized images
docker-compose -f docker-compose.yml build --no-cache

# Tag images for registry
docker tag tournament-backend:latest your-registry.com/tournament-backend:1.0.0

# Push to registry (Docker Hub, AWS ECR, etc.)
docker push your-registry.com/tournament-backend:1.0.0
```

### Using Docker in AWS/Cloud

Most cloud providers support Docker:

- **AWS ECS/Fargate**: Upload images to ECR, deploy with ECS
- **Google Cloud Run**: Direct Docker container deployment
- **Azure Container Instances**: Deploy Docker containers
- **DigitalOcean App Platform**: Git push → automatic Docker build & deploy

---

## Useful Resources

- [Docker Official Docs](https://docs.docker.com/)
- [Docker Compose Docs](https://docs.docker.com/compose/)
- [Docker Hub](https://hub.docker.com/) - Public image registry
- [Play with Docker](https://labs.play-with-docker.com/) - Free online Docker playground

---

## Quick Reference

```bash
# Start everything
docker-compose up -d

# Stop everything
docker-compose down

# View logs
docker-compose logs -f

# Rebuild after code changes
docker-compose up -d --build

# Check status
docker-compose ps

# Access database
docker-compose exec postgres psql -U tournament_manager tournament_host

# Shell into backend container
docker-compose exec backend bash

# Clean up everything (⚠️ deletes data)
docker-compose down -v
docker system prune -a
```

---

**Questions?** Docker can seem complex at first, but it's incredibly powerful once you understand the basics. This setup means your application will run identically on any machine that has Docker installed!
