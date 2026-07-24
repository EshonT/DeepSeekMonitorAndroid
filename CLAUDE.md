# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

## 架构核心

**MVVM + Clean Architecture 四层单向依赖**：

```
Compose UI → ViewModel → UseCase → Repository → [Retrofit / DataStore / Room]
```

- **展示层** (`presentation/`)：Compose Screen + ViewModel，ViewModel 暴露单个 `StateFlow<UiState>`，Screen 只做状态收集。
- **领域层** (`domain/`)：纯 Kotlin 无 Android 依赖。UseCase 持有 `operator fun invoke()`。Repository 只定义接口。
- **数据层** (`data/`)：Repository 实现、DTO 映射、拦截器链。

**双 Retrofit 实例**：`NetworkModule` 提供两个 Retrofit，共享同一个 OkHttpClient（拦截器链：Auth → PlatformHeaders → Error → Logging）。

- `DeepSeekApiService`：`https://api.deepseek.com/` — 余额查询（Bearer API Key）
- `DeepSeekPlatformApiService`：`https://platform.deepseek.com/` — 用量/费用查询（Bearer Web Token）

**敏感数据存储**：API Key 和用量 Token 走 `EncryptedSharedPreferences`（KeyStore AES-256），非敏感配置走 `DataStore Preferences`。`ConfigDataStore` 统一封装两者。

## DeepSeek API 要点

| 接口 | 端点 | 认证 |
|------|------|------|
| 余额 | `GET /user/balance` | Bearer `sk-xxx` API Key |
| 用量 | `GET /api/v0/usage/amount?month=&year=` | Bearer Web Token (JWT，通过 platform.deepseek.com 登录获取) |
| 费用 | `GET /api/v0/usage/cost?month=&year=` | 同上 |

用量接口非官方公开 API，Token 获取方案：WebView 打开 `platform.deepseek.com` → 用户登录 → `evaluateJavascript("localStorage.getItem('userToken')")` 提取 JWT → 验证保存。手动粘贴兜底。

用量 Token **不是** API Key（`sk-xxx`），两者互不通用。Token 过期返回 401 时需提示重新登录。

## 主题系统

三套主题由 `AppTheme` 统一分发：`ThemeMode.LIGHT` / `DARK` / `EINK`。

`LocalAnimationsEnabled`、`LocalElevationEnabled`、`LocalEInkMode` 三个 `CompositionLocal` 控制全局行为。E-Ink 模式下动画 duration=0、elevation=0、字号+2sp、强制 4 级灰阶（#000000 / #555555 / #999999 / #FFFFFF）。

设备类型检测：`DeviceTypeDetector.detect()` — smallestWidth + E-Ink 硬件特征（com.hmct.eink / com.onyx.eink）。

## 关键约定

- ViewModel 通过 `@HiltViewModel` + `@Inject constructor` 注入 UseCase
- Repository 接口在 `domain/repository/`，实现在 `data/repository/`，通过 `RepositoryModule` 的 `@Binds` 绑定
- 网络 DTO 与领域模型严格分离，映射逻辑在 Repository 实现中完成
- 错误处理：Interceptor 抛 `IOException` → Repository 转为 `AppException` 子类 → ViewModel 捕获并映射为 `DataState.Error`
- 与 Windows 版保持一致的命名：`fmtInt`、`fmtTokensShort`、`fmtMoney`、`tokenBreakdown`、`costSum`
