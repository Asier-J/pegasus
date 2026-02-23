package com.example.pegasus.domain

// ─── Preferences ──────────────────────────────────────────────────────────────
// Represents user preferences. Belongs to one User (1 to 1).
// preferredLanguage and theme match the values saved in SharedPreferences
// by saveLanguage() and saveTheme() in PreferencesScreen.kt.
data class UserPreferences(
    val userId: String,
    val notificationsEnabled: Boolean = true,

    // Matches getSavedLanguage() values: "en", "es", "ca"
    val preferredLanguage: String = "en",

    // Matches getSavedTheme() values: "dark" or "light"
    val theme: String = "dark",

    val currency: String = "EUR",
    val distanceUnit: String = "km",
    val lastUpdated: Long = System.currentTimeMillis()
) {
    /**
     * Returns a new Preferences with the updated theme and language.
     * Should be called after saveTheme() and saveLanguage() in PreferencesScreen.
     */
    fun updatePreferences(
        newTheme: String = theme,
        newLanguage: String = preferredLanguage,
        newNotificationsEnabled: Boolean = notificationsEnabled,
        newCurrency: String = currency,
        newDistanceUnit: String = distanceUnit
    ): UserPreferences {
        return this.copy(
            theme                = newTheme,
            preferredLanguage    = newLanguage,
            notificationsEnabled = newNotificationsEnabled,
            currency             = newCurrency,
            distanceUnit         = newDistanceUnit,
            lastUpdated          = System.currentTimeMillis()
        )
    }

    /**
     * Returns whether the current theme is dark mode.
     * Consistent with getSavedTheme() in PreferencesScreen.kt.
     */
    fun isDarkTheme(): Boolean = theme == "dark"

    /**
     * Future feature: sync preferences to cloud/backend.
     */
    fun syncToCloud() {
        // @TODO Implement cloud sync with backend API
    }

    /**
     * Future feature: reset all preferences to default values.
     */
    fun resetToDefaults(): UserPreferences {
        // @TODO Decide if resetting language/theme should also call
        //  saveLanguage() and saveTheme() in PreferencesScreen.kt
        return UserPreferences(userId = userId)
    }
}