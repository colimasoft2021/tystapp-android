package com.app.tyst.ui.authentication.forgotpasswordwithemail

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.app.tyst.R
import com.app.tyst.ui.authentication.forgotpassword.forgotpasswordwithemail.ForgotPasswordWithEmailActivity
import com.app.tyst.util.*
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ForgotPasswordWithEmailActivityTest {
    private val TIMER_SLEEP_1: Int = 1
    @get:Rule
    val forgotPasswordWithemailActivityRule = ActivityTestRule<ForgotPasswordWithEmailActivity>(ForgotPasswordWithEmailActivity::class.java, false, true)
    private val resources = ApplicationProvider.getApplicationContext<Context>().resources
    var mIdlingResource: RemoteIdlingResource? = null
    var activity: ForgotPasswordWithEmailActivity? = null

    companion object {
        private val TAG: String = "ForgotPasswordWithEmailActivityTest"
    }

    @Before
    fun init() {
        activity = forgotPasswordWithemailActivityRule.activity
        mIdlingResource = activity?.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

    //    @Test
    fun forgotPasswordWithEmailScreen_Test() {
        checkEmailAddress()
        check_forgotPasswordWithEmailTest()
    }

    /**
     * Test case for test email address
     */
//    @Test
    fun checkEmailAddress() {
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyEmailTest()
        check_invalidEmailTest()
        check_validEmailTest()
    }

    @Test
    fun check_emptyEmailTest() {
        "".fillEmail()
        capture(forgotPasswordWithemailActivityRule.activity, "Initial_setup_empty_email", TAG, "check_emptyEmailTest")
        onView(ViewMatchers.withId(R.id.btnSend)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_email))).check(matches(ViewMatchers.isDisplayed()))
        capture(forgotPasswordWithemailActivityRule.activity, "After_empty_email_test", TAG, "check_emptyEmailTest")
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
    }

    @Test
    fun check_invalidEmailTest() {
        "citmotog@gmail@.com".fillEmail()
        capture(forgotPasswordWithemailActivityRule.activity, "Initial_setup_invalid_email", TAG, "check_invalidEmailTest")
        onView(ViewMatchers.withId(R.id.btnSend)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_valid_email))).check(matches(ViewMatchers.isDisplayed()))
        capture(forgotPasswordWithemailActivityRule.activity, "After_invalid_email_test", TAG, "check_invalidEmailTest")
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
    }

    fun check_validEmailTest() {
        "citmotog@gmail.com".fillEmail()
        capture(forgotPasswordWithemailActivityRule.activity, "Initial_setup_valid_email", TAG, "check_validEmailTest")
        capture(forgotPasswordWithemailActivityRule.activity, "After_valid_email_test", TAG, "check_validEmailTest")
    }

    fun fillInputs() {
        Util.waitThread(TIMER_SLEEP_1)
        onView(ViewMatchers.withId(R.id.inputEmail)).perform(ViewActions.scrollTo())
        "rahul.shingane@hiddenbrains.in".fillEmail()
    }

    @Test
    fun check_forgotPasswordWithEmailTest() {
        fillInputs()
        capture(forgotPasswordWithemailActivityRule.activity, "Initial_setup_ForGotPasswordEmail", TAG, "check_forgotPasswordWithEmailTest")
        onView(ViewMatchers.withId(R.id.btnSend)).perform(ViewActions.scrollTo(), ViewActions.click())
        onView(ViewMatchers.withText("Forgot password email sent to 'rahul.shingane@hiddenbrains.in' successfully. Please check your email.")).check(matches(ViewMatchers.isDisplayed()))
        capture(forgotPasswordWithemailActivityRule.activity, "After_ForgotPasswordWithEmail", TAG, "check_forgotPasswordWithEmailTest")
    }

    fun finishActivity() {
        forgotPasswordWithemailActivityRule.activity.finish()
    }

}