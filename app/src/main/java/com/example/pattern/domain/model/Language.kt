package com.example.pattern.domain.model

data class Language(
    val name: String,
    val nativeName: String,
    val code: String,
    val flagEmoji: String
) {
    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            Language("English", "English", "en", "🇺🇸"),
            Language("Turkish", "Türkçe", "tr", "🇹🇷"),
            Language("Norwegian", "Norsk", "nb", "🇳🇴")
        )
        
        fun fromCode(code: String): Language {
            return SUPPORTED_LANGUAGES.find { it.code == code } ?: SUPPORTED_LANGUAGES[0]
        }
    }
}
