package io.tryvital.vitalhealthconnect.workers

import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodGlucoseRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.FloorsClimbedRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.HeightRecord
import androidx.health.connect.client.records.HydrationRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.Vo2MaxRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.response.ChangesResponse
import io.tryvital.vitalhealthconnect.model.VitalResource
import io.tryvital.vitalhealthconnect.model.processedresource.ProcessedResourceData
import io.tryvital.vitalhealthconnect.model.processedresource.TimeSeriesData
import io.tryvital.vitalhealthconnect.model.remapped
import io.tryvital.vitalhealthconnect.records.RecordProcessor
import io.tryvital.vitalhealthconnect.records.RecordReader
import java.time.Instant
import java.util.*
import kotlin.reflect.KClass

internal suspend fun processChangesResponse(
    resource: VitalResource,
    responses: ChangesResponse,
    timeZone: TimeZone,
    currentDevice: String,
    reader: RecordReader,
    processor: RecordProcessor,
    end: Instant? = null,
): ProcessedResourceData {
    val records = responses.changes
        .filterIsInstance<UpsertionChange>()
        .groupBy(keySelector = { it.record::class }, valueTransform = { it.record })

    val endAdjusted = end ?: Instant.MAX;


    suspend fun <Record, T : TimeSeriesData> readTimeseries(
        records: List<Record>,
        process: suspend (String, List<Record>) -> T
    ): ProcessedResourceData = process(currentDevice, records)
        .let(ProcessedResourceData::TimeSeries)

    return when (resource.remapped()) {
        VitalResource.ActiveEnergyBurned, VitalResource.BasalEnergyBurned, VitalResource.Steps ->
            throw IllegalArgumentException("Unexpected resource post remapped(): $resource")

        VitalResource.Activity -> processor.processActivitiesFromRecords(
            timeZone = timeZone,
            currentDevice = currentDevice,
            activeEnergyBurned = records.get<ActiveCaloriesBurnedRecord>()
                .filter { it.endTime <= endAdjusted },
            basalMetabolicRate = records.get<BasalMetabolicRateRecord>()
                .filter { it.time <= endAdjusted },
            floorsClimbed = records.get<FloorsClimbedRecord>().filter { it.endTime <= endAdjusted },
            distance = records.get<DistanceRecord>().filter { it.endTime <= endAdjusted },
            steps = records.get<StepsRecord>().filter { it.endTime <= endAdjusted },
            vo2Max = records.get<Vo2MaxRecord>().filter { it.time <= endAdjusted },
        ).let(ProcessedResourceData::Summary)

        VitalResource.Workout -> processor.processWorkoutsFromRecords(
            fallbackDeviceModel = currentDevice,
            exerciseRecords = records.get<ExerciseSessionRecord>()
                .filter { it.endTime <= endAdjusted }
        ).let(ProcessedResourceData::Summary)

        VitalResource.Sleep -> records.get<SleepSessionRecord>()
            .filter { it.endTime <= endAdjusted }.let { sessions ->
            processor.processSleepFromRecords(
                fallbackDeviceModel = currentDevice,
                sleepSessionRecords = sessions,
            ).let(ProcessedResourceData::Summary)
        }

        VitalResource.Body -> processor.processBodyFromRecords(
            fallbackDeviceModel = currentDevice,
            weightRecords = records.get<WeightRecord>().filter { it.time <= endAdjusted },
            bodyFatRecords = records.get<BodyFatRecord>().filter { it.time <= endAdjusted },
        ).let(ProcessedResourceData::Summary)

        VitalResource.Profile -> processor.processProfileFromRecords(
            heightRecords = records.get<HeightRecord>().filter { it.time <= endAdjusted }
        ).let(ProcessedResourceData::Summary)

        VitalResource.HeartRate ->
            readTimeseries(
                records.get<HeartRateRecord>().filter { it.endTime <= endAdjusted },
                processor::processHeartRateFromRecords
            )

        VitalResource.HeartRateVariability ->
            readTimeseries(
                records.get<HeartRateVariabilityRmssdRecord>().filter { it.time <= endAdjusted },
                processor::processHeartRateVariabilityRmssFromRecords
            )

        VitalResource.Glucose ->
            readTimeseries(
                records.get<BloodGlucoseRecord>().filter { it.time <= endAdjusted },
                processor::processGlucoseFromRecords
            )

        VitalResource.BloodPressure ->
            readTimeseries(
                records.get<BloodPressureRecord>().filter { it.time <= endAdjusted },
                processor::processBloodPressureFromRecords
            )

        VitalResource.Water ->
            readTimeseries(
                records.get<HydrationRecord>().filter { it.endTime <= endAdjusted },
                processor::processWaterFromRecords
            )
    }
}

inline fun <reified T : Record> Map<KClass<out Record>, List<Record>>.get(): List<T> {
    @Suppress("UNCHECKED_CAST")
    return (this[T::class] ?: emptyList()) as List<T>
}
