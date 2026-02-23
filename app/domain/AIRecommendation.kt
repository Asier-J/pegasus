package com.example.pegasus.domain

// ─── AIRecommendation ─────────────────────────────────────────────────────────
// Represents an AI-generated recommendation for a Trip (many to 1).
// Can be converted into an ItineraryItem once accepted by the user.
data class AIRecommendation(
    val id: String,
    val tripId: String,
    val type: String,           // "restaurant", "hotel", "activity", "transport"
    val prompt: String,
    val result: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val isSaved: Boolean = false,
    val isDismissed: Boolean = false
) {
    /**
     * Generates a new AI recommendation based on the given prompt.
     */
    fun generate(prompt: String): AIRecommendation {
        // @TODO Call AI API (e.g. Claude/OpenAI) with trip context and prompt
        return this.copy(prompt = prompt, generatedAt = System.currentTimeMillis())
    }

    /**
     * Saves this recommendation to the trip's list.
     */
    fun save(): AIRecommendation {
        // @TODO Persist saved recommendation to database
        return this.copy(isSaved = true)
    }

    /**
     * Dismisses this recommendation so it won't be shown again.
     */
    fun dismiss(): AIRecommendation {
        // @TODO Mark as dismissed in database
        return this.copy(isDismissed = true)
    }

    /**
     * Future feature: convert this recommendation into a concrete ItineraryItem.
     */
    fun toItineraryItem(): ItineraryItem {
        // @TODO Parse result and map fields to ItineraryItem
        return ItineraryItem(
            id      = "",
            tripId  = tripId,
            title   = result,
            type    = type,
            location = "",
            datetime = System.currentTimeMillis()
        )
    }

    /**
     * Future feature: refine the recommendation with additional context.
     */
    fun refine(additionalContext: String): AIRecommendation {
        // @TODO Implement follow-up prompt with previous result as context
        return this
    }
}