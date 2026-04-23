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
class OrganizedViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _categorizedNotes = MutableStateFlow<List<Note>>(emptyList())
    val categorizedNotes: StateFlow<List<Note>> = _categorizedNotes.asStateFlow()

    init { loadNotes() }

    private fun loadNotes() {
        viewModelScope.launch {
            val cat = _selectedCategory.value
            _categorizedNotes.value = if (cat == "all") repository.getRecentNotes(200)
            else repository.getNotesByCategory(cat)
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        loadNotes()
    }
}
