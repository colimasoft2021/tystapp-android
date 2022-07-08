package com.app.tyst.util

import android.app.Activity
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import com.app.tyst.R
import com.squareup.spoon.Spoon

/******** Spoon jar command for generate test cases  **************/
//  java -jar /Users/hb/Desktop/MyBackup/Software/SpoonJar/spoon-runner-1.7.1-jar-with-dependencies.jar --apk /Users/hb/Desktop/MyBackup/Software/SpoonJar/app.apk --test-apk /Users/hb/Desktop/MyBackup/Software/SpoonJar/atest.apk --sdk /Users/hb/Library/Android/sdk

fun capture(activity: Activity?, tag: String, className: String, method: String) {
    Spoon.screenshot(activity, tag)
}

fun String.fillEmail() {
    Espresso.onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.typeText(this))
}

fun String.fillPassword() {
    Espresso.onView(ViewMatchers.withId(R.id.inputPassword)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(R.id.inputPassword)).perform(ViewActions.typeText(this))
}
fun String.fillNewPassword() {
    Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(R.id.inputNewPassword)).perform(ViewActions.typeText(this))
}
fun String.fillConfirmPassword() {
    Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(R.id.inputConfirmPassword)).perform(ViewActions.typeText(this))
}

fun String.fillPhoneNumber() {
    Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(R.id.inputPhoneNumber)).perform(ViewActions.typeText(this))
}

fun String.fillOtp() {
    Espresso.onView(ViewMatchers.withId(R.id.otp_view)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(R.id.otp_view)).perform(ViewActions.typeText(this))
}

fun String.fillText(viewId: Int) {
    Espresso.onView(ViewMatchers.withId(viewId)).perform(ViewActions.clearText())
    Espresso.onView(ViewMatchers.withId(viewId)).perform(ViewActions.typeText(this))
}

class Util {

    companion object {
        fun waitThread(seconds: Int) {
            Thread.sleep(seconds * 1000L)
        }
        fun setEmail(value:String){
            Espresso.onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.replaceText(value))
        }

    }
}

