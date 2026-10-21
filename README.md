# Flame Weather

*A fully open-source weather app that supports custom APIs, dynamic Material You theming, and multiple languages.*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

**English** | [简体中文](./README_zh-Hans.md) | [繁體中文](./README_zh-Hant.md) | [日本語](./README_ja.md) | [한국어](./README_ko.md) | [Español](./README_es.md) | [Русский](./README_ru.md)

> Translations cover the same 7 languages as the app itself.

---

## ✨ Features

- **Live weather** — current conditions, 24-hour and 7-day forecasts, powered by the default MET Norway data feed (`weather.flameapis.com` → `api.met.no`) with an automatic fallback chain.
- **AI advisor** — a chat-style in-app weather consultant; bring your own provider (Google Gemini, OpenAI, Anthropic, DeepSeek, GLM, Qwen, OpenRouter…) with keys stored only on your device.
- **Custom data sources** — swap the weather, geocoding, and map-tile endpoints, plus user agent and API key for tiles. Use your own services or any preset (Google Maps satellite, OSM, Gaode, CartoDB).
- **Material You theming** — HCT-based dynamic color generated from your wallpaper seed, five curated palettes (Lavender, Mint, Matcha, Marigold, Rose) and a full custom color picker.
- **Light, dark & AMOLED black** modes that follow your system or your choice.
- **Screenshot export** — generate a portrait share card with map, metrics, and QR code in one tap.
- **In-app updates** — check GitHub Releases for new versions directly from About → Check for Updates.
- **7 languages** — English, 简体中文, 繁體中文, 日本語, 한국어, Español, Русский.

## 📥 Download

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

On device, updates are surfaced via **About → Check for Updates** (uses the GitHub Releases API; no external service involved).

Prefer to try weather in the browser first? The official **[web version](https://flameproject.app/weather)** is a full-featured, zero-install companion.

## 🔨 Build from Source

Requirements: Android Studio (any recent version), JDK 17, Android SDK 35.

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

Or simply open the project in Android Studio and hit Run. Output APK lands in `app/build/outputs/apk/debug/`.

**Minimum supported version:** Android 8.0 (API 26)

**Tech stack:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities (official Google HCT)

## ⚙️ Default Data Sources

| Purpose | Provider |
| --- | --- |
| Weather | MET Norway — `weather.flameapis.com` (`api.met.no`) |
| Geocoding | Open-Meteo → Nominatim → Photon (fallback chain) |
| Reverse geocoding | BigDataCloud |
| Map tiles | Google Maps satellite · OSM · Gaode · CartoDB |

All of the above can be replaced with your own endpoints in the app settings.

## 🔒 Privacy

This application is fully open source. It only collects the necessary device location data for weather analysis and will never retain it. Your weather queries go only to the data source you have chosen; AI requests go only to the AI provider you configure. No analytics, no tracking.

## 📄 License

Licensed under the [Apache License 2.0](./LICENSE).

## 🙏 Credits

Special thanks to **Google Gemini**, **GLM**, and **MiniMax** for their code-generation support, and to the open data / open-maps communities behind MET Norway, Open-Meteo, OSM, and friends.
