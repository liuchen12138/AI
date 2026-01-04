@echo off
chcp 65001 >nul
echo ========================================
echo   Gomoku 前端项目 - 安装脚本
echo ========================================
echo.

REM 检查Node.js是否已安装
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js！
    echo.
    echo 请先安装 Node.js:
    echo 1. 访问 https://nodejs.org/
    echo 2. 下载并安装 LTS 版本
    echo 3. 重新运行此脚本
    echo.
    pause
    exit /b 1
)

echo [信息] Node.js 版本:
node --version
echo.

echo [信息] npm 版本:
npm --version
echo.

REM 检查是否已安装依赖
if exist "node_modules" (
    echo [信息] 检测到已存在 node_modules 目录
    echo.
    choice /C YN /M "是否重新安装依赖"
    if errorlevel 2 goto :skip_install
    if errorlevel 1 (
        echo [信息] 删除旧的依赖...
        rd /s /q node_modules
    )
)

:install_deps
echo.
echo [信息] 正在安装依赖包...
echo.

REM 使用国内镜像加速（可选）
choice /C YN /M "是否使用淘宝镜像加速安装"
if errorlevel 1 (
    echo [信息] 配置淘宝镜像...
    npm config set registry https://registry.npmmirror.com
)

echo.
echo [信息] 开始安装...
npm install

if %errorlevel% neq 0 (
    echo.
    echo [错误] 依赖安装失败！
    echo.
    echo 可能的解决方案:
    echo 1. 检查网络连接
    echo 2. 使用 npm config set registry https://registry.npmmirror.com
    echo 3. 删除 node_modules 和 package-lock.json 后重试
    echo.
    pause
    exit /b 1
)

:skip_install
echo.
echo ========================================
echo   安装完成！
echo ========================================
echo.
echo 使用以下命令启动项目:
echo   npm run dev
echo.
echo 或运行 start.bat 直接启动
echo.
pause
