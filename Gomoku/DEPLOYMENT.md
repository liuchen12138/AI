# 五子棋在线对弈平台 - 部署指南

## 环境要求

### 后端环境
- **JDK**: 17 或更高版本
- **Maven**: 3.6+ 
- **PostgreSQL**: 12+ （推荐14+）
- **Redis**: 6+ （推荐7+）

### 前端环境
- **Node.js**: 16+ （推荐18+）
- **npm**: 8+ 或 **yarn**: 1.22+

## 快速部署

### 第一步：数据库准备

#### 1.1 安装PostgreSQL

**Windows:**
```powershell
# 下载并安装PostgreSQL
# https://www.postgresql.org/download/windows/
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**MacOS:**
```bash
brew install postgresql@14
brew services start postgresql@14
```

#### 1.2 创建数据库和表

```bash
# 登录PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE gomoku_db WITH ENCODING 'UTF8';

# 退出
\q

# 执行初始化脚本
psql -U postgres -d gomoku_db -f database/init.sql
```

#### 1.3 验证数据库

```bash
psql -U postgres -d gomoku_db

# 查看表
\dt

# 查看用户表结构
\d user

# 退出
\q
```

### 第二步：Redis准备

#### 2.1 安装Redis

**Windows:**
```powershell
# 下载Redis for Windows
# https://github.com/tporadowski/redis/releases
# 或使用WSL安装Linux版本
```

**Linux:**
```bash
sudo apt install redis-server
sudo systemctl start redis
sudo systemctl enable redis
```

**MacOS:**
```bash
brew install redis
brew services start redis
```

#### 2.2 验证Redis

```bash
redis-cli ping
# 应返回: PONG
```

### 第三步：后端部署

#### 3.1 配置后端

编辑 `backend/src/main/resources/application.properties`:

```properties
# 数据库配置（修改为你的数据库信息）
spring.datasource.url=jdbc:postgresql://localhost:5432/gomoku_db
spring.datasource.username=postgres
spring.datasource.password=你的数据库密码

# Redis配置（修改为你的Redis信息）
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=你的Redis密码（如果有）

# JWT密钥（生产环境必须修改）
jwt.secret=你的超长密钥字符串-请务必修改-至少64位
jwt.expiration=86400000
```

#### 3.2 编译和运行后端

```bash
# 进入后端目录
cd backend

# 清理和编译
mvn clean install

# 运行应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/gomoku-backend-1.0.0.jar
```

后端服务将在 `http://localhost:8080` 启动

#### 3.3 验证后端

```bash
# 测试健康检查（需要先实现health endpoint）
curl http://localhost:8080/api/ranking/score

# 或在浏览器访问
http://localhost:8080/api/ranking/score
```

### 第四步：前端部署

#### 4.1 安装依赖

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
# 或使用yarn
yarn install
```

#### 4.2 开发模式运行

```bash
# 启动开发服务器
npm run dev
# 或
yarn dev
```

前端将在 `http://localhost:5173` 启动

#### 4.3 生产构建

```bash
# 构建生产版本
npm run build
# 或
yarn build

# 预览生产构建
npm run preview
# 或
yarn preview
```

构建产物在 `frontend/dist` 目录

### 第五步：访问应用

1. 打开浏览器访问: `http://localhost:5173`
2. 注册新账号或使用测试账号登录
3. 进入大厅选择游戏模式

## 生产环境部署

### 使用Nginx部署前端

#### 1. 安装Nginx

```bash
# Ubuntu/Debian
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx
```

#### 2. 配置Nginx

创建配置文件 `/etc/nginx/sites-available/gomoku`:

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    root /var/www/gomoku/frontend/dist;
    index index.html;
    
    # 处理前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 代理后端API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 代理WebSocket
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

#### 3. 启用配置

```bash
sudo ln -s /etc/nginx/sites-available/gomoku /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 使用systemd管理后端服务

创建服务文件 `/etc/systemd/system/gomoku-backend.service`:

```ini
[Unit]
Description=Gomoku Backend Service
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=gomoku
WorkingDirectory=/opt/gomoku/backend
ExecStart=/usr/bin/java -jar /opt/gomoku/backend/gomoku-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务:

```bash
sudo systemctl daemon-reload
sudo systemctl enable gomoku-backend
sudo systemctl start gomoku-backend
sudo systemctl status gomoku-backend
```

### SSL证书配置（推荐）

使用Let's Encrypt免费证书:

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

## 故障排查

### 后端无法启动

**问题：数据库连接失败**
```bash
# 检查PostgreSQL是否运行
sudo systemctl status postgresql

# 检查数据库是否存在
psql -U postgres -l | grep gomoku_db

# 检查连接配置
cat backend/src/main/resources/application.properties
```

**问题：Redis连接失败**
```bash
# 检查Redis是否运行
redis-cli ping

# 检查Redis配置
cat /etc/redis/redis.conf
```

### 前端无法连接后端

**问题：CORS错误**
- 检查后端CORS配置
- 确认前端API地址配置正确

**问题：401未授权**
- 检查JWT token是否有效
- 确认请求头包含Authorization

### 数据库问题

**问题：表不存在**
```bash
# 重新执行初始化脚本
psql -U postgres -d gomoku_db -f database/init.sql
```

**问题：权限错误**
```sql
-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE gomoku_db TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
```

## 性能优化建议

### 数据库优化
1. 定期执行 `VACUUM` 和 `ANALYZE`
2. 监控慢查询日志
3. 根据实际情况调整连接池大小

### Redis优化
1. 配置合适的内存限制
2. 启用持久化（RDB或AOF）
3. 监控内存使用情况

### 应用优化
1. 启用JVM参数优化
2. 配置日志级别（生产环境使用INFO或WARN）
3. 使用连接池

## 监控和日志

### 应用日志

后端日志位置:
```bash
tail -f backend/logs/spring.log
```

### 数据库日志

PostgreSQL日志:
```bash
tail -f /var/log/postgresql/postgresql-14-main.log
```

### Redis日志

```bash
tail -f /var/log/redis/redis-server.log
```

## 备份策略

### 数据库备份

```bash
# 每日备份脚本
#!/bin/bash
BACKUP_DIR="/backup/gomoku"
DATE=$(date +%Y%m%d)
pg_dump -U postgres gomoku_db > $BACKUP_DIR/gomoku_db_$DATE.sql
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

### Redis备份

```bash
# 备份RDB文件
cp /var/lib/redis/dump.rdb /backup/redis/dump_$(date +%Y%m%d).rdb
```

## 安全建议

1. **修改默认密码**: 数据库、Redis密码
2. **使用HTTPS**: 配置SSL证书
3. **防火墙配置**: 只开放必要端口
4. **定期更新**: 保持依赖库最新
5. **JWT密钥**: 使用强随机密钥
6. **限流保护**: 配置API限流
7. **数据加密**: 敏感数据加密存储

## 联系支持

如遇到部署问题，请提交Issue到项目仓库或联系技术支持。
# 五子棋在线对弈平台 - 部署指南

## 环境要求

### 后端环境
- **JDK**: 17 或更高版本
- **Maven**: 3.6+ 
- **PostgreSQL**: 12+ （推荐14+）
- **Redis**: 6+ （推荐7+）

### 前端环境
- **Node.js**: 16+ （推荐18+）
- **npm**: 8+ 或 **yarn**: 1.22+

## 快速部署

### 第一步：数据库准备

#### 1.1 安装PostgreSQL

**Windows:**
```powershell
# 下载并安装PostgreSQL
# https://www.postgresql.org/download/windows/
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
```

**MacOS:**
```bash
brew install postgresql@14
brew services start postgresql@14
```

#### 1.2 创建数据库和表

```bash
# 登录PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE gomoku_db WITH ENCODING 'UTF8';

# 退出
\q

# 执行初始化脚本
psql -U postgres -d gomoku_db -f database/init.sql
```

#### 1.3 验证数据库

```bash
psql -U postgres -d gomoku_db

# 查看表
\dt

# 查看用户表结构
\d user

# 退出
\q
```

### 第二步：Redis准备

#### 2.1 安装Redis

**Windows:**
```powershell
# 下载Redis for Windows
# https://github.com/tporadowski/redis/releases
# 或使用WSL安装Linux版本
```

**Linux:**
```bash
sudo apt install redis-server
sudo systemctl start redis
sudo systemctl enable redis
```

**MacOS:**
```bash
brew install redis
brew services start redis
```

#### 2.2 验证Redis

```bash
redis-cli ping
# 应返回: PONG
```

### 第三步：后端部署

#### 3.1 配置后端

编辑 `backend/src/main/resources/application.properties`:

```properties
# 数据库配置（修改为你的数据库信息）
spring.datasource.url=jdbc:postgresql://localhost:5432/gomoku_db
spring.datasource.username=postgres
spring.datasource.password=你的数据库密码

# Redis配置（修改为你的Redis信息）
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=你的Redis密码（如果有）

# JWT密钥（生产环境必须修改）
jwt.secret=你的超长密钥字符串-请务必修改-至少64位
jwt.expiration=86400000
```

#### 3.2 编译和运行后端

```bash
# 进入后端目录
cd backend

# 清理和编译
mvn clean install

# 运行应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/gomoku-backend-1.0.0.jar
```

后端服务将在 `http://localhost:8080` 启动

#### 3.3 验证后端

```bash
# 测试健康检查（需要先实现health endpoint）
curl http://localhost:8080/api/ranking/score

# 或在浏览器访问
http://localhost:8080/api/ranking/score
```

### 第四步：前端部署

#### 4.1 安装依赖

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
# 或使用yarn
yarn install
```

#### 4.2 开发模式运行

```bash
# 启动开发服务器
npm run dev
# 或
yarn dev
```

前端将在 `http://localhost:5173` 启动

#### 4.3 生产构建

```bash
# 构建生产版本
npm run build
# 或
yarn build

# 预览生产构建
npm run preview
# 或
yarn preview
```

构建产物在 `frontend/dist` 目录

### 第五步：访问应用

1. 打开浏览器访问: `http://localhost:5173`
2. 注册新账号或使用测试账号登录
3. 进入大厅选择游戏模式

## 生产环境部署

### 使用Nginx部署前端

#### 1. 安装Nginx

```bash
# Ubuntu/Debian
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx
```

#### 2. 配置Nginx

创建配置文件 `/etc/nginx/sites-available/gomoku`:

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    root /var/www/gomoku/frontend/dist;
    index index.html;
    
    # 处理前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    # 代理后端API
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
    
    # 代理WebSocket
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
    }
}
```

#### 3. 启用配置

```bash
sudo ln -s /etc/nginx/sites-available/gomoku /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

### 使用systemd管理后端服务

创建服务文件 `/etc/systemd/system/gomoku-backend.service`:

```ini
[Unit]
Description=Gomoku Backend Service
After=network.target postgresql.service redis.service

[Service]
Type=simple
User=gomoku
WorkingDirectory=/opt/gomoku/backend
ExecStart=/usr/bin/java -jar /opt/gomoku/backend/gomoku-backend-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启用服务:

```bash
sudo systemctl daemon-reload
sudo systemctl enable gomoku-backend
sudo systemctl start gomoku-backend
sudo systemctl status gomoku-backend
```

### SSL证书配置（推荐）

使用Let's Encrypt免费证书:

```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

## 故障排查

### 后端无法启动

**问题：数据库连接失败**
```bash
# 检查PostgreSQL是否运行
sudo systemctl status postgresql

# 检查数据库是否存在
psql -U postgres -l | grep gomoku_db

# 检查连接配置
cat backend/src/main/resources/application.properties
```

**问题：Redis连接失败**
```bash
# 检查Redis是否运行
redis-cli ping

# 检查Redis配置
cat /etc/redis/redis.conf
```

### 前端无法连接后端

**问题：CORS错误**
- 检查后端CORS配置
- 确认前端API地址配置正确

**问题：401未授权**
- 检查JWT token是否有效
- 确认请求头包含Authorization

### 数据库问题

**问题：表不存在**
```bash
# 重新执行初始化脚本
psql -U postgres -d gomoku_db -f database/init.sql
```

**问题：权限错误**
```sql
-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE gomoku_db TO postgres;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO postgres;
```

## 性能优化建议

### 数据库优化
1. 定期执行 `VACUUM` 和 `ANALYZE`
2. 监控慢查询日志
3. 根据实际情况调整连接池大小

### Redis优化
1. 配置合适的内存限制
2. 启用持久化（RDB或AOF）
3. 监控内存使用情况

### 应用优化
1. 启用JVM参数优化
2. 配置日志级别（生产环境使用INFO或WARN）
3. 使用连接池

## 监控和日志

### 应用日志

后端日志位置:
```bash
tail -f backend/logs/spring.log
```

### 数据库日志

PostgreSQL日志:
```bash
tail -f /var/log/postgresql/postgresql-14-main.log
```

### Redis日志

```bash
tail -f /var/log/redis/redis-server.log
```

## 备份策略

### 数据库备份

```bash
# 每日备份脚本
#!/bin/bash
BACKUP_DIR="/backup/gomoku"
DATE=$(date +%Y%m%d)
pg_dump -U postgres gomoku_db > $BACKUP_DIR/gomoku_db_$DATE.sql
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

### Redis备份

```bash
# 备份RDB文件
cp /var/lib/redis/dump.rdb /backup/redis/dump_$(date +%Y%m%d).rdb
```

## 安全建议

1. **修改默认密码**: 数据库、Redis密码
2. **使用HTTPS**: 配置SSL证书
3. **防火墙配置**: 只开放必要端口
4. **定期更新**: 保持依赖库最新
5. **JWT密钥**: 使用强随机密钥
6. **限流保护**: 配置API限流
7. **数据加密**: 敏感数据加密存储

## 联系支持

如遇到部署问题，请提交Issue到项目仓库或联系技术支持。
