package com.example.service.gemini

import com.example.BuildConfig
import com.example.data.model.SchoolRole
import com.example.util.AiTextFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object GeminiChatModels {
    const val GEMINI_3_5_FLASH = "gemini-3.5-flash"
    const val GEMINI_3_1_PRO = "gemini-3.1-pro-preview"
    const val GEMINI_3_1_FLASH_LITE = "gemini-3.1-flash-lite-preview"
}

data class GeminiGroundingSource(
    val title: String,
    val url: String
)

data class GeminiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val isSearchGrounded: Boolean = false,
    val searchQueries: List<String> = emptyList(),
    val searchSources: List<GeminiGroundingSource> = emptyList()
)

object GeminiStudyService {
    private const val DEFAULT_MODEL = GeminiChatModels.GEMINI_3_5_FLASH
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getRoleSystemInstruction(role: SchoolRole, customInstruction: String? = null): String {
        val roleContext = when (role) {
            SchoolRole.APP_OWNER -> "You are also serving as the Platform Owner & SaaS Business Advisor for the app ecosystem."
            SchoolRole.ADMIN -> "You are also assisting a School Administrator & Principal with institutional operations."
            SchoolRole.TEACHER -> "You are also assisting a Teacher with curriculum design, lesson planning, and pedagogy."
            SchoolRole.STUDENT -> "You are also assisting a Student with learning, homework, and test preparation."
            SchoolRole.PARENT -> "You are also assisting a Parent with academic guidance and child development."
        }

        val baseInstruction = customInstruction ?: """
You are a brilliant, world-class general AI Assistant powered by Google Gemini (comparable to ChatGPT and Gemini Advanced).
You are universally knowledgeable, articulate, creative, and analytical.

CAPABILITIES:
1. UNIVERSAL KNOWLEDGE: You can answer ANY question the user asks across all domains — science, coding, technology, history, literature, philosophy, health, mathematics, business, creative writing, everyday advice, general trivia, and logic puzzles.
2. NO TOPIC RESTRICTIONS: Do not restrict yourself to only school subjects. If the user asks about world news, programming in Python/Kotlin/JavaScript, cooking recipes, space exploration, philosophical questions, or general conversation, answer thoroughly and enthusiastically with top-tier insight.
3. ROLE AWARENESS: $roleContext Tailor your tone and examples accordingly when appropriate, but never refuse a general knowledge or miscellaneous inquiry.
4. ACCURACY & DEPTH: Provide comprehensive, well-structured, step-by-step explanations, working code snippets, or clear summaries whenever requested.
""".trimIndent()

        return "$baseInstruction\n\nCRITICAL FORMATTING MANDATE: Output your response in clean, highly readable text with intuitive spacing. Use clear headings, neat bullet points (•), and numbered lists where appropriate."
    }

    /**
     * Multi-Turn Conversational Chat with Gemini models and optional Google Search Grounding.
     */
    suspend fun generateMultiTurnChat(
        history: List<GeminiChatMessage>,
        role: SchoolRole = SchoolRole.STUDENT,
        modelName: String = GeminiChatModels.GEMINI_3_5_FLASH,
        enableGoogleSearch: Boolean = false,
        customInstruction: String? = null
    ): Result<GeminiChatMessage> = withContext(Dispatchers.IO) {
        val targetModel = if (enableGoogleSearch) GeminiChatModels.GEMINI_3_5_FLASH else modelName
        val systemInstruction = getRoleSystemInstruction(role, customInstruction)
        val apiKey = BuildConfig.GEMINI_API_KEY

        val lastUserMessage = history.lastOrNull { it.role == "user" }?.text ?: "Hello"

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val offlineText = AiTextFormatter.toPlainText(getSmartOfflineRoleResponse(lastUserMessage, role))
            val fallbackMessage = GeminiChatMessage(
                role = "model",
                text = offlineText,
                modelUsed = "$targetModel (Offline Demo Mode)",
                isSearchGrounded = enableGoogleSearch,
                searchSources = if (enableGoogleSearch) listOf(
                    GeminiGroundingSource(title = "Kingsway Academic Syllabus Index", url = "https://kingswayacademy.edu/curriculum"),
                    GeminiGroundingSource(title = "WAEC Official Examination Guidelines", url = "https://waeconline.org.ng")
                ) else emptyList()
            )
            return@withContext Result.success(fallbackMessage)
        }

        try {
            val url = "$BASE_URL/$targetModel:generateContent?key=$apiKey"
            val rootJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray().apply {
                put(JSONObject().put("text", systemInstruction))
            }
            sysInstructionObj.put("parts", sysPartsArray)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Multi-turn contents array
            val contentsArray = JSONArray()
            history.forEach { msg ->
                val contentObj = JSONObject()
                contentObj.put("role", if (msg.role == "model") "model" else "user")
                val partsArr = JSONArray().apply {
                    put(JSONObject().put("text", msg.text))
                }
                contentObj.put("parts", partsArr)
                contentsArray.put(contentObj)
            }
            rootJson.put("contents", contentsArray)

            // Google Search Grounding Tool
            if (enableGoogleSearch) {
                val toolsArray = JSONArray().apply {
                    put(JSONObject().put("googleSearch", JSONObject()))
                }
                rootJson.put("tools", toolsArray)
            }

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val offlineText = AiTextFormatter.toPlainText(getSmartOfflineRoleResponse(lastUserMessage, role))
                return@withContext Result.success(
                    GeminiChatMessage(
                        role = "model",
                        text = offlineText,
                        modelUsed = "$targetModel (Offline Fallback)",
                        isSearchGrounded = enableGoogleSearch
                    )
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text")

            // Parse Grounding Metadata (Google Search Grounding)
            val groundingMetadata = candidate?.optJSONObject("groundingMetadata")
            val searchQueries = mutableListOf<String>()
            val searchSources = mutableListOf<GeminiGroundingSource>()

            if (groundingMetadata != null) {
                val webQueries = groundingMetadata.optJSONArray("webSearchQueries")
                if (webQueries != null) {
                    for (i in 0 until webQueries.length()) {
                        searchQueries.add(webQueries.optString(i))
                    }
                }

                val groundingChunks = groundingMetadata.optJSONArray("groundingChunks")
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.optJSONObject(i)
                        val web = chunk?.optJSONObject("web")
                        if (web != null) {
                            val uri = web.optString("uri")
                            val title = web.optString("title", uri)
                            if (uri.isNotBlank()) {
                                searchSources.add(GeminiGroundingSource(title = title, url = uri))
                            }
                        }
                    }
                }
            }

            val cleanOutputText = if (!rawText.isNullOrBlank()) {
                AiTextFormatter.toPlainText(rawText)
            } else {
                AiTextFormatter.toPlainText(getSmartOfflineRoleResponse(lastUserMessage, role))
            }

            val finalMessage = GeminiChatMessage(
                role = "model",
                text = cleanOutputText,
                modelUsed = targetModel,
                isSearchGrounded = enableGoogleSearch && (searchSources.isNotEmpty() || searchQueries.isNotEmpty()),
                searchQueries = searchQueries,
                searchSources = searchSources
            )

            Result.success(finalMessage)
        } catch (e: Exception) {
            val offlineText = AiTextFormatter.toPlainText(getSmartOfflineRoleResponse(lastUserMessage, role))
            Result.success(
                GeminiChatMessage(
                    role = "model",
                    text = offlineText,
                    modelUsed = "$targetModel (Offline)",
                    isSearchGrounded = enableGoogleSearch
                )
            )
        }
    }

    suspend fun generateAiResponse(
        prompt: String,
        role: SchoolRole = SchoolRole.STUDENT,
        customInstruction: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val chatResult = generateMultiTurnChat(
            history = listOf(GeminiChatMessage(role = "user", text = prompt)),
            role = role,
            modelName = GeminiChatModels.GEMINI_3_5_FLASH,
            enableGoogleSearch = false,
            customInstruction = customInstruction
        )
        val text = chatResult.getOrNull()?.text ?: getSmartOfflineRoleResponse(prompt, role)
        Result.success(AiTextFormatter.toPlainText(text))
    }

    suspend fun generateStudyAdvice(
        prompt: String,
        systemInstruction: String = "You are AcademiaTrack's AI Study Mentor."
    ): Result<String> = generateAiResponse(prompt, SchoolRole.STUDENT, systemInstruction)

    suspend fun generateCbtQuestions(
        topic: String,
        subject: String,
        classLevel: String,
        questionCount: Int = 5,
        difficulty: String = "WAEC Standard",
        marksPerQuestion: Int = 5
    ): Result<List<com.example.data.model.CbtQuestion>> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val prompt = """
            Generate exactly $questionCount multiple-choice questions for secondary school students.
            Subject: $subject
            Class Level: $classLevel
            Topic / Concept: $topic
            Difficulty: $difficulty
            Marks per question: $marksPerQuestion

            Output format MUST be a valid JSON array of objects without markdown backticks or commentary.
            Each object MUST have these exact JSON keys:
            - "questionText": String (the clear exam question)
            - "optionA": String (first option)
            - "optionB": String (second option)
            - "optionC": String (third option)
            - "optionD": String (fourth option)
            - "correctOption": String (exactly "A", "B", "C", or "D")
            - "explanation": String (concise educational rationale)
            - "marks": Int ($marksPerQuestion)

            Example:
            [
              {
                "questionText": "What is the acceleration due to gravity on Earth's surface approximately?",
                "optionA": "5.2 m/s²",
                "optionB": "9.8 m/s²",
                "optionC": "12.4 m/s²",
                "optionD": "15.0 m/s²",
                "correctOption": "B",
                "explanation": "Standard gravitational acceleration at sea level is approximately 9.8 m/s² (or 10 m/s² rounded).",
                "marks": $marksPerQuestion
              }
            ]
        """.trimIndent()

        val systemInstruction = "You are AcademiaTrack's AI Exam Master & CBT Question Setter. You create curriculum-aligned, high-precision multiple choice exam questions. Output ONLY a valid JSON array."

        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.success(getSmartOfflineCbtQuestions(topic, subject, classLevel, questionCount, marksPerQuestion))
        }

        try {
            val url = "$BASE_URL/$DEFAULT_MODEL:generateContent?key=$apiKey"
            val rootJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysPartsArray = JSONArray().apply {
                put(JSONObject().put("text", systemInstruction))
            }
            sysInstructionObj.put("parts", sysPartsArray)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
            }
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            val body = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.success(getSmartOfflineCbtQuestions(topic, subject, classLevel, questionCount, marksPerQuestion))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.optJSONObject(0)?.optString("text") ?: ""

            val parsedQuestions = parseCbtQuestionsJson(rawText, marksPerQuestion)
            if (parsedQuestions.isNotEmpty()) {
                Result.success(parsedQuestions)
            } else {
                Result.success(getSmartOfflineCbtQuestions(topic, subject, classLevel, questionCount, marksPerQuestion))
            }
        } catch (e: Exception) {
            Result.success(getSmartOfflineCbtQuestions(topic, subject, classLevel, questionCount, marksPerQuestion))
        }
    }

    private fun parseCbtQuestionsJson(rawText: String, defaultMarks: Int): List<com.example.data.model.CbtQuestion> {
        val result = mutableListOf<com.example.data.model.CbtQuestion>()
        try {
            // Clean markdown blocks if present
            var jsonStr = rawText.trim()
            if (jsonStr.startsWith("```json")) {
                jsonStr = jsonStr.substringAfter("```json")
            }
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.substringAfter("```")
            }
            if (jsonStr.endsWith("```")) {
                jsonStr = jsonStr.substringBeforeLast("```")
            }
            jsonStr = jsonStr.trim()

            val startIndex = jsonStr.indexOf('[')
            val endIndex = jsonStr.lastIndexOf(']')
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                jsonStr = jsonStr.substring(startIndex, endIndex + 1)
            }

            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val qText = obj.optString("questionText").takeIf { it.isNotBlank() } ?: continue
                val optA = obj.optString("optionA").takeIf { it.isNotBlank() } ?: "Option A"
                val optB = obj.optString("optionB").takeIf { it.isNotBlank() } ?: "Option B"
                val optC = obj.optString("optionC").takeIf { it.isNotBlank() } ?: "Option C"
                val optD = obj.optString("optionD").takeIf { it.isNotBlank() } ?: "Option D"
                val correct = obj.optString("correctOption", "A").uppercase().trim().take(1).let {
                    if (it in listOf("A", "B", "C", "D")) it else "A"
                }
                val explanation = obj.optString("explanation", "")
                val marks = obj.optInt("marks", defaultMarks)

                result.add(
                    com.example.data.model.CbtQuestion(
                        examId = 0,
                        questionNumber = i + 1,
                        questionText = qText,
                        optionA = optA,
                        optionB = optB,
                        optionC = optC,
                        optionD = optD,
                        correctOption = correct,
                        explanation = explanation,
                        marks = marks
                    )
                )
            }
        } catch (e: Exception) {
            // Parsing error handling
        }
        return result
    }

    private fun getSmartOfflineCbtQuestions(
        topic: String,
        subject: String,
        classLevel: String,
        count: Int,
        marks: Int
    ): List<com.example.data.model.CbtQuestion> {
        val lowerTopic = topic.lowercase()
        val lowerSub = subject.lowercase()

        val templates: List<com.example.data.model.CbtQuestion> = when {
            // Mathematics
            "math" in lowerSub || "algebra" in lowerTopic || "quadratic" in lowerTopic || "equation" in lowerTopic || "calculus" in lowerTopic || "trig" in lowerTopic -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "If 2x + 7 = 19, what is the value of x?",
                    optionA = "x = 4", optionB = "x = 6", optionC = "x = 8", optionD = "x = 12",
                    correctOption = "B", explanation = "Subtract 7 from both sides: 2x = 12, then divide by 2: x = 6.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "Find the roots of the quadratic equation x² - 5x + 6 = 0.",
                    optionA = "x = 2 or x = 3", optionB = "x = -2 or x = -3", optionC = "x = 1 or x = 6", optionD = "x = -1 or x = -6",
                    correctOption = "A", explanation = "Factorizing (x - 2)(x - 3) = 0 gives roots x = 2 and x = 3.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "Calculate the hypotenuse of a right-angled triangle with base 6 cm and height 8 cm.",
                    optionA = "14 cm", optionB = "10 cm", optionC = "12 cm", optionD = "100 cm",
                    correctOption = "B", explanation = "By Pythagorean theorem: c = √(6² + 8²) = √(36 + 64) = √100 = 10 cm.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "What is the gradient (slope) of the line represented by 3x - y = 8?",
                    optionA = "-3", optionB = "8", optionC = "3", optionD = "1/3",
                    correctOption = "C", explanation = "Rearranging to slope-intercept form y = mx + c gives y = 3x - 8, so slope m = 3.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "Evaluate log₁₀(10000).",
                    optionA = "2", optionB = "3", optionC = "4", optionD = "10",
                    correctOption = "C", explanation = "10⁴ = 10000, so log₁₀(10000) = 4.", marks = marks
                )
            )

            // Physics
            "physic" in lowerSub || "motion" in lowerTopic || "force" in lowerTopic || "electricity" in lowerTopic || "wave" in lowerTopic || "energy" in lowerTopic -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "According to Newton's Second Law of Motion, Force is equal to:",
                    optionA = "Mass × Velocity", optionB = "Mass × Acceleration", optionC = "Work ÷ Time", optionD = "Mass × Gravity × Height",
                    correctOption = "B", explanation = "Newton's second law states that Force F = m × a.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "A car moves with a constant speed of 25 m/s for 12 seconds. What distance does it cover?",
                    optionA = "200 m", optionB = "250 m", optionC = "300 m", optionD = "350 m",
                    correctOption = "C", explanation = "Distance = Speed × Time = 25 m/s × 12 s = 300 m.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "What is the SI unit of Electric Potential Difference (Voltage)?",
                    optionA = "Ampere (A)", optionB = "Ohm (Ω)", optionC = "Volt (V)", optionD = "Watt (W)",
                    correctOption = "C", explanation = "Electric potential difference is measured in Volts (V).", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "Which type of wave does NOT require a material medium for propagation?",
                    optionA = "Sound waves", optionB = "Water waves", optionC = "Electromagnetic waves", optionD = "Seismic waves",
                    correctOption = "C", explanation = "Electromagnetic waves (such as light and radio waves) can propagate through a vacuum.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "The rate of doing work or transferring energy is called:",
                    optionA = "Power", optionB = "Pressure", optionC = "Momentum", optionD = "Friction",
                    correctOption = "A", explanation = "Power is defined as Work done per unit time (P = W/t).", marks = marks
                )
            )

            // Chemistry
            "chem" in lowerSub || "acid" in lowerTopic || "periodic" in lowerTopic || "organic" in lowerTopic || "atom" in lowerTopic -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "What is the pH value of a neutral aqueous solution at 25°C?",
                    optionA = "0", optionB = "7", optionC = "14", optionD = "1",
                    correctOption = "B", explanation = "A neutral solution has a pH of exactly 7.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "Which gas is evolved when dilute hydrochloric acid reacts with calcium carbonate?",
                    optionA = "Hydrogen (H₂)", optionB = "Oxygen (O₂)", optionC = "Carbon dioxide (CO₂)", optionD = "Nitrogen (N₂)",
                    correctOption = "C", explanation = "Acid + Carbonate → Salt + Water + Carbon Dioxide gas.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "What is the chemical formula for ordinary table salt?",
                    optionA = "NaOH", optionB = "NaCl", optionC = "KCl", optionD = "CaCO₃",
                    correctOption = "B", explanation = "Table salt is Sodium Chloride (NaCl).", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "The atomic number of an element is determined by the number of:",
                    optionA = "Neutrons", optionB = "Protons in the nucleus", optionC = "Electrons and neutrons combined", optionD = "Valence shells",
                    correctOption = "B", explanation = "The atomic number (Z) equals the number of protons in an atom's nucleus.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "Which of the following is an alkene?",
                    optionA = "Methane (CH₄)", optionB = "Ethene (C₂H₄)", optionC = "Ethane (C₂H₆)", optionD = "Propane (C₃H₈)",
                    correctOption = "B", explanation = "Alkenes follow the general formula CnH2n (e.g. Ethene C₂H₄).", marks = marks
                )
            )

            // Biology
            "bio" in lowerSub || "cell" in lowerTopic || "photosynthesis" in lowerTopic || "genetics" in lowerTopic || "ecology" in lowerTopic -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "Which cell organelle is primarily responsible for protein synthesis?",
                    optionA = "Mitochondrion", optionB = "Ribosome", optionC = "Lysosome", optionD = "Vacuole",
                    correctOption = "B", explanation = "Ribosomes are the cellular machinery where amino acids are assembled into proteins.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "What green pigment absorbs sunlight during the process of photosynthesis?",
                    optionA = "Carotene", optionB = "Chlorophyll", optionC = "Xanthophyll", optionD = "Hemoglobin",
                    correctOption = "B", explanation = "Chlorophyll in chloroplasts captures light energy for photosynthesis.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "Which blood cells are primarily responsible for fighting infections in the human body?",
                    optionA = "Red Blood Cells (Erythrocytes)", optionB = "White Blood Cells (Leukocytes)", optionC = "Platelets (Thrombocytes)", optionD = "Plasma",
                    correctOption = "B", explanation = "Leukocytes (white blood cells) are the immune system's primary defense.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "In genetics, an individual with two identical alleles for a particular gene is described as:",
                    optionA = "Heterozygous", optionB = "Homozygous", optionC = "Phenotype", optionD = "Recessive",
                    correctOption = "B", explanation = "Homozygous means having identical alleles (e.g. BB or bb).", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "The process of maintaining a stable internal physiological environment in living organisms is known as:",
                    optionA = "Excretion", optionB = "Metabolism", optionC = "Homeostasis", optionD = "Osmosis",
                    correctOption = "C", explanation = "Homeostasis is the regulation and maintenance of constant internal conditions.", marks = marks
                )
            )

            // English & Literature
            "eng" in lowerSub || "lit" in lowerSub || "grammar" in lowerTopic || "concord" in lowerTopic || "speech" in lowerTopic -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "Choose the grammatically correct option: Neither the teacher nor the students ______ present in the auditorium.",
                    optionA = "was", optionB = "is", optionC = "were", optionD = "has been",
                    correctOption = "C", explanation = "In 'Neither... nor' constructions, the verb agrees with the closer subject ('students' -> plural 'were').", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "Identify the figure of speech in: 'The classroom was a bustling beehive of activity.'",
                    optionA = "Simile", optionB = "Metaphor", optionC = "Personification", optionD = "Hyperbole",
                    correctOption = "B", explanation = "A metaphor directly equates two distinct things without using 'like' or 'as'.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "Select the word that is nearest in meaning (synonym) to 'METICULOUS':",
                    optionA = "Careless", optionB = "Punctual", optionC = "Painstaking and precise", optionD = "Aggressive",
                    correctOption = "C", explanation = "Meticulous means showing great attention to detail; very careful and precise.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "Complete the idiom: 'To throw in the ______' means to surrender or admit defeat.",
                    optionA = "sponge", optionB = "towel", optionC = "hat", optionD = "rope",
                    correctOption = "B", explanation = "The idiom 'throw in the towel' originated from boxing to indicate surrender.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "What part of speech is the word 'ELEGANTLY' in the sentence: 'She danced elegantly across the stage'?",
                    optionA = "Adjective", optionB = "Noun", optionC = "Adverb", optionD = "Preposition",
                    correctOption = "C", explanation = "'Elegantly' modifies the verb 'danced', making it an adverb of manner.", marks = marks
                )
            )

            // Computer Science / ICT
            "comp" in lowerSub || "ict" in lowerSub || "network" in lowerTopic || "code" in lowerTopic || "data" in lowerTopic -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "Which component of the computer CPU is responsible for carrying out arithmetic and logic operations?",
                    optionA = "Control Unit (CU)", optionB = "Arithmetic Logic Unit (ALU)", optionC = "Registers", optionD = "BIOS",
                    correctOption = "B", explanation = "The ALU performs all fundamental arithmetic and logical decisions.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "What does the acronym HTTP stand for in computer networking?",
                    optionA = "HyperText Transfer Protocol", optionB = "High Tech Transmission Program", optionC = "Hyperlink Text Testing Process", optionD = "Host Transport Terminal Protocol",
                    correctOption = "A", explanation = "HTTP stands for HyperText Transfer Protocol.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "Which of the following is volatile primary computer storage memory?",
                    optionA = "ROM", optionB = "Hard Disk Drive", optionC = "RAM", optionD = "Flash Drive",
                    correctOption = "C", explanation = "RAM (Random Access Memory) is volatile and loses its contents when power is lost.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "How many bits make up one standard byte?",
                    optionA = "4", optionB = "8", optionC = "16", optionD = "32",
                    correctOption = "B", explanation = "1 Byte = 8 Bits.", marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "Which database language command is used to retrieve data from a relational table?",
                    optionA = "INSERT", optionB = "DELETE", optionC = "SELECT", optionD = "UPDATE",
                    correctOption = "C", explanation = "The SQL SELECT statement queries and retrieves records.", marks = marks
                )
            )

            // Default General Academic / Topic-Customized
            else -> listOf(
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 1,
                    questionText = "Regarding the study of $topic in $subject, which of the following statements is fundamentally correct?",
                    optionA = "It represents an inverse relationship between cause and consequence.",
                    optionB = "It establishes the standard foundational principles applicable across $classLevel.",
                    optionC = "It applies solely to theoretical models without practical application.",
                    optionD = "It has been superseded by non-standard empirical methods.",
                    correctOption = "B",
                    explanation = "Standard curriculum syllabus mandates foundational mastery of $topic for $classLevel students.",
                    marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 2,
                    questionText = "What is the primary objective of analyzing $topic in the context of secondary school $subject?",
                    optionA = "To memorize isolated terminology without problem solving.",
                    optionB = "To develop analytical reasoning, precision, and application to real-world scenarios.",
                    optionC = "To avoid continuous assessment testing.",
                    optionD = "To limit exploration to historical definitions.",
                    correctOption = "B",
                    explanation = "Curriculum objectives prioritize analytical competence and practical application.",
                    marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 3,
                    questionText = "Which of the following is considered a core criterion when evaluating $topic?",
                    optionA = "Consistent empirical evidence and systematic methodology.",
                    optionB = "Subjective personal opinion without data.",
                    optionC = "Unverified random hypotheses.",
                    optionD = "Exclusion of standard units and formulas.",
                    correctOption = "A",
                    explanation = "Scientific and academic evaluations rely strictly on verifiable empirical evidence.",
                    marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 4,
                    questionText = "When solving standard exam problems on $topic, what is the recommended first step?",
                    optionA = "Guess the final option immediately.",
                    optionB = "Identify the given variables, required outcome, and appropriate formula or rule.",
                    optionC = "Disregard boundary conditions.",
                    optionD = "Assume all coefficients are zero.",
                    correctOption = "B",
                    explanation = "Careful variable identification ensures correct formula application and error-free derivation.",
                    marks = marks
                ),
                com.example.data.model.CbtQuestion(
                    examId = 0, questionNumber = 5,
                    questionText = "In a WAEC/NECO examination setting, high mastery of $topic ($subject) is demonstrated by:",
                    optionA = "Clear step-by-step reasoning and accurate selection of key principles.",
                    optionB = "Leaving multiple choice answer keys blank.",
                    optionC = "Ignoring exam instructions and timer limits.",
                    optionD = "Selecting all four options simultaneously.",
                    correctOption = "A",
                    explanation = "Comprehensive mastery requires disciplined reasoning, accuracy, and adherence to time management.",
                    marks = marks
                )
            )
        }

        return templates.take(count).mapIndexed { idx, q ->
            q.copy(questionNumber = idx + 1, marks = marks)
        }
    }

    private fun getSmartOfflineRoleResponse(prompt: String, role: SchoolRole): String {
        val lower = prompt.lowercase()
        return when (role) {
            SchoolRole.ADMIN -> when {
                "circular" in lower || "memo" in lower || "notice" in lower || "parent" in lower -> """
OFFICIAL SCHOOL CIRCULAR MEMO
To: All Esteemed Parents, Guardians & Faculty
From: The Office of the Principal & School Administration
Subject: 1st Term Academic Updates, Mid-Term Assessments & Upcoming PTA Forum

Dear Parents and Guardians,

We commend our students for their diligent academic strides this term. Please take note of the following vital school notices:

1. Continuous Assessment (CA) & CBT Mid-Term: Commencing on Monday next week. Please ensure wards review their daily class portals and CBT revision practice.
2. School Fees Clearance: Parents with outstanding fee balances are kindly requested to complete settlements to avoid examination seating delays.
3. PTA General Assembly: Scheduled for Saturday at 10:00 AM in the College Auditorium. Terminal report cards and infrastructure upgrades will be reviewed.

Signed:
Dr. C. Adebayo, Ph.D
Principal, Kingsway Model College
                """.trimIndent()

                "calendar" in lower || "timetable" in lower || "term" in lower -> """
ACADEMIC TERM CALENDAR & OPERATIONS TIMETABLE
• Week 1 - 4: Curriculum Delivery & 1st Continuous Assessment (15 Marks)
• Week 5 - 7: Mid-Term Practical Labs, 2nd CA (15 Marks) & Mid-Term Exam (20 Marks)
• Week 8: Mid-Term Break & Staff Evaluation Forum
• Week 9 - 11: Terminal Revision, CBT Mock Drills & Revision Workshops
• Week 12: Final Terminal Computer-Based Examination (50 Marks)
• Week 13: CA Compilation, Class Teacher Moderation, Admin Sealing & Report Card Release
                """.trimIndent()

                "discipline" in lower || "commendation" in lower || "letter" in lower -> """
OFFICIAL STUDENT COMMENDATION & ACADEMIC HONORS CITATION
Date: Current Academic Session
Recipient: Chidinma Nwosu (Adm. No: ADM-SS2-042, SS 2 Gold)

Citation of Merit:
The Board of Governors and Academic Council proudly confer this Principal's Certificate of Academic Distinction upon you for demonstrating outstanding scholarly aptitude, securing 1st Position with an aggregate average of 88.4%, and exemplifying impeccable character. Keep flying the school's banner of excellence high!
                """.trimIndent()

                else -> """
EXECUTIVE SCHOOL ADMINISTRATION STRATEGY & METRICS
• Curriculum Adherence: Monitor real-time Teacher Register clock-ins and CA gradebook publishing status.
• CBT Infrastructure: Ensure backup power generators and LAN servers are pre-tested before scheduled examinations.
• Parental Engagement: Maintain proactive updates via the announcement board and digital report card approvals.
                """.trimIndent()
            }

            SchoolRole.TEACHER -> when {
                "lesson" in lower || "plan" in lower || "note" in lower -> """
STRUCTURED SECONDARY LESSON PLAN
Subject: Mathematics / Physics
Class Level: SS 2 | Duration: 45 Minutes
Topic: Quadratic Equations by Factorization & Graphic Analysis

BEHAVIORAL OBJECTIVES
By the end of the lesson, students should be able to:
1. Identify the standard form of a quadratic equation (ax² + bx + c = 0).
2. Factorize quadratic expressions where coefficient a = 1 and a > 1.
3. Solve for real roots accurately and check solutions.

INSTRUCTIONAL MATERIALS
• Algebraic tile models, Grid whiteboard, Scientific calculators.

LESSON PRESENTATION STAGES
• Step 1 (Introduction - 5 mins): Review linear factoring and perfect squares.
• Step 2 (Concept Development - 20 mins): Demonstrate splitting the middle term (p + q = b, p * q = a * c).
• Step 3 (Guided Practice - 10 mins): Solve 2x² - 5x - 3 = 0 -> (2x+1)(x-3)=0 -> x=3, x=-1/2.
• Step 4 (Evaluation - 7 mins): Students solve 3x² + 7x + 2 = 0 individually in notebooks.
• Step 5 (Assignment - 3 mins): Textbook Exercises 4B, Questions 1 to 10.
                """.trimIndent()

                "cbt" in lower || "question" in lower || "quiz" in lower -> """
CURRICULUM-ALIGNED CBT EXAM QUESTION BANK

Question 1: A car accelerates uniformly from rest at 3.0 m/s² for 6 seconds. What is its final velocity?
• A: 9.0 m/s
• B: 18.0 m/s (Correct Key)
• C: 54.0 m/s
• D: 12.0 m/s
Explanation: v = u + at = 0 + (3.0 * 6) = 18.0 m/s.

Question 2: If log10(x) = 3, find the value of x.
• A: 30
• B: 100
• C: 1000 (Correct Key)
• D: 0.001
Explanation: By logarithmic definition, x = 10³ = 1000.

Question 3: Which organelle is recognized as the powerhouse of the biological cell?
• A: Ribosome
• B: Mitochondrion (Correct Key)
• C: Golgi Apparatus
• D: Endoplasmic Reticulum
Explanation: Mitochondria generate most of the cell's supply of ATP energy.
                """.trimIndent()

                "remark" in lower || "comment" in lower || "report" in lower -> """
PERSONALIZED TERMINAL REPORT CARD REMARKS

• For High Distinction (80% - 100%):
  A phenomenal and disciplined student with sharp analytical aptitude. Consistently leads the class with exemplary character and academic excellence.

• For Strong Performance (65% - 79%):
  A very commendable effort with solid mastery across all core subjects. With focused consistency in science subjects, higher laurels are well within reach.

• For Developing Students (50% - 64%):
  Shows good potential and willingness to learn. Encouraged to participate more actively in revision sessions and complete weekly CBT practice tests.
                """.trimIndent()

                else -> """
TEACHER PEDAGOGICAL TOOLKIT
• Differentiated Learning: Pair students in peer study groups during challenging calculations.
• Formative Assessment: Use 5-minute exit tickets or mini-CBT quizzes to gauge retention after every lesson.
• Active Feedback: Leverage the CA grading screen to provide constructive remarks before publishing report cards.
                """.trimIndent()
            }

            SchoolRole.STUDENT -> when {
                "quiz" in lower || "test" in lower || "practice" in lower -> """
STUDENT CBT QUICK PRACTICE QUIZ

1. Question 1: Evaluate the derivative of (4x³ - 5x + 7) with respect to x.
   Solution: Using power rule -> 12x² - 5.

2. Question 2: What is the SI unit of Electric Resistance?
   Solution: Ohm (Ω).

3. Question 3: Identify the figure of speech in: "The wind whispered through the dark trees".
   Solution: Personification (giving human traits to inanimate nature).
                """.trimIndent()

                "schedule" in lower || "timetable" in lower || "plan" in lower -> """
RECOMMENDED DAILY STUDENT STUDY TIMETABLE
• 4:00 PM - 5:15 PM: Mathematics & Calculation Subjects (Sharp focus while mind is fresh)
• 5:15 PM - 5:30 PM: Active Break & Hydration
• 5:30 PM - 6:45 PM: Physics / Chemistry Lab Theories & Theorem Proofs
• 6:45 PM - 7:30 PM: Dinner & Family Time
• 7:30 PM - 8:30 PM: English Language Vocabulary, Lexis & Literature Summaries
• 8:30 PM - 9:00 PM: Flashcard Active Recall & Tomorrow's CBT Review
                """.trimIndent()

                "homework" in lower || "math" in lower || "solve" in lower || "explain" in lower -> """
STEP-BY-STEP PROBLEM SOLVER & CONCEPT GUIDE

Topic: Solving Quadratic Equations with the Quadratic Formula
Formula: x = (-b ± √(b² - 4ac)) / (2a)

1. Step 1: Compare your equation with ax² + bx + c = 0 to identify coefficients a, b, and c.
2. Step 2: Calculate the Discriminant Δ = b² - 4ac. If Δ > 0, there are two real distinct roots.
3. Step 3: Substitute into the formula and compute roots for +√Δ and -√Δ.
4. Step 4: Check your answers by substituting roots back into the original equation!
                """.trimIndent()

                else -> """
TOP EXAM SUCCESS STRATEGIES
• Active Recall over Passive Reading: Close your notes and write key formulas from memory.
• Teach to Master (Feynman Technique): Explain challenging concepts to your classmate in the Peer Study Room.
• Timed CBT Practice: Take practice tests on AcademiaTrack with the countdown timer to build exam confidence.
                """.trimIndent()
            }

            SchoolRole.PARENT -> """
PARENT ACADEMIC SUPPORT & PROGRESS GUIDE
• Review CA Grades: Check your child's Continuous Assessment scores regularly under the Report Card tab.
• Daily Study Routine: Ensure a quiet, well-lit study environment for 90 minutes each evening.
• Open Communication: Reach out directly to class teachers via the portal if your child needs extra subject reinforcement.
            """.trimIndent()

            SchoolRole.APP_OWNER -> """
APP PLATFORM OWNER SAAS ADVISOR
• Remote Feature Locking: You can lock or unlock CBT Exams, AI Tutors, and Report Cards per school.
• Broadcast Memos: Send instant payment reminders and invoices with your bank account details.
• License Activation: Generate 16-character license keys to activate school subscriptions after payment verification.
            """.trimIndent()
        }
    }
}

