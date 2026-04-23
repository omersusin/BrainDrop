package brain.drop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import brain.drop.db.Note
import brain.drop.ui.theme.*
import brain.drop.ui.viewmodel.OrganizedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizedScreen(
    navController: NavController,
    viewModel: OrganizedViewModel = hiltViewModel()
) {
    val notes by viewModel.categorizedNotes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("all", "idea", "task", "research", "code", "reference", "quote", "link")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organized", style = MaterialTheme.typography.titleLarge) },
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
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory),
                containerColor = DeepBackground,
                contentColor = AccentCyan,
                edgePadding = 16.dp
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        text = {
                            Text(
                                category.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    OrganizedNoteCard(
                        note = note,
                        onClick = { navController.navigate(Screen.NoteDetail.createRoute(note.id)) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrganizedNoteCard(note: Note, onClick: () -> Unit) {
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

    Surface(
        color = SurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    note.title ?: note.content.take(60),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (note.summary != null) {
                    Text(
                        note.summary!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    note.tags.take(3).forEach { tag ->
                        AssistChip(
                            onClick = { },
                            label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
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
