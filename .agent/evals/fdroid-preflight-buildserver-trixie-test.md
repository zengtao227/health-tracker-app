# F-Droid 发布证据 Gate — buildserver-trixie 镜像方案验证

2026-09-18。目的：验证「用官方 `registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie` 镜像跑 preflight」这个技术方向本身是否可行，用 health-tracker-app（com.taotao.healthtracker）做实测对象。

## 重要前提修正（执行前才发现，原计划基于错误假设）

原计划是"写一份新 metadata、本地验证、不推送"，当成**首次提交前**的演练。但实际检查发现：

- 官方 App Inclusion MR **`https://gitlab.com/fdroid/fdroiddata/-/merge_requests/45196` 早已于 2026-08-09 合并**（`state: merged`，via GitLab public API 独立核实，非记忆）。
- fdroiddata **master 上的 `metadata/com.taotao.healthtracker.yml` 已经是 versionName 1.1.7 / versionCode 6**，与本机仓库当前 tag `v1.1.7+6`（commit `4e014f41e994b931c286ebf1bc81a79c9933a73d`）完全一致。
- F-Droid 官方索引 API（`https://f-droid.org/api/v1/packages/com.taotao.healthtracker`）确认 **该版本已构建并已上线**（`suggestedVersionCode: 6`，1 个已构建包，`versionName 1.1.7 / versionCode 6`）。

结论：这个 App 当前版本**已经走完全流程并已发布**，不是"待提交"。本次测试因此调整为——**用官方已合并、已发布、已知正确的真实 metadata 做验证素材**，检验 buildserver-trixie 镜像方案能否独立复现官方结论，而不是凭空编一份 metadata。未修改、未提交、未推送任何远程仓库（严格遵守本轮范围边界）。

## Source 与模板

- [x] `PASS` 项目 source commit（完整 40 位）：`4e014f41e994b931c286ebf1bc81a79c9933a73d`（= tag `v1.1.7+6` peeled commit，annotated tag，与 HEAD 一致）
- [x] `PASS` fdroiddata upstream commit（本次 clone 时）：`6eae1068a466112efd6851621a35cc19fc17e8a4`（`git rev-parse --is-shallow-repository` = `false`）
- [x] `PASS` fdroidserver master revision：`a35fdfddd9c66823987a410566a6101186e39c84`（git clone master，非本地安装版号）
- [x] `PASS` 已存在的官方 metadata 即当前有效 recipe，无需重抓模板从零写（App 已合并，模板起点这一步在 2026-08 的原始提交流程中已完成）
- N/A MR 模板 — 本次不开 MR
- [x] `PASS` 框架版本仍在当前 builder 支持区间：本次实测构建成功即证明

## Metadata 静态门

全部针对官方 fdroiddata master 上的真实 `metadata/com.taotao.healthtracker.yml`（未做任何编造）：

- [x] `PASS` `fdroid rewritemeta com.taotao.healthtracker` 后 `git diff --exit-code` 为空 —— 输出：`2026-09-18 10:57:07 INFO: Rewriting 'com.taotao.healthtracker'`，`diff exit:0`
- [x] `PASS` 再跑一次仍为空（幂等）—— `run2 diff exit:0`
- [x] `PASS` `fdroid lint com.taotao.healthtracker` —— exit 0，无输出
- [x] `PASS` schema validation（`check-jsonschema --schemafile schemas/metadata.json metadata/com.taotao.healthtracker.yml`）—— `ok -- validation done`
- [x] `PASS` `fdroid checkupdates --allow-dirty com.taotao.healthtracker` —— 正确识别 `v1.1.7+6` 为最新匹配 tag，commit 与 metadata 记录一致，无 diff
- [x] `PASS` `Builds.commit`（`4e014f41e994b931c286ebf1bc81a79c9933a73d`）、tag peeled commit、GitHub/GitLab source commit 三方一致

## 构建与可复现门

命令：`fdroid build --verbose --test --on-server com.taotao.healthtracker:6`（容器内以**登录 shell**执行，见下方"发现的摩擦点"）

- [x] `PASS` scanner 真删文件后无报错阻断（`Removing Healthtracker/ios/build_log.txt/full_log.txt/create_icon.py/stitch_client.py` 等，均为 recipe 里 `rm:` 列表预期项）
- [x] `PASS` recipe 的 `subdir: app` + `gradle: yes` 命中预期，构建目录为 `build/com.taotao.healthtracker/app`
- [x] `PASS` builder 同款单一环境（镜像自带 OpenJDK 21.0.12.1 + Gradle 8.5，均由 recipe/gradle-wrapper 自动选中，未额外指定 JDK 路径）构建成功 —— `BUILD SUCCESSFUL in 8m 31s`，44 tasks（42 executed, 2 cached）
- [x] `PASS` 实际 APK output：`build/com.taotao.healthtracker/app/build/outputs/apk/release/app-release-unsigned.apk`；`Successfully built com.taotao.healthtracker:6 from 4e014f41e994b931c286ebf1bc81a79c9933a73d`
- N/A pre-tag source build 区分 — 本次是 post-release 场景，直接用完整 Build 块（含 `Binaries`/`AllowedAPKSigningKeys`）
- [x] `PASS` post-release 原样 recipe 保留 `Binaries`/`AllowedAPKSigningKeys`（未修改）
- [x] `PASS` built-vs-reference comparison 成功：自动下载 GitHub Release 参考包 `PersonalHealthRecords-1.1.7-6.apk`（14,225,414 bytes），`compared built binary to supplied reference binary successfully`；signer `798d088a15e4361da84f6245d8dd67d49e6c50320018f725368c995123f39348` 匹配 `AllowedAPKSigningKeys`；最终 `success: com.taotao.healthtracker` / `1 build succeeded`

## Fork、凭据与 pipeline 门

N/A — 本次未 fork fdroiddata、未推送、未开 MR，按范围边界不执行这部分。

## buildserver-trixie 镜像方案本身的结论

**方向成立，但不是"开箱即用"，有三个必须补的步骤，否则会得到假失败：**

1. **镜像里没有预装 fdroidserver**——`registry.gitlab.com/fdroid/fdroidserver:buildserver-trixie` 只是基础环境（Debian 13 trixie + OpenJDK 21.0.12.1 + Android SDK + Gradle 8.5 + Python3），`fdroid` 命令不在 PATH。必须按官方 CI 的真实做法自己装：`apt-get install fdroidserver`（拿到 apt 版依赖，如 `ruamel.yaml`，与官方 CI 环境对齐——这正是 lesson #38 要堵的 pip/apt 漂移）+ 用 fdroidserver **master** 源码覆盖 `/usr/lib/python3/dist-packages/fdroidserver/` 和 `/usr/bin/fdroid`（已用 `diff -q` 独立核实覆盖生效，而非只信 `fdroid --version` 字符串——该字符串覆盖前后都显示 `2.4.2`，因为 master 尚未改版本号，不代表覆盖失败）。
2. **`ANDROID_HOME` 只在登录 shell 里生效**——由 `/etc/profile.d/bsenv.sh` 设置，普通 `docker exec container cmd` 不会加载，会导致 `SDK location not found` 假失败（第一次尝试踩到，`ANDROID_HOME=` 为空实测确认）。preflight 脚本里所有 `docker exec` 调用必须用 `bash -lc "..."`（登录 shell），不能用裸 `bash -c`。
3. **镜像里没有 `sudo`**——`fdroid build --on-server` 会执行 `sudo passwd --lock root` 做安全加固（第二次尝试踩到：`OSError: [Errno 2] No such file or directory: 'sudo'`）。真实生产 buildserver 是完整 Vagrant/VM 环境自带 sudo；这个裸 Docker 镜像需要额外 `apt-get install sudo` 才能用 `--on-server` 模式。

堵住这三点之后，**同一份官方已发布、已知正确的 metadata 在这套环境里一次性全绿**，且额外做到了可复现构建比对（built vs. GitHub Release 参考包 signer 匹配）——这是比单纯 `pip install fdroidserver` 更接近真实官方环境的验证路径，直接解决了 lesson #38 的核心问题。

**相比 pip 装方式的实际摩擦**：镜像本身下载/拉取一次性成本（本机已缓存过一次，未重新计时），且是 amd64 镜像在本机 ARM64 (Apple Silicon) 上通过 QEMU 仿真运行（`docker image inspect` 确认 `linux/amd64`，运行时有 "platform does not match" 警告）；即便如此全流程（apt 装 fdroidserver + master 覆盖 + 全部 5 个 metadata 门 + 一次完整 Gradle 构建 + 可复现比对）总耗时约 14 分钟（10:57–11:11 UTC），可接受。

**建议**：把上述三点（apt+master 覆盖顺序、登录 shell、预装 sudo）直接写进 `scripts/fdroid-preflight.sh` 的容器初始化逻辑，作为固定步骤，不要每次现场摸索。

## 环境与清理

- Docker 镜像：`registry.gitlab.com/fdroid/fdroidserver@sha256:f81172f142454bccb6e198739d40bf3a98a393f09805140c1aa8b49807d0e3b7`（tag `buildserver-trixie`，amd64，QEMU 仿真于本机 arm64）
- 测试用 fdroiddata clone：`/private/tmp/phr-fdroid-preflight-test`（本机临时目录，未提交/未推送，可随时删除）
- 测试容器：`phr-fdroid-test`（测试结束后已停止移除）
- 未修改 health-tracker-app 仓库任何文件；未修改/未推送 fdroiddata 任何分支。

## 最终状态

- 当前层级：**已发布可安装**（该版本本来就已经是这个状态，本次测试只是验证工具链，未改变发布状态）
- 未完成项：无（本次测试目标已完成）；下一轮版本发布时，应直接套用本次验证过的容器初始化步骤
- Evidence reviewer：Claude（fork worker），2026-09-18
