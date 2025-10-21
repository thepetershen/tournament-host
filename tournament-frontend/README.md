# Tournament Host

A full-stack web application for managing tournaments and competitive events, built with Spring Boot and React.

## Features

### 🏆 Tournament Management
- Create and manage tournaments
- Add multiple events per tournament
- Support for single elimination brackets
- Tournament listing and details

### 👤 User Management
- User registration and authentication
- JWT-based secure sessions
- Player profiles with tournament associations

### 🎯 Event System
- Event creation within tournaments
- Player registration for events
- Automatic bracket generation
- Match tracking and visualization

### 🔧 API Features
- RESTful API with comprehensive endpoints
- Cross-origin support for frontend integration
- Error handling and validation
- Search functionality

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 24
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT
- **ORM**: JPA/Hibernate
- **Build Tool**: Maven

### Frontend
- **Framework**: React 19.1.0
- **Bundler**: Vite
- **Routing**: React Router 7.6.3
- **HTTP Client**: Axios
- **Language**: JavaScript (ES6+)

## Project Structure

```
tournament-host/
├── connect-frontend-with-backend/    # Spring Boot API
│   ├── src/main/java/
│   │   └── com/tournamenthost/connect/frontend/with/backend/
│   │       ├── Controller/           # REST controllers
│   │       ├── DTO/                 # Data transfer objects
│   │       ├── Model/               # JPA entities
│   │       ├── Repository/          # Data access layer
│   │       ├── Security/            # Authentication & authorization
│   │       └── Service/             # Business logic
│   └── pom.xml
└── tournament-frontend/              # React frontend
    ├── src/
    │   ├── Components/              # Reusable UI components
    │   ├── Pages/                   # Route components
    │   └── utils/                   # Utility functions
    └── package.json
```

## Getting Started

### Prerequisites
- Java 24
- Node.js (v18+)
- PostgreSQL
- Maven

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd connect-frontend-with-backend
   ```

2. Configure PostgreSQL database connection in `application.properties`

3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd tournament-frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm run dev
   ```

The frontend will be available at `http://localhost:5173`

## API Endpoints

### Authentication
- `POST /auth/signup` - User registration
- `POST /auth/login` - User login

### Tournaments
- `GET /api/tournaments` - Get all tournaments
- `POST /api/tournaments` - Create tournament
- `GET /api/tournaments/{id}` - Get tournament details
- `GET /api/tournaments/{id}/users` - Get tournament players
- `GET /api/tournaments/{id}/events` - Get tournament events

### Events
- `POST /api/tournaments/{tournamentId}/event` - Create event
- `GET /api/tournaments/{tournamentId}/event/{eventIndex}/players` - Get event players
- `POST /api/tournaments/{tournamentId}/event/{eventIndex}/players` - Add players to event
- `GET /api/tournaments/{tournamentId}/event/{eventIndex}/draw` - Get tournament bracket
- `POST /api/tournaments/{tournamentId}/event/{eventIndex}/initialize` - Initialize event

## Roadmap

### Planned Features
- 🎨 Enhanced UI/UX design
- 🏅 Multiple tournament formats (double elimination, round robin, Swiss)
- 📊 Ranking system and leagues
- 🔍 Advanced search functionality for users and tournaments
- 📢 Tournament hosting features:
  - Player approval system
  - Tournament announcements
  - Seeding based on rankings
  - Tournament advertising
  - Image/logo storage
- 👥 Enhanced user roles and permissions
- 📱 Mobile responsiveness improvements

### Technical Improvements
- Type safety with TypeScript migration
- Comprehensive test coverage
- Performance optimizations
- Enhanced error handling
- API documentation with Swagger/OpenAPI

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is open source and available under the [MIT License](LICENSE).


Todos:


Frontend: Support for every event type. Event pages and how that looks. Hosting. player pages. League pages. 
actual hosting part: sign up(and approval of said players), winners, announcements, ability to advertise tournament, picture storage, seeding using ranking

Better UI in general (pretty important)

security!!!