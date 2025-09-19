package io.github.hitoshura25.healthsyncapp.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.hitoshura25.healthsyncapp.service.AvroFileProcessingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AvroFileProcessorWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val avroFileProcessingService: AvroFileProcessingService
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "AvroFileProcessorWorker"
    }

    private val TAG = "AvroFileProcWorker"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.i(TAG, "$WORK_NAME started.") // Use WORK_NAME in log
        try {
            val success = avroFileProcessingService.processStagedAvroFiles()
            if (success) {
                Log.i(TAG, "AvroFileProcessingService completed successfully for $WORK_NAME.")
                Result.success()
            } else {
                Log.w(TAG, "AvroFileProcessingService reported one or more files failed to process or move for $WORK_NAME.")
                Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during Avro file processing in $WORK_NAME.", e)
            Result.retry() 
        }
    }
}
