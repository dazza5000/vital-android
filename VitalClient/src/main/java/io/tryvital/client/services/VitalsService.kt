package io.tryvital.client.services

import io.tryvital.client.services.data.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.*
import java.time.Instant
import java.util.*

@Suppress("unused")
class VitalsService private constructor(private val timeSeries: TimeSeries) {

    suspend fun getGlucose(
        userId: String,
        startDate: Instant,
        endDate: Instant? = null,
        provider: String? = null,
        cursor: String? = null,
    ): GroupedSamplesResponse<ScalarSample> {
        return timeSeries.scalarSampleTimeseriesRequest(
            userId = userId, resource = "glucose", startDate = startDate,
            endDate = endDate, provider = provider, cursor = cursor,
        )
    }

    suspend fun getCholesterol(
        cholesterolType: CholesterolType,
        userId: String,
        startDate: Instant,
        endDate: Instant? = null,
        provider: String? = null,
        cursor: String? = null,
    ): GroupedSamplesResponse<ScalarSample> {
        return timeSeries.scalarSampleTimeseriesRequest(
            userId = userId,
            resource = "cholesterol/${cholesterolType.name}",
            startDate = startDate,
            endDate = endDate,
            provider = provider,
            cursor = cursor,
        )
    }

    suspend fun getIge(
        userId: String,
        startDate: Instant,
        endDate: Instant? = null,
        provider: String? = null,
        cursor: String? = null,
    ): GroupedSamplesResponse<ScalarSample> {
        return timeSeries.scalarSampleTimeseriesRequest(
            userId = userId,
            resource = "ige",
            startDate = startDate,
            endDate = endDate,
            provider = provider,
            cursor = cursor,
        )
    }

    suspend fun getIgg(
        userId: String,
        startDate: Instant,
        endDate: Instant? = null,
        provider: String? = null,
        cursor: String? = null,
    ): GroupedSamplesResponse<ScalarSample> {
        return timeSeries.scalarSampleTimeseriesRequest(
            userId = userId,
            resource = "igg",
            startDate = startDate,
            endDate = endDate,
            provider = provider,
            cursor = cursor,
        )
    }

    suspend fun getHeartrate(
        userId: String,
        startDate: Instant,
        endDate: Instant? = null,
        provider: String? = null,
        cursor: String? = null,
    ): GroupedSamplesResponse<ScalarSample> {
        return timeSeries.scalarSampleTimeseriesRequest(
            userId = userId,
            resource = "heartrate",
            startDate = startDate,
            endDate = endDate,
            provider = provider,
            cursor = cursor,
        )
    }

    suspend fun sendBloodPressure(
        userId: String,
        timeseriesPayload: TimeseriesPayload<List<BloodPressureSamplePayload>>
    ) {
        timeSeries.bloodPressureTimeseriesPost(
            userId = userId,
            resource = "blood_pressure",
            payload = timeseriesPayload
        )
    }

    suspend fun sendQuantitySamples(
        resource: IngestibleTimeseriesResource,
        userId: String,
        timeseriesPayload: TimeseriesPayload<List<QuantitySamplePayload>>
    ) {
        timeSeries.timeseriesPost(
            userId = userId,
            resource = resource.toString(),
            payload = timeseriesPayload
        )
    }

    companion object {
        fun create(retrofit: Retrofit): VitalsService {
            return VitalsService(retrofit.create(TimeSeries::class.java))
        }
    }
}

private interface TimeSeries {
    @GET("timeseries/{user_id}/{resource}")
    suspend fun scalarSampleTimeseriesRequest(
        @Path("user_id") userId: String,
        @Path("resource", encoded = true) resource: String,
        @Query("start_date") startDate: Instant,
        @Query("end_date") endDate: Instant? = null,
        @Query("provider") provider: String? = null,
        @Query("cursor") cursor: String? = null,
    ): GroupedSamplesResponse<ScalarSample>

    @POST("timeseries/{user_id}/{resource}")
    suspend fun timeseriesPost(
        @Path("user_id") userId: String,
        @Path("resource", encoded = true) resource: String,
        @Body payload: TimeseriesPayload<List<QuantitySamplePayload>>
    ): Response<Unit>

    @POST("timeseries/{user_id}/{resource}")
    suspend fun bloodPressureTimeseriesPost(
        @Path("user_id") userId: String,
        @Path("resource", encoded = true) resource: String,
        @Body payload: TimeseriesPayload<List<BloodPressureSamplePayload>>
    ): Response<Unit>
}

