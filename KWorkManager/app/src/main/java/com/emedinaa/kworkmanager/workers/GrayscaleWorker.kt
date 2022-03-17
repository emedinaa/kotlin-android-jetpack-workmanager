package com.emedinaa.kworkmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.emedinaa.kworkmanager.utils.grayScaleBitmap
import com.emedinaa.kworkmanager.utils.writeBitmapToFile
import timber.log.Timber
import java.io.FileNotFoundException

class GrayscaleWorker(ctx: Context, params: WorkerParameters) : BaseWorker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext

        makeStatusNotification("Grayscale image", appContext)
        sleep()

        return try {
            val outputData = createGrayscaleBitmap(appContext, inputData.getString(KEY_IMAGE_URI))
            Result.success(outputData)
        } catch (fileNotFoundException: FileNotFoundException) {
            Timber.e(fileNotFoundException)
            throw RuntimeException("Failed to decode input stream", fileNotFoundException)
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            Result.failure()
        }
    }

    @Throws(FileNotFoundException::class, IllegalArgumentException::class)
    private fun createGrayscaleBitmap(appContext: Context, resourceUri: String?): Data {
        if (resourceUri.isNullOrEmpty()) {
            Timber.e("Invalid input uri")
            throw IllegalArgumentException("Invalid input uri")
        }

        val resolver = appContext.contentResolver

        val bitmap = BitmapFactory.decodeStream(
            resolver.openInputStream(Uri.parse(resourceUri))
        )

        val output = grayScaleBitmap(bitmap, appContext)

        val outputUri = writeBitmapToFile(appContext, output)

        return workDataOf(KEY_IMAGE_URI to outputUri.toString())
    }
}