package sdk.sahha.android.domain.model.device

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class AppUsage(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val category: Int,
    val milliseconds: Long
) {
    constructor(name: String, category: Int, milliseconds: Long) : this(0, name, category, milliseconds)
}
