# 前端依赖安装指南

## 环境要求
- Node.js 18.x 或更高版本
- npm 9.x 或更高版本

## 依赖列表

### 生产依赖 (dependencies)
```json
{
  "vue": "^3.4.0",           // Vue3框架
  "vue-router": "^4.2.5",    // Vue路由
  "pinia": "^2.1.7",         // 状态管理
  "axios": "^1.6.2"          // HTTP客户端
}
```

### 开发依赖 (devDependencies)
```json
{
  "@vitejs/plugin-vue": "^5.0.0",  // Vite的Vue插件
  "vite": "^5.0.0"                 // 构建工具
}
```

## 在线安装方法

### 1. 联网环境直接安装
```bash
cd u:\Workspace\AI-generated\AI\Gomoku\frontend
npm install
```

### 2. 使用国内镜像加速
```bash
# 使用淘宝镜像
npm config set registry https://registry.npmmirror.com

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 内网离线安装方法

### 步骤1：在有网络的机器上打包依赖

在有网络的机器上执行：
```bash
# 1. 安装依赖
cd /path/to/frontend
npm install

# 2. 打包node_modules
tar -czf node_modules.tar.gz node_modules/

# 或者使用zip（Windows）
# 压缩整个node_modules文件夹
```

### 步骤2：在内网机器上解压

将打包的文件传输到内网机器后：
```bash
# 解压到frontend目录
cd u:\Workspace\AI-generated\AI\Gomoku\frontend
tar -xzf node_modules.tar.gz

# 或者直接解压zip文件
```

### 步骤3：验证安装
```bash
# 检查依赖是否正常
npm list

# 启动开发服务器
npm run dev
```

## 启动项目

安装完成后，执行以下命令启动开发服务器：

```bash
npm run dev
```

服务器将在 http://localhost:5173 启动

## 构建生产版本

```bash
npm run build
```

构建后的文件将在 `dist/` 目录中

## 常见问题

### Q1: npm命令不可用
**A:** 需要先安装Node.js，下载地址：https://nodejs.org/

### Q2: 安装速度慢
**A:** 使用国内镜像：
```bash
npm config set registry https://registry.npmmirror.com
```

### Q3: 端口被占用
**A:** 修改 `vite.config.js` 中的端口号：
```javascript
server: {
  port: 5173  // 改为其他端口
}
```

### Q4: 后端连接失败
**A:** 确保后端服务已启动在 http://localhost:8080

## 项目结构
```
frontend/
├── node_modules/      # 依赖包（需要安装）
├── src/              # 源代码
│   ├── router/       # 路由配置
│   ├── stores/       # 状态管理
│   ├── utils/        # 工具函数
│   ├── views/        # 页面组件
│   ├── App.vue       # 根组件
│   ├── main.js       # 入口文件
│   └── style.css     # 全局样式
├── index.html        # HTML模板
├── package.json      # 项目配置
└── vite.config.js    # Vite配置
```

## 技术栈
- **Vue 3**: 渐进式JavaScript框架
- **Vue Router 4**: 官方路由管理器
- **Pinia**: 新一代状态管理库
- **Axios**: HTTP请求库
- **Vite**: 新一代前端构建工具
