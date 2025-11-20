# Database Migrations and Seed Data

This directory contains database migrations and seed data for the RestoHub project.

## Structure

- `liquibase/changelog/` - Liquibase changelog master files
- `liquibase/changesets/` - SQL migration files
- `seed/` - Seed data SQL scripts

## Migrations

Migrations are managed by Liquibase and are automatically applied when the admin-api service starts.

## Seed Data

Seed data can be run manually or through a separate Docker service.

