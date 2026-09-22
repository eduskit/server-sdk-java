# com.eduskit:server-sdk

Java 11+ B 端 Server SDK。`sdk.client()` 调课堂 server-api，`sdk.whiteboardClient()` 调白板 server-api。

入参用 `Map<String, Object>`，JSON 字段 camelCase。返回 `JsonElement`（Gson）。

概念与方法表见 [docs/overview.md](docs/overview.md)、[docs/api.md](docs/api.md)。

## 安装

本仓库本地：

```bash
mvn -s .mvn/settings.xml test
```

`.mvn/settings.xml` 配置了阿里云镜像（公共依赖在部分网络会 403）。发布后，Java 包默认发布到本仓库的
GitHub Packages Maven Registry；消费方需要配置 GitHub Packages 仓库和只读 token：

```xml
<dependency>
  <groupId>com.eduskit</groupId>
  <artifactId>server-sdk</artifactId>
  <version>0.1.1</version>
</dependency>
```

仓库地址为 `https://maven.pkg.github.com/eduskit/server-sdk-java`，认证使用 `read:packages` 权限。

## 初始化

```java
import com.eduskit.sdk.ClientConfig;
import com.eduskit.sdk.Eduskit;
import com.eduskit.sdk.EduskitError;

Eduskit sdk = new Eduskit(
    new ClientConfig("http://localhost:3112", "app_edu", "edu_key", "edu_secret"),
    new ClientConfig("http://localhost:3012", "app_wb", "wb_key", "wb_secret")
);
```

某一侧传 `null` 表示不配置。`ClientConfig` 还可指定 `timeout`、`lang`、自定义 `HttpClient`。

## 课堂 `sdk.client()`

```java
var user = sdk.client().users.register(Map.of(
    "originId", "stu_001",
    "nickname", "小明",
    "avatar", "https://example.com/a.png"
)).getAsJsonObject();

var token = sdk.client().auth.issueToken(Map.of(
    "eduUserId", user.get("eduUserId").getAsString(),
    "originId", "stu_001"
));

var classroom = sdk.client().classrooms.create(Map.of(
    "name", "一年级数学",
    "startsAt", "2026-08-17T10:00:00.000Z",
    "endsAt", "2026-08-17T11:00:00.000Z",
    "teacherEduUserId", user.get("eduUserId").getAsString()
)).getAsJsonObject();

String id = classroom.get("classroomId").getAsString();
sdk.client().classrooms.start(id);
sdk.client().classrooms.members.add(id, Map.of("eduUserId", user.get("eduUserId").getAsString(), "role", "student"));
sdk.client().classrooms.members.replaceStudents(id, Map.of("eduUserIds", List.of(user.get("eduUserId").getAsString())));
sdk.client().classrooms.permissions.set(id, user.get("eduUserId").getAsString(), Map.of(
    "permission", "camera",
    "effect", "grant",
    "operatorEduUserId", user.get("eduUserId").getAsString()
));
sdk.client().classrooms.coursewares.bind(id, Map.of("coursewareIds", List.of("cw_xxx")));
sdk.client().app.getUiConfig();
```

| 方法 | 说明 |
|------|------|
| `users.register` | 注册/更新 C 端用户 |
| `auth.issueToken` | 签发 C 端 accessToken |
| `classrooms.create/start/end` | 课堂生命周期 |
| `classrooms.members.add/list/replaceStudents` | 成员 |
| `classrooms.permissions.get/set/clear` | 权限 |
| `classrooms.coursewares.list/bind/unbind` | 课件 |
| `app.getUiConfig` / `setUiConfig` | App UI |

## 白板 `sdk.whiteboardClient()`

```java
sdk.whiteboardClient().auth.issueRoomToken(Map.of(
    "roomId", "room_1",
    "userId", "eu_xxx",
    "role", "host",
    "expiresIn", 3600
));
sdk.whiteboardClient().recordings.start("room_1", Map.of("externalRef", "lesson_001"));
sdk.whiteboardClient().recordings.enqueueVideoExport("rec_xxx", Map.of("profile", "hd"));
sdk.whiteboardClient().files.convert(Map.of("sourceUrl", "https://cdn.example.com/lesson.pptx"));
sdk.whiteboardClient().files.getConvertJob("job_xxx");
```

| 方法 | 说明 |
|------|------|
| `auth.issueRoomToken` | 签发 Room Token |
| `recordings.start/stop/list/get` | 录制 |
| `recordings.registerMediaAsset` / `deleteMediaAsset` | 媒体资产 |
| `recordings.enqueueVideoExport` / `getVideoExport` | 视频导出 |
| `captures.create/list` | 截图 |
| `files.convert` / `getConvertJob` | 转码 |

## 错误

```java
try {
    sdk.client().auth.issueToken(Map.of("eduUserId", ""));
} catch (EduskitError e) {
    System.err.println(e.getErrorCode() + " " + e.getStatus() + " " + e.getTraceId());
}
```

`sdk.client().lastTraceId()` / `sdk.whiteboardClient().lastTraceId()`。
