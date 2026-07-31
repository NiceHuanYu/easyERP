# EasyERP — 轻量级企业资源管理系统

**[English](README.md)**

[![License: AGPL-3.0](https://img.shields.io/badge/License-AGPL--3.0-blue.svg)](LICENSE)
[![Java 17](https://img.shields.io/badge/Java-17-orange)](https://adoptium.net/)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.2-green)](https://spring.io/)
[![Vue 3](https://img.shields.io/badge/Vue-3-brightgreen)](https://vuejs.org/)
[![Nuxt 3](https://img.shields.io/badge/Nuxt-3-00dc82)](https://nuxt.com/)

## 1. 项目简介

EasyERP 是一款轻量级、开源的企业资源管理系统，面向中小型制造和贸易企业。覆盖采购、销售、生产、库存、财务全业务链路，界面简洁，开箱即用。

- **技术栈**：Spring Boot 3.2 + MyBatis-Plus + Sa-Token（后端），Vue 3 + Nuxt 3 + Element Plus + ECharts（前端）
- **数据库**：MySQL 8.4 或 PostgreSQL 16，Flyway 自动迁移，无需手动建表
- **开源协议**：[GNU AGPL-3.0](LICENSE) — 自由使用；若将修改版部署为网络服务，必须公开源代码

## 2. 快速开始

### 环境要求

| 组件 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 20+ |
| MySQL | 8.4+（或 PostgreSQL 16+） |
| Maven | 3.9+（含 `mvnw` 包装器） |

### 配置

1. **克隆项目**
   ```bash
   git clone https://github.com/hubert/easyERP.git
   cd easyERP
   ```

2. **创建数据库**

   **MySQL（默认）**：
   ```sql
   CREATE DATABASE EasyERP CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
   **PostgreSQL**：
   ```sql
   CREATE DATABASE "EasyERP";
   ```

3. **修改数据库连接** — 编辑 `backend/src/main/resources/application-mysql.yml`（或 `application-postgres.yml`）：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/EasyERP?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
       username: root
       password: 你的密码
   ```

4. **端口配置** — 默认端口（如需修改）：
   - 后端：`application.yml` → `server.port`（默认 `8080`）
   - 前端：`frontend/.env` → `NUXT_PUBLIC_API_BASE`（默认 `http://localhost:8080`）

### 启动后端

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

或在 IntelliJ IDEA 中设置 active profile 为 `mysql`，运行 `BackendApplication`。

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:3000`。

### 默认登录

| 字段 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | `admin123` |

> 首次启动时 `DataInitializer` 会自动创建 admin 用户及初始化角色、权限。

## 3. Docker 一键部署

```bash
# MySQL + 后端
docker compose --profile mysql up -d

# PostgreSQL + 后端
docker compose --profile postgres up -d

# 全部（MySQL + 后端 + 前端）
docker compose --profile all up -d
```

**`.env` 配置项**：

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root 密码 |
| `MYSQL_PORT` | `3306` | MySQL 宿主机端口 |
| `PG_USER` / `PG_PASSWORD` | `postgres` | PostgreSQL 账号 |
| `PG_PORT` | `5432` | PostgreSQL 宿主机端口 |
| `BACKEND_PORT` | `8080` | 后端宿主机端口 |
| `FRONTEND_PORT` | `3000` | 前端宿主机端口 |
| `ADMIN_INIT_PASSWORD` | `admin123` | 初始 admin 密码 |
| `SPRING_PROFILES_ACTIVE` | `docker` | `docker`（MySQL）或 `postgres` |
| `JAVA_OPTS` | （空） | 额外 JVM 参数，如 `-Xmx1g` |

## 4. 功能模块

| 模块 | 功能 |
|------|------|
| **系统管理** | 用户、角色、权限（RBAC）、数据字典 |
| **基础数据** | 物料、BOM、客户、供应商、仓库、员工、银行账户 |
| **销售管理** | 订单 → 发货 → 自动生成应收台账 |
| **采购管理** | 申请 → 订单 → 收货 → 自动生成应付台账 |
| **生产管理** | 工单 → 领料 → 完工入库 |
| **库存管理** | 库存查询、库存流水、调拨、CSV 导出 |
| **财务管理** | 收付款、应收应付核销、银行账户管理 |
| **工作台** | 统计卡片、销售额趋势、库存与订单状态图表 |

## 5. 项目结构

```
easyERP/
├── backend/                       # Spring Boot 3.2
│   └── src/main/java/.../
│       ├── common/                # 基类、统一响应、异常处理、Jackson/Sa-Token 配置
│       ├── modules/
│       │   ├── base/              # 基础数据（物料/BOM/客户/供应商/仓库/员工/银行账户）
│       │   ├── sales/             # 销售订单、发货
│       │   ├── purchase/          # 采购申请、订单、收货
│       │   ├── production/        # 生产工单、领料、完工
│       │   ├── inventory/         # 库存、流水、调拨
│       │   ├── finance/           # 收付款、应付、应收
│       │   ├── system/            # 用户、角色、权限、字典、认证
│       │   └── dashboard/         # 工作台统计 API
│       └── resources/
│           └── db/migration/      # Flyway 迁移（MySQL & PostgreSQL 各一份）
│
├── frontend/                      # Nuxt 3 + Element Plus
│   └── app/
│       ├── pages/                 # 文件路由
│       │   ├── base-data/         # 物料、BOM、客户、供应商、仓库、员工
│       │   ├── sales/orders/      # 销售订单列表/创建/详情
│       │   ├── sales/deliveries/  # 销售发货
│       │   ├── purchase/          # 采购申请、订单、收货
│       │   ├── production/        # 生产工单、领料、完工
│       │   ├── inventory/         # 库存查询、流水、调拨
│       │   ├── finance/           # 应付、应收、收付款、银行账户
│       │   ├── system/            # 用户、角色、字典
│       │   ├── dashboard.vue      # 工作台
│       │   └── login.vue          # 登录
│       ├── composables/           # useApi（HTTP 客户端）
│       ├── layouts/               # 侧边栏布局
│       └── stores/                # Pinia 状态管理（auth, dict）
│
├── docker-compose.yml             # Docker 编排
├── .env                           # Docker 环境变量（不提交）
├── LICENSE                        # AGPL-3.0
├── README.md                      # 英文版
└── README_CN.md                   # 本文件
```

## 6. 数据库

- Flyway 在应用启动时自动执行，无需手动导入 SQL
- 初始化数据包括：
  - 1 个 admin 用户（默认密码 `admin123`）
  - 7 个角色（管理员、销售、采购、仓库、生产、财务、普通用户）
  - 完整权限树（覆盖所有模块）
  - 3 个数据字典（物料分类、单据状态、库存变动类型）
- 迁移已合并为 **V1**（全部 DDL）+ **V2**（全部种子数据），MySQL 和 PostgreSQL 各一份

## 7. API 文档

- **Knife4j**（仅 dev 环境）：`http://localhost:8080/doc.html`
- **认证方式**：Sa-Token。调用 `POST /api/v1/auth/login` 传入 `{ "username": "admin", "password": "admin123" }` 获取 token，后续请求携带 `Authorization: Bearer <token>` 头
- **响应格式**：所有接口返回 `{ "code": 200, "message": "...", "data": ... }`

## 8. 开发规范

### 权限码命名

格式：`模块:资源:操作`

| 示例 | 含义 |
|------|------|
| `sales:order:view` | 查看销售订单 |
| `sales:order:create` | 新建销售订单 |
| `sales:order:edit` | 编辑销售订单 |
| `sales:order:delete` | 删除销售订单 |
| `sales:order:approve` | 审核/反审核 |
| `finance:bank-account:view` | 查看银行账户 |
| `inventory:stock:export` | 导出库存 CSV |

### 单据状态流转

```
销售订单：草稿 → 已提交 → 已审核 → 已发货 → 已关闭
采购申请：草稿 → 已提交 → 已审核 → 已下单
采购订单：草稿 → 已审核 → （收货）→ 已完成
生产工单：草稿 → 已下达 → （领料）→ （完工）→ 已完成
库存调拨：草稿 → 已确认
收付款：  草稿 → 已确认
```

### 新增模块步骤

1. 创建实体 → `modules/xxx/entity/Xxx.java`（继承 `BaseEntity`）
2. 创建 Mapper → `modules/xxx/mapper/XxxMapper.java`（继承 `BaseMapper<Xxx>`）
3. 创建 Controller → `modules/xxx/controller/XxxController.java`（注解 `@SaCheckPermission`）
4. 如需新表，创建 Flyway 迁移（线上环境；本地可直接更新 `V1__init_schema.sql`）
5. 创建前端页面 → `app/pages/xxx/index.vue`（Nuxt 文件路由自动注册）
6. 在 `app/layouts/default.vue` 添加菜单项

## 9. 常见问题

### Flyway checksum 不匹配
如果修改了已执行的迁移文件，Flyway 会拒绝启动。修复方案：
```sql
DELETE FROM flyway_schema_history WHERE version = 'X';
```
然后重启应用。Flyway 会重新执行修正后的迁移。

### 端口被占用
修改以下文件中的端口：
- `backend/src/main/resources/application.yml` → `server.port`
- `frontend/.env` → `NUXT_PUBLIC_API_BASE`
- Docker 部署：`.env` → `BACKEND_PORT` / `FRONTEND_PORT`

### 浏览器提示跨域错误
确保前端 `.env` 中 `NUXT_PUBLIC_API_BASE` 与后端地址一致。开发环境下 `CorsConfig` 默认放行所有来源。

### 前端页面提示"无权限"
菜单树按用户角色权限过滤。如果侧边栏缺少某页面，检查：
1. 该角色是否拥有对应的 `xxx:view` 权限码
2. 重新登录刷新权限缓存
