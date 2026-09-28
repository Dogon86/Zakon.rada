package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LawDao {
    @Query("SELECT * FROM laws ORDER BY isFavorite DESC, title ASC")
    fun getAllLaws(): Flow<List<LawEntity>>

    @Query("SELECT * FROM laws WHERE category = :category ORDER BY isFavorite DESC, title ASC")
    fun getLawsByCategory(category: String): Flow<List<LawEntity>>

    @Query("SELECT DISTINCT category FROM laws WHERE category != '' ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM laws WHERE title LIKE '%' || :query || '%' OR shortTitle LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchLawsByName(query: String): Flow<List<LawEntity>>

    @Query("SELECT * FROM laws WHERE title LIKE '%' || :query || '%' OR shortTitle LIKE '%' || :query || '%' OR fullText LIKE '%' || :query || '%' ORDER BY title ASC")
    fun searchLawsByNameOrContent(query: String): Flow<List<LawEntity>>

    @Query("SELECT * FROM laws WHERE id = :id LIMIT 1")
    fun getLawById(id: Long): Flow<LawEntity?>

    @Query("SELECT * FROM laws WHERE id = :id LIMIT 1")
    suspend fun getLawByIdDirect(id: Long): LawEntity?

    @Query("SELECT * FROM laws WHERE radaId = :radaId LIMIT 1")
    suspend fun getLawByRadaId(radaId: String): LawEntity?

    @Query("SELECT COUNT(*) FROM laws")
    suspend fun getLawCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaw(law: LawEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(laws: List<LawEntity>)

    @Update
    suspend fun updateLaw(law: LawEntity)

    @Delete
    suspend fun deleteLaw(law: LawEntity)

    @Query("DELETE FROM laws WHERE id = :id")
    suspend fun deleteLawById(id: Long)

    @Query("UPDATE laws SET isFavorite = :isFav WHERE id = :id")
    suspend fun setFavorite(id: Long, isFav: Boolean)

    @Query("UPDATE laws SET category = :category WHERE id = :id")
    suspend fun updateCategory(id: Long, category: String)

    @Query("UPDATE laws SET lastReadPosition = :position WHERE id = :id")
    suspend fun updateLastReadPosition(id: Long, position: Int)
}
