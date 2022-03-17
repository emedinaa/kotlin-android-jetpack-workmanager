@file:JvmName("RenderscriptUtils")

package com.emedinaa.kworkmanager.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.renderscript.Allocation
import androidx.renderscript.RenderScript
import com.emedinaa.kworkmanager.ScriptC_grayscale

@WorkerThread
fun grayScaleBitmap(bitmap: Bitmap, applicationContext: Context): Bitmap {
    lateinit var rsContext: RenderScript
    try {

        val output = Bitmap.createBitmap(
            bitmap.width, bitmap.height, bitmap.config
        )

        rsContext = RenderScript.create(applicationContext, RenderScript.ContextType.DEBUG)
        val grayscale = ScriptC_grayscale(rsContext)

        val inputAllocation = Allocation.createFromBitmap(
            rsContext,
            bitmap,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SHARED or Allocation.USAGE_GRAPHICS_TEXTURE or Allocation.USAGE_SCRIPT
        )
        val outputAllocation = Allocation.createFromBitmap(
            rsContext,
            output,
            Allocation.MipmapControl.MIPMAP_NONE,
            Allocation.USAGE_SHARED or Allocation.USAGE_SCRIPT
        )
        grayscale.forEach_grayscale(inputAllocation, outputAllocation)
        outputAllocation.copyTo(output)

        return output
    } finally {
        rsContext.finish()
    }
}