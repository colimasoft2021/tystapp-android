package com.app.tyst.utility.extension

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.preference.PreferenceManager.*
import android.telephony.PhoneNumberUtils
import android.util.Base64
import android.widget.EditText
import android.widget.Toast
import com.andrognito.flashbar.Flashbar
import com.app.tyst.BuildConfig
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.data.local.AppPrefrrences
import com.app.tyst.data.model.hb.Settings
import com.app.tyst.utility.IConstants.Companion.SNAKBAR_TYPE_ERROR
import com.app.tyst.utility.IConstants.Companion.SNAKBAR_TYPE_MESSAGE
import com.app.tyst.utility.IConstants.Companion.SNAKBAR_TYPE_SUCCESS
import com.app.tyst.utility.helper.LOGApp
import javax.crypto.Cipher
import com.google.gson.GsonBuilder
import com.hb.logger.Logger
import com.hb.logger.Logger.Companion.context
import com.hb.logger.data.model.CustomLog
import java.io.*
import java.net.URLDecoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Created by hb on 15/3/18.
 */

val sharedPreference: AppPrefrrences by lazy {
    MainApplication.sharedPreference
}

var flashbar: Flashbar? = null

fun parseBoolean(type: String): Boolean {
    return type == "1"
}

fun String.getNumberOnly(): String {
    return this.replace("[^0-9]", "")
}

//fun String.showToast(context: Context?, toastType: Int) {
//    if (context != null)
//        FancyToast.makeText(context, this, FancyToast.LENGTH_SHORT, toastType, false).show()
//}

fun String.showSnackBar(context: Activity?) {
    showSnackBar(context, SNAKBAR_TYPE_ERROR, null)
}

fun String.showSnackBar(
        context: Activity?,
        type: Int,
        dismissListener: Flashbar.OnBarDismissListener? = null,
        duration: Long = 3000
) {
    if (context != null) {
        val logger = Logger(this::class.java.simpleName)
        var color = R.color.colorBlue
        when (type) {
            SNAKBAR_TYPE_ERROR -> {
                color = R.color.colorRed
                logger.debugEvent("Error Message", this, CustomLog.STATUS_ERROR)
            }
            SNAKBAR_TYPE_SUCCESS -> {
                color = R.color.colorGreen
                logger.debugEvent("Success Message", this,CustomLog.STATUS_SUCCESS)
            }
            SNAKBAR_TYPE_MESSAGE -> {
                color = R.color.colorBlue
                logger.debugEvent("User Message", this)
            }
        }

        if (((flashbar?.isShowing() == true) || (flashbar?.isShown() == true))) {
            flashbar?.dismiss()
        }

        val builder = Flashbar.Builder(context)

        builder
                .gravity(Flashbar.Gravity.TOP)
                .title(context.getString(R.string.application_name))
                .message(this)
                .backgroundColorRes(color)
                .enableSwipeToDismiss()
                .duration(duration)
        if (dismissListener != null)
            builder.barDismissListener(dismissListener)

        flashbar = builder.build()
        flashbar?.show()
    }
}


fun String.showNoInternetMessage(context: Context?) {
    if (context is Activity)
        this.showSnackBar(context, SNAKBAR_TYPE_MESSAGE, null)
}


fun EditText.getTrimText(): String {
    return this.text?.toString()?.trim() ?: ""
}

fun String.isValidFloat(): Boolean {
    var valid = false
    valid = try {
        this.toDouble()
        true
    } catch (ex: Exception) {
        false
    }
    return valid
}

fun String.isValidInt(): Boolean {
    var valid = false
    valid = try {
        this.toInt()
        true
    } catch (ex: Exception) {
        false
    }
    return valid
}

fun String.isZero(): Boolean {
    return if (this.isEmpty()) true
    else this.isValidFloat() && this.toDouble() == 0.0
}


fun String.getFailureSettings(code: String = "0"): Settings {
    val settings = Settings()
    settings.success = code
    settings.message = this
    return settings
}


/**
 * This function will return formatted phone number according to given country
 * @countryCode : the ISO 3166-1 two letters country code whose convention will be used if the phoneNumberE164 is null or invalid, or if phoneNumber contains IDD.
 */
fun String.getFormattedPhoneNumber(countryCode: String): String {
    return PhoneNumberUtils.formatNumber(this, "E164", countryCode)
}


fun getAppVersion(context: Context?, withoutLabel: Boolean = false): String {
    try {
        val pInfo = context?.packageManager?.getPackageInfo(context.packageName, 0)
        return if (withoutLabel) pInfo?.versionName ?: "" else "Version: " + pInfo?.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
    }
    return ""
}

fun String.makePhoneCall(context: Activity?) {
    try {
        context?.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", this, null)))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun String.openWebBrowser(context: Activity?) {
    val url = if (!this.startsWith("http://") && !this.startsWith("https://"))
        "http://" + this;
    else
        this
    try {
        context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}

fun String.removeBR(): String {
    return this.replace("<br/>", " ")
}

fun Int.getDurationFromSecond(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60

    return String.format("%02d h %02d min", hours, minutes, seconds)
}

fun String?.parseInt(defaultVal: Int = 0): Int {

    return when {
        this.isNullOrBlank() -> defaultVal
        this.isValidInt() -> this.toInt()
        else -> defaultVal
    }
}

/**
 * Encode giving string into base64
 */
fun String.encodeToBase64(): String {
    try {
        val data = this.toByteArray(charset("UTF-8"))
        LOGApp.i("Base 64 ", Base64.encodeToString(data, Base64.DEFAULT))
        return Base64.encodeToString(data, Base64.DEFAULT)
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }
    return ""
}

/**
 * Decode giving string into base64
 */
fun String.decodeToBase64(): String {
    return String(Base64.decode(this, Base64.DEFAULT))
}

/**Convert UTC time to Local*/
fun String?.utcToLocal(): String {
    LOGApp.e("UTC Date:-$this")
    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    df.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        val date = df.parse(this)
        LOGApp.e("Local Date:-$date")
        date.timeAgo()
    } catch (e: Exception) {
        e.printStackTrace()
        this ?: ""
    }
}

/**Time Ago Logic**/
fun Date.timeAgo(): String {
    LOGApp.e("millis:- $this")
    val different = Date().time - this.time
    LOGApp.e("Difference in time:- $different")
    val seconds = different.div(1000)
    LOGApp.e("seconds$seconds")
    val secondsInMilli = 1
    val minutesInSec = secondsInMilli * 60 //60
    val hoursInSec = minutesInSec * 60   //3600
    val daysInSec = hoursInSec * 24  // 86400
    val weekInSec = daysInSec * 7   // 604800
    val yearInSec = weekInSec * 52
    return when {
        seconds < minutesInSec -> "Just now"
        seconds < hoursInSec -> seconds.div(minutesInSec).toString() + " min ago"
        seconds < daysInSec -> seconds.div(hoursInSec).toString() + " hour ago"
        seconds < weekInSec -> seconds.div(daysInSec).toString() + " day ago"
        seconds < yearInSec -> seconds.div(weekInSec).toString() + " week ago"
        else -> seconds.div(yearInSec).toString() + " year ago"
    }
}

fun getObjectFromJsonString(jsonData: String, modelClass: Class<*>): Any? {
    return try {
        GsonBuilder().create().fromJson(jsonData, modelClass)
    } catch (e: java.lang.Exception) {
        null
    }
}

fun readStringFileFromAsset(context: Context, fileName: String): String {
    return try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer, Charsets.UTF_8)
    } catch (ex: IOException) {
        ex.printStackTrace()
        ""
    }
}

fun String?.toCurrency(local: Locale = Locale.US): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this?.toDoubleOrNull()
            ?: 0.0).toString()
}

fun Double?.toCurrency(local: Locale = Locale.US): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(this ?: 0.0).toString()
}

fun parseMoneyValue(value: String, groupingSeparator: String, currencySymbol: String): String =
        value.replace(groupingSeparator, "").replace(currencySymbol, "")

fun Double.whatPercentageOf(totalValue: Double): Double {
    return (this * 100) / totalValue
}

fun Double.uptoTwoDecimal(): String {
    return String.format("%.2f", this)
}

fun String.parseCurrencyToDouble(): Double {
    return try {
        NumberFormat.getCurrencyInstance(Locale.US).parse(this).toDouble()
    } catch (ex: Exception) {
        0.0
    }
}

/*fun String.toEncrypt():String{
    val plaintext: ByteArray = this.toByteArray()
    val keygen = KeyGenerator.getInstance("AES")
    keygen.init(256)
    val key: SecretKey = keygen.generateKey()
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val ciphertext: ByteArray = cipher.doFinal(plaintext)
    saveInitializationVector(context!!.applicationContext , cipher.iv)
    return ciphertext.toString()
}*/

/*fun String.toDecrypt():String{
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val ivSpec = IvParameterSpec(getSavedInitializationVector(context!!.applicationContext))
    cipher.init(Cipher.DECRYPT_MODE, getSavedSecretKey(context), ivSpec)
    val cipherText = cipher.doFinal(dataToDecrypt)
    return encrypt.decrypt(URLDecoder.decode(this))
}*/
fun String.toEncrypt(): String {
    val plainText = this.toByteArray()
    val keygen = KeyGenerator.getInstance("AES")
    keygen.init(256)
    val key = keygen.generateKey()
    saveSecretKey(context!!.applicationContext, key)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val cipherText = cipher.doFinal(plainText)
    saveInitializationVector(context!!.applicationContext, cipher.iv)

    val sb = StringBuilder()
    for (b in cipherText.indices) {
        sb.append(b.toChar())
    }
    Toast.makeText(context, "dbg encrypted = [" + sb.toString() + "]", Toast.LENGTH_LONG).show()

    return cipherText.toString()
}

fun String.decrypt(): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val ivSpec = IvParameterSpec(getSavedInitializationVector(context!!.applicationContext))
    cipher.init(Cipher.DECRYPT_MODE, getSavedSecretKey(context!!.applicationContext), ivSpec)
    val cipherText = cipher.doFinal(this.toByteArray())

    val sb = StringBuilder()
    for (b in cipherText.indices) {
        sb.append(b.toChar())
    }
    Toast.makeText(context, "dbg decrypted = [" + sb.toString() + "]", Toast.LENGTH_LONG).show()

    return cipherText.toString()
}

@Suppress("DEPRECATION")
fun saveSecretKey(context:Context, secretKey: SecretKey) {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(secretKey)
    val strToSave = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
    val sharedPref = getDefaultSharedPreferences(context)
    val editor = sharedPref.edit()
    editor.putString("secret_key", strToSave)
    editor.apply()
}

@Suppress("DEPRECATION")
fun getSavedSecretKey(context: Context): SecretKey {
    val sharedPref = getDefaultSharedPreferences(context)
    val strSecretKey = sharedPref.getString("secret_key", "")
    val bytes = Base64.decode(strSecretKey, Base64.DEFAULT)
    val ois = ObjectInputStream(ByteArrayInputStream(bytes))
    val secretKey = ois.readObject() as SecretKey
    return secretKey
}

@Suppress("DEPRECATION")
fun saveInitializationVector(context: Context, initializationVector: ByteArray) {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(initializationVector)
    val strToSave = String(Base64.encode(baos.toByteArray(), Base64.DEFAULT))
    val sharedPref = getDefaultSharedPreferences(context)
    val editor = sharedPref.edit()
    editor.putString("initialization_vector", strToSave)
    editor.apply()
}

fun getSavedInitializationVector(context: Context) : ByteArray {
    val sharedPref = getDefaultSharedPreferences(context)
    val strInitializationVector = sharedPref.getString("initialization_vector", "")
    val bytes = Base64.decode(strInitializationVector, Base64.DEFAULT)
    val ois = ObjectInputStream(ByteArrayInputStream(bytes))
    val initializationVector = ois.readObject() as ByteArray
    return initializationVector
}
