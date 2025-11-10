# AIAutoCode 后端（Spring Boot）说明

本目录为后端源码根目录（Java / Spring Boot）。提供应用管理、用户管理、对话历史与静态资源服务等能力，配合前端 `yu-ai-code-mother-frontend` 使用。

## 环境要求
- JDK 21（或与项目一致的版本）
- Maven 3.8+
- MySQL（或项目中所使用的数据库，参考 `application-*.yml` 配置）

## 快速开始
1. 配置数据库与环境：
   - 复制并完善 `src/main/resources/application.yml
   - 重点检查数据库连接、Redis（如有）、对象存储（如有）等参数。
2. 初始化数据库：
   - 如仓库包含初始化 SQL（例如 `sql/created_table.sql`），请先执行建表脚本。
3. 启动项目：
   - 本地开发：
     ```bash
     mvn spring-boot:run
     ```
   - 或打包运行：
     ```bash
     mvn clean package -DskipTests
     java -jar target/*.jar --spring.profiles.active=prod
     ```

> 提示：请根据实际需要选择 `dev` / `prod` 等运行 Profile。

## 主要模块与功能
- 控制器（`src/main/java/org/czjtu/aiautocode/controller`）：
  - `AppController`：应用的增删改查、部署、生成代码（含 SSE 流式接口）。
  - `UserController`：用户注册、登录、获取登录态、管理员管理用户。
  - `ChatHistryController`：应用对话历史的分页查询（含权限校验），管理员查询所有对话历史。
  - `StaticResourceController`：静态资源访问（用于应用生成后的静态文件预览 / 分发）。

## 常用接口速览
- 用户
  - POST `/user/register` 用户注册
  - POST `/user/login` 用户登录
  - GET  `/user/get/login` 获取当前登录用户信息
  - POST `/user/logout` 注销登录
  - 管理员：`/user/add`、`/user/update`、`/user/delete`、`/user/list/page/vo`
- 应用
  - GET  `/app/get/vo?id={id}` 获取应用详情（含用户信息）
  - POST `/app/my/list/page/vo` 当前用户的应用列表
  - POST `/app/admin/list/page/vo` 管理员应用列表
  - POST `/app/delete` 删除（本人或管理员）
  - GET  `/app/chat/gen/code` 生成代码（SSE 流式返回片段；前端 `EventSource` 订阅）
  - GET  `/app/download/{appId}` 下载生成的代码包
- 对话历史
  - GET  `/chatHistory/app/{appId}` 查询指定应用的对话历史（仅创建者或管理员）
  - POST `/chatHistory/admin/list/page/vo` 管理员查询所有对话记录
- 静态资源
  - 详见 `StaticResourceController`，用于生成后静态资源的读取 / 预览（与前端 `getStaticPreviewUrl` 对应）。

## 配置说明
- 参考 `src/main/resources/application-prod.yml`：
  - 数据库连接（`spring.datasource.*`）
  - 端口与上下文路径（`server.*`）
  - 其他业务相关配置（如静态资源根目录、对象存储、缓存等）

## 运行与调试建议
- 使用 `dev` Profile 进行本地联调，确保跨域、Cookie、SSE 在本地环境可正常工作。
- 前端默认通过 `axios` 的 `baseURL` 与本服务交互，注意前后端域名与端口一致性及 Cookie 域设置。
- SSE 接口（`/app/chat/gen/code`）需浏览器原生 `EventSource` 支持，服务端返回 `text/event-stream`。

## 权限与安全
- 用户鉴权：`UserService#getLoginUser` 读取登录态。
- 接口鉴权：`@AuthCheck(mustRole = ...)` 注解控制管理员接口。
- 对话历史权限：仅应用创建者与管理员可查看（`ChatHistoryServiceImpl#listAppChatHistoryByPage`）。

