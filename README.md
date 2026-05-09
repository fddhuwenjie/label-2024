# 在线心理咨询预约系统

## How to Run

### Docker 启动（推荐）

```bash
# 克隆项目后，在根目录执行
docker compose up --build -d

# 查看运行状态
docker compose ps

# 查看日志
docker compose logs -f

# 停止服务
docker compose down
```

访问地址：
- 用户前端：http://localhost:8081
- 管理后台：http://localhost:8082
- 后端 API：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html

### 本地启动

**1. 启动 MySQL 数据库：**
```bash
docker compose up -d mysql
```

**2. 启动后端：**
```bash
cd backend
mvn spring-boot:run
```

**3. 启动用户前端：**
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:5173
```

**4. 启动管理后台：**
```bash
cd frontend-admin
npm install
npm run dev
# 访问 http://localhost:5174
```

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend | 8081 | Vue3 用户前端应用 |
| frontend-admin | 8082 | Vue3 管理后台应用 |
| backend | 8080 | Spring Boot 后端 API |
| mysql | 3909 | MySQL 8.0 数据库 |

## 测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 可登录管理后台（管理员视图） |
| 咨询师 | counselor | 123456 | 可登录管理后台（咨询师工作台），张医生 |
| 咨询师 | counselor2 | 123456 | 可登录管理后台（咨询师工作台），李医生 |
| 咨询师 | counselor3 | 123456 | 可登录管理后台（咨询师工作台），王医生 |
| 普通用户 | user | 123456 | 普通用户账号，仅可登录用户前端 |

## 题目内容

基于 Spring Boot 2.7 和 Vue3、Java17、MySQL 写一个在线心理咨询预约系统

## ⚠️ 重要说明

> **支付功能声明**：本系统中的支付功能仅为演示用途，使用模拟支付流程。在实际生产项目中，需要接入真实的支付平台（如支付宝、微信支付等），并遵循相关支付安全规范。

> **安全提示**：当前 CORS 配置允许所有来源访问，仅适用于开发环境。生产部署时请务必配置具体的允许域名。

## 项目介绍

本系统是一个在线心理咨询预约平台，提供用户预约心理咨询师、管理预约记录等功能。

### 技术栈

- 后端：Spring Boot 2.7 + Java 17 + MyBatis-Plus + MySQL 8.0
- 前端：Vue 3 + Vite + Element Plus + Pinia + Axios
- 部署：Docker + Docker Compose（支持 ARM64 和 AMD64 架构）

### 功能模块

**用户端（frontend）：**
- 用户注册、登录
- 浏览咨询师列表和详情
- 预约咨询时段
- 查看和管理个人预约
- 模拟支付流程

**管理后台（frontend-admin）：**
- 管理员登录
- 仪表盘数据统计
- 用户管理
- 咨询师管理
- 预约管理

**咨询师工作台（frontend-admin，咨询师登录）：**
- 数据概览（待处理/已确认/已完成预约数、评分）
- 预约管理（确认/拒绝/完成预约）
- 时间管理（设置可预约时间段）
- 我的评价（查看用户评价）
- 资料设置（编辑个人资料）

**业务规则：**
- 咨询师不能预约自己的咨询服务

### 项目结构

```
├── backend/                          # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/psychology/booking/
│   │   │   │   ├── common/           # 通用类（Result 响应封装）
│   │   │   │   ├── config/           # 配置类（OpenAPI、WebConfig）
│   │   │   │   ├── controller/       # 控制器（Auth、Booking、Counselor、Payment、Review、Stats、TimeSlot、User）
│   │   │   │   ├── dto/              # 数据传输对象（BookingRequest、LoginRequest、RegisterRequest、ReviewRequest）
│   │   │   │   ├── entity/           # 实体类（Booking、Counselor、Payment、Review、TimeSlot、User）
│   │   │   │   ├── exception/        # 异常处理（BusinessException、GlobalExceptionHandler）
│   │   │   │   ├── mapper/           # MyBatis Mapper 接口
│   │   │   │   ├── service/          # 业务服务层
│   │   │   │   ├── util/             # 工具类（JwtUtil）
│   │   │   │   └── BookingApplication.java
│   │   │   └── resources/
│   │   │       ├── application.yml   # 应用配置
│   │   │       └── db/init.sql       # 数据库初始化脚本
│   │   └── test/                     # 单元测试
│   │       ├── java/com/psychology/booking/
│   │       │   ├── controller/       # 控制器测试（AuthControllerTest）
│   │       │   └── service/          # 服务层测试（Booking、Counselor、Payment、Review、TimeSlot、User）
│   │       └── resources/
│   │           └── application-test.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                         # Vue3 用户前端
│   ├── src/
│   │   ├── api/index.js              # API 请求封装
│   │   ├── router/index.js           # 路由配置
│   │   ├── stores/user.js            # Pinia 用户状态管理
│   │   ├── views/                    # 页面组件
│   │   │   ├── Bookings.vue          # 我的预约
│   │   │   ├── CounselorDetail.vue   # 咨询师详情
│   │   │   ├── Counselors.vue        # 咨询师列表
│   │   │   ├── Home.vue              # 首页
│   │   │   ├── Layout.vue            # 布局组件
│   │   │   ├── Login.vue             # 登录
│   │   │   ├── Profile.vue           # 个人中心
│   │   │   └── Register.vue          # 注册
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── frontend-admin/                   # Vue3 管理后台
│   ├── src/
│   │   ├── api/index.js              # API 请求封装
│   │   ├── router/index.js           # 路由配置
│   │   ├── stores/user.js            # Pinia 用户状态管理
│   │   ├── views/                    # 页面组件
│   │   │   ├── Bookings.vue          # 预约管理（管理员）
│   │   │   ├── Counselors.vue        # 咨询师管理（管理员）
│   │   │   ├── Dashboard.vue         # 仪表盘（管理员/咨询师）
│   │   │   ├── Layout.vue            # 布局组件
│   │   │   ├── Login.vue             # 登录
│   │   │   ├── MyBookings.vue        # 预约管理（咨询师）
│   │   │   ├── MyProfile.vue         # 资料设置（咨询师）
│   │   │   ├── MyReviews.vue         # 我的评价（咨询师）
│   │   │   ├── MyTimeSlots.vue       # 时间管理（咨询师）
│   │   │   ├── Reviews.vue           # 评价管理（管理员）
│   │   │   ├── TimeSlots.vue         # 时间段管理（管理员）
│   │   │   └── Users.vue             # 用户管理（管理员）
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
├── docker-compose.yml                # Docker Compose 编排文件
└── README.md
```

## API 文档

启动后端服务后，访问 Swagger UI 查看完整的 API 文档：

- Swagger UI：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

## 生产环境部署注意事项

1. **CORS 配置**：修改 `WebConfig.java` 中的 `allowedOriginPatterns`，配置具体的前端域名
2. **JWT 密钥**：通过环境变量 `JWT_SECRET` 设置强密钥
3. **数据库密码**：通过环境变量配置，不要使用默认密码
4. **支付集成**：接入真实支付平台 SDK，实现支付回调验签


## API 接口说明

### 认证接口 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /login | 用户登录 |
| POST | /register | 用户注册 |
| POST | /refresh | 刷新 Token |
| POST | /logout | 退出登录 |

### 用户接口 `/api/users`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /me | 获取当前用户信息 |
| PUT | /me | 更新当前用户信息 |
| GET | / | 获取用户列表（管理员） |
| PUT | /{id}/status | 更新用户状态（管理员） |
| PUT | /{id}/role | 更新用户角色（管理员） |

### 咨询师接口 `/api/counselors`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | / | 获取可用咨询师列表 |
| GET | /all | 获取所有咨询师（管理员） |
| GET | /{id} | 获取咨询师详情 |
| GET | /me | 获取当前咨询师资料（咨询师） |
| PUT | /me | 更新当前咨询师资料（咨询师） |
| POST | / | 创建咨询师（管理员） |
| PUT | /{id} | 更新咨询师信息（管理员） |
| PUT | /{id}/available | 更新咨询师上下架状态（管理员） |

### 预约接口 `/api/bookings`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /my | 获取我的预约列表 |
| GET | /counselor | 获取咨询师收到的预约（咨询师） |
| GET | /all | 获取所有预约（管理员） |
| POST | / | 创建预约 |
| PUT | /{id}/confirm | 确认预约（咨询师） |
| PUT | /{id}/complete | 完成预约（咨询师） |
| PUT | /{id}/reject | 拒绝预约（咨询师） |
| PUT | /{id}/status | 更新预约状态 |
| DELETE | /{id} | 取消预约 |

### 时间段接口 `/api/timeslots`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /counselor/{id} | 获取咨询师时间段列表 |
| GET | /counselor/{id}/date | 按日期获取可用时间段 |
| GET | /my | 获取当前咨询师的时间段（咨询师） |
| POST | / | 创建时间段 |
| POST | /batch | 批量创建时间段 |
| POST | /my/batch | 咨询师批量创建自己的时间段（咨询师） |
| PUT | /{id}/status | 更新时间段状态 |
| DELETE | /{id} | 删除时间段 |

### 评价接口 `/api/reviews`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /counselor/{id} | 获取咨询师评价列表 |
| GET | /my | 获取当前咨询师的评价（咨询师） |
| GET | / | 获取所有评价（管理员） |
| POST | / | 创建评价 |
| DELETE | /{id} | 删除评价（管理员） |

### 支付接口 `/api/payments`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | / | 创建支付订单 |
| POST | /mock-callback | 模拟支付回调 |
| POST | /{id}/refund | 申请退款 |
| GET | /order/{orderNo} | 根据订单号查询支付 |

### 统计接口 `/api/stats`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /overview | 获取统计概览（管理员） |
| GET | /bookings/trend | 获取预约趋势（管理员） |
| GET | /counselor/my | 获取咨询师个人统计（咨询师） |
