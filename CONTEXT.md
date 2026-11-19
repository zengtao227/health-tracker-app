# personal-health-records Context

## 项目定位

这是个人健康记录 Android 应用项目，核心场景是记录血压、心率、体重，并用医学测量习惯进行多次测量后的结果计算。

项目使用 Kotlin、Jetpack Compose、Room/SQLite，本地优先，支持 CSV/Excel 导入导出。

## 关键结构

- `app/`: Android 应用主模块。
- `Healthtracker/`: 可能的历史或相关项目目录。
- `ios/`: iOS 说明或尝试目录。
- `REQUIREMENTS.md`: PRD 和数据模型。
- `build.gradle.kts`、`settings.gradle.kts`: Gradle 配置。
- `data.db`、`superpowers.db`: 本地数据库文件，处理时注意隐私。

## 工作规则

- 健康数据属于敏感个人数据，不要上传、外传或写入公开日志。
- 核心计算规则：1 次用本次；2 次丢弃第 1 次用第 2 次；3 次及以上丢弃第 1 次，取第 2 和第 3 次平均。
- UI 要保持主界面操作极简，趋势页按 7D、30D、1Y、MAX 维度组织。
- 不要无意提交数据库、构建日志或 APK 产物。

## 常用命令

```bash
./gradlew build
./gradlew test
./gradlew connectedAndroidTest
```

## 验证要求

- Kotlin/Compose 修改后运行相关 Gradle build 或 test。
- 数据库 schema 修改后验证迁移或清库重建流程。
- 健康计算逻辑修改后补单元测试覆盖 1 次、2 次、3 次及以上。
