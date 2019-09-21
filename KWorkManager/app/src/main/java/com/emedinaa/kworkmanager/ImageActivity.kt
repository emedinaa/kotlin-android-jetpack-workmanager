package com.emedinaa.kworkmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image.*
import timber.log.Timber

class ImageActivity : BaseActivity() {

    private val REQUEST_CODE_IMAGE = 100

    private var imagePath:String?=null
    private lateinit var viewModel: ImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        viewModel = ViewModelProviders.of(this).get(ImageViewModel::class.java)
        viewModel.cleanUpWorkInfoItems.observe(this, cleanUpWorkInfosObserver)
        viewModel.grayScaleWorkInfoItems.observe(this, filterWorkInfosObserver)
        viewModel.resizeWorkInfoItems.observe(this, resizeWorkInfosObserver)
        viewModel.uploadWorkInfoItems.observe(this, uploadWorkInfosObserver)

        imageView.setOnClickListener {
            selectImage()
        }

        buttonProcess.setOnClickListener {
            imagePath?.let {
                progressBarTask1.visibility= View.VISIBLE
                buttonProcess.visibility=View.GONE
                viewModel.process(it)
            }
        }
    }

    private fun selectImage(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_IMAGE)
    }

    /*private val cleanUpWorkInfosObserver= Observer<WorkInfo> {workInfo ->
        if (workInfo!=null && workInfo.state.isFinished) {
            val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
            Timber.d("CleanUpWork completed")
        } else {
        }
    }*/

    private val cleanUpWorkInfosObserver= Observer<List<WorkInfo>> { listOfWorkInfo ->

        if (listOfWorkInfo.isNullOrEmpty()) {
            return@Observer
        }

        if (listOfWorkInfo[0]!=null ) {
            when(listOfWorkInfo[0].state){
                WorkInfo.State.SUCCEEDED ->{
                    showCleanUpWorkFinished()
                }

                WorkInfo.State.RUNNING ->{
                    imageViewTask1.setImageResource(R.drawable.circle_shape_gray)
                    progressBarTask1.visibility= View.VISIBLE
                }
            }
            Timber.d("CleanUpWork completed")
            progressBarTask1.visibility= View.GONE
        }
    }

    private val filterWorkInfosObserver= Observer<List<WorkInfo>> { listOfWorkInfo ->
        if (listOfWorkInfo.isNullOrEmpty()) {
            return@Observer
        }
        if (listOfWorkInfo[0]!=null) {
            when(listOfWorkInfo[0].state){
                WorkInfo.State.SUCCEEDED ->{
                    showGrayScaleWorkFinished()
                }

                WorkInfo.State.RUNNING ->{
                    imageViewTask2.setImageResource(R.drawable.circle_shape_gray)
                    progressBarTask2.visibility= View.VISIBLE
                }
            }
            Timber.d("FilterWork completed")
        }
    }

    private val resizeWorkInfosObserver= Observer<List<WorkInfo>> { listOfWorkInfo ->
        if (listOfWorkInfo.isNullOrEmpty()) {
            return@Observer
        }

        if (listOfWorkInfo[0]!=null) {
            when(listOfWorkInfo[0].state){
                WorkInfo.State.SUCCEEDED ->{
                    showResizeWorkFinished()
                }

                WorkInfo.State.RUNNING ->{
                    imageViewTask3.setImageResource(R.drawable.circle_shape_gray)
                    progressBarTask3.visibility= View.VISIBLE
                }
            }
            Timber.d("ResizeWork completed")
        }
    }

    private val uploadWorkInfosObserver= Observer<List<WorkInfo>> { listOfWorkInfo ->
        if (listOfWorkInfo.isNullOrEmpty()) {
            return@Observer
        }

        if (listOfWorkInfo[0]!=null) {
            when(listOfWorkInfo[0].state){
                WorkInfo.State.SUCCEEDED ->{
                    showUploadWorkFinished()
                    buttonProcess.visibility=View.VISIBLE
                }

                WorkInfo.State.RUNNING ->{
                    imageViewTask4.setImageResource(R.drawable.circle_shape_gray)
                    progressBarTask4.visibility= View.VISIBLE
                }
            }
            Timber.d("UploadWork completed")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            imagePath = data?.data?.toString()
            imagePath?.let {
                Glide.with(this).load(it).into(imageView)
            }
        } else {
            Timber.d("Ocurrió un error $resultCode")
        }
    }

    private fun showCleanUpWorkFinished(){
        progressBarTask1.visibility= View.GONE
        imageViewTask1.setImageResource(R.drawable.circle_shape)
    }

    private fun showGrayScaleWorkFinished(){
        progressBarTask2.visibility= View.GONE
        imageViewTask2.setImageResource(R.drawable.circle_shape)
    }

    private fun showResizeWorkFinished(){
        progressBarTask3.visibility= View.GONE
        imageViewTask3.setImageResource(R.drawable.circle_shape)
    }

    private fun showUploadWorkFinished(){
        progressBarTask4.visibility= View.GONE
        imageViewTask4.setImageResource(R.drawable.circle_shape)
    }
}
