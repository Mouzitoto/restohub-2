# Resto-Hub

Restaurant management system with client and admin interfaces.

## Prerequisites

- Docker Desktop (BuildKit is enabled by default in Docker Desktop)
- Git

## Quick Start

1. Clone the repository:
```bash
git clone <repository-url>
cd resto-hub
```

2. **Настройте локальные домены** (один раз):

**Windows:**
```powershell
# Запустите PowerShell от имени администратора
.\setup-hosts.ps1
```

**Linux/macOS:**
```bash
sudo ./setup-hosts.sh
```

**Ручная настройка:**
Отредактируйте файл hosts и добавьте:
```
127.0.0.1 restohub.local
127.0.0.1 partner.restohub.local
127.0.0.1 api.restohub.local
```

- Windows: `C:\Windows\System32\drivers\etc\hosts`
- Linux/macOS: `/etc/hosts`

3. Start all services:

**Option 1: Start all services at once (logs from all containers):**
```bash
docker compose up --build
```

**Option 2: Start services sequentially (see logs one by one):**
```powershell
.\start-containers.ps1
```

This script will start containers one by one, showing logs for each container sequentially. Press Ctrl+C to move to the next container.

4. **Откройте в браузере:**
- http://restohub.local - Клиентское приложение
- http://partner.restohub.local - Админ-панель
- http://api.restohub.local - API endpoints

That's it! All services will be built and started automatically.

**Note:** On the first run, Docker will:
- Download base images (Maven, Node.js, PostgreSQL, nginx, etc.)
- Download all Maven and npm dependencies
- Build all projects (Spring Boot apps and React apps)
- Create and start all containers

This may take 5-10 minutes depending on your internet connection. Subsequent builds will be much faster thanks to Docker layer caching.

## Services

- **PostgreSQL** - Database (port 5432)
- **admin-api** - Admin API (port 8082)
- **client-api** - Client API (port 8081)
- **admin-web** - Admin web interface
- **client-web** - Client web interface
- **nginx** - Reverse proxy (port 80)

## Local Domains

Проект настроен для работы через локальные домены без указания портов:

- `restohub.local` → Client web interface
- `partner.restohub.local` → Admin web interface
- `api.restohub.local/client-api` → Client API
- `api.restohub.local/admin-api` → Admin API

Все домены работают только на вашем компьютере и не доступны извне.

## Building

All projects are built automatically inside Docker containers. No need to run Maven or npm locally.

The build process:
- Downloads dependencies (cached between builds)
- Compiles source code
- Creates production artifacts
- Packages into Docker images

## Development

For local development, you can still build projects locally:

```powershell
.\build.ps1
```

Then run services individually or use Docker Compose.

## Environment Variables

All domain URLs are configurable through environment variables. You can override them by creating a `.env` file in the project root or by setting them in your environment.

### Frontend Build Variables (Vite)
These are embedded during build time:
- `VITE_API_BASE_URL` - API base URL (default: `http://api.restohub.local`)
- `VITE_PARTNER_DOMAIN` - Partner/admin domain (default: `http://partner.restohub.local`)

### Backend Runtime Variables
These can be changed at runtime:
- `ADMIN_WEB_URL` - Admin web interface URL for CORS (default: `http://partner.restohub.local`)
- `CLIENT_WEB_URL` - Client web interface URL for CORS (default: `http://restohub.local`)

### Nginx Domain Variables
These configure nginx server names:
- `CLIENT_DOMAIN` - Client domain name (default: `restohub.local`)
- `PARTNER_DOMAIN` - Partner/admin domain name (default: `partner.restohub.local`)
- `API_DOMAIN` - API domain name (default: `api.restohub.local`)

## Production Deployment

### Step 1: Create .env File

Create a `.env` file in the project root directory with your production domain values:

```env
# Frontend build-time variables (embedded in the build)
VITE_API_BASE_URL=https://api.restohub.kz
VITE_PARTNER_DOMAIN=https://partner.restohub.kz

# Backend runtime variables (for CORS configuration)
ADMIN_WEB_URL=https://partner.restohub.kz
CLIENT_WEB_URL=https://restohub.kz

# Nginx domain configuration
CLIENT_DOMAIN=restohub.kz
PARTNER_DOMAIN=partner.restohub.kz
API_DOMAIN=api.restohub.kz
```

**Important notes:**
- Use `https://` for frontend URLs (VITE_* variables) - they are embedded in the build
- Use `https://` for backend CORS URLs (ADMIN_WEB_URL, CLIENT_WEB_URL)
- Use only domain names (without protocol) for nginx variables (CLIENT_DOMAIN, PARTNER_DOMAIN, API_DOMAIN)

### Step 2: Configure DNS

Make sure your DNS records point to your server:

- `restohub.kz` → Your server IP
- `partner.restohub.kz` → Your server IP
- `api.restohub.kz` → Your server IP

### Step 3: Configure SSL/TLS

Before deploying, ensure you have SSL certificates for your domains. You can use:
- Let's Encrypt (free, recommended)
- Commercial SSL certificates

**For Let's Encrypt with nginx:**
1. Install certbot on your server
2. Generate certificates: `certbot certonly --nginx -d restohub.kz -d partner.restohub.kz -d api.restohub.kz`
3. Update nginx configuration to use SSL (you may need to modify nginx.conf.template or add SSL configuration)

### Step 4: Update docker-compose.yml for Production

You may need to adjust port mappings and add SSL configuration. For example, if you're using a reverse proxy (like Cloudflare or another nginx instance) in front of Docker, you might want to:

1. Remove port 80 mapping from nginx service (if using external reverse proxy)
2. Add SSL port mapping: `443:443`
3. Update nginx configuration to handle SSL termination

### Step 5: Build and Deploy

1. **Copy your project to the production server:**
   ```bash
   scp -r resto-hub user@your-server:/path/to/deployment/
   ```

2. **SSH into your server:**
   ```bash
   ssh user@your-server
   cd /path/to/deployment/resto-hub
   ```

3. **Create .env file with production values** (see Step 1)

4. **Build and start services:**
   ```bash
   docker compose up -d --build
   ```

5. **Verify services are running:**
   ```bash
   docker compose ps
   docker compose logs
   ```

### Step 6: Verify Deployment

Check that all services are accessible:

- https://restohub.kz - Client web interface
- https://partner.restohub.kz - Admin web interface
- https://api.restohub.kz/client-api - Client API
- https://api.restohub.kz/admin-api - Admin API

### Troubleshooting

**If services don't start:**
- Check logs: `docker compose logs [service-name]`
- Verify .env file is in the project root
- Ensure all required ports are available

**If domains don't resolve:**
- Verify DNS records are correct
- Check firewall settings
- Ensure SSL certificates are properly configured

**If CORS errors occur:**
- Verify ADMIN_WEB_URL and CLIENT_WEB_URL match your actual domains
- Check that URLs use `https://` protocol
- Restart backend services after changing CORS variables

**If frontend shows wrong API URLs:**
- Rebuild frontend containers: `docker compose build client-web admin-web`
- Restart services: `docker compose up -d`

### Security Checklist

Before going live:

- [ ] Change default database passwords
- [ ] Use strong JWT secrets
- [ ] Enable HTTPS/SSL
- [ ] Configure firewall rules
- [ ] Set up regular backups
- [ ] Configure log rotation
- [ ] Review and restrict exposed ports
- [ ] Set up monitoring and alerts

## Notes

- First build may take longer as dependencies are downloaded
- Subsequent builds use cached dependencies for faster builds
- Database migrations are applied automatically by admin-api on startup
- All services communicate through Docker network
- Frontend applications use environment variables for all domain URLs
- CORS is configured to allow requests from configured domains
- If port 80 is already in use, you may need to stop other services (e.g., IIS on Windows)

