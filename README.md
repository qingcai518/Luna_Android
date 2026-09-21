# Luna_Android

Luna 音乐流媒体产品的原生 Android 客户端（Kotlin + Jetpack Compose，Material 3）。和 `Luna_iOS` 功能对标，接口复用 `LunaAPI` 后端。

## 功能

- 登录 / 注册（token 加密存储，401 自动刷新）
- 首页：问候语、AI 推荐主角卡、下载入口、歌手、分类
- 搜索（歌曲 / 歌手 / 分类 / 地区）、歌手与分类的歌曲列表
- AI 推荐页（含推荐理由、换一批、左滑忽略）
- 播放：后台播放与系统媒体通知、全屏播放页（歌词、队列、随机 / 循环）、迷你播放条
- 离线下载与已下载页
- 设置：深色 / 浅色 / 跟随系统，语言（中文 / English / 日本語）

## 技术栈

Kotlin 2.2 · Jetpack Compose (Material 3) · Hilt · Media3（ExoPlayer + MediaSession） · Retrofit + OkHttp + kotlinx.serialization · Room · DataStore · WorkManager · Coil 3 · Navigation Compose。

`applicationId` 为 `jp.co.studio.kaka`（和后端 `LunaAPI` 的 Java 包名一致），`minSdk 24`，`targetSdk 36`。

## 构建与测试

```bash
./gradlew assembleDebug           # 调试包
./gradlew assembleRelease         # 发布包
./gradlew :app:testDebugUnitTest  # 单元测试（JUnit + coroutines-test + Turbine + MockWebServer）
```

需要 JDK 17+（Android Studio 自带的即可）。首次构建需要联网下载依赖。

后端地址在 `app/build.gradle.kts` 的 `API_BASE_URL`（`https://www.qingcai518.com/luna/api/`）。

## 代码结构

包名 `jp.co.studio.kaka`，按层划分：

| 目录 | 内容 |
|---|---|
| `ui/<feature>/` | 每个页面一组 `Screen` + `UiState` + `ViewModel`；`ui/components/` 是共用组件，`ui/theme/` 是主题 |
| `domain/` | 领域模型、Repository 接口、`SessionManager` |
| `data/` | `remote/`（Retrofit、DTO、拦截器）、`local/`（Room、DataStore、下载文件）、`mapper/`、`repository/`（接口实现） |
| `player/` | Media3 播放服务与播放状态 |
| `download/` | WorkManager 下载任务与下载状态 |
| `di/` | Hilt 模块 |

更详细的约定（状态与文案、主题、网络与认证、播放、测试）见 [`CLAUDE.md`](CLAUDE.md)。
