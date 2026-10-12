# RithickMart — Online Shoe Store

A shoe-shopping demo built with Java 17, Spring Boot, Maven, and MySQL. The frontend is served by Spring Boot.

## Features
- Shoe catalog with sneaker, sports, formal, walking, casual, and outdoor shoe examples.
- Search shoes by name or shoe type.
- Admin-only shoe create, update, and delete API.
- Buyer/seller registration, login/logout, role-based access, and BCrypt password hashing.
- Admin login history and user listing.

## Run locally (Windows)
1. Install JDK 17, Maven, and MySQL Server.
2. Run `database/rithickmart.sql` in MySQL, or allow the app's configured database user to create the database.
3. Set MySQL credentials in PowerShell if needed: `$env:DB_USERNAME="root"; $env:DB_PASSWORD="your_mysql_password"`.
4. From this folder, run `mvn spring-boot:run`.
5. Open http://localhost:8080.

The default database URL uses `rithickmart`. You can override it with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.

## Admin login
- Email: `admin@rithickmart.com`
- Password: `ChangeMe@123`

Change the demo password before using this app beyond local testing.

## API overview
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`, `POST /api/auth/logout`
- `GET /api/products`, `GET /api/products?q=sneakers`
- Admin only: `POST /api/products`, `PUT /api/products/{id}`, `DELETE /api/products/{id}`
- Admin only: `GET /api/admin/login-history`, `GET /api/admin/users`

## Notes
This is a starter storefront. Cart persistence, checkout/payment, order workflow, password reset, email verification, and production deployment hardening are not implemented.
