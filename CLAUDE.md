# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`Luna_Android` 是 Luna 音乐产品的原生 Android 客户端（Kotlin + Jetpack Compose / Material 3），功能对标 `Luna_iOS`，接口复用 `LunaAPI`。Git remote：`git@github.com:qingcai518/Luna_Android.git`（`main` 分支）。

```bash
./gradlew assembleDebug
./gradlew :app:compileDebugKotlin        # 只编译，最快的检查
./gradlew :app:testDebugUnitTest         # 全部单元测试
```

- `applicationId` = `jp.co.studio.kaka`（和后端 Java 包名一致），`minSdk 24`，`targetSdk 36`。版本统一在 `gradle/libs.versions.toml`。
- 后端地址是 `app/build.gradle.kts` 里的 `API_BASE_URL`（`BuildConfig`），和其他客户端同一个 `/luna/api`。
- 首次构建需要联网拉依赖，离线环境下 Gradle 会失败。
- 模拟器 `Pixel_6_Pro_API_31` 需要约 7.4 GB 空闲磁盘才能启动（数据分区大小启动时会被设备档案重写，改 `config.ini` 和 `-partition-size` 都没用），空间不够时直接报 `Not enough space to create userdata partition`。

## 分层与依赖方向

`ui`（Screen + ViewModel）→ `domain`（模型 + Repository 接口）← `data`（Repository 实现、Retrofit、Room、DataStore）。`player/` 和 `download/` 是独立模块（Media3 服务、WorkManager 任务），通过 Hilt 注入。

- 每个页面一组文件：`XxxScreen.kt` / `XxxUiState.kt` / `XxxViewModel.kt`，ViewModel 暴露 `StateFlow<XxxUiState>`。
- **Screen 拆成「有状态外壳 + 无状态内容」**：`XxxScreen` 负责 `hiltViewModel()` 和 `collectAsStateWithLifecycle()`，真正画界面的是 `internal fun XxxContent(state, 回调…)`，只吃参数。这样以后写预览或截图 harness 时不用 mock Hilt。新页面照这个写。
- Repository 的返回值统一用 `util/ApiResult`：`Success(data)`、`Error(code, message)`（业务错误或 HTTP 错误）、`NetworkError(throwable)`（IO 失败）。Retrofit 调用用 `safeApiCall` 包一层，它会兼容后端两种 401 响应（`JwtAuthenticationFilter` 返回空 body，`GlobalExceptionHandler` 返回 JSON）。ViewModel 里 `when` 处理三种结果，不要在 UI 层 try/catch 网络异常。
- 依赖注入全部用 Hilt，模块在 `di/`。ViewModel 构造函数改了依赖之后，检查测试里手写的 fake / `newViewModel` 辅助函数。

## 文案与错误信息（UiText）

ViewModel 里**不要**持有 `Context` 或直接拼本地化字符串。错误/提示统一用 `util/UiText`：

- `UiText.Dynamic(value)`：后端返回的 message（已经是服务端文案，不翻译）。
- `UiText.Resource(@StringRes id)`：本地文案，比如网络错误 `R.string.error_network`。
- 在 Compose 里调 `.asString()` 才解析成字符串，所以会跟着当前语言走。
- 一次性提示用 `SharedFlow<UiText>`（如 `RecommendViewModel.events`，界面里 Toast）。**测试里要用 `UnconfinedTestDispatcher(testScheduler)` 起收集协程**，否则 `SharedFlow` 在没有订阅者时会丢弃发射。
- 新增文案要同时加到 `values/`（中文，默认）、`values-en/`、`values-ja/` 三份 `strings.xml`。语言切换在设置页，走 `AppCompatDelegate.setApplicationLocales`，可选语言登记在 `res/xml/locales_config.xml`。

## 主题

`ui/theme/`：品牌色 Night（深色）/ Moon（浅色）两套 `ColorScheme`（靛蓝底 + 金色强调，对应 iOS），另有 `HeroTopDark…HeroInk` 等主角卡专用色。

- `LunaTheme(dynamicColor = false)` 默认**关闭**动态取色：开着的话 Android 12+ 会用壁纸色覆盖品牌色。想恢复 Material You 传 `true`。
- 强调色用 `MaterialTheme.colorScheme.primary`；判断深浅用扩展属性 `ColorScheme.isDark`，不要再读 `isSystemInDarkTheme()`（设置页可以手动指定深/浅）。
- 共用组件在 `ui/components/`：`PageHeader`（页面头部：标签 / 大标题 / 副标题 / 主次按钮，推荐页和已下载页在用；首页用自己的问候语，我的页用头像卡）、`Skeleton`（`shimmer()`、`SongRowSkeleton`）、`Avatar`（`AvatarCircle`，无头像时按名字取色相 + 首字）、`MusicRow`（歌曲行，支持 `reason` / `detail` / `isCurrent` / `trailing`）。歌曲行都走 `MusicRow`，不要各页自己画。

## 网络与认证

- 两个 OkHttp client：带鉴权的（`AuthInterceptor` 加 `Authorization: Bearer`，`TokenAuthenticator` 在 401 时用 refresh token 换新并重试）用于 API；**不带鉴权**的用于 CDN/OSS 上的封面、音频、`.lrc`——这些是带 1 小时有效期签名的 URL，403 说明签名过期，应该重新拉一次 `MusicVO`/`LyricsVO`，而不是走 401 刷新（见 `NetworkModule` 注释）。
- token 存在 `data/local/datastore/SecureTokenStore`（`EncryptedSharedPreferences`）。登录态的唯一来源是 `domain/SessionManager`；刷新彻底失败时 `TokenAuthenticator` 调 `forceLogout()`，`LunaRoot` 监听后跳登录页。`SessionManager` 不依赖 `player`（避免循环依赖），停止播放由调用方负责。
- `GET /recommendations` 在后端缓存未命中时会同步调 DeepSeek，可能很慢。首页把它单独加载（`HomeViewModel.loadHero()`，`withTimeoutOrNull(6000)`），超时/失败静默隐藏主角卡，**不要**和歌手、分类塞进同一个并发等待里。后端「换一批」在缓存期内会返回同一批，`RecommendViewModel` 检测到 id 完全一致时发 `recommend_no_new` 提示。

## 播放

- `player/PlaybackService`（Media3 `MediaSessionService`）持有 ExoPlayer，系统通知 / 锁屏控制由 Media3 自动生成，不要手写 `NotificationCompat`。UI 通过 `MediaControllerRepository` 拿状态和发命令，`PlayerViewModel` / `FullPlayerViewModel` 只是它的包装。
- `PlayerUiState` 是全局播放状态（队列、当前下标、位置、随机 / 循环）。「正在播放」的高亮统一用 `playerState.currentMusic?.id` 和行的歌曲 id 比较。
- 全屏播放页放在 `MainScaffold` 的 `ModalBottomSheet`（高度 0.92）里，**必须** `skipPartiallyExpanded = true`：默认会先只展开半屏，歌词和控制区都在屏幕外。进度条用自定义 4dp 轨道（默认是 16dp 粗、带缺口和终点圆点）。
- 全屏播放页 `ui/player/FullPlayerScreen.kt`：唱片尺寸由 `BoxWithConstraints` 按可用高度取 140–300dp；歌词区 `weight(1f)` 吃剩余高度，`LyricsView` 按离当前行的距离做透明度衰减。迷你播放条 `MiniPlayerBar` 的进度由 `MainScaffold` 用 `positionMs / durationMs` 传入。
- `AutoSkipHandler`：ExoPlayer 默认不会跳过播放失败的曲目，它记录本次会话里失败过的队列下标，挑下一个没试过的（会绕回队首）；每首都失败过一次后返回 `null`，调用方据此停止，避免死循环。

## 离线下载

`download/DownloadWorker`（WorkManager）下载音频 / 封面 / 歌词到应用私有目录：**音频必须成功**（失败则整个任务失败），封面和歌词是尽力而为，失败不影响整体（和 iOS 的「音频 → 封面 → 歌词」链一致；歌词要单独请求 `GET /lyrics`，因为签名的 `lyricUrl` 在音频开始下载时还不知道）。元数据写 Room（`LunaDatabase`，表 `DownloadedMusicEntity`）；`DownloadStateHolder` 汇总每首歌的 `DownloadState` 供 UI 显示。已下载页的文件大小是直接读本地文件长度算出来的，不入库。

## 测试

单元测试在 `app/src/test/`：ViewModel（`StandardTestDispatcher` + fake Repository）、mapper、`LrcParser`、`AutoSkipHandler`、网络层（MockWebServer）。没有 UI 测试，`androidTest/` 只有模板文件。

- 改了 ViewModel 的构造函数或 `UiState` 字段后先跑 `:app:testDebugUnitTest`。
- 断言错误信息时比较 `UiText`，不是 `String`：`assertEquals(UiText.Dynamic("…"), state.errorMessage)`。

## 已知限制

- 界面没有自动化 UI 测试。做过一次模拟器截图检查：用临时的 debug Activity 直接渲染各 `xxxContent`（喂假数据，深/浅色主题），检查完已删除。这类 harness 没有 `NavigationSuiteScaffold`，所以看不到底部导航栏和状态栏边距的真实效果——各页真实入口的 `XxxScreen` 已经自己加了 `statusBarsPadding()`。
- 还没有做的：微信登录、按自然语言描述推荐（后端有 `POST /recommendations/prompt`）。
