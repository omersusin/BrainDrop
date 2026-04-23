package brain.drop.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentNotes(limit: Int = 100): List<Note>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentNotesFlow(limit: Int = 100): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getNotesByCategory(category: String): List<Note>

    @Query("SELECT * FROM notes WHERE projectId = :projectId ORDER BY timestamp DESC")
    suspend fun getNotesByProject(projectId: Long): List<Note>

    @Query("SELECT * FROM notes WHERE isProcessed = 0")
    suspend fun getUnprocessedNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE isTemporary = 1 AND autoDeleteAt < :now")
    suspend fun getExpiredTemporaryNotes(now: Long): List<Note>

    @Query("SELECT * FROM notes WHERE content LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchNotes(query: String): List<Note>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Delete
    suspend fun deleteNotes(notes: List<Note>)
}
