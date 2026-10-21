# Flame Weather

*Una app del tiempo completamente de código abierto, con APIs personalizables, tematización dinámica Material You y soporte multilingüe.*

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](./LICENSE)
![Platform](https://img.shields.io/badge/Platform-Android_8.0%2B-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)

[English](./README.md) | [简体中文](./README_zh-Hans.md) | [繁體中文](./README_zh-Hant.md) | [日本語](./README_ja.md) | [한국어](./README_ko.md) | **Español** | [Русский](./README_ru.md)

> Las traducciones cubren los mismos 7 idiomas que la app.

---

## ✨ Características

- **Tiempo en vivo** — estado actual, pronóstico de 24 horas y de 7 días; la fuente de datos predeterminada es MET Norway (`weather.flameapis.com` → `api.met.no`) con una cadena de respaldo automática.
- **Asesor con IA** — consultor del tiempo conversacional dentro de la app; trae tu propio proveedor (Google Gemini, OpenAI, Anthropic, DeepSeek, GLM, Qwen, OpenRouter…) y las claves se quedan solo en tu dispositivo.
- **Fuentes de datos personalizables** — cambia los puntos de acceso de clima, geocodificación y mosaicos de mapa, además del User-Agent y la clave de API de los mosaicos. Usa tus servicios o un preset (satélite de Google Maps, OSM, Gaode, CartoDB).
- **Tematización Material You** — color dinámico basado en HCT generado con tu fondo de pantalla, cinco paletas curadas (Lavanda, Menta, Matcha, Caléndula, Rosa) y un selector de color personalizado completo.
- Modos **claro, oscuro y negro AMOLED**, siguiendo el sistema o tu elección.
- **Exportar captura** — genera una tarjeta vertical para compartir con mapa, métricas y código QR en un toque.
- **Actualizaciones en la app** — consulta las novedades de GitHub Releases desde Acerca de → Buscar actualizaciones.
- **7 idiomas** — English, 简体中文, 繁體中文, 日本語, 한국어, Español, Русский.

## 📥 Descargar

[<img src="./assets/badges/get-it-on-github.svg" alt="Get it on GitHub Release" width="200">](https://github.com/FlameSoftwareProject/Flame-Weather-App/releases)

Dentro de la app, las novedades aparecen en **Acerca de → Buscar actualizaciones** (usando la API de GitHub Releases, sin servicios externos). Si todavía no hay ninguna versión publicada, la primera está en camino.

¿Prefieres probarlo en el navegador? La **[versión web oficial](https://flameproject.app/weather)** es completa y no requiere instalación.

## 🔨 Compilar desde el Código Fuente

Requisitos: Android Studio (cualquier versión reciente), JDK 17 y Android SDK 35.

```bash
git clone https://github.com/FlameSoftwareProject/Flame-Weather-App.git
cd Flame-Weather-App
./gradlew :app:assembleDebug
```

O simplemente abre el proyecto en Android Studio y pulsa Run. El APK queda en `app/build/outputs/apk/debug/`.

**Versión mínima admitida:** Android 8.0 (API 26)

**Stack técnico:** Kotlin 2.0 · Jetpack Compose · OkHttp · Retrofit · DataStore · Coil · ZXing · Material color utilities (HCT oficial de Google)

## ⚙️ Fuentes de Datos Predeterminadas

| Uso | Proveedor |
| --- | --- |
| Clima | MET Norway — `weather.flameapis.com` (`api.met.no`) |
| Geocodificación | Open-Meteo → Nominatim → Photon (cadena de respaldo) |
| Geocodificación inversa | BigDataCloud |
| Mosaicos de mapa | Satélite de Google · OSM · Gaode · CartoDB |

Todas pueden sustituirse por tus propios puntos de acceso en la configuración de la app.

## 🔒 Privacidad

Esta aplicación es completamente de código abierto. Solo recopila los datos de ubicación del dispositivo necesarios para el análisis meteorológico y nunca los retiene. Tus consultas del tiempo van únicamente a la fuente que elijas, y las solicitudes de IA únicamente al proveedor de IA que configures. Sin analíticas ni rastreo.

## 📄 Licencia

Publicada bajo la [Licencia Apache 2.0](./LICENSE).

## 🙏 Créditos

Agradecimientos especiales a **Google Gemini**, **GLM** y **MiniMax** por su apoyo en la generación de código, y a las comunidades de datos y mapas abiertos detrás de MET Norway, Open-Meteo, OSM y otras.
