package com.sahha.android.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class SleepQueueHistory(
    @PrimaryKey val id: Int,
    val sleepDurationMinutes: Long,
    val sleepDurationMillis: Long,
    val startMillis: Long,
    val endMillis: Long,
    val createdAt: String
) {
    constructor(id: Int, startMillis: Long, endMillis: Long, createdAt: String) : this(
        id,
        (endMillis - startMillis) / 1000 / 60, // Convert the millis to minutes.
        endMillis - startMillis,
        startMillis,
        endMillis,
        createdAt
    )
}
