package com.app.tyst.data.model.response

import com.google.gson.annotations.SerializedName

data class StaticPageResponse(

	@field:SerializedName("page_id")
	val pageId: String? = null,

	@field:SerializedName("page_title")
	val pageTitle: String? = null,

	@field:SerializedName("page_code")
	val pageCode: String? = null,

	@field:SerializedName("page_content")
	val content: String? = null
)