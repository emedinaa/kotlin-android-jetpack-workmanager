package com.emedinaa.kworkmanager

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.work.*
import com.emedinaa.kworkmanager.di.Injector
import com.emedinaa.kworkmanager.workers.CleanupWorker
import com.emedinaa.kworkmanager.workers.GrayscaleWorker
import com.emedinaa.kworkmanager.workers.ResizeWorker
import com.emedinaa.kworkmanager.workers.UploadWorker

private const val IMAGE_MANIPULATION_WORK_NAME = "image_manipulation_work"
private const val TAG_CLEANUP = "CLEANUP"
private const val TAG_FILTER = "FILTER"
private const val TAG_RESIZE = "RESIZE"
private const val TAG_UPLOAD = "UPLOAD"

private const val KEY_IMAGE_URI = "KEY_IMAGE_URI"

class ImageViewModel : ViewModel() {

    private var imageUri: Uri? = null
    //internal var cleanUpWorkInfoItems: LiveData<WorkInfo>?=null

    private val cleanUpWorkInfoItems: LiveData<List<WorkInfo>>
    private val grayScaleWorkInfoItems: LiveData<List<WorkInfo>>
    private val resizeWorkInfoItems: LiveData<List<WorkInfo>>
    private val uploadWorkInfoItems: LiveData<List<WorkInfo>>

    private val workManager = Injector.provideWorkManager()

    init {
        cleanUpWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_CLEANUP)
        grayScaleWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_FILTER)
        resizeWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_RESIZE)
        uploadWorkInfoItems = workManager.getWorkInfosByTagLiveData(TAG_UPLOAD)
    }

    internal fun process(data: String) {
        imageUri = Uri.parse(data)

        val cleanUploadBuilder = OneTimeWorkRequestBuilder<CleanupWorker>()
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

        val upLoadConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val uploadBuilder = OneTimeWorkRequestBuilder<UploadWorker>()
            .addTag(TAG_UPLOAD)
            .setConstraints(upLoadConstraints)

        val uploadWorker = uploadBuilder.build()

        continuation = continuation.then(uploadWorker)

        //cleanUpWorkInfoItems = workManager.getWorkInfoByIdLiveData(cleanUpWorker.id)
        continuation.enqueue()
    }


    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, it.toString())
        }
        return builder.build()
    }

    internal fun cancelWork() {
        //workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
        //workManager.cancelAllWork()
    }

}
