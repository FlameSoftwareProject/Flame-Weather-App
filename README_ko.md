# Flame Weather

*사용자 지정 API, Material You 다이나믹 테마, 다국어를 지원하는 완전 오픈소스 날씨 앱입니다.*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

[English](./README.md) | [简体中文](./README_zh-Hans.md) | [繁體中文](./README_zh-Hant.md) | [日本語](./README_ja.md) | **한국어** | [Español](./README_es.md) | [Русский](./README_ru.md)

> 번역은 앱이 지원하는 7개 언어와 동일합니다.

---

## ✨ 주요 기능

- **실시간 날씨** — 현재 날씨, 24시간·7일 예보. 기본 데이터 소스는 MET Norway(`weather.flameapis.com` → `api.met.no`)이며 자동 폴백 체인을 내장했습니다.
- **AI 어드바이저** — 앱 안의 대화형 날씨 상담. 공급자를 선택해 사용하세요(Google Gemini, OpenAI, Anthropic, DeepSeek, GLM, Qwen, OpenRouter 등). 키는 기기에만 저장됩니다.
- **데이터 소스 사용자 지정** — 날씨·지오코딩·지도 타일 엔드포인트와 타일용 User-Agent, API 키를 자유롭게 교체. 자신의 서비스를 쓰거나 프리셋(Google 위성, OSM, Gaode, CartoDB)을 선택할 수 있습니다.
- **Material You 테마** — 배경화면 시드에서 HCT 기반 동적 색상을 생성. 엄선한 5가지 팔레트(라벤더, 민트, 마차, 메리골드, 로즈)와 완전한 사용자 지정 색상 선택기를 제공합니다.
- **라이트·다크·AMOLED 블랙** 모드. 시스템을 따르거나 직접 선택.
- **스크린샷 내보내기** — 지맵·지표·QR 코드가 담긴 세로형 공유 카드를 원탭으로 생성.
- **앱 내 업데이트 확인** — 정보 → 업데이트 확인에서 GitHub Releases의 새 버전을 조회.
- **7개 언어** — English, 简体中文, 繁體中文, 日本語, 한국어, Español, Русский.

## 📥 다운로드

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

앱 안에서는 **정보 → 업데이트 확인**으로 업데이트 알림을 받을 수 있습니다(GitHub Releases API를 직접 사용하며 외부 서비스를 거치지 않습니다). 아직 릴리스가 없다면 첫 버전을 준비 중입니다.

먼저 브라우저에서 써보고 싶다면? 공식 **[웹 버전](https://flameproject.app/weather)**은 설치 없이 모든 기능을 쓸 수 있습니다.

## 🔨 소스에서 빌드

필요 환경: Android Studio(최신 버전), JDK 17, Android SDK 35.

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

Android Studio에서 프로젝트를 열고 Run 해도 됩니다. 생성된 APK는 `app/build/outputs/apk/debug/`에 저장됩니다.

**최소 지원 버전:** Android 8.0(API 26)

**기술 스택:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities(공식 Google HCT)

## ⚙️ 기본 데이터 소스

| 용도 | 공급자 |
| --- | --- |
| 날씨 | MET Norway — `weather.flameapis.com` (`api.met.no`) |
| 지오코딩 | Open-Meteo → Nominatim → Photon(폴백 체인) |
| 역지오코딩 | BigDataCloud |
| 지도 타일 | Google 위성 · OSM · Gaode · CartoDB |

모두 앱 설정에서 자신의 엔드포인트로 교체할 수 있습니다.

## 🔒 개인정보

이 앱은 완전히 오픈소스입니다. 날씨 분석에 필요한 기기 위치 정보만 수집하며 절대 보존하지 않습니다. 날씨 조회는 선택한 데이터 소스로만, AI 요청은 설정한 AI 공급자로만 전송됩니다. 분석도 추적도 없습니다.

## 📄 라이선스

[Apache License 2.0](./LICENSE)에 따라 공개됩니다.

## 🙏 감사의 말

**Google Gemini**, **GLM**, **MiniMax**의 코드 생성 지원, 그리고 MET Norway·Open-Meteo·OSM 등 오픈 데이터·오픈 맵 커뮤니티에 감사드립니다.
