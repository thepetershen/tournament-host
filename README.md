# 🏆 Tournament Host

A complete, production-ready tournament management system supporting **Round Robin**, **Single Elimination**, and **Double Elimination** formats for both singles and doubles matches.

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/yourusername/tournament-host)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Quick Start](#-quick-start-with-docker)
- [Manual Installation](#-manual-installation)
- [API Documentation](#-api-documentation)
- [Configuration](#-configuration)
- [Docker Deployment](#-docker-deployment)
- [Production Deployment](#-production-deployment)
- [Project Structure](#-project-structure)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Tournament Management
- **Multiple Format Support**:
  - Round Robin (every player/team plays every other player/team)
  - Single Elimination (knockout brackets)
  - Double Elimination (winners and losers brackets with bronze match)

- **Singles & Doubles**: Full support for both individual and team-based competitions
- **Flexible Match Configuration**: Customize games per match, scoring systems
- **Automated Seeding**: Manual or automatic seeding based on rankings
- **Live Standings**: Real-time leaderboards and rankings

### User Features
- **Secure Authentication**: JWT-based auth with password reset via email
- **User Profiles**: Track player statistics and tournament history
- **Team Management**: Create and manage doubles teams
- **Event Registration**: Players can register for events with approval workflows

### Admin Features
- **Tournament Control**: Create, edit, and manage tournaments
- **Event Scheduling**: Set up multiple events per tournament
- **Match Management**: Record scores, update results
- **Access Control**: Tournament owners and authorized editors
- **League System**: Track player rankings across multiple tournaments

### Technical Features
- **REST API**: Comprehensive API with Swagger/OpenAPI documentation
- **Database Migrations**: Automated schema versioning with Flyway
- **Containerization**: Docker and Docker Compose support
- **Error Monitoring**: Structured logging with integration points for Sentry
- **Security**: Rate limiting, CORS protection, input validation
- **Production Ready**: Connection pooling, health checks, graceful shutdown

---

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 21
- **Database**: PostgreSQL 17
- **Security**: Spring Security + JWT
- **ORM**: Hibernate/JPA
- **Migrations**: Flyway
- **Documentation**: SpringDoc OpenAPI 3
- **Build Tool**: Maven

### Frontend
- **Framework**: React 18
- **Build Tool**: Vite
- **HTTP Client**: Axios
- **Routing**: React Router
- **Styling**: CSS Modules

### DevOps
- **Containerization**: Docker & Docker Compose
- **Web Server**: Nginx (for frontend)
- **Logging**: Logback with rolling file appenders

---

## 🚀 Quick Start with Docker

The fastest way to get Tournament Host running:

### Prerequisites
- [Docker](https://docs.docker.com/get-docker/) (v20.10+)
- [Docker Compose](https://docs.docker.com/compose/install/) (v2.0+)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/tournament-host.git
   cd tournament-host
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   ```

3. **Edit `.env` and configure**:
   ```bash
   # REQUIRED: Change these values!
   DB_PASSWORD=your_secure_password_here
   JWT_SECRET_KEY=$(openssl rand -base64 64)
   SENDGRID_API_KEY=your_sendgrid_api_key
   ```

4. **Start all services**
   ```bash
   docker-compose up -d
   ```

5. **Access the application**
   - **Frontend**: http://localhost:80
   - **Backend API**: http://localhost:8080
   - **API Docs**: http://localhost:8080/swagger-ui.html

That's it! The application is now running with:
- PostgreSQL database on port 5432
- Backend API on port 8080
- Frontend on port 80

### Stop the services
```bash
docker-compose down
```

### View logs
```bash
docker-compose logs -f
```

---

## 💻 Manual Installation

If you prefer to run without Docker:

### Prerequisites
- **Java 21** or higher ([Download](https://adoptium.net/))
- **Node.js 20** or higher ([Download](https://nodejs.org/))
- **PostgreSQL 17** ([Download](https://www.postgresql.org/download/))
- **Maven 3.9+** ([Download](https://maven.apache.org/download.cgi))

### Backend Setup

1. **Create PostgreSQL database**
   ```sql
   CREATE DATABASE tournament_host;
   CREATE USER tournament_manager WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE tournament_host TO tournament_manager;
   ```

2. **Configure backend**
   ```bash
   cd connectFrontendWithBackend
   cp .env.example .env
   ```

3. **Edit `connectFrontendWithBackend/.env`**:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/tournament_host
   DB_USERNAME=tournament_manager
   DB_PASSWORD=your_password
   JWT_SECRET_KEY=$(openssl rand -base64 64)
   SENDGRID_API_KEY=your_sendgrid_key
   ```

4. **Run database migrations & start backend**
   ```bash
   ./mvnw spring-boot:run
   ```

   Backend will be available at: http://localhost:8080

### Frontend Setup

1. **Install dependencies**
   ```bash
   cd tournamentFrontend
   npm install
   ```

2. **Create frontend environment file**
   ```bash
   echo "VITE_API_BASE_URL=http://localhost:8080" > .env
   ```

3. **Start development server**
   ```bash
   npm run dev
   ```

   Frontend will be available at: http://localhost:5173

---

## 📚 API Documentation

Interactive API documentation is available via Swagger UI:

**URL**: http://localhost:8080/swagger-ui.html

### Key Endpoints

#### Authentication
- `POST /auth/signup` - Register new user
- `POST /auth/login` - Login and get JWT token
- `POST /auth/forgot-password` - Request password reset
- `POST /auth/reset-password` - Reset password with token

#### Tournaments
- `GET /api/tournaments` - List all tournaments
- `POST /api/tournaments` - Create tournament
- `GET /api/tournaments/{id}` - Get tournament details
- `PUT /api/tournaments/{id}` - Update tournament
- `DELETE /api/tournaments/{id}` - Delete tournament

#### Events
- `POST /api/tournaments/{tournamentId}/events` - Create event
- `POST /api/tournaments/{tournamentId}/event/{eventIndex}/initialize` - Initialize event (create matches)
- `GET /api/tournaments/{tournamentId}/event/{eventIndex}/draw` - Get event bracket/draw

#### Matches
- `GET /api/tournaments/{tournamentId}/event/{eventIndex}/matches` - Get all matches
- `POST /api/matches/{matchId}/result` - Submit match result

For full details, visit the Swagger UI after starting the backend.

---

## ⚙️ Configuration

### Environment Variables

All configuration is done via environment variables (loaded from `.env`):

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `DB_URL` | PostgreSQL connection URL | - | ✅ |
| `DB_USERNAME` | Database username | tournament_manager | ✅ |
| `DB_PASSWORD` | Database password | - | ✅ |
| `JWT_SECRET_KEY` | Secret key for JWT tokens (64+ chars) | - | ✅ |
| `JWT_EXPIRATION_TIME` | Token expiration in ms | 86400000 (24h) | ❌ |
| `SENDGRID_API_KEY` | SendGrid API key for emails | - | ✅ |
| `FRONTEND_URL` | Frontend URL for CORS/emails | http://localhost:5173 | ❌ |
| `SERVER_PORT` | Backend server port | 8080 | ❌ |
| `SPRING_PROFILES_ACTIVE` | Spring profile (dev/prod) | prod | ❌ |

### Generating Secrets

```bash
# Generate JWT secret key
openssl rand -base64 64

# Generate database password
openssl rand -base64 32
```

---

## 🐳 Docker Deployment

### Build and Run

```bash
# Build images
docker-compose build

# Start services
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Stop services
docker-compose down

# Stop and remove volumes (WARNING: deletes database data)
docker-compose down -v
```

### Custom Ports

Edit `.env` to change ports:

```bash
BACKEND_PORT=8080     # Backend API port
FRONTEND_PORT=80      # Frontend port
DB_PORT=5432          # PostgreSQL port
```

---

## 🌐 Production Deployment

### Pre-Deployment Checklist

- [ ] Change all default passwords in `.env`
- [ ] Generate new `JWT_SECRET_KEY`
- [ ] Set `SPRING_PROFILES_ACTIVE=prod`
- [ ] Update `FRONTEND_URL` to your domain
- [ ] Configure `SENDGRID_API_KEY` for email
- [ ] Set up SSL/TLS certificates
- [ ] Configure firewall rules
- [ ] Set up backup strategy for database
- [ ] Configure monitoring (optional: Sentry, Datadog)

### Production Environment Variables

```bash
# Production configuration
DB_PASSWORD=<strong-secure-password>
JWT_SECRET_KEY=<64-character-random-string>
SENDGRID_API_KEY=<your-sendgrid-key>
FRONTEND_URL=https://yourdomain.com
VITE_API_BASE_URL=https://api.yourdomain.com
SPRING_PROFILES_ACTIVE=prod
```

### Reverse Proxy Setup (Nginx)

Example Nginx config for production:

```nginx
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # Frontend
    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Backend API
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### Database Backups

Automated daily backup script:

```bash
#!/bin/bash
# backup-db.sh
docker exec tournament-postgres pg_dump -U tournament_manager tournament_host > backup_$(date +%Y%m%d).sql
```

---

## 📁 Project Structure

```
tournament-host/
├── connectFrontendWithBackend/      # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/tournamenthost/
│   │   │   │       ├── Controller/     # REST endpoints
│   │   │   │       ├── Service/        # Business logic
│   │   │   │       ├── Model/          # JPA entities
│   │   │   │       ├── Repository/     # Database access
│   │   │   │       ├── Security/       # Auth & security
│   │   │   │       └── Config/         # Configuration
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── logback-spring.xml  # Logging config
│   │   │       └── db/migration/       # Flyway migrations
│   │   └── test/                       # Tests
│   ├── Dockerfile
│   └── pom.xml
│
├── tournamentFrontend/                 # React frontend
│   ├── src/
│   │   ├── Pages/                     # React pages
│   │   ├── Components/                # Reusable components
│   │   └── utils/                     # Utilities & API clients
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
│
├── docker-compose.yml                  # Multi-container setup
├── .env.example                        # Environment template
├── .dockerignore
└── README.md
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🆘 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/tournament-host/issues)
- **Email**: support@tournamenthost.com
- **Documentation**: http://localhost:8080/swagger-ui.html

---

## 🙏 Acknowledgments

Built with:
- [Spring Boot](https://spring.io/projects/spring-boot)
- [React](https://reactjs.org/)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker](https://www.docker.com/)

---

**Made with ❤️ for tournament organizers worldwide**
