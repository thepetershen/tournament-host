# Tournament Host Backend - Setup Guide

## 🔐 Environment Variables Setup

This project uses environment variables to store sensitive configuration like database passwords and JWT secrets.

### First Time Setup

1. **Copy the example environment file:**
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env` and replace placeholder values:**
   ```bash
   # Open in your editor
   nano .env
   # or
   code .env
   ```

3. **Generate a new JWT secret** (recommended):
   ```bash
   openssl rand -base64 64 | tr -d '\n'
   ```

   Copy the output and paste it as your `JWT_SECRET_KEY` in `.env`

4. **Update database credentials** if needed:
   - `DB_PASSWORD` - Your PostgreSQL password
   - `DB_USERNAME` - Your PostgreSQL username
   - `DB_URL` - Your database URL

### Important Security Notes

⚠️ **NEVER commit `.env` to git!**
- The `.env` file is already in `.gitignore`
- Only commit `.env.example` (without real secrets)
- Share real secrets securely (password manager, secure chat, etc.)

✅ **DO commit `.env.example`**
- This file shows the required variables
- Contains NO real secrets, only placeholders
- Helps team members set up their own `.env`

## 🚀 Running the Application

### Local Development

```bash
# Make sure you have a .env file with your secrets
mvn clean install
mvn spring-boot:run
```

The application will automatically load variables from `.env`

### Production Deployment

For production, use your hosting platform's environment variable system:

**Heroku:**
```bash
heroku config:set JWT_SECRET_KEY=your_secret_here
heroku config:set DB_PASSWORD=your_password_here
```

**AWS Elastic Beanstalk:**
- Set in Configuration → Software → Environment properties

**Docker:**
```bash
docker run -e JWT_SECRET_KEY=your_secret -e DB_PASSWORD=your_password ...
```

**Railway/Render:**
- Add in Dashboard → Environment Variables

## 📋 Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/tournament_host` |
| `DB_USERNAME` | Database username | `tournament_manager` |
| `DB_PASSWORD` | Database password | `your_secure_password` |
| `JWT_SECRET_KEY` | Secret key for JWT signing | `(64+ character random string)` |
| `JWT_EXPIRATION_TIME` | Token expiration in ms | `3600000` (1 hour) |
| `SERVER_PORT` | Application port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `http://localhost:5173` |

## 🔧 Troubleshooting

### "Could not resolve placeholder 'DB_PASSWORD'"

**Problem:** Spring can't find your environment variables

**Solution:**
1. Make sure `.env` file exists in project root
2. Check that `spring-dotenv` dependency is in `pom.xml`
3. Run `mvn clean install` to refresh dependencies
4. Restart your IDE

### "Invalid JWT signature"

**Problem:** JWT secret changed or mismatched

**Solution:**
1. Make sure all team members use the same `JWT_SECRET_KEY`
2. Clear browser localStorage/cookies
3. Generate new login token

## 🎯 Best Practices

1. **Rotate secrets regularly** - Change JWT secret every few months
2. **Use strong passwords** - At least 16 characters for DB password
3. **Different secrets per environment** - Dev, staging, and prod should have different secrets
4. **Document required variables** - Keep `.env.example` up to date
5. **Never log secrets** - Remove any `System.out.println()` of sensitive data

## 👥 Team Collaboration

When a new developer joins:

1. They clone the repository
2. They copy `.env.example` to `.env`
3. Team lead securely shares real secret values
4. They update their local `.env`
5. They run `mvn spring-boot:run`

**Never share secrets via:**
- ❌ Email
- ❌ Slack/Discord
- ❌ Git commits
- ❌ Screenshots

**Do share secrets via:**
- ✅ Password manager (1Password, LastPass)
- ✅ Encrypted file
- ✅ In-person/secure video call
- ✅ Secure secret management service (HashiCorp Vault)

## 🔒 Additional Security

For production deployments, consider:

1. **HashiCorp Vault** - Centralized secret management
2. **AWS Secrets Manager** - Cloud-based secret storage
3. **Azure Key Vault** - Microsoft's secret management
4. **Google Secret Manager** - Google Cloud secrets

These services provide:
- Secret rotation
- Audit logs
- Access control
- Encryption at rest
