package brain.drop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brain.drop.ui.theme.*
import brain.drop.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val focusMode by viewModel.focusMode.collectAsState()
    val autoDeleteEnabled by viewModel.autoDeleteEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBackground)
            )
        },
        containerColor = DeepBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("APPEARANCE", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                SettingSwitch(
                    icon = Icons.Outlined.Visibility,
                    title = "Focus Mode",
                    subtitle = "Disable animations, increase sizes, reduce colors",
                    checked = focusMode,
                    onCheckedChange = viewModel::setFocusMode
                )
            }
            item {
                SettingSwitch(
                    icon = Icons.Outlined.Notifications,
                    title = "Gentle Nudges",
                    subtitle = "Soft reminders for temporary notes",
                    checked = notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
            }
            item {
                Text("ORGANIZATION", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                SettingSwitch(
                    icon = Icons.Outlined.AutoDelete,
                    title = "Auto-Delete Temporary Notes",
                    subtitle = "Clean up notes marked as temporary",
                    checked = autoDeleteEnabled,
                    onCheckedChange = viewModel::setAutoDeleteEnabled
                )
            }
            item {
                Text("ABOUT", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.padding(vertical = 8.dp))
            }
            item {
                SettingItem(
                    icon = Icons.Outlined.Info,
                    title = "BrainDrop",
                    subtitle = "Version 1.0.0 · AI-Powered Brain Dump",
                    onClick = { }
                )
            }
            item {
                SettingItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = "Privacy",
                    subtitle = "All data stays on your device",
                    onClick = { }
                )
            }
        }
    }
}

@Composable
fun SettingSwitch(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentCyan,
                    checkedTrackColor = AccentCyan.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}
