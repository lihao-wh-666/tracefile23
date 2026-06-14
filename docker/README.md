# Docker Deployment Guide - 考试系统Docker部署指南

## 📋 服务架构

| 服务 | 镜像 | 端口 | 说明 |
|------|------|------|------|
| MySQL | mysql:8.0.36 | 3306 | 数据库服务 |
| Redis | redis:7.2.4-alpine | 6379 | 缓存服务 |
| Backend | 自定义构建 | 6080 | Spring Boot后端API |
| Frontend | 自定义构建 | 6070 | Vue前端Nginx服务 |

## 🚀 快速开始

### Windows系统
双击运行 `deploy.bat` 或在命令行中执行：
```cmd
cd docker
deploy.bat
```

### Linux/Mac系统
```bash
cd docker
chmod +x deploy.sh
./deploy.sh
```

### 手动部署步骤
```bash
# 1. 进入docker目录
cd docker

# 2. 配置Docker镜像源（如果网络慢）
# 参考下方【Docker镜像源配置】部分

# 3. 验证配置文件
docker compose config

# 4. 构建所有镜像
docker compose build --no-cache

# 5. 启动所有服务
docker compose up -d

# 6. 查看服务状态
docker compose ps

# 7. 查看启动日志
docker compose logs -f backend
docker compose logs -f frontend
```

## ⚠️ 常见问题与解决方案

### 1. Docker镜像拉取失败 / TLS握手超时

**问题现象：**
```
TLS handshake timeout
failed to do request: Head "https://xxx": net/http: TLS handshake timeout
short read: expected X bytes but got Y: unexpected EOF
```

**解决方案：**

#### 方案A：配置Docker镜像源（推荐）
1. 打开 Docker Desktop
2. 点击右上角设置图标 ⚙️
3. 选择左侧 **Docker Engine**
4. 将以下配置粘贴到JSON编辑器中（替换原有内容）：
```json
{
  "builder": {
    "gc": {
      "defaultKeepStorage": "20GB",
      "enabled": true
    }
  },
  "experimental": false,
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://docker.1ms.run",
    "https://hub-mirror.c.163.com",
    "mirror.baidubce.com",
    "https://docker.nju.edu.cn",
    "https://docker.mirrors.ustc.edu.cn"
  ],
  "dns": [
    "8.8.8.8",
    "8.8.4.4",
    "223.5.5.5",
    "114.114.114.114"
  ],
  "max-concurrent-downloads": 3,
  "max-concurrent-uploads": 3
}
```
5. 点击 **Apply & restart** 重启Docker
6. 等待Docker重启完成后重新构建

#### 方案B：手动拉取镜像
如果自动拉取失败，可以逐个手动拉取并设置重试：
```bash
# 逐个拉取基础镜像，多次重试
docker pull --platform linux/amd64 mysql:8.0.36
docker pull --platform linux/amd64 redis:7.2.4-alpine
docker pull --platform linux/amd64 maven:3.8.6-eclipse-temurin-11
docker pull --platform linux/amd64 openjdk:11-jre-slim
docker pull --platform linux/amd64 node:18-alpine
docker pull --platform linux/amd64 nginx:alpine
```

#### 方案C：清理损坏的镜像层后重试
```bash
# 清理所有未使用的镜像、容器、网络
docker system prune -a --volumes -f

# 或者只清理失败的构建缓存
docker builder prune -a -f
```

### 2. Windows系统 `/etc/localtime` 挂载错误

**问题现象：**
```
Error response from daemon: create /etc/localtime: path not found
```

**解决方案：**
已在新版 `docker-compose.yml` 中移除该挂载，时区通过环境变量 `TZ=Asia/Shanghai` 设置。

### 3. 数据库连接失败

**问题现象：** 后端启动日志显示 `Communications link failure`

**解决方案：**
1. 确认MySQL容器已健康启动：
```bash
docker compose ps mysql
docker compose logs mysql
```
2. MySQL启动较慢，会自动重试，等待1-2分钟
3. 检查MySQL端口是否被占用：
```cmd
netstat -ano | findstr 3306
```
如果端口被占用，修改`docker-compose.yml`中ports映射

### 4. 后端健康检查失败 / 启动超时

**问题现象：** `Container is unhealthy`

**解决方案：**
1. 查看后端详细日志：
```bash
docker compose logs --tail=200 backend
```
2. 确认所有依赖服务健康：
```bash
docker compose ps
```
3. 常见原因：
   - 数据库还在初始化中（耐心等待）
   - Redis未启动
   - JVM内存不足（修改docker-compose.yml中的deploy.resources）

### 5. 前端页面无法访问

**问题现象：** 浏览器打开 http://localhost:6070 无响应

**解决方案：**
1. 检查前端容器状态：
```bash
docker compose ps frontend
docker compose logs frontend
```
2. 确认端口映射正确：
```cmd
netstat -ano | findstr 6070
```
3. 检查后端是否已健康启动（前端依赖后端）

### 6. Maven构建下载依赖慢

**问题现象：** 后端构建卡在Maven下载依赖

**解决方案：**
1. 使用阿里云Maven镜像（已在Dockerfile中配置）
2. 如果仍然慢，可以本地先打包，然后修改Dockerfile：
   - 本地执行：`cd backend && mvn clean package -DskipTests`
   - 修改Dockerfile，移除maven构建步骤，直接COPY本地jar包

### 7. NPM构建下载依赖慢

**问题现象：** 前端构建卡在npm install

**解决方案：**
1. 使用淘宝NPM镜像（已在Dockerfile中配置）
2. 本地预构建：
   - 本地执行：`cd frontend && npm install && npm run build`
   - 修改Dockerfile直接使用本地dist目录

### 8. 端口冲突

**问题现象：** `Bind for 0.0.0.0:XXXX failed: port is already allocated`

**解决方案：**
修改`docker-compose.yml`中对应服务的ports映射：
```yaml
ports:
  - "新端口:原有容器端口"
```

常用检查命令：
```cmd
# Windows查看端口占用
netstat -ano | findstr "端口号"

# 查看占用进程详情
tasklist | findstr "进程ID"
```

## 🔧 常用运维命令

### 服务管理
```bash
# 启动服务
docker compose up -d

# 停止服务
docker compose stop

# 重启服务
docker compose restart

# 停止并删除容器（保留数据）
docker compose down

# 停止并删除所有（含数据卷）
docker compose down -v
```

### 日志查看
```bash
# 查看所有服务日志（实时）
docker compose logs -f

# 查看特定服务日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql
docker compose logs -f redis

# 查看最近200行日志
docker compose logs --tail=200 backend
```

### 进入容器
```bash
# 进入后端容器
docker compose exec backend bash

# 进入MySQL容器
docker compose exec mysql mysql -u root -proot123

# 进入Redis容器
docker compose exec redis redis-cli
```

### 数据备份
```bash
# 备份MySQL数据库
docker compose exec mysqldump -u root -proot123 exam_db > backup.sql

# 恢复MySQL数据库
docker compose exec -T mysql mysql -u root -proot123 exam_db < backup.sql
```

## 📊 健康检查说明

每个服务都配置了健康检查，确保服务正常运行：

| 服务 | 检查方式 | 初始延迟 | 间隔 | 超时 |
|------|---------|---------|------|------|
| MySQL | mysqladmin ping | 40s | 10s | 10s |
| Redis | redis-cli ping | 15s | 10s | 5s |
| Backend | HTTP GET /api/dashboard/stats | 90s | 30s | 10s |
| Frontend | HTTP GET / | 30s | 30s | 10s |

## 🔑 默认账号

| 角色 | 用户名 | 密码 | 账号范围 |
|------|--------|------|---------|
| 管理员 | admin | admin123 | - |
| 教师 | teacher1 ~ teacher8 | teacher123 | 8个教师账号 |
| 学生 | student1 ~ student30 | student123 | 30个学生账号 |

## 🌐 访问地址

部署成功后可以通过以下地址访问：
- **前端界面：** http://localhost:6070
- **后端API：** http://localhost:6080/api
- **API文档：** http://localhost:6080/api/doc.html
- **MySQL：** localhost:3306 (root / root123)
- **Redis：** localhost:6379

## 🖥️ 系统要求

### 最低配置
- CPU: 2核
- 内存: 4GB
- 磁盘: 20GB可用空间

### 推荐配置
- CPU: 4核以上
- 内存: 8GB以上
- 磁盘: 50GB以上可用SSD空间

## 📞 技术支持

如遇到其他问题，请：
1. 首先查看本指南的【常见问题与解决方案】部分
2. 查看对应服务的详细日志
3. 检查服务健康状态：`docker compose ps`
4. 确保Docker Desktop版本 >= 4.0.0
