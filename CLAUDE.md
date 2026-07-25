# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目交互规则

- 所有对话回复、Plan 规划方案、代码解释使用**简体中文**。
- 代码注释优先使用中文，变量和函数名保持英文开发规范。
- 不引入不必要的第三方依赖。
- 优先使用项目已有的工具函数和组件。
- **不要每次修改后立即编译**，改完一起编译。

## Git 提交规则

提交信息严格遵循 Conventional Commits 规范：

```
<type>(<scope>): <subject>
```

**type 必填**：`feat` / `fix` / `docs` / `style` / `refactor` / `perf` / `test` / `chore` / `ci` / `build` / `revert`

**subject 必填**：使用中文描述，简洁明了，不超过 72 个字符，结尾不加句号。

**提交粒度**：一次提交只做一件事，可独立 revert。提交前确保编译通过。

## 构建命令

```bash
# 编译 Debug APK
./gradlew assembleDebug

# 安装到已连接设备
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 安装并启动
adb install -r app/build/outputs/apk/debug/app-debug.apk && \
  adb shell am start -n com.deepseek.monitor.debug/com.deepseek.monitor.MainActivity

# 查看设备
adb devices
```

本地 SDK 路径：`c:/Users/eshon/AppData/Local/Android/Sdk`（API 36，build-tools 36.0.0）。需要在项目根目录 `local.properties` 中配置 `sdk.dir`。

国内镜像已配置在 `settings.gradle.kts`（阿里云 Maven）。

## 测试命令

```bash
# 运行单元测试（JUnit 4 + MockK + kotlinx-coroutines-test）
./gradlew testDebugUnitTest

# 运行仪器化测试（Compose UI Test + Espresso，需模拟器或设备）
./gradlew connectedDebugAndroidTest

# 仅运行特定测试类
./gradlew testDebugUnitTest --tests "*DashboardViewModel*"
```

测试报告：`app/build/reports/tests/testDebugUnitTest/index.html`（单元测试）、`app/build/reports/androidTests/connected/`（仪器化测试）。

当前状态：测试依赖已声明（JUnit 4.13.2、MockK 1.13.12、kotlinx-coroutines-test 1.9.0、Compose UI Test、Espresso 3.6.1），但 `src/test/` 和 `src/androidTest/` 目录为空，测试尚未实现。

## 项目配置

- **版本目录**：`gradle/libs.versions.toml` 集中管理所有依赖版本（Kotlin 2.0.21、AGP 8.7.3、Compose BOM 2024.12.01、Hilt 2.52 等）。`build.gradle.kts` 中不直接硬编码版本号。
- **Gradle 属性**：并行构建 `org.gradle.parallel=true`、构建缓存 `org.gradle.caching=true`、JVM 2GB `-Xmx2048m`、非传递 R 类 `android.nonTransitiveRClass=true`。
- **单模块项目**：仅 `:app` 模块，无多模块拆分。
- **无 CI/CD**：无 GitHub Actions 等自动化管道配置。

## 架构核心

**MVVM + Clean Architecture 四层单向依赖**：

```
Compose UI → ViewModel → UseCase → Repository → [Retrofit / DataStore / Room]
```

- **展示层** (`presentation/`)：Compose Screen + ViewModel，ViewModel 暴露单个 `StateFlow<UiState>`，Screen 只做状态收集。
- **领域层** (`domain/`)：纯 Kotlin 无 Android 依赖。UseCase 持有 `operator fun invoke()`。Repository 只定义接口。
- **数据层** (`data/`)：Repository 实现、DTO 映射、拦截器链。

**包结构**：`com.deepseek.monitor/` 下按层分包 — `di/`、`data/`（`remote/`、`local/`、`repository/`）、`domain/`（`model/`、`repository/`、`usecase/`）、`presentation/`（`dashboard/`、`settings/`、`detail/`、`theme/`、`common/`、`navigation/`）、`background/`、`util/`。

**双 Retrofit 实例**：`NetworkModule` 提供两个 Retrofit，共享同一个 OkHttpClient（拦截器链：Auth → PlatformHeaders → Error → Logging）。

- `DeepSeekApiService`：`https://api.deepseek.com/` — 余额查询（Bearer API Key）
- `DeepSeekPlatformApiService`：`https://platform.deepseek.com/` — 用量/费用查询（Bearer Web Token）

**敏感数据存储**：API Key 和用量 Token 走 `EncryptedSharedPreferences`（KeyStore AES-256），非敏感配置走 `DataStore Preferences`。`ConfigDataStore` 统一封装两者。Token 写入使用 `.commit()`（同步）确保 AuthInterceptor 立即可读。

**导航**：`AdaptiveNavHost` 根据屏幕宽度在手机/平板布局间切换。

- 阈值：`smallestScreenWidth >= 600dp` 启用双窗格；平板左侧 220dp 导航侧边栏，手机全屏单窗格。
- 三个路由：`Dashboard`（"dashboard"）、`Settings`（"settings"）、`Detail`（"detail/{model}"，model = "flash" | "pro"）。
- 当前状态：`AdaptiveNavHost` 已实现但 `MainActivity` 仍直接使用 `DeepSeekNavGraph` 单窗格，自适应容器未接入。

## DeepSeek API 要点

| 接口 | 端点 | 认证 |
|------|------|------|
| 余额 | `GET /user/balance` | Bearer `sk-xxx` API Key |
| 用量 | `GET /api/v0/usage/amount?month=&year=` | Bearer Web Token (JWT，通过 platform.deepseek.com 登录获取) |
| 费用 | `GET /api/v0/usage/cost?month=&year=` | 同上 |

用量接口非官方公开 API，Token 获取方案：WebView 打开 `platform.deepseek.com` → 用户登录 → `shouldInterceptRequest` 拦截 API 请求提取 Authorization header → 验证保存。手动粘贴兜底。

用量 Token **不是** API Key（`sk-xxx`），两者互不通用。Token 过期返回 401 时需提示重新登录。

## 关键实现细节

**AuthInterceptor**：双模式，根据请求 URL host 自动切换 — `api.deepseek.com` 用 API Key，`platform.deepseek.com` 用 Usage Token。使用 `configDataStore.getApiKeySync()` / `getUsageTokenSync()` 同步读取，避免协程异步写入的时序问题。

**ConfigRepositoryImpl**：6 个 Flow 通过嵌套 `combine` 合并（Compose `combine` 最多 5 个入参，超过需嵌套）。

**DashboardViewModel**：
- 初始化时始终尝试刷新，无凭据时 API 返回错误则数值显示 "-"。
- Config 监听：`configRepository.config.drop(1).collectLatest { refresh() }` — 从设置页配置凭据后自动刷新仪表盘。
- `doRefresh()` 独立处理余额和用量结果，一方失败不影响另一方显示。

**SaveUsageTokenUseCase**：先备份旧 Token → 保存新 Token → 调 `verifyUsageToken()` 验证（用新建 OkHttpClient 直接带新 Token，不走 AuthInterceptor）→ 验证失败则回滚恢复旧 Token。

**页面布局规则**：
- 主页和详情页内容必须控制在一屏内，不滚动。`fillMaxSize()` Column，不使用 `verticalScroll`。
- 横屏自适应：主页卡片/图表按 30%/70% 左右分栏；详情页卡片/图表按 15%/85% 左右分栏。图表传 `fillHeight = true` 自适应高度。
- 图表不强行拉伸，保持合理比例。竖屏图表用固定高度（柱状图 200dp，折线图 170dp），横屏用 `fillHeight` 自适应。

**图表实现**：
- `UsageTrendChart`（堆叠柱状图）：`awaitEachGesture` 手势跟踪、`drawBehind` 绘制横轴刻度线、`BoxWithConstraints` 自适应高度、SpaceBetween 对齐柱子和日期标签、tooltip 显示日期 + 命中/未命中/输出三行明细。
- `DailyLineChart`（平滑折线图）：`cubicTo` 贝塞尔曲线、纵轴左侧标注 topLabel 和 "0"、两条横轴线、三条曲线（命中/未命中/输出）、tooltip 浮层。
- E-Ink 配色适配：`if (isEInk) EInkColors.darkGray else LightColors.chartHit` 模式，所有 Canvas 绘制前捕获颜色为本地变量。

**TokenFormatter**（`util/TokenFormatter.kt`）：
- `fmtTokensShort(value: Long)`：始终以 M 单位输出，2 位小数。`val m = value / 1_000_000.0; return "${DecimalFormat("0.00").format(m)}M"`
- `fmtMoney(value: Double)`：金额格式化，`¥` 前缀 + 2 位小数。
- `fmtInt(value: Long)`：整数千分位格式化。例：1234567 → "1,234,567"
- `fmtDecimal(value: Double)`：保留两位小数，无货币符号。例：12.5 → "12.50"
- `fmtPercent(ratio: Double)`：百分比格式。例：0.85 → "85%"
- `cacheHitRatio(hit: Long, miss: Long): Double`：缓存命中率 = hit / (hit + miss)，分母为 0 时返回 0.0

**主题系统**：

三套主题由 `AppTheme` 统一分发：`ThemeMode.LIGHT` / `DARK` / `EINK`。`MainActivity` 从 DataStore 读取 `themeMode`（auto/light/dark/eink），解析 `ThemeMode` 枚举传入。

`LocalAnimationsEnabled`、`LocalElevationEnabled`、`LocalEInkMode` 三个 `CompositionLocal` 控制全局行为。E-Ink 模式下动画 duration=0、elevation=0、字号+2sp、强制 4 级灰阶（#000000 / #555555 / #999999 / #FFFFFF）。

**E-Ink 适配要点**：
- 按钮不用黑底白字（FillButton），改用 `outlinedButtonColors()` + `border(1.5.dp, onSurface, shape)`，黑色外框 + 黑色文字。
- 成功色/图表色全部映射到灰阶。
- 设备类型检测：`DeviceTypeDetector.detect()` — smallestWidth + E-Ink 硬件特征（com.hmct.eink / com.onyx.eink）。

**状态流转**：`DataState` sealed class — `Idle` / `Loading` / `Ok` / `Error(message)`。Screen 中先提取本地变量避免委托属性 `when` 智能转换问题。无凭据时直接显示主页面，数值显示 "-"。

**设置页布局**：列表式布局，`HorizontalDivider` 分割线。区域：显示与刷新 → API Key → 用量同步。主题选择：点击行弹 AlertDialog 单选（●/○ 标记）。刷新间隔：5 档自定义滑块（Canvas 拖拽 + 圆点）。

**后台任务**：`RefreshScheduler`（`@Singleton`）封装 WorkManager 周期性刷新。

- 用户可选间隔：60 / 300 / 1800 / 3600 秒。
- `coerceAtLeast(15 * 60)`：WorkManager 系统强制最小 15 分钟间隔，短于此值的配置由系统自行调节。
- 网络约束：`Constraints(NetworkType.CONNECTED)`，仅联网时执行。
- `ExistingPeriodicWorkPolicy.UPDATE`：修改间隔后覆盖旧任务。
- `RefreshWorker`（`@HiltWorker` + `@AssistedInject`）调用 `RefreshAllUseCase`，余额 < 10 时触发低余额通知。

**通知系统**：`NotificationHelper`（object）管理应用通知。

- 渠道：`deepseek_monitor` / "DeepSeek Monitor 提醒"（Android 8.0+ 必需）。
- 低余额阈值：`totalBalance < 10.0`（硬编码于 `RefreshWorker`）。
- Android 13+：静默检查 `POST_NOTIFICATIONS` 权限，未授权则跳过。
- `PendingIntent`：`FLAG_IMMUTABLE` 打开 MainActivity（Android 12+ 要求）。

## 架构边界

以下为已知的有意架构偏离：

1. **SaveUsageTokenUseCase 绕过 Repository 层**：直接注入 `ConfigDataStore`（数据层），而非通过 `ConfigRepository` 接口。原因：Token 验证需要同步 save → read 紧耦合操作，通过 Repository 的 Flow 抽象会引入不必要的延迟和竞态条件。
2. **Vico 图表库未使用**：`gradle/libs.versions.toml` 声明 `vico-compose-m3`（v2.0.1），`build.gradle.kts` 已添加 `implementation`。但所有图表实现均为自定义 Compose `Canvas`（`drawBehind` / `cubicTo` 贝塞尔曲线）。Vico 为无用依赖。
3. **Room 数据库脚手架为空**：`data/local/db/dao/` 和 `data/local/db/entity/` 目录存在但无任何 DAO、实体或 `RoomDatabase` 子类。待需要本地缓存时实现。
4. **E-Ink 自动检测未连接**：`DeviceTypeDetector` 可检测 E-Ink 硬件特征（华为/文石/海信），但调用入口未接入。`presentation/eink/` 中的 `EInkRefreshManager`、`ImageGrayscaleTransform` 为预留工具类，尚未接入 UI 层。

## 关键约定

- ViewModel 通过 `@HiltViewModel` + `@Inject constructor` 注入 UseCase
- Repository 接口在 `domain/repository/`，实现在 `data/repository/`，通过 `RepositoryModule` 的 `@Binds` 绑定
- 网络 DTO 与领域模型严格分离，映射逻辑在 Repository 实现中完成
- 错误处理：Interceptor 抛 `IOException` → Repository 转为 `AppException` 子类 → ViewModel 捕获并映射为 `DataState.Error`
- 与 Windows 版保持一致的命名：`fmtInt`、`fmtTokensShort`、`fmtMoney`、`tokenBreakdown`、`costSum`
- 魔法值提取为常量或枚举，不在代码中直接写死
- 禁止 `System.out.println`，使用 `@Slf4j` + `log.info/error`
- 不在代码中硬编码密钥、Token、连接字符串

## 加密存储

`EncryptedStore`（`util/EncryptedStore.kt`）封装 `EncryptedSharedPreferences`：

- **主密钥**：`MasterKey.Builder` + `KeyScheme.AES256_GCM`，在 Android KeyStore 硬件安全模块（TEE/StrongBox）中生成的 AES-256 密钥。
- **加密方案**：`AES256_SIV` 加密 Key，`AES256_GCM` 加密 Value（EncryptedSharedPreferences 规范要求）。
- **使用方**：`ConfigDataStore` 通过 `EncryptedStore.create(context)` 获取 `SharedPreferences` 实例，用于存储 API Key、用量 Token 和 API Key 预览。
- **同步写入**：Token 写入使用 `.commit()`（同步），确保 `AuthInterceptor` 在 OkHttp 调用线程同步读取时立即可见。
- **非敏感数据**：主题模式、刷新间隔、自动刷新开关、设备类型通过 DataStore Preferences 明文存储。

## 已知限制

1. Release 构建：R8 混淆失败，已禁用 `isMinifyEnabled = false`、`isShrinkResources = false`，需修复 ProGuard 规则后重新启用。
2. Release APK 未签名，需生成 Keystore 并配置 `signingConfigs`。
3. E-Ink 设备自动检测已有 `DeviceTypeDetector`，但未接入自动切换主题逻辑（当前手动选择水墨屏主题）。
4. 自适应导航（平板双窗格）已在 `AdaptiveNavHost.kt` 中实现，但 `MainActivity` 尚未从单窗格 `DeepSeekNavGraph` 切换使用。
5. Room 数据库仅有空目录 — DAO、实体和 `RoomDatabase` 子类未实现。Schema 导出目录 `app/schemas/` 已配置。
6. Vico 图表库（`vico-compose-m3`）已声明依赖但无源文件引用 — 所有图表均为自定义 Compose Canvas 实现。
7. 无测试实现 — 测试依赖已声明，但 `src/test/` 和 `src/androidTest/` 为空。
8. 无 CI/CD 管道。
