SimBlog17API - Testcontainers 与 Docker Compose 使用指南

本项目支持两种容器工作流：

- Testcontainers：用于测试和测试启动应用。
- Docker Compose：用于运行常规应用时的本地基础设施。

## 前置条件

- Docker daemon 已运行
- Java 25
- Maven wrapper（`mvnw`）
- 项目根目录存在 `.env` 文件

## 本项目中的 Compose 与 Testcontainers

- Testcontainers（`src/test/java/cn/chunana/simblog17api/TestcontainersConfiguration.java`）
    - 通过代码方式为测试启动 PostgreSQL + Redis。
    - Spring 会通过 `@ServiceConnection` 自动注入连接信息。
    - 在测试流程中生效（`@ActiveProfiles("test")` / `TestSimBlog17ApiApplication`）。
- Docker Compose（`compose.yaml`）
    - 以共享服务形式启动 PostgreSQL + Redis，供本地使用。
    - 常用于标准应用启动（`SimBlog17ApiApplication`）。
- 在 test profile 下，Compose 被禁用：`src/test/resources/application-test.properties` 中配置了
  `spring.docker.compose.enabled=false`。

## 共享的 .env 变量

两种流程都会读取以下核心变量：

- PostgreSQL：`POSTGRES_IMAGE`、`POSTGRES_DB`、`POSTGRES_USER`、`POSTGRES_PASSWORD`、`POSTGRES_PORT`
- Redis：`REDIS_IMAGE`、`REDIS_PASSWORD`、`REDIS_PORT`
- 应用安全：`APP_ADMIN_USERNAME`、`APP_ADMIN_PASSWORD`（`prod` 必填，且无默认回退）
- Encryptor：`APP_CRYPTO_AES_SECRET`、`APP_CRYPTO_XOR_PEPPER`（生产环境必须使用强随机值）

Compose 通过 `compose.yaml` 的变量插值使用这些键。
Testcontainers 通过 Spring `Environment` 读取这些值（`spring.config.import` 会导入 `.env`）。

### 生产环境启动前检查（必做）

- `SPRING_PROFILES_ACTIVE=prod`
- `APP_ADMIN_USERNAME` 已设置且非空
- `APP_ADMIN_PASSWORD` 已设置且非空
- `APP_CRYPTO_AES_SECRET`、`APP_CRYPTO_XOR_PEPPER` 已替换为强随机值

说明：当 `prod` profile 生效时，`SecurityConfig` 会强制校验 `APP_ADMIN_USERNAME` / `APP_ADMIN_PASSWORD`。
若缺失或为空，应用会在启动阶段直接失败（fail fast），不会使用默认账号或旧明文兼容逻辑。

### 快速生成 Encryptor 密钥（建议）

在项目根目录执行以下命令，将输出可直接填写到 `.env` 的随机值：

```bash
openssl rand -base64 48
openssl rand -base64 32
```

建议映射关系：

- 第一行输出填到 `APP_CRYPTO_AES_SECRET`
- 第二行输出填到 `APP_CRYPTO_XOR_PEPPER`

## 使用 Testcontainers 运行测试

```bash
cd /home/chunana/workspace/SimBlog17/SimBlog17API
sh mvnw test
```

## 使用 Compose 启动本地基础设施

```bash
cd /home/chunana/workspace/SimBlog17/SimBlog17API
docker compose up -d
```

停止 Compose 服务：

```bash
cd /home/chunana/workspace/SimBlog17/SimBlog17API
docker compose down
```

## 使用 Testcontainers 运行测试启动应用

运行 main class：

- `cn.chunana.simblog17api.TestSimBlog17ApiApplication`

该入口会强制启用 `test` profile，并通过 `TestcontainersConfiguration` 启动 PostgreSQL + Redis。

## 备注

- 测试 profile 配置：`src/test/resources/application-test.properties`
- Testcontainers 配置：`src/test/java/cn/chunana/simblog17api/TestcontainersConfiguration.java`
- Compose 配置：`compose.yaml`
- 标准应用入口：`src/main/java/cn/chunana/simblog17api/SimBlog17ApiApplication.java`
