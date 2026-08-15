package com.example.domain.model

data class AiThreatAssessment(
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val confidencePercent: Int = 92,
    val summary: String = "Environment appears secure. Baseline acoustic and motion telemetry are normal.",
    val detectedFactors: List<String> = listOf("Ambient noise within standard range", "Normal user movement vector", "Location within recognized safety perimeter"),
    val explainableSafetyIndicators: List<SafetyIndicator> = listOf(
        SafetyIndicator("Acoustic Threat", 12, "Calm baseline decibels"),
        SafetyIndicator("Movement Stability", 8, "No rapid violent acceleration"),
        SafetyIndicator("Check-In Compliance", 0, "No pending overdue timers")
    ),
    val recommendedActions: List<String> = listOf("Maintain routine awareness", "Keep emergency contacts updated"),
    val falsePositivePreventionReasoning: String = "AI multi-sensor cross-validation filters environmental transient spikes.",
    val evaluatedTimestamp: Long = System.currentTimeMillis(),
    val isEvaluatedByGemini: Boolean = false
)

data class SafetyIndicator(
    val name: String,
    val threatScorePercent: Int,
    val statusText: String
)
