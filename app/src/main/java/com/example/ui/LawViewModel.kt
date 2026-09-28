package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.LawEntity
import com.example.data.local.NoteEntity
import com.example.data.model.LegalCategories
import com.example.data.network.CatalogLawItem
import com.example.data.repository.LawRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SearchScope {
    NAME_ONLY,
    FULL_TEXT
}

enum class ReaderTheme {
    LIGHT,
    DARK,
    SEPIA
}

enum class AppScreen {
    HOME,
    READER,
    CATALOG,
    NOTES_AND_BOOKMARKS
}

data class DownloadState(
    val isLoading: Boolean = false,
    val progressMessage: String = "",
    val error: String? = null,
    val successLawId: Long? = null
)

class LawViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: LawRepository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = LawRepository(database)
        viewModelScope.launch {
            repository.ensureInitialDataLoaded()
        }
    }

    // Navigation & Screen State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentLawId = MutableStateFlow<Long?>(null)
    val currentLawId: StateFlow<Long?> = _currentLawId.asStateFlow()

    // Filters and Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchScope = MutableStateFlow(SearchScope.NAME_ONLY)
    val searchScope: StateFlow<SearchScope> = _searchScope.asStateFlow()

    private val _selectedCategory = MutableStateFlow(LegalCategories.ALL)
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Download state
    private val _downloadState = MutableStateFlow(DownloadState())
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    // Reader preferences
    private val _fontSizeSp = MutableStateFlow(16)
    val fontSizeSp: StateFlow<Int> = _fontSizeSp.asStateFlow()

    private val _readerTheme = MutableStateFlow(ReaderTheme.LIGHT)
    val readerTheme: StateFlow<ReaderTheme> = _readerTheme.asStateFlow()

    private val _readerSearchQuery = MutableStateFlow("")
    val readerSearchQuery: StateFlow<String> = _readerSearchQuery.asStateFlow()

    private val _readerCurrentMatch = MutableStateFlow(0)
    val readerCurrentMatch: StateFlow<Int> = _readerCurrentMatch.asStateFlow()

    // All laws from database
    val allLaws: StateFlow<List<LawEntity>> = repository.allLaws
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered laws combining search, category, and all laws
    val displayedLaws: StateFlow<List<LawEntity>> = combine(
        allLaws,
        _searchQuery,
        _searchScope,
        _selectedCategory
    ) { laws, query, scope, category ->
        var list = laws

        // Filter by category
        if (category != LegalCategories.ALL) {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val q = query.trim()
            list = list.filter { law ->
                val matchesName = law.title.contains(q, ignoreCase = true) ||
                        law.shortTitle.contains(q, ignoreCase = true) ||
                        law.radaId.contains(q, ignoreCase = true)
                if (scope == SearchScope.NAME_ONLY) {
                    matchesName
                } else {
                    matchesName || law.fullText.contains(q, ignoreCase = true)
                }
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Law for Reader
    val currentLaw: StateFlow<LawEntity?> = _currentLawId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.getLawById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Bookmarks for Active Law
    val currentLawBookmarks: StateFlow<List<BookmarkEntity>> = _currentLawId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getBookmarksForLaw(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notes for Active Law
    val currentLawNotes: StateFlow<List<NoteEntity>> = _currentLawId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getNotesForLaw(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All bookmarks across all laws
    val allBookmarks: StateFlow<List<BookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All notes across all laws
    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchScope(scope: SearchScope) {
        _searchScope.value = scope
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openLaw(lawId: Long) {
        _currentLawId.value = lawId
        _readerSearchQuery.value = ""
        _readerCurrentMatch.value = 0
        _currentScreen.value = AppScreen.READER
    }

    fun closeReader() {
        _currentLawId.value = null
        _readerSearchQuery.value = ""
        _readerCurrentMatch.value = 0
        _currentScreen.value = AppScreen.HOME
    }

    fun toggleFavorite(law: LawEntity) {
        viewModelScope.launch {
            repository.setFavorite(law.id, !law.isFavorite)
        }
    }

    fun deleteLaw(lawId: Long) {
        viewModelScope.launch {
            repository.deleteLawById(lawId)
            if (_currentLawId.value == lawId) {
                closeReader()
            }
        }
    }

    fun updateLawCategory(lawId: Long, category: String) {
        viewModelScope.launch {
            repository.updateCategory(lawId, category)
        }
    }

    fun downloadLaw(queryOrUrl: String, category: String = "") {
        viewModelScope.launch {
            _downloadState.value = DownloadState(
                isLoading = true,
                progressMessage = "Підключення до zakon.rada.gov.ua..."
            )
            val result = repository.downloadAndSaveLaw(queryOrUrl, category)
            if (result.isSuccess) {
                val savedLaw = result.getOrThrow()
                _downloadState.value = DownloadState(
                    isLoading = false,
                    successLawId = savedLaw.id
                )
            } else {
                _downloadState.value = DownloadState(
                    isLoading = false,
                    error = result.exceptionOrNull()?.localizedMessage ?: "Невідома помилка"
                )
            }
        }
    }

    fun clearDownloadState() {
        _downloadState.value = DownloadState()
    }

    fun downloadFromCatalog(item: CatalogLawItem) {
        downloadLaw(item.radaId, item.category)
    }

    // Reader functions
    fun increaseFontSize() {
        if (_fontSizeSp.value < 28) _fontSizeSp.value += 2
    }

    fun decreaseFontSize() {
        if (_fontSizeSp.value > 12) _fontSizeSp.value -= 2
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerTheme.value = theme
    }

    fun setReaderSearchQuery(query: String) {
        _readerSearchQuery.value = query
        _readerCurrentMatch.value = 0
    }

    fun nextMatch(totalMatches: Int) {
        if (totalMatches > 0) {
            _readerCurrentMatch.value = (_readerCurrentMatch.value + 1) % totalMatches
        }
    }

    fun prevMatch(totalMatches: Int) {
        if (totalMatches > 0) {
            _readerCurrentMatch.value = if (_readerCurrentMatch.value - 1 < 0) totalMatches - 1 else _readerCurrentMatch.value - 1
        }
    }

    fun addBookmark(lawId: Long, title: String, paragraphIndex: Int, snippet: String) {
        viewModelScope.launch {
            repository.addBookmark(lawId, title, paragraphIndex, snippet)
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmarkById(bookmarkId)
        }
    }

    fun deleteBookmarkAtParagraph(lawId: Long, paragraphIndex: Int) {
        viewModelScope.launch {
            repository.deleteBookmarkAtParagraph(lawId, paragraphIndex)
        }
    }

    fun addNote(lawId: Long, paragraphIndex: Int, selectedText: String, noteText: String) {
        viewModelScope.launch {
            repository.addNote(lawId, paragraphIndex, selectedText, noteText)
        }
    }

    fun updateNote(note: NoteEntity, newText: String) {
        viewModelScope.launch {
            repository.updateNote(note.copy(noteText = newText))
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNoteById(noteId)
        }
    }

    fun saveLastReadPosition(lawId: Long, position: Int) {
        viewModelScope.launch {
            repository.updateLastReadPosition(lawId, position)
        }
    }
}
