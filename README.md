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

2. Start all services:
```bash
docker compose up --build
```

That's it! All services will be built and started automatically.

**Note:** On the first run, Docker will:
- Download base images (Maven, Node.js, PostgreSQL, etc.)
- Download all Maven and npm dependencies
- Build all projects (Spring Boot apps and React apps)
- Create and start all containers

This may take 5-10 minutes depending on your internet connection. Subsequent builds will be much faster thanks to Docker layer caching.

## Services

- **PostgreSQL** - Database (port 5432)
- **admin-api** - Admin API (port 8082)
- **client-api** - Client API (port 8081)
- **admin-web** - Admin web interface (port 3001)
- **client-web** - Client web interface (port 3000)

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

## Notes

- First build may take longer as dependencies are downloaded
- Subsequent builds use cached dependencies for faster builds
- Database migrations are applied automatically by admin-api on startup

