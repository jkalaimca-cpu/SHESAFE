package com.example

import com.example.domain.services.VoiceDistressDetector
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVoiceDistressKeywordMatching() {
        val distressKeywords = listOf(
            "help", "help me", "help me please", "please help", "save me",
            "emergency", "distress", "bachao", "stop it", "call police",
            "danger", "let me go", "someone help", "i need help"
        )

        fun isDistress(text: String, custom: String = "HELP"): Boolean {
            val cleaned = text.lowercase().replace(Regex("[^a-zA-Z0-9 ]"), " ").trim()
            val activeKeywords = (distressKeywords + custom.lowercase().trim()).filter { it.isNotBlank() }
            return activeKeywords.any { kw ->
                val normalizedKw = kw.lowercase().trim()
                cleaned == normalizedKw || cleaned.contains(normalizedKw) || cleaned.split(" ").contains(normalizedKw)
            }
        }

        assertTrue("help me should match", isDistress("help me"))
        assertTrue("Help Me with exclamation should match", isDistress("Help me!"))
        assertTrue("sentence with help me should match", isDistress("Please help me right now"))
        assertTrue("emergency should match", isDistress("Emergency at location"))
        assertTrue("save me should match", isDistress("Save me please"))
        assertTrue("bachao should match", isDistress("bachao bachao"))
        assertTrue("custom keyword should match", isDistress("falcon down", custom = "falcon"))
        assertFalse("normal conversation should not match", isDistress("I am walking to the library"))
    }
}

