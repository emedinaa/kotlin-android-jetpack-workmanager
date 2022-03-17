package com.emedinaa.kworkmanager.workers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.work.WorkerParameters
import com.emedinaa.kworkmanager.storage.APIClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

private const val FORM_DATA_PARAM = "userPhoto"

class UploadWorker(ctx: Context, param: WorkerParameters) : BaseWorker(ctx, param) {

    override fun doWork(): Result {
        val appContext = applicationContext
        makeStatusNotification("Upload image", appContext)
        sleep()
        return try {
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val uri = Uri.parse(resourceUri)
            val file = File(getRealPathFromUri(appContext, uri))

            val requestFile =
                RequestBody.create(MediaType.parse(appContext.contentResolver.getType(uri)), file)
            val multipartBody =
                MultipartBody.Part.createFormData(FORM_DATA_PARAM, file.name, requestFile)

            val call = APIClient().build().uploadFile(multipartBody)
            val response: Response<Any> = call.execute()

            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.failure()
            }

            Result.success()
        } catch (exception: Exception) {
            Result.failure()
        }
    }

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            return cursor?.getString(columnIndex ?: 0) ?: ""
        } finally {
            cursor?.close()
        }
    }
}