# 五子棋在线对弈平台

这是一个基于Web的五子棋在线对弈平台，支持双人在线对弈和人机对战两种游戏模式。

## 技术栈

### 后端
- **框架**: Spring Boot 3.2.0
- **语言**: Java 17
- **数据库**: PostgreSQL
- **缓存**: Redis
- **实时通信**: WebSocket
- **认证**: JWT (JSON Web Token)
- **构建工具**: Maven

### 前端
- **框架**: Vue.js 3
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router
- **UI组件**: 自定义组件

## 项目结构

```
Gomoku/
├── backend/                    # 后端项目
│   ├── src/
│   │   └── main/
│   │       ├── java/com/gomoku/
│   │       │   ├── GomokuApplication.java    # 主应用入口
│   │       │   ├── entity/                   # 实体类
│   │       │   │   ├── User.java
│   │       │   │   ├── Game.java
│   │       │   │   └── GameMove.java
│   │       │   ├── repository/               # 数据访问层
│   │       │   │   ├── UserRepository.java
│   │       │   │   ├── GameRepository.java
│   │       │   │   └── GameMoveRepository.java
│   │       │   ├── dto/                      # 数据传输对象
│   │       │   │   ├── UserDTO.java
│   │       │   │   ├── LoginRequest.java
│   │       │   │   ├── RegisterRequest.java
│   │       │   │   ├── AuthResponse.java
│   │       │   │   └── ApiResponse.java
│   │       │   ├── enums/                    # 枚举类
│   │       │   │   ├── GameMode.java
│   │       │   │   ├── GameStatus.java
│   │       │   │   ├── AIDifficulty.java
│   │       │   │   ├── EndReason.java
│   │       │   │   └── PieceColor.java
│   │       │   └── util/                     # 工具类
│   │       │       └── JwtUtil.java
│   │       └── resources/
│   │           └── application.properties    # 配置文件
│   └── pom.xml                              # Maven配置
├── frontend/                   # 前端项目（待创建）
├── database/                   # 数据库脚本
│   ├── init.sql               # 初始化SQL脚本
│   └── README.md              # 数据库部署说明
└── README.md                  # 项目说明文档

```

## 核心功能

### 1. 用户系统
- ✅ 用户注册与登录
- ✅ JWT令牌认证
- ✅ 用户信息管理
- ✅ 等级积分系统

### 2. 游戏模式
- 🚧 双人在线对弈（PVP）
  - 按等级匹配
  - 实时对局
  - 断线重连
- 🚧 人机对战（PVE）
  - 简单难度AI
  - 中等难度AI
  - 困难难度AI

### 3. 对局管理
- ✅ 对局状态管理
- 🚧 落子验证
- 🚧 胜负判定
- ✅ 对局历史记录
- ✅ 棋谱保存

### 4. 排行榜系统
- 🚧 积分排行榜
- 🚧 胜率排行榜
- 🚧 等级排行榜
- 🚧 Redis缓存

## 快速开始

### 前置要求
- JDK 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+
- Node.js 16+ (前端开发)

### 后端启动

1. **配置数据库**
   ```bash
   # 创建数据库
   createdb gomoku_db
   
   # 执行初始化脚本
   psql -U postgres -d gomoku_db -f database/init.sql
   ```

2. **配置application.properties**
   ```properties
   # 修改数据库连接信息
   spring.datasource.url=jdbc:postgresql://localhost:5432/gomoku_db
   spring.datasource.username=你的用户名
   spring.datasource.password=你的密码
   
   # 修改Redis连接信息
   spring.data.redis.host=localhost
   spring.data.redis.port=6379
   ```

3. **编译和运行**
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```

4. **访问API**
   - 后端服务运行在: http://localhost:8080
   - API文档: http://localhost:8080/swagger-ui.html (待配置)

### 前端启动

```bash
# 待实现
cd frontend
npm install
npm run dev
```

## 数据库设计

### 核心表结构

1. **用户表 (user)**
   - 用户ID、用户名、密码哈希、邮箱
   - 等级、积分
   - 对局统计（总数、胜/负/平）

2. **对局表 (game)**
   - 对局ID、游戏模式（PVP/PVE）
   - 玩家ID、AI难度
   - 对局状态、胜者、结束原因
   - 积分变化、时间戳

3. **棋谱表 (game_move)**
   - 落子ID、对局ID
   - 落子序号、玩家ID
   - 坐标位置、落子时间、耗时

4. **排行榜快照表 (ranking_snapshot)**
   - 快照ID、用户ID
   - 排名、积分、等级、胜率
   - 快照类型、日期

详细设计请参考: [数据库设计文档](database/README.md)

## API接口

### 用户管理
- `POST /api/user/register` - 用户注册
- `POST /api/user/login` - 用户登录
- `GET /api/user/profile` - 获取用户信息
- `PUT /api/user/profile` - 更新用户信息
- `GET /api/user/stats` - 获取用户统计

### 匹配服务
- `POST /api/match/start` - 开始匹配
- `POST /api/match/cancel` - 取消匹配
- `GET /api/match/status` - 查询匹配状态

### 对局管理
- `POST /api/game/create` - 创建人机对战
- `GET /api/game/{gameId}` - 获取对局详情
- `POST /api/game/{gameId}/resign` - 认输
- `GET /api/game/history` - 获取历史对局
- `GET /api/game/{gameId}/moves` - 获取棋谱

### 排行榜
- `GET /api/ranking/score` - 积分排行榜
- `GET /api/ranking/winrate` - 胜率排行榜
- `GET /api/ranking/level` - 等级排行榜
- `GET /api/ranking/my` - 我的排名

### WebSocket
- `ws://localhost:8080/ws/game` - 游戏WebSocket连接

## 开发进度

### 后端（✅ 核心功能已完成）
- [x] 项目结构搭建
- [x] 数据库设计与初始化
- [x] 用户实体层实现（User、Repository）
- [x] 对局实体层实现（Game、GameMove、Repository）
- [x] JWT认证工具
- [x] 用户服务层实现（注册、登录、积分管理）
- [x] 用户控制器实现（REST API）
- [x] 对局核心逻辑实现（棋盘、落子验证、胜负判定）
- [x] AI引擎实现（简单、中等、困难三种难度）
- [x] 对局服务层实现（人机对战、对局管理、历史记录）
- [x] 对局控制器实现（游戏API接口）
- [x] 积分等级系统（积分计算、等级晋升）
- [x] 排行榜服务实现（Redis缓存、积分榜/胜率榜/等级榜）
- [x] 排行榜控制器实现
- [x] WebSocket配置与认证拦截器
- [x] WebSocket消息处理器（落子、认输、心跳）
- [x] 匹配服务实现（按等级匹配、队列管理）
- [x] 匹配控制器实现
- [x] Spring Security配置
- [x] Redis配置

### 前端（已完成基础框架）
- [x] 前端项目搭建（Vue3 + Vite）
- [x] 路由配置（Vue Router）
- [x] 状态管理（Pinia）
- [x] API封装（Axios）
- [x] 认证模块（登录/注册页面、JWT管理、路由守卫）
- [x] 大厅页面（游戏模式选择）
- [x] 排行榜页面（三种榜单展示）
- [ ] 棋盘组件（Canvas渲染，待实现）
- [ ] WebSocket客户端（待实现）
- [ ] 对局页面完善（待实现）

### 待完成
- [ ] WebSocket实时通信
- [ ] 匹配系统
- [ ] 棋盘UI渲染
- [ ] 人机对战完整流程
- [ ] 在线对弈完整流程
- [ ] 集成测试
- [ ] 性能优化

## 开发说明

### 积分计算规则
- **胜利**: +10 + (对手等级 - 自己等级) × 2
- **失败**: -5 - (自己等级 - 对手等级) × 1  
- **平局**: +2

### 等级晋升表
| 等级 | 所需积分 | 称号 |
|-----|---------|------|
| 1   | 0       | 初学者 |
| 2   | 100     | 入门 |
| 3   | 300     | 进阶 |
| 4   | 600     | 熟练 |
| 5   | 1000    | 高手 |

### 游戏规则
- 标准15×15棋盘
- 黑棋先手
- 支持禁手规则（三三、四四、长连）
- 每步30秒思考时间

## 贡献指南

欢迎贡献代码或提出建议！

## 许可证

MIT License

## 联系方式

如有问题，请提交Issue或联系项目维护者。
