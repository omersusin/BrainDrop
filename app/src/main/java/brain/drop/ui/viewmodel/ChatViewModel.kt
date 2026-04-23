package brain.drop.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import brain.drop.db.Note
import brain.drop.db.NoteRepository
import brain.drop.worker.AINoteProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: NoteRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _focusMode = MutableStateFlow(false)
    val focusMode: StateFlow<Boolean> = _focusMode.asStateFlow()

    private var lastDeletedNote: Note? = null
    private var isRecording = false

    init {
        loadNotes()
    }

    private fun loadNotes() {
        viewModelScope.launch {
            _notes.value = repository.getRecentNotes(100)
        }
    }

    fun onInputChange(text: String) {
        _inputText.value = text
    }

    fun sendText() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return

        val note = Note(
            content = text,
            sourceType = "text",
            isProcessed = false
        )

        viewModelScope.launch {
            val id = repository.insertNote(note)
            _inputText.value = ""
            loadNotes()
            enqueueAIProcessing(id)
        }
    }

    fun sendImage(uri: Uri) {
        val note = Note(
            content = "Image note",
            sourceType = "image",
            mediaUri = uri.toString(),
            isProcessed = false
        )
        viewModelScope.launch {
            val id = repository.insertNote(note)
            loadNotes()
            enqueueAIProcessing(id)
        }
    }

    fun sendFile(uri: Uri) {
        val note = Note(
            content = "File note",
            sourceType = "file",
            mediaUri = uri.toString(),
            isProcessed = false
        )
        viewModelScope.launch {
            val id = repository.insertNote(note)
            loadNotes()
            enqueueAIProcessing(id)
        }
    }

    fun startRecording() {
        isRecording = true
    }

    fun stopRecording() {
        isRecording = false
        val note = Note(
            content = "Voice note (processing...)",
            sourceType = "voice",
            isProcessed = false
        )
        viewModelScope.launch {
            val id = repository.insertNote(note)
            loadNotes()
            enqueueAIProcessing(id)
        }
    }

    fun deleteNote(note: Note) {
        lastDeletedNote = note
        viewModelScope.launch {
            repository.deleteNote(note)
            loadNotes()
        }
    }

    fun undoDelete(note: Note) {
        viewModelScope.launch {
            repository.insertNote(note)
            loadNotes()
        }
    }

    fun toggleFocusMode() {
        _focusMode.value = !_focusMode.value
    }

    fun startBrainDumpTimer(minutes: Int) {
        val note = Note(
            content = "Brain dump session started (${minutes}min)",
            sourceType = "timer",
            isTemporary = true,
            autoDeleteAt = System.currentTimeMillis() + (minutes * 60 * 1000),
            isProcessed = true
        )
        viewModelScope.launch {
            repository.insertNote(note)
            loadNotes()
        }
    }

    private fun enqueueAIProcessing(noteId: Long) {
        val inputData = workDataOf("note_id" to noteId)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<AINoteProcessor>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "process_note_$noteId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
