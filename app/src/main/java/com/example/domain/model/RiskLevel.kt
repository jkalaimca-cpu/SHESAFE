package com.example.domain.model

enum class RiskLevel(
    val label: String,
    val description: String,
    val colorHex: String
) {
    LOW(
        label = "LOW RISK",
        description = "Environment appears calm. Standard background monitoring active.",
        colorHex = "#10B981" // Emerald Green
    ),
    MEDIUM(
        label = "ELEVATED CONCERN",
        description = "Unusual acoustic or motion variation detected. Pre-alert state.",
        colorHex = "#F59E0B" // Amber
    ),
    HIGH(
        label = "HIGH THREAT",
        description = "Distress vocal signals or sharp rapid impact detected. User check-in advised.",
        colorHex = "#EA580C" // Orange Red
    ),
    CRITICAL(
        label = "CRITICAL EMERGENCY",
        description = "Confirmed distress phrase or verified SOS sequence. Dispatching emergency workflow.",
        colorHex = "#E11D48" // Crimson Rose
    )
}
