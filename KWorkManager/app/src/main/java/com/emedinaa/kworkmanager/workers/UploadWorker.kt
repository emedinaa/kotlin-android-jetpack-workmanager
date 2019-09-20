package com.example.background.workers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.work.WorkerParameters
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File
import android.provider.MediaStore
import com.emedinaa.kworkmanager.storage.APIClient
import com.emedinaa.kworkmanager.workers.BaseWorker

class UploadWorker(ctx:Context,param:WorkerParameters):BaseWorker(ctx,param) {

    private val FORMDATA_PARAM= "userPhoto"

    override fun doWork(): Result {
        val appContext = applicationContext
        makeStatusNotification("Upload image", appContext)
        sleep()
        return try{
            val resourceUri = inputData.getString(KEY_IMAGE_URI)
            val uri = Uri.parse(resourceUri)
            val file = File(getRealPathFromUri(appContext,uri))

            val requestFile = RequestBody.create(MediaType.parse(appContext.contentResolver.getType(uri)),file)
            val multipartBody= MultipartBody.Part.createFormData(FORMDATA_PARAM,file.name,requestFile)

            val call = APIClient().build().uploadFile(multipartBody)
            val response:Response<Any> = call.execute()

            if(response.isSuccessful){
                Result.success()
            }else{
                Result.failure()
            }

            Result.success()
        }catch (e:Exception){
            Result.failure()
        }
    }

    private fun getRealPathFromUri(context: Context, contentUri: Uri): String {
        var cursor: Cursor? = null
        try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            return cursor?.getString(column_index?:0)?:""
        } finally {
            cursor?.close()
        }
    }
}