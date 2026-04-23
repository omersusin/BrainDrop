package brain.drop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brain.drop.db.Note
import brain.drop.db.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _note = MutableStateFlow<Note?>(null)
    val note: StateFlow<Note?> = _note.asStateFlow()

    fun loadNote(id: Long) {
        viewModelScope.launch { _note.value = repository.getNoteById(id) }
    }

    fun toggleComplete(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(isCompleted = !note.isCompleted)
            repository.updateNote(updated)
            _note.value = updated
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }
}
