package brain.drop.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmbeddingModel @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val modelPath = "all-MiniLM-L6-v2-quant.tflite"
    private val vectorDim = 384

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    fun generateEmbedding(text: String): FloatArray? {
        val interpreter = this.interpreter ?: return null
        return try {
            val input = tokenize(text)
            val output = Array(1) { FloatArray(vectorDim) }
            interpreter.run(input, output)
            output[0]
        } catch (e: Exception) {
            null
        }
    }

    fun embeddingToString(embedding: FloatArray?): String? {
        return embedding?.joinToString(",")
    }

    fun stringToEmbedding(str: String?): FloatArray? {
        return str?.split(",")?.map { it.toFloat() }?.toFloatArray()
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return dot / (kotlin.math.sqrt(normA) * kotlin.math.sqrt(normB))
    }

    private fun tokenize(text: String): Array<LongArray> {
        val maxLen = 128
        val tokens = LongArray(maxLen) { 0L }
        val words = text.lowercase().split("\s+".toRegex()).take(maxLen)
        words.forEachIndexed { i, word ->
            tokens[i] = word.hashCode().toLong().and(0xFFFFFFFFL)
        }
        return arrayOf(tokens)
    }
}
