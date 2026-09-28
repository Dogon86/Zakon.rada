package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
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
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val lawId: Long,
    val paragraphIndex: Int,
    val selectedText: String,        // Excerpt being commented on
    val noteText: String,            // User commentary
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
