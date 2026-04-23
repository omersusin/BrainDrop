package brain.drop.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import brain.drop.ai.GroqService
import brain.drop.db.Note
import brain.drop.db.NoteRepository
import brain.drop.ml.EmbeddingModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AINoteProcessor @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NoteRepository,
    private val groqService: GroqService,
    private val embeddingModel: EmbeddingModel
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getLong("note_id", -1)
        if (noteId == -1L) return Result.failure()

        val note = repository.getNoteById(noteId) ?: return Result.failure()

        return try {
            val result = groqService.categorizeNote(note.content)
            val embedding = embeddingModel.generateEmbedding(note.content)
            val embeddingStr = embeddingModel.embeddingToString(embedding)

            val allNotes = repository.getAllNotes()
            val similarNotes = allNotes.filter { other ->
                other.id != note.id && other.contentVector != null && embedding != null && run {
                    val otherVec = embeddingModel.stringToEmbedding(other.contentVector)
                    otherVec != null && embeddingModel.cosineSimilarity(embedding, otherVec) > 0.75f
                }
            }.map { it.id.toString() }

            val updatedNote = note.copy(
                category = result.category,
                title = result.title ?: note.title,
                summary = result.summary,
                tags = result.tags,
                isTemporary = result.isTemporary,
                autoDeleteAt = result.suggestedDeleteHours?.let {
                    System.currentTimeMillis() + (it * 3600000)
                },
                contentVector = embeddingStr,
                linkedNoteIds = similarNotes.joinToString(",").ifEmpty { null },
                isProcessed = true,
                updatedAt = System.currentTimeMillis()
            )

            repository.updateNote(updatedNote)
            Result.success()
        } catch (e: Exception) {
            val failedNote = note.copy(
                isProcessed = false,
                processingError = e.message
            )
            repository.updateNote(failedNote)
            Result.retry()
        }
    }
}
