# Flame Weather

*カスタム API、Material You ダイナミックテーマ、多言語対応の完全オープンソース天気アプリ。*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

[English](./README.md) | [简体中文](./README_zh-Hans.md) | [繁體中文](./README_zh-Hant.md) | **日本語** | [한국어](./README_ko.md) | [Español](./README_es.md) | [Русский](./README_ru.md)

> 翻訳はアプリ本体と同じ 7 言語に対応しています。

---

## ✨ 特徴

- **リアルタイム天気** — 現在の天気、24 時間・7 日間予報。既定のデータソースは MET Norway(`weather.flameapis.com` → `api.met.no`)で、自動フォールバックチェーンを内蔵。
- **AI アドバイザー** — アプリ内のチャット形式天気相談相手。プロバイダーを選択して利用(Google Gemini、OpenAI、Anthropic、DeepSeek、GLM、Qwen、OpenRouter など)。キーは端末内にのみ保存。
- **データソースのカスタマイズ** — 天気・ジオコーディング・地図タイルのエンドポイントと、タイル用の User-Agent・API キーを自由に変更。自分のサービスを使うか、プリセット(Google 衛星、OSM、Amap、CartoDB)を選択。
- **Material You テーマ** — 壁紙のシードから HCT ベースで動的に配色を生成。厳選された 5 つのパレット(ラベンダー、ミント、抹茶、マリーゴールド、ローズ)と完全なカスタムカラーピッカー。
- **ライト・ダーク・AMOLED ブラック** モード。システムに追従、または自分で選択。
- **スクリーンショット書き出し** — 地図・指標・QR コード入りの縦型シェアカードをワンタップで生成。
- **アプリ内アップデート確認** — 「このアプリについて → 更新を確認」から GitHub Releases の新バージョンを照会。
- **7 言語対応** — English、简体中文、繁體中文、日本語、한국어、Español、Русский。

## 📥 ダウンロード

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

アプリ内では **このアプリについて → 更新を確認** からアップデートを確認できます(GitHub Releases API を直接使用し、外部サービスは介しません)。リリースがまだない場合は、最初のバージョンを準備中です。

まずブラウザで試したい方へ — 公式 **[Web 版](https://flameproject.app/weather)** はインストール不要で全機能を利用できます。

## 🔨 ソースからビルド

必要環境:Android Studio(最近のバージョン)、JDK 17、Android SDK 35。

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

Android Studio でプロジェクトを開いて Run するだけでも OK。生成された APK は `app/build/outputs/apk/debug/` に出力されます。

**最小サポート バージョン:** Android 8.0(API 26)

**技術スタック:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities(公式 Google HCT)

## ⚙️ 既定のデータソース

| 用途 | プロバイダー |
| --- | --- |
| 天気 | MET Norway — `weather.flameapis.com`(`api.met.no`) |
| ジオコーディング | Open-Meteo → Nominatim → Photon(フォールバックチェーン) |
| 逆ジオコーディング | BigDataCloud |
| 地図タイル | Google 衛星 · OSM · Amap · CartoDB |

いずれもアプリ設定から自分のエンドポイントに置き換えできます。

## 🔒 プライバシー

本アプリは完全にオープンソースです。天気判定に必要な端末の位置情報のみを収集し、決して保持しません。天気の照会は選択したデータソースへ、AI リクエストは設定した AI プロバイダーへのみ送信されます。分析もトラッキングもありません。

## 📄 ライセンス

[Apache License 2.0](./LICENSE) で公開されています。

## 🙏 謝辞

**Google Gemini**、**GLM**、**MiniMax** のコード生成サポート、および MET Norway、Open-Meteo、OSM などのオープンデータ/オープンマップコミュニティに感謝します。
