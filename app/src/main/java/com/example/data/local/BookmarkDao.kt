package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE lawId = :lawId ORDER BY paragraphIndex ASC")
    fun getBookmarksForLaw(lawId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE lawId = :lawId AND paragraphIndex = :paragraphIndex")
    suspend fun deleteBookmarkAtParagraph(lawId: Long, paragraphIndex: Int)

    @Query("SELECT COUNT(*) FROM bookmarks WHERE lawId = :lawId AND paragraphIndex = :paragraphIndex")
    suspend fun hasBookmarkAtParagraph(lawId: Long, paragraphIndex: Int): Int
}
