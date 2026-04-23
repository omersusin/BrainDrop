package brain.drop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brain.drop.db.Note
import brain.drop.ui.theme.*
import brain.drop.ui.viewmodel.NoteDetailViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long?,
    navController: NavController,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val note by viewModel.note.collectAsState()

    LaunchedEffect(noteId) {
        noteId?.let { viewModel.loadNote(it) }
    }

    if (note == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AccentCyan)
        }
        return
    }

    val currentNote = note!!
    val categoryColor = when (currentNote.category) {
        "idea" -> CategoryIdea
        "task" -> CategoryTask
        "research" -> CategoryResearch
        "code" -> CategoryCode
        "reference" -> CategoryReference
        "quote" -> CategoryQuote
        "link" -> CategoryLink
        else -> TextMuted
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Note", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleComplete(currentNote) }) {
                        Icon(
                            if (currentNote.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = "Complete",
                            tint = if (currentNote.isCompleted) CategoryTask else TextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.deleteNote(currentNote) }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = AccentRose)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBackground)
            )
        },
        containerColor = DeepBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Text(
                    currentNote.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = categoryColor
                )
                if (currentNote.isTemporary) {
                    Surface(
                        color = AccentRose.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Temporary",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentRose,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            currentNote.title?.let { title ->
                Text(
                    title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }

            Text(
                currentNote.content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )

            currentNote.summary?.let { summary ->
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI Summary", style = MaterialTheme.typography.labelLarge, color = AccentCyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(summary, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }

            currentNote.extractedText?.let { extracted ->
                Surface(
                    color = SurfaceElevated,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Extracted Text", style = MaterialTheme.typography.labelLarge, color = AccentLavender)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(extracted, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    }
                }
            }

            currentNote.mediaUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Attached media",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            if (currentNote.tags.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tags", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentNote.tags.forEach { tag ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text("#$tag") },
                                colors = InputChipDefaults.inputChipColors(
                                    containerColor = SurfaceOverlay,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            if (currentNote.linkedNoteIds != null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Connected Notes", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    // Linked notes would be fetched and displayed here
                }
            }

            Text(
                "Created ${java.text.SimpleDateFormat("MMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault()).format(java.util.Date(currentNote.timestamp))}",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}
