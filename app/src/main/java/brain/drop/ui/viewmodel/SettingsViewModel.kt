package brain.drop.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore by preferencesDataStore(name = "settings")

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val FOCUS_MODE = booleanPreferencesKey("focus_mode")
    private val AUTO_DELETE = booleanPreferencesKey("auto_delete")
    private val NOTIFICATIONS = booleanPreferencesKey("notifications")

    val focusMode: StateFlow<Boolean> = context.dataStore.data
        .map { it[FOCUS_MODE] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    val autoDeleteEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { it[AUTO_DELETE] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    val notificationsEnabled: StateFlow<Boolean> = context.dataStore.data
        .map { it[NOTIFICATIONS] ?: true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), true)

    fun setFocusMode(enabled: Boolean) {
        viewModelScope.launch { context.dataStore.edit { it[FOCUS_MODE] = enabled } }
    }
    fun setAutoDeleteEnabled(enabled: Boolean) {
        viewModelScope.launch { context.dataStore.edit { it[AUTO_DELETE] = enabled } }
    }
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { context.dataStore.edit { it[NOTIFICATIONS] = enabled } }
    }
}
