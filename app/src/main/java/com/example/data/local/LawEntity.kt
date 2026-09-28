package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "laws",
    indices = [
        Index(value = ["radaId"], unique = true),
        Index(value = ["category"])
    ]
)
data class LawEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val radaId: String,               // e.g. "254к/96-вр" or "435-15"
    val title: String,                // Full title: "Конституція України"
    val shortTitle: String = "",      // e.g. "КУ", "ЦКУ"
    val radaUrl: String,              // Official rada.gov.ua URL
    val category: String,             // Legal area: "Конституційне", "Цивільне", etc.
    val fullText: String,             // Offline stored full legal text
    val summary: String = "",         // Law identifier / adoption date / description
    val dateAdopted: String = "",     // e.g. "28.06.1996"
    val status: String = "Чинний",    // Current status (Чинний / Зі змінами)
    val dateSaved: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val lastReadPosition: Int = 0
)
