@echo off
chcp 65001 >nul
echo ========================================
echo   创建离线安装包
echo ========================================
echo.

REM 检查node_modules是否存在
if not exist "node_modules" (
    echo [错误] 未找到 node_modules 目录！
    echo.
    echo 请先在有网络的环境中运行:
    echo   npm install
    echo.
    pause
    exit /b 1
)

echo [信息] 开始打包依赖...
echo.

REM 创建输出目录
if not exist "offline-package" mkdir offline-package

REM 复制必要文件
echo [1/4] 复制配置文件...
copy package.json offline-package\
copy package-lock.json offline-package\ 2>nul
copy vite.config.js offline-package\
copy index.html offline-package\

REM 复制源代码
echo [2/4] 复制源代码...
xcopy /E /I /Y src offline-package\src

REM 打包node_modules
echo [3/4] 压缩 node_modules (这可能需要几分钟)...
if exist "offline-package\node_modules.zip" del offline-package\node_modules.zip
powershell -command "Compress-Archive -Path 'node_modules' -DestinationPath 'offline-package\node_modules.zip' -Force"

if %errorlevel% neq 0 (
    echo [警告] PowerShell压缩失败，尝试使用7zip...
    7z a -tzip offline-package\node_modules.zip node_modules\ 2>nul
    if errorlevel 1 (
        echo [错误] 压缩失败！请手动压缩 node_modules 目录
        pause
        exit /b 1
    )
)

REM 创建安装说明
echo [4/4] 创建安装说明...
(
echo # 离线安装说明
echo.
echo ## 安装步骤
echo.
echo 1. 将整个 offline-package 文件夹复制到内网机器
echo.
echo 2. 在目标机器上安装 Node.js ^(如果尚未安装^)
echo    - 从 https://nodejs.org/ 下载离线安装包
echo    - 或使用本地的 Node.js 安装文件
echo.
echo 3. 解压依赖包:
echo    ```
echo    # 在 offline-package 目录中
echo    解压 node_modules.zip 到当前目录
echo    ```
echo.
echo 4. 返回上级目录并复制文件:
echo    ```
echo    cd ..
echo    xcopy /E /I /Y offline-package\* .
echo    ```
echo.
echo 5. 启动项目:
echo    ```
echo    npm run dev
echo    ```
echo.
echo ## 文件说明
echo.
echo - package.json: 项目配置文件
echo - vite.config.js: Vite构建配置
echo - node_modules.zip: 所有依赖包
echo - src/: 源代码目录
echo.
echo ## 注意事项
echo.
echo - 确保 Node.js 版本 ^>= 18.0
echo - 解压后的 node_modules 目录应与 package.json 在同一目录
echo - 如果启动失败，尝试: npm install
) > offline-package\INSTALL.txt

echo.
echo ========================================
echo   打包完成！
echo ========================================
echo.
echo 离线安装包位置: offline-package\
echo.
echo 包含文件:
echo   - package.json
echo   - vite.config.js
echo   - index.html
echo   - src\ (源代码)
echo   - node_modules.zip (依赖包)
echo   - INSTALL.txt (安装说明)
echo.
echo 现在可以将 offline-package 目录复制到内网机器
echo.
pause
