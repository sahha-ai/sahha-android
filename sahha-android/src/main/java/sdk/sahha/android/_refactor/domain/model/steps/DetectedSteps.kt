package sdk.sahha.android._refactor.domain.model.steps

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class DetectedSteps(
  @PrimaryKey(autoGenerate = true) val id: Int,
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
    0,
    steps,
    distance,
    startDateTime,
    endDateTime,
    createdAt
  )
}
