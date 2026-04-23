package brain.drop.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brain.drop.db.Note
import brain.drop.ui.theme.*
import brain.drop.ui.viewmodel.ChatViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val focusMode by viewModel.focusMode.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showAttachmentMenu by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendImage(it) }
    }

    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.sendFile(it) }
    }

    LaunchedEffect(notes.size) {
        if (notes.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "BrainDrop",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (isProcessing) {
                            Text(
                                "Organizing your thoughts...",
                                style = MaterialTheme.typography.labelSmall,
                                color = AccentCyan
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground.copy(alpha = 0.95f)
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleFocusMode() }) {
                        Icon(
                            if (focusMode) Icons.Filled.Visibility else Icons.Outlined.Visibility,
                            contentDescription = "Focus mode",
                            tint = if (focusMode) AccentCyan else TextSecondary
                        )
                    }
                    IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search", tint = TextSecondary)
                    }
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(Icons.Outlined.Timer, contentDescription = "Brain dump timer", tint = TextSecondary)
                    }
                }
            )
        },
        bottomBar = {
            ChatInputBar(
                value = inputText,
                onValueChange = viewModel::onInputChange,
                onSend = { viewModel.sendText() },
                onAttachmentClick = { showAttachmentMenu = !showAttachmentMenu },
                isRecording = isRecording,
                onRecordToggle = { 
                    isRecording = !isRecording
                    if (isRecording) viewModel.startRecording()
                    else viewModel.stopRecording()
                },
                focusMode = focusMode,
                modifier = Modifier.imePadding()
            )
        },
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                BrainDropSnackbar(data)
            }
        },
        containerColor = DeepBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notes.isEmpty()) {
                EmptyState(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteBubble(
                            note = note,
                            onClick = { navController.navigate(Screen.NoteDetail.createRoute(note.id)) },
                            onDelete = { 
                                viewModel.deleteNote(note)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Note moved to void",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete(note)
                                    }
                                }
                            },
                            focusMode = focusMode
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = showAttachmentMenu,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AttachmentMenu(
                    onImageClick = { 
                        imagePicker.launch("image/*")
                        showAttachmentMenu = false
                    },
                    onFileClick = {
                        filePicker.launch("*/*")
                        showAttachmentMenu = false
                    },
                    onLinkClick = {
                        viewModel.onInputChange(inputText + " https://")
                        showAttachmentMenu = false
                    }
                )
            }
        }
    }

    if (showTimerDialog) {
        BrainDumpTimerDialog(
            onDismiss = { showTimerDialog = false },
            onStart = { minutes ->
                viewModel.startBrainDumpTimer(minutes)
                showTimerDialog = false
            }
        )
    }
}

@Composable
fun NoteBubble(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    focusMode: Boolean
) {
    val categoryColor = when (note.category) {
        "idea" -> CategoryIdea
        "task" -> CategoryTask
        "research" -> CategoryResearch
        "code" -> CategoryCode
        "reference" -> CategoryReference
        "quote" -> CategoryQuote
        "link" -> CategoryLink
        else -> TextMuted
    }

    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .scale(scale)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .align(Alignment.CenterEnd)
                .clip(RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp))
                .background(SurfaceElevated)
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Text(
                    note.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
                if (note.isTemporary) {
                    Icon(
                        Icons.Outlined.Timer,
                        contentDescription = "Temporary",
                        modifier = Modifier.size(12.dp),
                        tint = AccentRose
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    formatTime(note.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            note.title?.let { title ->
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            note.summary?.let { summary ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (note.mediaUri != null && !focusMode) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = note.mediaUri,
                    contentDescription = "Attached image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            if (note.tags.isNotEmpty() && !focusMode) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    note.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = SurfaceOverlay,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    isRecording: Boolean,
    onRecordToggle: () -> Unit,
    focusMode: Boolean,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        color = DeepBackground.copy(alpha = 0.95f),
        tonalElevation = if (isFocused) 8.dp else 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onAttachmentClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    Icons.Outlined.AddCircle,
                    contentDescription = "Attachments",
                    tint = if (focusMode) TextMuted else AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize
                    ),
                    cursorBrush = SolidColor(AccentCyan),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                "Drop your thoughts here...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (value.isBlank()) {
                IconButton(
                    onClick = onRecordToggle,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) AccentRose else SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isRecording) Icons.Filled.Mic else Icons.Outlined.Mic,
                            contentDescription = "Voice note",
                            tint = if (isRecording) DeepBackground else if (focusMode) TextMuted else AccentCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Send",
                            tint = DeepBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            Icons.Outlined.Psychology,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SurfaceOverlay
        )
        Text(
            "Your mind is clear",
            style = MaterialTheme.typography.headlineMedium,
            color = TextSecondary
        )
        Text(
            "Just start typing. The AI will handle everything else.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
fun AttachmentMenu(
    onImageClick: () -> Unit,
    onFileClick: () -> Unit,
    onLinkClick: () -> Unit
) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentOption(
                icon = Icons.Outlined.Image,
                label = "Image",
                color = CategoryIdea,
                onClick = onImageClick
            )
            AttachmentOption(
                icon = Icons.Outlined.AttachFile,
                label = "File",
                color = CategoryResearch,
                onClick = onFileClick
            )
            AttachmentOption(
                icon = Icons.Outlined.Link,
                label = "Link",
                color = CategoryLink,
                onClick = onLinkClick
            )
        }
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
fun BrainDropSnackbar(data: SnackbarData) {
    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                data.visuals.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            data.visuals.actionLabel?.let { label ->
                TextButton(onClick = { data.performAction() }) {
                    Text(label, color = AccentCyan)
                }
            }
        }
    }
}

@Composable
fun BrainDumpTimerDialog(
    onDismiss: () -> Unit,
    onStart: (Int) -> Unit
) {
    var selectedMinutes by remember { mutableIntStateOf(5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceElevated,
        title = { Text("Brain Dump Session", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Set a gentle timer to focus your thoughts. You can dismiss it anytime.",
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(3, 5, 10, 15).forEach { min ->
                        FilterChip(
                            selected = selectedMinutes == min,
                            onClick = { selectedMinutes = min },
                            label = { Text("$min min") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan.copy(alpha = 0.2f),
                                selectedLabelColor = AccentCyan
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onStart(selectedMinutes) },
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("Start Session", color = DeepBackground)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60000 -> "now"
        diff < 3600000 -> "${diff / 60000}m"
        diff < 86400000 -> "${diff / 3600000}h"
        else -> "${diff / 86400000}d"
    }
}
