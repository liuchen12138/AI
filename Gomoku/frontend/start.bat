@echo off
chcp 65001 >nul
echo ========================================
echo   Gomoku 前端项目 - 启动脚本
echo ========================================
echo.

REM 检查Node.js是否已安装
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] 未检测到 Node.js！
    echo 请先运行 install.bat 安装依赖
    echo.
    pause
    exit /b 1
)

REM 检查依赖是否已安装
if not exist "node_modules" (
    echo [错误] 未检测到 node_modules 目录！
    echo.
    echo 请先运行 install.bat 安装依赖
    echo.
    pause
    exit /b 1
)

echo [信息] 正在启动开发服务器...
echo.
echo 服务器地址: http://localhost:5173
echo 按 Ctrl+C 停止服务器
echo.
echo ========================================
echo.

npm run dev

pause
