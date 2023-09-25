package sdk.sahha.android.domain.model.test

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
data class TestDataLocal(
    @PrimaryKey val metaId: String,
    val count: Long? = null,
    val json: String,
    val dataType: String,
    val lastModifiedTime: String
)

fun TestDataLocal.toTestDataPost(): TestDataPost {
    return TestDataPost(
        count = count,
        json = json,
        dataType = dataType,
        lastModifiedTime = lastModifiedTime
    )
}

@Keep
@Entity
data class TestDataPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val count: Long? = null,
    val json: String,
    val dataType: String,
    val lastModifiedTime: String
)
