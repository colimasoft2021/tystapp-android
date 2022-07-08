package com.app.tyst.ui.settings.feedback

import android.net.Uri

class FeedbackImageModel (var contentUri: Uri? = null,
                          var imagePath: String = ""){

    /**
     * This will show add image button in list item for add image
     */
    fun showAddButton():Boolean{
        return contentUri == null
    }
}