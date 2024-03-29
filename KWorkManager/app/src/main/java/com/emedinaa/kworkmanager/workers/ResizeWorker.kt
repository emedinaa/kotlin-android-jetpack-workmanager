package com.emedinaa.kworkmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

private const val TITLE = "Resized Image"

class ResizeWorker(ctx: Context, params: WorkerParameters) : BaseWorker(ctx, params) {

    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    override fun doWork(): Result {
        val appContext = applicationContext
        makeStatusNotification("Resizing image", applicationContext)
        sleep()

        val resolver = appContext.contentResolver
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)

            val bitmap = BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)), null, this
                )

                inSampleSize = calculateInSampleSize(this, 100, 100)

                inJustDecodeBounds = false

                BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)), null, this
                )
            }

            if (bitmap != null) {
                val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, TITLE, dateFormatter.format(Date())
                )

                if (!imageUrl.isNullOrEmpty()) {
                    val output = workDataOf(KEY_IMAGE_URI to imageUrl)
                    Result.success(output)
                } else {
                    Timber.e("Writing to MediaStore failed")
                    Result.failure()
                }
            } else {
                Timber.e("Writing to MediaStore failed")
                Result.failure()
            }
        } catch (exception: Exception) {
            Timber.e("Unable to save image to Gallery $exception")
            Result.failure()
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

}
