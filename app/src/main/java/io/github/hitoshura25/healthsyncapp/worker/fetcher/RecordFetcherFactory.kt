package io.github.hitoshura25.healthsyncapp.worker.fetcher

import io.github.hitoshura25.healthsyncapp.data.SupportedHealthRecordType
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class RecordFetcherFactory @Inject constructor(
    private val fetchers: Map<KClass<out androidx.health.connect.client.records.Record>, @JvmSuppressWildcards Provider<out RecordFetcher>>
) {
    fun create(type: SupportedHealthRecordType<*>): RecordFetcher {
        val fetcherProvider = fetchers[type.recordKClass]
            ?: throw IllegalArgumentException("No fetcher found for type ${type.recordKClass.simpleName}")
        return fetcherProvider.get()
    }
}
