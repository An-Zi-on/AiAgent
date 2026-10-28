# Common-Template-Project · agent

Spring Boot 4.1.1 + Spring AI 2.0.1 的通用后台模板 + 智能体后端。

## 环境要求

- JDK 17+（本机可用 21）
- Maven 3.9+
- MySQL 8

## 初始化数据库

1. 建库：

   ```sql
   CREATE DATABASE an_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. 导入表结构（仅空库执行，`sql/sql.sql` 里有 `DROP TABLE`）：

   ```bash
   mysql -uroot -p an_ai < sql/sql.sql
   ```

3. 导入种子管理员：

   ```bash
   mysql -uroot -p an_ai < sql/seed_admin.sql
   ```

> `sql/sql.sql` 是旧库 `an_project` 的导出，导入前确认目标库是 `an_ai`。

## 启动

```bash
mvn spring-boot:run
```

- 端口：`8123`
- 前缀：`/api`
- 接口文档：http://localhost:8123/api/swagger-ui/index.html

## 默认账号

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `12345678` | 超级管理员 |

首次登录后请立即修改密码；生产环境禁止使用默认密码。

## 配置说明

- `src/main/resources/application.yml` 是开发环境配置（MySQL、模型 Key）。
- 上线前把 API Key / DB 密码改为环境变量或密钥管理，见
  `docs/PM-评审与任务书-2026-10-26.md` 的「上线前 Gate」。
