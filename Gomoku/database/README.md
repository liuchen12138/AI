# 五子棋在线对弈平台数据库部署说明

## 数据库要求
- PostgreSQL 12+
- 推荐使用PostgreSQL 14或更高版本

## 初始化步骤

### 1. 安装PostgreSQL
请根据操作系统安装PostgreSQL数据库：
- Windows: https://www.postgresql.org/download/windows/
- macOS: `brew install postgresql`
- Linux: 参考发行版包管理器

### 2. 创建数据库
```bash
# 登录PostgreSQL
psql -U postgres

# 创建数据库
CREATE DATABASE gomoku_db WITH ENCODING 'UTF8';

# 退出
\q
```

### 3. 执行初始化脚本
```bash
# 执行SQL初始化脚本
psql -U postgres -d gomoku_db -f init.sql
```

### 4. 验证数据库
```bash
# 连接到数据库
psql -U postgres -d gomoku_db

# 查看表
\dt

# 查看表结构
\d user
\d game
\d game_move
\d ranking_snapshot

# 退出
\q
```

## 配置后端连接
修改后端配置文件 `backend/src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gomoku_db
spring.datasource.username=postgres
spring.datasource.password=你的数据库密码
```

## 注意事项
1. 生产环境请修改默认密码
2. 建议定期备份数据库
3. 索引已优化，无需额外创建
4. 触发器会自动更新用户表的updated_at字段
