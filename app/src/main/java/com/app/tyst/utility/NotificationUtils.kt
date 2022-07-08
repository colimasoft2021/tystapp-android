package com.app.tyst.utility

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.AsyncTask
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.tyst.MainApplication
import com.app.tyst.R
import com.app.tyst.ui.SplashActivity
import com.app.tyst.utility.helper.LOGApp
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.*


/**
 * Created by root on 31/5/18.
 */

@SuppressLint("NewApi")
class NotificationUtils {

    companion object {

        fun createAppNotification(mContext: Context, application: MainApplication, remoteMessage: RemoteMessage?) {

            try {
                var notificationHelper: NotificationHelper? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    if (notificationHelper == null) {
                    notificationHelper = NotificationHelper(mContext)
                    notificationHelper.setDefaultChannelIfNotSet(mContext)
//                    }
                }

                if (remoteMessage?.data?.isNotEmpty() == true) {//{"notification_type":"shared_crekid","user_id":"144","kid_id":"27"}

                    sendNotification(mContext, application, notificationHelper, remoteMessage.data)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * Create and show a simple notification containing the received FCM message.
         *
         * @param data FCM body received.
         */
        private fun sendNotification(mContext: Context, application: MainApplication, notificationHelper: NotificationHelper?, data: Map<String, String>) {
            try {

//  {others={"route_id":"96","type":"share route"}, id=eaa36, code=USER, badge=0, sound=, title=, message=Rahul Shingane share route Share Route 2}
                val jsonObject = JSONObject(data["others"])
                LOGApp.e(jsonObject.toString())


                // Create an Intent for the activity you want to start


                val resultIntent = Intent(mContext, getLaunchClass()).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }


                // Create the TaskStackBuilder and add the intent, which inflates the back stack
                val stackBuilder = TaskStackBuilder.create(mContext)
                stackBuilder.addNextIntentWithParentStack(resultIntent)
                val pendingIntent: PendingIntent?
                pendingIntent = if ((application.getCurrentActivity() == null) || (application.getCurrentActivity()?.isDestroyed == true)) {
                    stackBuilder.getPendingIntent(Random().nextInt(50), PendingIntent.FLAG_UPDATE_CURRENT)
                } else {
                    PendingIntent.getActivity(mContext, Random().nextInt(50)/* Request code */, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                }

                createNotification(mContext, application, notificationHelper, pendingIntent, data, jsonObject.getString(IConstants.PARAM_NOTIFICATION_TYPE))

            } catch (e: Exception) {
                e.printStackTrace()
//                Crashlytics.logException(e)
            }

        }

        private fun createNotification(mContext: Context, application: MainApplication, notificationHelper: NotificationHelper?, pendingIntent: PendingIntent?,
                                       data: Map<String, String>, notificationType: String) {

            val GROUP_KEY_ROUTE = "com.app.tyst.ROUTE"
            val notificationId = Random().nextInt(50)
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val title = if (data["title"]?.isNotEmpty() == true) {
                data["title"]
            } else {
                mContext.getString(R.string.application_name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nb = notificationHelper?.getNotificationDefault(title, data["message"])
                nb?.setSmallIcon(R.mipmap.ic_launcher)
                nb?.setSound(defaultSoundUri)
                nb?.setAutoCancel(true)
                nb?.setGroup(GROUP_KEY_ROUTE)
                nb?.setWhen(System.currentTimeMillis())
                nb?.color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark)

                nb?.setContentIntent(pendingIntent)

                notificationHelper?.notify(notificationId, nb)
                LOGApp.e("Notification Id : " + notificationId)
            } else {
                val notificationBuilder = NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(data["message"])
                        .setAutoCancel(true)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(data["message"]))
                        .setSound(defaultSoundUri)
                        .setGroup(GROUP_KEY_ROUTE)


                notificationBuilder.setContentIntent(pendingIntent)

                notificationBuilder.color = ContextCompat.getColor(mContext, R.color.colorPrimaryDark)
                val notificationManager = mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                notificationManager!!.notify(notificationId /* ID of notification */, notificationBuilder.build())
                LOGApp.e("Notification Id : " + notificationId)
            }
        }

        class ImageDownloader(val mContext: Context, val notificationHelper: NotificationHelper?, val pendingIntent: PendingIntent?,
                              val data: Map<String, String>, val retailerName: String) : AsyncTask<String, Void, Bitmap>() {
            override fun doInBackground(vararg p0: String?): Bitmap? {
                val input: InputStream
                try {

                    val url = URL(p0[0])
                    val connection = url.openConnection()
                    connection.doInput = true
                    connection.connect()
                    input = connection.getInputStream()
                    return BitmapFactory.decodeStream(input)
                } catch (e: MalformedURLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                return null
            }


            override fun onPreExecute() {
                super.onPreExecute()
                // ...
            }

            override fun onPostExecute(result: Bitmap?) {
                super.onPostExecute(result)

//                createNotification(mContext, notificationHelper, result, pendingIntent, data, retailerName)
            }
        }

        /**
         * Choose activity to open
         *
         * @return if any of first sync (contact or music) not completed than open login activity
         * else open main activity
         */
        private fun getLaunchClass(): Class<*> {
            return SplashActivity::class.java
        }

        private fun getHomePendingIntent(mContext: Context, application: MainApplication): PendingIntent {
            // Create an Intent for the activity you want to start

            val resultIntent =
                    Intent(mContext, SplashActivity::class.java)

            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Create the TaskStackBuilder and add the intent, which inflates the back stack
            val stackBuilder = TaskStackBuilder.create(mContext)
            stackBuilder.addNextIntentWithParentStack(resultIntent)
            return if ((application.getCurrentActivity() == null) || (application.getCurrentActivity()?.isDestroyed == true)) {
                stackBuilder.getPendingIntent(Random().nextInt(50), PendingIntent.FLAG_UPDATE_CURRENT)
            } else {
                PendingIntent.getActivity(mContext, Random().nextInt(50)/* Request code */, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        }
    }
}