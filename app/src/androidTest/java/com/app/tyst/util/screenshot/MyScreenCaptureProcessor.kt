package com.app.tyst.util.screenshot

import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import androidx.test.runner.screenshot.BasicScreenCaptureProcessor
import java.io.File

class MyScreenCaptureProcessor(parentFolderPath: String) : BasicScreenCaptureProcessor() {

    init {
        this.mDefaultScreenshotPath = File(
                File(
                        getExternalStoragePublicDirectory(DIRECTORY_PICTURES),
                        "my_app_folder"
                ).absolutePath,
                "screenshots/$parentFolderPath"
        )
        this.mDefaultScreenshotPath.mkdirs()
    }

    override fun getFilename(prefix: String): String = prefix
}