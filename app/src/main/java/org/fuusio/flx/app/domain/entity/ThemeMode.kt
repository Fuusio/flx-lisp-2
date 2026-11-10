package org.fuusio.flx.app.domain.entity

/**
 * The theme mode preference of the application.
 */
enum class ThemeMode(val label: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System");

    companion object {

        val DEFAULT = SYSTEM

        /**
         * Returns the [ThemeMode] matching the given persisted [key], or [DEFAULT] if the key is
         * null or unknown.
         */
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.name == key } ?: DEFAULT
    }
}
