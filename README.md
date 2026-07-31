# EasyERP — Lightweight Enterprise Resource Planning System

**[中文版](README_CN.md)**

[![License: AGPL-3.0](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2-green)](https://spring.io/)
[![Vue 3](https://img.shields.io/badge/Vue-3-brightgreen)](https://vuejs.org/)
[![Nuxt 3](https://img.shields.io/badge/Nuxt-3-00dc82)](https://nuxt.com/)

## 1. Introduction

EasyERP is a lightweight, open-source Enterprise Resource Planning system built for small and medium-sized manufacturing and trading companies. It covers the full business cycle — sales, purchasing, production, inventory, and finance — with a clean web interface.

- **Tech Stack**: Spring Boot 3.2 + MyBatis-Plus + Sa-Token (backend), Vue 3 + Nuxt 3 + Element Plus + ECharts (frontend)
- **Database**: MySQL 8.4 or PostgreSQL 16, auto-migrated via Flyway
- **License**: [GNU AGPL-3.0](LICENSE) — free for all uses. If you deploy a modified version as a network service, you must release the source code.

## 2. Quick Start

### Prerequisites

| Component | Version |
|-----------|---------|
| JDK | 17+ |
| Node.js | 20+ |
| MySQL | 8.4+ (or PostgreSQL 16+) |
| Maven | 3.9+ (included in `mvnw`) |

### Configuration

1. **Clone the repository**
   ```bash
   git clone https://github.com/hubert/easyERP.git
   cd easyERP
   ```

2. **Database setup**

   **MySQL (default)**:
   ```sql
   CREATE DATABASE EasyERP CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   **PostgreSQL**:
   ```sql
   CREATE DATABASE "EasyERP";
   ```

3. **Application configuration** — edit `backend/src/main/resources/application-mysql.yml` (or `application-postgres.yml`):
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/EasyERP?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
       username: root
       password: your_password
   ```

4. **Port configuration** — default ports (change if needed):
   - Backend: `application.yml` → `server.port` (default `8080`)
   - Frontend: `frontend/.env` → `NUXT_PUBLIC_API_BASE` (default `http://localhost:8080`)

### Run Backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Or import into IntelliJ IDEA, set active profile to `mysql`, and run `BackendApplication`.

### Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

### Default Login

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |

> The admin user and initial roles/permissions are auto-created on first startup by `DataInitializer`.

## 3. Docker Deployment

```bash
# MySQL + backend
docker compose --profile mysql up -d

# PostgreSQL + backend
docker compose --profile postgres up -d

# Full stack (MySQL + backend + frontend)
docker compose --profile all up -d
```

**`.env` reference**:

| Variable | Default | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root password |
| `MYSQL_PORT` | `3306` | MySQL host port |
| `PG_USER` / `PG_PASSWORD` | `postgres` | PostgreSQL credentials |
| `PG_PORT` | `5432` | PostgreSQL host port |
| `BACKEND_PORT` | `8080` | Backend host port |
| `FRONTEND_PORT` | `3000` | Frontend host port |
| `ADMIN_INIT_PASSWORD` | `admin123` | Initial admin password |
| `SPRING_PROFILES_ACTIVE` | `docker` | `docker` (MySQL) or `postgres` |
| `JAVA_OPTS` | (empty) | Extra JVM flags, e.g. `-Xmx1g` |

## 4. Modules

| Module | Key Features |
|--------|-------------|
| **System** | Users, Roles, Permissions (RBAC), Data Dictionary |
| **Master Data** | Materials, BOMs, Customers, Suppliers, Warehouses, Employees, Bank Accounts |
| **Sales** | Orders → Delivery → Auto-generates Accounts Receivable |
| **Purchase** | Requisitions → Orders → Receiving → Auto-generates Accounts Payable |
| **Production** | Work Orders → Material Picking → Finished Goods Receiving |
| **Inventory** | Stock Query, Transaction Log, Inter-warehouse Transfer, CSV Export |
| **Finance** | Payments/Receipts, AR/AP Reconcile, Bank Account Management |
| **Dashboard** | Stats Cards, Sales Trend (real data), Inventory & Order Status Distribution |

## 5. Project Structure

```
easyERP/
├── backend/                       # Spring Boot 3.2
│   └── src/main/java/.../
│       ├── common/                # Base entity, API response, exception handler, Jackson/Sa-Token config
│       ├── modules/
│       │   ├── base/              # Master data (Material, BOM, Customer, Supplier, Warehouse, Employee, BankAccount)
│       │   ├── sales/             # Sales order, delivery
│       │   ├── purchase/          # Purchase requisition, order, receiving
│       │   ├── production/        # Production order, picking, finishing
│       │   ├── inventory/         # Stock, transaction, transfer
│       │   ├── finance/           # Payment, payable, receivable
│       │   ├── system/            # User, role, permission, dict, auth
│       │   └── dashboard/         # Dashboard statistics API
│       └── resources/
│           └── db/migration/      # Flyway migrations (MySQL & PostgreSQL)
│               ├── mysql/         # V1 + V2 (schema + init data)
│               └── postgresql/    # V1 + V2 (schema + init data)
│
├── frontend/                      # Nuxt 3 + Element Plus
│   └── app/
│       ├── pages/                 # File-based routes
│       │   ├── base-data/         # Materials, BOMs, Customers, Suppliers, Warehouses, Employees
│       │   ├── sales/orders/      # Sales order list/create/detail
│       │   ├── sales/deliveries/  # Sales delivery list/create
│       │   ├── purchase/          # Requisition, order, receiving
│       │   ├── production/        # Orders, pickings, finishings
│       │   ├── inventory/         # Stock, transactions, transfers
│       │   ├── finance/           # Payables, receivables, payments, bank-accounts
│       │   ├── system/            # Users, roles, dicts
│       │   ├── dashboard.vue      # Dashboard
│       │   └── login.vue          # Login
│       ├── composables/           # useApi (HTTP client)
│       ├── layouts/               # Sidebar layout
│       └── stores/                # Pinia stores (auth, dict)
│
├── docker-compose.yml             # Docker orchestration
├── .env                           # Docker environment variables (not committed)
├── LICENSE                        # AGPL-3.0
├── README.md                      # This file
└── README_CN.md                   # Chinese version
```

## 6. Database

- Flyway automatically runs on startup. No manual SQL execution needed.
- Initial data includes:
  - 1 admin user (`admin` / `admin123` by default)
  - 7 roles (Admin, Sales, Purchase, Warehouse, Production, Finance, General)
  - Full permission tree covering all modules
  - 3 data dictionary types (material category, document status, inventory transaction type)
- Migrations are unified into **V1** (all DDL) + **V2** (all seed data) for both MySQL and PostgreSQL.

## 7. API Documentation

- **Knife4j** (dev profile only): `http://localhost:8080/doc.html`
- **Authentication**: Sa-Token. Call `POST /api/v1/auth/login` with `{ "username": "admin", "password": "admin123" }` to obtain a token, then attach `Authorization: Bearer <token>` to all subsequent requests.
- **Response format**: All endpoints return `{ "code": 200, "message": "...", "data": ... }`.

## 8. Development Guide

### Permission Codes

Format: `module:resource:action`

| Example | Meaning |
|---------|---------|
| `sales:order:view` | View sales order list |
| `sales:order:create` | Create sales order |
| `sales:order:edit` | Edit sales order |
| `sales:order:delete` | Delete sales order |
| `sales:order:approve` | Approve / un-approve |
| `finance:bank-account:view` | View bank accounts |
| `inventory:stock:export` | Export inventory CSV |

### Document Status Flow

```
Sales Order:  DRAFT → SUBMITTED → APPROVED → SHIPPED → CLOSED
Purchase Req: DRAFT → SUBMITTED → APPROVED → ORDERED
Purchase Ord: DRAFT → APPROVED → (receiving) → COMPLETED
Prod Order:   DRAFT → RELEASED → (picking) → (finishing) → COMPLETED
Transfer:     DRAFT → CONFIRMED
Payment:      DRAFT → CONFIRMED
```

### Adding a New Module

1. Create entity → `modules/xxx/entity/Xxx.java` (extends `BaseEntity`)
2. Create mapper → `modules/xxx/mapper/XxxMapper.java` (extends `BaseMapper<Xxx>`)
3. Create controller → `modules/xxx/controller/XxxController.java` (annotate `@SaCheckPermission`)
4. Create Flyway migration if new tables needed (only if DB is live; otherwise update `V1__init_schema.sql`)
5. Create frontend page → `app/pages/xxx/index.vue` (Nuxt file-based routing)
6. Add menu item in `app/layouts/default.vue`

## 9. FAQ

### Flyway checksum mismatch
If you edited an already-applied migration, Flyway will refuse to start. Fix:
```sql
DELETE FROM flyway_schema_history WHERE version = 'X';
```
Then restart the application. Flyway will re-apply the corrected migration.

### Port already in use
Change ports in:
- `backend/src/main/resources/application.yml` → `server.port`
- `frontend/.env` → `NUXT_PUBLIC_API_BASE`
- Docker: `docker-compose.yml` or `.env` → `BACKEND_PORT` / `FRONTEND_PORT`

### CORS errors in browser
Ensure `NUXT_PUBLIC_API_BASE` in the frontend `.env` matches the backend URL. The backend `CorsConfig` allows all origins in dev mode by default.

### Frontend page shows "permission denied"
The menu tree is filtered by the user's role permissions. If a page is missing from the sidebar, check:
1. The role has the corresponding `xxx:view` permission
2. Re-login to refresh the permission cache
