package app.flameproject.weather.ui.locale

import android.content.Context
import android.content.res.Configuration
import app.flameproject.weather.data.local.AppLanguage
import java.util.Locale

fun AppLanguage.toLocale(): Locale = when (this) {
    AppLanguage.SYSTEM -> Locale.getDefault()
    AppLanguage.EN -> Locale.ENGLISH
    AppLanguage.ZH_HANS -> Locale.SIMPLIFIED_CHINESE
    AppLanguage.ZH_HANT -> Locale.TRADITIONAL_CHINESE
    AppLanguage.JA -> Locale.JAPANESE
    AppLanguage.KO -> Locale.KOREAN
    AppLanguage.RU -> Locale("ru")
    AppLanguage.ES -> Locale("es")
}

fun Context.localized(language: AppLanguage): Context {
    val config = Configuration(resources.configuration)
    config.setLocale(language.toLocale())
    return createConfigurationContext(config) ?: this
}

fun AppLanguage.apiLanguageCode(): String = when (this) {
    AppLanguage.SYSTEM -> systemLanguageCode()
    AppLanguage.EN -> "en"
    AppLanguage.ZH_HANS -> "zh"
    AppLanguage.ZH_HANT -> "zh-TW"
    AppLanguage.JA -> "ja"
    AppLanguage.KO -> "ko"
    AppLanguage.RU -> "ru"
    AppLanguage.ES -> "es"
}

private fun systemLanguageCode(locale: Locale = Locale.getDefault()): String {
    if (locale.language != "zh") return locale.language
    val traditional = locale.script == "Hant" || locale.country in setOf("TW", "HK", "MO")
    return if (traditional) "zh-TW" else "zh"
}

fun AppLanguage.aiReplyLanguageName(): String = when (this) {
    AppLanguage.SYSTEM -> systemReplyLanguageName()
    AppLanguage.EN -> "English"
    AppLanguage.ZH_HANS -> "简体中文"
    AppLanguage.ZH_HANT -> "繁體中文"
    AppLanguage.JA -> "日本語"
    AppLanguage.KO -> "한국어"
    AppLanguage.RU -> "Русский"
    AppLanguage.ES -> "Español"
}

private fun systemReplyLanguageName(locale: Locale = Locale.getDefault()): String {
    return when (locale.language) {
        "zh" -> if (locale.script == "Hant" || locale.country in setOf("TW", "HK", "MO")) "繁體中文" else "简体中文"
        "ja" -> "日本語"
        "ko" -> "한국어"
        "ru" -> "Русский"
        "es" -> "Español"
        else -> "English"
    }
}
