package com.emedinaa.kworkmanager

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.emedinaa.kworkmanager.di.Injector
import com.example.background.workers.CleanupWorker
import com.example.background.workers.GrayscaleWorker
import com.example.background.workers.ResizeWorker
import com.example.background.workers.UploadWorker

class ImageViewModel:ViewModel(){

    protected val IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work"
    private val TAG_OUTPUT= "OUTPUT"
    private val KEY_IMAGE_URI = "KEY_IMAGE_URI"

    internal var imageUri: Uri? = null
    internal val outputWorkInfoItems: LiveData<List<WorkInfo>>

    private val workManager= Injector.provideWorkManager()

    init {
        outputWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    internal fun process(data:String){
        imageUri= Uri.parse(data)

        var continuation = workManager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequest.from(CleanupWorker::class.java)
            )


        val grayBuilder = OneTimeWorkRequestBuilder<GrayscaleWorker>()
        grayBuilder.setInputData(createInputDataForUri())
        continuation = continuation.then(grayBuilder.build())

        val constraints = Constraints.Builder()
            .setRequiresCharging(true)
            .build()

        val resize = OneTimeWorkRequestBuilder<ResizeWorker>()
            .setConstraints(constraints)
            .build()
        continuation = continuation.then(resize)


        val upload= OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(upload)
        continuation.enqueue()
    }


    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

}
