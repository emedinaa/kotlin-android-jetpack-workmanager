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
    private val TAG_CLEANUP = "CLEANUP"
    private val TAG_FILTER = "FILTER"
    private val TAG_RESIZE = "RESIZE"
    private val TAG_UPLOAD = "UPLOAD"

    private val KEY_IMAGE_URI = "KEY_IMAGE_URI"

    internal var imageUri: Uri? = null
    //internal var cleanUpWorkInfoItems: LiveData<WorkInfo>?=null

    internal val cleanUpWorkInfoItems: LiveData<List<WorkInfo>>
    internal val grayScaleWorkInfoItems: LiveData<List<WorkInfo>>
    internal val resizeWorkInfoItems: LiveData<List<WorkInfo>>
    internal val uploadWorkInfoItems: LiveData<List<WorkInfo>>

    private val workManager= Injector.provideWorkManager()

    init {
        cleanUpWorkInfoItems=workManager.getWorkInfosByTagLiveData(TAG_CLEANUP)
        grayScaleWorkInfoItems=workManager.getWorkInfosByTagLiveData(TAG_FILTER)
        resizeWorkInfoItems=workManager.getWorkInfosByTagLiveData(TAG_RESIZE)
        uploadWorkInfoItems=workManager.getWorkInfosByTagLiveData(TAG_UPLOAD)
    }

    internal fun process(data:String){
        imageUri= Uri.parse(data)

        val cleanUploadBuilder= OneTimeWorkRequestBuilder<CleanupWorker>()
        cleanUploadBuilder.addTag(TAG_CLEANUP)
        val cleanUpWorker = cleanUploadBuilder.build()

        var continuation = workManager
            .beginUniqueWork(
                IMAGE_MANIPULATION_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                cleanUpWorker
            )

        val grayBuilder = OneTimeWorkRequestBuilder<GrayscaleWorker>()
        grayBuilder.setInputData(createInputDataForUri())
        grayBuilder.addTag(TAG_FILTER)
        val grayScaleWorker = grayBuilder.build()

        continuation = continuation.then(grayScaleWorker)

        val resizeConstraints = Constraints.Builder()
            .setRequiresCharging(true) // .setRequiresStorageNotLow(true)
            .build()

        val resizeBuilder = OneTimeWorkRequestBuilder<ResizeWorker>()
            .setConstraints(resizeConstraints)
            .addTag(TAG_RESIZE)
        val resizeWorker = resizeBuilder.build()

        continuation = continuation.then(resizeWorker)


        val upLoadconstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadBuilder= OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag(TAG_UPLOAD)
            .setConstraints(upLoadconstraints)

        val uploadWorker = uploadBuilder.build()

        continuation = continuation.then(uploadWorker)

        //cleanUpWorkInfoItems = workManager.getWorkInfoByIdLiveData(cleanUpWorker.id)
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
        //workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
        //workManager.cancelAllWork()
    }

}
