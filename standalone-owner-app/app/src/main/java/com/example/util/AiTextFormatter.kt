package com.example.util

/**
 * Utility to sanitize AI-generated strings into 100% clean, readable plain text.
 * Completely eliminates raw markdown formatting such as "####", "___", "***", "**", "*", "#", etc.
 */
object AiTextFormatter {

    fun toPlainText(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var text = raw

        // 1. Remove markdown horizontal lines like ____, ---, ***, ======
        text = text.replace(Regex("(?m)^[\\s-_*=]{2,}\\s*$"), "")

        // 2. Remove markdown heading symbols like ####, ###, ##, # at beginning of line or anywhere
        text = text.replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")

        // 3. Remove bold-italics: ***text*** or ___text___
        text = text.replace(Regex("\\*\\*\\*(.*?)\\*\\*\\*"), "$1")
        text = text.replace(Regex("___(.*?)___"), "$1")

        // 4. Remove bold: **text** or __text__
        text = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        text = text.replace(Regex("__(.*?)__"), "$1")

        // 5. Remove italics: *text* or _text_
        text = text.replace(Regex("\\*([^*\\n]+)\\*"), "$1")
        text = text.replace(Regex("(?<=\\s|^)_([^_\\n]+)_(?=\\s|$|[.,!?;:])"), "$1")

        // 6. Clean any remaining standalone or stray markdown symbols (####, ***, ___, etc.)
        text = text.replace(Regex("#{1,}"), "")
        text = text.replace(Regex("\\*{1,}"), "")
        text = text.replace(Regex("_{2,}"), "")

        // 7. Standardize markdown bullet points (- item, * item, + item) into clean plain bullet points (• item)
        text = text.replace(Regex("(?m)^[\\s]*[-*+]\\s+"), "• ")
        text = text.replace(Regex("(?m)^[\\s]*•\\s*•\\s*"), "• ")

        // 8. Remove code block markers (```kotlin, ```, `code`)
        text = text.replace(Regex("`{3}[a-zA-Z]*\\n?"), "")
        text = text.replace(Regex("`{3}"), "")
        text = text.replace(Regex("`([^`]+)`"), "$1")

        // 9. Remove markdown blockquote markers (> Quote)
        text = text.replace(Regex("(?m)^>\\s*"), "")

        // 10. Clean markdown table borders and pipe characters (| Col 1 | Col 2 | -> clean tabbed or spaced text)
        text = text.replace(Regex("(?m)^\\|[-:\\s|]+\\|$"), "")
        text = text.replace(Regex("(?m)^\\|"), "")
        text = text.replace(Regex("\\|(?=\\s*$)"), "")
        text = text.replace(Regex("\\|"), " • ")

        // 11. Normalize excessive whitespace and multiple consecutive blank lines
        text = text.replace(Regex("\\n{3,}"), "\n\n")

        return text.trim()
    }
}

