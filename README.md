# Java Spring Boot 即时通讯（IM）系统

一款基于 Spring Boot、Netty（WebSocket 实现）和 Redis 构建的高性能、可扩展即时通讯后端系统。该系统支持实时文本/多媒体消息收发、群聊、WebRTC 信令交互以及大文件断点续传功能。

系统采用时间线（序列 ID）一致性模型保障消息同步与有序性，架构设计对标微信、WhatsApp 等主流即时通讯应用。

---

## 🚀 核心功能

### 💬 消息通讯与实时性

- **通信协议**：基于 WebSocket（STOMP）实现双向实时通信
- **一致性保障**：通过 Redis Lua 脚本生成单调递增的序列 ID（Seq），落地时间线模型，确保消息可靠同步
- **持久化机制**：借助 RabbitMQ 异步将消息写入 MySQL，应对高写入吞吐量场景
- **离线支持**：客户端可根据本地与服务端的序列 ID 区间（minSeq、maxSeq）拉取缺失消息
- **已读回执**：针对单聊/群聊会话，维护消息已读状态记录（ReadRecord）

### 👥 社交关系体系

- **好友功能**：支持好友添加、删除与列表查询
- **群聊功能**：支持群创建、加入、退出、成员移除、群解散，以及群主/普通成员的角色权限管理
- **用户搜索**：支持通过唯一账号 ID 检索用户

### 📞 实时音视频通信（RTC）

- **信令服务**：基于 WebSocket 完成 WebRTC 的 SDP 与 ICE Candidate 信息交换
- **CoTurn 集成**：生成临时 TURN/STUN 凭证，解决 NAT 穿透问题
- **通话逻辑**：支持通话请求、接听、挂断等信令状态的处理

### 📂 文件存储

- **分片上传**：基于 Redis 位图（Bitmaps）实现大文件断点续传
- **存储后端**：兼容 MinIO 或 AWS S3 对象存储服务
- **权限管控**：支持生成私有文件的鉴权访问链接

### 🛡️ 安全与可靠性

- **身份认证**：采用 JWT（JSON Web Token）实现 Access Token/Refresh Token 双令牌轮换机制
- **密码加密**：使用 Argon2 算法进行高强度密码哈希存储
- **限流防护**：基于 AOP + Redis 实现接口限流，覆盖文件上传、短信/邮件发送、群创建等敏感操作
- **分布式锁**：通过 Redisson 实现分布式锁，避免用户/群组信息更新时的并发冲突

---

## 🛠️ 技术栈

- **开发语言**：Java 17+
- **核心框架**：Spring Boot 3.x
- **数据库**：MySQL（持久层框架：MyBatis + MyBatis Providers）
- **缓存与 NoSQL**：Redis（客户端：StringRedisTemplate + Redisson）
- **消息队列**：RabbitMQ
- **对象存储**：MinIO / AWS S3 SDK v2
- **安全组件**：Spring Security、JJWT、Argon2
- **接口文档**：OpenAPI 3（Swagger）

---

## 🏗️ 架构概览

- **网关/控制器层**：接收 REST API 请求，处理 WebSocket 协议升级
- **服务层**：封装即时通讯、音视频通话、用户管理的核心业务逻辑
- **时间线服务（Redis）**：通过 Redis 有序集合（ZSET）存储近期消息，支持快速检索与同步；通过 Lua 脚本原子化生成序列 ID
- **异步任务（RabbitMQ）**：消息先入队缓冲，再批量写入 MySQL，避免数据库压力峰值
- **文件处理服务**：基于 Redis 维护分片上传状态，将文件块流式传输至 MinIO

---

## ⚙️ 配置说明

需在 `application.yml` 或 `application.properties` 中配置以下依赖组件的连接信息：

```properties
# Server
server.port=8090

# Database (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/im?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.password=your_redis_password

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# MinIO (Object Storage)
minio.endpoint=http://localhost:9000
minio.accessKey=minioadmin
minio.secretKey=minioadmin
minio.bucket=myim

# CoTurn (For WebRTC)
coturn.host=your_turn_server_ip:3478
coturn.secretKey=your_turn_secret

# Mail (For notifications/reset password)
spring.mail.username=your_email@qq.com
spring.mail.password=your_email_auth_code
```
## 🚀 快速开始
 
**前置条件**
 
- 安装 JDK 17 或更高版本
- 安装 Maven 3.6+
- 确保 MySQL、Redis、RabbitMQ、MinIO 服务正常运行（推荐使用 Docker 部署）
 
**安装步骤**
 
- 克隆代码仓库
git clone https://github.com/mean-player/TinyIM.git
cd im-backend
 
-  构建项目
mvn clean package -DskipTests
 
-  启动应用
java -jar target/demo-0.0.1-SNAPSHOT.jar
 
 
## 🔌 API 接口概览
 
- **认证模块**： /auth/login （登录）、 /auth/register （注册）、 /auth/refresh （刷新令牌）
- **用户模块**： /user/search （搜索用户）、 /user/changeAvatar （更换头像）、 /user/changeNickname （修改昵称）
- **好友模块**： /friend/add （添加好友）、 /friend/remove （删除好友）、 /friend/friendList （好友列表）
- **群聊模块**： /group/create （创建群聊）、 /group/join （加入群聊）、 /group/leave （退出群聊）、 /group/memberList （群成员列表）
- **消息模块**：
  GET /message/getMessages （同步历史消息）
  WebSocket 推送： /app/sendMessage （发送消息）
  WebSocket 订阅： /user/queue/messageSeq （接收消息序列 Ack）
- **文件上传**： /test/initUpload （初始化上传）、 /test/uploadPart （上传分片）、 /test/completeUpload （完成上传）
- **音视频模块**： /rtc/turnToken （获取 TURN 令牌）、WebSocket 音视频信令接口
 
## 🤝 贡献指南
 
欢迎贡献代码！
 
## 📄 许可证
 
本项目基于 MIT 许可证开源。
