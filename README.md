# DeepSeek Monitor Android

DeepSeek 账户用量监控 Android 原生应用。监控 API 余额、Token 消耗统计、费用图表可视化。

## 功能

- API Key 管理（加密存储、验证）
- DeepSeek 账户余额实时查询
- V4 Flash / V4 Pro 模型 Token 消耗统计
- 7 天用量趋势堆叠柱状图
- 用量 Token WebView 自动捕获 + 手动粘贴
- 亮色 / 暗色 / 水墨屏三套主题
- 手机 / 平板 / 折叠屏 / 电纸书多设备适配
- WorkManager 后台自动刷新

## 技术栈

- **语言**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **架构**: MVVM + Clean Architecture
- **DI**: Hilt
- **网络**: Retrofit + OkHttp
- **存储**: DataStore + Room
- **安全**: Android KeyStore (EncryptedSharedPreferences)
- **图表**: Vico
- **后台**: WorkManager

## 项目结构

```
com.deepseek.monitor/
├── di/                    # Hilt 依赖注入模块
├── data/
│   ├── remote/            # API Service, DTO, Interceptor
│   ├── local/             # DataStore, Room DB, WebView Token 捕获
│   └── repository/        # Repository 实现
├── domain/
│   ├── model/             # 领域模型
│   ├── repository/        # Repository 接口
│   └── usecase/           # 业务用例
├── presentation/
│   ├── navigation/        # 导航图
│   ├── dashboard/         # 仪表盘
│   ├── settings/          # 设置页
│   ├── detail/            # 模型详情页
│   ├── theme/             # 三套主题（Light/Dark/EInk）
│   ├── common/            # 通用组件
│   └── eink/              # 水墨屏适配
├── background/            # WorkManager + 通知
└── util/                  # 工具类
```

## 构建

1. Android Studio 打开项目根目录
2. 配置 Android SDK (API 36+)
3. 运行 `./gradlew assembleDebug`

## 相关项目

- [DeepSeekMonitorWindows](https://github.com/Joyi-code/DeepSeekMonitorWindows) — Windows 桌面版（Tauri 2 + Rust）

## 许可证

MIT License
