package sdk.sahha.android._refactor.domain.model.activities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class RecognisedActivity(
  @PrimaryKey(autoGenerate = true) val id: Int,
  val movementType: Int,
  val confidence: Int,
  val startDateTime: String
) {
  constructor(movementType: Int, confidence: Int, startDateTime: String) : this(
    0,
    movementType,
    confidence,
    startDateTime
  )
}
