package com.dc.retroapi.utils;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by HB on 22/8/19.
 */
public class WebServiceUtils {


  public static RequestBody getStringRequestBody(String value) {
    MediaType MEDIA_TYPE_TEXT = MediaType.parse("text/plain");
    RequestBody requestBody = RequestBody.create(MEDIA_TYPE_TEXT, value == null ? "" : value);
    return requestBody;
  }

  public static MultipartBody.Part getStringMultipartBodyPart(String key, String filePath) {
    File file = new File(filePath);
    RequestBody filebody = RequestBody.create(MediaType.parse("image/*"), file);
    return MultipartBody.Part.createFormData(key, file.getName(), filebody);
  }
}
