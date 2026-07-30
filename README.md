# DeepSeek Monitor Android

DeepSeek Monitor Android 是一个面向 Android 的 DeepSeek API 用量监控原生应用，用于查看账户余额、当月消费、模型 Token 用量和最近 7 天用量趋势。

本项目基于 [JayHome137/deepseek-monitor](https://github.com/JayHome137/DeepSeekMonitor) 的开源项目思路做 Android 平台适配，**感谢原作者 JayHome137 的开源工作**。原项目是 Python Web Dashboard，用于追踪 DeepSeek 平台多类公开变化，仅支持 macOS 版本。本项目开发目标是 Android 端监控工具，技术栈和使用方式已按 Android 平台重构实现。

郑重声明：本项目不是 DeepSeek 官方产品。

## About

DeepSeek Monitor Android: Android native adaptation of felikschu/deepseek-monitor, built with Kotlin, Jetpack Compose and Clean Architecture for DeepSeek balance and usage monitoring.

## 当前能力

- 查询 DeepSeek API 账户余额，使用 DeepSeek 官方余额接口。
- 查询 DeepSeek 平台用量数据，包括当月消费、模型 Token 总量、请求数、缓存命中、缓存未命中和输出 Token。
- 支持 V4 Flash 与 V4 Pro 两类模型用量展示。
- 支持最近 7 天用量趋势堆叠柱状图和平滑折线图详情。
- 支持 API Key 保存、清除和余额验证。
- 支持用量 Token WebView 自动同步和手动粘贴兜底。
- 支持亮色 / 暗色 / 水墨屏（E-Ink）三套主题。
- 水墨屏 6+1 局部/全屏刷新策略，减少残影闪烁。
- 堆叠柱状图柱顶数值标注、顶部圆角视觉优化。
- 图表 Tooltip 自适应定位，跟随触摸点居中。
- 无凭据时直接显示主页面，数值显示 "-"，无需跳转设置页。
- 屏幕常亮（FLAG_KEEP_SCREEN_ON + View.keepScreenOn）。
- 支持手机 / 平板 / 折叠屏 / 电纸书多设备自适应布局。
- 支持 WorkManager 后台定时刷新与低余额通知。

## 与原项目的关系

| 项目 | 原项目 deepseek-monitor | 本项目 DeepSeekMonitorAndroid |
| --- | --- | --- |
| 目标平台 | macOS / Web Dashboard | Android |
| 核心技术 | Python, Web Server, HTML Dashboard | Kotlin, Jetpack Compose, MVVM + Clean Architecture |
| 主要用途 | 追踪 DeepSeek 网页端、Feature Flags、API 端点、法律文档、GitHub 等公开变化 | 查看 DeepSeek API 余额、消费、Token 用量和趋势 |
| 启动方式 | Python 服务 + 浏览器访问 | Android 原生应用 |
| 本项目是否复用原事件追踪内容 | 不复用 | 不写入 README，不作为本项目能力声明 |

## 系统要求

- Android 12 (API 31) 及以上。
- 水墨屏主题需设备支持相关硬件特性（文石、海信等 E-Ink 设备）。

## 构建

开发环境要求：

- Android Studio Hedgehog (2023.1.1) 及以上
- Android SDK API 36
- JDK 17

```bash
# 克隆项目
git clone https://github.com/EshonT/DeepSeekMonitorAndroid.git
cd DeepSeekMonitorAndroid

# 编译 Debug APK
./gradlew assembleDebug

# 安装到已连接设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

国内镜像已配置在 `settings.gradle.kts`（阿里云 Maven），无需额外配置。

本地 SDK 路径需在项目根目录 `local.properties` 中配置 `sdk.dir`。

## 使用方式

打开应用后进入设置页，先配置 DeepSeek API Key。API Key 用于查询账户余额，来自 DeepSeek 开放平台的 [API Keys](https://platform.deepseek.com/api_keys) 页面。

因为 DeepSeek 官方未提供公开的用量统计 API，因此用量统计需要网页登录 Token。这个 Token 与 API Key 不同，用于访问 DeepSeek 平台的用量接口。

**方式一，网页登录自动同步**：

- 点击「方式一：网页登录自动同步」。
- 在弹出的 DeepSeek 登录窗口完成登录。
- 登录成功后，应用会从 WebView 的 HTTP 请求中自动提取平台用量 Token。
- 同步成功后会自动刷新当月消费和 Token 统计。

**方式二，手动粘贴 token**：

- 点击「方式二：手动粘贴 token」。
- 按页面提示从浏览器控制台获取 `JSON.parse(localStorage.userToken).value`。
- 粘贴后保存，作为自动同步失败时的兜底方案。

**Token 可能过期。用量查询失败时，重新执行网页登录同步或手动粘贴即可。**

## 数据存储

API Key 和用量 Token 存储在 Android EncryptedSharedPreferences（KeyStore AES-256 加密）。非敏感配置（主题、刷新间隔等）存储在 DataStore Preferences。

**请不要把截图、日志或配置文件中的密钥内容公开。**

## 项目结构

```text
DeepSeekMonitorAndroid/
├── app/src/main/java/com/deepseek/monitor/
│   ├── di/                         # Hilt 依赖注入模块
│   ├── data/
│   │   ├── remote/                 # Retrofit API Service、DTO、拦截器链
│   │   ├── local/                  # DataStore、加密存储、WebView Token 捕获
│   │   └── repository/             # Repository 实现
│   ├── domain/
│   │   ├── model/                  # 领域模型
│   │   ├── repository/             # Repository 接口
│   │   └── usecase/                # 业务用例
│   ├── presentation/
│   │   ├── navigation/             # 导航图与路由
│   │   ├── dashboard/              # 仪表盘（余额卡片、用量行、趋势图）
│   │   ├── detail/                 # 模型详情页（每日折线图）
│   │   ├── settings/               # 设置页（API Key、Token、主题、刷新间隔）
│   │   ├── theme/                  # 三套主题（Light / Dark / E-Ink）
│   │   └── common/                 # 通用组件（LoadingView、ErrorView 等）
│   ├── background/                 # WorkManager 后台刷新 + 通知
│   └── util/                       # TokenFormatter、DeviceTypeDetector 等工具类
├── app/src/main/res/               # Android 资源文件
├── gradle/                         # Gradle 配置
├── settings.gradle.kts             # 项目设置（含阿里云 Maven 镜像）
└── README.md                       # 项目说明
```

## 技术栈

| 层 | 技术 |
| --- | --- |
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture（四层单向依赖） |
| DI | Hilt |
| 网络 | Retrofit + OkHttp（双实例：api.deepseek.com / platform.deepseek.com） |
| 本地存储 | DataStore Preferences + EncryptedSharedPreferences |
| 安全 | Android KeyStore (AES-256 加密) |
| 图表 | 原生 Canvas 自绘（柱状图 + 贝塞尔折线图） |
| 后台 | WorkManager + ForegroundService |
| WebView Token | shouldInterceptRequest 拦截 + 验证回滚机制 |

## 不应提交的文件

- `.gradle/`、`build/`
- `.idea/`
- `.kotlin/`
- `local.properties`
- `app/build/`
- `*.keystore`、`*.jks`
- `.env`、`.env.local`

以上已通过 `.gitignore` 忽略。

## 相关项目

- [DeepSeekMonitorWindows](https://github.com/Joyi-code/DeepSeekMonitorWindows) — Windows 桌面版（Tauri 2 + React + Rust）

## 许可证

本项目使用 MIT License，与原项目 README 中声明的许可证保持一致。详见 [LICENSE](LICENSE)。

## 免责声明

本项目仅用于学习和研究目的。请遵守 DeepSeek 的使用条款，合理使用相关接口，避免频繁请求。

DeepSeek 平台页面结构、登录状态、WebView 缓存和内部用量接口都可能变化，本项目不保证长期可用。**API Key 和用量 Token 属于敏感凭据，使用者需自行承担本机存储、账号安全、网络请求和数据展示带来的风险。**
