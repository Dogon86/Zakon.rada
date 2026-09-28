package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    foreignKeys = [
        ForeignKey(
            entity = LawEntity::class,
            parentColumns = ["id"],
            childColumns = ["lawId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lawId"])]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lawId: Long,
    val title: String,               // e.g. "Стаття 10. Державна мова"
    val paragraphIndex: Int,         // Line or paragraph index for jumping
    val snippet: String,             // First ~150 chars of paragraph
    val createdAt: Long = System.currentTimeMillis()
)
