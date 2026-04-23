package brain.drop.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import brain.drop.db.NoteRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AutoDeleteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: NoteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val expired = repository.getTemporaryNotes()
        if (expired.isNotEmpty()) {
            repository.deleteNotes(expired)
        }
        return Result.success()
    }
}
