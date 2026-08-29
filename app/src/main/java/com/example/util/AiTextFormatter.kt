package com.example.util

/**
 * Utility to sanitize AI-generated strings into clean, readable plain text.
 * Strips out raw markdown syntax such as "####", "___", "***", "**", "*", "#", etc.
 */
object AiTextFormatter {

    fun toPlainText(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var text = raw

        // 1. Remove markdown horizontal dividing lines like ____, ---, ***, ======
        text = text.replace(Regex("(?m)^[\\s-_*=]{3,}\\s*$"), "")

        // 2. Remove markdown heading symbols like ####, ###, ##, # at the beginning of lines
        text = text.replace(Regex("(?m)^#{1,6}\\s*"), "")

        // 3. Remove markdown triple-asterisk and triple-underscore bold-italics: ***text*** or ___text___
        text = text.replace(Regex("\\*\\*\\*(.*?)\\*\\*\\*"), "$1")
        text = text.replace(Regex("___(.*?)___"), "$1")

        // 4. Remove markdown double-asterisk and double-underscore bold: **text** or __text__
        text = text.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        text = text.replace(Regex("__(.*?)__"), "$1")

        // 5. Remove single-asterisk and single-underscore italics: *text* or _text_
        text = text.replace(Regex("\\*(.*?)\\*"), "$1")
        text = text.replace(Regex("_(.*?)_"), "$1")

        // 6. Remove any remaining stray markdown markers like ####, ***, ____, **, etc.
        text = text.replace(Regex("#{1,6}"), "")
        text = text.replace(Regex("\\*{1,}"), "")
        text = text.replace(Regex("_{2,}"), "")

        // 7. Standardize markdown bullet points (- item or * item) into clean readable bullet points (• item)
        text = text.replace(Regex("(?m)^[\\s]*[-*+]\\s+"), "• ")
        text = text.replace(Regex("(?m)^[\\s]*•\\s*•\\s*"), "• ")

        // 8. Remove code block markers like ```kotlin or ``` and inline backticks
        text = text.replace(Regex("`{3}[a-zA-Z]*\\n?"), "")
        text = text.replace(Regex("`{3}"), "")
        text = text.replace(Regex("`([^`]+)`"), "$1")

        // 9. Remove multiple consecutive blank lines
        text = text.replace(Regex("\\n{3,}"), "\n\n")

        return text.trim()
    }
}
