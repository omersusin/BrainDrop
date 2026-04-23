package brain.drop.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "notes")
@TypeConverters(StringListConverter::class)
 data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String = "",
    val title: String? = null,
    val summary: String? = null,
    val category: String = "uncategorized",
    val projectId: Long? = null,
    val projectName: String? = null,
    val tags: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isTemporary: Boolean = false,
    val autoDeleteAt: Long? = null,
    val isCompleted: Boolean = false,
    val sourceType: String = "text",
    val mediaUri: String? = null,
    val extractedText: String? = null,
    val isProcessed: Boolean = false,
    val processingError: String? = null,
    val contentVector: String? = null,
    val linkedNoteIds: String? = null
)
