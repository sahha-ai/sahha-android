package sdk.sahha.android.domain.mapper

interface HealthConnectConstantsMapper {
    fun devices(constantInt: Int?): String
    fun recordingMethod(constantInt: Int): String
    fun mealType(constantInt: Int): String?
    fun relationToMeal(constantInt: Int): String?
    fun specimenSource(constantInt: Int): String?
    fun bodyPosition(constantInt: Int): String?
    fun measurementLocation(constantInt: Int): String?
    fun sleepStages(constantInt: Int): String?
    fun measurementMethod(constantInt: Int): String?
    fun bodyTempMeasurementLocation(constantInt: Int): String?
    fun exerciseTypes(constantInt: Int): String?
    fun exerciseSegments(constantInt: Int): String?
}