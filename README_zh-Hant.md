# Flame Weather

*一款完全開源的天氣應用,支援自訂介面、Material You 動態配色與多語言。*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

[English](./README.md) | [简体中文](./README_zh-Hans.md) | **繁體中文** | [日本語](./README_ja.md) | [한국어](./README_ko.md) | [Español](./README_es.md) | [Русский](./README_ru.md)

> 翻譯版本與應用支援的 7 種語言保持一致。

---

## ✨ 功能特性

- **即時天氣** — 目前天氣、24 小時與 7 天預報;預設資料來源 MET Norway(`weather.flameapis.com` → `api.met.no`),內建自動降級鏈。
- **AI 顧問** — 應用內對話式天氣顧問;自帶提供者即可使用(Google Gemini、OpenAI、Anthropic、DeepSeek、GLM、通義千問、OpenRouter 等),金鑰僅保存在本機。
- **自訂資料來源** — 自由更換天氣、地理編碼、地圖圖磚介面,以及圖磚的 User Agent 與 API Key;可用自己的服務,或選擇預設源(Google 衛星圖、OSM、高德、CartoDB)。
- **Material You 主題** — 基於 HCT、跟隨桌布種子動態取色;另有五種精選預設(薰衣草紫、薄荷青、抹茶綠、金盞黃、玫瑰粉)與完整的自訂取色器。
- **淺色、深色與純黑(AMOLED)** 模式,跟隨系統或自選。
- **截圖匯出** — 一鍵產生帶地圖、天氣指標與 QR Code 的直式分享卡片。
- **應用內檢查更新** — 在「關於 → 檢查更新」中直接查詢 GitHub Releases 的新版本。
- **七種語言** — 英语、简体中文、繁體中文、日本語、한국어、Español、Русский。

## 📥 下載

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

在應用內可透過 **關於 → 檢查更新** 取得更新提醒(走 GitHub Releases API,不經任何外部服務)。

想先在瀏覽器裡體驗?官方 **[網頁版](https://flameproject.app/weather)** 功能完整、免安裝。

## 🔨 從原始碼建置

環境需求:Android Studio(較新版本均可)、JDK 17、Android SDK 35。

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

或在 Android Studio 中直接開啟專案並執行。產生的 APK 位於 `app/build/outputs/apk/debug/`。

**最低支援版本:** Android 8.0(API 26)

**技術棧:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities(官方 Google HCT)

## ⚙️ 預設資料來源

| 用途 | 資料來源 |
| --- | --- |
| 天氣 | MET Norway — `weather.flameapis.com`(`api.met.no`) |
| 地理編碼 | Open-Meteo → Nominatim → Photon(降級鏈) |
| 逆地理編碼 | BigDataCloud |
| 地圖圖磚 | Google 衛星圖 · OSM · 高德 · CartoDB |

以上均可在應用設定中替換為自己的介面。

## 🔒 隱私

本應用完全開源,僅收集用於天氣分析的必要裝置位置資訊,且絕不留存;天氣查詢只送給你選擇的資料來源,AI 請求只送給你設定的 AI 提供者——無統計,無追蹤。

## 📄 授權條款

基於 [Apache License 2.0](./LICENSE) 開源。

## 🙏 銘謝

特別感謝 **Google Gemini**、**GLM**、**MiniMax** 提供的程式碼支援,以及 MET Norway、Open-Meteo、OSM 等開放資料與開放地圖社群。
