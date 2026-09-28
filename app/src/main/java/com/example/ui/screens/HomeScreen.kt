package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.LawEntity
import com.example.data.model.LegalCategories
import com.example.ui.AppScreen
import com.example.ui.LawViewModel
import com.example.ui.SearchScope
import com.example.ui.components.AddLawDialog
import com.example.ui.components.ChangeCategoryDialog
import com.example.ui.components.LegalCategoryBadge
import com.example.ui.components.SearchBarComponent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LawViewModel,
    modifier: Modifier = Modifier
) {
    val displayedLaws by viewModel.displayedLaws.collectAsStateWithLifecycle()
    val allLaws by viewModel.allLaws.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchScope by viewModel.searchScope.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()

    var isAddDialogOpen by remember { mutableStateOf(false) }
    var lawToChangeCategory by remember { mutableStateOf<LawEntity?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(downloadState.error) {
        downloadState.error?.let { err ->
            snackbarHostState.showSnackbar("Помилка завантаження: $err")
            viewModel.clearDownloadState()
        }
    }

    LaunchedEffect(downloadState.successLawId) {
        downloadState.successLawId?.let {
            snackbarHostState.showSnackbar("Закон успішно збережено в пам'ять телефону!")
            viewModel.clearDownloadState()
            isAddDialogOpen = false
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Закони України",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "База ВРУ • Офлайн доступ (${allLaws.size} в пам'яті)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.CATALOG) },
                        modifier = Modifier.testTag("open_catalog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Каталог законів ВРУ",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.NOTES_AND_BOOKMARKS) },
                        modifier = Modifier.testTag("open_bookmarks_notes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Закладки та нотатки",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_law_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Завантажити новий закон"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar Component (Name & Full-text search)
            SearchBarComponent(
                query = searchQuery,
                onQueryChange = { viewModel.setSearchQuery(it) },
                searchScope = searchScope,
                onScopeChange = { viewModel.setSearchScope(it) }
            )

            // Categorization System - Category Filter Chips
            CategoryFilterBar(
                selectedCategory = selectedCategory,
                allLaws = allLaws,
                onSelectCategory = { viewModel.setSelectedCategory(it) }
            )

            // Main Laws List
            if (displayedLaws.isEmpty()) {
                EmptyLawsState(
                    hasQuery = searchQuery.isNotBlank(),
                    query = searchQuery,
                    selectedCategory = selectedCategory,
                    onClearFilter = {
                        viewModel.setSearchQuery("")
                        viewModel.setSelectedCategory(LegalCategories.ALL)
                    },
                    onOpenDownload = { isAddDialogOpen = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedLaws, key = { it.id }) { law ->
                        LawCard(
                            law = law,
                            searchQuery = if (searchScope == SearchScope.FULL_TEXT) searchQuery else "",
                            onOpen = { viewModel.openLaw(law.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(law) },
                            onChangeCategory = { lawToChangeCategory = law },
                            onDelete = {
                                viewModel.deleteLaw(law.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Документ видалено з пам'яті")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Add Law Dialog
    AddLawDialog(
        isOpen = isAddDialogOpen,
        onDismiss = { isAddDialogOpen = false },
        onDownload = { input, category ->
            viewModel.downloadLaw(input, category)
        },
        isLoading = downloadState.isLoading
    )

    // Change Category Dialog
    lawToChangeCategory?.let { law ->
        ChangeCategoryDialog(
            law = law,
            onDismiss = { lawToChangeCategory = null },
            onCategorySelected = { newCat ->
                viewModel.updateLawCategory(law.id, newCat)
                lawToChangeCategory = null
                scope.launch {
                    snackbarHostState.showSnackbar("Галузь права оновлено на: $newCat")
                }
            }
        )
    }
}

@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    allLaws: List<LawEntity>,
    onSelectCategory: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "Всі галузі" chip
        val allSelected = selectedCategory == LegalCategories.ALL
        FilterChip(
            selected = allSelected,
            onClick = { onSelectCategory(LegalCategories.ALL) },
            label = {
                Text(
                    text = "Всі (${allLaws.size})",
                    fontSize = 12.sp,
                    fontWeight = if (allSelected) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("filter_all_categories_chip")
        )

        // Categorized chips
        LegalCategories.CATEGORIES.forEach { cat ->
            val count = allLaws.count { it.category.equals(cat.name, ignoreCase = true) }
            val isSelected = selectedCategory.equals(cat.name, ignoreCase = true)

            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(cat.name) },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${cat.shortName} ($count)",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = cat.primaryColor,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.testTag("filter_category_${cat.shortName}")
            )
        }
    }
}

@Composable
fun LawCard(
    law: LawEntity,
    searchQuery: String,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeCategory: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .testTag("law_card_${law.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Category badge + Offline icon + Favorite + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    LegalCategoryBadge(
                        categoryName = law.category,
                        useShortName = true,
                        onClick = onChangeCategory
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflinePin,
                                contentDescription = "Збережено офлайн",
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Офлайн",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (law.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (law.isFavorite) "У вибраному" else "Додати у вибране",
                            tint = if (law.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Меню дій"
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Змінити галузь права") },
                                onClick = {
                                    menuExpanded = false
                                    onChangeCategory()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Видалити з пам'яті") },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Law Title
            Text(
                text = law.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Short Title & Rada ID & Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (law.shortTitle.isNotBlank()) {
                    Text(
                        text = law.shortTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = " • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text = "№ ${law.radaId}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (law.dateAdopted.isNotBlank()) {
                    Text(text = " • ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "від ${law.dateAdopted}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary / Excerpt
            if (law.summary.isNotBlank()) {
                Text(
                    text = law.summary,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Search match snippet if searching inside text
            if (searchQuery.isNotBlank() && law.fullText.contains(searchQuery, ignoreCase = true)) {
                val snippet = extractMatchSnippet(law.fullText, searchQuery)
                if (snippet.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Знайдено в тексті:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "«...$snippet...»",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun extractMatchSnippet(fullText: String, query: String): String {
    val index = fullText.indexOf(query, ignoreCase = true)
    if (index == -1) return ""
    val start = (index - 40).coerceAtLeast(0)
    val end = (index + query.length + 60).coerceAtMost(fullText.length)
    return fullText.substring(start, end).replace("\n", " ").trim()
}

@Composable
fun EmptyLawsState(
    hasQuery: Boolean,
    query: String,
    selectedCategory: String,
    onClearFilter: () -> Unit,
    onOpenDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LibraryBooks,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (hasQuery) "Нічого не знайдено за запитом «$query»" else "Немає збережених законів у даній категорії",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasQuery) "Спробуйте змінити пошуковий запит або перемкнути режим пошуку (в назвах / у всьому тексті)" else "Завантажте закон з сайту zakon.rada.gov.ua або виберіть іншу категорію",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasQuery || selectedCategory != LegalCategories.ALL) {
                FilterChip(
                    selected = true,
                    onClick = onClearFilter,
                    label = { Text("Скинути фільтри") }
                )
            }
            FilterChip(
                selected = false,
                onClick = onOpenDownload,
                label = { Text("Завантажити новий") }
            )
        }
    }
}
