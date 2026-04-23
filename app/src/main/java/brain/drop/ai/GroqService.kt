package brain.drop.ai

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

 data class GroqMessage(val role: String, val content: String)
 data class GroqRequest(
    val model: String = "llama-3.1-8b-instant",
    val messages: List<GroqMessage>,
    val temperature: Double = 0.3,
    val max_tokens: Int = 1024
)
 data class GroqChoice(val message: GroqMessage)
 data class GroqResponse(val choices: List<GroqChoice>)

 data class WhisperRequest(val model: String = "whisper-large-v3", val file: String)
 data class WhisperResponse(val text: String)

interface GroqApi {
    @POST("v1/chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: GroqRequest
    ): Response<GroqResponse>

    @POST("v1/audio/transcriptions")
    suspend fun transcribe(
        @Header("Authorization") auth: String,
        @Body request: WhisperRequest
    ): Response<WhisperResponse>
}

@Singleton
class GroqService @Inject constructor() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GroqApi::class.java)
    private val apiKey = brain.drop.BuildConfig.GROQ_API_KEY

    suspend fun categorizeNote(content: String): AICategorizationResult {
        val prompt = """
            Analyze this note and categorize it. Respond ONLY in this exact JSON format:
            {"category":"idea|task|research|code|reference|quote|link","title":"short title","summary":"one line summary","tags":["tag1","tag2"],"isTemporary":false,"suggestedDeleteHours":null}

            Note content: $content
        """.trimIndent()

        return try {
            val response = api.chatCompletion(
                "Bearer $apiKey",
                GroqRequest(messages = listOf(GroqMessage("user", prompt)))
            )
            if (response.isSuccessful) {
                val text = response.body()?.choices?.firstOrNull()?.message?.content ?: "{}"
                parseCategorization(text)
            } else {
                AICategorizationResult(category = "uncategorized")
            }
        } catch (e: Exception) {
            AICategorizationResult(category = "uncategorized")
        }
    }

    suspend fun transcribeAudio(audioBase64: String): String {
        return try {
            val response = api.transcribe(
                "Bearer $apiKey",
                WhisperRequest(file = audioBase64)
            )
            response.body()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun parseCategorization(json: String): AICategorizationResult {
        return try {
            val clean = json.replace("```json", "").replace("```", "").trim()
            val map = com.google.gson.JsonParser.parseString(clean).asJsonObject
            AICategorizationResult(
                category = map.get("category")?.asString ?: "uncategorized",
                title = map.get("title")?.asString,
                summary = map.get("summary")?.asString,
                tags = map.getAsJsonArray("tags")?.map { it.asString } ?: emptyList(),
                isTemporary = map.get("isTemporary")?.asBoolean ?: false,
                suggestedDeleteHours = map.get("suggestedDeleteHours")?.asInt
            )
        } catch (e: Exception) {
            AICategorizationResult(category = "uncategorized")
        }
    }
}

 data class AICategorizationResult(
    val category: String = "uncategorized",
    val title: String? = null,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
    val isTemporary: Boolean = false,
    val suggestedDeleteHours: Int? = null
)
