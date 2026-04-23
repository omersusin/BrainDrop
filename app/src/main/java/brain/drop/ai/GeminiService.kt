package brain.drop.ai

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

interface GeminiApi {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Response<GeminiResponse>
}

 data class GeminiRequest(val contents: List<GeminiContent>)
 data class GeminiContent(val parts: List<GeminiPart>)
 data class GeminiPart(val text: String)
 data class GeminiResponse(val candidates: List<GeminiCandidate>?)
 data class GeminiCandidate(val content: GeminiContent?)

@Singleton
class GeminiService @Inject constructor() {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GeminiApi::class.java)
    private val apiKey = brain.drop.BuildConfig.GEMINI_API_KEY

    suspend fun synthesizeNotes(notesText: String): String {
        val prompt = "Synthesize these notes into key insights and connections:

$notesText"
        return try {
            val response = api.generateContent(
                apiKey,
                GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))))
            )
            response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun extractLinkSummary(url: String): String {
        val prompt = "Summarize the content at this URL in 2 sentences: $url"
        return try {
            val response = api.generateContent(
                apiKey,
                GeminiRequest(contents = listOf(GeminiContent(parts = listOf(GeminiPart(prompt)))))
            )
            response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
