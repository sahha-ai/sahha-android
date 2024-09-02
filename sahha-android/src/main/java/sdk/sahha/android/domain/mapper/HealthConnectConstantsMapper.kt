package sdk.sahha.android.domain.mapper

internal interface HealthConnectConstantsMapper {
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
    fun cervicalMucusAppearance(appearanceInt: Int): String?
    fun cervicalMucusSensation(sensationInt: Int): String?
    fun menstruationFlow(flowInt: Int): String?
    fun ovulationTestResult(resultInt: Int): String?
    fun sexualActivityProtectionUsed(protectionUsedInt: Int): String?
}