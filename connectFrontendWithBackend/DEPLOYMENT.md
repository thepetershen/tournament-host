# Deployment Guide

This guide covers deploying the Tournament Host backend (Spring Boot) and frontend to production servers.

---

## Table of Contents

1. [Backend Deployment Options](#backend-deployment-options)
2. [Frontend Deployment Options](#frontend-deployment-options)
3. [Complete Deployment Walkthrough](#complete-deployment-walkthrough)
4. [Production Environment Variables](#production-environment-variables)
5. [Database Setup](#database-setup)
6. [Domain and SSL](#domain-and-ssl)

---

## Backend Deployment Options

### Option 1: Railway (Recommended - Easiest)

**Why Railway:**
- Free tier available
- Automatic PostgreSQL database provisioning
- Easy environment variable management
- Automatic deployments from GitHub
- Built-in SSL certificates

**Cost:** Free tier includes 500 hours/month ($5/month after free tier)

### Option 2: Render

**Why Render:**
- Free tier for web services
- Managed PostgreSQL available
- Automatic HTTPS
- GitHub integration

**Cost:** Free tier available, PostgreSQL $7/month

### Option 3: AWS (Most Scalable)

**Services needed:**
- EC2 (server) or Elastic Beanstalk (managed)
- RDS (PostgreSQL database)
- Route 53 (DNS)
- Load Balancer (optional)

**Cost:** More expensive but enterprise-grade

### Option 4: DigitalOcean

**Services needed:**
- Droplet (VPS server)
- Managed PostgreSQL database

**Cost:** Starts at $6/month for droplet + $15/month for database

---

## Frontend Deployment Options

### Option 1: Vercel (Recommended for React)

**Why Vercel:**
- Optimized for React/Next.js
- Free tier with unlimited bandwidth
- Automatic deployments from GitHub
- Built-in CDN
- Custom domain support

**Cost:** Free for personal projects

### Option 2: Netlify

**Why Netlify:**
- Great for static sites
- Free SSL certificates
- Easy CI/CD from GitHub

**Cost:** Free tier generous

### Option 3: Same Server as Backend

**Why:**
- Single deployment
- No CORS issues (same origin)
- Lower cost

**How:** Build React app to static files, serve via Spring Boot

---

## Complete Deployment Walkthrough

### Deployment Strategy 1: Railway + Vercel (Recommended)

This separates backend and frontend for better scalability and easier management.

#### Step 1: Deploy Backend to Railway

**1.1. Create Railway Account**
```bash
# Install Railway CLI
npm i -g @railway/cli

# Login
railway login
```

**1.2. Initialize Railway Project**
```bash
cd connect-frontend-with-backend
railway init
```

**1.3. Add PostgreSQL Database**
- Go to Railway dashboard: https://railway.app/dashboard
- Click your project
- Click "New" → "Database" → "PostgreSQL"
- Railway automatically creates database and provides connection URL

**1.4. Set Environment Variables**

In Railway dashboard, go to your backend service → Variables:

```env
DB_URL=${RAILWAY_PROVIDED_DATABASE_URL}
DB_USERNAME=${DATABASE_USERNAME}
DB_PASSWORD=${DATABASE_PASSWORD}
JWT_SECRET_KEY=V8RLVN0CTaNxDqcqURtY4t5NSJJULZk5pQdTfpl1g3RstWweysUuPpyr8VNMFxU8qGGyCvs9m9CSDThAAZdNgQ==
JWT_EXPIRATION_TIME=3600000
SPRING_PROFILES_ACTIVE=prod
ALLOWED_ORIGINS=https://your-frontend-domain.vercel.app
```

**1.5. Create Railway Configuration**

Create `railway.toml` in your backend root:

```toml
[build]
builder = "NIXPACKS"
buildCommand = "mvn clean package -DskipTests"

[deploy]
startCommand = "java -jar target/connect-frontend-with-backend-0.0.1-SNAPSHOT.jar"
restartPolicyType = "ON_FAILURE"
restartPolicyMaxRetries = 10
```

**1.6. Update CORS Configuration**

Update `SecurityConfig.java` to allow your production frontend:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Get allowed origins from environment variable
    String allowedOrigins = System.getenv("ALLOWED_ORIGINS");
    if (allowedOrigins != null) {
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
    } else {
        // Fallback to localhost for development
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
    }

    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**1.7. Deploy**

```bash
# Deploy to Railway
railway up

# Or connect to GitHub for automatic deployments
railway link
```

Your backend will be available at: `https://your-project.up.railway.app`

#### Step 2: Deploy Frontend to Vercel

**2.1. Prepare Frontend Build**

Create `.env.production` in your frontend folder:

```env
REACT_APP_API_URL=https://your-backend.up.railway.app/api
```

Update your API calls to use this environment variable:

```javascript
// api/client.js or similar
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

export const apiClient = {
  get: (endpoint) => fetch(`${API_BASE_URL}${endpoint}`),
  post: (endpoint, data) => fetch(`${API_BASE_URL}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }),
  // ... other methods
};
```

**2.2. Deploy to Vercel**

```bash
# Install Vercel CLI
npm i -g vercel

# Login
vercel login

# Deploy from frontend directory
cd tournament-frontend
vercel

# For production deployment
vercel --prod
```

Or connect GitHub repository for automatic deployments:
1. Go to https://vercel.com/new
2. Import your repository
3. Set root directory to `tournament-frontend`
4. Add environment variable `REACT_APP_API_URL`
5. Deploy

Your frontend will be available at: `https://your-project.vercel.app`

**2.3. Update Railway CORS**

Update the `ALLOWED_ORIGINS` variable in Railway to include your Vercel domain:

```env
ALLOWED_ORIGINS=https://your-project.vercel.app,https://your-custom-domain.com
```

---

### Deployment Strategy 2: Single Server Deployment

Deploy both frontend and backend on the same server.

#### Option A: Serve React from Spring Boot

**1. Build React App**

```bash
cd tournament-frontend
npm run build
```

This creates a `build/` folder with static files.

**2. Copy Build to Spring Boot**

```bash
# Copy React build to Spring Boot static resources
cp -r build/* ../connect-frontend-with-backend/src/main/resources/static/
```

**3. Configure Spring Boot to Serve Frontend**

Create `WebConfig.java`:

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward all non-API routes to index.html for React Router
        registry.addViewController("/{spring:\\w+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:\\w+}")
                .setViewName("forward:/index.html");
    }
}
```

**4. Update Security Config**

```java
http
    .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/static/**", "/index.html", "/manifest.json", "/favicon.ico").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/tournaments/**").permitAll()
        // ... rest of your auth rules
    )
```

**5. Build and Deploy**

```bash
# Build the combined application
mvn clean package

# Deploy the JAR file to your server
scp target/connect-frontend-with-backend-0.0.1-SNAPSHOT.jar user@your-server:/opt/tournament-host/
```

#### Option B: Use NGINX Reverse Proxy

**1. Deploy Backend**

```bash
# On server
java -jar connect-frontend-with-backend-0.0.1-SNAPSHOT.jar
```

Backend runs on `http://localhost:8080`

**2. Deploy Frontend**

```bash
# Build React
npm run build

# Copy to server
scp -r build/* user@your-server:/var/www/tournament-host/
```

**3. Configure NGINX**

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Frontend - serve React static files
    location / {
        root /var/www/tournament-host;
        try_files $uri /index.html;
    }

    # Backend - proxy API requests
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**4. Enable HTTPS with Let's Encrypt**

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

---

## Production Environment Variables

### Backend Environment Variables

Required environment variables for production:

```env
# Database
DB_URL=jdbc:postgresql://your-db-host:5432/tournament_host
DB_USERNAME=your_db_user
DB_PASSWORD=your_secure_password

# JWT Security
JWT_SECRET_KEY=your_very_long_secret_key_here
JWT_EXPIRATION_TIME=3600000

# CORS
ALLOWED_ORIGINS=https://your-frontend.com,https://www.your-frontend.com

# Spring Profile
SPRING_PROFILES_ACTIVE=prod

# Server Configuration (optional)
SERVER_PORT=8080
```

### Frontend Environment Variables

```env
REACT_APP_API_URL=https://your-backend-domain.com/api
```

---

## Database Setup

### Option 1: Managed PostgreSQL (Recommended)

**Railway:**
- Automatically provisioned with backend
- Connection details in environment variables
- Automatic backups

**Render:**
- $7/month managed PostgreSQL
- Automatic backups
- Easy scaling

**AWS RDS:**
- Most scalable
- Complex pricing
- Best for enterprise

### Option 2: Self-Hosted PostgreSQL

**On Ubuntu/Debian:**

```bash
# Install PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Create database and user
sudo -u postgres psql
CREATE DATABASE tournament_host;
CREATE USER tournament_manager WITH ENCRYPTED PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE tournament_host TO tournament_manager;
\q

# Configure remote access (edit /etc/postgresql/14/main/postgresql.conf)
listen_addresses = '*'

# Allow remote connections (edit /etc/postgresql/14/main/pg_hba.conf)
host    all             all             0.0.0.0/0               md5

# Restart PostgreSQL
sudo systemctl restart postgresql

# Update firewall
sudo ufw allow 5432/tcp
```

**Security Warning:** Only allow specific IPs in production, not 0.0.0.0/0

---

## Domain and SSL

### Custom Domain Setup

**1. Purchase Domain**
- Namecheap, Google Domains, Cloudflare

**2. Point DNS to Your Server**

For Railway/Render/Vercel:
- Add CNAME record: `www` → `your-app.railway.app`
- Add A record or CNAME: `@` → provided IP or domain

For self-hosted:
- Add A record: `@` → `your-server-ip`
- Add A record: `www` → `your-server-ip`

**3. SSL Certificate**

Railway/Render/Vercel: Automatic

Self-hosted with Let's Encrypt:
```bash
sudo certbot --nginx -d your-domain.com -d www.your-domain.com
```

---

## Complete Example: Railway + Vercel Deployment

### Step-by-Step

**1. Backend on Railway**

```bash
# In backend directory
cd connect-frontend-with-backend

# Install Railway CLI
npm i -g @railway/cli

# Login and initialize
railway login
railway init

# Create PostgreSQL database via dashboard
# Set environment variables via dashboard

# Deploy
railway up
```

**Backend URL:** `https://tournament-host-production.up.railway.app`

**2. Frontend on Vercel**

```bash
# In frontend directory
cd tournament-frontend

# Create .env.production
echo "REACT_APP_API_URL=https://tournament-host-production.up.railway.app/api" > .env.production

# Update API client to use environment variable
# (see code example above)

# Install Vercel CLI
npm i -g vercel

# Login and deploy
vercel login
vercel --prod
```

**Frontend URL:** `https://tournament-host.vercel.app`

**3. Update CORS in Railway**

Set `ALLOWED_ORIGINS` environment variable:
```
https://tournament-host.vercel.app
```

**4. Test**

Visit `https://tournament-host.vercel.app` and verify:
- Frontend loads correctly
- API calls work
- Authentication works
- CORS is configured properly

---

## Troubleshooting

### CORS Issues

**Symptom:** "CORS policy: No 'Access-Control-Allow-Origin' header"

**Fix:**
1. Verify `ALLOWED_ORIGINS` includes your frontend URL
2. Check SecurityConfig.java CORS configuration
3. Restart backend after changing environment variables

### Database Connection Failed

**Symptom:** "Unable to connect to database"

**Fix:**
1. Verify `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` are correct
2. Check database is running and accessible
3. Verify firewall allows connection to database port
4. For Railway: Use provided `DATABASE_URL` variable

### Build Failures

**Backend:**
```bash
# Skip tests if they fail
mvn clean package -DskipTests
```

**Frontend:**
```bash
# Clear cache and rebuild
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Environment Variables Not Loading

**Railway/Render:**
- Redeploy after changing variables
- Variables are injected at runtime

**Self-hosted:**
- Verify .env file exists and is readable
- Check spring-dotenv dependency is included
- Use `export VAR_NAME=value` for shell environment

---

## Cost Estimates

### Free Tier Option
- **Backend:** Railway free tier (500 hours/month)
- **Database:** Railway PostgreSQL free tier (shared)
- **Frontend:** Vercel free tier
- **Total:** $0/month (with limitations)

### Starter Production
- **Backend:** Railway Hobby ($5/month)
- **Database:** Railway PostgreSQL ($5/month)
- **Frontend:** Vercel Pro ($20/month) or Free tier
- **Total:** $10-30/month

### Enterprise Option
- **Backend:** AWS EC2 t3.medium ($30/month)
- **Database:** AWS RDS PostgreSQL ($15-50/month)
- **Frontend:** AWS S3 + CloudFront ($5-20/month)
- **Total:** $50-100/month

---

## Next Steps

1. Choose deployment strategy (Railway + Vercel recommended for beginners)
2. Set up production database
3. Configure environment variables
4. Deploy backend
5. Deploy frontend
6. Configure custom domain (optional)
7. Set up monitoring and logging
8. Configure backups

For questions or issues, refer to:
- Railway docs: https://docs.railway.app
- Vercel docs: https://vercel.com/docs
- Spring Boot deployment: https://spring.io/guides/gs/spring-boot/
