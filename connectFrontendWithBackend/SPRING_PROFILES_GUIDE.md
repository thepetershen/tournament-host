# Spring Profiles Guide

This application uses Spring Profiles to manage different configurations for development and production environments.

## Available Profiles

- **dev** - Local development (default)
- **prod** - Production deployment

## Running Locally (Development)

### Option 1: Using Maven
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Option 2: Using your IDE
Set the environment variable or VM option:
- **Environment Variable**: `SPRING_PROFILES_ACTIVE=dev`
- **IntelliJ IDEA**: Run → Edit Configurations → Environment Variables → Add `SPRING_PROFILES_ACTIVE=dev`
- **VS Code**: Add to launch.json: `"env": { "SPRING_PROFILES_ACTIVE": "dev" }`

### Option 3: Using .env file
The application will use `dev` profile by default (configured in application.properties).

Copy the example environment file:
```bash
cp .env.dev .env
```

Then run normally:
```bash
./mvnw spring-boot:run
```

## Running in Production

Set the profile via environment variable:
```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar your-app.jar
```

Or use the Spring Boot run command:
```bash
java -jar -Dspring.profiles.active=prod your-app.jar
```

## Key Differences Between Profiles

### Development (dev)
- Uses local PostgreSQL database
- More verbose logging (DEBUG level)
- Shows SQL queries
- Smaller connection pool
- `hibernate.ddl-auto=update` (auto-creates/updates schema)
- Fake/test credentials have defaults
- Frontend URL: `http://localhost:5173`

### Production (prod)
- Uses environment variables for all secrets (REQUIRED)
- Minimal logging (INFO/WARN level)
- No SQL query logging
- Full connection pool with timeouts
- `hibernate.ddl-auto=validate` (strict schema validation)
- Flyway migrations enabled
- All credentials MUST be provided via environment variables
- Frontend URL from environment variable

## Environment Variables

### Required for Development
See `.env.dev` for development defaults. Most have sensible defaults.

### Required for Production
All of these MUST be set in production:
- `SPRING_PROFILES_ACTIVE=prod`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET_KEY` (minimum 256 bits for HS256)
- `SENDGRID_API_KEY`
- `FRONTEND_URL`

## Making Local Changes Without Affecting Production

1. **Always use the dev profile locally**
2. **Keep your .env file for local settings** (it's gitignored)
3. **Production uses environment variables** set in your deployment platform
4. **Work on feature branches** - changes only affect production when merged and deployed

## Verifying Your Profile

When the application starts, check the logs for:
```
The following profiles are active: dev
```

or

```
The following profiles are active: prod
```
