package brain.drop.db

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    suspend fun getAllNotes(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getAllNotes()
    }

    fun getNotesFlow(): Flow<List<Note>> = noteDao.getRecentNotesFlow(100)

    suspend fun getNotesByCategory(category: String): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getNotesByCategory(category)
    }

    suspend fun getNotesByProject(projectId: Long): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getNotesByProject(projectId)
    }

    suspend fun getUnprocessedNotes(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getUnprocessedNotes()
    }

    suspend fun getTemporaryNotes(): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getExpiredTemporaryNotes(System.currentTimeMillis())
    }

    suspend fun searchNotes(query: String): List<Note> = withContext(Dispatchers.IO) {
        noteDao.searchNotes(query)
    }

    suspend fun insertNote(note: Note): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNotes(notes: List<Note>) = withContext(Dispatchers.IO) {
        noteDao.deleteNotes(notes)
    }

    suspend fun getNoteById(id: Long): Note? = withContext(Dispatchers.IO) {
        noteDao.getNoteById(id)
    }

    suspend fun getRecentNotes(limit: Int = 50): List<Note> = withContext(Dispatchers.IO) {
        noteDao.getRecentNotes(limit)
    }
}
