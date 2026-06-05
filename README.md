# 在线考试系统

基于 Spring Boot + Vue 3 的前后端分离在线考试系统。

## 技术栈

### 后端
- Spring Boot 2.7.x
- MyBatis Plus
- MySQL 8.0
- Spring Security + JWT
- Knife4j (API文档)

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia (状态管理)
- Vue Router
- Axios
- ECharts (图表)

## 端口说明

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 6000 | Nginx 反向代理 |
| 后端 | 6080 | Spring Boot API |
| MySQL | 3306 | 数据库服务 |

## 快速开始

### 方式一：Docker 部署（推荐）

#### 前置要求
- Docker 20.10+
- Docker Compose 2.0+

#### 启动服务

```bash
cd docker
docker-compose up -d --build
```

首次启动会自动构建镜像，需要下载 Maven 和 npm 依赖，请耐心等待。

#### 停止服务

```bash
docker-compose down
```

#### 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 方式二：本地开发

#### 后端启动

```bash
cd backend
mvn clean package
java -jar target/exam-system-1.0.0.jar
```

#### 前端启动

```bash
cd frontend
npm install
npm run dev
```

## 访问地址

- **前端页面**: http://localhost:6000
- **后端API**: http://localhost:6080/api
- **API文档**: http://localhost:6080/api/doc.html

## 数据库配置

- 数据库名: `exam_db`
- 用户名: `root`
- 密码: `root123`

数据库初始化脚本位于 `sql/init.sql`，Docker 启动时会自动执行。

## 项目结构

```
exam-system/
├── backend/              # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/    # Java源代码
│   │   │   └── resources/
│   │   │       └── application.yml  # 配置文件
│   └── pom.xml
├── frontend/             # 前端项目
│   ├── src/
│   │   ├── api/         # API接口
│   │   ├── views/       # 页面组件
│   │   ├── router/      # 路由配置
│   │   ├── store/       # 状态管理
│   │   └── utils/       # 工具函数
│   └── vite.config.js   # Vite配置
├── docker/               # Docker配置
│   ├── docker-compose.yml
│   ├── backend.Dockerfile
│   ├── frontend.Dockerfile
│   └── nginx.conf
└── sql/                  # 数据库脚本
    └── init.sql
```

## 默认账号

系统初始化后可使用以下账号登录（根据数据库初始化脚本配置）：

- 管理员账号: admin / admin123
- 普通用户账号: user / user123

## Docker 容器说明

### 服务列表

| 容器名 | 镜像 | 说明 |
|--------|------|------|
| exam-mysql | mysql:8.0 | MySQL数据库 |
| exam-backend | 自定义构建 | Spring Boot后端 |
| exam-frontend | 自定义构建 | Vue前端 + Nginx |

### 数据持久化

MySQL 数据通过 Docker 卷 `mysql-data` 持久化，删除容器不会丢失数据。

如需完全重置数据库：

```bash
docker-compose down -v
docker-compose up -d
```

## 常见问题

### 1. 构建速度慢

首次构建需要下载依赖，建议配置国内镜像源：

- Maven: 配置阿里云镜像
- npm: 配置淘宝源 `npm config set registry https://registry.npmmirror.com`

### 2. 端口冲突

如果端口被占用，请修改 `docker/docker-compose.yml` 中的端口映射。

### 3. 后端启动失败

请检查 MySQL 是否健康检查通过：

```bash
docker-compose ps
```

### 4. 前端无法访问后端API

确保 nginx 配置正确，检查 `docker/nginx.conf` 中的代理配置。

## 开发说明

### 前端开发配置

前端开发时通过 Vite 代理转发 API 请求到后端，配置在 `vite.config.js` 中。

### 跨域配置

后端已通过 `CorsConfig` 配置跨域支持，开发环境可直接访问。

## License

MIT
