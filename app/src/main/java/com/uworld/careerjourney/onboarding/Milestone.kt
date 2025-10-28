package com.uworld.careerjourney.onboarding;

import java.util.Date

data class Milestone(
    var id: String,
    var name: String,
    var description: String,
    var date: Date,
    var isMain: Boolean = true // set false for checkpoints in between main milestone
)

enum class JourneyType(val label: String) {
    PRE_COLLEGE("Pre-College"),
    IN_COLLEGE("In-College"),
    AFTER_COLLEGE("After-College");

    companion object {
        fun fromLabel(l: String) = JourneyType.entries.firstOrNull { it.label == l } ?: PRE_COLLEGE
    }
}
