package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.BookmarkEntity
import com.example.data.local.NoteEntity
import com.example.ui.LawViewModel
import com.example.ui.ReaderTheme
import com.example.ui.components.LegalCategoryBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: LawViewModel,
    modifier: Modifier = Modifier
) {
    // Handle back button to return to home
    BackHandler {
        viewModel.closeReader()
    }

    val law by viewModel.currentLaw.collectAsStateWithLifecycle()
    val bookmarks by viewModel.currentLawBookmarks.collectAsStateWithLifecycle()
    val notes by viewModel.currentLawNotes.collectAsStateWithLifecycle()
    val fontSizeSp by viewModel.fontSizeSp.collectAsStateWithLifecycle()
    val readerTheme by viewModel.readerTheme.collectAsStateWithLifecycle()
    val searchInTextQuery by viewModel.readerSearchQuery.collectAsStateWithLifecycle()
    val currentMatchIndex by viewModel.readerCurrentMatch.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isSearchVisible by remember { mutableStateOf(false) }
    var isBookmarksSheetVisible by remember { mutableStateOf(false) }
    var noteDialogParagraphIndex by remember { mutableStateOf<Int?>(null) }
    var noteDialogSelectedText by remember { mutableStateOf("") }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }

    // Theme color setup
    val (backgroundColor, textColor, cardColor, accentColor) = when (readerTheme) {
        ReaderTheme.LIGHT -> Quad(Color(0xFFFCFDFD), Color(0xFF1E293B), Color(0xFFF1F5F9), Color(0xFF1E3A8A))
        ReaderTheme.DARK -> Quad(Color(0xFF0F172A), Color(0xFFE2E8F0), Color(0xFF1E293B), Color(0xFF60A5FA))
        ReaderTheme.SEPIA -> Quad(Color(0xFFFBF0D9), Color(0xFF43302B), Color(0xFFEFE2CA), Color(0xFF854D0E))
    }

    // Split law text into paragraphs
    val paragraphs = remember(law?.fullText) {
        law?.fullText?.split("\n\n")?.filter { it.isNotBlank() } ?: emptyList()
    }

    // Paragraph indices that match the search query
    val matchingParagraphIndices = remember(paragraphs, searchInTextQuery) {
        if (searchInTextQuery.isBlank()) emptyList()
        else {
            paragraphs.indices.filter { idx ->
                paragraphs[idx].contains(searchInTextQuery, ignoreCase = true)
            }
        }
    }

    // Jump to match when currentMatchIndex changes
    LaunchedEffect(currentMatchIndex, matchingParagraphIndices) {
        if (matchingParagraphIndices.isNotEmpty() && currentMatchIndex in matchingParagraphIndices.indices) {
            val targetParagraph = matchingParagraphIndices[currentMatchIndex]
            listState.animateScrollToItem(targetParagraph)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = law?.title ?: "Читання закону",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            law?.let {
                                LegalCategoryBadge(categoryName = it.category, useShortName = true)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "№ ${it.radaId}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeReader() },
                        modifier = Modifier.testTag("reader_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад до списку"
                        )
                    }
                },
                actions = {
                    // Search in text toggle
                    IconButton(
                        onClick = { isSearchVisible = !isSearchVisible },
                        modifier = Modifier.testTag("reader_search_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Пошук у тексті закону"
                        )
                    }

                    // Bookmarks & Notes sheet toggle
                    IconButton(
                        onClick = { isBookmarksSheetVisible = true },
                        modifier = Modifier.testTag("reader_bookmarks_notes_toggle")
                    ) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "Закладки та коментарі"
                            )
                            val totalItems = bookmarks.size + notes.size
                            if (totalItems > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.TopEnd)
                                ) {
                                    Text(
                                        text = "$totalItems",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                    }

                    // Reader Display Options (Font Size & Theme)
                    ReaderOptionsDropdown(
                        currentTheme = readerTheme,
                        onThemeSelect = { viewModel.setReaderTheme(it) },
                        onIncreaseFont = { viewModel.increaseFontSize() },
                        onDecreaseFont = { viewModel.decreaseFontSize() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(innerPadding)
        ) {
            // Search in law text toolbar
            AnimatedVisibility(visible = isSearchVisible) {
                Surface(
                    color = cardColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchInTextQuery,
                            onValueChange = { viewModel.setReaderSearchQuery(it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reader_search_input"),
                            placeholder = { Text("Пошук по статті чи слову...", fontSize = 13.sp) },
                            singleLine = true,
                            trailingIcon = {
                                if (searchInTextQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setReaderSearchQuery("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Очистити")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Match counter and Next/Prev buttons
                        if (matchingParagraphIndices.isNotEmpty()) {
                            Text(
                                text = "${currentMatchIndex + 1}/${matchingParagraphIndices.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                            IconButton(
                                onClick = { viewModel.prevMatch(matchingParagraphIndices.size) },
                                modifier = Modifier.testTag("reader_prev_match_button")
                            ) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Попередній збіг")
                            }
                            IconButton(
                                onClick = { viewModel.nextMatch(matchingParagraphIndices.size) },
                                modifier = Modifier.testTag("reader_next_match_button")
                            ) {
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Наступний збіг")
                            }
                        } else if (searchInTextQuery.isNotBlank()) {
                            Text(text = "0 збігів", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Paragraphs list
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(paragraphs) { index, paragraphText ->
                    val isBookmarked = bookmarks.any { it.paragraphIndex == index }
                    val paragraphNotes = notes.filter { it.paragraphIndex == index }
                    val isArticleHeader = paragraphText.startsWith("Стаття", ignoreCase = true) ||
                            paragraphText.startsWith("РОЗДІЛ", ignoreCase = true) ||
                            paragraphText.startsWith("Глава", ignoreCase = true) ||
                            paragraphText.startsWith("КНИГА", ignoreCase = true)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isBookmarked) cardColor.copy(alpha = 0.5f) else Color.Transparent)
                            .padding(4.dp)
                    ) {
                        // Action buttons row for each paragraph (Bookmark & Note)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isArticleHeader) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = accentColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Стаття / Розділ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "§ ${index + 1}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }

                            Row {
                                // Add/Toggle Bookmark button
                                IconButton(
                                    onClick = {
                                        val lawId = law?.id ?: return@IconButton
                                        if (isBookmarked) {
                                            viewModel.deleteBookmarkAtParagraph(lawId, index)
                                        } else {
                                            val title = if (isArticleHeader) paragraphText.take(60) else "Абзац ${index + 1}"
                                            viewModel.addBookmark(lawId, title, index, paragraphText.take(150))
                                        }
                                    },
                                    modifier = Modifier.size(32.dp).testTag("bookmark_button_$index")
                                ) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = if (isBookmarked) "Видалити закладку" else "Додати закладку",
                                        tint = if (isBookmarked) Color(0xFFD97706) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Add Note button
                                IconButton(
                                    onClick = {
                                        noteDialogParagraphIndex = index
                                        noteDialogSelectedText = paragraphText.take(200)
                                        noteToEdit = null
                                    },
                                    modifier = Modifier.size(32.dp).testTag("add_note_button_$index")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddComment,
                                        contentDescription = "Додати нотатку",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Paragraph content with highlighted search matches
                        val annotatedText = remember(paragraphText, searchInTextQuery) {
                            buildAnnotatedString {
                                if (searchInTextQuery.isBlank()) {
                                    append(paragraphText)
                                } else {
                                    var startIndex = 0
                                    val queryLower = searchInTextQuery.lowercase()
                                    val textLower = paragraphText.lowercase()

                                    while (startIndex < paragraphText.length) {
                                        val matchIndex = textLower.indexOf(queryLower, startIndex)
                                        if (matchIndex == -1) {
                                            append(paragraphText.substring(startIndex))
                                            break
                                        }
                                        append(paragraphText.substring(startIndex, matchIndex))
                                        withStyle(
                                            style = SpanStyle(
                                                background = Color(0xFFFEF08A), // Yellow highlight
                                                color = Color(0xFF713F12),
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append(paragraphText.substring(matchIndex, matchIndex + searchInTextQuery.length))
                                        }
                                        startIndex = matchIndex + searchInTextQuery.length
                                    }
                                }
                            }
                        }

                        Text(
                            text = annotatedText,
                            fontSize = fontSizeSp.sp,
                            lineHeight = (fontSizeSp * 1.5).sp,
                            fontWeight = if (isArticleHeader) FontWeight.Bold else FontWeight.Normal,
                            fontFamily = FontFamily.Serif,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        // Display notes attached to this paragraph
                        if (paragraphNotes.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                paragraphNotes.forEach { note ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                noteToEdit = note
                                                noteDialogParagraphIndex = note.paragraphIndex
                                                noteDialogSelectedText = note.selectedText
                                            },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = cardColor
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddComment,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Нотатка до статті:",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = accentColor
                                                )
                                                Text(
                                                    text = note.noteText,
                                                    fontSize = 12.sp,
                                                    color = textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Bookmarks and Notes for current law
    if (isBookmarksSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { isBookmarksSheetVisible = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Закладки та нотатки закону",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (bookmarks.isEmpty() && notes.isEmpty()) {
                    Text(
                        text = "У цьому законі ще немає збережених закладок чи нотаток. Натисніть на значок закладки або коментаря біля будь-якого абзацу.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (bookmarks.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Закладки (${bookmarks.size}):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(bookmarks) { bookmark ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isBookmarksSheetVisible = false
                                            scope.launch {
                                                listState.animateScrollToItem(bookmark.paragraphIndex)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = bookmark.title,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = bookmark.snippet,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteBookmark(bookmark.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Видалити закладку",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (notes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Нотатки (${notes.size}):",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                            items(notes) { note ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            isBookmarksSheetVisible = false
                                            scope.launch {
                                                listState.animateScrollToItem(note.paragraphIndex)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddComment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .size(20.dp)
                                                .padding(top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = note.noteText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (note.selectedText.isNotBlank()) {
                                                Text(
                                                    text = "«${note.selectedText}»",
                                                    fontSize = 11.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteNote(note.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Видалити нотатку",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add or Edit Note Dialog
    if (noteDialogParagraphIndex != null) {
        val paragraphIdx = noteDialogParagraphIndex!!
        var noteInput by remember { mutableStateOf(noteToEdit?.noteText ?: "") }

        AlertDialog(
            onDismissRequest = {
                noteDialogParagraphIndex = null
                noteToEdit = null
            },
            title = {
                Text(
                    text = if (noteToEdit != null) "Редагувати нотатку" else "Додати нотатку до абзацу ${paragraphIdx + 1}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (noteDialogSelectedText.isNotBlank()) {
                        Text(
                            text = "«$noteDialogSelectedText...»",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Ваша примітка або аналіз") },
                        placeholder = { Text("Введіть коментар чи зауваження...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_field"),
                        minLines = 3,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lawId = law?.id ?: return@Button
                        if (noteInput.isNotBlank()) {
                            if (noteToEdit != null) {
                                viewModel.updateNote(noteToEdit!!, noteInput.trim())
                            } else {
                                viewModel.addNote(lawId, paragraphIdx, noteDialogSelectedText, noteInput.trim())
                            }
                        }
                        noteDialogParagraphIndex = null
                        noteToEdit = null
                    },
                    enabled = noteInput.isNotBlank(),
                    modifier = Modifier.testTag("confirm_note_button")
                ) {
                    Text("Зберегти")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        noteDialogParagraphIndex = null
                        noteToEdit = null
                    }
                ) {
                    Text("Скасувати")
                }
            }
        )
    }
}

@Composable
fun ReaderOptionsDropdown(
    currentTheme: ReaderTheme,
    onThemeSelect: (ReaderTheme) -> Unit,
    onIncreaseFont: () -> Unit,
    onDecreaseFont: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag("reader_options_menu_button")
        ) {
            Icon(
                imageVector = Icons.Default.FormatSize,
                contentDescription = "Налаштування читання"
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Збільшити шрифт (A+)") },
                onClick = { onIncreaseFont() }
            )
            DropdownMenuItem(
                text = { Text("Зменшити шрифт (A-)") },
                onClick = { onDecreaseFont() }
            )
            DropdownMenuItem(
                text = { Text("Тема: Світла") },
                onClick = {
                    onThemeSelect(ReaderTheme.LIGHT)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Тема: Сепія (для читання)") },
                onClick = {
                    onThemeSelect(ReaderTheme.SEPIA)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Тема: Темна") },
                onClick = {
                    onThemeSelect(ReaderTheme.DARK)
                    expanded = false
                }
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
