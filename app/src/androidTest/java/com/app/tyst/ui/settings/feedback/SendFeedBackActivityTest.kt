package com.app.tyst.ui.settings.feedback

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.app.tyst.R
import com.app.tyst.ui.core.BaseTest
import com.app.tyst.util.Util
import com.app.tyst.util.capture
import com.app.tyst.util.fillText
import com.app.tyst.utility.RemoteIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

class SendFeedBackActivityTest : BaseTest() {

    companion object {
        private val TAG: String = "SendFeedBackActivityTest"
    }

    private val TIMER_SLEEP_1: Int = 1
    private val TIMER_SLEEP_2: Int = 3

    @get:Rule
    val activityRule = ActivityTestRule<SendFeedbackActivity>(SendFeedbackActivity::class.java)

    @JvmField
    @get:Rule
    val ruleChain: RuleChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE))
            .around(activityRule)

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources
    var mIdlingResource: RemoteIdlingResource? = null
    var activity: SendFeedbackActivity? = null

    @Before
    fun init() {
        activity = activityRule.activity
        mIdlingResource = activity?.getIdlingResource()
        IdlingRegistry.getInstance().register(mIdlingResource)
        Intents.init()
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(mIdlingResource)
        Intents.release()
    }

    @Test
    fun sendFeedBackScreen_Test() {
        check_sendFeedBack()

        check_sendFeedBackTest()
    }

    /**
     * Test case for test new password
     */
     fun check_sendFeedBack() {
        Util.waitThread(TIMER_SLEEP_1)
        Espresso.onView(ViewMatchers.withId(R.id.inputBrief)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
        check_emptyFeedBackTest()
        check_validFeedBackTest()
    }

     fun check_emptyFeedBackTest() {
        "".fillText(R.id.inputBrief)
        capture(activityRule.activity, "Initial_setup_empty_feedBack", TAG, "check_emptyFeedBackTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnSend)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText(resources.getString(R.string.alert_enter_brief))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_empty_feedBack", TAG, "check_emptyFeedBackTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputBrief)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
    }

     fun check_validFeedBackTest() {
        "Abc@123".fillText(R.id.inputBrief)
        capture(activityRule.activity, "Initial_setup_valid_empty_feedBack", TAG, "check_validFeedBackTest")
        capture(activityRule.activity, "After_valid_empty_feedBack_test", TAG, "check_validFeedBackTest")
        Espresso.onView(ViewMatchers.withId(R.id.inputBrief)).perform(ViewActions.scrollTo())
        Util.waitThread(TIMER_SLEEP_1)
    }

     fun check_sendFeedBackTest(){
        Espresso.onView(ViewMatchers.withId(R.id.inputBrief)).perform(ViewActions.scrollTo())
        "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s".fillText(R.id.inputBrief)
         capture(activityRule.activity, "Initial_setup_invalid_feedBack", TAG, "check_invalidFeedBackTest")
        Espresso.onView(ViewMatchers.withId(R.id.btnSend)).perform(ViewActions.scrollTo(), ViewActions.click())
        Espresso.onView(ViewMatchers.withText("You have successfully posted your feedback.")).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        capture(activityRule.activity, "After_invalid_feedBack", TAG, "check_invalidFeedBackTest")
        Util.waitThread(TIMER_SLEEP_2)
    }

}