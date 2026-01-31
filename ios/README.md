# Health Tracker iOS

健康追踪 iOS 版本 - 使用 SwiftUI + SwiftData 构建

## 📱 功能特点

与 Android 版本完全一致：

- ✅ **首页**：黄历显示、快速记录表单、多用户支持
- ✅ **趋势**：BMI 渐变仪表盘、WHO 血压分级、趋势图表（点击显示数据）
- ✅ **历史**：数据表格、CSV 导入/导出
- ✅ **参考**：血压/BMI 标准参考
- ✅ **双语**：中英文切换

## 🛠 在 Xcode 中创建项目

### 步骤 1：创建新项目

1. 打开 Xcode
2. 选择 **File → New → Project**
3. 选择 **iOS → App**
4. 填写项目信息：
   - **Product Name**: `HealthTracker`
   - **Team**: 选择您的 Apple ID（即使是免费账号也可以）
   - **Organization Identifier**: `com.taotao` (或您自己的)
   - **Interface**: `SwiftUI`
   - **Language**: `Swift`
   - **Storage**: `SwiftData` ✅ (勾选)
5. 选择保存位置（建议保存到 `~/personal-health-records/ios/` 外面，避免覆盖）

### 步骤 2：替换源代码

1. 删除 Xcode 自动生成的文件：
   - `ContentView.swift`
   - `Item.swift`（如果有）

2. 将本目录下的所有 `.swift` 文件拖入 Xcode 项目：
   - 拖动 `HealthTracker/` 文件夹中的所有文件
   - 确保勾选 **"Copy items if needed"**
   - 确保勾选 **"Create folder references"**

### 步骤 3：设置项目

1. 在项目设置中确认：
   - **Minimum Deployments**: iOS 17.0
   - **Frameworks**: 自动包含 SwiftUI, SwiftData, Charts

### 步骤 4：运行

1. 选择模拟器（如 iPhone 15 Pro）
2. 点击 ▶️ 运行按钮
3. 等待编译完成

## 📁 文件结构

```
HealthTracker/
├── HealthTrackerApp.swift      # 应用入口
├── ContentView.swift           # 主视图 + TabView
├── Models/
│   ├── HealthRecord.swift      # 健康记录模型
│   └── UserProfile.swift       # 用户配置模型
├── Views/
│   ├── HomeView.swift          # 首页
│   ├── TrendsView.swift        # 趋势页
│   ├── HistoryView.swift       # 历史页
│   ├── KnowledgeView.swift     # 参考页
│   └── Components/
│       └── GaugeView.swift     # 渐变仪表盘组件
├── Services/
│   ├── L10n.swift              # 本地化服务
│   ├── LunarService.swift      # 黄历服务
│   └── CSVService.swift        # CSV 导入导出
└── Assets.xcassets/            # 资源文件
```

## 🔧 系统要求

- macOS Sonoma 14.0+
- Xcode 15.0+
- iOS 17.0+ (目标设备)

## 📝 与 Android 版本对照

| 功能 | Android | iOS |
|-----|---------|-----|
| UI 框架 | Jetpack Compose | SwiftUI |
| 数据库 | Room | SwiftData |
| 图表 | MPAndroidChart | Swift Charts |
| 语言 | Kotlin | Swift |

## 📱 安装到真机

### 方法 1：免费账号（7天有效）
1. 用 USB 连接 iPhone 到 Mac
2. 在 Xcode 中选择您的 iPhone 作为目标设备
3. 点击运行
4. 首次需要在 iPhone 设置中信任开发者证书

### 方法 2：付费账号（永久有效）
1. 注册 Apple Developer Program ($99/年)
2. 配置签名证书
3. 可通过 TestFlight 分发给最多 10,000 人

## 📤 数据迁移

iOS 和 Android 版本使用相同的 CSV 格式，可以互相导入导出数据：

```csv
Date,SBP,DBP,HR,Weight
2024-01-15,120,80,72,65.5
2024-01-16,118,78,70,65.3
```

## 🆘 常见问题

### Q: 编译报错 "Cannot find type 'HealthRecord'"
A: 确保所有 Swift 文件都已添加到项目中，并且 Target Membership 正确。

### Q: 模拟器无法启动
A: 尝试 Product → Clean Build Folder，然后重新运行。

### Q: 图表不显示
A: 确保 iOS 版本设置为 17.0+，Swift Charts 只在 iOS 16+ 可用。
