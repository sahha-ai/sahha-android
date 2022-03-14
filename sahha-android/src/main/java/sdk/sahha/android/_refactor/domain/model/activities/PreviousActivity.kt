package sdk.sahha.android._refactor.domain.model.activities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class PreviousActivity(
  @PrimaryKey val id: Int = 1,
  val activity: Int,
  val confidence: Int
) {
  constructor(activity: Int, confidence: Int) : this(1, activity, confidence)
}
