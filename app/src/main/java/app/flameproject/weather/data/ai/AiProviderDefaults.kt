package app.flameproject.weather.data.ai

import app.flameproject.weather.data.local.AiProviderType

/**
 * Single source of truth for the official endpoint and default model of every provider.
 */
object AiProviderDefaults {

    fun baseUrl(provider: AiProviderType): String = when (provider) {
        AiProviderType.GOOGLE -> "https://generativelanguage.googleapis.com/v1beta"
        AiProviderType.OPENAI -> "https://api.openai.com/v1"
        AiProviderType.ANTHROPIC -> "https://api.anthropic.com"
        AiProviderType.DEEPSEEK -> "https://api.deepseek.com"
        AiProviderType.ZHIPU -> "https://open.bigmodel.cn/api/paas/v4"
        AiProviderType.QWEN -> "https://dashscope.aliyuncs.com/compatible-mode/v1"
        AiProviderType.OPENROUTER -> "https://openrouter.ai/api/v1"
        AiProviderType.CUSTOM -> ""
    }

    fun model(provider: AiProviderType): String = when (provider) {
        AiProviderType.GOOGLE -> "gemini-3.1-flash-lite"
        AiProviderType.OPENAI -> "gpt-4o-mini"
        AiProviderType.ANTHROPIC -> "claude-haiku-4-5"
        AiProviderType.DEEPSEEK -> "deepseek-flash"
        AiProviderType.ZHIPU -> "glm-4-flash"
        AiProviderType.QWEN -> "qwen-flash"
        AiProviderType.OPENROUTER -> "google/gemma-4-26b-a4b-it"
        AiProviderType.CUSTOM -> "model-id"
    }
}
