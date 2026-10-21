# Flame Weather

*一款完全开源的天气应用,支持自定义接口、Material You 动态配色与多语言。*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

[English](./README.md) | **简体中文** | [繁體中文](./README_zh-Hant.md) | [日本語](./README_ja.md) | [한국어](./README_ko.md) | [Español](./README_es.md) | [Русский](./README_ru.md)

> 翻译版本与应用支持的 7 种语言保持一致。

---

## ✨ 功能特性

- **实时天气** — 当前天气、24 小时与 7 天预报;默认数据源 MET Norway(`weather.flameapis.com` → `api.met.no`),并内置自动降级链。
- **AI 顾问** — 应用内对话式天气顾问;自带提供商即可使用(Google Gemini、OpenAI、Anthropic、DeepSeek、GLM、通义千问、OpenRouter 等),密钥仅保存在本机。
- **自定义数据源** — 自由更换天气、地理编码、地图瓦片接口,以及瓦片的 User Agent 与 API Key;可用自己的服务,或选择预置源(Google 卫星图、OSM、高德、CartoDB)。
- **Material You 主题** — 基于 HCT、跟随壁纸种子动态取色;另有五种精选预设(薰衣草紫、薄荷青、抹茶绿、金盏黄、玫瑰粉)与完整的自定义取色器。
- **浅色、深色与纯黑(AMOLED)** 模式,跟随系统或自选。
- **截图导出** — 一键生成带地图、天气指标与二维码的竖版分享卡片。
- **应用内检查更新** — 在"关于 → 检查更新"中直接查询 GitHub Releases 的新版本。
- **七种语言** — 英语、简体中文、繁體中文、日本語、한국어、Español、Русский。

## 📥 下载

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

在应用内可通过 **关于 → 检查更新** 获取更新提醒(走 GitHub Releases API,不经任何外部服务)。若还没有发布版本,第一个版本正在路上。

想先在浏览器里体验?官方 **[网页版](https://flameproject.app/weather)** 功能完整、免安装。

## 🔨 从源码构建

环境要求:Android Studio(较新版本均可)、JDK 17、Android SDK 35。

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

或在 Android Studio 中直接打开项目并运行。生成的 APK 位于 `app/build/outputs/apk/debug/`。

**最低支持版本:** Android 8.0(API 26)

**技术栈:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities(官方 Google HCT)

## ⚙️ 默认数据源

| 用途 | 数据源 |
| --- | --- |
| 天气 | MET Norway — `weather.flameapis.com`(`api.met.no`) |
| 地理编码 | Open-Meteo → Nominatim → Photon(降级链) |
| 逆地理编码 | BigDataCloud |
| 地图瓦片 | Google 卫星图 · OSM · 高德 · CartoDB |

以上均可在应用设置中替换为自己的接口。

## 🔒 隐私

本应用完全开源,仅收集用于天气分析的必要设备位置信息,且绝不留存;天气查询只发给你选择的数据源,AI 请求只发给你配置的 AI 提供商——无统计,无追踪。

## 📄 许可证

基于 [Apache License 2.0](./LICENSE) 开源。

## 🙏 鸣谢

特别感谢 **Google Gemini**、**GLM**、**MiniMax** 提供的代码支持,以及 MET Norway、Open-Meteo、OSM 等开放数据与开放地图社区。
