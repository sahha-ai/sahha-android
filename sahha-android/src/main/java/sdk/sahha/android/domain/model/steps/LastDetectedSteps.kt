package sdk.sahha.android.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LastDetectedSteps(
  @PrimaryKey val id: Int = 1,
  val steps: Int,
  val distance: Int,
  val startDateTime: String,
  val endDateTime: String,
  val createdAt: String
) {
  constructor(
    steps: Int,
    distance: Int,
    startDateTime: String,
    endDateTime: String,
    createdAt: String
  ) : this(
    1,
    steps,
    distance,
    startDateTime,
    endDateTime,
    createdAt
  )
}
