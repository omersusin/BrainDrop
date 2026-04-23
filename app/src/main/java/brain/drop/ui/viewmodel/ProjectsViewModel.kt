package brain.drop.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import brain.drop.db.NoteRepository
import brain.drop.ui.screens.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _projects = MutableStateFlow<List<Project>>(emptyList())
    val projects: StateFlow<List<Project>> = _projects.asStateFlow()

    init { loadProjects() }

    private fun loadProjects() {
        viewModelScope.launch {
            val notes = repository.getAllNotes()
            val projectMap = notes
                .filter { it.projectId != null && it.projectName != null }
                .groupBy { it.projectId!! }
                .map { (id, notes) ->
                    Project(
                        id = id,
                        name = notes.first().projectName ?: "Untitled",
                        noteCount = notes.size,
                        lastActive = notes.maxOf { it.timestamp }
                    )
                }
                .sortedByDescending { it.lastActive }
            _projects.value = projectMap
        }
    }
}
