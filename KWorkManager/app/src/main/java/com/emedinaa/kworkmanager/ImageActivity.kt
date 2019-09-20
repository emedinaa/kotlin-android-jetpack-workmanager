package com.emedinaa.kworkmanager

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkInfo
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image.*
import timber.log.Timber

class ImageActivity : BaseActivity() {

    private val KEY_IMAGE_URI = "KEY_IMAGE_URI"
    private val REQUEST_CODE_IMAGE = 100

    private var imagePath:String?=null
    private lateinit var viewModel: ImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        viewModel = ViewModelProviders.of(this).get(ImageViewModel::class.java)
        viewModel.outputWorkInfoItems.observe(this, workInfosObserver)


        imageView.setOnClickListener {
            selectImage()
        }

        buttonProcess.setOnClickListener {
            imagePath?.let {
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

    private val workInfosObserver= Observer<List<WorkInfo>> {listOfWorkInfo ->
        if (listOfWorkInfo.isNullOrEmpty()) {
            return@Observer
        }

        val workInfo = listOfWorkInfo[0]

        if (workInfo.state.isFinished) {
            showWorkFinished()

            val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

        } else {
            showWorkInProgress()
        }
    }

    private fun showWorkInProgress() {}

    private fun showWorkFinished() {}


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_IMAGE && resultCode == Activity.RESULT_OK) {
            imagePath = data?.data?.toString()
            imagePath?.let {
                Glide.with(this).load(it).into(imageView)
            }
        } else {
            Timber.e("Ocurrió un error $resultCode")
        }
    }

}
