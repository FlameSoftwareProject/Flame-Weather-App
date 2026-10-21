# Flame Weather

*Полностью открытое погодное приложение с настраиваемыми API, динамической темой Material You и поддержкой нескольких языков.*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

[English](./README.md) | [简体中文](./README_zh-Hans.md) | [繁體中文](./README_zh-Hant.md) | [日本語](./README_ja.md) | [한국어](./README_ko.md) | [Español](./README_es.md) | **Русский**

> Переводы соответствуют тем же 7 языкам, что и само приложение.

---

## ✨ Возможности

- **Погода в реальном времени** — текущая погода, прогноз на 24 часа и 7 дней; источник по умолчанию — MET Norway (`weather.flameapis.com` → `api.met.no`) с автоматической цепочкой отката.
- **ИИ-консультант** — диалоговый погодный помощник внутри приложения; подключите своего провайдера (Google Gemini, OpenAI, Anthropic, DeepSeek, GLM, Qwen, OpenRouter…), ключи хранятся только на устройстве.
- **Свои источники данных** — свободно меняйте конечные точки погоды, геокодирования и тайлов карты, а также User-Agent и API-ключ тайлов. Используйте свои сервисы или готовые (спутник Google Maps, OSM, Gaode, CartoDB).
- **Тема Material You** — динамические цвета на основе HCT по семени обоев, пять отобранных палитр (Lavender, Mint, Matcha, Marigold, Rose) и полноценный выбор пользовательского цвета.
- **Светлая, тёмная и чёрная (AMOLED)** темы — по системе или по выбору.
- **Экспорт скриншота** — портретная карточка с картой, показателями и QR-кодом в одно касание.
- **Обновления в приложении** — проверка новых версий GitHub Releases прямо из «О приложении → Проверить обновления».
- **7 языков** — English, 简体中文, 繁體中文, 日本語, 한국어, Español, Русский.

## 📥 Загрузка

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

Внутри приложения новые версии видны в **О приложении → Проверить обновления** (напрямую через GitHub Releases API, без внешних сервисов). Если релизов пока нет — первый уже готовится.

Хотите сначала попробовать в браузере? Официальная **[веб-версия](https://flameproject.app/weather)** — со всеми функциями и без установки.

## 🔨 Сборка из Исходников

Требования: Android Studio (любая свежая версия), JDK 17, Android SDK 35.

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

Или просто откройте проект в Android Studio и нажмите Run. Готовый APK появится в `app/build/outputs/apk/debug/`.

**Минимальная поддерживаемая версия:** Android 8.0 (API 26)

**Стек:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities (официальный HCT от Google)

## ⚙️ Источники Данных по Умолчанию

| Назначение | Провайдер |
| --- | --- |
| Погода | MET Norway — `weather.flameapis.com` (`api.met.no`) |
| Геокодирование | Open-Meteo → Nominatim → Photon (цепочка отката) |
| Обратное геокодирование | BigDataCloud |
| Тайлы карты | Спутник Google · OSM · Gaode · CartoDB |

Любой из них можно заменить своим адресом в настройках приложения.

## 🔒 Конфиденциальность

Приложение полностью открытое. Оно собирает только необходимые для анализа погоды данные о местоположении устройства и никогда их не сохраняет. Ваши запросы о погоде уходят только в выбранный источник, запросы ИИ — только в настроенного провайдера. Никакой аналитики и слежки.

## 📄 Лицензия

Распространяется по [лицензии Apache 2.0](./LICENSE).

## 🙏 Благодарности

Особая благодарность **Google Gemini**, **GLM** и **MiniMax** за поддержку в генерации кода, а также сообществам открытых данных и карт за MET Norway, Open-Meteo, OSM и другим.
