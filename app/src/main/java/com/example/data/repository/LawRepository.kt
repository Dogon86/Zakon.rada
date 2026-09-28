package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.DatabasePrepopulate
import com.example.data.local.LawEntity
import com.example.data.local.NoteEntity
import com.example.data.network.RadaWebScraper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LawRepository(
    private val database: AppDatabase,
    private val scraper: RadaWebScraper = RadaWebScraper()
) {
    private val lawDao = database.lawDao()
    private val bookmarkDao = database.bookmarkDao()
    private val noteDao = database.noteDao()

    val allLaws: Flow<List<LawEntity>> = lawDao.getAllLaws()
    val allBookmarks: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allCategories: Flow<List<String>> = lawDao.getAllCategories()

    fun getLawsByCategory(category: String): Flow<List<LawEntity>> = lawDao.getLawsByCategory(category)

    fun searchLawsByName(query: String): Flow<List<LawEntity>> = lawDao.searchLawsByName(query)

    fun searchLawsByNameOrContent(query: String): Flow<List<LawEntity>> = lawDao.searchLawsByNameOrContent(query)

    fun getLawById(id: Long): Flow<LawEntity?> = lawDao.getLawById(id)

    suspend fun getLawByIdDirect(id: Long): LawEntity? = lawDao.getLawByIdDirect(id)

    fun getBookmarksForLaw(lawId: Long): Flow<List<BookmarkEntity>> = bookmarkDao.getBookmarksForLaw(lawId)

    fun getNotesForLaw(lawId: Long): Flow<List<NoteEntity>> = noteDao.getNotesForLaw(lawId)

    suspend fun downloadAndSaveLaw(queryOrUrl: String, category: String = ""): Result<LawEntity> = withContext(Dispatchers.IO) {
        val fetchResult = scraper.fetchLaw(queryOrUrl, category)
        if (fetchResult.isSuccess) {
            val fetchedLaw = fetchResult.getOrThrow()
            // Check if already exists in DB
            val existing = lawDao.getLawByRadaId(fetchedLaw.radaId)
            val lawToSave = if (existing != null) {
                fetchedLaw.copy(
                    id = existing.id,
                    isFavorite = existing.isFavorite,
                    category = if (category.isNotBlank()) category else existing.category,
                    lastReadPosition = existing.lastReadPosition
                )
            } else {
                fetchedLaw
            }
            val id = lawDao.insertLaw(lawToSave)
            val finalLaw = lawToSave.copy(id = if (existing != null) existing.id else id)
            Result.success(finalLaw)
        } else {
            fetchResult
        }
    }

    suspend fun insertLaw(law: LawEntity): Long = withContext(Dispatchers.IO) {
        lawDao.insertLaw(law)
    }

    suspend fun deleteLawById(id: Long) = withContext(Dispatchers.IO) {
        lawDao.deleteLawById(id)
    }

    suspend fun setFavorite(id: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        lawDao.setFavorite(id, isFav)
    }

    suspend fun updateCategory(id: Long, category: String) = withContext(Dispatchers.IO) {
        lawDao.updateCategory(id, category)
    }

    suspend fun updateLastReadPosition(id: Long, position: Int) = withContext(Dispatchers.IO) {
        lawDao.updateLastReadPosition(id, position)
    }

    suspend fun addBookmark(lawId: Long, title: String, paragraphIndex: Int, snippet: String): Long = withContext(Dispatchers.IO) {
        bookmarkDao.insertBookmark(
            BookmarkEntity(
                lawId = lawId,
                title = title,
                paragraphIndex = paragraphIndex,
                snippet = snippet
            )
        )
    }

    suspend fun deleteBookmarkById(id: Long) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkById(id)
    }

    suspend fun deleteBookmarkAtParagraph(lawId: Long, paragraphIndex: Int) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmarkAtParagraph(lawId, paragraphIndex)
    }

    suspend fun addNote(lawId: Long, paragraphIndex: Int, selectedText: String, noteText: String): Long = withContext(Dispatchers.IO) {
        noteDao.insertNote(
            NoteEntity(
                lawId = lawId,
                paragraphIndex = paragraphIndex,
                selectedText = selectedText,
                noteText = noteText
            )
        )
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNoteById(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNoteById(id)
    }

    suspend fun ensureInitialDataLoaded() = withContext(Dispatchers.IO) {
        if (lawDao.getLawCount() == 0) {
            lawDao.insertAll(DatabasePrepopulate.getInitialLaws())
        }
    }
}
