package com.sahha.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class SleepQueue(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sleepDurationMinutes: Long,
    val sleepDurationMillis: Long,
    val startMillis: Long,
    val endMillis: Long,
    val createdAt: String
) {
    constructor(startMillis: Long, endMillis: Long, createdAt: String) : this(
        0,
        (endMillis - startMillis) / 1000 / 60, // Convert the millis to minutes.
        endMillis - startMillis,
        startMillis,
        endMillis,
        createdAt
    )
}
