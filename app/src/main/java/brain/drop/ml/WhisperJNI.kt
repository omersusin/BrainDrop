package brain.drop.ml

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhisperJNI @Inject constructor() {

    init {
        System.loadLibrary("whisper")
    }

    external fun transcribeFromFile(modelPath: String, audioPath: String): String
    external fun transcribeFromBuffer(modelPath: String, audioData: ByteArray): String

    fun transcribeOffline(audioData: ByteArray): String {
        return try {
            transcribeFromBuffer("/data/data/brain.drop/files/whisper-tiny.en.bin", audioData)
        } catch (e: Exception) {
            ""
        }
    }
}
